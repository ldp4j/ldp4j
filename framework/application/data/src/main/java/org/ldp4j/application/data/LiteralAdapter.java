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