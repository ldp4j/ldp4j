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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final  class MetaAnnotation<T extends Annotation> {

	private final List<AnnotationDeclaration<T>> declarations;

	public static class AnnotationDeclaration<T extends Annotation> {
		
		private final T annotation;

		private final Annotated<?> scope;

		private AnnotationDeclaration(T annotation, Annotated<?> scope) {
			this.annotation = annotation;
			this.scope = scope;
		}

		public T get() {
			return annotation;
		}

		public  Annotated<?> getScope() {
			return scope;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("AnnotationDeclaration [");
			builder.append("annotation=").append(annotation).append(", ");
			builder.append("scope=").append(scope);
			builder.append("]");
			return builder.toString();
		}

	}
	
	public MetaAnnotation() {
		this.declarations = new ArrayList<AnnotationDeclaration<T>>();
	}

	public T get() {
		return declarations.get(0).get();
	}

	public Annotated<?> getScope() {
		return declarations.get(0).getScope();
	}
	
	public boolean isOverriden() {
		return declarations.size()>1;
	}
	
	public List<AnnotationDeclaration<T>> getDeclarations() {
		return Collections.unmodifiableList(new ArrayList<AnnotationDeclaration<T>>(declarations));
	}

	void addDeclaration(T annotation, Annotated<?> scope) {
		this.declarations.add(new AnnotationDeclaration<T>(annotation,scope));
	}

	private static final String NL = System.getProperty("line.separator");

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetaAnnotation<").append(get().annotationType().getCanonicalName()).append("> {");
		int i=0;
		for(Iterator<AnnotationDeclaration<T>> it=declarations.iterator();it.hasNext();i++) {
			builder.append(NL).append("\t[").append(i).append("] ").append(it.next());
		}
		builder.append(NL).append("}");
		return builder.toString();
	}
	
	
	
}