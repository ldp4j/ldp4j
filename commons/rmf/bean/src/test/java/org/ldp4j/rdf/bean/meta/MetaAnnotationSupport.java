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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ldp4j.rdf.bean.meta.MetaAnnotation.AnnotationDeclaration;

final class MetaAnnotationSupport<T> implements MetaAnnotated<T> {

	private final Map<Class<? extends Annotation>, MetaAnnotation<? extends Annotation>> metaAnnotations;
	private final Annotated<?> annotated;
	private final T metaAnnotated;

	private MetaAnnotationSupport(T metaAnnotated, Annotated<?> annotated) {
		this.metaAnnotations = new HashMap<Class<? extends Annotation>, MetaAnnotation<?>>();
		this.metaAnnotated = metaAnnotated;
		this.annotated=annotated;
	}

	@Override
	public T get() {
		return metaAnnotated;
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return metaAnnotations.containsKey(annotationClass);
	}

	@Override
	public <S extends Annotation> S getAnnotation(Class<S> annotationClass) {
		@SuppressWarnings("unchecked")
		MetaAnnotation<S> annotation = (MetaAnnotation<S>) metaAnnotations.get(annotationClass);
		return (annotation == null) ? null : annotation.get();
	}

	@Override
	public Annotation[] getAnnotations() {
		List<Annotation> annotations=new ArrayList<Annotation>();
		for(MetaAnnotation<?> metaAnnotation:metaAnnotations.values()) {
			for(AnnotationDeclaration<?> declaration:metaAnnotation.getDeclarations()) {
				annotations.add(declaration.get());
			}
		}
		Annotation[] result = new Annotation[this.metaAnnotations.size()];
		return annotations.toArray(result);
	}

    @Override
	public Annotation[] getDeclaredAnnotations() {
		return annotated.getDeclaredAnnotations();
	}

	@Override
	public boolean isMetaAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return metaAnnotations.containsKey(annotationClass);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends Annotation> MetaAnnotation<S> getMetaAnnotation(Class<S> annotationClass) {
		return (MetaAnnotation<S>)metaAnnotations.get(annotationClass);
	}

	@Override
	public Collection<MetaAnnotation<?>> getMetaAnnotations() {
		return metaAnnotations.values();
	}

	private static <T, S extends Annotation> void addMetaAnnotation(S annotation, Annotated<?> scope, MetaAnnotationSupport<T> map) {
		Class<? extends Annotation> annotationType = annotation.annotationType();
		@SuppressWarnings("unchecked")
		MetaAnnotation<S> metaAnnotation = (MetaAnnotation<S>)map.metaAnnotations.get(annotationType);
		if(metaAnnotation==null) {
			metaAnnotation=new MetaAnnotation<S>();
			map.metaAnnotations.put(annotationType, metaAnnotation);
		}
		metaAnnotation.addDeclaration(annotation, scope);
	}

	static <T> MetaAnnotationSupport<T> forAnnotatedObjects(T object, final List<? extends Annotated<?>> elements) {
		MetaAnnotationSupport<T> result = new MetaAnnotationSupport<T>(object,elements.get(0));
		for(Annotated<?> element:elements) {
			for(Annotation annotation:element.getDeclaredAnnotations()) {
				addMetaAnnotation(annotation,element,result);
			}
		}
		return result;
	}

}
