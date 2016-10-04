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
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;

import java.util.Locale;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;

@RunWith(JMockit.class)
public class LanguagesTest {

	private static final Language WILDCARD = Languages.wildcard();
	private static final Language US_ENGLISH = Languages.of(Locale.US);
	private static final Language ENGLISH = Languages.of(Locale.ENGLISH);

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(Languages.class),equalTo(true));
	}

	@Test
	public void parsesLanguagesWithJustLanguageTag() throws Exception {
		final Locale expected=Locale.ENGLISH;
		final Language result = Languages.fromString("en");
		assertThat(result,not(nullValue()));
		assertThat(result.locale(),equalTo(expected));
		assertThat(result.isWildcard(),equalTo(false));
		assertThat(result.primaryTag(),equalTo(expected.getLanguage()));
		assertThat(result.subTag(),equalTo(expected.getCountry()));
	}

	@Test
	public void parsesLanguagesWithLanguageAndCountryTags() throws Exception {
		final Locale expected=Locale.US;
		final Language result = Languages.fromString("en-US");
		assertThat(result,not(nullValue()));
		assertThat(result.locale(),equalTo(expected));
		assertThat(result.isWildcard(),equalTo(false));
		assertThat(result.primaryTag(),equalTo(expected.getLanguage()));
		assertThat(result.subTag(),equalTo(expected.getCountry()));
	}

	@Test
	public void parsesWildcardLanguage() throws Exception {
		Language result = Languages.fromString("*");
		assertThat(result,not(nullValue()));
		assertThat(result.locale(),nullValue());
		assertThat(result.isWildcard(),equalTo(true));
		assertThat(result.primaryTag(),equalTo("*"));
		assertThat(result.subTag(),nullValue());
	}

	@Test
	public void doesNotParseLanguagesWithoutTags() throws Exception {
		try {
			Languages.fromString("");
			fail("Should fail when there are no tags");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithTooManyTags() throws Exception {
		try {
			Languages.fromString("to-many-tags");
			fail("Should fail when there are more than 2 tags");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithBadPrimaryTagLength() throws Exception {
		try {
			Languages.fromString("badPrimaryTagLength");
			fail("Should fail when primary tag is too long");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithBadPrimaryTagComposition() throws Exception {
		try {
			Languages.fromString("bad 1st tag Composition");
			fail("Should fail when primary tag is not alphabetic");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithHyphenPrimaryTag() throws Exception {
		try {
			Languages.fromString("-");
			fail("Should fail when cannot find sensible media range");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithBadSubTagLength() throws Exception {
		try {
			Languages.fromString("en-badSubTagLength");
			fail("Should fail when sub tag is too long");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void doesNotParseLanguagesWithBadSubTagComposition() throws Exception {
		try {
			Languages.fromString("en-bad sub tag composition");
			fail("Should fail when sub tag is not alphanumeric");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Invalid language-range: "));
		}
	}

	@Test
	public void cannotCreateLanguageFromVariantCharset() throws Exception {
		try {
			Languages.of(new Locale("th","TH","TH"));
			fail("Should fail when locale is a variant");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Language does not allow locale variants (TH)"));
		}
	}

	@Test
	public void nullDoesNotIncludeAnything(@Mocked final Language language) throws Exception {
		assertThat(Languages.includes(null,language),equalTo(false));
	}

	@Test
	public void nothingIncludesNull(@Mocked final Language language) throws Exception {
		assertThat(Languages.includes(language,null),equalTo(false));
	}

	@Test
	public void wildcardLanguageIncludesEverything(@Mocked final Language language) throws Exception {
		assertThat(Languages.includes(WILDCARD,language),equalTo(true));
	}

	@Test
	public void wildcardLanguageIsNotIncludedBySpecificLanguages(@Mocked final Language language) throws Exception {
		new Expectations() {{
			language.isWildcard();result=false;
		}};
		assertThat(Languages.includes(language,WILDCARD),equalTo(false));
	}

	@Test
	public void aLanguageWithJustPrimaryTagIncludesItself() throws Exception {
		assertThat(Languages.includes(ENGLISH,ENGLISH),equalTo(true));
	}

	@Test
	public void aLanguageWithJustPrimaryTagIncludesLanguagesWithTheSamePrimaryTagAndAnyOtherSubTag() throws Exception {
		assertThat(Languages.includes(ENGLISH,US_ENGLISH),equalTo(true));
	}

	@Test
	public void aLanguageWithSubTagDoesNotIncludeLanguagesWithSamePrimaryTagButDifferentSubtag() throws Exception {
		assertThat(Languages.includes(US_ENGLISH,ENGLISH),equalTo(false));
	}

	@Test
	public void aLanguageWithPrimaryTagAndSubtagIncludesItself() throws Exception {
		assertThat(Languages.includes(US_ENGLISH, US_ENGLISH),equalTo(true));
	}

}
