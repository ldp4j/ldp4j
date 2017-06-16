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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetHelper;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.IndividualHelper;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.InvalidQueryException;
import org.ldp4j.application.ext.ObjectTransformationException;
import org.ldp4j.application.ext.Query;
import org.ldp4j.application.sdk.QueryBuilder;
import org.ldp4j.commons.testing.Utils;


public class QuerySupportTest {

	@Test
	public void verifyIsUtilityClass() {
		assertThat(Utils.isUtilityClass(QuerySupport.class),equalTo(true));
	}

	@Test
	public void testGetDescription() throws Exception {
		Name<?> id = NamingScheme.getDefault().name("Test");
		Query query =
			QueryBuilder.
				newInstance().
					withParameter("param1", "value1").
					withParameter("param1", "value2").
					withParameter("param2", "value1").
					build();
		DataSet data=QuerySupport.getDescription(id, query);
		assertThat((Object)data.name(),sameInstance((Object)id));
		DataSetHelper dHelper = DataSetUtils.newHelper(data);
		IndividualHelper qInd = dHelper.localIndividual(QuerySupport.queryId());
		assertThat(qInd.types(),contains(QuerySupport.QUERY_TYPE));
		IndividualHelper pInd = dHelper.localIndividual(QuerySupport.parameterId("param1"));
		assertThat(pInd.types(),contains(QuerySupport.PARAMETER_TYPE));
	}

	@Test
	public void testGetDescription$failureRequest$happyPath() throws Exception {
		Name<?> id = NamingScheme.getDefault().name("Test");
		Query query =
			QueryBuilder.
				newInstance().
					withParameter(QueryableResourceHandler.FAILURE, "true").
					build();
		try {
			QuerySupport.getDescription(id, query);
			fail("Should not accept queries with the failure parameter set to true");
		} catch (InvalidQueryException e) {
			assertThat(e.getQuery(),equalTo(query));
			assertThat(e.getCause(),nullValue());
		}
	}

	@Test
	public void testGetDescription$failureRequest$otherPath() throws Exception {
		Name<?> id = NamingScheme.getDefault().name("Test");
		Query query =
			QueryBuilder.
				newInstance().
					withParameter(QueryableResourceHandler.FAILURE, "FALSE").
					build();
		DataSet data=QuerySupport.getDescription(id, query);
		assertThat((Object)data.name(),sameInstance((Object)id));
		DataSetHelper dHelper = DataSetUtils.newHelper(data);
		IndividualHelper qInd = dHelper.localIndividual(QuerySupport.queryId());
		assertThat(qInd.types(),contains(QuerySupport.QUERY_TYPE));
		IndividualHelper pInd = dHelper.localIndividual(QuerySupport.parameterId(QueryableResourceHandler.FAILURE));
		assertThat(pInd.types(),contains(QuerySupport.PARAMETER_TYPE));
	}

	@Test
	public void testGetDescription$failureRequest$invalidValue() throws Exception {
		Name<?> id = NamingScheme.getDefault().name("Test");
		Query query =
			QueryBuilder.
				newInstance().
					withParameter(QueryableResourceHandler.FAILURE, "not a boolean").
					build();
		try {
			QuerySupport.getDescription(id, query);
			fail("Should not accept queries with the failure parameter");
		} catch (InvalidQueryException e) {
			assertThat(e.getQuery(),equalTo(query));
			assertThat(e.getCause(),instanceOf(ObjectTransformationException.class));
		}
	}

	@Test
	public void testGetValueLessParameterDescription() throws Exception {
		Name<?> id = NamingScheme.getDefault().name("Test");
		Query query =
			QueryBuilder.
				newInstance().
					withParameter("param1", "").
					withParameter("param1", "").
					withParameter("param2", "value1").
					build();
		DataSet data=QuerySupport.getDescription(id, query);
		assertThat((Object)data.name(),sameInstance((Object)id));
		DataSetHelper dHelper = DataSetUtils.newHelper(data);
		IndividualHelper qInd = dHelper.localIndividual(QuerySupport.queryId());
		assertThat(qInd.types(),contains(QuerySupport.QUERY_TYPE));
		IndividualHelper pInd = dHelper.localIndividual(QuerySupport.parameterId("param1"));
		assertThat(pInd.types(),contains(QuerySupport.PARAMETER_TYPE));
	}

}
