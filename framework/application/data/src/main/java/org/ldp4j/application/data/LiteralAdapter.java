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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-data-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import org.ldp4j.application.data.LanguageLiteral;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.LiteralVisitor;
import org.ldp4j.application.data.TypedLiteral;

final class LiteralAdapter<T> implements LiteralVisitor {

	T value=null;
	private final Class<? extends T> clazz;

	LiteralAdapter(Class<? extends T> aClazz) {
		this.clazz=aClazz;
	}

	private T cast(Object object) {
		// Extension for supporting conversions
		if(this.clazz.isInstance(object)) {
			return this.clazz.cast(object);
		}
		return null;
	}

	@Override
	public void visitLiteral(Literal<?> literal) {
		this.value=cast(literal.get());
	}

	@Override
	public void visitTypedLiteral(TypedLiteral<?> literal) {
		this.value=cast(literal.get());
	}

	@Override
	public void visitLanguageLiteral(LanguageLiteral literal) {
		this.value=cast(literal.get());
	}

}