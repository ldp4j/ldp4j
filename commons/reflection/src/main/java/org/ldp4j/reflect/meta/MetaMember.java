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

import java.lang.reflect.Member;

import org.ldp4j.reflect.model.Modifiers;

/**
 * An interface for facilitating the introspection of members.
 *
 * @author Miguel Esteban Guti&eacute;rrez
 *
 * @param <T>
 *            The introspected {@code Member} type.
 * @see java.lang.reflect.Member
 */
public interface MetaMember<C, T extends Member> extends Meta<T> {

	/**
	 * @see java.lang.reflect.Member#getDeclaringClass()
	 */
	MetaClass<C> getDeclaringClass();

	/**
	 * Retrieve the modifiers of the member.
	 *
	 * @see java.lang.reflect.Member#getModifiers()
	 */
	Modifiers getModifiers();

	/**
	 * @see java.lang.reflect.Member#getName()
	 */
	String getName();

	/**
	 * @see java.lang.reflect.Member#isSynthetic()
	 */
	boolean isSynthetic();

}