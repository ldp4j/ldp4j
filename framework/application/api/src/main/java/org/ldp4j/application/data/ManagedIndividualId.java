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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.io.Serializable;
import java.net.URI;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

public final class ManagedIndividualId implements Serializable {

	private static final long serialVersionUID = 3320976326537867325L;

	private final String managerId;
	private final Name<?> name;
	private final URI indirectId;

	private ManagedIndividualId(Name<?> name, String managerId, URI indirectId) {
		this.name = name;
		this.managerId = managerId;
		this.indirectId = indirectId;
	}

	public Name<?> name() {
		return this.name;
	}

	public String managerId() {
		return this.managerId;
	}

	public URI indirectId() {
		return this.indirectId;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.name,this.managerId,this.indirectId);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj!=null && obj.getClass()==this.getClass()) {
			ManagedIndividualId that=(ManagedIndividualId)obj;
			result=
				Objects.equal(this.name, that.name) &&
				Objects.equal(this.managerId, that.managerId) &&
				Objects.equal(this.indirectId, that.indirectId);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("name", this.name).
					add("managerId",this.managerId).
					add("indirectId", this.indirectId).
					toString();
	}

	public static ManagedIndividualId createId(Name<?> name, String managerId) {
		checkNotNull(name,"Resource name cannot be null");
		checkNotNull(managerId,"Manager identifier cannot be null");
		return new ManagedIndividualId(name,managerId,null);
	}

	public static ManagedIndividualId createId(URI indirectId, ManagedIndividualId parent) {
		checkNotNull(indirectId,"Indirect identifier cannot be null");
		checkNotNull(parent,"Parent identifier cannot be null");
		return new ManagedIndividualId(parent.name,parent.managerId,indirectId);
	}

}