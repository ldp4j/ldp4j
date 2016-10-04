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

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.Test;

public class AlternativeComparatorTest extends AbstractComparatorTest<Alternative>{

	private static final Language ENGLISH = Languages.of(Locale.ENGLISH);
	private static final Language GERMAN = Languages.of(Locale.GERMAN);

	private static final CharacterEncoding UTF_8 = CharacterEncodings.of(StandardCharsets.UTF_8);
	private static final CharacterEncoding US_ASCII = CharacterEncodings.of(StandardCharsets.US_ASCII);

	private static final MediaType TEXT_PLAIN=MediaTypes.of("text","plain");

	private static final Alternative VALUE =
		ImmutableAlternative.
			create(
				0.123D,
				ImmutableVariant.
					newInstance().
						type(TEXT_PLAIN).
						charset(UTF_8).
						language(ENGLISH));

	private static final Alternative NO_VARIANT =
		ImmutableAlternative.
			create(
				0.123D,
				ImmutableVariant.newInstance());

	private static final Alternative SMALLER_QUALITY =
		ImmutableAlternative.
			create(
				0.001D,
				ImmutableVariant.
					newInstance().
						type(TEXT_PLAIN).
						charset(UTF_8).
						language(ENGLISH));

	private static final Alternative SMALLER_MEDIA_TYPE =
		ImmutableAlternative.
			create(
				0.123D,
				ImmutableVariant.
					newInstance().
						type(MediaTypes.from(TEXT_PLAIN).withParam("format", "flow").build()).
						charset(UTF_8).
						language(ENGLISH));

	private static final Alternative SMALLER_CHARACTER_ENCODING =
		ImmutableAlternative.
			create(
				0.123D,
				ImmutableVariant.
					newInstance().
						type(TEXT_PLAIN).
						charset(US_ASCII).
						language(ENGLISH));

	private static final Alternative SMALLER_LANGUAGE =
		ImmutableAlternative.
			create(
				0.123D,
				ImmutableVariant.
					newInstance().
						type(TEXT_PLAIN).
						charset(UTF_8).
						language(GERMAN));

	protected AlternativeComparator sut() {
		return AlternativeComparator.INSTANCE;
	}

	@Test
	public void alternativesAreEqualIfAllAttributesAreEqual() {
		assertIsEqualTo(VALUE, VALUE);
	}

	@Test
	public void comparatorChecksQualityFirst() throws Exception {
		assertIsGreaterThan(VALUE,SMALLER_QUALITY);
		assertIsLowerThan(SMALLER_QUALITY,VALUE);
	}

	@Test
	public void comparatorChecksMediaTypeSecond() throws Exception {
		assertIsGreaterThan(VALUE,SMALLER_MEDIA_TYPE);
		assertIsLowerThan(SMALLER_MEDIA_TYPE,VALUE);
	}

	@Test
	public void comparatorChecksCharacterEncodingThird() throws Exception {
		assertIsGreaterThan(VALUE,SMALLER_CHARACTER_ENCODING);
		assertIsLowerThan(SMALLER_CHARACTER_ENCODING,VALUE);
	}

	@Test
	public void comparatorChecksLanguageLast() throws Exception {
		assertIsGreaterThan(VALUE,SMALLER_LANGUAGE);
		assertIsLowerThan(SMALLER_LANGUAGE,VALUE);
	}

	@Test
	public void comparatorEnsuresNullIsSmallerThanNonNull() throws Exception {
		assertIsGreaterThan(VALUE,NO_VARIANT);
		assertIsLowerThan(NO_VARIANT,VALUE);
	}

	@Test
	public void comparatorEnsuresNullIsEqualToNull() throws Exception {
		assertIsEqualTo(NO_VARIANT,NO_VARIANT);
	}
}
