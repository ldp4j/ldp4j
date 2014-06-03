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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.testing.stubs;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.core.ILinkedDataPlatformContainer;
import org.ldp4j.server.sdk.IndividualFormattedContent;
import org.ldp4j.server.sdk.IndividualFormattedContent.Individual;

public class DeletableContainer implements ILinkedDataPlatformContainer {

	public static final String CONTAINER_ID = "DeletableContainer";

	public DeletableContainer() {
		ResourceManagerController.getInstance().attachResourceManager(CONTAINER_ID);
	}
	
	@Override
	public String getContainerId() {
		return CONTAINER_ID;
	}

	@Override
	public String createResource(IContent content, Format format) throws LinkedDataPlatformException {
		try {
			return getResourceManager().createResource(content, format);
		} catch (IOException e) {
			throw new LinkedDataPlatformException("Could not read content",e);
		}
	}
	
	@Override
	public IContent getSummary(final Collection<String> resources, final Format format) throws LinkedDataPlatformException {
		List<Individual> individuals=getResourceManager().getSummary(resources, format);
		return new IndividualFormattedContent(format,individuals.toArray(new Individual[]{}));
	}

	ResourceManager getResourceManager() {
		return ResourceManagerController.getInstance().getResourceManager(CONTAINER_ID);
	}


}