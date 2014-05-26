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
package org.ldp4j.server.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.UriBuilder;

import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.spi.IResourceManager;

public final class ResourceManager implements IResourceManager {

	private static class ResourceKey {
		
		private final String containerId;
		private final String resourceId;
		
		public ResourceKey(String containerId, String resourceId) {
			this.containerId = containerId;
			this.resourceId = resourceId;
		}
	
		
		/**
		 * @return the containerId
		 */
		public String getContainerId() {
			return containerId;
		}
	
		/**
		 * @return the resourceId
		 */
		public String getResourceId() {
			return resourceId;
		}
	
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 131;
			int result = 29;
			result = prime * result
					+ ((containerId == null) ? 0 : containerId.hashCode());
			result = prime * result
					+ ((resourceId == null) ? 0 : resourceId.hashCode());
			return result;
		}
	
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ResourceManager.ResourceKey)) {
				return false;
			}
			ResourceManager.ResourceKey other = (ResourceManager.ResourceKey) obj;
			if (containerId == null) {
				if (other.containerId != null) {
					return false;
				}
			} else if (!containerId.equals(other.containerId)) {
				return false;
			}
			if (resourceId == null) {
				if (other.resourceId != null) {
					return false;
				}
			} else if (!resourceId.equals(other.resourceId)) {
				return false;
			}
			return true;
		}
		
	}

	private final Map<ResourceManager.ResourceKey, URL> registry=new HashMap<ResourceManager.ResourceKey, URL>();
	private final Map<String,ResourceManager.ResourceKey> reverseRegistry=new HashMap<String,ResourceManager.ResourceKey>();

	private final ReadWriteLock lock=new ReentrantReadWriteLock();
	
	private String createRelativeResource(String containerId, String resourceId) {
		return "resources/"+containerId+"/"+resourceId;
	}

	private void addLocation(ResourceManager.ResourceKey key, URL location) {
		lock.writeLock().lock();
		try {
			registry.put(key, location);
			reverseRegistry.put(location.toString(), key);
		} finally {
			lock.writeLock().unlock();
		}
	}

	private ResourceManager.ResourceKey resolveLocation(URL location) {
		lock.readLock().lock();
		try {
			return reverseRegistry.get(location.toString());
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public URL registerResource(String containerId, String resourceId) {
		if(containerId==null) {
			throw new IllegalArgumentException("Object 'containerId' cannot be null");
		}
		String tContainerId=containerId.trim();
		if(tContainerId.isEmpty()) {
			throw new IllegalArgumentException("Object 'containerId' cannot be null");
		}
		if(resourceId==null) {
			throw new IllegalArgumentException("Object 'resourceId' cannot be empty");
		}
		String tResourceId=resourceId.trim();
		if(tResourceId.isEmpty()) {
			throw new IllegalArgumentException("Object 'resourceId' cannot be empty");
		}
		UriBuilder uriBuilder = UriInfoProvider.getUriInfo().getBaseUriBuilder();
		
		ResourceManager.ResourceKey key=new ResourceKey(tContainerId,tResourceId);
		try {
			URL location = uriBuilder.path(createRelativeResource(containerId, resourceId)).build().toURL();
			addLocation(key, location);
			return location;
		} catch (MalformedURLException e) {
			// TODO: Improve logging here
			throw new AssertionError("Shouldn't fail: "+e.getMessage());
		}
	}

	@Override
	public void deregisterResource(String containerId, String resourceId) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void publishResource(URL location, IContent content, Format format) throws LinkedDataPlatformException {
		try {
			if(location==null) {
				throw new IllegalArgumentException("Object 'location' cannot be null");
			}
			if(content==null) {
				throw new IllegalArgumentException("Object 'content' cannot be null");
			}
			if(format==null) {
				throw new IllegalArgumentException("Object 'format' cannot be null");
			}
			ResourceManager.ResourceKey key = resolveLocation(location);
			if(key==null) {
				throw new IllegalArgumentException(String.format("Unknown location '%s'",location));
			}
			EntityTag etag = EntityTagHelper.createTag(content.serialize(String.class), format);
			ResourceStateRegistry.createResourceState(key.getContainerId(), key.getResourceId(), location, etag);
		} catch (IOException e) {
			throw new LinkedDataPlatformException("Cannot process contents", e);
		}
		
	}

	@Override
	public void unpublishResource(URL location) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}