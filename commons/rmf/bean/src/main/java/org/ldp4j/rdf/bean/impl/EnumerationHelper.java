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
package org.ldp4j.rdf.bean.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ldp4j.rdf.bean.InvalidDefinitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EnumerationHelper<S> {

	private static final String NAMES_METHOD_VIOLATION = "Public static method names() should return Set<String>, not ";

	private static final Logger LOGGER=LoggerFactory.getLogger(EnumerationHelper.class);

	private final Class<? extends S> clazz;
	private final Set<String> names;
	private final Method name;
	private final Method valueOf;
	private final Set<S> values;

	private EnumerationHelper(Set<String> names, Class<? extends S> clazz, Method name, Method valueOf) {
		this.names = Collections.unmodifiableSet(new HashSet<String>(names));
		this.clazz = clazz;
		this.name = name;
		this.valueOf = valueOf;
		this.values=getValues(names);
	}

	private Set<S> getValues(Set<String> names) {
		Set<S> tmp=new HashSet<S>();
		for(String aName:names) {
			tmp.add(valueOf(aName));
		}
		return Collections.unmodifiableSet(tmp);
	}

	Class<? extends S> getEnumeratedClass() {
		return clazz;
	}

	Set<String> names() {
		return names;
	}

	String name(S object) {
		try {
			return (String)name.invoke(object);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	Set<S> values() {
		return values;
	}

	S valueOf(String name) {
		if(!names.contains(name)) {
			throw new IllegalArgumentException("Unknown name '"+name+"'");
		}
		try {
			return getEnumeratedClass().cast(valueOf.invoke(null,name));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	boolean isEnumeration(Class<?> clazz) {
		List<String> violations=new ArrayList<String>();
		getNames(clazz,violations);
		getNameMethod(clazz,violations);
		getFromURIMethod(clazz,violations);
		return violations.isEmpty();
	}

	static <S> EnumerationHelper<S> newInstance(Class<? extends S> clazz) {
		List<String> violations=new ArrayList<String>();
		Set<String> names = getNames(clazz,violations);
		Method toName=getNameMethod(clazz,violations);
		Method fromName=getFromURIMethod(clazz,violations);
		if(!violations.isEmpty()) {
			throw new InvalidDefinitionException("Violations found: "+violations);
		}
		return new EnumerationHelper<S>(names, clazz,toName,fromName);
	}

	private static Set<String> getNames(Class<?> clazz, List<String> violations) {
		Set<String> result = Collections.emptySet();
		try {
			Method method = clazz.getMethod("names");
			int modifiers = method.getModifiers();
			if(!(Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))) {
				violations.add("Method Set<String> names() should be public and static");
			} else {
				Type returnType = method.getGenericReturnType();
				if(!(returnType instanceof ParameterizedType)) {
					violations.add(NAMES_METHOD_VIOLATION+returnType);
				} else {
					Set<String> tmp=getNames(violations, method, (ParameterizedType)returnType);
					result=
						Collections.
							unmodifiableSet(
								new HashSet<String>(tmp));
				}
			}
		} catch (NoSuchMethodException e) {
			violations.add("Method public static Set<String> names() is not defined");
			LOGGER.trace("Method public Set<String> names() is not defined for class {}",clazz.getName(),e);
		}
		return result;
	}

	private static Set<String> getNames(List<String> violations, Method method, ParameterizedType returnType) {
		if(!Set.class.isAssignableFrom((Class<?>)returnType.getRawType()) || returnType.getActualTypeArguments()[0]!=String.class) {
			violations.add(NAMES_METHOD_VIOLATION+returnType);
			return Collections.<String>emptySet();
		}
		return invokeMethod(violations, method);
	}

	@SuppressWarnings("unchecked")
	private static Set<String> invokeMethod(List<String> violations, Method method) {
		Set<String> result=Collections.<String>emptySet();
		try {
			result=(Set<String>)method.invoke(null);
		} catch (Exception e) {
			violations.add("Invocation of method public static Set<String> names() failed ("+e.getMessage()+")");
			LOGGER.trace("Invocation of method public Set<String> names() of class {} failed. Full stacktrace follows",method.getDeclaringClass().getName(),e);
		}
		return result;
	}

	private static Method getNameMethod(Class<?> clazz, List<String> violations) {
		Method method=null;
		try {
			method=clazz.getMethod("name");
			int modifiers = method.getModifiers();
			if(!(!Modifier.isStatic(modifiers) && !Modifier.isAbstract(modifiers) && Modifier.isPublic(modifiers))) {
				violations.add("Method String name() must be public and cannot be abstract or static");
			}
			Class<?> returnType = method.getReturnType();
			if(returnType != String.class) {
				violations.add("Public method name() should return String, not "+returnType);
			}
			return method;
		} catch (NoSuchMethodException e) {
			violations.add("Method public String name() is not defined");
			LOGGER.trace("Method public String name() is not defined for class {}",clazz.getName(),e);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return method;
	}

	private static Method getFromURIMethod(Class<?> clazz, List<String> violations) {
		Method method=null;
		try {
			method=clazz.getMethod("valueOf",String.class);
			int modifiers = method.getModifiers();
			if(!(Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))) {
				violations.add("Method "+clazz.getCanonicalName()+" valueOf(String) should be public and static");
			}
			Class<?> returnType = method.getReturnType();
			if(returnType != clazz) {
				violations.add("Public static method valueOf(String) should return "+clazz.getName()+", not "+returnType);
			}
		} catch (NoSuchMethodException e) {
			violations.add("Method public static "+clazz.getCanonicalName()+" valueOf(String) is not defined");
			LOGGER.trace("Method public static {} valueOf(String) is not defined for class {}",clazz.getName(),clazz.getName(),e);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return method;
	}
}