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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-reflection:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-reflection-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.reflect.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * An interface for facilitating the introspection of annotated elements.
 *
 * @author Miguel Esteban Guti&eacute;rrez
 *
 * @param <T>
 *            The introspected {@code AnnotatedElement} type.
 * @see java.lang.reflect.AnnotatedElement
 */
public interface MetaAnnotatedElement<T extends AnnotatedElement> extends Meta<T> {

	/**
	 * @see java.lang.reflect.AnnotatedElement#getAnnotation(Class)
	 */
	<A extends Annotation> A getAnnotation(Class<A> annotationClass);

	/**
	 * @see java.lang.reflect.AnnotatedElement#getAnnotations()
	 */
	List<Annotation> getAnnotations();

	/**
	 * @see java.lang.reflect.AnnotatedElement#getDeclaredAnnotations()
	 */
	List<Annotation> getDeclaredAnnotations();

	/**
	 * @see java.lang.reflect.AnnotatedElement#isAnnotationPresent(Class)
	 */
	boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);
}