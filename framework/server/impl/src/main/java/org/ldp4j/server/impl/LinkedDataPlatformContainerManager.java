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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.server.Format;
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.LinkedDataPlatformServer;
import org.ldp4j.server.core.ILinkedDataPlatformContainer;
import org.ldp4j.server.core.InitializationException;
import org.ldp4j.server.core.InvalidResourceContentsException;
import org.ldp4j.server.impl.ContainerHelper.DescriptionOptions;
import org.ldp4j.server.impl.ContainerHelper.DescriptionOptionsBuilder;
import org.ldp4j.server.impl.LinkedDataPlatformRegistry.IControlledContainer;
import org.ldp4j.server.impl.ResourceStateRegistry.ResourceState;
import org.ldp4j.server.sdk.StringContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * A pluggable <b><i>Linked Data Platform Container</i> Manager</b>
 * implementation that provides support for the <b>LDP4j Linked Data
 * Platform Server SPI</b>.<br />
 * 
 * <i>Linked Data Platform Container</i> implementations should be registered
 * using the standard <a href=
 * "http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html">JSE 6
 * Service Provider mechanism</a>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.server.core.ILinkedDataPlatformContainer
 */
@Path("/containers")
@Singleton
public class LinkedDataPlatformContainerManager implements ILinkedDataPlatformContainerManager {

	/**
	 * Internal logger.
	 */
	private static final Logger LOGGER=LoggerFactory.getLogger(LinkedDataPlatformContainerManager.class);
	
	private static class ResourceLocatorHelper {
		
		private final IResourceLocator locator;
		private final UriInfo context;
	
		public ResourceLocatorHelper(IResourceLocator locator, UriInfo context) {
			this.locator = locator;
			this.context = context;
		}
		
		public URI createRelativeResourcePath(String containerId, String resourceId) {
			return locator.createResourceLocation(containerId, resourceId);
		}
		
		public URI createAbsoluteResourcePath(String containerId, String resourceId) {
			return locator.createResourceLocation(context, containerId, resourceId);
		}
		
		@SuppressWarnings("unused")
		public URI createRelativeResourcePath(String containerId) {
			return locator.createContainerLocation(containerId);
		}
		
		public URI createAbsoluteContainerPath(String containerId) {
			return locator.createContainerLocation(context, containerId);
		}
	}

	private final Component component;

	private IResourceLocator locator;

	private ResourceLocatorHelper helper;

	/**
	 * Dump the stack trace of an exception to a String.
	 * 
	 * @param e
	 *            The exception that is to be dumped.
	 * @return A String that contains the stack trace dump.
	 */
	private String getFailure(Throwable e) {
		return Throwables.getStackTraceAsString(e);
	}

	private synchronized ResourceLocatorHelper getHelper() {
		if(helper==null) {
			helper = new ResourceLocatorHelper(getResourceLocator(),getUriInfo());
		}
		return helper;
	}



