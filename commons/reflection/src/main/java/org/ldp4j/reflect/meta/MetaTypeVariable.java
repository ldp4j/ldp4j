/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-reflection:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-reflection-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.reflect.meta;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * An interface for facilitating the introspection of {@code TypeVariables}
 * entities.
 *
 * @author Miguel Esteban Guti&eacute;rrez
 *
 * @param <D>
 *            the type of generic declaration that declared the introspected
 *            type variable.
 * @param <T>
 *            The {@code ParameterizedDeclaration} where the introspected
 *            {@code TypeVariable} was declared.
 * @see java.lang.reflect.TypeVariable
 */
public interface MetaTypeVariable<D extends GenericDeclaration> extends Meta<TypeVariable<D>> {

	/**
	 * Get the declarations of the bounds of the introspected type variable.
	 *
	 * @return An immutable list with the declarations of the type variable
	 *         bounds.
	 * @see java.lang.reflect.TypeVariable#getBounds()
	 */
	List<MetaClass<?>> getBounds();

	/**
	 * Get the declaration where the introspected type variable was declared.
	 *
	 * @return The parameterized declaration where the type variable was
	 *         declared.
	 * @see java.lang.reflect.TypeVariable#getGenericDeclaration();
	 */
	MetaGenericDeclaration<D> getDeclaration();

	/**
	 * Get the name of the introspected type variable
	 *
	 * @return The name of the type variable.
	 * @see java.lang.reflect.TypeVariable#getName()
	 */
	String getName();

	/**
	 * Check whether of not the introspected type variable is defined.
	 *
	 * @return True if the declaration where the type variable was defined also
	 *         defines an actual argument for the variable
	 */
	boolean isDefined();

	/**
	 * Get the declaration of the actual argument defined for the introspected
	 * type variable.
	 *
	 * @return The declaration of the actual argument for the type variable if
	 *         defined, or {@code null} otherwise.
	 */
	MetaClass<?> actualArgument();

}