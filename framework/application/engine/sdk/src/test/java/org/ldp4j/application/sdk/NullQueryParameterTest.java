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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.ldp4j.application.ext.Parameter;

public class NullQueryParameterTest {

	private static final String PARAMETER_NAME = "parameter";

	private Parameter sut=NullQueryParameter.create(PARAMETER_NAME);

	@Test
	public void testName() throws Exception {
		assertThat(this.sut.name(),equalTo(PARAMETER_NAME));
	}

	@Test
	public void testIsMultivalued() throws Exception {
		assertThat(this.sut.isMultivalued(),equalTo(false));
	}

	@Test
	public void testRawValuesAs() throws Exception {
		assertThat(this.sut.rawValuesAs(Integer.class),hasSize(0));
	}

	@Test
	public void testRawValues() throws Exception {
		assertThat(this.sut.rawValues(),hasSize(0));
	}

	@Test
	public void testRawValueAs() throws Exception {
		assertThat(this.sut.rawValueAs(Integer.class),nullValue());
	}

	@Test
	public void testRawValue() throws Exception {
		assertThat(this.sut.rawValue(),nullValue());
	}

	@Test
	public void testCardinality() throws Exception {
		assertThat(this.sut.cardinality(),equalTo(0));
	}

}