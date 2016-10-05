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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.net.URI;

import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class MutableDataSetTest {

	private interface UnsupportedIndividual extends Individual<String,UnsupportedIndividual> {

	}

	@Injectable
	private Name<?> name;

	@Tested
	private MutableDataSet sut;

	@Test
	public void testName() {
		assertThat((Object)sut.name(),sameInstance((Object)name));
	}

	@Test
	public void testIsEmpty$noIndividuals() throws Exception {
		assertThat(sut.isEmpty(),equalTo(true));
	}

	@Test
	public void testIsEmpty$anEmptyIndividual() throws Exception {
		sut.individual(URI.create("urn:individual"), ExternalIndividual.class);
		assertThat(sut.isEmpty(),equalTo(true));
	}

	@Test
	public void testIsEmpty$nonEmptyIndividual() throws Exception {
		Individual<?,?> individual=sut.individual(URI.create("urn:individual"), ExternalIndividual.class);
		individual.addValue(URI.create("urn:property"), Literals.newLiteral("data"));
		assertThat(sut.isEmpty(),equalTo(false));
	}

	@Test
	public void testHasIndividuals$noIndividuals() throws Exception {
		assertThat(sut.hasIndividuals(),equalTo(false));
	}

	@Test
	public void testHasIndividuals$anEmptyIndividual() throws Exception {
		sut.individual(URI.create("urn:individual"), ExternalIndividual.class);
		assertThat(sut.hasIndividuals(),equalTo(true));
	}

	@Test
	public void testNumberOfIndividuals$noIndividuals() throws Exception {
		assertThat(sut.numberOfIndividuals(),equalTo(0));
	}

	@Test
	public void testNumberOfIndividuals$anEmptyIndividual() throws Exception {
		sut.individual(URI.create("urn:individual"), ExternalIndividual.class);
		assertThat(sut.numberOfIndividuals(),equalTo(1));
	}

	@Test
	public void testIndividual$unsupportedClass() throws Exception {
		try {
			sut.individual("urn:individual", UnsupportedIndividual.class);
			fail("Should not create individuals for unsupported individual types");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Unsupported individual type 'org.ldp4j.application.data.MutableDataSetTest.UnsupportedIndividual'"));
		}
	}

	@Test
	public void testIdentifierReuseIsForbidden() throws Exception {
		sut.individual(URI.create("urn:individual"), ExternalIndividual.class);
		try {
			sut.individual(URI.create("urn:individual"), NewIndividual.class);
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Existing individual 'urn:individual' is not compatible with 'org.ldp4j.application.data.NewIndividual' (org.ldp4j.application.data.MutableExternalIndividual)"));
		}
	}

	@Test
	public void testIndividualsAreOnlyCreatedOnce() throws Exception {
		ExternalIndividual individual = sut.individual(URI.create("urn:individual"), ExternalIndividual.class);
		assertThat(sut.individual(URI.create("urn:individual"), ExternalIndividual.class),sameInstance(individual));
	}

}
