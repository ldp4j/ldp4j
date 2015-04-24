package org.ldp4j.application.data;

import java.net.URI;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.vocabulary.Term;

public final class IndividualHelper {

	private final Individual<?, ?> individual;

	public IndividualHelper(Individual<?,?> individual) {
		this.individual = individual;
	}

	public PropertyHelper property(URI propertyId) {
		return new PropertyHelper(this.individual.property(propertyId));
	}

	public PropertyHelper property(String propertyId) {
		return property(URI.create(propertyId));
	}

	public PropertyHelper property(Term property) {
		return property(property.as(URI.class));
	}

}