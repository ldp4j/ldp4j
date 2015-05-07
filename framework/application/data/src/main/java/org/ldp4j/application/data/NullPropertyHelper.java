package org.ldp4j.application.data;

import java.net.URI;

import org.ldp4j.application.vocabulary.Term;

import com.google.common.base.Objects;

final class NullPropertyHelper implements PropertyHelper {

	private final URI propertyId;

	private final IndividualPropertyHelperImpl iph;

	NullPropertyHelper(URI propertyId) {
		this.propertyId = propertyId;
		this.iph = new IndividualPropertyHelperImpl(new NullIndividualHelper(), this);
	}

	@Override
	public <T> T firstValue(Class<? extends T> aClazz) {
		return null;
	}

	@Override
	public IndividualHelper firstIndividual() {
		return new NullIndividualHelper();
	}

	@Override
	public <T, S extends Individual<T, S>> T firstIndividual(Class<? extends S> clazz) {
		return null;
	}

	@Override
	public <T> IndividualPropertyHelper withLiteral(T rawValue) {
		return new IndividualPropertyHelperImpl(new NullIndividualHelper(), this);
	}

	@Override
	public <T> IndividualPropertyHelper withIndividual(Name<?> id) {
		return this.iph;
	}

	@Override
	public <T> IndividualPropertyHelper withIndividual(URI id) {
		return new IndividualPropertyHelperImpl(new NullIndividualHelper(), this);
	}

	@Override
	public <T> IndividualPropertyHelper withIndividual(String id) {
		return new IndividualPropertyHelperImpl(new NullIndividualHelper(), this);
	}

	@Override
	public <T> IndividualPropertyHelper withIndividual(Term id) {
		return new IndividualPropertyHelperImpl(new NullIndividualHelper(), this);
	}

	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					add("propertyId", this.propertyId).
					toString();
	}

}