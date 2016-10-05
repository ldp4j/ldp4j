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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;
import org.ldp4j.http.Variants.VariantBuilder;

@RunWith(JMockit.class)
public class VariantsTest {

	private static final Language LANGUAGE = Languages.of(Locale.ENGLISH);
	private static final CharacterEncoding CHARACTER_ENCODING = CharacterEncodings.of(StandardCharsets.UTF_8);
	private static final MediaType MEDIA_TYPE = MediaTypes.of("application","xml");

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(Variants.class),equalTo(true));
	}

	@Test
	public void variantCannotBeNull() throws Exception {
		try {
			Variants.contentType(null);
			fail("Should not accept null variants");
		} catch (NullPointerException e) {
			assertThat(e.getMessage(),equalTo("Variant cannot be null"));
		}
	}

	@Test
	public void doesNotGenerateContentTypeForVariantWithoutMediaType() throws Exception {
		final Variant variant = Variants.builder().variant();
		assertThat(Variants.contentType(variant),nullValue());
	}

	@Test
	public void contentTypeIsVariantsMediaTypeIfNoCharsetIsDefined() throws Exception {
		final Variant variant = Variants.builder().type(MEDIA_TYPE).variant();
		assertThat(Variants.contentType(variant),equalTo(MEDIA_TYPE));
	}

	@Test
	public void contentTypeAggregatesTypeAndCharsetWhenAvailable() throws Exception {
		final Variant variant=
			Variants.
				builder().
					type(MEDIA_TYPE).
					charset(CHARACTER_ENCODING).
					variant();
		final MediaType contentType = Variants.contentType(variant);
		assertThat(contentType,equalTo(MediaTypes.from(MEDIA_TYPE).withCharset(CHARACTER_ENCODING.charset()).build()));
	}

	@Test
	public void variantBuildMethodProducesImmutableVariantObjects() throws Exception {
		final Variant result=defaultVariantBuilder().variant();
		assertThat(result,instanceOf(ImmutableVariant.class));
		checkDefaultValues(result);
	}

	@Test
	public void variantBuildMethodProducesNewInstances() throws Exception {
		final Variant original=defaultVariantBuilder().variant();
		final Variant copy=Variants.from(original).variant();
		assertThat(copy,not(sameInstance(original)));
		checkDefaultValues(copy);
	}

	@Test
	public void alternativeBuildMethodProducesImmutableAlternativeObjects() throws Exception {
		final Alternative result=defaultVariantBuilder().alternative(0.123D);
		assertThat(result,instanceOf(ImmutableAlternative.class));
		checkDefaultValues(result);
	}

	@Test
	public void nullVariantsAreEqual() throws Exception {
		assertThat(Variants.equals(null,null),equalTo(true));
	}

	@Test
	public void nullIsNotEqualToAnyVariant(@Mocked final Variant variant) {
		assertThat(Variants.equals(null,variant),equalTo(false));
	}

	@Test
	public void noVariantIsEqualToNull(@Mocked final Variant variant) {
		assertThat(Variants.equals(variant,null),equalTo(false));
	}

	@Test
	public void variantsAreNotEqualIfMediaTypesAreDifferent() {
		Variant v1=Variants.builder().type(MediaTypes.of("text","plain")).variant();
		Variant v2=Variants.builder().type(MediaTypes.of("text","turtle")).alternative(0.0D);
		assertThat(Variants.equals(v1,v2),equalTo(false));
	}

	@Test
	public void variantsAreNotEqualIfCharacterEncodingsAreDifferent() {
		Variant v1=Variants.builder().charset(CharacterEncodings.of(StandardCharsets.UTF_8)).variant();
		Variant v2=Variants.builder().charset(CharacterEncodings.of(StandardCharsets.US_ASCII)).alternative(0.0D);
		assertThat(Variants.equals(v1,v2),equalTo(false));
	}

	@Test
	public void variantsAreNotEqualIfLanguagesAreDifferent() {
		Variant v1=Variants.builder().language(Languages.of(Locale.ENGLISH)).variant();
		Variant v2=Variants.builder().language(Languages.of(Locale.GERMAN)).alternative(0.0D);
		assertThat(Variants.equals(v1,v2),equalTo(false));
	}

	@Test
	public void variantsAreEqualIfBothHaveNoComponents() {
		Variant v1=Variants.builder().variant();
		Variant v2=Variants.builder().alternative(0.0D);
		assertThat(Variants.equals(v1,v2),equalTo(true));
	}

	@Test
	public void variantsAreEqualIfAllTheirComponentsMatch() {
		Variant v1=defaultVariantBuilder().variant();
		Variant v2=Variants.from(v1).alternative(0.0D);
		assertThat(Variants.equals(v1,v2),equalTo(true));
	}

	private VariantBuilder defaultVariantBuilder() {
		return
			Variants.
				builder().
					type(MEDIA_TYPE).
					charset(CHARACTER_ENCODING).
					language(LANGUAGE);
	}

	private void checkDefaultValues(final Variant result) {
		assertThat(result.type(),equalTo(MEDIA_TYPE));
		assertThat(result.language(),equalTo(LANGUAGE));
		assertThat(result.charset(),equalTo(CHARACTER_ENCODING));
	}

}
