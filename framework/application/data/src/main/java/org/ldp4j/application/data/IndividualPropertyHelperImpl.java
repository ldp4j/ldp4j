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
import java.util.Set;

import org.ldp4j.application.vocabulary.Term;

final class IndividualPropertyHelperImpl implements IndividualPropertyHelper {

	private final IndividualHelper iHelper;
	private final PropertyHelper pHelper;

	IndividualPropertyHelperImpl(IndividualHelper iHelper, PropertyHelper pHelper) {
		this.iHelper = iHelper;
		this.pHelper = pHelper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T firstValue(Class<? extends T> aClazz) {
		return this.pHelper.firstValue(aClazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Serializable, S extends Individual<T, S>> T firstIndividual(Class<? extends S> clazz) {
		return this.pHelper.firstIndividual(clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualHelper firstIndividual() {
		return this.pHelper.firstIndividual();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<URI> types() {
		return this.iHelper.types();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyHelper property(URI propertyId) {
		return this.iHelper.property(propertyId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyHelper property(String propertyId) {
		return this.iHelper.property(propertyId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyHelper property(Term property) {
		return this.iHelper.property(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withLiteral(Object rawValue) {
		return this.pHelper.withLiteral(rawValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id) {
		return this.pHelper.withIndividual(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id, String managerId) {
		return this.pHelper.withIndividual(id,managerId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id, String managerId, URI path) {
		return this.pHelper.withIndividual(id,managerId,path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id, String managerId, String path) {
		return this.pHelper.withIndividual(id,managerId,path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(URI id) {
		return this.pHelper.withIndividual(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(String id) {
		return this.pHelper.withIndividual(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Term id) {
		return this.pHelper.withIndividual(id);
	}

}