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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.server.Format;
import org.ldp4j.server.IResource;
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.LinkedDataPlatformServer;
import org.ldp4j.server.core.ILinkedDataPlatformContainer;
import org.ldp4j.server.core.ILinkedDataPlatformResourceHandler;
import org.ldp4j.server.core.InvalidResourceContentsException;
import org.ldp4j.server.impl.LinkedDataPlatformRegistry.IControlledContainer;
import org.ldp4j.server.impl.ResourceStateRegistry.ResourceState;
import org.ldp4j.server.sdk.StringContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pluggable <b><i>Linked Data Platform Resource</i> Manager</b>
 * implementation that provides support for the <b>LDP4j Linked Data
 * Platform Server SPI</b>.<br />
 * 
 * <i>Linked Data Platform Resource Handler</i> implementations should be registered
 * using the standard <a href=
 * "http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html">JSE 6
 * Service Provider mechanism</a>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.server.core.ILinkedDataPlatformResourceHandler
 */
@Path("/resources")
@Singleton
public class LinkedDataPlatformResourceManager implements ILinkedDataPlatformResourceManager {

	/**
	 * Internal logger.
	 */
	private static final Logger LOGGER=LoggerFactory.getLogger(LinkedDataPlatformResourceManager.class);

	private final Component component;

	/**
	 * Dump the stack trace of an exception to a String.
	 * 
	 * @param e
	 *            The exception that is to be dumped.
	 * @return A String that contains the stack trace dump.
	 */
	private String getFailure(Throwable e) {
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		writer.flush();
		return writer.toString();
	}

	private Response createNotFoundResponse(String errorMessage) {
		return
			Response.
				status(Status.NOT_FOUND).
				type(MediaType.TEXT_PLAIN).
				entity(errorMessage).
				build();
	}

