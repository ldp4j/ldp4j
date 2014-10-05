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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.lifecycle.ApplicationLifecycleListener;
import org.ldp4j.application.lifecycle.ApplicationState;
import org.ldp4j.server.controller.EndpointController;
import org.ldp4j.server.controller.EndpointControllerFactory;
import org.ldp4j.server.controller.OperationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
public class ServerFrontend {

	private final class LocalApplicationLifecycleListener implements ApplicationLifecycleListener {

		@Override
		public void applicationStateChanged(ApplicationState newState) {
			LOGGER.debug("{} :: Application state changed to '{}'",this,newState);
			switch(newState) {
			case AVAILABLE:
				break;
			case SHUTDOWN:
				applicationContext().
					deregisterApplicationLifecycleListener(ServerFrontend.this.appLifecyleListener);
				break;
			case UNAVAILABLE:
				break;
			case UNDEFINED:
				break;
			}
		}

		private ApplicationContext applicationContext() {
			return ServerFrontend.this.applicationContext;
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ServerFrontend.class);
	
	private static final String ENDPOINT_PATH_PARAM = "path";
	private static final String ENDPOINT_PATH = "/{"+ENDPOINT_PATH_PARAM+":.*}";

	private final LocalApplicationLifecycleListener appLifecyleListener;

	private EndpointControllerFactory endpointControllerfactory;

	private final ApplicationContext applicationContext;

	public ServerFrontend() {
		this.appLifecyleListener=new LocalApplicationLifecycleListener();
		this.applicationContext=ApplicationContext.currentContext();
		this.applicationContext.registerApplicationLifecycleListener(this.appLifecyleListener);
		this.endpointControllerfactory=
			EndpointControllerFactory.
				newInstance(this.applicationContext);
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
		EndpointController controller=this.endpointControllerfactory.createController(path);
		OperationContext context = 
			OperationContext.
				builder(this.applicationContext,controller.endpoint()).
					withUriInfo(uriInfo).
					withHeaders(headers).
					withRequest(request).
					build();
		return controller.options(context);
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
		EndpointController controller=this.endpointControllerfactory.createController(path);
		OperationContext context = 
			OperationContext.
				builder(this.applicationContext,controller.endpoint()).
					withUriInfo(uriInfo).
					withHeaders(headers).
					withRequest(request).
					build();
		return controller.head(context);
	}

	@GET
	@Path(ENDPOINT_PATH)
	public Response get(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request) {
		EndpointController controller=this.endpointControllerfactory.createController(path);
		OperationContext context = 
			OperationContext.
				builder(this.applicationContext,controller.endpoint()).
					withUriInfo(uriInfo).
					withHeaders(headers).
					withRequest(request).
					build();
		return controller.getResource(context);
	}

	@PUT
	@Path(ENDPOINT_PATH)
	public Response put(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		EndpointController controller=this.endpointControllerfactory.createController(path);
		OperationContext context = 
			OperationContext.
				builder(this.applicationContext,controller.endpoint()).
					withUriInfo(uriInfo).
					withHeaders(headers).
					withRequest(request).
					withEntity(entity).
					build();
		return controller.modifyResource(context);
	}
	
	@POST
	@Path(ENDPOINT_PATH)
	public Response post(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		EndpointController controller=this.endpointControllerfactory.createController(path);
		OperationContext context = 
			OperationContext.
				builder(this.applicationContext,controller.endpoint()).
					withUriInfo(uriInfo).
					withHeaders(headers).
					withRequest(request).
					withEntity(entity).
					build();
		return controller.createResource(context);
	}

	@DELETE
	@Path(ENDPOINT_PATH)
	public Response delete(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request) {
		EndpointController controller=this.endpointControllerfactory.createController(path);
		OperationContext context = 
			OperationContext.
				builder(this.applicationContext,controller.endpoint()).
					withUriInfo(uriInfo).
					withHeaders(headers).
					withRequest(request).
					build();
		return controller.deleteResource(context);
	}

	@PATCH
	@Path(ENDPOINT_PATH)
	public Response patch(
		@Context UriInfo uriInfo, 
		@PathParam(ENDPOINT_PATH_PARAM) String path, 
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		EndpointController controller=this.endpointControllerfactory.createController(path);
		OperationContext context = 
			OperationContext.
				builder(this.applicationContext,controller.endpoint()).
					withUriInfo(uriInfo).
					withHeaders(headers).
					withRequest(request).
					withEntity(entity).
					build();
		return controller.patchResource(context);
	}

}
