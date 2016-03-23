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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-http-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static java.util.Objects.requireNonNull;

public final class MediaTypes {

	/**
	 * The key for the standard 'charset' media type parameter
	 */
	public static final String PARAM_CHARSET   = "charset";

	/**
	 * The key for the standard 'quality' media type parameter
	 */
	public static final String PARAM_QUALITY   = "q";

	/**
	 * The wildcard type/subtype
	 */
	public static final String WILDCARD_TYPE   = "*";

	private MediaTypes() {
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
		return ImmutableMediaType.fromString(mediaType);
	}

	public static boolean isWildcardType(final MediaType mediaType) {
		requireNonNull(mediaType,"Media type cannot be null");
		return WILDCARD_TYPE.equals(mediaType.type());
	}

	/**
	 * Indicates whether the {@linkplain MediaType#subType() subtype} is the
	 * wildcard character <code>&#42;</code>.
	 *
	 * @return whether the subtype is a wildcard
	 */
	public static boolean isWildcardSubType(final MediaType mediaType) {
		requireNonNull(mediaType,"Media type cannot be null");
		return WILDCARD_TYPE.equals(mediaType.subType());
	}

	public static boolean isStructured(final MediaType mediaType) {
		requireNonNull(mediaType,"Media type cannot be null");
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
		requireNonNull(one,"Reference media type cannot be null");
		if(other==null) {
			return false;
		}
		return
			equalsIgnoreCase(one.suffix(), other.suffix()) &&
			(isWildcardType(one) ||
				(one.type().equalsIgnoreCase(other.type()) &&
					(isWildcardSubType(one) || one.subType().equalsIgnoreCase(other.subType()))));
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
		requireNonNull(one,"Reference media type cannot be null");
		if(other==null) {
			return false;
		}
		return
			equalsIgnoreCase(one.suffix(), other.suffix()) &&
			(isWildcardType(one) || isWildcardType(other)||
				(one.type().equalsIgnoreCase(other.type()) &&
					(isWildcardSubType(one) || isWildcardSubType(other) || one.subType().equalsIgnoreCase(other.subType()))));
	}

	private static boolean equalsIgnoreCase(String str, String anotherStr) {
		return str==null ? anotherStr==null : str.equalsIgnoreCase(anotherStr);
	}

}
