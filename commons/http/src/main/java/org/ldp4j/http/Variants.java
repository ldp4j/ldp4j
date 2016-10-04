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

import static java.util.Objects.requireNonNull;

import java.nio.charset.Charset;
import java.util.Objects;

public final class Variants {

	public static final class VariantBuilder {

		private ImmutableVariant variant;

		private VariantBuilder() {
			this.variant=ImmutableVariant.newInstance();
		}

		public VariantBuilder type(MediaType type) {
			this.variant=this.variant.type(type);
			return this;
		}

		public VariantBuilder charset(CharacterEncoding charset) {
			this.variant=this.variant.charset(charset);
			return this;
		}

		public VariantBuilder language(Language language) {
			this.variant=this.variant.language(language);
			return this;
		}

		public Variant variant() {
			return this.variant;
		}

		public Alternative alternative(double quality) {
			return ImmutableAlternative.create(quality,this.variant);
		}

	}

	private Variants() {
	}

	public static boolean equals(final Variant v1, final Variant v2) { // NOSONAR
		if(v1==v2) {
			return true;
		}
		if(v1==null || v2==null) {
			return false;
		}
		return
			Objects.equals(v1.type(), v2.type()) &&
			Objects.equals(v1.charset(), v2.charset()) &&
			Objects.equals(v1.language(), v2.language());
	}

	public static MediaType contentType(Variant variant) {
		requireNonNull(variant, "Variant cannot be null");
		MediaType result=null;
		final MediaType type=variant.type();
		if(type!=null) {
			result=assemble(type,charset(variant.charset()));
		}
		return result;
	}

	public static VariantBuilder builder() {
		return new VariantBuilder();
	}

	public static VariantBuilder from(Variant variant) {
		requireNonNull(variant,"Variant cannot be null");
		return
			builder().
				type(variant.type()).
				charset(variant.charset()).
				language(variant.language());
	}

	private static Charset charset(CharacterEncoding characterEncoding) {
		Charset charset=null;
		if(characterEncoding!=null) {
			charset=characterEncoding.charset();
		}
		return charset;
	}

	private static MediaType assemble(MediaType mediaType, Charset charset) {
		return
			MediaTypes.
				from(mediaType).
					withCharset(charset).
					build();
	}

}
