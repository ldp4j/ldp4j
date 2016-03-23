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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;


public class LanguagesTest {

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(Languages.class),equalTo(true));
	}

	@Test
	public void parsesLanguagesWithOnlyLanguageTag() throws Exception {
		final Locale expected=Locale.ENGLISH;
		final Language result = Languages.fromString("en;q=0.000");
		assertThat(result,not(nullValue()));
		assertThat(result.locale(),equalTo(expected));
		assertThat(result.isWildcard(),equalTo(false));
		assertThat(result.hasWeight(),equalTo(true));
		assertThat(result.weight(),equalTo(0.0D));
		assertThat(result.primaryTag(),equalTo(expected.getLanguage()));
		assertThat(result.subTag(),equalTo(expected.getCountry()));
	}

	@Test
	public void parsesLanguagesWithlanguageAndCountryTag() throws Exception {
		final Locale expected=Locale.US;
		final Language result = Languages.fromString("en-US;q=0.000");
		assertThat(result,not(nullValue()));
		assertThat(result.locale(),equalTo(expected));
		assertThat(result.isWildcard(),equalTo(false));
		assertThat(result.hasWeight(),equalTo(true));
		assertThat(result.weight(),equalTo(0.0D));
		assertThat(result.primaryTag(),equalTo(expected.getLanguage()));
		assertThat(result.subTag(),equalTo(expected.getCountry()));
	}

	@Test
	public void parsesWildcardLanguage() throws Exception {
		Language result = Languages.fromString("*;q=0.000");
		assertThat(result,not(nullValue()));
		assertThat(result.locale(),nullValue());
		assertThat(result.isWildcard(),equalTo(true));
		assertThat(result.hasWeight(),equalTo(true));
		assertThat(result.weight(),equalTo(0.0D));
		assertThat(result.primaryTag(),equalTo("*"));
		assertThat(result.subTag(),nullValue());
	}

	@Test
	public void doesNotParseLanguagesWithMultipleWeights() throws Exception {
		try {
			Languages.fromString("es;q=0.000;q=1.000");
			fail("Should fail for a language with multiple qualities");
		} catch(final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Only one quality value can be specified (found 2: 0.000, 1.000)"));
		}
	}

	@Test
	public void doesNotParseLanguagesWithoutTags() throws Exception {
		try {
			Languages.fromString(";q=0.000");
			fail("Should fail when there are no tags");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguageWithTooManyTags() throws Exception {
		try {
			Languages.fromString("to-many-tags;q=0.000");
			fail("Should fail when there are more than 2 tags");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithBadPrimaryTagLength() throws Exception {
		try {
			Languages.fromString("badPrimaryTagLength;q=0.000");
			fail("Should fail when primary tag is too long");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithBadPrimaryTagComposition() throws Exception {
		try {
			Languages.fromString("bad 1st tag Composition;q=0.000");
			fail("Should fail when primary tag is not alphabetic");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithHyphenPrimaryTag() throws Exception {
		try {
			Languages.fromString("-;q=0.000");
			fail("Should fail when cannot find sensible media range");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithBadSubTagLength() throws Exception {
		try {
			Languages.fromString("en-badSubTagLength;q=0.000");
			fail("Should fail when sub tag is too long");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithBadSubTagComposition() throws Exception {
		try {
			Languages.fromString("en-bad sub tag composition;q=0.000");
			fail("Should fail when sub tag is not alphanumeric");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

}
