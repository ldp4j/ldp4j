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

import org.ldp4j.application.kernel.constraints.ConstraintReport;
import org.ldp4j.application.kernel.constraints.ConstraintReportId;
import org.ldp4j.application.kernel.constraints.ConstraintReportRepository;
import org.ldp4j.application.kernel.resource.Resource;

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
		EntityManager em = entityManager();

		CriteriaBuilder cb=em.getCriteriaBuilder();

		CriteriaQuery<JPAConstraintReport> query =
				cb.createQuery(JPAConstraintReport.class);

		Root<JPAConstraintReport> descriptor = query.from(JPAConstraintReport.class);
		query.
			select(descriptor).
			where(cb.equal(descriptor.get("id"),id)).
			distinct(true);
		List<JPAConstraintReport> results = em.createQuery(query).getResultList();
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
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

		CriteriaQuery<JPAConstraintReport> query =
				cb.createQuery(JPAConstraintReport.class);

		Root<JPAConstraintReport> descriptor = query.from(JPAConstraintReport.class);
		query.
			select(descriptor).
			where(cb.equal(descriptor.get("id").get("resourceId"),resource.id())).
			distinct(true);
		List<JPAConstraintReport> results = em.createQuery(query).getResultList();
		for(JPAConstraintReport report:results){
			em.remove(report);
		}
	}

}
