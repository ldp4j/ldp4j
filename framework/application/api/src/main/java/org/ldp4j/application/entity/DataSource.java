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
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.entity.Entity.EntityController;
import org.ldp4j.application.entity.spi.IdentifierGenerator;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;

public class DataSource implements Iterable<Entity> {


	private static final class DefaultLocalIdentity<T> extends LocalIdentity<T> {

		private UUID ownerId;
		private T localId;

		private DefaultLocalIdentity(UUID ownerId, T localId) {
			super(IdentifierUtil.createLocalIdentifier(ownerId, localId));
			this.ownerId = ownerId;
			this.localId = localId;
		}

		@Override
		public UUID ownerId() { return this.ownerId; }

		@Override
		public T localId() { return this.localId ; }

		private static <T> LocalIdentity<T> create(UUID ownerId, T localId) {
			return new DefaultLocalIdentity<T>(ownerId,localId);
		}

	}

	private static final class DefaultNameGenerator implements IdentifierGenerator<Long> {

		private AtomicLong counter;

		private DefaultNameGenerator() {
			this.counter=new AtomicLong();
		}

		@Override
		public Long nextIdentifier() {
			return counter.incrementAndGet();
		}

	}

	private interface EntityManager {

		Entity entity();

		void detach();

	}

	private static final class NullEntityManager implements EntityManager {

		private Identity identity;

		private NullEntityManager(Identity identity) {
			this.identity = identity;
		}

		@Override
		public Entity entity() {
			return null;
		}

		@Override
		public void detach() {
			throw new IllegalStateException("Entity '"+identity+"' is not managed by the data source");
		}

	}

	private static final class DefaultEntityManager implements EntityManager {

		private final class InnerEntityController implements EntityController {

			private final UUID ownerId=DefaultEntityManager.this.dataSource.id();
			private final UUID id=DefaultEntityManager.this.identifier;

