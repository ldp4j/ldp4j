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

final class PropertyHelperImpl implements PropertyHelper {

	private URI propertyId;
	private Individual<?, ?> individual;

	PropertyHelperImpl(URI propertyId, Individual<?,?> individual) {
		this.propertyId = propertyId;
		this.individual = individual;
	}

	private Property getProperty() {
		return this.individual.property(propertyId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T firstValue(final Class<? extends T> aClazz) {
		Property property=getProperty();
		if(property==null) {
			return null;
		}
		LiteralValueExtractor<T> extractor =LiteralValueExtractor.newInstance(aClazz);
		for(Value value:property) {
			value.accept(extractor);
			if(extractor.isAvailable()) {
				break;
			}
		}
		return extractor.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualHelper firstIndividual() {
		Property property=getProperty();
		if(property==null) {
			return null;
		}
		for(Value value:property) {
			if(value instanceof Individual<?,?>) {
				return new IndividualHelperImpl((Individual<?,?>)value);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Serializable, S extends Individual<T,S>> T firstIndividual(final Class<? extends S> clazz) {
		Property property=getProperty();
		if(property==null) {
			return null;
		}
		IndividualExtractor<T,S> extractor=IndividualExtractor.newInstance(clazz);
		for(Value value:property) {
			value.accept(extractor);
			if(extractor.isAvailable()) {
				return extractor.getValue().id();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withLiteral(Object rawValue) {
		if(rawValue!=null) {
			Literal<?> value = Literals.newLiteral(rawValue);
			this.individual.addValue(this.propertyId,value);
		}
		return new IndividualPropertyHelperImpl(new IndividualHelperImpl(this.individual),this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id) {
		if(id!=null) {
			@SuppressWarnings("rawtypes")
			Individual<?,?> srcIndividual = this.individual.dataSet().individual((Name)id, LocalIndividual.class);
			this.individual.addValue(this.propertyId,srcIndividual);
		}
		return new IndividualPropertyHelperImpl(new IndividualHelperImpl(this.individual),this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id, String managerId) {
		if(id!=null) {
			Individual<?,?> srcIndividual =
				this.individual.
					dataSet().
						individual(
							ManagedIndividualId.createId(id, managerId),
							ManagedIndividual.class);
			this.individual.addValue(this.propertyId,srcIndividual);
		}
		return new IndividualPropertyHelperImpl(new IndividualHelperImpl(this.individual),this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id, String managerId, URI path) {
		if(id!=null) {
			ManagedIndividualId parentId=ManagedIndividualId.createId(id,managerId);
			RelativeIndividualId individualId=RelativeIndividualId.createId(parentId, path);
			Individual<?,?> srcIndividual=
				this.individual.
					dataSet().
						individual(
							individualId,
							RelativeIndividual.class);
			this.individual.addValue(this.propertyId,srcIndividual);
		}
		return new IndividualPropertyHelperImpl(new IndividualHelperImpl(this.individual),this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Name<?> id, String managerId, String path) {
		if(id!=null) {
			return withIndividual(id, managerId, URI.create(path));
		}
		return new IndividualPropertyHelperImpl(new IndividualHelperImpl(this.individual),this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(URI id) {
		if(id!=null) {
			Individual<?,?> srcIndividual=null;
			if(id.isAbsolute()) {
				srcIndividual=this.individual.dataSet().individual(id, ExternalIndividual.class);
			} else {
				srcIndividual=this.individual.dataSet().individual(id, NewIndividual.class);
			}
			this.individual.addValue(this.propertyId,srcIndividual);
		}
		return new IndividualPropertyHelperImpl(new IndividualHelperImpl(this.individual),this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(String id) {
		if(id!=null) {
			withIndividual(URI.create(id));
		}
		return new IndividualPropertyHelperImpl(new IndividualHelperImpl(this.individual),this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndividualPropertyHelper withIndividual(Term id) {
		if(id!=null) {
			withIndividual(id.qualifiedEntityName());
		}
		return new IndividualPropertyHelperImpl(new IndividualHelperImpl(this.individual),this);
	}

}