	/**
	 * Create a new Linked Data Container Manager.
	 */
	public LinkedDataPlatformContainerManager() {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Starting Linked Data Platform Container Manager (%X)",hashCode()));
		}
		this.component = new Component();
	}

	/**
	 * {@inheritDoc}
	 */
	public final UriInfo getUriInfo() {
		return component.getUriInfo();
	}

	/**
	 * {@inheritDoc}
	 */
	@Context
	public final synchronized void setUriInfo(UriInfo uriInfo) {
		component.setUriInfo(uriInfo);
		helper=null;
	}

	public synchronized IResourceLocator getResourceLocator() {
		if(locator==null) {
			// TODO: Externalize configuration
			this.locator=new ResourceLocator();
		}
		return locator;
	}

	public synchronized void setResourceLocator(IResourceLocator locator) {
		if(locator==null) {
			throw new IllegalArgumentException("Object 'locator' cannot be null");
		}
		this.locator = locator;
		this.helper=null;
	}

	/**
	 * For the time being, the current implementation does not check that the
	 * body contains a valid RDFS(S) TURTLE serialization. SPI implementors must
	 * check it. <br />
	 * 
	 * Also, there is not content-negotiation. All response entities are
	 * represented as {@code text/plain}.
	 * 
	 * @param containerId
	 *            {@inheritDoc}
	 * @param body
	 *            {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public Response createResource(String containerId, String body, String contentType) {
		ResourceLocatorHelper sessionHelper = getHelper();
		
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Requested resource creation to container '%s'...",containerId));
			LOGGER.debug(String.format("- Entity-Type: %s",contentType));
			LOGGER.debug(String.format("- Entity.....: %n%s",body));
		}
		
		ILinkedDataPlatformContainer container = LinkedDataPlatformServer.getRegistry().getContainer(containerId);
		if(container==null) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Container '%s' does not exist.",containerId));
			}
			return 
				Response.
					status(Status.NOT_FOUND).
					type(MediaType.TEXT_PLAIN).
					entity(String.format("Container '%s' does not exist.",containerId)).
					build();
		}
		try {
			if(container instanceof IControlledContainer) {
				IControlledContainer controller=(IControlledContainer)container;
				if(controller.isInitializable()) {
					if(!controller.initialize(LinkedDataPlatformServer.getManager())) {
						if(LOGGER.isInfoEnabled()) {
							LOGGER.info(String.format("Container '%s' initialized.",containerId));
						}
					}
				}
			}
			
			String resourceId=container.createResource(new StringContent(body), Format.fromMime(contentType));
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Resource '%s' created by container '%s'.",resourceId,containerId));
			}
			
			String location = sessionHelper.createAbsoluteResourcePath(containerId,resourceId).toString();
			URL resourceLocation = new URL(location);

			// TODO: Right now the etag only takes into account the input representation, not what it is stored
			EntityTag etag = EntityTagHelper.createTag(body, null);
			ResourceState status = ResourceStateRegistry.createResourceState(containerId, resourceId, resourceLocation, etag);
			if(LOGGER.isInfoEnabled()) {
				status.readLock().lock();
				try {
					LOGGER.info(String.format("Resource '%s' published at '%s' (%s, %s).",resourceId,status.getLocation(),status.getEntityTag(),status.getLastModified()));
				} finally {
					status.readLock().unlock();
				}
			}
			
			URI resourceUri = sessionHelper.createRelativeResourcePath(containerId,resourceId);
			return 
				Response.
					created(resourceUri).
					type(MediaType.TEXT_PLAIN).
					entity(resourceLocation.toString()).
					header("Link",LinkUtil.create("http://www.w3.org/ns/ldp#DirectContainer","type")).
					build();
		} catch (InitializationException e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format("Container '%s' could not be initialized. Full stacktrace follows.",containerId),e);
			}
			return 
				Response.
					serverError().
					type(MediaType.TEXT_PLAIN).
					entity(getFailure(e)).
					header("Link",LinkUtil.create("http://www.w3.org/ns/ldp#DirectContainer","type")).
					build();
		} catch (InvalidResourceContentsException e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format("Resource handler for container '%s' cannot create a resource with the specified contents: %s%nFull stacktrace follows.",containerId,body),e);
			}
			return 
				Response.
					status(Status.CONFLICT.getStatusCode()).
					type(MediaType.TEXT_PLAIN).
					entity(getFailure(e)).
					header("Link",LinkUtil.create("http://www.w3.org/ns/ldp#DirectContainer","type")).
					header("Link",LinkUtil.create(getUriInfo().getRequestUri().toString().concat("?describedBy"),"describedBy")).
					build();
		} catch (LinkedDataPlatformException e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format("Container '%s' could not handle resource creation. Full stacktrace follows.",containerId),e);
			}
			return 
				Response.
					serverError().
					type(MediaType.TEXT_PLAIN).
					entity(getFailure(e)).
					header("Link",LinkUtil.create("http://www.w3.org/ns/ldp#DirectContainer","type")).
					build();
		} catch (MalformedURLException e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format("Could not create resource location (%s). Full stacktrace follows.",containerId),e);
			}
			return 
				Response.
					serverError().
					type(MediaType.TEXT_PLAIN).
					entity(getFailure(e)).
					header("Link",LinkUtil.create("http://www.w3.org/ns/ldp#DirectContainer","type")).
					build();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response getContainerIdentifiers() {
		StringBuilder builder=new StringBuilder();
		builder.append("Containers: ").append(LinkedDataPlatformServer.getRegistry().getContainerIds().toString());
		return Response.ok(builder.toString()).type(MediaType.TEXT_PLAIN).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response search(UriInfo uriInfo, String containerId, String acceptHeader) {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Requested container '%s' search...",containerId));
			LOGGER.debug(String.format("- Accept: %s",acceptHeader));
		}
		
		ILinkedDataPlatformContainer container = LinkedDataPlatformServer.getRegistry().getContainer(containerId);
		if(container==null) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Container '%s' does not exist.",containerId));
			}
			return 
				Response.
					status(Status.NOT_FOUND).
					type(MediaType.TEXT_PLAIN).
					entity(String.format("Container '%s' does not exist.",containerId)).
					build();
		}
		Format acceptFormat = Format.fromMime(acceptHeader);
		try {
			if(container instanceof IControlledContainer) {
				IControlledContainer controller=(IControlledContainer)container;
				if(controller.isInitializable()) {
					if(!controller.initialize(LinkedDataPlatformServer.getManager())) {
						if(LOGGER.isInfoEnabled()) {
							LOGGER.info(String.format("Container '%s' initialized.",containerId));
						}
					}
				}
			}
			MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

			DescriptionOptionsBuilder builder = 
				ContainerHelper.
					newBuilder().
						withFormat(acceptFormat);

			if(!queryParameters.containsKey("non-member-properties")) {
				builder.includeMembers();
				if(!queryParameters.containsKey("member-properties")) {
					builder.includeSummary();
				} 
			}

			DescriptionOptions options = builder.build();
			if(LOGGER.isInfoEnabled()) {
				LOGGER.debug(String.format("- Include members: %s",options.includeMembers()));
				LOGGER.debug(String.format("- Include summary: %s",options.includeSummary()));
			}
			
			Map<String, URI> members=new HashMap<String,URI>();
			if(options.includeMembers()) {
				Collection<ResourceState> status=ResourceStateRegistry.getContainerStatus(containerId);
				for(ResourceState resourceState:status) {
					resourceState.readLock().lock();
					try {
						if(!resourceState.isDeleted()) {
							members.put(resourceState.getResourceId(),resourceState.getLocation().toURI());
						}
					} catch (URISyntaxException e) {
						if(LOGGER.isErrorEnabled()) {
							LOGGER.error("A URL should always be transformed to a URI. Full stacktrace follows.",e);
						}
						throw new AssertionError("A URL should always be transformed to a URI ("+e.getMessage()+")");
					} finally {
						resourceState.readLock().unlock();
					}
				}
			}

			String representation = 
				new ContainerHelper(container,getHelper().createAbsoluteContainerPath(containerId),members).
					getRepresentation(options);
			return 
				Response.
					ok().
					type(acceptFormat.getMime()).
					entity(representation).
					build();
		} catch (InitializationException e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format("Container '%s' could not be initialized. Full stacktrace follows.",containerId),e);
			}
			return 
				Response.
					serverError().
					type(MediaType.TEXT_PLAIN).
					entity(getFailure(e)).
					build();
		} catch (LinkedDataPlatformException e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format("Container '%s' could not handle search. Full stacktrace follows.",containerId),e);
			}
			return 
				Response.
					serverError().
					type(MediaType.TEXT_PLAIN).
					entity(getFailure(e)).
					build();
		} catch (IOException e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format("Container '%s' could not handle search. Full stacktrace follows.",containerId),e);
			}
			return 
				Response.
					serverError().
					type(MediaType.TEXT_PLAIN).
					entity(getFailure(e)).
					build();
		}
	}

}