/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.constraints;

import java.net.URI;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.IndividualVisitor;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.data.NewIndividual;
import org.ldp4j.application.data.RelativeIndividual;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.ValueVisitor;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.data.constraints.Constraints.Cardinality;
import org.ldp4j.application.data.constraints.Constraints.Describable;
import org.ldp4j.application.data.constraints.Constraints.InversePropertyConstraint;
import org.ldp4j.application.data.constraints.Constraints.NodeKind;
import org.ldp4j.application.data.constraints.Constraints.PropertyConstraint;
import org.ldp4j.application.data.constraints.Constraints.Shape;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.application.domain.RDFS;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.context.HttpRequest.Header;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.vocabulary.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.Maps;

/**
 * The current implementation has the following caveats:
 * <ol>
 * <li>Blank nodes referenced in the constraints may not show up in the body of
 * the failed request, as the body includes the raw representation.
 * <li>The URIs for the managed, relative and new individuals will be resolved
 * using as base the URI specified in the request for the retrieval of the
 * constraints report.
 * <li>There's no way yet for specifying the character encoding of the body
 * </ol>
 *
 * @author Miguel Esteban Guti&eacute;rrez
 */
public final class ConstraintReportTransformer {

	private final class IndividualTranslator implements IndividualVisitor {

		private final IdentityHashMap<Individual<?, ?>, Individual<?, ?>> individualCache;

		private IndividualTranslator() {
			this.individualCache=Maps.newIdentityHashMap();
		}

		@Override
		public void visitRelativeIndividual(RelativeIndividual individual) {
			individualCache.put(individual, dataset.individual(individual.id(),RelativeIndividual.class));
		}

		@Override
		public void visitNewIndividual(NewIndividual individual) {
			individualCache.put(individual,dataset.individual(individual.id(), NewIndividual.class));
		}

		@Override
		public void visitManagedIndividual(ManagedIndividual individual) {
			individualCache.put(individual, dataset.individual(individual.id(),ManagedIndividual.class));
		}

		@Override
		public void visitLocalIndividual(LocalIndividual individual) {
			individualCache.put(individual, dataset.individual(individual.id(),LocalIndividual.class));
		}

		@Override
		public void visitExternalIndividual(ExternalIndividual individual) {
			individualCache.put(individual, externalIndividual(individual.id()));
		}

		Individual<?,?> translate(Individual<?,?> individual) {
			Individual<?, ?> aNodeInd = this.individualCache.get(individual);
			if(aNodeInd==null) {
				individual.accept(this);
				aNodeInd=this.individualCache.get(individual);
			}
			return aNodeInd;
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ConstraintReportTransformer.class);

	private final ConstraintReport report;

	private final DataSet dataset;

	private final Individual<?,?> targetResource;
	private final Individual<?,?> constraintReport;
	private final Individual<?,?> failedRequest;

	@SuppressWarnings("rawtypes")
	private ConstraintReportTransformer(Resource resource, ConstraintReport report) {
		this.report = report;
		ManagedIndividualId tId = ManagedIndividualId.createId(resource.id().name(), resource.id().templateId());
		ManagedIndividualId rId = ManagedIndividualId.createId(URI.create("?ldp:constrainedBy="+report.id().constraintsId()), tId);
		this.dataset=DataSetFactory.createDataSet(rId.name());
		this.constraintReport= dataset.individual(rId, ManagedIndividual.class);
		this.targetResource = dataset.individual(tId, ManagedIndividual.class);
		this.failedRequest=dataset.individual((Name)NamingScheme.getDefault().name("request"), LocalIndividual.class);
	}

	private Individual<?,?> resourceInd() {
		return this.targetResource;
	}

	private Individual<?,?> reportInd() {
		return this.constraintReport;
	}

	private Individual<?,?> requestInd() {
		return this.failedRequest;
	}

	private DataSet dataSet() {
		return this.dataset;
	}

	private void populateResourceIndividual(Endpoint endpoint) {
		resourceInd().addValue(ldp4jTerm("entityTag"), literal(endpoint.entityTag()));
		resourceInd().addValue(ldp4jTerm("lastModified"), literal(endpoint.lastModified()));
		resourceInd().addValue(ldpTerm("constrainedBy"), reportInd());
	}

