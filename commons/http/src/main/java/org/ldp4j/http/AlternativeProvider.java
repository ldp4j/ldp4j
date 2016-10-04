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

import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.math.IntMath;

final class AlternativeProvider implements Iterator<Alternative> {

	private static final int    MAX_BUCKETS    = 8;
	private static final double BUCKET_QUALITY = 1.0D / MAX_BUCKETS;

	private final EntityProvider<MediaType> mediaTypes;
	private final EntityProvider<CharacterEncoding> characterEncodings;
	private final EntityProvider<Language> languages;
	private final int size;
	private final int buckets;

	private int position;

	AlternativeProvider(List<MediaType> mediaTypes, List<CharacterEncoding> characterEncodings, List<Language> languages) {
		this.mediaTypes=EntityProvider.of(mediaTypes);
		this.characterEncodings=EntityProvider.of(characterEncodings);
		this.languages=EntityProvider.of(languages);
		this.size=
			this.mediaTypes.consumableEntities()*
			this.characterEncodings.consumableEntities()*
			this.languages.consumableEntities();
		this.position=0;
		this.buckets=IntMath.divide(this.size,MAX_BUCKETS,RoundingMode.CEILING);
	}

	@Override
	public boolean hasNext() {
		return !this.mediaTypes.isExhausted() && hasMoreVariants();
	}

	@Override
	public ImmutableAlternative next() {
		if(!hasNext()) {
			throw new NoSuchElementException("No more alternatives available");
		}
		final ImmutableAlternative alternative=
			ImmutableAlternative.create(currentQuality(),currentVariant());
		discardVariant();
		return alternative;

	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Alternative removal is not supported");
	}

	private boolean hasMoreVariants() {
		return
			this.mediaTypes.hasMoreEntities() ||
			this.characterEncodings.hasMoreEntities() ||
			this.languages.hasMoreEntities();
	}

	private void discardVariant() {
		this.position++;
		this.languages.discard();
		if(!this.languages.hasMoreEntities()) {
			this.languages.reset();
			if(hasCharset(this.mediaTypes.entity())) {
				skipCharacterEncodings();
			} else {
				discardCharacterEncoding();
			}
		}
	}

	private void discardCharacterEncoding() {
		this.characterEncodings.discard();
		if(!this.characterEncodings.hasMoreEntities()) {
			discardMediaType();
		}
	}

	private void skipCharacterEncodings() {
		this.position+=
			(this.characterEncodings.consumableEntities()-1)*this.languages.consumableEntities()-1;
		discardMediaType();
	}

	private void discardMediaType() {
		this.characterEncodings.reset();
		this.mediaTypes.discard();
	}

	private ImmutableVariant currentVariant() {
		return
			ImmutableVariant.
				newInstance().
					language(this.languages.entity()).
					charset(this.characterEncodings.entity()).
					type(this.mediaTypes.entity());
	}

	private double currentQuality() {
		return
			DoubleUtils.
				limitPrecision(
					1.0D-IntMath.divide(this.position,this.buckets,RoundingMode.HALF_UP)*BUCKET_QUALITY,
					3);
	}

	private static boolean hasCharset(final MediaType mediaType) {
		return mediaType!=null && mediaType.charset()!=null;
	}


}