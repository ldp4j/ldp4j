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

import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.core.ILinkedDataPlatformContainer;
import org.ldp4j.server.core.InvalidResourceContentsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailingContainer implements ILinkedDataPlatformContainer {

	public static final String INVALID_CONTENT = "INVALID_CONTENT";

	private static final Logger LOGGER=LoggerFactory.getLogger(FailingContainer.class);
	
	public static final String CONTAINER_ID = "FailingContainer";

	@Override
	public String getContainerId() {
		return CONTAINER_ID;
	}

	@Override
	public String createResource(IContent content, Format format) throws LinkedDataPlatformException {
		try {
			String body = content.serialize(String.class);
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Failing creation of resource for content '%s'...",body));
			}
			if(body.equals(INVALID_CONTENT)) {
				throw new InvalidResourceContentsException(String.format("Bad contents: resource creation for content '%s' failed",body));
			} else {
				throw new LinkedDataPlatformException(String.format("Unexpected failure: resource creation for content '%s' failed",body));
			}
		} catch (IOException e) {
			throw new LinkedDataPlatformException("Could not process contents",e);
		}
	}

	@Override
	public IContent getSummary(Collection<String> resources, Format format) throws LinkedDataPlatformException {
		throw new LinkedDataPlatformException("Could create summary in format '"+format+"' for resources "+resources);
	}

}