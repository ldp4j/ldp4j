/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

final class DelegatedManagedEntity extends ManagedEntity {

	private static final String LITERAL_VALUE_CANNOT_BE_NULL = "Literal value cannot be null";

	private static final String ENTITY_VALUE_CANNOT_BE_NULL = "Entity value cannot be null";

	private static final String PROPERTY_PREDICATE_CANNOT_BE_NULL = "Property predicate cannot be null";

	private static final TimeUnit WAIT_TIME_UNIT = TimeUnit.MICROSECONDS;

	private static final int WAIT_TIME_INCREASE_FACTOR = 2;

	private static final int INITIAL_WAIT_TIME = 1;

	private static final int MAX_WAIT_TIME = 1000000;

	private final Lock write;
	private final Lock read;

	private final Entity delegate;

	private DelegatedManagedEntity(Entity src) {
		this.delegate = src;
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		this.write = lock.writeLock();
		this.read = lock.readLock();
	}

	private ImmutableProperty immutable(URI predicate, Property property) {
		ImmutableProperty result=new ImmutableProperty(predicate,this);
		if(property!=null) {
			result=immutable(property);
		}
		return result;
	}

	private ImmutableProperty immutable(Property property) {
		ImmutableProperty result=new ImmutableProperty(property.predicate(),this);
		for(Value v:property) {
			result=result.addValue(v);
		}
		return result;
	}

	/**
	 * Truncated exponential back-off safe copy
	 * @param entity
	 * @return
	 */
	private Set<ImmutableProperty> safeProperties() {
		long timeout=INITIAL_WAIT_TIME;
		while(true) {
			try {
				if(this.read.tryLock() || this.read.tryLock(timeout,WAIT_TIME_UNIT)) {
					try {
						Set<ImmutableProperty> result=Sets.newLinkedHashSet();
						for(Property p:this.delegate) {
							result.add(immutable(p));
						}
						return result;
					} finally {
						this.read.unlock();
					}
				}
			} catch (InterruptedException e) {
			 // Nothing to do
			}
			timeout=Math.min(timeout*WAIT_TIME_INCREASE_FACTOR, MAX_WAIT_TIME);
		}
	}

	@Override
	synchronized void attach(UUID id, DataSource dataSource) {
		throw new UnsupportedOperationException("Wrapped entities cannot be attached");
	}

	@Override
	synchronized void dettach(DataSource dataSource) {
		throw new UnsupportedOperationException("Wrapped entities cannot be dettached");
	}

	@Override
	void removeProperties(Entity entity) {
		this.write.lock();
		try {
			Property[] properties=Iterables.toArray(this.delegate, Property.class);
			for(Property property:properties) {
				if(property.hasEntityValues()) {
					this.delegate.removeProperty(property.predicate(), entity);
				}
			}
		} finally {
			this.write.unlock();
		}

	}

	@Override
	Collection<ImmutableProperty> properties() {
		return safeProperties();
	}

	@Override
	public DataSource dataSource() {
		return this.delegate.dataSource();
	}

	@Override
	public UUID id() {
		return this.delegate.id();
	}

	@Override
	public Identity identity() {
		return this.delegate.identity();
	}

	@Override
	public ImmutableProperty getProperty(URI predicate) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		this.read.lock();
		try {
			return immutable(predicate, this.delegate.getProperty(predicate));
		} finally {
			this.read.unlock();
		}
	}

	@Override
	public void addProperty(URI predicate, Literal<?> literal) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(literal,LITERAL_VALUE_CANNOT_BE_NULL);
		this.write.lock();
		try {
			this.delegate.addProperty(predicate, literal);
		} finally {
			this.write.unlock();
		}
	}

	@Override
	public void addProperty(URI predicate, Entity entity) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(entity,ENTITY_VALUE_CANNOT_BE_NULL);
		this.write.lock();
		try {
			this.delegate.addProperty(predicate, entity);
		} finally {
			this.write.unlock();
		}
	}

	public void removeProperty(URI predicate, Literal<?> literal) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(literal,LITERAL_VALUE_CANNOT_BE_NULL);
		this.write.lock();
		try {
			this.delegate.removeProperty(predicate, literal);
		} finally {
			this.write.unlock();
		}
	}

	@Override
	public void removeProperty(URI predicate, Entity entity) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(entity,ENTITY_VALUE_CANNOT_BE_NULL);
		this.write.lock();
		try {
			this.delegate.removeProperty(predicate, entity);
		} finally {
			this.write.unlock();
		}
	}

	@Override
	public void removeProperty(URI predicate) {
		this.write.lock();
		try {
			this.delegate.removeProperty(predicate);
		} finally {
			this.write.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(ValueVisitor visitor) {
		visitor.visitEntity(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.delegate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof DelegatedManagedEntity) {
			DelegatedManagedEntity that= (DelegatedManagedEntity) obj;
			result=Objects.equal(this.delegate,that.delegate);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
					add("delegate",this.delegate).
					toString();
	}

	static DelegatedManagedEntity create(Entity src) {
		checkNotNull(src,"Source entity cannot be null");
		return new DelegatedManagedEntity(src);
	}

}