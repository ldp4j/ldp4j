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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

import org.ldp4j.rdf.bean.InvalidDefinitionException;
import org.ldp4j.rdf.bean.Range;

final class RangeExtractor {
	
	private static class RangeValidator {

		private final TypeManager typeManager;

		private RangeValidator(TypeManager typeManager) {
			this.typeManager = typeManager;
		}
		
		private boolean isValid(Class<?> clazz) {
			boolean valid=TypeSupport.isLiteral(clazz);
			if(!valid) {
				valid=typeManager.isType(clazz);
			}
			return valid;
		}
		
	}

	private final RangeValidator validator;

	private RangeDefinition range;

	RangeExtractor(TypeManager typeManager) {
		this.validator=new RangeValidator(typeManager);
	}

	private boolean isValidClass(java.lang.reflect.Type type) {
		boolean result=false;
		if(type instanceof Class<?>) {
			Class<?> clazz = (Class<?>)type;
			result=validator.isValid(clazz);
			if(result) {
				range=new RangeDefinition();
				range.setType(clazz);
			}
		} else if(type instanceof ParameterizedType) {
			ParameterizedType pt=(ParameterizedType)type;
			result=isValidClass(pt.getRawType());
		} else if(type instanceof TypeVariable<?>) {
			TypeVariable<?> typeVariable = (TypeVariable<?>)type;
			java.lang.reflect.Type[] bounds = typeVariable.getBounds();
			for(int i=0;i<bounds.length && !result;i++) {
				result=isValidClass(typeVariable.getBounds()[i]);
			}
		}
		if(result) {
			range.setGenericType(type);
		}
		return result;
	}
	
	private boolean isValidAggregation(java.lang.reflect.Type type) {
		boolean result=false;
		if(type instanceof ParameterizedType) {
			ParameterizedType pt=(ParameterizedType)type;
			result=TypeSupport.isAggregation((Class<?>)pt.getRawType());
			if(result) {
				java.lang.reflect.Type[] actualTypeArguments = pt.getActualTypeArguments();
				result=isValidClass(actualTypeArguments[0]);
			}
		}
		return result;
	}

	private boolean isValid(java.lang.reflect.Type type) {
		return isValidAggregation(type) || isValidClass(type);
	}

	Range getRange(java.lang.reflect.Type type) {
		if(!isValid(type)) {
			throw new InvalidDefinitionException("Unsupported range type '"+type+"'");
		}
		return range;
	}

}