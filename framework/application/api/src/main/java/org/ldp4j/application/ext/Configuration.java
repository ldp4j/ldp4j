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

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Provides a programmatic mechanism for an LDP4j application to specify the
 * configuration parameters for the <b>LDP4j Application Engine</b>. <br>
 *
 * This class is meant to be extended (refined) to also include specific
 * configuration requirements of LDP4j applications.
 */
public class Configuration {

	private static final class NullNamespaces implements Namespaces {
		@Override
		public List<String> getPrefixes(String namespaceURI) {
			return Collections.emptyList();
		}

		@Override
		public String getPrefix(String namespaceURI) {
			return null;
		}

		@Override
		public String getNamespaceURI(String prefix) {
			return null;
		}

		@Override
		public Set<String> getDeclaredPrefixes() {
			return Collections.emptySet();
		}
	}

	/**
	 * Get the namespace declarations of the application.
	 *
	 * @return the namespaces of the application
	 */
	public Namespaces namespaces() {
		return new NullNamespaces();
	}

}
