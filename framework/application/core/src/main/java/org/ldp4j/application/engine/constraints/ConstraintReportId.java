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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.constraints;

import org.ldp4j.application.engine.resource.ResourceId;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

public final class ConstraintReportId {

	private final ResourceId resourceId;
	private final String constraintsId;

	private ConstraintReportId(ResourceId resourceId, String constraintsId) {
		this.resourceId = resourceId;
		this.constraintsId = constraintsId;
	}

	public ResourceId resourceId() {
		return resourceId;
	}

	public String constraintsId() {
		return this.constraintsId;
	}

	public static ConstraintReportId create(ResourceId resourceId, String constraintsId) {
		checkNotNull(resourceId,"Resource identifier cannot be null");
		checkNotNull(constraintsId,"Constraints identifier cannot be null");
		return new ConstraintReportId(resourceId,constraintsId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.resourceId,this.constraintsId);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof ConstraintReportId) {
			ConstraintReportId that=(ConstraintReportId)obj;
			result=
				Objects.equal(this.resourceId,that.resourceId) &&
				Objects.equal(this.constraintsId,that.constraintsId);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					add("resourceId",this.resourceId).
					add("constraintsId",this.constraintsId).
					toString();
	}

}
