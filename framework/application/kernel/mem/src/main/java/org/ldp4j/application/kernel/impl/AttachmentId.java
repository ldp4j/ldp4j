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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-mem:0.2.2
 *   Bundle      : ldp4j-application-kernel-mem-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import org.ldp4j.application.kernel.resource.ResourceId;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

final class AttachmentId {

	private final String id;
	private final ResourceId resourceId;

	private AttachmentId(String id, ResourceId resourceId) {
		this.resourceId = resourceId;
		this.id = id;
	}

	public ResourceId resourceId() {
		return resourceId;
	}

	public String id() {
		return id;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.id,this.resourceId);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj!=null && obj.getClass()==this.getClass()) {
			AttachmentId that=(AttachmentId)obj;
			result=
				Objects.equal(this.id,that.id) &&
				Objects.equal(this.resourceId,that.resourceId);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("id", this.id).
					add("resourceId", this.resourceId).
					toString();
	}

	public static AttachmentId createId(String attachmentId, ResourceId resourceId) {
		checkNotNull(attachmentId,"Template identifier cannot be null");
		checkNotNull(resourceId,"Resource identifier cannot be null");
		return new AttachmentId(attachmentId,resourceId);
	}

}