			/**
			 * {@inheritDoc}
			 */
			@Override
			public UUID ownerId() {
				return detached?null:ownerId;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public UUID id() {
				return detached?null:id;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Entity attach(Entity entity) {
				checkState(!detached,"Entity is detached");
				return DefaultEntityManager.this.dataSource.merge(entity);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public int hashCode() {
				return Objects.hashCode(this.ownerId,this.id);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean equals(Object obj) {
				boolean result=false;
				if(obj instanceof InnerEntityController) {
					InnerEntityController that = (InnerEntityController) obj;
					result=
						Objects.equal(this.ownerId, that.ownerId) &&
						Objects.equal(this.id, that.id);
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
						detached?
							"{%2$s} detached from data source {%1$s}":
							"{%2$s} attached to data source {%1$s}",
						this.ownerId,this.id);
			}

		}

		private boolean detached;

		private DataSource dataSource;
		private InnerEntityController controller;
		private Entity entity;
		private UUID identifier;

		private DefaultEntityManager(DataSource dataSource, UUID identifier, Identity identity) {
			this.dataSource=dataSource;
			this.identifier=identifier;
			this.controller=new InnerEntityController();
			this.entity=new Entity(this.controller,identity);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Entity entity() {
			return this.entity;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void detach() {
			this.detached=true;
		}

	}

	private final UUID identifier;
	private final IdentifierGenerator<?> identifierGenerator;

	private final Lock write;
	private final Lock read;
	private final Map<Identity,EntityManager> entityManagers;
	private final Map<UUID,Identity> entityIdentifiers;

	private DataSource(UUID identifier, IdentifierGenerator<?> nameGenerator) {
		this.identifier = identifier;
		this.identifierGenerator = nameGenerator;
		ReadWriteLock lock = new ReentrantReadWriteLock();
		this.write = lock.writeLock();
		this.read = lock.readLock();
		this.entityManagers=Maps.newLinkedHashMap();
		this.entityIdentifiers=Maps.newLinkedHashMap();
	}

	private Entity safeNewEntity(Identity identity) {
		this.write.lock();
		try {
			if(findByIdentity(identity)!=null) {
				throw new IllegalArgumentException("An entity with the same identity is already managed by the datasource");
			}
			EntityManager manager=new DefaultEntityManager(this,this.nextEntityId(),identity);
			Entity newEntity = manager.entity();
			this.entityIdentifiers.put(newEntity.id(),newEntity.identity());
			this.entityManagers.put(newEntity.identity(),manager);
			return newEntity;
		} finally {
			this.write.unlock();
		}
	}

	private EntityManager unsafeNullableEntityManager(Identity identity) {
		EntityManager entityManager = this.entityManagers.get(identity);
		if(entityManager==null) {
			entityManager=new NullEntityManager(identity);
		}
		return entityManager;
	}

	private UUID nextEntityId() {
		UUID identifier=null;
		do {
			identifier=UUID.randomUUID();
		} while(this.entityIdentifiers.containsKey(identifier));
		return identifier;
	}

	private Object nextLocalId() {
		return this.identifierGenerator.nextIdentifier();
	}

	public UUID id() {
		return this.identifier;
	}

	public Entity newEntity() {
		return safeNewEntity(DefaultLocalIdentity.create(this.identifier, nextLocalId()));
	}

	/**
	 * Create a new entity managed by the data source that has the specified
	 * identity. If the data source does not manage an entity with such
	 * identity, null entity will be returned.
	 *
	 * @param identity
	 *            The identity for the new entity
	 * @return The entity managed by the data source that has the specified
	 *         identity
	 * @throws IllegalArgumentException
	 *             if the data source already manages an entity with the
	 *             specified identity
	 */
	public Entity newEntity(Identity identity) {
		checkNotNull(identity,"Identity cannot be null");
		checkArgument(!DataSource.isLocal(identity),"Identity cannot be local");
		return safeNewEntity(identity);
	}

 	/**
	 * Find the entity managed by the data source that has the specified identifier.
	 * If the data source does not manage an entity with such identity, null
	 * be returned.
	 *
	 * @param identity
	 * @return
	 */
	public Entity findById(UUID id) {
		checkNotNull(id,"Entity identifier cannot be null");
		this.read.lock();
		try {
			return findByIdentity(this.entityIdentifiers.get(id));
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * Find the entity managed by the data source that has the specified identity.
	 * If the data source does not manage an entity with such identity, null
	 * be returned.
	 *
	 * @param identity
	 * @return
	 */
	public Entity findByIdentity(Identity identity) {
		checkNotNull(identity,"Entity identity cannot be null");
		this.read.lock();
		try {
			return unsafeNullableEntityManager(identity).entity();
		} finally {
			this.read.unlock();
		}
	}

	public boolean contains(Entity entity) {
		boolean result=false;
		if(entity!=null) {
			result=findByIdentity(entity.identity())==entity;
		}
		return result;
	}

	/**
	 * Remove the entity from the data source. If the entity is not managed by
	 * the data source nothing happens. In other case, the removal cascades to
	 * the removal of the relationships to the entity with other managed
	 * entities.
	 *
	 * @param entity
	 */
	public void remove(Entity entity) {
		checkNotNull(entity,"Entity cannot be null");
		this.write.lock();
		try {
			if(!contains(entity)) {
				return;
			}
			this.entityIdentifiers.remove(entity.id());
			EntityManager manager = this.entityManagers.remove(entity.identity());
			manager.detach();
			for(EntityManager tmp:this.entityManagers.values()) {
				tmp.entity().removeProperties(entity);
			}
		} finally {
			this.write.unlock();
		}
	}

	/**
	 * Merge the state of the given entity into the current data source.
	 *
	 * @param entity
	 *            Entity instance
	 * @return the input entity if it was already managed by the data source or
	 *         the entity managed by data source to which the input entity state
	 *         was merged to otherwise
	 */
	public Entity merge(Entity entity) {
		checkNotNull(entity,"Entity cannot be null");
		this.write.lock();
		try {
			if(contains(entity)) {
				return entity;
			}
			Entity target=findByIdentity(entity.identity());
			if(target==null) {
				target=newEntity(entity.identity());
			}
			target.merge(entity);
			return target;
		} finally {
			this.write.unlock();
		}
	}

	@Override
	public Iterator<Entity> iterator() {
		this.read.lock();
		try {
			Builder<Entity> builder = ImmutableList.<Entity>builder();
			for(EntityManager manager:this.entityManagers.values()) {
				builder.add(manager.entity());
			}
			return builder.build().iterator();
		} finally {
			this.read.unlock();
		}
	}

	// Syntactic sugar API

	public Entity newEntity(Key<?> key) {
		return newEntity(IdentityFactory.createManagedIdentity(key));
	}

	public Entity newEntity(Class<?> owner, Object nativeId) {
		return newEntity(IdentityFactory.createManagedIdentity(owner,nativeId));
	}

	public Entity newEntity(Key<?> key, URI path) {
		return newEntity(IdentityFactory.createRelativeIdentity(key,path));
	}

	public Entity newEntity(Class<?> owner, Object nativeId, URI path) {
		return newEntity(IdentityFactory.createRelativeIdentity(owner,nativeId,path));
	}

	public Entity newEntity(URI location) {
		return newEntity(IdentityFactory.createExternalIdentity(location));
	}

	private static boolean isLocal(Identity identity) {
		return identity instanceof LocalIdentity<?>;
	}

	public static DataSource create() {
		return create(new DefaultNameGenerator());
	}

	public static DataSource create(IdentifierGenerator<?> nameGenerator) {
		return new DataSource(UUID.randomUUID(),nameGenerator);
	}


}