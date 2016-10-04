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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.http.Quality.Type;

@RunWith(JMockit.class)
public class ImmutableQualityTest {

	private static final Type TYPE = Type.SPECULATIVE;
	private static final Type SMALLER_TYPE = Type.DEFINITE;
	private static final double WEIGHT = 0.123D;
	private static final double SMALLER_WEIGHT = 0.001D;

	private static final ImmutableQuality VALUE=ImmutableQuality.create(TYPE,WEIGHT);
	private static final ImmutableQuality VALUE_SMALLER_TYPE = ImmutableQuality.create(SMALLER_TYPE,WEIGHT);
	private static final ImmutableQuality VALUE_SMALLER_WEIGHT = ImmutableQuality.create(TYPE,SMALLER_WEIGHT);
	private static final ImmutableQuality DIFFERENT = ImmutableQuality.create(SMALLER_TYPE,SMALLER_WEIGHT);

	@Test
	public void cannotCreateInstancesWithNullType() throws Exception {
		try {
			ImmutableQuality.create(null,WEIGHT);
			fail("Should not create instances with null type");
		} catch(NullPointerException e) {
			assertThat(e.getMessage(),equalTo("Quality type cannot be null"));
		}
	}

	@Test
	public void cannotCreateInstancesWithInvalidWeight() throws Exception {
		try {
			ImmutableQuality.create(TYPE,-1D);
			fail("Should not create instances with invalid weight");
		} catch(IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Quality weight cannot be negative (-1.0)"));
		}
	}

	@Test
	public void canRetrieveType() {
		assertThat(VALUE.type(),equalTo(TYPE));
	}

	@Test
	public void canRetrieveWeight() {
		assertThat(VALUE.weight(),equalTo(WEIGHT));
	}

	@Test
	public void twoQualitiesAreEqualIfBothTypeAndWeightAreEqual() {
		assertThat(VALUE,equalTo(ImmutableQuality.create(TYPE, WEIGHT)));
	}

	@Test
	public void equalQualitiesHaveSameHashCode() {
		assertThat(VALUE.hashCode(),equalTo(ImmutableQuality.create(TYPE, WEIGHT).hashCode()));
	}

	@Test
	public void qualitiesWithDifferentTypeAreDifferent() {
		assertThat(VALUE,not(equalTo(VALUE_SMALLER_TYPE)));
	}

	@Test
	public void qualitiesWithDifferentTypeHaveDifferentHashCode() {
		assertThat(VALUE.hashCode(),not(equalTo(VALUE_SMALLER_TYPE.hashCode())));
	}

	@Test
	public void qualitiesWithDifferentWeightAreDifferent() {
		assertThat(VALUE,not(equalTo(VALUE_SMALLER_WEIGHT)));
	}

	@Test
	public void qualitiesWithDifferentWeightHaveDifferentHashCode() {
		assertThat(VALUE.hashCode(),not(equalTo(VALUE_SMALLER_WEIGHT.hashCode())));
	}

	@Test
	public void qualitiesWithDifferentAttributesAreDifferent() {
		assertThat(VALUE,not(equalTo(DIFFERENT)));
	}

	@Test
	public void qualitiesWithDifferentAttributesHaveDifferentHashCode() {
		assertThat(VALUE.hashCode(),not(equalTo(DIFFERENT.hashCode())));
	}

	@Test
	public void qualitiesCanOnlyBeEqualToOtherQualities() {
		assertThat((Object)VALUE,not(equalTo((Object)"value")));
	}

	@Test
	public void copyOfDoesNotCloneImmutableInstances() {
		assertThat(ImmutableQuality.copyOf(VALUE),sameInstance(VALUE));
	}

	@Test
	public void copyOfClonesNonImmutableInstances(@Mocked final Quality quality) {
		new Expectations() {{
			quality.type();result=VALUE.type();
			quality.weight();result=VALUE.weight();
		}};
		ImmutableQuality clone = ImmutableQuality.copyOf(quality);
		assertThat(clone,not(sameInstance(quality)));
		assertThat(clone,equalTo(VALUE));
	}

	@Test
	public void speculativeQualityHasCustomToStringRepresentation() {
		assertThat(VALUE.toString(),equalTo("0.123 [SPECULATIVE]"));
	}

	@Test
	public void definiteQualityHasCustomToStringRepresentation() {
		assertThat(DIFFERENT.toString(),equalTo("0.001 [DEFINITE]"));
	}

	@Test
	public void qualitiesAreGreaterThanNull() throws Exception {
		assertThat(VALUE.compareTo(null),equalTo(1));
	}

	@Test
	public void equalQualitiesAreOrderedEqually() throws Exception {
		assertThat(VALUE.compareTo(VALUE),equalTo(0));
	}

	@Test
	public void qualitiesOrderingDependsOnTypeFirst() throws Exception {
		assertThat(VALUE.compareTo(VALUE_SMALLER_TYPE),equalTo(1));
		assertThat(VALUE_SMALLER_TYPE.compareTo(VALUE),equalTo(-1));
	}

	@Test
	public void qualitiesOrderingDependsOnWeightSecond() throws Exception {
		assertThat(VALUE.compareTo(VALUE_SMALLER_WEIGHT),equalTo(1));
		assertThat(VALUE_SMALLER_WEIGHT.compareTo(VALUE),equalTo(-1));
	}

}
