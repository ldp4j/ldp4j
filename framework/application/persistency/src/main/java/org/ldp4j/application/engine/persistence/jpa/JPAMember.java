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

import org.ldp4j.application.engine.resource.Member;
import org.ldp4j.application.engine.resource.ResourceId;

import com.google.common.base.MoreObjects;

final class JPAMember implements Member {

	/**
	 * Persistent key required by JPA
	 */
	private long primaryKey;

	/**
	 * Not final to enable its usage in JPA
	 */
	private ResourceId memberId;

	/**
	 * Not final to enable its usage in JPA
	 */
	private ResourceId containerId;

	/**
	 * Not final to enable its usage in JPA
	 */
	private long number;

	private JPAMember() {
		// JPA Friendly
	}

	JPAMember(ResourceId containerId, ResourceId memberId, long number) {
		this();
		this.memberId = memberId;
		this.containerId = containerId;
		this.number = number;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long number() {
		return this.number;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceId containerId() {
		return this.containerId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceId memberId() {
		return this.memberId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("primaryKey",this.primaryKey).
					add("number",this.number).
					add("containerId",this.containerId).
					add("memberId", this.memberId).
					toString();
	}

}