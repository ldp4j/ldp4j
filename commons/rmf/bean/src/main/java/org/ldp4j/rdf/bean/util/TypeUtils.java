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
package org.ldp4j.rdf.bean.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

public final class TypeUtils {

	private final List<Type> visited;

	private TypeUtils() {
		visited=new ArrayList<Type>();
	}

	private String toString(Type type, boolean qualify) {
		visited.add(type);
		String result=null;
		if (type instanceof TypeVariable) {
			result=printTypeVariable((TypeVariable<?>) type,qualify);
		} else if (type instanceof ParameterizedType) {
			result=printParameterizedType((ParameterizedType) type,qualify);
		} else if (type instanceof Class) {
			result=printClass((Class<?>) type,qualify);
		} else if (type instanceof GenericArrayType) {
			result=printGenericArrayDeclaration((GenericArrayType) type,qualify);
		} else if (type instanceof WildcardType) {
			result=printWildcardType((WildcardType) type);
		} else {
			throw new IllegalStateException("Unknown type '"+type+"'");
		}
		return result;
	}

	public static String toString(Type type) {
		return new TypeUtils().toString(type, false);
	}

	private String printClass(Class<?> clazz, boolean qualify) {
		String result=null;
		if(qualify) {
			result=(clazz.isInterface()?"interface ":(clazz.isPrimitive()?"":"class "))+clazz.getCanonicalName();
		} else {
			result=clazz.getCanonicalName();
		}
		return result;
	}

	private String printGenericArrayDeclaration(GenericArrayType type, boolean qualify) {
		String componentType = toString(type.getGenericComponentType(),qualify);
		if(componentType.indexOf(' ')>0) { // NOSONAR
			return String.format("(%s)[]",componentType);
		} else {
			return componentType.concat("[]");
		}
	}

	private String printParameterizedType(ParameterizedType type, boolean qualify) {
		StringBuilder builder = new StringBuilder();
		builder.append(toString(type.getRawType(),qualify));
		if(type.getActualTypeArguments().length>0) {
			builder.append("<");
			boolean first=true;
			for(Type s:type.getActualTypeArguments()) {
				if(first) {
					first=false;
				} else {
					builder.append(", ");
				}
				if(visited.contains(s)) {
					builder.append(s);
				} else {
					builder.append(toString(s,false));
				}
			}
			builder.append(">");
		}
		return builder.toString();
	}

	private String printTypeVariable(TypeVariable<?> variable, boolean qualify) {
		StringBuilder builder=new StringBuilder();
		if(qualify) {
			builder.append("<");
			builder.append(variable.getGenericDeclaration());
			builder.append("> ");
		}
		builder.append(printBounds(variable.getName(),variable.getBounds(), false));
		return builder.toString();
	}

	private String printWildcardType(WildcardType type) {
		Type[] bounds=type.getLowerBounds();
		boolean hasLowerBounds = bounds.length>0;
		if(!hasLowerBounds) {
			bounds=type.getUpperBounds();
		}
		return printBounds("?",bounds,hasLowerBounds);
	}

	private String printBounds(String name, Type[] bounds, boolean lower) {
		StringBuilder builder=new StringBuilder();
		builder.append(name);
		if(bounds.length>0) {
			String prefix = lower?"super":"extends";
			builder.append(" ").append(prefix).append(" ");
			for(int i=0;i<bounds.length;i++) {
				if(i>0) {
					builder.append(" & ");
				}
				Type bound = bounds[i];
				builder.append(toString(bound,false));
			}
		}
		return builder.toString();
	}

}