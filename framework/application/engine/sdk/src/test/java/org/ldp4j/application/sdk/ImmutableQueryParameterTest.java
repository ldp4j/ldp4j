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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-engine-sdk-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class ImmutableQueryParameterTest {



	private static final ImmutableList<String> SINGLE_VALUED_RAW_VALUES = ImmutableList.of("value3");
	private static final ImmutableList<String> MULTI_VALUED_RAW_VALUES = ImmutableList.of("value1","value2");

	private static final String PARAMETER_NAME = "parameter name";

	private ImmutableQueryParameter singleValuedParameter() {
		return ImmutableQueryParameter.create(PARAMETER_NAME, SINGLE_VALUED_RAW_VALUES);
	}

	private ImmutableQueryParameter multiValuedParameter() {
		return ImmutableQueryParameter.create(PARAMETER_NAME, MULTI_VALUED_RAW_VALUES);
	}

	@Test(expected=NullPointerException.class)
	public void failOnCreationWithNullParameterName() {
		ImmutableQueryParameter.create(null, MULTI_VALUED_RAW_VALUES);
	}

	@Test(expected=IllegalArgumentException.class)
	public void failOnCreationWithEmptyParameterName() {
		ImmutableQueryParameter.create("  ", ImmutableList.of("value1"));
	}

	@Test(expected=NullPointerException.class)
	public void failOnCreationWithNullRawValueCollection() {
		ImmutableQueryParameter.create(PARAMETER_NAME, (Collection<?>)null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void failOnCreationWithEmptyRawValueCollection() {
		ImmutableQueryParameter.create(PARAMETER_NAME, Collections.emptyList());
	}

	@Test
	public void createSingleValuedParameter() {
		ImmutableQueryParameter parameter = singleValuedParameter();
		assertThat(parameter,notNullValue());
		assertThat(parameter.cardinality(),equalTo(1));
		assertThat(parameter.isMultivalued(),equalTo(false));
		assertThat(parameter.name(),equalTo(PARAMETER_NAME));
		assertThat(parameter.rawValue(),equalTo(SINGLE_VALUED_RAW_VALUES.get(0)));
		assertThat(parameter.rawValues(),hasSize(1));
		assertThat(parameter.rawValues(),equalTo((List<String>)SINGLE_VALUED_RAW_VALUES));
	}

	@Test
	public void createMultiValuedParameter() {
		ImmutableQueryParameter parameter = multiValuedParameter();
		assertThat(parameter,notNullValue());
		assertThat(parameter.cardinality(),equalTo(2));
		assertThat(parameter.isMultivalued(),equalTo(true));
		assertThat(parameter.name(),equalTo(PARAMETER_NAME));
		assertThat(parameter.rawValue(),equalTo(MULTI_VALUED_RAW_VALUES.get(0)));
		assertThat(parameter.rawValues(),hasSize(2));
		assertThat(parameter.rawValues(),equalTo((List<String>)MULTI_VALUED_RAW_VALUES));
	}

	@Test
	public void createParameterFromVariableArguments() {
		ImmutableQueryParameter parameter = ImmutableQueryParameter.create(PARAMETER_NAME,"value4",5);
		assertThat(parameter,notNullValue());
		assertThat(parameter.cardinality(),equalTo(2));
		assertThat(parameter.isMultivalued(),equalTo(true));
		assertThat(parameter.name(),equalTo(PARAMETER_NAME));
		assertThat(parameter.rawValue(),equalTo("value4"));
		assertThat(parameter.rawValues(),hasSize(2));
		assertThat(parameter.rawValues(),contains("value4","5"));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void failOnRawValueTransformation() {
		ImmutableQueryParameter parameter = singleValuedParameter();
		parameter.rawValueAs(Integer.class);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void failOnRawValuesTransformation() {
		ImmutableQueryParameter parameter = singleValuedParameter();
		parameter.rawValuesAs(Integer.class);
	}

}
