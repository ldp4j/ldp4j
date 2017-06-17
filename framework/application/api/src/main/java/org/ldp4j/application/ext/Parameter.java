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
import java.util.List;

/**
 * A collection of values defined for a query parameter. The values are defined
 * as strings, which can be transformed to other Java types.
 */
public interface Parameter extends Serializable {

	/**
	 * Returns the name of the parameter.
	 *
	 * @return the name of the parameter
	 */
	String name();

	/**
	 * Returns {@code true} if this parameter contains multiple values.
	 *
	 * @return {@code true} if this parameter contains multiple values
	 */
	boolean isMultivalued();

	/**
	 * Returns the number of values in this parameter. If the parameter contains
	 * more than {@code Integer.MAX_VALUE} value, returns
	 * {@code Integer.MAX_VALUE}.
	 *
	 * @return the number of values in this parameter
	 */
	int cardinality();

	/**
	 * Returns the first raw value of this parameter.
	 *
	 * @return the first raw value of this parameter
	 */
	String rawValue();

	/**
	 * Returns all the raw values of this parameter. If the parameter is not
	 * multivalued, the result will just contain that parameter's single value.
	 *
	 * @return the raw values of this parameter
	 */
	List<String> rawValues();

	/**
	 * Returns the first raw value of this parameter as an instance of a given
	 * type.
	 *
	 * @param <T> the type of object to be returned
	 * @param clazz
	 *            the {@code Class} for the type T to which the raw value will
	 *            be transformed to.
	 * @return the instance of the specified {@code Class} to which the first
	 *         raw value of this parameter is transformed to.
	 * @throws ObjectTransformationException
	 *             if the raw value cannot be transformed to the specified type
	 *             T.
	 */
	<T> T rawValueAs(Class<? extends T> clazz);

	/**
	 * Returns the raw values of this parameter as instances of a given type.
	 *
	 * @param <T> the type of object to be returned
	 * @param clazz
	 *            the {@code Class} for the type T to which the raw values will
	 *            be transformed to.
	 * @return the instances of the specified {@code Class} to which the raw
	 *         values of this parameter are transformed to.
	 * @throws ObjectTransformationException
	 *             if any of the raw values cannot be transformed to the
	 *             specified type T.
	 */
	<T> List<T> rawValuesAs(Class<? extends T> clazz);

}
