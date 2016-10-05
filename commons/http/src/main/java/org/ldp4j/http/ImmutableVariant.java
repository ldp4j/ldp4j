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

import java.nio.charset.Charset;

import com.google.common.base.MoreObjects;

final class ImmutableVariant implements Variant {

	private final ImmutableMediaType mediaType;
	private final ImmutableCharacterEncoding characterEncoding;
	private final ImmutableLanguage language;

	private ImmutableVariant(ImmutableMediaType mediaType, ImmutableCharacterEncoding characterEncoding, ImmutableLanguage language) {
		this.mediaType=mediaType;
		this.characterEncoding=characterEncoding;
		this.language=language;
	}

	ImmutableVariant type(final MediaType mediaType) {
		MediaType type=mediaType;
		CharacterEncoding charset=this.characterEncoding;
		if(mediaType!=null) {
			checkArgument(!mediaType.isWildcard(),"Type cannot be a wildcard media type (%s)",mediaType);
			final Charset typeCharset=mediaType.charset();
			if(typeCharset!=null) {
				type=MediaTypes.from(mediaType).withCharset(null).build();
				charset=CharacterEncodings.of(typeCharset);
			}
		}
		return new ImmutableVariant((ImmutableMediaType)type,ImmutableCharacterEncoding.copyOf(charset),this.language);
	}

	ImmutableVariant charset(CharacterEncoding charset) {
		checkArgument(charset==null || !charset.isWildcard(),"Charset cannot be a wildcard character encoding (%s)",charset);
		return new ImmutableVariant(this.mediaType,ImmutableCharacterEncoding.copyOf(charset),this.language);
	}

	ImmutableVariant language(Language language) {
		checkArgument(language==null || !language.isWildcard(),"Language cannot be a wildcard language (%s)",language);
		return new ImmutableVariant(this.mediaType,this.characterEncoding,ImmutableLanguage.copyOf(language));
	}

	@Override
	public ImmutableMediaType type() {
		return this.mediaType;
	}

	@Override
	public ImmutableCharacterEncoding charset() {
		return this.characterEncoding;
	}

	@Override
	public ImmutableLanguage language() {
		return this.language;
	}

	static ImmutableVariant newInstance() {
		return new ImmutableVariant(null,null,null);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("type",this.mediaType).
					add("charset",this.characterEncoding).
					add("language",this.language).
					toString();
	}

	static ImmutableVariant copyOf(Variant other) {
		ImmutableVariant result=null;
		if(other instanceof ImmutableVariant) {
			result=(ImmutableVariant)other;
		} else if(other!=null) {
			result=
				new ImmutableVariant(
					ImmutableMediaType.copyOf(other.type()),
					ImmutableCharacterEncoding.copyOf(other.charset()),
					ImmutableLanguage.copyOf(other.language())
					);
		}
		return result;

	}

}
