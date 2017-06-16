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

public final class ContentNegotiation {

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * <a href="https://tools.ietf.org/html/rfc7231#section-5.3.2">Section 5.3.2</a>.
	 */
	public static final String ACCEPT = "Accept";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * <a href="https://tools.ietf.org/html/rfc7231#section-5.3.3">Section 5.3.3</a>.
	 */
	public static final String ACCEPT_CHARSET = "Accept-Charset";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * <a href="https://tools.ietf.org/html/rfc7231#section-5.3.4">Section 5.3.4</a>.
	 */
	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * <a href="https://tools.ietf.org/html/rfc7231#section-5.3.5">Section 5.3.5</a>.
	 */
	public static final String ACCEPT_LANGUAGE = "Accept-Language";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * <a href="https://tools.ietf.org/html/rfc7231#section-7.1.4">Section 7.1.4</a>.
	 */
	public static final String VARY = "Vary";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.5">Section 3.1.1.5</a>.
	 */
	public static final String CONTENT_TYPE = "Content-Type";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.2">Section 3.1.3.2</a>.
	 */
	public static final String CONTENT_LANGUAGE = "Content-Language";

	private ContentNegotiation() {
	}

}
