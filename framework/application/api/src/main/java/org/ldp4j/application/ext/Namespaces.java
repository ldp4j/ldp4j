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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.ext;

import java.util.List;
import java.util.Set;

/**
 * Interface for read only Namespace processing. <br>
 *
 * A Namespace has the properties: <br>
 *
 * <ul>
 * <li><b>Namespace URI</b>: Namespace name expressed as a URI to which the
 * prefix is bound.</li>
 * <li><b>Prefix</b></li>
 * </ul>
 *
 *<br>
 *
 * Note that a Namespace URI can be bound to multiple prefixes. However a prefix
 * can only be bound to a single Namespace URI.
 */
public interface Namespaces {

	/**
	 * Get the declared prefixes.
	 *
	 * @return the collection of defined prefixes.
	 */
	Set<String> getDeclaredPrefixes();

	/**
	 * Get Namespace URI bound to a prefix.
	 *
	 * @param prefix
	 *            prefix to look up
	 * @return Namespace URI bound to prefix
	 */
	String getNamespaceURI(String prefix);

	/**
	 * Get prefix bound to Namespace URI.
	 *
	 * @param namespaceURI
	 *            URI of the Namespace to look up
	 * @return Prefix bound to the Namespace URI
	 */
	String getPrefix(String namespaceURI);

	/**
	 * Get prefixes bound to Namespace URI.
	 *
	 * @param namespaceURI
	 *            URI of the Namespace to look up
	 * @return All the prefixes bound to the Namespace URI
	 */
	List<String> getPrefixes(String namespaceURI);

}