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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;

@RunWith(JMockit.class)
public class CharacterEncodingsTest {

	private static final CharacterEncoding US_ASCII = CharacterEncodings.of(StandardCharsets.US_ASCII);
	private static final CharacterEncoding UTF_8 = CharacterEncodings.of(StandardCharsets.UTF_8);

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(CharacterEncodings.class),equalTo(true));
	}

	@Test
	public void parsesWildcardCharacterEncodings() throws Exception {
		CharacterEncoding result = CharacterEncodings.fromString("*");
		assertThat(result,notNullValue());
		assertThat(result.isWildcard(),equalTo(true));
	}

	@Test
	public void parsesValidCharacterEncodings() throws Exception {
		Charset expected = StandardCharsets.US_ASCII;
		CharacterEncoding result = CharacterEncodings.fromString(expected.name());
		assertThat(result,notNullValue());
		assertThat(result.charset(),equalTo(expected));
	}

	@Test
	public void cannotParsesUnknownCharsets() throws Exception {
		try {
			CharacterEncodings.fromString("cateporra");
			fail("Should not parse unknown charsets");
		} catch (IllegalArgumentException e) {
			assertThat(e.getCause(),instanceOf(UnsupportedCharsetException.class));
		}
	}

	@Test
	public void cannotParseInvalidCharsetNames() throws Exception {
		try {
			CharacterEncodings.fromString("<UTF-8>");
			fail("Should not parse invalid charset names");
		} catch (IllegalArgumentException e) {
			assertThat(e.getCause(),instanceOf(IllegalCharsetNameException.class));
		}
	}

	@Test
	public void nullDoesNotIncludeAnything(@Mocked final CharacterEncoding encoding) throws Exception {
		assertThat(CharacterEncodings.includes(null,encoding),equalTo(false));
	}

	@Test
	public void nothingIncludesNull(@Mocked final CharacterEncoding encoding) throws Exception {
		assertThat(CharacterEncodings.includes(encoding,null),equalTo(false));
	}

	@Test
	public void wildcardIncludesEverything(@Mocked final CharacterEncoding encoding) throws Exception {
		assertThat(CharacterEncodings.includes(CharacterEncodings.wildcard(),encoding),equalTo(true));
	}

	@Test
	public void wildcardIsNotIncludedByNonWildcardCharacterEncodings(@Mocked final CharacterEncoding encoding) throws Exception {
		new Expectations() {{
			encoding.isWildcard();result=false;
		}};
		assertThat(CharacterEncodings.includes(encoding,CharacterEncodings.wildcard()),equalTo(false));
	}

	@Test
	public void aRegularCharacterEncodingsIncludesAnotherIffTheyHaveTheSameCharset() throws Exception {
		assertThat(CharacterEncodings.includes(UTF_8,UTF_8),equalTo(true));
	}

	@Test
	public void aRegularCharacterEncodingsDoesNotIncludeAnotherIffTheyHaveTheDifferentCharset() throws Exception {
		assertThat(CharacterEncodings.includes(UTF_8,US_ASCII),equalTo(false));
	}

}
