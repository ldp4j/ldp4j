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
package org.ldp4j.application.kernel.engine;

import org.ldp4j.application.engine.context.Capabilities;


final class MutableCapabilities implements Capabilities {

	private boolean queryable;
	private boolean modifiable;
	private boolean deletable;
	private boolean patchable;
	private boolean factory;

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
		return deletable;
	}

	@Override
	public boolean isPatchable() {
		return patchable;
	}

	@Override
	public boolean isFactory() {
		return factory;
	}

	void setQueryable(boolean queryable) {
		this.queryable = queryable;
	}

	void setModifiable(boolean modifiable) {
		this.modifiable=modifiable;
	}

	void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	void setPatchable(boolean patchable) {
		this.patchable = patchable;
	}

	void setFactory(boolean factory) {
		this.factory = factory;
	}

}
