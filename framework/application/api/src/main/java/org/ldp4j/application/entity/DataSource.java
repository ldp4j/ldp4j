package org.ldp4j.application.entity;

import java.util.UUID;

public interface DataSource extends Iterable<Entity> {

	UUID id();

	/**
	 * Find the entity managed by the data source that has the specified identifier.
	 * If the data source does not manage an entity with such identity, null
	 * be returned.
	 *
	 * @param identity
	 * @return
	 */
	Entity findById(UUID id);

	/**
	 * Find the entity managed by the data source that has the specified identity.
	 * If the data source does not manage an entity with such identity, null
	 * be returned.
	 *
	 * @param identity
	 * @return
	 */
	Entity findByIdentity(Identity identity);

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
	Entity newEntity(Identity identity);

	boolean contains(Entity entity);

	/**
	 * Remove the entity from the data source. If the entity is not managed by
	 * the data source nothing happens. In other case, the removal cascades to
	 * the removal of the relationships to the entity with other managed
	 * entities.
	 *
	 * @param entity
	 */
	void remove(Entity entity);

	/**
	 * Merge the state of the given entity into the current data source.
	 *
	 * @param entity
	 *            Entity instance
	 * @return the input entity if it was already managed by the data source or
	 *         the entity managed by data source to which the input entity state
	 *         was merged to otherwise
	 */
	Entity merge(Entity entity);

}