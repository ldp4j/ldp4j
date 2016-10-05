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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map.Entry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.ldp4j.http.Quality.Type;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

public class ContentNegotiatorTest {


	@Rule
	public TestName name=new TestName();

	private static final MediaType TEXT_HTML = MediaTypes.of("text", "html");
	private static final MediaType POSTSCRIPT = MediaTypes.of("application", "postscript");
	private static final CharacterEncoding UTF_8 = CharacterEncodings.of(StandardCharsets.UTF_8);
	private static final CharacterEncoding US_ASCII = CharacterEncodings.of(StandardCharsets.US_ASCII);
	private static final CharacterEncoding ISO_8859_1 = CharacterEncodings.of(StandardCharsets.ISO_8859_1);
	private static final Language FRENCH = Languages.of(Locale.FRENCH);
	private static final Language ENGLISH = Languages.of(Locale.ENGLISH);

	private static final Language GERMAN = Languages.of(Locale.GERMAN);

	@Test
	public void negotiatesWithNoSupportedAlternatives() throws Exception {
		checkCannotNegotiate(specRequirementsNegotiator());
	}

	@Test
	public void negotiatesWithNoAcceptableRequirements() throws Exception {
		NegotiationResult result =
			baseNegotiator().
				support(POSTSCRIPT).
				support(TEXT_HTML).
				support(ENGLISH).
				support(FRENCH).
				negotiate();
		checkNegotiation(
			result,
			true,
			POSTSCRIPT,
			ContentNegotiator.DEFAULT_CHARACTER_ENCODING,
			ENGLISH,
			Optional.of(Quality.Type.DEFINITE),
			null,
			Optional.of(4));
	}

	@Test
	public void negotiatesWithGenericSupportedEntities() throws Exception {
		NegotiationResult result =
			specRequirementsNegotiator().
				support(POSTSCRIPT).
				support(TEXT_HTML).
				support(ENGLISH).
				support(FRENCH).
				negotiate();
		checkNegotiation(
			result,
			true,
			TEXT_HTML,
			ContentNegotiator.DEFAULT_CHARACTER_ENCODING,
			ENGLISH,
			Optional.of(Quality.Type.DEFINITE),
			null,
			Optional.of(4));
	}

	@Test
	public void negotiatesWithOverridingAlternatives() throws Exception {
		NegotiationResult result =
			specRequirementsNegotiator().
				support(ContentNegotiator.DEFAULT_MEDIA_TYPE).
				support(POSTSCRIPT).
				support(FRENCH).
				support(ENGLISH).
				support(Variants.builder().type(TEXT_HTML).language(ENGLISH).alternative(0.9D)).
				support(Variants.builder().type(TEXT_HTML).language(FRENCH).alternative(0.7D)).
				support(Variants.builder().type(POSTSCRIPT).language(ENGLISH).alternative(1.0D)).
				negotiate();

		checkNegotiation(
			result,
			true,
			TEXT_HTML,
			null,
			ENGLISH,
			Optional.of(Quality.Type.DEFINITE),
			null,
			Optional.of(6));
	}

	@Test
	public void negotiationMightResultInSpeculativeChoices() throws Exception {
		NegotiationResult result =
			baseNegotiator().
				accept("text/*;q=0.2").
				accept("*/*;q=1.0").
				acceptLanguage("en;q=1.0").
				acceptLanguage("fr;q=0.5").
				support(Variants.builder().type(TEXT_HTML).language(ENGLISH).alternative(1.0D)).
				support(Variants.builder().type(TEXT_HTML).language(FRENCH).alternative(0.8D)).
				support(Variants.builder().type(POSTSCRIPT).language(ENGLISH).alternative(0.5D)).
				negotiate();

		checkNegotiation(
			result,
			true,
			POSTSCRIPT,
			null,
			ENGLISH,
			Optional.of(Quality.Type.SPECULATIVE),
			null,
			Optional.of(3));
	}

	@Test
	public void negotiationDoesNotEnrichPredefinedAlternatives() throws Exception {
		NegotiationResult result =
			baseNegotiator().
				accept("text/*;q=0.2").
				accept("*/*;q=1.0").
				acceptLanguage("en;q=1.0").
				acceptLanguage("fr;q=0.5").
				acceptCharset("us-ascii;q=0.1").
				acceptCharset("*;q=0.5").
				support(Variants.builder().type(TEXT_HTML).language(ENGLISH).alternative(1.0D)).
				support(Variants.builder().type(TEXT_HTML).language(FRENCH).alternative(0.8D)).
				support(Variants.builder().type(POSTSCRIPT).language(ENGLISH).alternative(0.5D)).
				negotiate();

		checkNegotiation(
			result,
			true,
			POSTSCRIPT,
			null,
			ENGLISH,
			Optional.of(Quality.Type.SPECULATIVE),
			null,
			Optional.of(3));
	}

