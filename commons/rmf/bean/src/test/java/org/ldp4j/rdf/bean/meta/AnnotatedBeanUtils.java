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

import java.util.Iterator;
import java.util.List;

import org.ldp4j.rdf.bean.util.BeanUtils;

public final class AnnotatedBeanUtils {
	
	private AnnotatedBeanUtils() {
	}
	
	public static boolean isGetter(AnnotatedMethod method) {
		return BeanUtils.isGetter(method.get());
	}
	
	public static AnnotatedMethod getSetter(AnnotatedMethod getter) {
		return 
			getSetter(
				getter.getDeclaringClass(), 
				getPropertyName(getter), 
				getter.getReturnType());
	}

	private static AnnotatedMethod getSetter(AnnotatedClass<?> clazz, String propertyName, Class<?> propertyType) {
		String setterName = "set"+BeanUtils.capitalize(propertyName);
		Iterator<AnnotatedMethod> iterator = clazz.getDeclaredMethods().iterator();
		while(iterator.hasNext()) {
			AnnotatedMethod candidate=iterator.next();
			List<Class<?>> parameters=candidate.getRawParameterTypes();
			if(candidate.isPublic() && 
				candidate.getReturnType()==Void.TYPE && 
				candidate.getName().equals(setterName) && 
				parameters.size()==1 && 
				propertyType==parameters.get(0)) {
				return candidate;
			}
		}
		return null;
	}

	public static AnnotatedMethod findSetter(AnnotatedClass<?> clazz, String propertyName, Class<?> propertyType) {
		AnnotatedClass<?> ctx=clazz;
		AnnotatedMethod result=null;
		while(ctx!=null && result==null) {
			result=getSetter(ctx,propertyName,propertyType);
			ctx=ctx.getSuperclass();
		}
		return result;
	}

	public static String getPropertyName(AnnotatedMethod method) {
		return BeanUtils.getPropertyName(method.get());
	}

}