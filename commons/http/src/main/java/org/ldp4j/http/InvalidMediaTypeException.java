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


/**
 * Exception thrown from {@link ImmutableMediaType#fromString(String, MediaRangeSyntax)} in case of
 * encountering an invalid media type specification String.
 */
public class InvalidMediaTypeException extends IllegalArgumentException {

	private static final long serialVersionUID = -2107062439428302429L;

	private final String mediaType;

	/**
	 * Create a new InvalidMediaTypeException for the given media type.
	 *
	 * @param mediaType
	 *            the offending media type
	 * @param message
	 *            a detail message indicating the invalid part
	 */
	public InvalidMediaTypeException(final String mediaType, final String message) {
		this(mediaType,null,message);
	}

	/**
	 * Create a new InvalidMediaTypeException for the given media type and
	 * cause.
	 *
	 * @param mediaType
	 *            the offending media type
	 * @param cause
	 *            the root cause of the failure
	 * @param message
	 *            a detail message indicating the invalid part
	 */
	public InvalidMediaTypeException(final String mediaType, final Throwable cause, final String message) {
		super(message,cause);
		this.mediaType = mediaType;
	}

	/**
	 * Create a new InvalidMediaTypeException without an offending media type
	 * (i.e., when no one is provided)
	 *
	 * @param message
	 *            a detail message indicating the problem
	 */
	public InvalidMediaTypeException(final String message) {
		this(null,message);
	}

	/**
	 * Get the offending media type.
	 *
	 * @return the offending media type.
	 */
	public String getMediaType() {
		return this.mediaType;
	}

}