	@Test
	public void negotiationDoesNotEnrichMissingAttributesWithSpeculativeValues() throws Exception {
		NegotiationResult result =
			baseNegotiator().
				accept("text/*;q=0.2").
				accept("*/*;q=1.0").
				acceptLanguage("en;q=1.0").
				acceptLanguage("fr;q=0.5").
				support(ENGLISH).
				support(FRENCH).
				negotiate();

		checkNegotiation(
			result,
			true,
			ContentNegotiator.DEFAULT_MEDIA_TYPE,
			ContentNegotiator.DEFAULT_CHARACTER_ENCODING,
			ENGLISH,
			Optional.of(Quality.Type.DEFINITE),
			null,
			Optional.of(2));
	}

	@Test
	public void negotiationEnrichesMissingAttributesWithDefiniteValues() throws Exception {
		NegotiationResult result =
			baseNegotiator().
				accept("text/*;q=0.2").
				accept("*/*;q=1.0").
				acceptLanguage("en;q=1.0").
				acceptLanguage("fr;q=0.5").
				acceptCharset("us-ascii;q=0.1").
				acceptCharset("utf-8;q=0.5").
				support(TEXT_HTML).
				support(POSTSCRIPT).
				support(ENGLISH).
				support(FRENCH).
				negotiate();

		checkNegotiation(
			result,
			true,
			POSTSCRIPT,
			UTF_8,
			ENGLISH,
			Optional.of(Quality.Type.SPECULATIVE),
			null,
			Optional.of(4));
	}

	@Test
	public void negotiatesWithPredefinedAlternatives() throws Exception {
		NegotiationResult result =
			specRequirementsNegotiator().
				support(Variants.builder().type(TEXT_HTML).language(ENGLISH).alternative(0.9D)).
				support(Variants.builder().type(TEXT_HTML).language(FRENCH).alternative(0.7D)).
				support(Variants.builder().type(POSTSCRIPT).language(ENGLISH).alternative(1.0D)).
				negotiate();

		checkNegotiation(
			result,
			true,
			TEXT_HTML,
			null,
			ENGLISH,
			Optional.of(Quality.Type.DEFINITE),
			null,
			Optional.of(3));
	}

	@Test
	public void doesNotAddContentLanguageHeaderIfNotAvailable() throws Exception {
		NegotiationResult result =
			specRequirementsNegotiator().
				support(Variants.builder().type(TEXT_HTML).alternative(0.7D)).
				support(Variants.builder().type(POSTSCRIPT).alternative(1.0D)).
				negotiate();

		checkNegotiation(
			result,
			true,
			TEXT_HTML,
			null,
			null,
			Optional.of(Quality.Type.DEFINITE),
			null,
			Optional.of(2));
		assertThat(result.responseHeaders(true).containsKey(ContentNegotiation.CONTENT_LANGUAGE),equalTo(false));
	}

	@Test
	public void failsNegotiationIfCannotFindAlternative() throws Exception {
		NegotiationResult result =
			specRequirementsNegotiator().
				support(Variants.builder().type(TEXT_HTML).language(GERMAN).alternative(0.7D)).
				support(Variants.builder().type(POSTSCRIPT).language(GERMAN).alternative(1.0D)).
				negotiate();

		checkNegotiation(
			result,
			false,
			null,
			null,
			null,
			null,
			null,
			Optional.of(2));
	}

	@Test
	public void negotiatesCharsetsOnlyRequirements() throws Exception {
		NegotiationResult result =
			baseNegotiator().
				support(UTF_8).
				support(US_ASCII).
				support(ISO_8859_1).
				acceptCharset("utf-8;q=0.3").
				acceptCharset("iso-8859-1;q=0.5").
				acceptCharset("*;q=0.8").
				negotiate();
		checkNegotiation(
			result,
			true,
			ContentNegotiator.DEFAULT_MEDIA_TYPE,
			ISO_8859_1,
			ContentNegotiator.DEFAULT_LANGUAGE,
			Optional.of(Quality.Type.DEFINITE),
			null,
			Optional.of(3));
	}

	@Test
	public void negotiatesWithoutMediaTypeAlternatives() throws Exception {
		NegotiationResult result =
			specRequirementsNegotiator().
				support(Variants.builder().language(FRENCH).alternative(0.7D)).
				support(Variants.builder().language(ENGLISH).alternative(1.0D)).
				negotiate();

		checkNegotiation(
			result,
			true,
			null,
			null,
			ENGLISH,
			Optional.of(Quality.Type.DEFINITE),
			null,
			Optional.of(2));
	}

	@Test
	public void ignoresNullAsSupportedMediaTypes() throws Exception {
		checkCannotNegotiate(
			specRequirementsNegotiator().
				support((MediaType)null));
	}

	@Test
	public void ignoresNullAsSupportedCharacterEncoding() throws Exception {
		checkCannotNegotiate(
			specRequirementsNegotiator().
				support((CharacterEncoding)null));
	}

	@Test
	public void ignoresNullAsSupportedLanguage() throws Exception {
		checkCannotNegotiate(
			specRequirementsNegotiator().
				support((Language)null));
	}

