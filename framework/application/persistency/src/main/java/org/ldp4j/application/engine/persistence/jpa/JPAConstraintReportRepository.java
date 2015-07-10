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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-persistency:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-persistency-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.persistence.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.ldp4j.application.engine.constraints.ConstraintReport;
import org.ldp4j.application.engine.constraints.ConstraintReportId;
import org.ldp4j.application.engine.constraints.ConstraintReportRepository;
import org.ldp4j.application.engine.resource.Resource;

final class JPAConstraintReportRepository implements ConstraintReportRepository {

	private final EntityManagerProvider provider;

	JPAConstraintReportRepository(EntityManagerProvider provider) {
		this.provider = provider;
	}

	private EntityManager entityManager() {
		return this.provider.entityManager();
	}

	@Override
	public ConstraintReport constraintReportOfId(ConstraintReportId id) {
		return entityManager().find(ConstraintReport.class,id);
	}

	@Override
	public void add(ConstraintReport report) {
		entityManager().persist(report);
	}

	@Override
	public void remove(ConstraintReport report) {
		entityManager().remove(report);
	}

	@Override
	public void removeByResource(Resource resource) {
		EntityManager em = entityManager();

		CriteriaBuilder cb=em.getCriteriaBuilder();

		CriteriaQuery<ConstraintReport> query =
				cb.createQuery(ConstraintReport.class);

		Root<ConstraintReport> descriptor = query.from(ConstraintReport.class);
		query.
			select(descriptor).
			where(cb.equal(descriptor.get("resourceId"),resource.id())).
			distinct(true);
		List<ConstraintReport> results = em.createQuery(query).getResultList();
		for(ConstraintReport report:results){
			em.remove(report);
		}
	}

}
