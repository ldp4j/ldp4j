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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-http-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;
import static org.ldp4j.http.ContentNegotiation.acceptCharset;
import static org.ldp4j.http.ContentNegotiation.acceptLanguage;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;

@RunWith(JMockit.class)
public class ContentNegotiationTest {

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(ContentNegotiation.class),equalTo(true));
	}

	@Test
	public void testAcceptCharset$onlyAcceptsOneWeight() throws Exception {
		try {
			acceptCharset("utf-8;q=0.000;q=1.000");
			fail("Should fail for a charset with multiple qualities");
		} catch(final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Only one quality value can be specified (found 2: 0.000, 1.000)"));
		}
	}

	@Test
	public void testAcceptCharset$supported() throws Exception {
		final Charset expected=StandardCharsets.UTF_8;
		final Weighted<Charset> result=acceptCharset(expected.name()+";q=0.000");
		assertThat(result,not(nullValue()));
		final Charset actual = result.get();
		assertThat(actual,not(nullValue()));
		assertThat(actual,equalTo(expected));
	}

	@Test
	public void testAcceptCharset$wildcard() throws Exception {
		assertThat(acceptCharset("*;q=0.000").get(),nullValue());
	}

	@Test
	public void testAcceptCharset$badCharset() throws Exception {
		try {
			acceptCharset("<bad charset name>;q=0.000");
			fail("Should fail when charset name is invalid");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid charset: "));
			assertThat(e.getCause(),instanceOf(IllegalCharsetNameException.class));
		}
	}

	@Test
	public void testAcceptCharset$unsupportedCharset() throws Exception {
		new MockUp<Charset>() {
			@Mock
			Charset forName(final String name) {
				throw new UnsupportedCharsetException(name);
			}
		};
		try {
			acceptCharset("<bad charset name>;q=0.000");
			fail("Should fail when charset name is invalid");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid charset: "));
			assertThat(e.getCause(),instanceOf(UnsupportedCharsetException.class));
			assertThat(((UnsupportedCharsetException)e.getCause()).getCharsetName(),equalTo("<bad charset name>"));
		}
	}

	@Test
	public void testAcceptLanguage$onlyAcceptsOneWeight() throws Exception {
		try {
			acceptLanguage("es;q=0.000;q=1.000");
			fail("Should fail for a language with multiple qualities");
		} catch(final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Only one quality value can be specified (found 2: 0.000, 1.000)"));
		}
	}

	@Test
	public void testAcceptLanguage$onlyLanguageTag() throws Exception {
		final Locale expected=Locale.ENGLISH;
		final Weighted<Locale> result = acceptLanguage("en;q=0.000");
		assertThat(result,not(nullValue()));
		final Locale actual = result.get();
		assertThat(actual,not(nullValue()));
		assertThat(actual.getLanguage(),equalTo(expected.getLanguage()));
		assertThat(actual.getCountry(),equalTo(expected.getCountry()));
	}

	@Test
	public void testAcceptLanguage$languageAndCountryTag() throws Exception {
		final Locale expected=Locale.US;
		final Weighted<Locale> result = acceptLanguage("en-US;q=0.000");
		assertThat(result,not(nullValue()));
		final Locale actual = result.get();
		assertThat(actual,not(nullValue()));
		assertThat(actual.getLanguage(),equalTo(expected.getLanguage()));
		assertThat(actual.getCountry(),equalTo(expected.getCountry()));
	}

	@Test
	public void testAcceptLanguage$wildcard() throws Exception {
		assertThat(acceptLanguage("*;q=0.000").get(),nullValue());
	}

	@Test
	public void testAcceptLanguage$noTags() throws Exception {
		try {
			acceptLanguage(";q=0.000");
			fail("Should fail when there are no tags");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void testAcceptLanguage$tooManyTags() throws Exception {
		try {
			acceptLanguage("to-many-tags;q=0.000");
			fail("Should fail when there are more than 2 tags");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void testAcceptLanguage$badPrimaryTagLength() throws Exception {
		try {
			acceptLanguage("badPrimaryTagLength;q=0.000");
			fail("Should fail when primary tag is too long");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void testAcceptLanguage$badPrimaryTagComposition() throws Exception {
		try {
			acceptLanguage("bad 1st tag Composition;q=0.000");
			fail("Should fail when primary tag is not alphabetic");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void testAcceptLanguage$badPrimaryTagComposition$cornerCase() throws Exception {
		try {
			acceptLanguage("-;q=0.000");
			fail("Should fail when cannot find sensible media range");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void testAcceptLanguage$badSubTagLength() throws Exception {
		try {
			acceptLanguage("en-badSubTagLength;q=0.000");
			fail("Should fail when sub tag is too long");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void testAcceptLanguage$badSubTagComposition() throws Exception {
		try {
			acceptLanguage("en-bad sub tag composition;q=0.000");
			fail("Should fail when sub tag is not alphanumeric");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

}
