/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.constraints;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.IndividualVisitor;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.Literals;
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
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.context.HttpRequest.Header;
import org.ldp4j.application.engine.context.HttpRequest.ProtocolVersion;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.vocabulary.RDF;
import org.ldp4j.application.vocabulary.RDFS;
import org.ldp4j.application.vocabulary.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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

		private final Map<Individual<?, ?>, Individual<?, ?>> individualCache;

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

		@SuppressWarnings("unchecked")
		<T extends Serializable, S extends Individual<T,S>> Individual<T,S> translate(Individual<T,S> individual) {
			Individual<?, ?> aNodeInd = this.individualCache.get(individual);
			if(aNodeInd==null) {
				individual.accept(this);
				aNodeInd=this.individualCache.get(individual);
			}
			return (Individual<T,S>)aNodeInd;
		}
	}

	private static class CacheEntry {

		private final LocalIndividual individual;
		private final String id;
		private boolean populated;

		private CacheEntry(String id, LocalIndividual individual) {
			this.id = id;
			this.individual = individual;
		}

		boolean isPopulated() {
			return this.populated;
		}

		void populate() {
			this.populated=true;
		}

		LocalIndividual individual() {
			return this.individual;
		}

		String id() {
			return this.id;
		}

	}

	private class ShapeIndividualCache {

		private final Map<Shape,CacheEntry> cache;

		private int shapeCount;

		private ShapeIndividualCache() {
			this.cache=Maps.newIdentityHashMap();
		}

		private CacheEntry getOrCreate(Shape shape) {
			CacheEntry entry=this.cache.get(shape);
			if(entry==null) {
				String nextId = "s"+(shapeCount++);
				entry=new CacheEntry(nextId, localIndividual(nextId));
				this.cache.put(shape, entry);
			}
			return entry;
		}

		String id(Shape shape) {
			return getOrCreate(shape).id();
		}

		LocalIndividual individual(Shape shape) {
			return getOrCreate(shape).individual();
		}

		public boolean isPopulated(Shape shape) {
			return getOrCreate(shape).isPopulated();
		}

		public void populate(Shape shape) {
			getOrCreate(shape).populate();
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
		ManagedIndividualId rId = ManagedIndividualId.createId(URI.create("?ldp:constrainedBy="+report.id().failureId()), tId);
		this.dataset=DataSets.createDataSet(rId.name());
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
		String protocolVersion="1.1";
		if(!ProtocolVersion.HTTP_1_1.equals(request.protocolVersion())) {
			protocolVersion="1.0";
		}
		requestInd().addValue(httpTerm("httpVersion"),literal(protocolVersion));

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
		reportInd().addValue(ldp4jTerm("failureId"), literal(this.report.id().failureId()));
		reportInd().addValue(ldp4jTerm("failureDate"), literal(this.report.getDate()));
		reportInd().addValue(ldp4jTerm("failureRequest"), requestInd());

		IndividualTranslator translator = new IndividualTranslator();
		ShapeIndividualCache shapeCache=new ShapeIndividualCache();

		Queue<Shape> pendingShapes=Lists.newLinkedList(constraints.shapes());
		while(!pendingShapes.isEmpty()) {
			Shape shape=pendingShapes.poll();
			if(!shapeCache.isPopulated(shape)) {
				shapeCache.populate(shape);
				Set<Shape> foundShapes = populateShapeDefinition(shape, shapeCache, translator);
				pendingShapes.addAll(foundShapes);
			}
		}

		for(URI type:constraints.types()) {
			Individual<?,?> typeInd=externalIndividual(type);
			typeInd.addValue(shaclTerm("typeShape"), shapeCache.individual(constraints.typeShape(type)));
		}
		for(Individual<?, ?> node:constraints.nodes(dataset)) {
			Individual<?, ?> individual = translator.translate(node);
			individual.addValue(shaclTerm("nodeShape"), shapeCache.individual(constraints.nodeShape(node)));
		}


	}

	private Set<Shape> populateShapeDefinition(Shape shape, ShapeIndividualCache cache, IndividualTranslator translator) {
		LocalIndividual shapeInd = cache.individual(shape);
		String shapeId = cache.id(shape);
		reportInd().addValue(ldp4jTerm("hasShape"), shapeInd);
		shapeInd.addValue(vocabularyTerm(RDF.TYPE), externalIndividual(shaclTerm("Shape")));
		populateDescription(shapeInd, shape);
		List<PropertyConstraint> pcs = shape.propertyConstraints();
		Set<Shape> shapes=Sets.newLinkedHashSet();
		for(int j=0;j<pcs.size();j++) {
			PropertyConstraint pc = pcs.get(j);
			LocalIndividual pcInd = localIndividual(shapeId+"_pc"+j);
			shapeInd.addValue(shaclTerm("property"), pcInd);
			pcInd.addValue(vocabularyTerm(RDF.TYPE), externalIndividual(shaclTerm("PropertyConstraint")));
			pcInd.addValue(shaclTerm("predicate"), externalIndividual(pc.predicate()));
			populateDescription(pcInd, pc);
			populateCardinality(pcInd, pc.cardinality());
			populateNodeKind(pcInd, pc.nodeKind());
			populateHasValues(pcInd, pc.literals(), translator);
			populateHasValues(pcInd, pc.individuals(dataset), translator);
			Set<Value> allowedValues=Sets.newHashSet();
			allowedValues.addAll(pc.allowedLiterals());
			allowedValues.addAll(pc.allowedIndividuals(dataset));
			populateAllowedValues(pcInd, allowedValues, translator);
			populateValueType(pcInd,pc.valueType());
			populateDatatype(pcInd,pc.datatype());
			Shape valueShape = pc.valueShape();
			if(valueShape!=null) {
				populateValueShape(pcInd,cache.individual(valueShape));
				shapes.add(valueShape);
			}
		}
		List<InversePropertyConstraint> ipcs = shape.inversePropertyConstraints();
		for(int j=0;j<ipcs.size();j++) {
			InversePropertyConstraint ipc = ipcs.get(j);
			LocalIndividual ipcInd = localIndividual(shapeId+"_ipc"+j);
			shapeInd.addValue(shaclTerm("property"), ipcInd);
			ipcInd.addValue(vocabularyTerm(RDF.TYPE), externalIndividual(shaclTerm("InversePropertyConstraint")));
			ipcInd.addValue(shaclTerm("predicate"), externalIndividual(ipc.predicate()));
			populateDescription(ipcInd, ipc);
			populateCardinality(ipcInd, ipc.cardinality());
			populateNodeKind(ipcInd, ipc.nodeKind());
			populateHasValues(ipcInd, ipc.literals(), translator);
			populateHasValues(ipcInd, ipc.individuals(dataset), translator);
			Set<Value> allowedValues=Sets.newHashSet();
			allowedValues.addAll(ipc.allowedLiterals());
			allowedValues.addAll(ipc.allowedIndividuals(dataset));
			populateAllowedValues(ipcInd, allowedValues, translator);
			populateValueType(ipcInd,ipc.valueType());
			populateDatatype(ipcInd,ipc.datatype());
			Shape valueShape = ipc.valueShape();
			if(valueShape!=null) {
				populateValueShape(ipcInd,cache.individual(valueShape));
				shapes.add(valueShape);
			}
		}
		return shapes;
	}

	private void populateDatatype(LocalIndividual constraint, URI datatype) {
		if(datatype!=null) {
			constraint.addValue(shaclTerm("datatype"),externalIndividual(datatype));
		}
	}

	private void populateValueShape(LocalIndividual constraint, LocalIndividual shape) {
		if(shape!=null) {
			constraint.addValue(shaclTerm("valueShape"),shape);
		}
	}

	private void populateValueType(LocalIndividual constraint, URI valueType) {
		if(valueType!=null) {
			constraint.addValue(shaclTerm("valueType"),externalIndividual(valueType));
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
			individual.addValue(shaclTerm("minCount"), typedLiteral(cardinality.min(),"integer"));
		}
		if(cardinality.max()>=0) {
			individual.addValue(shaclTerm("maxCount"), typedLiteral(cardinality.max(),"integer"));
		}
	}

	private void populateNodeKind(Individual<?,?> individual, NodeKind nodeKind) {
		if(nodeKind!=null) {
			individual.addValue(shaclTerm("nodeKind"), externalIndividual(shaclTerm(nodeKind.localName())));
		}
	}

	private void populateHasValues(final Individual<?,?> individual, List<? extends Value> values, final IndividualTranslator translator) {
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

	private void populateAllowedValues(LocalIndividual individual, Set<Value> allowedValues, final IndividualTranslator translator) {
		class ValueAdapter implements ValueVisitor {
			private Value value=null;

			@Override
			public void visitLiteral(Literal<?> value) {
				this.value=value;
			}
			@Override
			public void visitIndividual(Individual<?, ?> value) {
				this.value=translator.translate(value);
			}
		}
		ValueAdapter adapter=new ValueAdapter();
		Iterator<Value> values = allowedValues.iterator();
		if(values.hasNext()) {
			int i=0;
			String prefix = individual.id().id()+"_allowedValues_";
			Individual<?,?> current=localIndividual(prefix+i);
			individual.addValue(shaclTerm("allowedValues"), current);
			while(values.hasNext()) {
				Value value=values.next();
				value.accept(adapter);
				current.addValue(vocabularyTerm(RDF.FIRST),adapter.value);
				if(values.hasNext()) {
					Individual<?,?> last=current;
					current=localIndividual(prefix+(++i));
					last.addValue(vocabularyTerm(RDF.REST),current);
				} else {
					current.
						addValue(
							vocabularyTerm(RDF.REST),
							externalIndividual(RDF.NIL.as(URI.class)));
				}
			}
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

	private static Literal<?> literal(Object value) {
		return Literals.newLiteral(value);
	}

	private static Literal<?> typedLiteral(Object value, String type) {
		return Literals.newTypedLiteral(value, URI.create("http://www.w3.org/2001/XMLSchema#"+type));
	}

	private static URI vocabularyTerm(Term term) {
		return term.as(URI.class);
	}

	private static URI ldp4jTerm(String term) {
		return URI.create("http://www.ldp4j.org/ns/constraints#"+term);
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