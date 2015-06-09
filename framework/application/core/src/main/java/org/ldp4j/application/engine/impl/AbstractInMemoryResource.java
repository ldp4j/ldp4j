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
package org.ldp4j.application.engine.impl;

import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.engine.template.ResourceTemplate;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

abstract class AbstractInMemoryResource {

	private InMemoryPersistencyManager persistencyManager;

	final void setPersistencyManager(InMemoryPersistencyManager persistencyManager) {
		this.persistencyManager=persistencyManager;
	}

	final InMemoryPersistencyManager getPersistencyManager() {
		if(persistencyManager==null) {
			throw new IllegalStateException("Resource factory service not been initialized yet");
		}
		return persistencyManager;
	}

	final ResourceTemplate getTemplate(ResourceId resourceId) {
		return getPersistencyManager().templateOfId(resourceId.templateId());
	}

	protected ToStringHelper stringHelper() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues();
	}

}
