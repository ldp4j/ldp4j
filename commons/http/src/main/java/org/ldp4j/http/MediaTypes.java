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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Maps;

public final class MediaTypes {

	public static final class MediaTypeBuilder {

		private final MediaType mediaType;
		private final Map<String, String> parameters;
		private MediaRangeSyntax syntax;

		public MediaTypeBuilder(MediaType mediaType) {
			this.mediaType = mediaType;
			this.parameters=Maps.newLinkedHashMap(mediaType.parameters());
			this.syntax=preferredSyntax();
		}

		public MediaTypeBuilder withSyntax(MediaRangeSyntax syntax) {
			if(syntax==null) {
				this.syntax=preferredSyntax();
			} else {
				this.syntax=syntax;
			}
			return this;
		}

		public MediaTypeBuilder withCharset(Charset charset) {
			if(charset==null) {
				this.parameters.remove(MediaTypes.PARAM_CHARSET);
			} else {
				this.parameters.put(MediaTypes.PARAM_CHARSET,charset.name());
			}
			return this;
		}

		public MediaTypeBuilder withParam(String name, String value) {
			requireNonNull(name,"Parameter name cannot be null");
			requireNonNull(value,"Parameter value cannot be null");
			this.parameters.put(name, value);
			return this;
		}

		public MediaType build() {
			return
				new ImmutableMediaType(
					this.syntax,
					this.mediaType.type(),
					this.mediaType.subType(),
					this.mediaType.suffix(),
					this.parameters);
		}

	}

	/**
	 * The wildcard type/subtype
	 */
	public static final String WILDCARD_TYPE   = "*";

	private static final String MEDIA_TYPE_CANNOT_BE_NULL           = "Media type cannot be null";
	private static final String TYPE_CANNOT_BE_NULL                 = "Type cannot be null";
	private static final String REFERENCE_MEDIA_TYPE_CANNOT_BE_NULL = "Reference media type cannot be null";
	/**
	 * The key for the standard 'charset' media type parameter
	 */
	public static final String PARAM_CHARSET   = "charset";

	private static final AtomicReference<MediaRangeSyntax> SYNTAX=new AtomicReference<>(MediaRangeSyntax.RFC7230);

	private MediaTypes() {
	}

	/**
	 * Get the preferred syntax used for parsing media types
	 *
	 * @return the syntax
	 */
	public static MediaRangeSyntax preferredSyntax() {
		return SYNTAX.get();
	}

	/**
	 * Set the preferred syntax to use for parsing media types. If the specified
	 * syntax is {@code null}, RFC7230 syntax will be used as default.
	 *
	 * @param syntax
	 *            the syntax
	 */
	public static void preferredSyntax(MediaRangeSyntax syntax) {
		if(syntax==null) {
			SYNTAX.set(MediaRangeSyntax.RFC7230);
		} else {
			SYNTAX.set(syntax);
		}
	}

	/**
	 * Create a wildcard media type
	 *
	 * @return a wildcard media type
	 */
	public static MediaType wildcard() {
		return new ImmutableMediaType(MediaTypes.preferredSyntax(),WILDCARD_TYPE,WILDCARD_TYPE,null,null);
	}

	/**
	 * Create a wildcard media type for a given primary type
	 *
	 * @param type
	 *            The media type primary type
	 * @return a wildcard media type for the given primary type
	 * @throws NullPointerException
	 *             if the type is null
	 * @throws IllegalArgumentException
	 *             if the specified type is not valid
	 */
	public static MediaType wildcard(String type) {
		requireNonNull(type,TYPE_CANNOT_BE_NULL);
		return new ImmutableMediaType(MediaTypes.preferredSyntax(),type,WILDCARD_TYPE,null,null);
	}

	/**
	 * Create a wildcard structured-syntax media type for a given primary type
	 *
	 * @param type
	 *            The media type primary type
	 * @param suffix
	 *            The suffix associated to the structured-syntax
	 * @return a wildcard media type for the given primary type and suffix
	 * @throws NullPointerException
	 *             if the type or suffix is null
	 * @throws IllegalArgumentException
	 *             if the specified type or suffix is not valid
	 */
	public static MediaType wildcard(String type, String suffix) {
		requireNonNull(type,TYPE_CANNOT_BE_NULL);
		requireNonNull(suffix,"Suffix cannot be null");
		return new ImmutableMediaType(MediaTypes.preferredSyntax(),type,WILDCARD_TYPE,suffix,null);
	}

