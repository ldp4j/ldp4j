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

import static com.google.common.base.Preconditions.*;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public final class Entity extends Value implements Iterable<Property> {

	private static final String LITERAL_VALUE_CANNOT_BE_NULL = "Literal value cannot be null";

	private static final String ENTITY_VALUE_CANNOT_BE_NULL = "Entity value cannot be null";

	private static final String PROPERTY_PREDICATE_CANNOT_BE_NULL = "Property predicate cannot be null";

	private static final TimeUnit WAIT_TIME_UNIT = TimeUnit.MICROSECONDS;

	private static final int WAIT_TIME_INCREASE_FACTOR = 2;

	private static final int INITIAL_WAIT_TIME = 1;

	private static final int MAX_WAIT_TIME = 1000000;

	interface EntityController {

		UUID ownerId();

		UUID id();

		Entity attach(Entity entity);

	}

	private EntityController controller;

	private final Identity identity;

	private final Map<URI,ImmutableProperty> properties;
	private final Lock write;
	private final Lock read;

	Entity(EntityController controller, Identity identity) {
		this.controller = controller;
		this.identity=identity;
		this.properties=Maps.newConcurrentMap();
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		this.write = lock.writeLock();
		this.read = lock.readLock();
	}

	void removeProperties(Entity entity) {
		this.write.lock();
		try {
			Set<URI> predicates=Sets.newLinkedHashSet(this.properties.keySet());
			for(URI predicate:predicates) {
				safeRemove(predicate,entity);
			}
		} finally {
			this.write.unlock();
		}

	}

	private ImmutableProperty unsafeFindPropertyByPredicate(URI propertyId) {
		ImmutableProperty property=this.properties.get(propertyId);
		if(property==null) {
			property=new ImmutableProperty(propertyId,this);
		}
		return property;
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
						return Sets.newLinkedHashSet(this.properties.values());
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

	private boolean safeRemove(URI propertyId, Value value) {
		this.write.lock();
		try {
			return unsafeRemove(propertyId, value);
		} finally {
			this.write.unlock();
		}
	}

	private boolean unsafeRemove(URI propertyId, Value value) {
		ImmutableProperty original=this.properties.get(propertyId);
		if(original==null) {
			return false;
		}
		ImmutableProperty modified=original.removeValue(value);
		if(modified==original) {
			return false;
		}
		if(modified.hasValues()) {
			this.properties.put(propertyId, modified);
		} else {
			this.properties.remove(propertyId);
		}
		return true;
	}

	private void unsafeAdd(URI predicate, Value value) {
		ImmutableProperty original=unsafeFindPropertyByPredicate(predicate);
		ImmutableProperty updated=original.addValue(value);
		if(original!= updated) {
			this.properties.put(updated.predicate(),updated);
		}
	}

	public UUID ownerId() {
		return this.controller.ownerId();
	}

	public UUID id() {
		return this.controller.id();
	}

	public Identity identity() {
		return this.identity;
	}

	public Property getProperty(URI predicate) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		this.read.lock();
		try {
			return unsafeFindPropertyByPredicate(predicate);
		} finally {
			this.read.unlock();
		}
	}

	public void addProperty(URI predicate, Literal<?> literal) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(literal,LITERAL_VALUE_CANNOT_BE_NULL);
		this.write.lock();
		try {
			unsafeAdd(predicate, literal);
		} finally {
			this.write.unlock();
		}
	}

	public void addProperty(URI predicate, Entity entity) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(entity,ENTITY_VALUE_CANNOT_BE_NULL);
		this.write.lock();
		try {
			entity.read.lock();
			try {
				unsafeAdd(predicate, this.controller.attach(entity));
			} finally {
				entity.read.unlock();
			}
		} finally {
			this.write.unlock();
		}
	}

	public void removeProperty(URI predicate, Literal<?> literal) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(literal,LITERAL_VALUE_CANNOT_BE_NULL);
		safeRemove(predicate, literal);
	}

	public void removeProperty(URI predicate, Entity entity) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(entity,ENTITY_VALUE_CANNOT_BE_NULL);
		safeRemove(predicate, entity);
	}

	public void removeProperty(URI predicate) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		this.write.lock();
		try {
			this.properties.remove(predicate);
		} finally {
			this.write.unlock();
		}
	}

	public void merge(Entity entity) {
		checkNotNull(entity,ENTITY_VALUE_CANNOT_BE_NULL);
		checkArgument(entity.identity.equals(this.identity),"Cannot merge individuals with different identities");
		if(entity==this) {
			return;
		}
		Collection<ImmutableProperty> sourceProperties = entity.safeProperties();
		this.write.lock();
		try {
			for(ImmutableProperty source:sourceProperties) {
				ImmutableProperty target = unsafeFindPropertyByPredicate(source.predicate());
				ImmutableProperty merged = target.merge(source);
				this.properties.put(merged.predicate(), merged);
			}
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
	public Iterator<Property> iterator() {
		return ImmutableList.<Property>copyOf(safeProperties()).iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.controller,this.identity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Entity) {
			Entity that= (Entity) obj;
			result=
				Objects.equal(this.controller,that.controller) &&
				Objects.equal(this.identity,that.identity);
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
					add("controller",this.controller).
					add("identity",this.identity).
					toString();
	}

}