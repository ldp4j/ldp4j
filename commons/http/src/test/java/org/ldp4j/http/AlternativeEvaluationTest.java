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

import org.junit.Test;

import com.google.common.collect.Lists;


public class AlternativeEvaluationTest {

	@Test
	public void completesVariantWithDefaultValues() throws Exception {
		AlternativeEvaluation sut = evaluationOfEmptyGeneratedVariant();
		evaluate(sut);
		assertThat(sut.variant().type(),equalTo(ContentNegotiator.DEFAULT_MEDIA_TYPE));
		assertThat(sut.variant().language(),equalTo(ContentNegotiator.DEFAULT_LANGUAGE));
		assertThat(sut.variant().charset(),equalTo(ContentNegotiator.DEFAULT_CHARACTER_ENCODING));
	}

	@Test
	public void stringRepresentationDependsOnEvaluation() throws Exception {
		AlternativeEvaluation sut = evaluationOfEmptyGeneratedVariant();
		String before=sut.toString();
		evaluate(sut);
		assertThat(sut.toString(),not(equalTo(before)));
	}

	private AlternativeEvaluation evaluationOfEmptyGeneratedVariant() {
		AlternativeEvaluation sut=
			AlternativeEvaluation.
				generated(
					ImmutableAlternative.
						create(0.3d,ImmutableVariant.newInstance()));
		return sut;
	}

	@SuppressWarnings("unchecked")
	private void evaluate(AlternativeEvaluation sut) {
		sut.
			evaluate(
				Lists.
					<Weighted<MediaType>>newArrayList(
						ContentNegotiationUtils.accept("text/*;q=0.2"),
						ContentNegotiationUtils.accept("*/*;q=1.0")
					),
				Lists.
					<Weighted<CharacterEncoding>>newArrayList(
						ContentNegotiationUtils.acceptCharset("us-ascii;q=0.1"),
						ContentNegotiationUtils.acceptCharset("utf-8;q=0.5")
					),
				Lists.
					<Weighted<Language>>newArrayList(
						ContentNegotiationUtils.acceptLanguage("en;q=1.0"),
						ContentNegotiationUtils.acceptLanguage("fr;q=0.5")
					)
			);
	}

}
