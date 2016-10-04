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

import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.endpoint.EndpointRepository;
import org.ldp4j.application.kernel.resource.ResourceId;

final class JPAEndpointRepository implements EndpointRepository {

	 private final EntityManagerProvider provider;

	JPAEndpointRepository(EntityManagerProvider provider) {
		this.provider = provider;
	}

	private EntityManager entityManager() {
		return this.provider.entityManager();
	}

	@Override
	public Endpoint endpointOfPath(String path) {
		return entityManager().find(JPAEndpoint.class, path);
	}

	@Override
	public Endpoint endpointOfResource(ResourceId id) {
		CriteriaBuilder cb =
				entityManager().getCriteriaBuilder();

		CriteriaQuery<JPAEndpoint> query =
				cb.createQuery(JPAEndpoint.class);

		Root<JPAEndpoint> descriptor = query.from(JPAEndpoint.class);
		query.
			select(descriptor).
			where(cb.equal(descriptor.get("resourceId"),Key.newInstance(id))).
			distinct(true);
		List<JPAEndpoint> results = entityManager().createQuery(query).getResultList();
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	@Override
	public void add(Endpoint endpoint) {
		entityManager().persist(endpoint);
	}

}
