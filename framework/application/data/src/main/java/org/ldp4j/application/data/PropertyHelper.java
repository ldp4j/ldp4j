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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.io.Serializable;
import java.net.URI;

import org.ldp4j.application.vocabulary.Term;

public interface PropertyHelper {

	<T> T firstValue(Class<? extends T> aClazz);

	IndividualHelper firstIndividual();

	<T extends Serializable, S extends Individual<T, S>> T firstIndividual(Class<? extends S> clazz);

	/**
	 * Add a literal value
	 * @param rawValue The literal value to be added
	 * @return A helper for the current individual and property
	 */
	IndividualPropertyHelper withLiteral(Object rawValue);

	/**
	 * Add a local individual value
	 * @param id The name of the individual to add
	 * @return A helper for the current individual and property
	 */
	IndividualPropertyHelper withIndividual(Name<?> id);

	/**
	 * Add a managed individual value
	 * @param id The name of the individual to add
	 * @param managerId The identifier of the handler that manages the resource
	 * @return A helper for the current individual and property
	 */
	IndividualPropertyHelper withIndividual(Name<?> id, String managerId);

	/**
	 * Add a relative individual value
	 * @param id The name of the individual to parent individual
	 * @param managerId The identifier of the handler that manages the parent resource
	 * @param path The relative path for the individual to be added
	 * @return A helper for the current individual and property
	 */
	IndividualPropertyHelper withIndividual(Name<?> id, String managerId, URI path);

	/**
	 * Add a relative individual value
	 * @param id The name of the individual to parent individual
	 * @param managerId The identifier of the handler that manages the parent resource
	 * @param path The relative path for the individual to be added
	 * @return A helper for the current individual and property
	 */
	IndividualPropertyHelper withIndividual(Name<?> id, String managerId, String path);

	/**
	 * Add a external individual value
	 * @param id The identifier of the individual to add
	 * @return A helper for the current individual and property
	 */
	IndividualPropertyHelper withIndividual(URI id);

	/**
	 * Add a external individual value
	 * @param id The identifier of the individual to add
	 * @return A helper for the current individual and property
	 */
	IndividualPropertyHelper withIndividual(String id);

	/**
	 * Add a external individual value
	 * @param id The identifier of the individual to add
	 * @return A helper for the current individual and property
	 */
	IndividualPropertyHelper withIndividual(Term id);

}