	/**
	 * Parse the given String into a single {@code MediaType}.
	 *
	 * @param mediaType
	 *            the string to parse
	 * @return the mime type
	 * @throws InvalidMediaTypeException
	 *             if the string cannot be parsed
	 */
	public static MediaType fromString(final String mediaType) {
		return ImmutableMediaType.fromString(mediaType, MediaTypes.preferredSyntax());
	}

	/**
	 * Create a media type
	 *
	 * @param type
	 *            The media type primary type
	 * @param subtype
	 *            The media type subtype
	 * @return A media type for the specified media range
	 * @throws NullPointerException
	 *             if any of the specified media range elements is null
	 * @throws InvalidMediaTypeException
	 *             if any of the specified media range elements is not valid
	 */
	public static MediaType of(String type, String subtype) {
		requireNonNull(type,TYPE_CANNOT_BE_NULL);
		requireNonNull(subtype,"Subtype cannot be null");
		return fromString(type+"/"+subtype);
	}

	/**
	 * Create a structured-syntax media type
	 *
	 * @param type
	 *            The media type primary type
	 * @param subtype
	 *            The media type subtype
	 * @param suffix
	 *            The suffix associated to the structured-syntax
	 * @return A media type for the specified media range
	 * @throws NullPointerException
	 *             if any of the specified media range elements is null
	 * @throws InvalidMediaTypeException
	 *             if any of the specified media range elements is not valid
	 */
	public static MediaType of(String type, String subtype, String suffix) {
		requireNonNull(type,TYPE_CANNOT_BE_NULL);
		requireNonNull(subtype,"Subtype cannot be null");
		requireNonNull(suffix,"Suffix cannot be null");
		return fromString(type+"/"+subtype+"+"+suffix);
	}

	/**
	 * Create a {@code MediaTypeBuilder} from a given media type.
	 *
	 * @param mediaType
	 *            The media type to use as builder configuration
	 * @return a builder instance prepopulated with the specified media type.
	 * @throws NullPointerException
	 *             if the media type is null
	 */
	public static MediaTypeBuilder from(MediaType mediaType) {
		requireNonNull(mediaType,"Media type cannot be null");
		return new MediaTypeBuilder(mediaType);
	}

	/**
	 * Indicates whether the {@linkplain MediaType#type() type} is the
	 * wildcard character <code>&#42;</code>.
	 *
	 * @param mediaType
	 *            the instance to verify
	 * @return whether the type of the specified instance is a wildcard
	 * @throws NullPointerException
	 *             if the specified instance is null
	 */
	public static boolean isWildcardType(final MediaType mediaType) {
		requireNonNull(mediaType,MEDIA_TYPE_CANNOT_BE_NULL);
		return WILDCARD_TYPE.equals(mediaType.type());
	}

	/**
	 * Indicates whether the {@linkplain MediaType#subType() subtype} is the
	 * wildcard character <code>&#42;</code>.
	 *
	 * @param mediaType
	 *            the instance to verify
	 * @return whether the subtype of the specified instance is a wildcard
	 * @throws NullPointerException
	 *             if the specified instance is null
	 */
	public static boolean isWildcardSubType(final MediaType mediaType) {
		requireNonNull(mediaType,MEDIA_TYPE_CANNOT_BE_NULL);
		return WILDCARD_TYPE.equals(mediaType.subType());
	}

	/**
	 * Indicates whether the {@linkplain MediaType#suffix() suffix} is
	 * {@code null}.
	 *
	 * @param mediaType
	 *            the instance to verify
	 * @return {@code true} if the suffix of the specified instance is not
	 *         {@code null}; {@code false} otherwise.
	 * @throws NullPointerException
	 *             if the specified instance is null
	 */
	public static boolean isStructured(final MediaType mediaType) {
		requireNonNull(mediaType,MEDIA_TYPE_CANNOT_BE_NULL);
		return mediaType.suffix()!=null;
	}