	@Test
	public void ignoresNullAsSupportedAlternative() throws Exception {
		checkCannotNegotiate(
			specRequirementsNegotiator().
				support((Alternative)null));
	}

	@Test
	public void defaultErrorVariantHasDefaultValues() throws Exception {
		checkDefaultErrorVariant(ContentNegotiator.newInstance().onError());
	}

	@Test
	public void canChangeErrorVariant() throws Exception {
		Variant original = ImmutableVariant.newInstance().type(POSTSCRIPT).charset(ISO_8859_1).language(GERMAN);
		final ContentNegotiator sut = ContentNegotiator.newInstance().onError(original);
		final Variant variant = sut.onError();
		assertThat(variant.type(),equalTo(original.type()));
		assertThat(variant.charset(),equalTo(original.charset()));
		assertThat(variant.language(),equalTo(original.language()));
	}

	@Test
	public void changingErrorVariantToNullResultsInDefaultErrorVariant() throws Exception {
		Variant original = ImmutableVariant.newInstance().type(POSTSCRIPT).charset(ISO_8859_1).language(GERMAN);
		final ContentNegotiator sut = ContentNegotiator.newInstance().onError(original).onError(null);
		checkDefaultErrorVariant(sut.onError());
	}

	private void checkCannotNegotiate(ContentNegotiator negotiator) {
		try {
			negotiator.negotiate();
			fail("Should not be able to negotiate if no alternatives where defined");
		} catch (CannotNegotiateException e) {
		}
	}

	private ContentNegotiator baseNegotiator() {
		return ContentNegotiator.newInstance();
	}

	private ContentNegotiator specRequirementsNegotiator() {
		return
			baseNegotiator().
				accept("text/html;q=1.0").
				accept("*/*;q=0.8").
				acceptLanguage("en;q=1.0").
				acceptLanguage("fr;q=0.5");
	}

	private void checkNegotiation(
			NegotiationResult negotiation,
			boolean accepted,
			MediaType type,
			CharacterEncoding charset,
			Language language,
			Optional<Type> qualityType,
			Optional<Double> qualityWeight,
			Optional<Integer> numberOfAlternatives) {
		dumpNegotiation(negotiation);
		assertThat(negotiation.isAcceptable(),equalTo(accepted));
		if(accepted) {
			assertThat(negotiation.variant().type(),equalTo(type));
			assertThat(negotiation.variant().charset(),equalTo(charset));
			assertThat(negotiation.variant().language(),equalTo(language));
		} else {
			assertThat(negotiation.variant(),nullValue());
			assertThat(negotiation.quality(),nullValue());
		}
		if(qualityWeight!=null) {
			assertThat(negotiation.quality().weight(),equalTo(qualityWeight.orNull()));
		}
		if(qualityType!=null) {
			assertThat(negotiation.quality().type(),equalTo(qualityType.orNull()));
		}
		if(numberOfAlternatives!=null) {
			assertThat(negotiation.alternatives().size(),equalTo(numberOfAlternatives.orNull()));
		}
	}

	private void dumpNegotiation(NegotiationResult negotiation) {
		System.out.println("Content negotiation ["+name.getMethodName()+"]:");
		if(negotiation.isAcceptable()) {
			System.out.printf("- Acceptable (%s): %s%n",negotiation.quality(),negotiation.variant());
		} else {
			System.out.printf("- Not acceptable%n");
		}
		try {
			dumpAlternatives(negotiation.alternatives());
		} catch (Exception e) {
			System.out.println("* Failed to dump alternatives: ");
			e.printStackTrace(System.out);
		}
		try {
			dumpHeaders(negotiation, true);
		} catch (Exception e) {
			System.out.println("* Failed to dump accepted headers: ");
			e.printStackTrace(System.out);
		}
		try {
			dumpHeaders(negotiation, false);
		} catch (Exception e) {
			System.out.println("* Failed to dump failed headers: ");
			e.printStackTrace(System.out);
		}
		System.out.println();
	}

	private void dumpAlternatives(Alternatives alternatives) {
		System.out.println("- Alternatives:");
		for(int i=0;i<alternatives.size();i++) {
			Alternative a=alternatives.alternative(i);
			Quality q=alternatives.quality(i);
			System.out.printf("  + %s %s%n",q,a);
		}
	}

	private void dumpHeaders(NegotiationResult result, boolean status) {
		System.out.printf("- %s response headers:%n",status?"Accepted":"Rejected");
		Multimap<String,String> headers=result.responseHeaders(status);
		for(Entry<String, Collection<String>> entry:headers.asMap().entrySet()) {
			System.out.printf("  +  %s: %s%n",entry.getKey(),Joiner.on(", ").join(entry.getValue()));
		}
	}

	private void checkDefaultErrorVariant(final Variant variant) {
		assertThat(variant.type(),equalTo(ContentNegotiator.DEFAULT_MEDIA_TYPE));
		assertThat(variant.charset(),equalTo(ContentNegotiator.DEFAULT_CHARACTER_ENCODING));
		assertThat(variant.language(),equalTo(ContentNegotiator.DEFAULT_LANGUAGE));
	}

}
