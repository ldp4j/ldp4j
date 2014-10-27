package org.ldp4j.application.entity;

final class ImmutableLiteral<T> implements Literal<T> {

	private final T value;

	private ImmutableLiteral(T value) {
		this.value = value;
	}

	@Override
	public void accept(ValueVisitor visitor) {
		visitor.visitLiteral(this);
	}

	@Override
	public T value() {
		return this.value;
	}

	static <T> ImmutableLiteral<T> create(T value) {
		return new ImmutableLiteral<T>(value);
	}

}
