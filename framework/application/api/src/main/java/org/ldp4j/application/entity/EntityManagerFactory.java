package org.ldp4j.application.entity;

import java.util.UUID;

import org.ldp4j.application.entity.util.FieldProxy;

final class EntityManagerFactory {

	static class EntityManager {

		private DataSource dataSource;
		private Entity entity;

		private EntityManager(DataSource dataSource, Entity entity) {
			this.dataSource = dataSource;
			this.entity = entity;
		}

		void initialize(UUID identifier) {
			setIdentifier(this.entity,identifier);
			setDataSource(this.entity,this.dataSource);
		}

		public void detach() {
			setIdentifier(this.entity,null);
			setDataSource(this.entity,null);
		}

	}

	private static FieldProxy<Entity,UUID>       IDENTIFIER_PROXY=FieldProxy.create(Entity.class,UUID.class,"identifier");
	private static FieldProxy<Entity,DataSource> DATASOURCE_PROXY=FieldProxy.create(Entity.class,DataSource.class,"dataSource");
	private DataSource dataSource;

	private EntityManagerFactory(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	EntityManager newEntityManager(Entity entity) {
		return new EntityManager(this.dataSource, entity);
	}

	private static void setIdentifier(Entity entity, UUID identifier) {
		IDENTIFIER_PROXY.set(entity, identifier);
	}

	private static void setDataSource(Entity entity, DataSource dataSource) {
		DATASOURCE_PROXY.set(entity, dataSource);
	}

	static EntityManagerFactory create(DataSource dataSource) {
		return new EntityManagerFactory(dataSource);
	}

}