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

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.ws.rs.core.EntityTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ResourceStateRegistry {

	/**
	 * Internal logger.
	 */
	private static final Logger LOGGER=LoggerFactory.getLogger(ResourceStateRegistry.class);
	
	public static class ResourceState {

		/**
		 * Internal logger.
		 */
		private static final Logger LOGGER=LoggerFactory.getLogger(ResourceState.class);

		private static class LoggingWriteLock extends WriteLock {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2316674765875656949L;

			private final String containerId;
			private final String resourceId;

			public LoggingWriteLock(ReentrantReadWriteLock lock, String containerId, String resourceId) {
				super(lock);
				this.containerId= containerId;
				this.resourceId = resourceId;
			}
			
			/* (non-Javadoc)
			 * @see java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock#lock()
			 */
			@Override
			public void lock() {
				super.lock();
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug(String.format("State of resource '%s' in container '%s' write-locked by thread %x",resourceId,containerId,Thread.currentThread().getId()));
				}
				
			}

			/* (non-Javadoc)
			 * @see java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock#unlock()
			 */
			@Override
			public void unlock() {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug(String.format("State of resource '%s' in container '%s' write-unlocked by thread %x",resourceId,containerId,Thread.currentThread().getId()));
				}
				super.unlock();
			}

		}

		private static class LoggingReadLock extends ReadLock {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2316674765875656949L;

			private final String containerId;
			private final String resourceId;

			private final ReentrantReadWriteLock lock;

			private final ConcurrentMap<Long,AtomicLong> readingThreads=new ConcurrentHashMap<Long, AtomicLong>();
			
			public LoggingReadLock(ReentrantReadWriteLock lock, String containerId, String resourceId) {
				super(lock);
				this.lock = lock;
				this.containerId= containerId;
				this.resourceId = resourceId;
			}
			
			private void log(String action, Thread currentThread) {
				LOGGER.debug(String.format("State of resource '%s' in container '%s' read-%s by thread '%s' (%X)",resourceId,containerId,action,currentThread.getName(),currentThread.getId()));
			}
			
			
			/* (non-Javadoc)
			 * @see java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock#lock()
			 */
			@Override
			public void lock() {
				super.lock();
				Thread currentThread = Thread.currentThread();
				Long threadId = currentThread.getId();
				readingThreads.putIfAbsent(threadId, new AtomicLong());
				if(readingThreads.get(threadId).getAndIncrement()==0) {
					if(LOGGER.isDebugEnabled()) {
						if(!lock.isWriteLockedByCurrentThread()) {
							log("lock",currentThread);
						}
					}
				}
			}

			/* (non-Javadoc)
			 * @see java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock#unlock()
			 */
			@Override
			public void unlock() {
				Thread currentThread = Thread.currentThread();
				Long threadId = currentThread.getId();
				AtomicLong counter = readingThreads.get(threadId);
				if(counter.decrementAndGet()==0) {
					readingThreads.remove(threadId);
					if(LOGGER.isDebugEnabled()) {
						if(!lock.isWriteLockedByCurrentThread()) {
							log("unlock",currentThread);
						}
					}
				}
				super.unlock();
			}

		}

		public static class LoggingReentrantReadWriteLock extends ReentrantReadWriteLock {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2188070076315788962L;
			private final LoggingWriteLock loggingWriteLock;
			private final LoggingReadLock loggingReadLock;

			public LoggingReentrantReadWriteLock(String containerId, String resourceId) {
				loggingWriteLock = new LoggingWriteLock(this,containerId,resourceId);
				loggingReadLock = new LoggingReadLock(this,containerId,resourceId);
			}

			/* (non-Javadoc)
			 * @see java.util.concurrent.locks.ReentrantReadWriteLock#writeLock()
			 */
			@Override
			public WriteLock writeLock() {
				return loggingWriteLock;
			}

			/* (non-Javadoc)
			 * @see java.util.concurrent.locks.ReentrantReadWriteLock#readLock()
			 */
			@Override
			public ReadLock readLock() {
				return loggingReadLock;
			}
			
		}
		

		private final String containerId;
		private final String resourceId;
		private EntityTag etag;
		private Date lastModified;
	
		private final ReadWriteLock lock;
		private final URL location;
		private boolean deleted;
		
		private ResourceState(String containerId, String resourceId, URL location, EntityTag etag, Date lastModified) {
			if(containerId==null) {
				throw new IllegalArgumentException("Object 'containerId' cannot be null");
			}
			if(resourceId==null) {
				throw new IllegalArgumentException("Object 'resourceId' cannot be null");
			}
			if(location==null) {
				throw new IllegalArgumentException("Object 'location' cannot be null");
			}
			if(etag==null) {
				throw new IllegalArgumentException("Object 'etag' cannot be null");
			}
			if(lastModified==null) {
				throw new IllegalArgumentException("Object 'date' cannot be null");
			}
	
			this.containerId = containerId.trim();
			this.resourceId = resourceId;
			this.location = location;
			this.lock=new LoggingReentrantReadWriteLock(this.containerId,this.resourceId);
			this.etag = etag;
			this.lastModified = lastModified;
	
			if(this.containerId.isEmpty()) {
				throw new IllegalArgumentException("String 'containerId' cannot be empty");
			}
			if(this.containerId.isEmpty()) {
				throw new IllegalArgumentException("String 'resourceId' cannot be empty");
			}
		}
	
		public Lock readLock() {
			return lock.readLock();
		}
	
		public Lock writeLock() {
			return lock.readLock();
		}
		
		/**
		 * @return the etag
		 */
		public EntityTag getEntityTag() {
			readLock().lock();
			try {
				return etag;
			} finally {
				readLock().unlock();
			}
		}
	
		/**
		 * @param etag the etag to set
		 */
		public void setEntityTag(EntityTag etag) {
			if(etag==null) {
				throw new IllegalArgumentException("Object 'etag' cannot be null");
			}
			writeLock().lock();
			try {
				this.etag=etag;
			} finally {
				writeLock().unlock();
			}
		}
	
		/**
		 * @return the lastModified
		 */
		public Date getLastModified() {
			readLock().lock();
			try {
				return lastModified;
			} finally {
				readLock().unlock();
			}
		}
	
		/**
		 * @param lastModified the lastModified to set
		 */
		public void setLastModified(Date lastModified) {
			if(lastModified==null) {
				throw new IllegalArgumentException("Object 'lastModified' cannot be null");
			}
			writeLock().lock();
			try {
				this.lastModified = new Date(lastModified.getTime());
			} finally {
				writeLock().unlock();
			}
		}
	
		/**
		 * @return the containerId
		 */
		public String getContainerId() {
			readLock().lock();
			try {
				return containerId;
			} finally {
				readLock().unlock();
			}
		}
	
		/**
		 * @return the resourceId
		 */
		public String getResourceId() {
			readLock().lock();
			try {
				return resourceId;
			} finally {
				readLock().unlock();
			}
		}

		public URL getLocation() {
			return location;
		}

		public void deleted() {
			this.deleted=true;
		}
		
		public boolean isDeleted() {
			return deleted;
		}

	}
	
	private static final Map<String,Map<String,ResourceState>> STATUS_REGISTRY=new HashMap<String, Map<String,ResourceState>>();
	
	private ResourceStateRegistry() {
	}
	
	public static synchronized ResourceState createResourceState(String containerId, String resourceId, URL location, EntityTag etag) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Requested state creation for container '%s' and resource '%s' (etag: %s)...",containerId,resourceId,etag));
		}
		Map<String, ResourceState> containerMap = STATUS_REGISTRY.get(containerId);
		if(containerMap==null) {
			containerMap=new HashMap<String, ResourceState>();
			STATUS_REGISTRY.put(containerId, containerMap);
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Created registry entry for container '%s'",containerId));
			}
		}
		ResourceState resourceStatus = containerMap.get(resourceId);
		if(resourceStatus!=null) {
			if(LOGGER.isErrorEnabled()) {
				LOGGER.error(String.format("An existing state for resource '%s' exists in container '%s'",resourceId,containerId));
			}
			throw new IllegalArgumentException(String.format("A resource state for resource '%s' in container '%s' already exists",resourceId,containerId));
		}
		Date lastModified = new Date();
		resourceStatus=new ResourceState(containerId, resourceId, location, etag, lastModified);
		containerMap.put(resourceId,resourceStatus);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Created state for resource '%s' in container '%s' (%s,%s)",resourceId,containerId,etag,lastModified));
		}
		return resourceStatus;
	}

	public static synchronized ResourceState getResourceState(String containerId, String resourceId) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Requested state retrieval for container '%s' and resource '%s'...",containerId,resourceId));
		}
		ResourceState result=null;
		Map<String, ResourceState> containerMap = STATUS_REGISTRY.get(containerId);
		if(containerMap==null) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("No registry entry found for container '%s'",containerId));
			}
		} else {
			result = containerMap.get(resourceId);
			if(result==null) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug(String.format("No container '%s' entry found for resource '%s'",containerId,resourceId));
				}
			}
		}
		return result;
	}

	public static synchronized Collection<ResourceState> getContainerStatus(String containerId) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Requested status retrieval for container '%s'...",containerId));
		}
		Collection<ResourceState> result=null;
		Map<String, ResourceState> containerMap = STATUS_REGISTRY.get(containerId);
		if(containerMap==null) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("No registry entry found for container '%s'",containerId));
			}
			result=Collections.emptyList();
		} else {
			result=containerMap.values();
		}
		return result;
	}

}