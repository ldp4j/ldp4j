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

import java.util.Locale;

/**
 * <h1>NOTE:</h1>
 * <p>
 * Beware, <b>{@code Accept-Language}</b> and <b>{@code Content-Language} </b>
 * headers have different requirements. Whereas the former only allows <i>
 * {@code language-ranges}</i>, the latter accepts full-fledged <i>
 * {@code language-tags}</i>.
 * <p>
 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>, Sections <a
 * href="https://tools.ietf.org/html/rfc7231#section-3.1.3.1">3.1.3.1</a> and <a
 * href="https://tools.ietf.org/html/rfc7231#section-5.3.5">5.3.5</a>.
 */
public interface Language extends Negotiable {

	/**
	 * Get the language's primary tag.
	 *
	 * @return the primary tag.
	 */
	String primaryTag();

	/**
	 * Get the language's sub tag.
	 *
	 * @return the sub tag.
	 */
	String subTag();

	/**
	 * Get the language's locale.
	 *
	 * @return the locale.
	 */
	Locale locale();

}
