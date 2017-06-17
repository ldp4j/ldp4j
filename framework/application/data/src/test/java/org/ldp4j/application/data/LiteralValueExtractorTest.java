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

import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class LiteralValueExtractorTest {

	private static final Number LITERAL_VALUE = (Number)1;

	@Test
	public void testVisitLiteral$plainLiteral() throws Exception {
		LiteralValueExtractor<Number> extractor = LiteralValueExtractor.newInstance(Number.class);
		extractor.visitLiteral(Literals.newLiteral(1));
		assertThat(extractor.getValue(),equalTo(LITERAL_VALUE));
		assertThat(extractor.isAvailable(),equalTo(true));
	}

	@Test
	public void testVisitLiteral$plainLiteral$doesNotMatch() throws Exception {
		LiteralValueExtractor<Number> extractor = LiteralValueExtractor.newInstance(Number.class);
		extractor.visitLiteral(Literals.newLiteral("test"));
		assertThat(extractor.getValue(),nullValue());
		assertThat(extractor.isAvailable(),equalTo(false));
	}

	@Test
	public void testVisitLiteral$typedLiteral() throws Exception {
		LiteralValueExtractor<Number> extractor = LiteralValueExtractor.newInstance(Number.class);
		extractor.visitLiteral(Literals.newTypedLiteral(1,Datatypes.STRING));
		assertThat(extractor.getValue(),equalTo(LITERAL_VALUE));
		assertThat(extractor.isAvailable(),equalTo(true));
	}

	@Test
	public void testVisitLiteral$typedLiteral$doesNotMatch() throws Exception {
		LiteralValueExtractor<Number> extractor = LiteralValueExtractor.newInstance(Number.class);
		extractor.visitLiteral(Literals.newTypedLiteral("test",Datatypes.STRING));
		assertThat(extractor.getValue(),nullValue());
		assertThat(extractor.isAvailable(),equalTo(false));
	}

	@Test
	public void testVisitLiteral$languageLiteral() throws Exception {
		LiteralValueExtractor<String> extractor = LiteralValueExtractor.newInstance(String.class);
		extractor.visitLiteral(Literals.newTypedLiteral("1",Datatypes.STRING));
		assertThat(extractor.getValue(),equalTo(LITERAL_VALUE.toString()));
		assertThat(extractor.isAvailable(),equalTo(true));
	}

	@Test
	public void testVisitLiteral$languageLiteral$doesNotMatch() throws Exception {
		LiteralValueExtractor<Number> extractor = LiteralValueExtractor.newInstance(Number.class);
		extractor.visitLiteral(Literals.newLanguageLiteral("test","en"));
		assertThat(extractor.getValue(),nullValue());
		assertThat(extractor.isAvailable(),equalTo(false));
	}

	@Test
	public void testVisitIndividual(@Mocked Individual<?,?> individual) throws Exception {
		LiteralValueExtractor<Number> extractor = LiteralValueExtractor.newInstance(Number.class);
		extractor.visitIndividual(individual);
		assertThat(extractor.getValue(),nullValue());
		assertThat(extractor.isAvailable(),equalTo(false));
	}

}
