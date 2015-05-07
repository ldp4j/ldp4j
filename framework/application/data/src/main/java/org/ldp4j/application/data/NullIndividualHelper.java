package org.ldp4j.application.data;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.ldp4j.application.vocabulary.Term;

final class NullIndividualHelper implements IndividualHelper {

	@Override
	public Set<URI> types() {
		return Collections.emptySet();
	}

	@Override
	public PropertyHelper property(URI propertyId) {
		return new NullPropertyHelper(propertyId);
	}

	@Override
	public PropertyHelper property(String propertyId) {
		return property(URI.create(propertyId));
	}

	@Override
	public PropertyHelper property(Term property) {
		return property(property.as(URI.class));
	}

}