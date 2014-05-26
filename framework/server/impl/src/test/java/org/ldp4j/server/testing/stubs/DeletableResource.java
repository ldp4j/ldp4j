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
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;
import org.ldp4j.server.IResource;
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.LinkedDataPlatformServer;
import org.ldp4j.server.core.Deletable;
import org.ldp4j.server.core.DeletionException;
import org.ldp4j.server.core.DeletionResult;
import org.ldp4j.server.core.ILinkedDataPlatformResourceHandler;

public class DeletableResource implements ILinkedDataPlatformResourceHandler, Deletable {

	private DeletableContainer container;

	private synchronized DeletableContainer getContainer() {
		if(container==null) {
			container=LinkedDataPlatformServer.getRegistry().getContainer(getContainerId(), DeletableContainer.class);
			if(container==null) {
				throw new IllegalStateException("Could not find container");
			}
		}
		return container;
	}
	
	@Override
	public String getContainerId() {
		return DeletableContainer.CONTAINER_ID;
	}

	@Override
	public IResource getResource(final String id) throws LinkedDataPlatformException {
		final String resource=getContainer().getResource(id);
		return new IResource() {
			@Override
			public URL getIdentity() {
				throw new UnsupportedOperationException("Method not implemented yet");
			}

			@Override
			public IContent getContent(Format format) throws IOException {
				return new IContent() {
					@Override
					public <S> S serialize(Class<S> clazz) throws IOException {
						S result=null;
						if(clazz.isAssignableFrom(String.class)) {
							result=clazz.cast(resource);
						} else if(clazz.isAssignableFrom(InputStream.class)) {
							result=clazz.cast(IOUtils.toInputStream(resource));
						} else {
							throw new IOException(String.format("Could not serialize content to '%s'",clazz.getCanonicalName()));
						}
						return result;
					}
				};
			}
			
		};
	}

	@Override
	public Collection<String> getResourceList() throws LinkedDataPlatformException {
		return getContainer().getResourceList();
	}

	@Override
	public IResource updateResource(String resourceId, final IContent content, Format format) throws LinkedDataPlatformException  {
		try {
			if(!getContainer().updateResource(resourceId,content.serialize(String.class))) {
				return null;
			}
			return getResource(resourceId);
		} catch (IOException e) {
			throw new LinkedDataPlatformException("Could not process contents",e);
		}
	}
	
	@Override
	public DeletionResult delete(String resourceId) throws DeletionException {
		return getContainer().deleteResource(resourceId);
	}

}
