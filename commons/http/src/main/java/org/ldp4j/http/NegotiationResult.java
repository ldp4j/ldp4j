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

import com.google.common.collect.Multimap;

/**
 * This interface represents the results of the content negotiation process.
 */
public interface NegotiationResult {


	/**
	 * Check whether or not the negotiation was positive.
	 *
	 * @return {@code true} if it is acceptable, {@code false} otherwise.
	 */
	boolean isAcceptable();

	/**
	 * Get the quality of the acceptable variant.
	 *
	 * @return the resultant quality or {@code null} if not acceptable.
	 */
	Quality quality();


	/**
	 * Get the acceptable variant.
	 *
	 * @return the acceptable variant or {@code null} if not acceptable
	 */
	Variant variant();

	/**
	 * Get the alternatives available.
	 *
	 * @return the available alternatives.
	 */
	Alternatives alternatives();

	/**
	 * Get the headers that should be included in the response.
	 *
	 * @param accepted
	 *            whether to return the headers for an accepted response or a
	 *            not acceptable response.
	 * @return the headers for an accepted response if accepted is {@code true}
	 *         or the headers for a not acceptable response otherwise.
	 */
	Multimap<String,String> responseHeaders(boolean accepted);

}
