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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import mockit.Invocation;
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
	public void dependsOnLanguages() {
		final String value = "en-US";
		new MockUp<Languages>() {
			@Mock
			public Language fromString(Invocation context, String aValue) {
				assertThat(aValue,equalTo(value));
				return context.proceed(aValue);
			}
		};
		Language result=ContentNegotiation.acceptLanguage(value);
		assertThat(result.primaryTag(),equalTo("en"));
		assertThat(result.subTag(),equalTo("US"));
	}

}
