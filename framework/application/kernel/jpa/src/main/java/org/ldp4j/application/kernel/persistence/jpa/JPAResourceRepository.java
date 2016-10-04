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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.2.2
 *   Bundle      : ldp4j-application-kernel-jpa-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceRepository;
import org.ldp4j.application.kernel.template.TemplateLibrary;

final class JPAResourceRepository implements ResourceRepository {

	private final EntityManagerProvider provider;
	private TemplateLibrary templateLibrary;

	JPAResourceRepository(EntityManagerProvider provider) {
		this.provider = provider;
	}

	void setTemplateLibrary(TemplateLibrary templateLibrary) {
		this.templateLibrary = templateLibrary;
	}

	private EntityManager entityManager() {
		return this.provider.entityManager();
	}

	@Override
	public <T extends Resource> T resourceById(ResourceId id, Class<? extends T> expectedResourceClass) {
		EntityManager em = entityManager();

		CriteriaBuilder cb=em.getCriteriaBuilder();

		CriteriaQuery<JPAResource> query =
				cb.createQuery(JPAResource.class);

		Root<JPAResource> descriptor = query.from(JPAResource.class);
		query.
			select(descriptor).
			where(cb.equal(descriptor.get("id"),id)).
			distinct(true);
		List<JPAResource> results = em.createQuery(query).getResultList();
		if(results.isEmpty()) {
			return null;
		}
		JPAResource found = results.get(0);
		if(expectedResourceClass.isInstance(found)) {
			found.setTemplateLibrary(this.templateLibrary);
			return expectedResourceClass.cast(found);
		}
		return null;
	}

	@Override
	public Resource resourceOfId(ResourceId id) {
		Resource found = resourceById(id, Resource.class);
		if(found instanceof Container) {
			return null;
		}
		return found;
	}

	@Override
	public Container containerOfId(ResourceId id) {
		return resourceById(id, Container.class);
	}

	@Override
	public void add(Resource resource) {
		entityManager().persist(resource);
	}

	@Override
	public void remove(Resource resource) {
		entityManager().remove(resource);
	}

}