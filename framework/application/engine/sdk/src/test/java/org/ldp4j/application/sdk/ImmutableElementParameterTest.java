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
import static org.hamcrest.Matchers.not;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;

public class ImmutableElementParameterTest extends HttpRequestSupport {

	@Test
	public void testHasCustomStringRepresentation() {
		ImmutableElementParameter sut = createParameter("header", "value");
		assertThat(sut.toString(),not(equalTo(Utils.defaultToString(sut))));
	}

	@Test
	public void testHashCodeEqualsContract$null() throws Exception {
		ImmutableElementParameter sut = createParameter("header", "value");
		assertThat(sut.equals(null),equalTo(false));
	}

	@Test
	public void testHashCodeEqualsContract$self() throws Exception {
		ImmutableElementParameter sut = createParameter("header", "value");
		assertThat(sut,equalTo(sut));
	}

	@Test
	public void testHashCodeEqualsContract$equal() throws Exception {
		ImmutableElementParameter sut = createParameter("header", "value");
		ImmutableElementParameter other = createParameter("header", "value");
		assertThat(sut,equalTo(other));
		assertThat(sut.hashCode(),equalTo(other.hashCode()));
	}

	@Test
	public void testHashCodeEqualsContract$differentHeader() throws Exception {
		ImmutableElementParameter sut = createParameter("header1", "value");
		ImmutableElementParameter other = createParameter("header2", "value");
		assertThat(sut,not(equalTo(other)));
		assertThat(sut.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void testHashCodeEqualsContract$differentValue() throws Exception {
		ImmutableElementParameter sut = createParameter("header", "value1");
		ImmutableElementParameter other = createParameter("header", "value2");
		assertThat(sut,not(equalTo(other)));
		assertThat(sut.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void testHashCodeEqualsContract$differentParts() throws Exception {
		ImmutableElementParameter sut = createParameter("header1", "value1");
		ImmutableElementParameter other = createParameter("header2", "value2");
		assertThat(sut,not(equalTo(other)));
		assertThat(sut.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void testHashCodeEqualsContract$anotherType() throws Exception {
		ImmutableElementParameter sut = createParameter("header", "value");
		assertThat((Object)sut,not(equalTo(new Object())));
	}

}
