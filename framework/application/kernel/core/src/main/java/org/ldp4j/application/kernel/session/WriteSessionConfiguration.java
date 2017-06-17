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
package org.ldp4j.application.kernel.session;

import java.net.URI;
import java.util.Date;

import org.ldp4j.application.kernel.resource.Resource;

import com.google.common.base.MoreObjects;

public final class WriteSessionConfiguration {

	private String path;
	private URI indirectId;
	private Resource target;
	private Date lastModified;

	private WriteSessionConfiguration() {
		this.lastModified=new Date();
	}

	private void setPath(String path) {
		this.path=path;
	}

	private void setIndirectId(URI indirectId) {
		this.indirectId = indirectId;
	}

	private void setTarget(Resource resource) {
		this.target=resource;
	}

	private void setLastModified(Date lastModified) {
		this.lastModified=lastModified;
	}

	public Resource getTarget() {
		return this.target;
	}

	public String getPath() {
		return this.path;
	}

	public URI getIndirectId() {
		return this.indirectId;
	}

	public Date getLastModified() {
		return this.lastModified;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper("WriteSessionConfiguration").
					omitNullValues().
					add("target",this.target).
					add("path",this.path).
					add("indirectId",this.indirectId).
					toString();
	}

	public static WriteSessionConfiguration.WriteSessionConfigurationBuilder builder() {
		return new WriteSessionConfigurationBuilder();
	}

	public static final class WriteSessionConfigurationBuilder {

		private final WriteSessionConfiguration configuration;

		private WriteSessionConfigurationBuilder() {
			this.configuration = new WriteSessionConfiguration();
		}

		public WriteSessionConfigurationBuilder withTarget(Resource resource) {
			this.configuration.setTarget(resource);
			return this;
		}

		public WriteSessionConfigurationBuilder withIndirectId(URI indirectId) {
			this.configuration.setIndirectId(indirectId);
			return this;
		}

		public WriteSessionConfigurationBuilder withPath(String path) {
			this.configuration.setPath(path);
			return this;
		}

		public WriteSessionConfigurationBuilder withLastModified(Date lastModified) {
			this.configuration.setLastModified(lastModified);
			return this;
		}

		public WriteSessionConfiguration build() {
			return this.configuration;
		}

	}

}