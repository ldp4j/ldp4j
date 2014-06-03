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
import org.ldp4j.server.IResource;
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.LinkedDataPlatformServer;
import org.ldp4j.server.core.Delete;
import org.ldp4j.server.core.DeletionException;
import org.ldp4j.server.core.DeletionResult;
import org.ldp4j.server.core.ILinkedDataPlatformResourceHandler;
import org.ldp4j.server.core.InvalidResourceContentsException;

public class WorkingResource implements ILinkedDataPlatformResourceHandler {

	public static final String INVALID_CONTENT = "INVALID_CONTENT";
	
	@Override
	public String getContainerId() {
		return WorkingContainer.CONTAINER_ID;
	}

	@Override
	public IResource getResource(final String id) throws LinkedDataPlatformException {
		return new ResourceImpl(getResourceManager().retrieveResource(id));
	}

	@Override
	public Collection<String> getResourceList() throws LinkedDataPlatformException {
		return getResourceManager().getResources();
	}

	@Override
	public IResource updateResource(String resourceId, final IContent content, Format format) throws LinkedDataPlatformException  {
		try {
			String body = content.serialize(String.class);
			if(body.equals(INVALID_CONTENT)) {
				throw new InvalidResourceContentsException(String.format("Bad contents: resource creation for content '%s' failed",body));
			} else {
				if(!getResourceManager().updateResource(resourceId,body)) {
					return null;
				}
				return getResource(resourceId);
			}
		} catch (IOException e) {
			throw new LinkedDataPlatformException("Could not process contents",e);
		}
	}
	
	@Delete
	public DeletionResult deleteResource(String resourceId) throws DeletionException {
		DeletionResult result;
		if(getResourceManager().deleteResource(resourceId)) {
			result=DeletionResult.newBuilder().enacted(true).withMessage(String.format("Resource '%s' succesfully deleted",resourceId)).build();
		} else {
			throw new DeletionException(String.format("Resource '%s' not found",resourceId));
		}
		return result;
	}

	private ResourceManager getResourceManager() {
		return LinkedDataPlatformServer.getRegistry().getContainer(getContainerId(), WorkingContainer.class).getResourceManager();
	}

}