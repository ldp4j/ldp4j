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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.template;

import java.net.URI;

import org.ldp4j.application.kernel.template.AttachedTemplate;
import org.ldp4j.application.kernel.template.ResourceTemplate;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

final class MutableAttachedTemplate implements AttachedTemplate {

	private final String id;
	private final String path;
	private final ResourceTemplate template;
	private URI predicate;

	MutableAttachedTemplate(String id, ResourceTemplate attachment, String path) {
		this.id = id;
		this.template = attachment;
		this.path = path;
	}

	void setPredicate(URI predicate) {
		this.predicate=predicate;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String path() {
		return this.path;
	}

	@Override
	public ResourceTemplate template() {
		return this.template;
	}

	@Override
	public Optional<URI> predicate() {
		return Optional.fromNullable(this.predicate);
	}

	@Override
	public int hashCode() {
		return
			Objects.hashCode(id,path)+
			System.identityHashCode(template);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj!=null && obj.getClass()==this.getClass()) {
			MutableAttachedTemplate that = (MutableAttachedTemplate) obj;
			result=
				Objects.equal(this.id, that.id) &&
				Objects.equal(this.path, that.path) &&
				this.template==that.template;
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("id", this.id).
					add("path", this.path).
					add("template.id()", this.template.id()).
					add("predicate", predicate).
					toString();
	}

}