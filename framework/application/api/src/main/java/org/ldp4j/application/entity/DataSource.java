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
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.entity.spi.NameGenerator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class DataSource implements Iterable<Entity> {

	private static final class DefaultNameGenerator implements NameGenerator<Long> {

		private AtomicLong counter;

		private DefaultNameGenerator() {
			this.counter=new AtomicLong();
		}

		@Override
		public Name<Long> nextName() {
			return NamingScheme.getDefault().name(counter.incrementAndGet());
		}

	}

	private final UUID identifier;
	private final NameGenerator<?> nameGenerator;

	private final ReadWriteLock lock;
	private final Map<Identity,Entity> entities;
	private final Map<UUID,Identity> identifiers;

	private DataSource(UUID identifier, NameGenerator<?> nameGenerator) {
		this.identifier = identifier;
		this.nameGenerator = nameGenerator;
		this.lock=new ReentrantReadWriteLock();
		this.entities=Maps.newLinkedHashMap();
		this.identifiers=Maps.newLinkedHashMap();
	}

	private boolean isStored(Entity entity) {
		return
			this.identifiers.get(entity.identifier())==entity.identity() &&
			this.entities.get(entity.identity())==entity;
	}

	private UUID storeEntity(Entity entity) {
		UUID identifier=null;
		do {
			identifier=UUID.randomUUID();
		} while(this.identifiers.containsKey(identifier));
		this.identifiers.put(identifier, entity.identity());
		this.entities.put(entity.identity(),entity);
		return identifier;
	}

	private void deleteEntity(Entity entity) {
		this.entities.remove(entity.identity());
		this.identifiers.remove(entity.identifier());
	}

	// TODO: Make thread-safe before making it public
	private boolean contains(Entity entity) {
		boolean result=false;
		if(entity!=null) {
			result=
				entity.identifier()!=null &&
				entity.dataSource()==this &&
				isStored(entity);
		}
		return result;
	}

	UUID identifier() {
		return this.identifier;
	}

	Name<?> nextName() {
		return this.nameGenerator.nextName();
	}

	/**
	 * Find the entity managed by the data source that has the specified entity.
	 * If the data source does not manage an entity with such identity, null
	 * entity will be returned.
	 *
	 * @param identity
	 * @return
	 */
	public Entity find(Identity identity) {
		checkNotNull(identity,"Identity cannot be null");
		this.lock.readLock().lock();
		try {
			return this.entities.get(identity);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	/**
	 * Add a new entity to the data source. The entity becomes managed by the
	 * data source. If the entity is already managed by another data source an
	 * exception will be thrown.
	 *
	 * The operation is not cascading, that is, any entity that is related to
	 * the entity and is not already managed by the data source will not be
	 * added to the data source.
	 *
	 * @param entity
	 */
	public void add(Entity entity) {
		checkNotNull(entity,"Entity cannot be null");
		this.lock.writeLock().lock();
		try {
			if(contains(entity)) {
				return;
			}
			if(entity.isInitialized()) {
				throw new IllegalArgumentException("Entity is already managed by another datasource");
			}
			if(find(entity.identity())!=null) {
				throw new IllegalArgumentException("An entity with the same identity is already managed by the datasource");
			}
			UUID identifier = storeEntity(entity);
			entity.initialize(this,identifier);
		} finally {
			this.lock.writeLock().unlock();
		}
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
		this.lock.writeLock().lock();
		try {
			if(!contains(entity)) {
				return;
			}
			deleteEntity(entity);
			entity.detach();
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	/**
	 * Merge the state of the given entity into the current data source.
	 * @param entity Entity instance
	 * @return the managed instance that the state was merged to
	 */
	public Entity merge(Entity entity) {
		checkNotNull(entity,"Entity cannot be null");
		this.lock.writeLock().lock();
		try {
			if(contains(entity)) {
				return entity;
			}
			Entity target=this.entities.get(entity.identity());
			if(target==null) {
				target=new Entity(entity.identity());
				add(target);
			}
			mergeEntities(entity,target);
			return target;
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public Iterator<Entity> iterator() {
		this.lock.readLock().lock();
		try {
			return ImmutableList.copyOf(this.entities.values()).iterator();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	private static void mergeEntities(final Entity source, final Entity target) {
		checkNotNull(source,"Entity cannot be null");
		for(Property property:source) {
			final URI predicateId = property.predicate();
			for(Value value:property) {
				value.accept(
					new ValueVisitor() {
						@Override
						public void visitLiteral(Literal<?> value) {
							target.addProperty(predicateId,value);
						}
						@Override
						public void visitEntity(Entity value) {
							target.addProperty(predicateId,value);
						}
					}
				);
			}
		}
	}

	public static DataSource create() {
		return create(new DefaultNameGenerator());
	}

	public static DataSource create(NameGenerator<?> nameGenerator) {
		return new DataSource(UUID.randomUUID(),nameGenerator);
	}


}