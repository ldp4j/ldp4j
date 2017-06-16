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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-api:0.2.2
 *   Bundle      : ldp4j-application-kernel-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.constraints;

import java.io.Serializable;

import org.ldp4j.application.kernel.resource.ResourceId;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

public final class ConstraintReportId implements Serializable {

	private static final long serialVersionUID = -1692929146077368881L;

	/**
	 * Not final to enable its usage in JPA
	 */
	private ResourceId resourceId;

	/**
	 * Not final to enable its usage in JPA
	 */
	private String failureId;

	private ConstraintReportId() {
		// JPA Friendly
	}

	private ConstraintReportId(ResourceId resourceId, String failureId) {
		this();
		this.resourceId = resourceId;
		this.failureId = failureId;
	}

	public ResourceId resourceId() {
		return this.resourceId;
	}

	public String failureId() {
		return this.failureId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.resourceId,this.failureId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof ConstraintReportId) {
			ConstraintReportId that=(ConstraintReportId)obj;
			result=
				Objects.equal(this.resourceId,that.resourceId) &&
				Objects.equal(this.failureId,that.failureId);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("resourceId",this.resourceId).
					add("failureId",this.failureId).
					toString();
	}

	public static ConstraintReportId create(ResourceId resourceId, String failureId) {
		checkNotNull(resourceId,"Resource identifier cannot be null");
		checkNotNull(failureId,"Failure identifier cannot be null");
		return new ConstraintReportId(resourceId,failureId);
	}

}
