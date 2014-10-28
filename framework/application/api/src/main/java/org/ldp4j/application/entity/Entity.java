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
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class Entity implements Value, Iterable<Property> {

	private static final class SurrogateEntity extends Entity {

		private Entity state;

		private SurrogateEntity(Entity state) {
			super(state.identity);
			this.state=state;
		}

		private synchronized Entity getState() {
			return this.state;
		}

		private synchronized void attach(DataSource dataSource) {
			this.state=this.state.attachTo(dataSource);
		}

		@Override
		boolean isInitialized() {
			return getState().isInitialized();
		}

		@Override
		void initialize(DataSource dataSource, UUID identifier) {
			getState().initialize(dataSource, identifier);
		}

		@Override
		void detach() {
			getState().detach();
		}

		@Override
		public DataSource dataSource() {
			return getState().dataSource();
		}

		@Override
		public UUID identifier() {
			return getState().identifier();
		}

		@Override
		public void addProperty(URI propertyId, Literal<?> value) {
			getState().addProperty(propertyId, value);
		}

		@Override
		public void addProperty(URI propertyId, Entity entity) {
			getState().addProperty(propertyId, entity);
		}

		@Override
		public void removeProperty(URI propertyId, Literal<?> value) {
			getState().removeProperty(propertyId, value);
		}

		@Override
		public void removeProperty(URI propertyId, Entity entity) {
			getState().removeProperty(propertyId, entity);
		}

		@Override
		public void removeProperty(URI propertyId) {
			getState().removeProperty(propertyId);
		}

		@Override
		public void accept(ValueVisitor visitor) {
			getState().accept(visitor);
		}

		@Override
		public Iterator<Property> iterator() {
			return getState().iterator();
		}

	}

	private final ConcurrentMap<URI,Property> properties;

	private final Lock lock;
	private final Condition notUpdatingState;
	private final Condition notInUse;
	private final AtomicBoolean updatingState;
	private final AtomicBoolean inUse;

	/**
	 * Guarded by {@code lock}, associated conditions, and flags.
	 */
	private final Map<Entity,SurrogateEntity> surrogates;

	/**
	 * Guarded by {@code lock}, associated conditions, and flags.
	 */
	private DataSource dataSource;

	/**
	 * Guarded by {@code lock}, associated conditions, and flags.
	 */
	private UUID identifier;

	/**
	 * Guarded by {@code lock}, associated conditions, and flags.
	 */
	private final AtomicBoolean initialized;

	private Identity identity;

	public Entity(Identity identity) {
		this.properties=Maps.newConcurrentMap();
		this.surrogates=Maps.newIdentityHashMap();
		this.lock=new ReentrantLock();
		this.initialized=new AtomicBoolean(false);
		this.updatingState=new AtomicBoolean(false);
		this.inUse=new AtomicBoolean(false);
		this.notUpdatingState=lock.newCondition();
		this.notInUse=lock.newCondition();
		setIdentity(identity);
	}

	public Entity(Key<?> key) {
		this(IdentityFactory.createManagedIdentity(key));
	}

	public Entity(Class<?> owner, Object nativeId) {
		this(IdentityFactory.createManagedIdentity(owner,nativeId));
	}

	public Entity(Key<?> key, URI path) {
		this(IdentityFactory.createRelativeIdentity(key,path));
	}

	public Entity(Class<?> owner, Object nativeId, URI path) {
		this(IdentityFactory.createRelativeIdentity(owner,nativeId,path));
	}

	public Entity(URI location) {
		this(IdentityFactory.createExternalIdentity(location));
	}

	public Entity(DataSource dataSource) {
		this(IdentityFactory.createLocalIdentity(dataSource));
	}

	private void setIdentity(Identity identity) {
		checkNotNull(identity,"Identity cannot be null");
		this.identity=identity;
	}

	private Entity createSurrogate(Entity entity) {
		SurrogateEntity surrogate=this.surrogates.get(entity);
		if(surrogate==null) {
			surrogate=new SurrogateEntity(entity);
			this.surrogates.put(entity, surrogate);
		}
		return surrogate;
	}

	private void attachSurrogates(DataSource dataSource) {
		for(SurrogateEntity surrogate:this.surrogates.values()) {
			surrogate.attach(dataSource);
		}
		this.surrogates.clear();
	}

	private Property modifiableProperty(URI propertyId) {
		Property property=new Property(propertyId, this);
		Property result=this.properties.putIfAbsent(propertyId, property);
		if(result==null) {
			result=property;
		}
		return result;
	}

	private Property optionalProperty(URI propertyId) {
		Property result=this.properties.get(propertyId);
		if(result==null) {
			result=new Property(propertyId, this);
		}
		return result;
	}

	private Entity resolve(Entity entity) {
		this.lock.lock();
		try {
			while(!this.initialized.get() && this.updatingState.get()) {
				try {
					this.notUpdatingState.await();
				} catch (InterruptedException e) {
					// Nothing to do but retry
				}
			}
			this.inUse.set(true);
			try {
				Entity surrogateEntity=null;
				if(this.initialized.get()) {
					surrogateEntity=this.dataSource.merge(entity);
				} else {
					surrogateEntity=createSurrogate(entity);
				}
				return surrogateEntity;
			} finally {
				this.inUse.set(false);
				this.notInUse.signalAll();
			}
		} finally {
			this.lock.unlock();
		}
	}

	private Entity attachTo(DataSource dataSource) {
		this.lock.lock();
		try {
			while(this.updatingState.get()) {
				try {
					this.notUpdatingState.await();
				} catch (InterruptedException e) {
					// Nothing to do but retry
				}
			}
			this.inUse.set(true);
			Entity surrogateEntity=this;
			if(this.initialized.get()) {
				surrogateEntity=dataSource.merge(surrogateEntity);
			} else {
				dataSource.add(surrogateEntity);
			}
			this.inUse.set(false);
			this.notInUse.signalAll();
			return surrogateEntity;
		} finally {
			this.lock.unlock();
		}
	}

	boolean isInitialized() {
		this.lock.lock();
		try {
			while(this.updatingState.get()) {
				try {
					this.notUpdatingState.await();
				} catch (InterruptedException e) {
					// Nothing to do but retry
				}
			}
			this.inUse.set(true);
			try {
				return this.initialized.get();
			} finally {
				this.inUse.set(false);
				this.notInUse.signalAll();
			}
		} finally {
			this.lock.unlock();
		}
	}

	void initialize(DataSource dataSource, UUID identifier) {
		checkNotNull(dataSource,"Datasource cannot be null");
		checkNotNull(identifier,"Identifier cannot be null");
		this.lock.lock();
		try {
			while(this.inUse.get()) {
				try {
					this.notInUse.await();
				} catch (InterruptedException e) {
					// Nothing to do but retry
				}
			}
			this.updatingState.set(true);
			try {
				checkState(!this.initialized.get(), "Entity has already been initialized");
				this.dataSource=dataSource;
				this.identifier=identifier;
				attachSurrogates(dataSource);
				this.initialized.set(true);
			} finally {
				this.updatingState.set(false);
				this.notUpdatingState.signalAll();
			}
		} finally {
			this.lock.unlock();
		}
	}

	void detach() {
		this.lock.lock();
		try {
			while(this.inUse.get()) {
				try {
					this.notInUse.await();
				} catch (InterruptedException e) {
					// Nothing to do but retry
				}
			}
			this.updatingState.set(true);
			try {
				checkState(this.initialized.get(),"Entity has already been detached");
				this.dataSource=null;
				this.identifier=null;
				this.initialized.set(false);
			} finally {
				this.updatingState.set(false);
				this.notUpdatingState.signalAll();
			}
		} finally {
			this.lock.unlock();
		}
	}

	public DataSource dataSource() {
		return this.dataSource;
	}

	public UUID identifier() {
		return this.identifier;
	}

	public Identity identity() {
		return this.identity;
	}

	public Property getProperty(URI propertyId) {
		Property result=this.properties.get(propertyId);
		if(result==null) {
			result=new Property(propertyId, this);
		} else {
			result=new Property(result);
		}
		return result;
	}

	public void addProperty(URI propertyId, Literal<?> value) {
		checkNotNull(propertyId,"Property identifier cannot be null");
		checkNotNull(value,"Literal value cannot be null");
		modifiableProperty(propertyId).addValue(value);
	}

	public void addProperty(URI propertyId, Entity entity) {
		checkNotNull(propertyId,"Property identifier cannot be null");
		checkNotNull(entity,"Entity cannot be null");
		Entity managedEntity = resolve(entity);
		modifiableProperty(propertyId).
			addValue(managedEntity);
	}

	public void removeProperty(URI propertyId, Literal<?> value) {
		checkNotNull(propertyId,"Property identifier cannot be null");
		checkNotNull(value,"Literal value cannot be null");
		optionalProperty(propertyId).removeValue(value);
	}

	public void removeProperty(URI propertyId, Entity entity) {
		checkNotNull(propertyId,"Property identifier cannot be null");
		checkNotNull(entity,"Entity cannot be null");
		Entity managedEntity=dataSource().find(entity.identity());
		optionalProperty(propertyId).
			removeValue(managedEntity);
	}

	public void removeProperty(URI propertyId) {
		checkNotNull(propertyId,"Property identifier cannot be null");
		this.properties.remove(propertyId);
	}

	@Override
	public void accept(ValueVisitor visitor) {
		visitor.visitEntity(this);
	}

	@Override
	public Iterator<Property> iterator() {
		return ImmutableList.copyOf(this.properties.values()).iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.identifier(),this.dataSource(),this.identity());
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
				Objects.equal(this.dataSource(), that.dataSource()) &&
				Objects.equal(this.identifier(), that.identifier()) &&
				Objects.equal(this.identity(), that.identity());
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
					add("dataSource",this.dataSource()).
					add("identifier",this.identifier()).
					add("identity",this.identity()).
					toString();
	}

}
