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
import org.ldp4j.application.data.constraints.Constraints.InversePropertyConstraint;
import org.ldp4j.application.data.constraints.Constraints.PropertyConstraint;
import org.ldp4j.application.data.constraints.Constraints.Shape;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.application.domain.RDFS;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.context.HttpRequest.Header;
import org.ldp4j.application.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

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

	public static ConstraintReportTransformer create(Resource resource, ConstraintReport report) {
		return new ConstraintReportTransformer(resource, report);
	}

	public DataSet transform(Endpoint endpoint) {
		populateResourceIndividual(endpoint);
		populateConstraintReportIndividual();
		populateHttpRequestIndividual(this.report.getRequest());
		return dataSet();
	}

	private void populateResourceIndividual(Endpoint endpoint) {
		resourceInd().addValue(URI.create("http://www.ldp4j.org/vocab#entityTag"), DataSetUtils.newLiteral(endpoint.entityTag()));
		resourceInd().addValue(URI.create("http://www.ldp4j.org/vocab#lastModified"), DataSetUtils.newLiteral(endpoint.lastModified()));
		resourceInd().addValue(URI.create("http://www.w3.org/ns/ldp#constrainedBy"), reportInd());
	}

	private void populateHttpRequestIndividual(HttpRequest request) {
		requestInd().addValue(RDF.TYPE.as(URI.class), externalIndividual(httpTerm("Request")));
		requestInd().addValue(httpTerm("methodName"),DataSetUtils.newLiteral(request.method().toString()));
		requestInd().addValue(httpTerm("mthd"),externalIndividual(methodsTerm(request.method().toString())));
		requestInd().addValue(httpTerm("absolutePath"),DataSetUtils.newLiteral(request.absolutePath()));
		requestInd().addValue(httpTerm("httpVersion"),DataSetUtils.newLiteral("1.1"));

		Date clientDate = request.clientDate();
		if(clientDate!=null) {
			requestInd().addValue(dctTerm("date"),DataSetUtils.newLiteral(clientDate));
		}

		Iterator<Header> headers = request.headers().iterator();
		if(headers.hasNext()) {
			int i=0;
			Individual<?,?> current=localIndividual("n"+i);
			requestInd().addValue(httpTerm("headers"), current);
			while(headers.hasNext()) {
				Header header=headers.next();
				LocalIndividual headerInd=localIndividual("header"+i);
				current.addValue(RDF.FIRST.as(URI.class),headerInd);
				headerInd.addValue(RDF.TYPE.as(URI.class),headerType(header));
				headerInd.addValue(httpTerm("fieldName"),DataSetUtils.newLiteral(header.name()));
				headerInd.addValue(httpTerm("fieldValue"),DataSetUtils.newLiteral(header.rawValue()));
				headerInd.addValue(httpTerm("hdrName"),externalIndividual(headersTerm(header.name())));
				i++;
				if(headers.hasNext()) {
					Individual<?,?> last=current;
					current=localIndividual("n"+i);
					last.addValue(RDF.REST.as(URI.class),current);
				} else {
					current.addValue(RDF.REST.as(URI.class),externalIndividual(RDF.NIL.as(URI.class)));
				}
			}
		}

		String body = request.body();
		if(body!=null) {
			LocalIndividual bodyInd=localIndividual("body");
			requestInd().addValue(httpTerm("body"), bodyInd);
			bodyInd.addValue(RDF.TYPE.as(URI.class),externalIndividual(cntTerm("ContentAsText")));
			// TODO: How do we guess the encoding now?
			bodyInd.addValue(cntTerm("characterEncoding"),DataSetUtils.newLiteral("UTF-8"));
			bodyInd.addValue(cntTerm("chars"),DataSetUtils.newLiteral(body));
		}
	}

	private void populateConstraintReportIndividual() {
		Constraints constraints = this.report.getConstraints();
		LOGGER.debug("Populating constraints: {}",constraints);
		reportInd().addValue(RDF.TYPE.as(URI.class), dataset.individual(URI.create("http://www.ldp4j.org/vocab#ConstraintReport"), ExternalIndividual.class));
		reportInd().addValue(URI.create("http://www.ldp4j.org/vocab#failureId"), DataSetUtils.newLiteral(this.report.id().constraintsId()));
		reportInd().addValue(URI.create("http://www.ldp4j.org/vocab#failureDate"), DataSetUtils.newLiteral(this.report.getDate()));
		reportInd().addValue(URI.create("http://www.ldp4j.org/vocab#failureRequest"), requestInd());

		List<Shape> shapes = constraints.shapes();
		if(!shapes.isEmpty()) {
			final IndividualTranslator translator = new IndividualTranslator();
			IdentityHashMap<Shape,LocalIndividual> shapeIndCache=Maps.newIdentityHashMap();
			for(int i=0;i<shapes.size();i++) {
				Shape shape=shapes.get(i);
				LocalIndividual shapeInd = localIndividual("s"+i);
				shapeIndCache.put(shape, shapeInd);
				reportInd().addValue(URI.create("http://www.ldp4j.org/vocab#hasShape"), shapeInd);
				shapeInd.addValue(RDF.TYPE.as(URI.class), externalIndividual(shaclTerm("Shape")));
				if(shape.label()!=null) {
					shapeInd.addValue(RDFS.LABEL.as(URI.class),DataSetUtils.newLiteral(shape.label()));
				}
				if(shape.comment()!=null) {
					shapeInd.addValue(RDFS.COMMENT.as(URI.class),DataSetUtils.newLiteral(shape.comment()));
				}
				List<PropertyConstraint> pcs = shape.propertyConstraints();
				for(int j=0;j<pcs.size();j++) {
					PropertyConstraint pc = pcs.get(j);
					final LocalIndividual pcInd = localIndividual("s"+i+"_pc"+j);
					shapeInd.addValue(shaclTerm("property"), pcInd);
					pcInd.addValue(RDF.TYPE.as(URI.class), externalIndividual(shaclTerm("PropertyConstraint")));
					if(pc.label()!=null) {
						pcInd.addValue(RDFS.LABEL.as(URI.class),DataSetUtils.newLiteral(pc.label()));
					}
					if(pc.comment()!=null) {
						pcInd.addValue(RDFS.COMMENT.as(URI.class),DataSetUtils.newLiteral(pc.comment()));
					}
					pcInd.addValue(shaclTerm("predicate"), externalIndividual(pc.predicate()));

					Cardinality cardinality = pc.cardinality();
					if(cardinality.min()>0) {
						pcInd.addValue(shaclTerm("minCard"), DataSetUtils.newLiteral(cardinality.min()));
					}
					if(cardinality.max()>=0) {
						pcInd.addValue(shaclTerm("maxCard"), DataSetUtils.newLiteral(cardinality.max()));
					}

					String nodeKind=null;
					switch(pc.nodeKind()) {
					case BLANK_NODE:
						nodeKind="BlankNode";
						break;
					case BLANK_NODE_OR_IRI:
						nodeKind="BlankNodeOrIRI";
						break;
					case BLANK_NODE_OR_LITERAL:
						nodeKind="BlankNodeOrLiteral";
						break;
					case IRI:
						nodeKind="IRI";
						break;
					case LITERAL:
						nodeKind="Literal";
						break;
					case LITERAL_OR_IRI:
						nodeKind="LiteralOrIRI";
						break;
					case NODE:
						nodeKind="Node";
						break;
					default:
						throw new IllegalStateException("Unknown node kind "+pc.nodeKind());
					}
					pcInd.addValue(shaclTerm("nodeKind"), externalIndividual(shaclTerm(nodeKind)));
					for(Value value:pc.values()) {
						value.accept(
							new ValueVisitor() {
								@Override
								public void visitLiteral(Literal<?> value) {
									pcInd.addValue(shaclTerm("hasValue"),value);
								}
								@Override
								public void visitIndividual(Individual<?, ?> value) {
									Individual<?, ?> individual = translator.translate(value);
									pcInd.addValue(shaclTerm("hasValue"),individual);
								}
							}
						);
					}
				}
				List<InversePropertyConstraint> ipcs = shape.inversePropertyConstraints();
				for(int j=0;j<ipcs.size();j++) {
					InversePropertyConstraint ipc = ipcs.get(j);
					LocalIndividual ipcInd = localIndividual("s"+i+"_ipc"+j);
					shapeInd.addValue(shaclTerm("property"), ipcInd);
					ipcInd.addValue(RDF.TYPE.as(URI.class), externalIndividual(shaclTerm("InversePropertyConstraint")));
					if(ipc.label()!=null) {
						ipcInd.addValue(RDFS.LABEL.as(URI.class),DataSetUtils.newLiteral(ipc.label()));
					}
					if(ipc.comment()!=null) {
						ipcInd.addValue(RDFS.COMMENT.as(URI.class),DataSetUtils.newLiteral(ipc.comment()));
					}
					ipcInd.addValue(shaclTerm("predicate"), externalIndividual(ipc.predicate()));
					Cardinality cardinality = ipc.cardinality();
					if(cardinality.min()>0) {
						ipcInd.addValue(shaclTerm("minCard"), DataSetUtils.newLiteral(cardinality.min()));
					}
					if(cardinality.max()>=0) {
						ipcInd.addValue(shaclTerm("maxCard"), DataSetUtils.newLiteral(cardinality.max()));
					}

					String nodeKind=null;
					switch(ipc.nodeKind()) {
					case BLANK_NODE:
						nodeKind="BlankNode";
						break;
					case BLANK_NODE_OR_IRI:
						nodeKind="BlankNodeOrIRI";
						break;
					case BLANK_NODE_OR_LITERAL:
						nodeKind="BlankNodeOrLiteral";
						break;
					case IRI:
						nodeKind="IRI";
						break;
					case LITERAL:
						nodeKind="Literal";
						break;
					case LITERAL_OR_IRI:
						nodeKind="LiteralOrIRI";
						break;
					case NODE:
						nodeKind="Node";
						break;
					default:
						throw new IllegalStateException("Unknown node kind "+ipc.nodeKind());
					}
					ipcInd.addValue(shaclTerm("nodeKind"), externalIndividual(shaclTerm(nodeKind)));
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

	@SuppressWarnings("rawtypes")
	private LocalIndividual localIndividual(String string) {
		return this.dataset.individual((Name)NamingScheme.getDefault().name(string), LocalIndividual.class);
	}

	private ExternalIndividual headerType(Header header) {
		return externalIndividual(httpTerm(getHeaderType(header.name())));
	}

	private URI methodsTerm(String term) {
		return URI.create("http://www.w3.org/2011/http-methods#"+term);
	}

	private URI shaclTerm(String term) {
		return URI.create("http://www.w3.org/ns/shacl#"+term);
	}

	private static final ImmutableList<String> GENERAL_HEADERS=
			ImmutableList.
				<String>builder().
					add("Cache-Control").
					add("Connection").
					add("Date").
					add("Pragma").
					add("Trailer").
					add("Transfer-Encoding").
					add("Upgrade").
					add("Via").
					add("Warning").
					build();

	private static final ImmutableList<String> REQUEST_HEADERS=
			ImmutableList.
				<String>builder().
					add("Accept").
					add("Accept-Charset").
					add("Accept-Encoding").
					add("Accept-Language").
					add("Authorization").
					add("Expect").
					add("From").
					add("Host").
					add("If-Match").
					add("If-Modified-Since").
					add("If-None-Match").
					add("If-Range").
					add("If-Unmodified-Since").
					add("Max-Forwards").
					add("Proxy-Authorization").
					add("Range").
					add("Referer").
					add("TE").
					add("User-Agent").
					build();

	private static final ImmutableList<String> ENTITY_HEADERS=
			ImmutableList.
				<String>builder().
					add("Allow").
					add("Content-Encoding").
					add("Content-Language").
					add("Content-Length").
					add("Content-Location").
					add("Content-MD5").
					add("Content-Range").
					add("Content-Type").
					add("Expires").
					add("Last-Modified").
					build();

	private static String getHeaderType(String name) {
		if(matches(name, GENERAL_HEADERS)) {
			return "GeneralHeader";
		} else if(matches(name, REQUEST_HEADERS)) {
			return "RequestHeader";
		} else if(matches(name, ENTITY_HEADERS)) {
			return "EntityHeader";
		}
		return "MessageHeader";
	}

	private static boolean matches(String name, List<String> headers) {
		boolean result=false;
		for(String header:headers) {
			if(header.equalsIgnoreCase(name)) {
				result=true;
				break;
			}
		}
		return result;
	}

	private ExternalIndividual externalIndividual(URI term) {
		return this.dataset.individual(term, ExternalIndividual.class);
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

	private static URI headersTerm(String term) {
		return URI.create("http://www.w3.org/2011/http-headers#"+term.toLowerCase());
	}
}