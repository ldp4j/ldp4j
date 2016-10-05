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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

final class ImmutableNegotiationResult implements NegotiationResult {

	private final ImmutableSet<MediaType> mediaTypes;
	private final ImmutableSet<CharacterEncoding> characterEncodings;
	private final ImmutableSet<Language> languages;

	private final ImmutableVariant variant;
	private final ImmutableQuality quality;
	private final ImmutableVariant errorVariant;
	private final ImmutableAlternatives alternatives;

	ImmutableNegotiationResult(final ImmutableVariant variant, ImmutableQuality quality, final ImmutableVariant errorVariant, ImmutableAlternatives alternatives) {
		checkArgument(
			(variant==null && quality==null) ||
			(variant!=null && quality!=null),
			"Variant and quality must be simultaneously defined or not (%s <--> %s)",variant,quality);
		checkNotNull(errorVariant,"Error variant cannot be null");
		checkNotNull(alternatives,"Alternatives cannot be null");
		this.variant=variant;
		this.quality=quality;
		this.errorVariant=errorVariant;
		this.alternatives=alternatives;
		Builder<MediaType> mtBuilder=ImmutableSet.<MediaType>builder();
		Builder<CharacterEncoding> ceBuilder=ImmutableSet.<CharacterEncoding>builder();
		Builder<Language> lBuilder=ImmutableSet.<Language>builder();
		for(Alternative alternative:alternatives) {
			addEntityIfPresent(mtBuilder,alternative.type());
			addEntityIfPresent(ceBuilder,alternative.charset());
			addEntityIfPresent(lBuilder,alternative.language());
		}
		this.mediaTypes=mtBuilder.build();
		this.characterEncodings=ceBuilder.build();
		this.languages=lBuilder.build();
	}

	private static <T> void addEntityIfPresent(Builder<T> builder, T entity) {
		if(entity!=null) {
			builder.add(entity);
		}
	}

	@Override
	public boolean isAcceptable() {
		return this.variant != null;
	}

	@Override
	public ImmutableVariant variant() {
		return this.variant;
	}

	@Override
	public ImmutableQuality quality() {
		return this.quality;
	}

	@Override
	public ImmutableAlternatives alternatives() {
		return this.alternatives;
	}

	@Override
	public Multimap<String,String> responseHeaders(final boolean accepted) {
		final Multimap<String,String> headers=contentNegotiationHeaders();
		addContentHeaders(headers,accepted?this.variant:this.errorVariant);
		return headers;
	}

	private Multimap<String,String> contentNegotiationHeaders() {
		final Multimap<String,String> headers=LinkedHashMultimap.create();
		addVariantHeaders(headers,this.mediaTypes,ContentNegotiation.ACCEPT);
		addVariantHeaders(headers,this.characterEncodings,ContentNegotiation.ACCEPT_CHARSET);
		addVariantHeaders(headers,this.languages,ContentNegotiation.ACCEPT_LANGUAGE);
		return headers;
	}

	private <T extends Negotiable> void addVariantHeaders(final Multimap<String,String> headers, final Set<T> headerCandidates, final String headerName) {
		if(!headerCandidates.isEmpty()) {
			headers.put(ContentNegotiation.VARY,headerName);
			for(Negotiable candidate:headerCandidates) {
				headers.put(headerName,candidate.toHeader());
			}
		}
	}

	private void addContentHeaders(final Multimap<String,String> headers, final Variant variant) {
		headers.put(ContentNegotiation.CONTENT_TYPE,Variants.contentType(variant).toHeader());
		if(variant.language()!=null) {
			headers.put(ContentNegotiation.CONTENT_LANGUAGE,variant.language().toHeader());
		}
	}

}