	private void populateHttpRequestIndividual(HttpRequest request) {
		requestInd().addValue(vocabularyTerm(RDF.TYPE), externalIndividual(httpTerm("Request")));
		requestInd().addValue(httpTerm("methodName"),literal(request.method().toString()));
		requestInd().addValue(httpTerm("mthd"),externalIndividual(methodsTerm(request.method().toString())));
		requestInd().addValue(httpTerm("absolutePath"),literal(request.absolutePath()));
		requestInd().addValue(httpTerm("httpVersion"),literal("1.1"));

		Date clientDate = request.clientDate();
		if(clientDate!=null) {
			requestInd().addValue(dctTerm("date"),literal(clientDate));
		}

		Iterator<Header> headers = request.headers().iterator();
		if(headers.hasNext()) {
			int i=0;
			Individual<?,?> current=localIndividual("n"+i);
			requestInd().addValue(httpTerm("headers"), current);
			while(headers.hasNext()) {
				Header header=headers.next();
				LocalIndividual headerInd=localIndividual("header"+i);
				current.addValue(vocabularyTerm(RDF.FIRST),headerInd);
				headerInd.addValue(vocabularyTerm(RDF.TYPE),headerType(header));
				headerInd.addValue(httpTerm("fieldName"),literal(header.name()));
				headerInd.addValue(httpTerm("fieldValue"),literal(header.rawValue()));
				headerInd.addValue(httpTerm("hdrName"),externalIndividual(headersTerm(header.name())));
				i++;
				if(headers.hasNext()) {
					Individual<?,?> last=current;
					current=localIndividual("n"+i);
					last.addValue(vocabularyTerm(RDF.REST),current);
				} else {
					current.addValue(vocabularyTerm(RDF.REST),externalIndividual(RDF.NIL.as(URI.class)));
				}
			}
		}

		String body = request.body();
		if(body!=null) {
			LocalIndividual bodyInd=localIndividual("body");
			requestInd().addValue(httpTerm("body"), bodyInd);
			bodyInd.addValue(vocabularyTerm(RDF.TYPE),externalIndividual(cntTerm("ContentAsText")));
			// TODO: How do we guess the encoding now?
			bodyInd.addValue(cntTerm("characterEncoding"),literal("UTF-8"));
			bodyInd.addValue(cntTerm("chars"),literal(body));
		}
	}

	private void populateConstraintReportIndividual() {
		Constraints constraints = this.report.getConstraints();
		LOGGER.debug("Populating constraints: {}",constraints);
		reportInd().addValue(vocabularyTerm(RDF.TYPE), externalIndividual(ldp4jTerm("ConstraintReport")));
		reportInd().addValue(ldp4jTerm("failureId"), literal(this.report.id().constraintsId()));
		reportInd().addValue(ldp4jTerm("failureDate"), literal(this.report.getDate()));
		reportInd().addValue(ldp4jTerm("failureRequest"), requestInd());

		List<Shape> shapes = constraints.shapes();
		if(!shapes.isEmpty()) {
			final IndividualTranslator translator = new IndividualTranslator();
			IdentityHashMap<Shape,LocalIndividual> shapeIndCache=Maps.newIdentityHashMap();
			for(int i=0;i<shapes.size();i++) {
				Shape shape=shapes.get(i);
				LocalIndividual shapeInd = localIndividual("s"+i);
				shapeIndCache.put(shape, shapeInd);
				reportInd().addValue(ldp4jTerm("hasShape"), shapeInd);
				shapeInd.addValue(vocabularyTerm(RDF.TYPE), externalIndividual(shaclTerm("Shape")));
				populateDescription(shapeInd, shape);
				List<PropertyConstraint> pcs = shape.propertyConstraints();
				for(int j=0;j<pcs.size();j++) {
					PropertyConstraint pc = pcs.get(j);
					LocalIndividual pcInd = localIndividual("s"+i+"_pc"+j);
					shapeInd.addValue(shaclTerm("property"), pcInd);
					pcInd.addValue(vocabularyTerm(RDF.TYPE), externalIndividual(shaclTerm("PropertyConstraint")));
					pcInd.addValue(shaclTerm("predicate"), externalIndividual(pc.predicate()));
					populateDescription(pcInd, pc);
					populateCardinality(pcInd, pc.cardinality());
					populateNodeKind(pcInd, pc.nodeKind());
					populateHasValues(pcInd, pc.values(), translator);
				}
				List<InversePropertyConstraint> ipcs = shape.inversePropertyConstraints();
				for(int j=0;j<ipcs.size();j++) {
					InversePropertyConstraint ipc = ipcs.get(j);
					LocalIndividual ipcInd = localIndividual("s"+i+"_ipc"+j);
					shapeInd.addValue(shaclTerm("property"), ipcInd);
					ipcInd.addValue(vocabularyTerm(RDF.TYPE), externalIndividual(shaclTerm("InversePropertyConstraint")));
					ipcInd.addValue(shaclTerm("predicate"), externalIndividual(ipc.predicate()));
					populateDescription(ipcInd, ipc);
					populateCardinality(ipcInd, ipc.cardinality());
					populateNodeKind(ipcInd, ipc.nodeKind());
					populateHasValues(ipcInd, ipc.values(), translator);
				}
			}
			for(URI type:constraints.types()) {
				Individual<?,?> typeInd=externalIndividual(type);
				typeInd.addValue(shaclTerm("typeShape"), shapeIndCache.get(constraints.typeShape(type)));
			}
			for(Individual<?, ?> node:constraints.nodes()) {
				Individual<?, ?> individual = translator.translate(node);
				individual.addValue(shaclTerm("nodeShape"), shapeIndCache.get(constraints.nodeShape(node)));
			}
		}
	}

