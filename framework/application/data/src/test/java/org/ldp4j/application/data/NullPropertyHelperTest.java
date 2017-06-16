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

public class NullPropertyHelperTest extends DataTestingSupport {

	private PropertyHelper sut=new NullIndividualHelper().property(RDFS.LABEL);

	@Test
	public void testFirstValue() throws Exception {
		assertThat(sut.firstValue(String.class),nullValue());
	}

	@Test
	public void testFirstIndividual$helper() throws Exception {
		assertThat(sut.firstIndividual(),instanceOf(NullIndividualHelper.class));
	}

	@Test
	public void testFirstIndividual() throws Exception {
		assertThat(sut.firstIndividual(LocalIndividual.class),nullValue());
		assertThat(sut.firstIndividual(ExternalIndividual.class),nullValue());
		assertThat(sut.firstIndividual(ManagedIndividual.class),nullValue());
		assertThat(sut.firstIndividual(NewIndividual.class),nullValue());
		assertThat(sut.firstIndividual(RelativeIndividual.class),nullValue());
	}

	@Test
	public void testWithLiteral() throws Exception {
		assertThat(sut.withLiteral("value"),instanceOf(IndividualPropertyHelperImpl.class));
	}

	@Test
	public void testWithIndividualURI() throws Exception {
		IndividualPropertyHelper result = sut.withIndividual(RDFS.CLASS.as(URI.class));
		assertThat(result,instanceOf(IndividualPropertyHelperImpl.class));
	}

	@Test
	public void testWithIndividualString() throws Exception {
		IndividualPropertyHelper result = sut.withIndividual(RDFS.CLASS.qualifiedEntityName());
		assertThat(result,instanceOf(IndividualPropertyHelperImpl.class));
	}

	@Test
	public void testWithIndividualTerm() throws Exception {
		IndividualPropertyHelper result = sut.withIndividual(RDFS.CLASS);
		assertThat(result,instanceOf(IndividualPropertyHelperImpl.class));
	}

	@Test
	public void testWithIndividual$local() throws Exception {
		IndividualPropertyHelper result = sut.withIndividual(name("resourceName"));
		assertThat(result,instanceOf(IndividualPropertyHelperImpl.class));
	}

	@Test
	public void testWithIndividual$managedId() throws Exception {
		IndividualPropertyHelper result = sut.withIndividual(name("resourceName"),"managerId");
		assertThat(result,instanceOf(IndividualPropertyHelperImpl.class));
	}

	@Test
	public void testWithIndividual$managedId$relativeId() throws Exception {
		IndividualPropertyHelper result = sut.withIndividual(name("resourceName"),"managerId","relativePath/");
		assertThat(result,instanceOf(IndividualPropertyHelperImpl.class));
	}

	@Test
	public void testWithIndividual$managedId$relativeId$URI() throws Exception {
		IndividualPropertyHelper result = sut.withIndividual(name("resourceName"),"managerId",URI.create("relativePath/"));
		assertThat(result,instanceOf(IndividualPropertyHelperImpl.class));
	}

	@Test
	public void testHasCustomString() {
		assertThat(sut.toString(),containsString(RDFS.LABEL.qualifiedEntityName()));
	}

}
