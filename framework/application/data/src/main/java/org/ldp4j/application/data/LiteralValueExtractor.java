package org.ldp4j.application.data;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.ValueVisitor;

final class LiteralValueExtractor<T> implements ValueVisitor {

	private T value=null;

	private final LiteralAdapter<T> adapter;

	LiteralValueExtractor(LiteralAdapter<T> adapter) {
		this.adapter = adapter;
	}

	@Override
	public void visitLiteral(Literal<?> value) {
		value.accept(this.adapter);
		this.value=this.adapter.value;
	}

	@Override
	public void visitIndividual(Individual<?, ?> value) {
		// Discard undesired value
	}

	public T getValue() {
		return this.value;
	}

	public boolean isAvailable() {
		return this.value!=null;
	}

}