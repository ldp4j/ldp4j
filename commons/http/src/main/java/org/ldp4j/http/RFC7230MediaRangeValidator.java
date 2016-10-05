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


final class RFC7230MediaRangeValidator implements MediaRangeValidator {

	private static final long serialVersionUID = 8974499521430853690L;

	RFC7230MediaRangeValidator() {
	}

	/**
	 * Checks the given type name for illegal characters, as defined in RFC
	 * 7230, section 3.2.6.
	 *
	 * @param type
	 *            the string to validate
	 * @throws IllegalArgumentException
	 *             in case of illegal characters
	 * @see <a href="http://tools.ietf.org/html/rfc7230#section-3.2.6">Hypertext
	 *      Transfer Protocol (HTTP/1.1): Message Syntax and Routing, Section
	 *      3.2.6</a>
	 */
	@Override
	public void checkType(final String type) {
		HttpUtils.checkToken(type,"Type '%s' is not a valid token",type);
	}

	/**
	 * Checks the given subtype name for illegal characters, as defined in RFC
	 * 7230, section 3.2.6.
	 *
	 * @param subType
	 *            the string to validate
	 * @throws IllegalArgumentException
	 *             in case of illegal characters
	 * @see <a href="http://tools.ietf.org/html/rfc7230#section-3.2.6">Hypertext
	 *      Transfer Protocol (HTTP/1.1): Message Syntax and Routing, Section
	 *      3.2.6</a>
	 */
	@Override
	public void checkSubType(final String subType) {
		HttpUtils.checkToken(subType,"Subtype '%s' is not a valid token",subType);
	}

	/**
	 * Checks the given suffix for illegal characters, as defined in RFC
	 * 7230, section 3.2.6.
	 *
	 * @param suffix
	 *            the string to validate
	 * @throws IllegalArgumentException
	 *             in case of illegal characters
	 * @see <a href="http://tools.ietf.org/html/rfc7230#section-3.2.6">Hypertext
	 *      Transfer Protocol (HTTP/1.1): Message Syntax and Routing, Section
	 *      3.2.6</a>
	 */
	@Override
	public void checkSuffix(String suffix) {
		HttpUtils.checkToken(suffix,"Suffix '%s' is not a valid token",suffix);
	}

}
