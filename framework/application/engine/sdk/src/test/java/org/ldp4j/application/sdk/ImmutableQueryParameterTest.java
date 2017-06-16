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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;

import com.google.common.collect.ImmutableList;

public class ImmutableQueryParameterTest {

	private static final ImmutableList<String> SINGLE_VALUED_RAW_VALUES = ImmutableList.of("3.3");
	private static final ImmutableList<String> MULTI_VALUED_RAW_VALUES = ImmutableList.of("1","2");

	private static final ImmutableList<String> INVALID_MULTI_VALUED_RAW_VALUES = ImmutableList.of("1","http://www.ldp4j.org");

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

	@Test
	public void transformFirstRawValue$singleValued() {
		ImmutableQueryParameter parameter = singleValuedParameter();
		Float value = parameter.rawValueAs(Float.class);
		assertThat(value,equalTo(3.3f));
	}

	@Test
	public void transformFirstRawValue$multiValued() {
		ImmutableQueryParameter parameter = multiValuedParameter();
		int value = parameter.rawValueAs(Integer.class);
		assertThat(value,equalTo(1));
	}

	@Test
	public void transformRawValues$singleValued() {
		ImmutableQueryParameter parameter = singleValuedParameter();
		List<Float> value = parameter.rawValuesAs(Float.class);
		assertThat(value,contains(3.3f));
	}

	@Test
	public void transformRawValues$multiValued() {
		ImmutableQueryParameter parameter = multiValuedParameter();
		List<Integer> value = parameter.rawValuesAs(int.class);
		assertThat(value,contains(1,2));
	}

	@Test
	public void failOnMixedRawValuesTransformation() {
		ImmutableQueryParameter parameter = mixedParameterValues();
		try {
			parameter.rawValuesAs(Integer.class);
			fail("Should nor parse a URI as an integer");
		} catch (Exception e) {

		}
	}

	@Test
	public void transformMixedRawValues() {
		ImmutableQueryParameter parameter = mixedParameterValues();
		try {
			parameter.rawValuesAs(URI.class);
		} catch (Exception e) {
			fail("Should parse an integer as a URI");

		}
	}

	@Test
	public void testHasCustomStringRepresentation() {
		assertThat(singleValuedParameter().toString(),not(equalTo(Utils.defaultToString(singleValuedParameter()))));
	}

	private ImmutableQueryParameter mixedParameterValues() {
		return ImmutableQueryParameter.create(PARAMETER_NAME, INVALID_MULTI_VALUED_RAW_VALUES);
	}

}