	/**
	 * Indicate whether the former {@code MediaType} includes the latter.
	 * <p>
	 * For instance, {@code text/*} includes {@code text/plain} and
	 * {@code text/html}, and {@code application/*+xml} includes
	 * {@code application/soap+xml}, etc. This method is <b>not</b> symmetric.
	 *
	 * @param one
	 *            the reference media type with which to compare
	 * @param other
	 *            the media type to compare
	 * @return {@code true} if the reference media type includes the compared
	 *         media type; {@code false} otherwise
	 */
	public static boolean includes(final MediaType one, final MediaType other) {
		return areCompatibleMediaTypes(one,other,false);
	}

	/**
	 * Indicate whether the former {@code MediaType} is compatible with the
	 * latter.
	 * <p>
	 * For instance, {@code text/*} is compatible with {@code text/plain},
	 * {@code text/html}, and vice versa. In effect, this method is similar to
	 * {@link #includes}, except that it <b>is</b> symmetric.
	 *
	 * @param one
	 *            the reference media type with which to compare
	 * @param other
	 *            the media type to compare
	 * @return {@code true} if both media types are compatible; {@code false}
	 *         otherwise
	 */
	public static boolean areCompatible(final MediaType one, final MediaType other) {
		return areCompatibleMediaTypes(one,other,true);
	}

	/**
	 * Format the media type in a HTTP-header compliant manner using preferred
	 * format.
	 *
	 * @param mediaType
	 *            the media type to format
	 * @return the compliant representation of the media type
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.1">[RFC
	 *      7231] Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content,
	 *      section 3.1.1.1</a>
	 */
	public static String toHeader(final MediaType mediaType) {
		requireNonNull(mediaType,REFERENCE_MEDIA_TYPE_CANNOT_BE_NULL);
		final StringBuilder builder=
			new StringBuilder().
				append(mediaType.type().toLowerCase(Locale.ENGLISH)).
				append('/').
				append(mediaType.subType().toLowerCase(Locale.ENGLISH));
		final String suffix=mediaType.suffix();
		if(suffix!=null) {
			builder.append('+').append(suffix.toLowerCase(Locale.ENGLISH));
		}
		final Charset charset=mediaType.charset();
		if(charset!=null) {
			builder.append(";charset=").append(charset.name().toLowerCase(Locale.ENGLISH));
		}
		for(Entry<String,String> entry:mediaType.parameters().entrySet()) {
			final String key=entry.getKey();
			if(isStandardParameter(key)) {
				continue;
			}
			builder.append(';').append(key.toLowerCase(Locale.ENGLISH)).append('=').append(entry.getValue());
		}
		return builder.toString();
	}

	/**
	 * Check whether or not the specified parameter is a standard Media Type
	 * parameter
	 *
	 * @param parameter
	 *            the parameter to check
	 * @return {@code true} if the parameter is standard, or {@code false}
	 *         otherwise.
	 */
	public static boolean isStandardParameter(final String parameter) {
		return PARAM_CHARSET.equalsIgnoreCase(parameter);
	}

	private static boolean areCompatibleMediaTypes(final MediaType one, final MediaType other, boolean symmetric) {
		requireNonNull(one,REFERENCE_MEDIA_TYPE_CANNOT_BE_NULL);
		if(other==null) {
			return false;
		}
		return haveCompatibleMediaRange(one, other, symmetric);
	}

	private static boolean haveCompatibleMediaRange(final MediaType one, final MediaType other, boolean symmetric) {
		if(isWildcardType(one)) {
			return true;
		}
		if(symmetric && isWildcardType(other)) {
			return true;
		}
		if(!equalsIgnoreCase(one.suffix(), other.suffix())) {
			return false;
		}
		return
			one.type().equalsIgnoreCase(other.type()) &&
			haveCompatibleSubtype(one, other, symmetric);
	}

	private static boolean haveCompatibleSubtype(final MediaType one, final MediaType other, boolean symmetric) {
		if(isWildcardSubType(one)) {
			return true;
		}
		if(symmetric && isWildcardSubType(other)) {
			return true;
		}
		return one.subType().equalsIgnoreCase(other.subType());
	}

	private static boolean equalsIgnoreCase(String str, String anotherStr) {
		return str==null ? anotherStr==null : str.equalsIgnoreCase(anotherStr);
	}

}
