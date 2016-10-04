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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.2
 *   Bundle      : ldp4j-application-examples-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.InvalidQueryException;
import org.ldp4j.application.ext.ObjectTransformationException;
import org.ldp4j.application.ext.Parameter;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.vocabulary.RDF;

final class QuerySupport {

	public static final String NAMESPACE = "http://www.ldp4j.org/examples#";

	public static final URI QUERY_TYPE = URI.create(NAMESPACE+"Query");
	public static final URI PARAMETER_TYPE = URI.create(NAMESPACE+"Parameter");
	public static final URI HAS_PARAMETER = URI.create(NAMESPACE+"hasParameter");
	public static final URI HAS_VALUE = URI.create(NAMESPACE+"hasValue");
	public static final URI HAS_NAME = URI.create(NAMESPACE+"hasName");

	private static final URI TYPE = RDF.TYPE.as(URI.class);

	private QuerySupport() {
	}

	static DataSet getDescription(Name<?> id, Query query) throws InvalidQueryException {
		DataSet dataset = DataSets.createDataSet(id);

		if(query.hasParameter(QueryableResourceHandler.FAILURE)) {
			Parameter failure=query.getParameter(QueryableResourceHandler.FAILURE);
			try {
				if(failure.rawValueAs(Boolean.class)) {
					throw new InvalidQueryException("Failure requested", query);
				}
			} catch (ObjectTransformationException e) {
				throw new InvalidQueryException("Could not understand: '"+failure.rawValue()+"' is not a valid boolean value for parameter '"+QueryableResourceHandler.FAILURE+"'",e,query);
			}
		}

		LocalIndividual    qIndividual =dataset.individual(queryId(), LocalIndividual.class);
		ExternalIndividual qtIndividual=dataset.individual(QuerySupport.QUERY_TYPE, ExternalIndividual.class);
		ExternalIndividual ptIndividual=dataset.individual(QuerySupport.PARAMETER_TYPE, ExternalIndividual.class);

		qIndividual.addValue(QuerySupport.TYPE, qtIndividual);
		for(String parameterName:query.parameterNames()) {
			LocalIndividual pIndividual=
				dataset.
					individual(
						parameterId(parameterName),
						LocalIndividual.class);
			qIndividual.addValue(QuerySupport.HAS_PARAMETER, pIndividual);
			pIndividual.addValue(QuerySupport.TYPE, ptIndividual);
			pIndividual.addValue(QuerySupport.HAS_NAME, Literals.of(parameterName));
			for(String rawValue:query.getParameter(parameterName).rawValues()) {
				pIndividual.addValue(QuerySupport.HAS_VALUE, Literals.of(rawValue));
			}
		}
		return dataset;
	}

	@SuppressWarnings("rawtypes")
	static Name parameterId(String parameterName) {
		return NamingScheme.getDefault().name("parameter",parameterName);
	}

	@SuppressWarnings("rawtypes")
	static Name queryId() {
		return NamingScheme.getDefault().name("query");
	}

}
