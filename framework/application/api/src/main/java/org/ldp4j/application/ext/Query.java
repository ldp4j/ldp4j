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

import java.io.Serializable;
import java.util.Set;

/**
 * A collection of {@code Parameter}s, where is each parameter is indexed by its
 * name.
 */
public interface Query extends Serializable {

	/**
	 * Returns {@code true} if this query contains no parameters.
	 *
	 * @return {@code true} if this query contains no parameters
	 */
	boolean isEmpty();

	/**
	 * Returns the number of parameters in this query. If the query contains
	 * more than {@code Integer.MAX_VALUE} parameters, returns
	 * {@code Integer.MAX_VALUE}.
	 *
	 * @return the number of parameters in this query
	 */
	int size();

	/**
	 * Returns {@code true} if this query contains a parameter with the
	 * specified name.
	 *
	 * @param paramName
	 *            the name of the parameter
	 * @return {@code true} if this query contains a parameter with the
	 *         specified name.
	 */
	boolean hasParameter(String paramName);

	/**
	 * Returns the parameter with the specified name, or {@code null} if this
	 * query contains no parameter with that name.
	 *
	 * @param paramName
	 *            the name of the parameter that is to be returned
	 * @return the parameter with the specified name, or {@code null} if this
	 *         query does not contain a parameter with the specified name
	 * @throws NullPointerException
	 *             if the specified key is null
	 */
	Parameter getParameter(String paramName);

	/**
	 * Return the names of the parameters in this query. If no parameters are
	 * available and empty set will be returned.
	 *
	 * @return the names of the parameters in this query, or an empty set if the
	 *         query is empty
	 */
	Set<String> parameterNames();

}
