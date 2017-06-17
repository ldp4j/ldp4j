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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ParameterMap implements Parameterized {

	private final List<TypeVariable<?>> parameters;
	private final List<String> parameterNames;
	private final Map<String,Integer> parameterIndex;
	private final Map<TypeVariable<?>,Type> resolutions;

	private ParameterMap(List<TypeVariable<?>> parameters, Map<TypeVariable<?>,Type> resolutions) {
		this.parameters=parameters;
		this.parameterNames=new ArrayList<String>();
		this.parameterIndex=new HashMap<String,Integer>();
		this.resolutions=resolutions;
		for(int i=0;i<parameters.size();i++) {
			TypeVariable<?> parameter=this.parameters.get(i);
			this.parameterNames.add(parameter.getName());
			this.parameterIndex.put(parameter.getName(),i);
		}
	}
	
	public ParameterMap(TypeVariable<?>[] parameters) {
		this(Arrays.asList(parameters),emptyResolution(parameters));
	}
	
	public ParameterMap(TypeVariable<?>[] parameters, Type[] actualTypeArguments) {
		this(Arrays.asList(parameters),simpleResolution(parameters,actualTypeArguments));
	}

	public ParameterMap(TypeVariable<?>[] parameters, Type[] actualTypeArguments, Parameterized ctx) {
		this(Arrays.asList(parameters),annealedResolution(parameters,actualTypeArguments,ctx));
	}

	
	private static Map<TypeVariable<?>, Type> emptyResolution(TypeVariable<?>[] parameters) {
		Map<TypeVariable<?>, Type> result=new HashMap<TypeVariable<?>, Type>();
		for(int i=0;i<parameters.length;i++) {
			result.put(parameters[i], parameters[i]);
		}
		return result;
	}

	private static Map<TypeVariable<?>, Type> simpleResolution(TypeVariable<?>[] parameters,Type[] actualTypeArguments) {
		Map<TypeVariable<?>, Type> result=new HashMap<TypeVariable<?>, Type>();
		for(int i=0;i<parameters.length;i++) {
			result.put(parameters[i], actualTypeArguments[i]);
		}
		return result;
	}

	private static Map<TypeVariable<?>, Type> annealedResolution(TypeVariable<?>[] parameters,Type[] actualTypeArguments, Parameterized resolution) {
		Map<TypeVariable<?>, Type> result=new HashMap<TypeVariable<?>, Type>();
		for(int i=0;i<parameters.length;i++) {
			TypeVariable<?> parameter = parameters[i];
			Type argument = actualTypeArguments[i];
			if(argument instanceof TypeVariable<?>) {
				TypeVariable<?> annealedParameter = (TypeVariable<?>)argument;
				Type annealedArgument=resolution.getParameterArgument(annealedParameter.getName());
				if(annealedArgument!=null) {
					argument=annealedArgument;
				}
			}
			result.put(parameter, argument);
		}
		return result;
	}

	@Override
	public List<String> parameterNames() {
		return Collections.unmodifiableList(parameterNames);
	}

	@Override
	public TypeVariable<?> getParameter(String name) {
		if(!parameterNames.contains(name)) {
			throw new IllegalArgumentException("Unknown type parameter '"+name+"'");
		}
		return parameters.get(parameterIndex.get(name));
	}

	@Override
	public Type getParameterArgument(String name) {
		return resolutions.get(getParameter(name));
	}
	
	@Override
	public Class<?> getActualParameterArgument(String name) {
		return resolveType(resolutions.get(getParameter(name)));
	}

	private Class<?> resolveType(Type argument) {
		Class<?> result = null;
		if(argument instanceof TypeVariable<?>) {
			TypeVariable<?> var=(TypeVariable<?>)argument;
			Type[] bounds = var.getBounds();
			if(bounds.length>0 && bounds[0] instanceof Class<?>) {
				result=(Class<?>)bounds[0];
			}
		}
		return result;
	}

}