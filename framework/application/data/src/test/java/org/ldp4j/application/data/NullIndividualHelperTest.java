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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;

import org.junit.Test;
import org.ldp4j.application.vocabulary.RDFS;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class NullIndividualHelperTest {

	private NullIndividualHelper sut=new NullIndividualHelper();

	@Test
	public void testTypes() throws Exception {
		assertThat(sut.types(),hasSize(0));
	}

	@Test
	public void testPropertyURI() throws Exception {
		assertThat(sut.property(RDFS.LABEL.as(URI.class)),instanceOf(NullPropertyHelper.class));
	}

	@Test
	public void testPropertyString() throws Exception {
		assertThat(sut.property(RDFS.LABEL.qualifiedEntityName()),instanceOf(NullPropertyHelper.class));
	}

	@Test
	public void testPropertyTerm() throws Exception {
		assertThat(sut.property(RDFS.LABEL),instanceOf(NullPropertyHelper.class));
	}

}
