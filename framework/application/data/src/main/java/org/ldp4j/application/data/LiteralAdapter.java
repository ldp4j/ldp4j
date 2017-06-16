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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import org.ldp4j.application.data.LanguageLiteral;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.LiteralVisitor;
import org.ldp4j.application.data.TypedLiteral;

final class LiteralAdapter<T> implements LiteralVisitor {

	private T value=null;
	private final Class<? extends T> clazz;

	private LiteralAdapter(Class<? extends T> aClazz) {
		this.clazz=aClazz;
	}

	private void adaptValue(Object object) {
		// Extension for supporting conversions
		this.value=null;
		if(this.clazz.isInstance(object)) {
			this.value=this.clazz.cast(object);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitLiteral(Literal<?> literal) {
		adaptValue(literal.get());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitTypedLiteral(TypedLiteral<?> literal) {
		adaptValue(literal.get());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitLanguageLiteral(LanguageLiteral literal) {
		adaptValue(literal.get());
	}

	T adaptedValue() {
		return this.value;
	}

	static <T> LiteralAdapter<T> newInstance(Class<? extends T> clazz) {
		return new LiteralAdapter<T>(clazz);
	}

}