	private void populateDescription(Individual<?,?> individual, Describable describable) {
		if(describable.label()!=null) {
			individual.addValue(vocabularyTerm(RDFS.LABEL),literal(describable.label()));
		}
		if(describable.comment()!=null) {
			individual.addValue(vocabularyTerm(RDFS.COMMENT),literal(describable.comment()));
		}
	}

	private void populateCardinality(Individual<?,?> individual, Cardinality cardinality) {
		if(cardinality.min()>0) {
			individual.addValue(shaclTerm("minCard"), typedLiteral(cardinality.min(),"integer"));
		}
		if(cardinality.max()>=0) {
			individual.addValue(shaclTerm("maxCard"), typedLiteral(cardinality.max(),"integer"));
		}
	}

	private void populateNodeKind(Individual<?,?> individual, NodeKind nodeKind) {
		if(nodeKind!=null) {
			individual.addValue(shaclTerm("nodeKind"), externalIndividual(shaclTerm(nodeKind.localName())));
		}
	}

	private void populateHasValues(final Individual<?,?> individual, List<Value> values, final IndividualTranslator translator) {
		for(Value value:values) {
			value.accept(
				new ValueVisitor() {
					@Override
					public void visitLiteral(Literal<?> value) {
						individual.addValue(shaclTerm("hasValue"),value);
					}
					@Override
					public void visitIndividual(Individual<?, ?> value) {
						Individual<?, ?> translatedIndividual = translator.translate(value);
						individual.addValue(shaclTerm("hasValue"),translatedIndividual);
					}
				}
			);
		}
	}

	private ExternalIndividual headerType(Header header) {
		return externalIndividual(httpTerm(HeaderHelper.getHeaderType(header.name())));
	}

	@SuppressWarnings("rawtypes")
	private LocalIndividual localIndividual(String string) {
		return this.dataset.individual((Name)NamingScheme.getDefault().name(string), LocalIndividual.class);
	}

	private ExternalIndividual externalIndividual(URI term) {
		return this.dataset.individual(term, ExternalIndividual.class);
	}

	public DataSet transform(Endpoint endpoint) {
		checkNotNull(endpoint,"Endpoint cannot be null");
		populateResourceIndividual(endpoint);
		populateConstraintReportIndividual();
		populateHttpRequestIndividual(this.report.getRequest());
		return dataSet();
	}

	public static ConstraintReportTransformer create(Resource resource, ConstraintReport report) {
		checkNotNull(resource,"Resource cannot be null");
		checkNotNull(report,"Constraint report cannot be null");
		return new ConstraintReportTransformer(resource, report);
	}

	private static <T> Literal<T> literal(T value) {
		return DataSetUtils.newLiteral(value);
	}

	private static <T> Literal<T> typedLiteral(T value, String type) {
		return DataSetUtils.newTypedLiteral(value, URI.create("http://www.w3.org/2001/XMLSchema#"+type));
	}

	private static URI vocabularyTerm(Term term) {
		return term.as(URI.class);
	}

	private static URI ldp4jTerm(String term) {
		return URI.create("http://www.ldp4j.org/vocab#"+term);
	}

	private static URI ldpTerm(String term) {
		return URI.create("http://www.w3.org/ns/ldp#"+term);
	}

	private static URI shaclTerm(String term) {
		return URI.create("http://www.w3.org/ns/shacl#"+term);
	}

	private static URI httpTerm(String term) {
		return URI.create("http://www.w3.org/2011/http#"+term);
	}

	private static URI dctTerm(String term) {
		return URI.create("http://purl.org/dc/terms/"+term);
	}

	private static URI cntTerm(String term) {
		return URI.create("http://www.w3.org/2011/content#"+term);
	}

	private static URI methodsTerm(String term) {
		return URI.create("http://www.w3.org/2011/http-methods#"+term);
	}

	private static URI headersTerm(String term) {
		return URI.create("http://www.w3.org/2011/http-headers#"+term.toLowerCase());
	}
}