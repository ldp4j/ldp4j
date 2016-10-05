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
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;
import static org.ldp4j.http.ContentNegotiationUtils.accept;
import static org.ldp4j.http.ContentNegotiationUtils.acceptCharset;
import static org.ldp4j.http.ContentNegotiationUtils.acceptLanguage;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;

@RunWith(JMockit.class)
public class ContentNegotiationUtilsTest {

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(ContentNegotiationUtils.class),equalTo(true));
	}

	@Test
	public void testAcceptCharset$onlyAcceptsOneWeight() throws Exception {
		try {
			acceptCharset("utf-8;q=0.000;q=1.000");
			fail("Should fail for a charset with multiple qualities");
		} catch(final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Only one quality value can be specified (found 2: "));
		}
	}

	@Test
	public void dependsOnLanguagesClassForCreatingWeightedMediaTypes() {
		final String value = "en-US";
		new MockUp<Languages>() {
			@Mock
			public Language fromString(Invocation context, String aValue) {
				assertThat(aValue,equalTo(value));
				return context.proceed(aValue);
			}
		};
		final Weighted<Language> result=ContentNegotiationUtils.acceptLanguage(value);
		assertThat(result.entity().primaryTag(),equalTo("en"));
		assertThat(result.entity().subTag(),equalTo("US"));
		assertThat(result.hasWeight(),equalTo(false));
		assertThat(result.weight(),equalTo(1.0D));
	}

	@Test
	public void dependsOnCharacterEncodingsClassForCreatingWeightedCharacterEncodings() throws Exception {
		final Charset expected=StandardCharsets.UTF_8;
		new MockUp<CharacterEncodings>() {
			@Mock
			public CharacterEncoding fromString(Invocation context, String aValue) {
				assertThat(aValue,equalTo(expected.name()));
				return context.proceed(aValue);
			}
		};
		final Weighted<CharacterEncoding> result=acceptCharset(expected.name());
		assertThat(result,not(nullValue()));
		assertThat(result.entity().charset(),equalTo(expected));
		assertThat(result.hasWeight(),equalTo(false));
		assertThat(result.weight(),equalTo(1.0D));
	}

	@Test
	public void dependsOnMediaTypesClassForCreatingWeightedMediaTypes() {
		final String value = "text/turtle";
		new MockUp<MediaTypes>() {
			@Mock
			public MediaType fromString(Invocation context, String aValue) {
				assertThat(aValue,equalTo(value));
				return context.proceed(aValue);
			}
		};
		final Weighted<MediaType> result=ContentNegotiationUtils.accept(value);
		assertThat(result.entity().type(),equalTo("text"));
		assertThat(result.entity().subType(),equalTo("turtle"));
		assertThat(result.hasWeight(),equalTo(false));
		assertThat(result.weight(),equalTo(1.0D));
	}

	@Test
	public void acceptAllowsAnExtensionParameterAfterQualityDefinition() {
		final Weighted<MediaType> weighted = accept("text/turtle;q=0.000;param=value");
		assertThat(weighted.hasWeight(),equalTo(true));
		assertThat(weighted.weight(),equalTo(0.0D));
		final MediaType result = weighted.entity();
		assertThat(result.parameters().isEmpty(),equalTo(true));
		assertThat(result.type(),equalTo("text"));
		assertThat(result.subType(),equalTo("turtle"));
	}

	@Test
	public void acceptDoesNotRequireAnExtensionParameterAfterQualityDefinition() {
		final Weighted<MediaType> weighted = accept("text/turtle;q=0.000");
		assertThat(weighted.hasWeight(),equalTo(true));
		assertThat(weighted.weight(),equalTo(0.0D));
		final MediaType result = weighted.entity();
		assertThat(result.parameters().isEmpty(),equalTo(true));
		assertThat(result.type(),equalTo("text"));
		assertThat(result.subType(),equalTo("turtle"));
	}

	@Test
	public void acceptDoesNotAllowsMultipleParametersAfterQualityDefinition() {
		try {
			accept("text/turtle;q=0.000;param=value;ext=value");
			fail("accept should fail when multiple parameters are found after quality definition");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Invalid content after extension parameter [;ext=value] (;param=value;ext=value)"));
		}
	}

	@Test
	public void acceptDoesNotAllowsBadContentAfterQualityDefinition() {
		try {
			accept("text/turtle;q=0.000;bad data");
			fail("accept should fail when bad content is found after the quality definition");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),endsWith(" (;bad data)"));
		}
	}

	@Test
	public void acceptDoesNotAllowsBadContentBeforeExtensionParameter() {
		try {
			accept("text/turtle;q=0.000;;param=value dangling data");
			fail("accept should fail when fails to find first candidate extension parameter");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid content before extension parameter: "));
			assertThat(e.getMessage(),endsWith(" (;;param=value dangling data)"));
		}
	}

	@Test
	public void acceptDoesNotAllowsBadContentAfterExtensionParameter() {
		try {
			accept("text/turtle;q=0.000;param=value dangling data");
			fail("accept should fail when bad content is defined after extension parameter");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Invalid content after extension parameter [ dangling data] (;param=value dangling data)"));
		}
	}

	@Test
	public void acceptCharsetDoesNotAllowParametersAfterQualityDefinition() {
		try {
			acceptCharset("utf-8;q=0.000;param=value");
			fail("acceptCharset should fail when parameters are found after quality definition");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Content after quality definition is not allowed (;param=value)"));
		}
	}

	@Test
	public void acceptLanguageDoesNotAllowParametersAfterQualityDefinition() {
		try {
			acceptLanguage("en-US;q=0.000;param=value");
			fail("acceptLanguage should fail when parameters are found after quality definition");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Content after quality definition is not allowed (;param=value)"));
		}
	}

}
