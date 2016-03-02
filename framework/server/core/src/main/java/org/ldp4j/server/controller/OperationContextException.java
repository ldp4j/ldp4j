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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;

import org.ldp4j.application.engine.context.Capabilities;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.context.PublicResource;

public class OperationContextException extends RuntimeException {

	private static final class SerializableCapabilities implements Capabilities, Serializable {

		private static final long serialVersionUID = 3550471347041282150L;

		private final boolean queryable;
		private final boolean modifiable;
		private final boolean deletable;
		private final boolean patchable;
		private final boolean factory;

		SerializableCapabilities(Capabilities capabilities) {
			this.queryable=capabilities.isQueryable();
			this.modifiable=capabilities.isModifiable();
			this.deletable=capabilities.isDeletable();
			this.patchable=capabilities.isPatchable();
			this.factory=capabilities.isFactory();
		}

		@Override
		public boolean isQueryable() {
			return this.queryable;
		}

		@Override
		public boolean isModifiable() {
			return this.modifiable;
		}

		@Override
		public boolean isDeletable() {
			return this.deletable;
		}

		@Override
		public boolean isPatchable() {
			return this.patchable;
		}

		@Override
		public boolean isFactory() {
			return this.factory;
		}

	}

	private static final String DEFAULT_ERROR_MESSAGE = "Unexpected operation context failure";

	private static final long serialVersionUID = 5924666884556832771L;

	private final URI location;
	private final Date lastModified;
	private final EntityTag entityTag;
	private final Class<? extends PublicResource> clazz;
	private final SerializableCapabilities capabilities;

	public OperationContextException(String message, Throwable cause, OperationContext context) {
		super(message, cause);
		PublicResource resource=context.resource();
		this.capabilities=new SerializableCapabilities(resource.capabilities());
		this.location = context.base().resolve(resource.path());
		this.lastModified=resource.lastModified();
		this.entityTag=resource.entityTag();
		this.clazz=resource.getClass();
	}

	public OperationContextException(OperationContext context) {
		this(DEFAULT_ERROR_MESSAGE,context);
	}

	public OperationContextException(String message, OperationContext context) {
		this(message,null,context);
	}

	public OperationContextException(Throwable cause, OperationContext context) {
		this(DEFAULT_ERROR_MESSAGE,cause,context);
	}

	public final Class<? extends PublicResource> resourceClass() {
		return this.clazz;
	}

	public final URI resourceLocation() {
		return this.location;
	}

	public final Capabilities resourceCapabilities() {
		return this.capabilities;
	}
	public final Date resourceLastModified() {
		return this.lastModified;
	}

	public final EntityTag resourceEntityTag() {
		return this.entityTag;
	}

}