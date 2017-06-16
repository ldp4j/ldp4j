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

import java.io.Serializable;
import java.net.URI;

import org.ldp4j.application.vocabulary.Term;

import com.google.common.base.MoreObjects;

final class NullPropertyHelper implements PropertyHelper {

	private final URI propertyId;

	private final IndividualPropertyHelperImpl iph;

	NullPropertyHelper(URI propertyId) {
		this.propertyId = propertyId;
		this.iph = new IndividualPropertyHelperImpl(new NullIndividualHelper(), this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T firstValue(Class<? extends T> aClazz) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualHelper firstIndividual() {
		return new NullIndividualHelper();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Serializable, S extends Individual<T, S>> T firstIndividual(Class<? extends S> clazz) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withLiteral(Object rawValue) {
		return new IndividualPropertyHelperImpl(new NullIndividualHelper(), this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id) {
		return this.iph;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id, String managerId) {
		return this.iph;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id, String managerId, URI path) {
		return this.iph;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id, String managerId, String path) {
		return this.iph;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(URI id) {
		return new IndividualPropertyHelperImpl(new NullIndividualHelper(), this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(String id) {
		return new IndividualPropertyHelperImpl(new NullIndividualHelper(), this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Term id) {
		return new IndividualPropertyHelperImpl(new NullIndividualHelper(), this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("propertyId", this.propertyId).
					toString();
	}

}