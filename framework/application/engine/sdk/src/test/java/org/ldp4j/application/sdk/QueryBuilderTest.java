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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.2
 *   Bundle      : ldp4j-application-engine-sdk-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.ldp4j.application.ext.Parameter;
import org.ldp4j.application.ext.Query;

public class QueryBuilderTest {

	private static final String OTHER_PARAMETER = "otherParameter";
	private static final String OTHER_RAW_VALUE = "otherRawValue";
	private static final String PARAMETER = "parameter";
	private static final String RAW_VALUE = "rawValue";

	private void assertHasParameter(Query query, String parameterName, String... rawValues) {
		assertThat(query.hasParameter(parameterName),equalTo(true));
		Parameter parameter = query.getParameter(parameterName);
		assertThat(parameter,notNullValue());
		assertThat(parameter.isMultivalued(),equalTo(rawValues.length>1));
		assertThat(parameter.cardinality(),equalTo(rawValues.length));
		assertThat(parameter.rawValue(),equalTo(rawValues[0]));
		assertThat(parameter.rawValues(),hasItems(rawValues));
	}

	@Test(expected=NullPointerException.class)
	public void testDisallowNullParameterName() {
		QueryBuilder.newInstance().withParameter(null,RAW_VALUE);
	}

	@Test(expected=NullPointerException.class)
	public void testDisallowNullParameterValue() {
		QueryBuilder.newInstance().withParameter(PARAMETER,null);
	}

	@Test
	public void testAcceptNonNullArguments() {
		Query query =
			QueryBuilder.
				newInstance().
					withParameter(PARAMETER,RAW_VALUE).
					build();
		assertHasParameter(query, PARAMETER, RAW_VALUE);
	}

	@Test
	public void testAcceptRepeatedParameterValues() {
		Query query =
			QueryBuilder.
				newInstance().
					withParameter(PARAMETER,RAW_VALUE).
					withParameter(PARAMETER,RAW_VALUE).
					build();
		assertHasParameter(query, PARAMETER, RAW_VALUE, RAW_VALUE);
	}

	@Test
	public void testAcceptDifferentParameterValues() {
		Query query =
			QueryBuilder.
				newInstance().
					withParameter(PARAMETER,RAW_VALUE).
					withParameter(PARAMETER,OTHER_RAW_VALUE).
					build();
		assertHasParameter(query, PARAMETER, RAW_VALUE, OTHER_RAW_VALUE);
	}

	@Test
	public void testAcceptDifferentParameters() {
		Query query =
			QueryBuilder.
				newInstance().
					withParameter(PARAMETER,RAW_VALUE).
					withParameter(OTHER_PARAMETER,OTHER_RAW_VALUE).
					build();
		assertHasParameter(query, PARAMETER, RAW_VALUE);
		assertHasParameter(query, OTHER_PARAMETER, OTHER_RAW_VALUE);
	}

}
