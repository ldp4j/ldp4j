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

import org.ldp4j.application.kernel.resource.Member;
import org.ldp4j.application.kernel.resource.ResourceId;

import com.google.common.base.MoreObjects;

final class JPAMember implements Member {

	/**
	 * Persistent key required by JPA
	 */
	private long primaryKey;

	/**
	 * Surrogate object to guarantee DB portability
	 */
	private Key memberId;

	/**
	 * Surrogate object to guarantee DB portability
	 */
	private Key containerId;

	/**
	 * Not final to enable its usage in JPA
	 */
	private long number;

	private JPAMember() {
		// JPA Friendly
	}

	JPAMember(Key containerId, Key memberId, long number) {
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
		return this.containerId.resourceId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceId memberId() {
		return this.memberId.resourceId();
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