	private Response createResourceNotFoundResponse(String containerId, String resourceId) {
		String errorMessage=null;
		if(resourceId==null) {
			errorMessage=String.format("No resource handler for container '%s' exists",containerId);
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(errorMessage);
			}
		} else {
			errorMessage = String.format("No resource '%s' is registered for container '%s'",resourceId,containerId);
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(errorMessage);
			}
		}
		return createNotFoundResponse(errorMessage);
	}

	private Response createResourceHandlerNotFoundResponse(String containerId) {
		return createResourceNotFoundResponse(containerId,null);
	}

	private Response createInternalServerErrorResponse(String errorMessage) {
		return createInternalServerErrorResponse(errorMessage,null);
	}

	private Response createInternalServerErrorResponse(String errorMessage, Throwable e) {
		String body=null;
		if(e==null) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(errorMessage);
			}
			body=errorMessage;
		} else {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(errorMessage.concat(". Full stacktrace follows"),e);
			}
			body=getFailure(e);
		}
		Response result=null;
		result= 
			Response.
				status(Status.INTERNAL_SERVER_ERROR).
				type(MediaType.TEXT_PLAIN).
				entity(body).
				build();
		return result;
	}

	private Response createResourceUpdateErrorResponse(String errorMessage, ResourceState status, InvalidResourceContentsException e) {
		String body=null;
		if(e==null) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(errorMessage);
			}
			body=errorMessage;
		} else {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(errorMessage.concat(". Full stacktrace follows"),e);
			}
			body=getFailure(e);
		}
		Response result=null;
		result= 
			Response.
				status(Status.CONFLICT).
				type(MediaType.TEXT_PLAIN).
				lastModified(status.getLastModified()).
				tag(status.getEntityTag()).
				entity(body).
				header("Link",LinkUtil.create("http://www.w3.org/ns/ldp#Resource","type")).
				header("Link",LinkUtil.create(getUriInfo().getRequestUri().toString().concat("?describedBy"),"describedBy")).
				build();
		return result;
	}

	private Response isDeleted(ResourceState status) {
		Response result=null;
		if(status.isDeleted()) {
			String errorMessage = String.format("Resource '%s' for container '%s' was deleted",status.getResourceId(),status.getContainerId());
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(errorMessage);
			}
			result= 
				Response.
					status(Status.GONE).
					build();
		}
		return result;
	}

	private Response initializeContainer(String containerId) {
		Response response=null;
		ILinkedDataPlatformContainer container = LinkedDataPlatformServer.getRegistry().getContainer(containerId);
		if(container==null) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Container '%s' does not exist.",containerId));
			}
			response=
				Response.
					status(Status.NOT_FOUND).
					type(MediaType.TEXT_PLAIN).
					entity(String.format("Container '%s' does not exist.",containerId)).
					build();
		} else {
			try {
				if(LOGGER.isInfoEnabled()) {
					LOGGER.info(String.format("Checking if container '%s' is controllable...",containerId));
				}
				if(container instanceof IControlledContainer) {
					IControlledContainer controller=(IControlledContainer)container;
					if(controller.isInitializable()) {
						if(!controller.initialize(LinkedDataPlatformServer.getManager())) {
							if(LOGGER.isInfoEnabled()) {
								LOGGER.info(String.format("Container '%s' initialized.",containerId));
							}
						} else {
							if(LOGGER.isInfoEnabled()) {
								LOGGER.info(String.format("Container '%s' already initialized.",containerId));
							}
						}
					}
				} else {
					if(LOGGER.isInfoEnabled()) {
						LOGGER.info(String.format("Container '%s' is not controllable.",containerId));
					}
				}
			} catch (Exception e) {
				response=
					createInternalServerErrorResponse(String.format("Container '%s' could not be initialized",containerId), e);
			}
		}
		return response;
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
	public final void setUriInfo(UriInfo uriInfo) {
		component.setUriInfo(uriInfo);
	}

	/**
	 * Create a new Linked Data Platform Resource Manager.
	 */
	public LinkedDataPlatformResourceManager() {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Starting Linked Data Platform Resource Manager (%X)",hashCode()));
		}
		component=new Component();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response getResource(String containerId, String resourceId, String format) {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Requested resource '%s' retrieval for container '%s' using format '%s'...",resourceId,containerId,format));
		}

		// TODO: Document this behaviour in the JavaDoc
		ILinkedDataPlatformResourceHandler resourceHandler = LinkedDataPlatformServer.getRegistry().getResourceHandler(containerId);
		if(resourceHandler==null) {
			return createResourceHandlerNotFoundResponse(containerId);
		}

		Response response = initializeContainer(containerId);
		if(response!=null) {
			return response;
		}

		try {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Looking for resource '%s' in container '%s'...",resourceId, containerId));
			}

			ResourceState status = ResourceStateRegistry.getResourceState(containerId, resourceId);
			if(status==null) {
				return createResourceNotFoundResponse(containerId,resourceId);
			}

			status.readLock().lock();
			try {
				Response result = isDeleted(status);
				if(result!=null) {
					return result;
				}
				EntityTag etag=status.getEntityTag();
				Date lastModified=status.getLastModified();

				IResource resource=resourceHandler.getResource(resourceId);
				if(resource==null) {
					return createInternalServerErrorResponse(String.format("Resource '%s' is registered for container '%s', but the container fails to find it",resourceId,containerId));
				}

				if(LOGGER.isInfoEnabled()) {
					LOGGER.info(String.format("Resource '%s' found in container '%s' (%s, %s).",resourceId,containerId,etag,lastModified));
				}

				Format contentFormat=Format.fromMime(format);
				if(contentFormat==null) {
					contentFormat=Format.Turtle;
				}
				return 
					Response.
						ok(resource.getContent(contentFormat).serialize(String.class)).
						tag(etag).
						lastModified(lastModified).
						header("Link",LinkUtil.create("http://www.w3.org/ns/ldp#Resource","type")).
						type(contentFormat.getMime()).
						build();
			} finally {
				status.readLock().unlock();
			}

		} catch (Exception e) {
			return createInternalServerErrorResponse(String.format("Container '%s' could not handle resource '%s' retrieval",containerId,resourceId),e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response updateResource(String containerId, String resourceId, String body, String contentType) {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Requested resource '%s' update for container '%s'",resourceId,containerId));
			LOGGER.debug(String.format("- Content-Type: %s",contentType));
			LOGGER.debug(String.format("- Content.....: %n%s",body));
		}
		
		// TODO: Document this behaviour in the JavaDoc
		ILinkedDataPlatformResourceHandler resourceHandler = LinkedDataPlatformServer.getRegistry().getResourceHandler(containerId);
		if(resourceHandler==null) {
			return createResourceHandlerNotFoundResponse(containerId);
		}

		Response response = initializeContainer(containerId);
		if(response!=null) {
			return response;
		}

		try {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Looking for resource '%s' in container '%s'...",resourceId, containerId));
			}

			// TODO: Right now the etag only takes into account the input representation, not what it is stored
			ResourceState status = ResourceStateRegistry.getResourceState(containerId, resourceId);
			if(status==null) {
				return createResourceNotFoundResponse(containerId,resourceId);
			}

			status.writeLock().lock();
			try {
				Response result = isDeleted(status);
				if(result!=null) {
					return result;
				}

				if(LOGGER.isInfoEnabled()) {
					LOGGER.info(String.format("Updating resource '%s' in container '%s' (%s, %s)...",resourceId, containerId,status.getEntityTag(),status.getLastModified()));
				}

				IResource resource=resourceHandler.updateResource(resourceId,new StringContent(body),Format.fromMime(contentType));
				if(resource==null) {
					return createInternalServerErrorResponse(String.format("Resource handler for container '%s' returned a null resource after updating resource '%s'",containerId,resourceId));
				}

				EntityTag etag = EntityTagHelper.createTag(body, null);
				Date lastModified = new Date();
				status.setEntityTag(etag);
				status.setLastModified(lastModified);
				if(LOGGER.isInfoEnabled()) {
					LOGGER.info(String.format("Resource '%s' updated (%s, %s).",resourceId,etag,lastModified));
				}
				return 
						Response.
							noContent().
							tag(etag).
							lastModified(lastModified).
							header("Link",LinkUtil.create("http://www.w3.org/ns/ldp#Resource","type")).
							build();
			} catch (InvalidResourceContentsException e) {
				return createResourceUpdateErrorResponse(String.format("Resource handler for container '%s' cannot update resource '%s' with the specified contents:%s",containerId,resourceId,body), status, e);
			} catch (LinkedDataPlatformException e) {
				return createInternalServerErrorResponse(String.format("Resource handler for container '%s' could not handle resource '%s' update",containerId,resourceId), e);
			} finally {
				status.writeLock().unlock();
			}
		} catch (RuntimeException e) {
			return createInternalServerErrorResponse(String.format("Unexpected failure resource handler '%s' was updating resource '%s'",containerId,resourceId), e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response deleteResource(String containerId, String resourceId) {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Requested deletion of resource '%s' from container '%s'...",resourceId,containerId));
		}
	
		// TODO: Document this behaviour in the JavaDoc
		ILinkedDataPlatformResourceHandler resourceHandler = LinkedDataPlatformServer.getRegistry().getResourceHandler(containerId);
		if(resourceHandler==null) {
			return createResourceHandlerNotFoundResponse(containerId);
		}
	
		Response response = initializeContainer(containerId);
		if(response!=null) {
			return response;
		}
	
		try {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Looking for resource '%s' in container '%s'...",resourceId, containerId));
			}
	
			// TODO: Right now the etag only takes into account the input representation, not what it is stored
			ResourceState status = ResourceStateRegistry.getResourceState(containerId, resourceId);
			if(status==null) {
				response=createResourceNotFoundResponse(containerId,resourceId);
			} else {
				DeletionHelper helper=new DeletionHelper(resourceHandler);
				status.writeLock().lock();
				try {
					Response result = isDeleted(status);
					if(result!=null) {
						return result;
					}
					if(LOGGER.isInfoEnabled()) {
						LOGGER.info(String.format("Handling resource '%s' deletion to handler...",resourceId));
					}
					if(helper.isDeletionSupported()) {
						response=helper.delete(resourceId);
						status.deleted();
						if(LOGGER.isInfoEnabled()) {
							LOGGER.info(String.format("Resource '%s' deleted.",resourceId));
						}
					} else {
						response=
							Response.
								status(Status.FORBIDDEN).
								build();
					}
				} finally {
					status.writeLock().unlock();
				}
			}
		} catch (Exception e) {
			response=createInternalServerErrorResponse(String.format("Resource handler for container '%s' could not handle resource '%s' deletion",containerId,resourceId),e);
		}
		return response; 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response getResourceIdentifiers(String containerId) {
		StringBuilder builder=new StringBuilder();

		// TODO: Document this behaviour in the JavaDoc
		ILinkedDataPlatformResourceHandler resourceHandler = LinkedDataPlatformServer.getRegistry().getResourceHandler(containerId);
		if(resourceHandler==null) {
			return createResourceHandlerNotFoundResponse(containerId);
		}

		Response response = initializeContainer(containerId);
		if(response!=null) {
			return response;
		}

		try {
			builder.append("Container '").append(containerId).append("' resources: ").append(resourceHandler.getResourceList());
			response=Response.ok(builder.toString()).type(MediaType.TEXT_PLAIN).build();
		} catch (LinkedDataPlatformException e) {
			response=createInternalServerErrorResponse(String.format("Could not retrieve list of resources for container '%s'",containerId), e);
		}
		return response;
	}

}