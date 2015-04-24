package org.ldp4j.application.data;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.ValueVisitor;

final class IndividualExtractor<T, S extends Individual<T,S>> implements ValueVisitor {
	private S value=null;
	private final Class<? extends S> clazz;
	
	IndividualExtractor(final Class<? extends S> clazz) {
		this.clazz=clazz;
	}
	@Override
	public void visitLiteral(Literal<?> value) {
		// Discard undesired value
	}
	@Override
	public void visitIndividual(Individual<?, ?> value) {
		if(clazz.isInstance(value)) {
			this.value = clazz.cast(value);
		}
	}
	public S getValue() {
		return this.value;
	}
	public boolean isAvailable() {
		return this.value!=null;
	}
}