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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:1.0.0-SNAPSHOT
 *   Bundle      : rmf-bean-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class BeanUtils {
	
	private BeanUtils() {
	}
	
	public static boolean isGetter(Method method) {
		String name = method.getName();
		return 
			(Modifier.isPublic(method.getModifiers())) &&
			(method.getParameterTypes().length==0) && 
			(
				(name.startsWith("get") && name.length()>3) || 
				(name.startsWith("is") && name.length()>2 && method.getReturnType()==Boolean.TYPE)
			);
	}
	
	public static Method getSetter(Method getter) {
		return getSetter(getter.getDeclaringClass(), getPropertyName(getter), getter.getReturnType());
	}

	public static Method findSetter(Class<?> clazz, String propertyName, Class<?> propertyType) {
		Class<?> ctx=clazz;
		Method result=null;
		while(ctx!=null && result==null) {
			result=getSetter(ctx,propertyName,propertyType);
			ctx=ctx.getSuperclass();
			if(ctx==Object.class) {
				ctx=null;
			}
		}
		return result;
	}

	private static Method getSetter(Class<?> clazz, String propertyName, Class<?> propertyType) {
		String setterName = "set"+capitalize(propertyName);
		Method candidate;
		try {
			candidate = clazz.getDeclaredMethod(setterName, propertyType);
			if(candidate.getReturnType()!=Void.TYPE || !Modifier.isPublic(candidate.getModifiers())) {
				candidate=null;
			}
		} catch (SecurityException e) {
			throw new IllegalStateException(e);
		} catch (NoSuchMethodException e) {
			candidate=null;
		}
		return candidate;
	}

	public static String getPropertyName(Method method) {
		String name=method.getName();
		if(name.startsWith("get")) {
			name=decapitalize(name.substring(3));
		} else if(name.startsWith("is")) {
			Class<?> resultType=method.getReturnType();
			if(resultType == Boolean.TYPE) {
				name=decapitalize(name.substring(2));
			}
		}
		return name;
	}

	public static String decapitalize(String name) {
		if(name==null||name.length()==0) {
			return name;
		}
		if(name.length()>1&&Character.isUpperCase(name.charAt(1))&&Character.isUpperCase(name.charAt(0))) {
			return name;
		}
		char[] chars=name.toCharArray();
		chars[0]=Character.toLowerCase(chars[0]);
		return new String(chars);
	}
	
	public static String capitalize(String name) {
		if(name==null||name.length()==0) {
			return name;
		}
		if(name.length()>1&&Character.isUpperCase(name.charAt(1))&&Character.isUpperCase(name.charAt(0))) {
			return name;
		}
		char[] chars=name.toCharArray();
		chars[0]=Character.toUpperCase(chars[0]);
		return new String(chars);
	}
}