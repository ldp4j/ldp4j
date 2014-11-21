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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

final class DefaultManagedEntity extends ManagedEntity {

	private interface EntityController {

		DataSource owner();

		UUID id();

		CompositeManagedEntity merge(Entity entity);

		boolean isOwner(DataSource dataSource);

	}

	private final class AttachedEntityController implements EntityController {
		private final DataSource dataSource;
		private final UUID id;

		private AttachedEntityController(DataSource dataSource, UUID id) {
			this.dataSource = dataSource;
			this.id = id;
		}

		@Override
		public DataSource owner() {
			return this.dataSource;
		}

		@Override
		public UUID id() {
			return this.id;
		}

		@Override
		public CompositeManagedEntity merge(Entity entity) {
			return dataSource.merge(entity);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isOwner(DataSource dataSource) {
			return this.dataSource==dataSource;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(this.id,this.dataSource);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			boolean result=false;
			if(obj instanceof AttachedEntityController) {
				AttachedEntityController that = (AttachedEntityController) obj;
				result=
					Objects.equal(this.id, that.id) &&
					this.dataSource==that.dataSource;
			}
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return
				String.format(
					"{%2$s} attached to data source {%1$s}",
					this.dataSource.id(),this.id);
		}

	}

	private static final class DetachedEntityController implements EntityController {
		@Override
		public DataSource owner() {
			return null;
		}
		@Override
		public UUID id() {
			return null;
		}
		@Override
		public CompositeManagedEntity merge(Entity entity) {
			return CompositeManagedEntity.wrap(entity);
		}
		@Override
		public boolean isOwner(DataSource dataSource) {
			return false;
		}
	}

	private static final String LITERAL_VALUE_CANNOT_BE_NULL = "Literal value cannot be null";

	private static final String ENTITY_VALUE_CANNOT_BE_NULL = "Entity value cannot be null";

	private static final String PROPERTY_PREDICATE_CANNOT_BE_NULL = "Property predicate cannot be null";

	private static final TimeUnit WAIT_TIME_UNIT = TimeUnit.MICROSECONDS;

	private static final int WAIT_TIME_INCREASE_FACTOR = 2;

	private static final int INITIAL_WAIT_TIME = 1;

	private static final int MAX_WAIT_TIME = 1000000;

	private EntityController controller;

	private final Identity identity;

	private final Map<URI,ImmutableProperty> properties;
	private final Lock write;
	private final Lock read;

	DefaultManagedEntity(Identity identity) {
		setController(new DetachedEntityController());
		this.identity=identity;
		this.properties=Maps.newConcurrentMap();
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		this.write = lock.writeLock();
		this.read = lock.readLock();
	}

	private void setController(EntityController controller) {
		this.controller=controller;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	synchronized void attach(UUID id, DataSource dataSource) {
		setController(new AttachedEntityController(dataSource, id));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	synchronized void dettach(DataSource dataSource) {
		checkNotNull(dataSource,"Data source cannot be null");
		checkArgument(this.controller.isOwner(dataSource),"Cannot detach from '"+dataSource.id()+"'");
		setController(new DetachedEntityController());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	Collection<ImmutableProperty> properties() {
		return safeProperties();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataSource dataSource() {
		return this.controller.owner();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UUID id() {
		return this.controller.id();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity identity() {
		return this.identity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImmutableProperty getProperty(URI predicate) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		this.read.lock();
		try {
			return unsafeFindPropertyByPredicate(predicate);
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addProperty(URI predicate, Entity entity) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(entity,ENTITY_VALUE_CANNOT_BE_NULL);
		this.write.lock();
		try {
			unsafeAdd(predicate, this.controller.merge(entity));
		} finally {
			this.write.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeProperty(URI predicate, Literal<?> literal) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(literal,LITERAL_VALUE_CANNOT_BE_NULL);
		safeRemove(predicate, literal);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeProperty(URI predicate, Entity entity) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		checkNotNull(entity,ENTITY_VALUE_CANNOT_BE_NULL);
		safeRemove(predicate, entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeProperty(URI predicate) {
		checkNotNull(predicate,PROPERTY_PREDICATE_CANNOT_BE_NULL);
		this.write.lock();
		try {
			this.properties.remove(predicate);
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
		return Objects.hashCode(this.controller,this.identity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof DefaultManagedEntity) {
			DefaultManagedEntity that= (DefaultManagedEntity) obj;
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

	static <T extends Identity> DefaultManagedEntity create(T identity) {
		checkNotNull(identity,"Identity cannot be null");
		return new DefaultManagedEntity(identity);
	}

}