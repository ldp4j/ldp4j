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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.frontend;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.lifecycle.ApplicationLifecycleListener;
import org.ldp4j.application.lifecycle.ApplicationState;
import org.ldp4j.server.api.Endpoint;
import org.ldp4j.server.api.EndpointFactory;
import org.ldp4j.server.api.EndpointRegistry;
import org.ldp4j.server.commands.Command;
import org.ldp4j.server.commands.CommandExecutionException;
import org.ldp4j.server.commands.CommandProcessingException;
import org.ldp4j.server.commands.CommandProcessingService;
import org.ldp4j.server.commands.InvalidCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JAXRSFrontend {

	private static final Logger LOGGER=LoggerFactory.getLogger(JAXRSFrontend.class);
	
	private static final String ENDPOINT_PATH_PARAM = "path";
	private static final String ENDPOINT_PATH = "/legacy/{"+ENDPOINT_PATH_PARAM+":.*}";

	private final EndpointRegistry registry;

	private final CommandProcessingService service;

	protected ApplicationState state;

	public JAXRSFrontend(EndpointRegistry registry, CommandProcessingService service) {
		this.registry=registry;
		this.service=service;
		ApplicationContext.
			currentContext().
				registerApplicationLifecycleListener( 
					new ApplicationLifecycleListener() {
						@Override
						public void applicationStateChanged(ApplicationState newState) {
							JAXRSFrontend.this.state=newState;
							LOGGER.debug("{} :: Application state changed to '{}'",this,newState);
							if(ApplicationState.SHUTDOWN.equals(newState)) {
								ApplicationContext.
									currentContext().
										deregisterApplicationLifecycleListener(this);
							}
						}
					}
			);
	}
	
	private URI normalizePath(String path) {
		String tPath=path;
		if(tPath==null) {
			tPath="";
		} else {
			tPath = tPath.trim();
		}
		return URI.create(tPath);
	}

	private Endpoint findEndpoint(String path) {
		URI resourceUri = normalizePath(path);
		Endpoint result=null;
		switch(this.state) {
		case AVAILABLE:
			result=registry.findEndpoint(resourceUri);
			break;
		case SHUTDOWN:
			result=EndpointFactory.unavailable(resourceUri);
			break;
		case UNAVAILABLE:
			result=EndpointFactory.serverError(resourceUri);
			break;
		case UNDEFINED:
			result=EndpointFactory.serverError(resourceUri);
		}
		return result;
	}

	@POST
	@Consumes(Command.MIME)
	@Produces("text/plain")
	@Path("/action")
	public Response processCommand(
		@Context UriInfo uriInfo, 
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		Response response=null;
		try {
			service.process(entity);
			response=
				Response.
					ok("Command executed succesfully ").
					type(MediaType.TEXT_PLAIN).
					build();
		} catch (InvalidCommandException e) {
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Invalid command",e);
			}
			response=
				Response.
					status(Status.BAD_REQUEST).
					type(MediaType.TEXT_PLAIN).
					entity("Invalid command: \n"+e.getRawCommand()).
					build();
		} catch (CommandExecutionException e) {
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Could not execute command",e);
			}
			response=
				Response.
					status(Status.CONFLICT).
					type(MediaType.TEXT_PLAIN).
					entity("Could not execute command: \n"+e.getMessage()).
					build();
		} catch (CommandProcessingException e) {
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Could not process command",e);
			}
			response=
				Response.
					serverError().
					type(MediaType.TEXT_PLAIN).
					entity("Could not process command: \n"+e.getMessage()).
					build();
		}
		return response;
	}

	/**
	 * LDP 1.0 - 4.2.6.1 LDP servers must support the HTTP HEAD method.
	 * @param uriInfo
	 * @param path
	 * @param headers
	 * @param request
	 * @return
	 */
	@HEAD
	@Path(ENDPOINT_PATH)
	public Response head(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request) {
		return 
			findEndpoint(path).
				getHeader(uriInfo,headers,request);
	}

	@GET
	@Path(ENDPOINT_PATH)
	public Response get(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request) {
		return 
			findEndpoint(path).
				retrieveResource(uriInfo,headers,request);
	}

	@PUT
	@Path(ENDPOINT_PATH)
	public Response put(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		return 
			findEndpoint(path).
				updateResource(uriInfo,headers,request,entity);
	}
	
	@POST
	@Path(ENDPOINT_PATH)
	public Response post(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		return 
			findEndpoint(path).
				createResource(uriInfo,headers,request,entity);
	}

	@DELETE
	@Path(ENDPOINT_PATH)
	public Response delete(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		return 
			findEndpoint(path).
				deleteResource(uriInfo,headers,request);
	}

	@PATCH
	@Path(ENDPOINT_PATH)
	public Response patch(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		return 
			findEndpoint(path).
				patchResource(uriInfo,headers,request,entity);
	}

	/**
	 * LDP 1.0 - 4.2.8.1 : "LDP servers must support the HTTP OPTIONS method."
	 * HTTP/1.1 - 9.2 : The current support for the OPTIONS method
	 * <b>discards</b> request entity-bodies and does not return a response
	 * body.
	 * 
	 * @param uriInfo
	 * @param path
	 * @param headers
	 * @param request
	 * @return
	 */
	@OPTIONS
	@Path(ENDPOINT_PATH)
	public Response options(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request) {
		return 
			findEndpoint(path).
				getOptions(uriInfo,headers,request);
	}
}
