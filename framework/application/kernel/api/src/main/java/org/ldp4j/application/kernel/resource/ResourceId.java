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
package org.ldp4j.application.kernel.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.kernel.template.ResourceTemplate;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class ResourceId implements Serializable {

	private static final long serialVersionUID = -8083258917826432416L;

	/**
	 * Not final to enable its usage in JPA
	 */
	private String templateId;

	/**
	 * Not final to enable its usage in JPA
	 */
	private Name<?> name;

	private ResourceId() {
		// JPA FRIENDLY
	}

	private ResourceId(Name<?> name, String templateId) {
		this.name = name;
		this.templateId = templateId;
	}

	public Name<?> name() {
		return this.name;
	}

	public String templateId() {
		return this.templateId;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.name,this.templateId);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj!=null && obj.getClass()==this.getClass()) {
			ResourceId that=(ResourceId)obj;
			result=
				Objects.equal(this.name,that.name) &&
				Objects.equal(this.templateId,that.templateId);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("name", this.name).
					add("templateId", this.templateId).
					toString();
	}

	public static ResourceId createId(Name<?> name, ResourceTemplate template) {
		checkNotNull(name,"Resource name cannot be null");
		checkNotNull(template,"Template cannot be null");
		return createId(name,template.id());
	}

	public static ResourceId createId(Name<?> name, String templateId) {
		checkNotNull(name,"Resource name cannot be null");
		checkNotNull(templateId,"Template identifier cannot be null");
		return new ResourceId(name,templateId);
	}

}