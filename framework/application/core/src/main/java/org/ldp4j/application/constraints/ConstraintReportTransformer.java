package org.ldp4j.application.constraints;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.context.HttpRequest.Header;
import org.ldp4j.application.resource.Resource;

import com.google.common.collect.ImmutableList;

public final class ConstraintReportTransformer {

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

		List<Header> headers = request.headers();
		for(int i=0;i<headers.size();i++) {
			Header header=headers.get(i);
			LocalIndividual headerInd=localIndividual("header"+i);
			requestInd().addValue(httpTerm("headers"), headerInd);
			headerInd.addValue(RDF.TYPE.as(URI.class),headerType(header));
			headerInd.addValue(httpTerm("fieldName"),DataSetUtils.newLiteral(header.name()));
			headerInd.addValue(httpTerm("fieldValue"),DataSetUtils.newLiteral(header.rawValue()));
			headerInd.addValue(httpTerm("hdrName"),externalIndividual(headersTerm(header.name())));
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
		reportInd().addValue(RDF.TYPE.as(URI.class), dataset.individual(URI.create("http://www.ldp4j.org/vocab#ConstraintReport"), ExternalIndividual.class));
		reportInd().addValue(URI.create("http://www.ldp4j.org/vocab#failureId"), DataSetUtils.newLiteral(this.report.id().constraintsId()));
		reportInd().addValue(URI.create("http://www.ldp4j.org/vocab#failureDate"), DataSetUtils.newLiteral(this.report.getDate()));
		reportInd().addValue(URI.create("http://www.ldp4j.org/vocab#failureRequest"), requestInd());
		// TODO: Add constraint transformations when content is clear
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