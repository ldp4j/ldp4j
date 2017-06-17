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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.meta;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.ldp4j.rdf.bean.util.TypeUtils;


public abstract class AnnotatedMember<T extends Member> implements Annotated<T> {

	private final T member;
	private final AnnotatedClass<?> declaringClass;
	
	protected AnnotatedMember(AnnotatedClass<?> declaringClass, T member) {
		this.declaringClass = declaringClass;
		this.member = member;
	}

	@Override
	public T get() {
		return member;
	}

	public final AnnotatedClass<?> getDeclaringClass() {
		return declaringClass;
	}

	public final String getName() {
		return member.getName();
	}

	public final boolean isProtected() {
		return Modifier.isProtected(member.getModifiers());
	}

	public final boolean isPublic() {
		return Modifier.isPublic(member.getModifiers());
	}

	public final boolean isPrivate() {
		return Modifier.isPrivate(member.getModifiers());
	}

	public final boolean isPackagePrivate() {
		return !isPublic() && !isPrivate() && !isProtected();
	}

	public final boolean isFinal() {
		return Modifier.isFinal(member.getModifiers());
	}

	public final boolean isAbstract() {
		return Modifier.isAbstract(member.getModifiers());
	}

	public final boolean isStatic() {
		return Modifier.isStatic(member.getModifiers());
	}

	public final boolean isSynthetic() {
		return member.isSynthetic();
	}

	@SuppressWarnings("unchecked")
	protected final  <S extends Parameterized> Class<?> resolveType(S context, Type type, Class<?> defaultRawType) {
		Class<?> result=defaultRawType;
		if(type instanceof TypeVariable<?>) {
			TypeVariable<? extends GenericDeclaration> variable=(TypeVariable<? extends GenericDeclaration>)type;
			if(variable.getGenericDeclaration() instanceof Type) {
				Class<?> argument=
					getDeclaringClass().
						getActualParameterArgument(
							variable.getName());
				if(argument!=null) {
					result=argument;
				}
			} else {
				Type[] bounds = variable.getBounds();
				if(bounds.length>0) {
					Type bound = bounds[0];
					if(bound instanceof Class<?>) {
						result=(Class<?>)bound;
					} else if(bound instanceof ParameterizedType) {
						result=(Class<?>)((ParameterizedType)bound).getRawType();
					} else {
						throw new IllegalStateException(String.format("Unexpected bound type '%s' (%s)", bound.getClass().getCanonicalName(),TypeUtils.toString(bound)));
					}
				} 
			}
		}
		return result;
	}

}