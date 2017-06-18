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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-application-data-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.ValueVisitor;

final class LiteralValueExtractor<T> implements ValueVisitor {

	private T value=null;

	private final LiteralAdapter<T> adapter;

	private LiteralValueExtractor(LiteralAdapter<T> adapter) {
		this.adapter = adapter;
	}

	@Override
	public void visitLiteral(Literal<?> value) {
		value.accept(this.adapter);
		this.value=this.adapter.adaptedValue();
	}

	@Override
	public void visitIndividual(Individual<?, ?> value) {
		// Discard undesired value
	}

	T getValue() {
		return this.value;
	}

	boolean isAvailable() {
		return this.value!=null;
	}

	static <T> LiteralValueExtractor<T> newInstance(Class<? extends T> clazz) {
		return new LiteralValueExtractor<T>(LiteralAdapter.newInstance(clazz));
	}

}