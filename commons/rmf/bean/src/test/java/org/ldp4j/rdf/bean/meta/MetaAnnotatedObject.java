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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

public abstract class MetaAnnotatedObject<T> implements MetaAnnotated<T> {

	private final MetaAnnotationSupport<T> metaAnnotations;
	
	protected MetaAnnotatedObject(T metaAnnotated, List<? extends Annotated<?>> elements) {
		this.metaAnnotations = MetaAnnotationSupport.forAnnotatedObjects(metaAnnotated,elements);
	}
	
	protected MetaAnnotatedObject(MetaAnnotatedObject<T> provider) {
		this.metaAnnotations=provider.metaAnnotations;
	}

	@Override
	public final Collection<MetaAnnotation<?>> getMetaAnnotations() {
		return metaAnnotations.getMetaAnnotations();
	}

	@Override
	public final boolean isMetaAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return metaAnnotations.isMetaAnnotationPresent(annotationClass);
	}

	@Override
	public final <S extends Annotation> MetaAnnotation<S> getMetaAnnotation(Class<S> annotationClass) {
		return metaAnnotations.getMetaAnnotation(annotationClass);
	}

	@Override
	public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return metaAnnotations.isAnnotationPresent(annotationClass);
	}

	@Override
	public final <S extends Annotation> S getAnnotation(Class<S> annotationClass) {
		return metaAnnotations.getAnnotation(annotationClass);
	}

	@Override
	public final Annotation[] getAnnotations() {
		return metaAnnotations.getAnnotations();
	}

	@Override
	public final Annotation[] getDeclaredAnnotations() {
		return metaAnnotations.getDeclaredAnnotations();
	}
 
}
