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
import java.util.Objects;

import com.google.common.collect.ImmutableList;

public final class ContentNegotiator {

	static final MediaType         DEFAULT_MEDIA_TYPE         = MediaTypes.of("text","plain");
	static final Language          DEFAULT_LANGUAGE           = Languages.of(Locale.ENGLISH);
	static final CharacterEncoding DEFAULT_CHARACTER_ENCODING = CharacterEncodings.of(StandardCharsets.UTF_8);

	private final ImmutableList.Builder<MediaType> mediaTypes;
	private final ImmutableList.Builder<CharacterEncoding> characterEncodings;
	private final ImmutableList.Builder<Language> languages;
	private final ImmutableList.Builder<Alternative> supported;

	private final ImmutableList.Builder<Weighted<MediaType>> accepts;
	private final ImmutableList.Builder<Weighted<CharacterEncoding>> acceptCharsets;
	private final ImmutableList.Builder<Weighted<Language>> acceptLanguages;

	private ImmutableVariant errorVariant;

	private ContentNegotiator() {
		this.mediaTypes=ImmutableList.builder();
		this.characterEncodings=ImmutableList.builder();
		this.languages=ImmutableList.builder();
		this.supported=ImmutableList.builder();
		this.accepts=ImmutableList.builder();
		this.acceptCharsets=ImmutableList.builder();
		this.acceptLanguages=ImmutableList.builder();
		this.errorVariant=defaultErrorVariant();
	}

	private ImmutableVariant defaultErrorVariant() {
		return ImmutableVariant.
			newInstance().
				charset(DEFAULT_CHARACTER_ENCODING).
				language(DEFAULT_LANGUAGE).
				type(DEFAULT_MEDIA_TYPE);
	}

	public Variant onError() {
		return this.errorVariant;
	}

	public ContentNegotiator onError(final Variant variant) {
		if(variant!=null) {
			this.errorVariant=ImmutableVariant.copyOf(variant);
		} else {
			this.errorVariant=defaultErrorVariant();
		}
		return this;
	}

	public ContentNegotiator support(final MediaType mediaType) {
		if(mediaType!=null) {
			this.mediaTypes.add(mediaType);
		}
		return this;
	}

	public ContentNegotiator support(final CharacterEncoding characterEncoding) {
		if(characterEncoding!=null) {
			this.characterEncodings.add(characterEncoding);
		}
		return this;
	}

	public ContentNegotiator support(final Language language) {
		if(language!=null) {
			this.languages.add(language);
		}
		return this;
	}

	public ContentNegotiator support(final Alternative alternative) {
		if(alternative!=null) {
			this.supported.add(alternative);
		}
		return this;
	}

	public ContentNegotiator accept(final String header) {
		Objects.requireNonNull(header,"Accept header cannot be null");
		this.accepts.add(ContentNegotiationUtils.accept(header));
		return this;
	}

	public ContentNegotiator acceptCharset(final String header) {
		Objects.requireNonNull(header,"Accept-Charset header cannot be null");
		this.acceptCharsets.add(ContentNegotiationUtils.acceptCharset(header));
		return this;
	}

	public ContentNegotiator acceptLanguage(final String header) {
		Objects.requireNonNull(header,"Accept-Language header cannot be null");
		this.acceptLanguages.add(ContentNegotiationUtils.acceptLanguage(header));
		return this;
	}

	/**
	 * Carry out the content negotiation taking into account both the supported
	 * and the acceptable media types, character encodings, and languages.
	 * <p>
	 * The method implements a simplified version of the <b>Remote Variant
	 * Selection Algorithm</b> defined in <a
	 * href="https://tools.ietf.org/html/rfc2296">RFC 2296</a>.
	 * <p>
	 * The differences between the implemented algorithm and the original one
	 * are the following:
	 * <ol>
	 * <li>There is no support for <i>features</i>,
	 * <li>Variant <i>source-quality</i> is assigned linearly (with ratio
	 * -0.125) of the values suggested in Section 5.3 of RFC 2296;
	 * <li>The <i>round5</i> function is not applied during the computation of
	 * overall quality values.
	 * </ol>
	 * The reason for this latter difference is that in the presence of
	 * fine-grained qualities (<i>i.e.</i>, 0.001) computed quality values
	 * converge to 10<sup>-12</sup>, and can make variants with non-zero
	 * computed qualities appear like they are not amenable to selection.
	 *
	 * @return the result of the content negotiation
	 * @see <a href="https://tools.ietf.org/html/rfc2296">RFC 2296: HTTP Remote
	 *      Variant Selection Algorithm -- RVSA/1.0</a>
	 */
	public NegotiationResult negotiate() {
		return
			new ContentNegotiationAlgorithm(
				this.mediaTypes.build(),
				this.characterEncodings.build(),
				this.languages.build(),
				this.supported.build(),
				this.accepts.build(),
				this.acceptCharsets.build(),
				this.acceptLanguages.build(),
				this.errorVariant).
				execute();
	}

	public static ContentNegotiator newInstance() {
		return new ContentNegotiator();
	}

}
