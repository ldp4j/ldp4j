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
package org.ldp4j.rdf.bean.meta.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.ldp4j.rdf.bean.meta.MetaAnnotation;

class MetaAnnotationHelper {

	static boolean isMetaAnnotation(Class<? extends Annotation> clazz) {
		for(Annotation annotation:clazz.getDeclaredAnnotations()) {
			if(isMetaAnnotationAnnotation(annotation.annotationType())) {
				return true;
			}
		}
		return false;
	}

	private static boolean isMetaAnnotationAnnotation(Class<? extends Annotation> type) {
		if(MetaAnnotation.class.equals(type)) {
			return true;
		}

		for(Annotation annotation:type.getAnnotations()) {
			if(Meta.class.equals(annotation.annotationType())) {
				return true;
			}
		}

		return isSelfAnnotatedMetaAnnotation(type);
	}

	private static boolean isSelfAnnotatedMetaAnnotation(Class<? extends Annotation> type) {
		if(isSelfAnnotated(type, "MetaAnnotation")) {
			return true;
		}

		for(Annotation annotation:type.getAnnotations()) {
			if(isSelfAnnotated(annotation.annotationType(), "Meta")) {
				return true;
			}
		}

		return false;
	}

	private static boolean isSelfAnnotated(Class<? extends Annotation> type, String name) {
		return 
			type.isAnnotationPresent(type) && 
			type.getSimpleName().equals(name) && 
			validTarget(type);
	}

	private static boolean validTarget(Class<? extends Annotation> type) {
		final Target target = type.getAnnotation(Target.class);

		if(target==null) {
			return false;
		}

		final ElementType[] targets=target.value();

		return 
			targets.length == 1 && 
			targets[0] == ElementType.ANNOTATION_TYPE;
	}
	
}