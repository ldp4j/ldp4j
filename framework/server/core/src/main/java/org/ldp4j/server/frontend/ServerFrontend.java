/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.frontend;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.application.engine.ApplicationEngine;
import org.ldp4j.application.engine.context.ApplicationContext;
import org.ldp4j.application.engine.context.HttpRequest.HttpMethod;
import org.ldp4j.application.engine.lifecycle.ApplicationEngineLifecycleListener;
import org.ldp4j.application.engine.lifecycle.ApplicationEngineState;
import org.ldp4j.server.controller.EndpointControllerFactory;
import org.ldp4j.server.controller.OperationContext;
import org.ldp4j.server.controller.OperationContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(ServerFrontend.PATH)
public final class ServerFrontend {

	private final class LocalApplicationEngineLifecycleListener extends ApplicationEngineLifecycleListener {

		private final AtomicReference<ApplicationEngineState> currentState;

		private LocalApplicationEngineLifecycleListener() {
			this.currentState=new AtomicReference<ApplicationEngineState>();
		}

		boolean available() {
			return ApplicationEngineState.STARTED.equals(this.currentState.get());
		}

		@Override
		protected void onApplicationEngineShutdown() {
			ApplicationEngine.deregisterLifecycleListener(this);
		}

		@Override
		protected void onStateChange(ApplicationEngineState newState) {
			this.currentState.set(newState);
			LOGGER.debug("LDP4j Application Engine state changed to '{}'",newState);
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ServerFrontend.class);

	private static final String ENDPOINT_PATH_PARAM = "path";
	private static final String ENDPOINT_PATH       = "/{"+ENDPOINT_PATH_PARAM+":.*}";

	public static final String LDP4J_APPLICATION_CONTEXT = "ldp4jApplicationContext";
	public static final String PATH                      = "/";

	private final LocalApplicationEngineLifecycleListener lifecyleListener;

	@Context
	private ServletContext servletContext;

	public ServerFrontend() {
		this.lifecyleListener=new LocalApplicationEngineLifecycleListener();
		ApplicationEngine.registerLifecycleListener(this.lifecyleListener);
	}

	private void checkApplicationEngineAvailable() {
		if(!this.lifecyleListener.available()) {
			Response failure =
				Response.
					serverError().
					type(MediaType.TEXT_PLAIN).
					language(Locale.ENGLISH).
					entity("Application engine is not available").
					build();
			throw new WebApplicationException(failure);
		}
	}

	private ApplicationContext currentContext() {
		ApplicationContext currentContext = (ApplicationContext)this.servletContext.getAttribute(LDP4J_APPLICATION_CONTEXT);
		if(currentContext==null) {
			Response failure =
				Response.
					serverError().
					type(MediaType.TEXT_PLAIN).
					language(Locale.ENGLISH).
					entity("Application is not available").
					build();
			throw new WebApplicationException(failure);
		}
		return currentContext;
	}

	private OperationContextBuilder newOperationBuilder(HttpMethod operation) {
		checkApplicationEngineAvailable();
		return
			new OperationContextBuilder().
				withApplicationContext(currentContext()).
				withOperation(operation);
	}

	/**
	 * LDP 1.0 - 4.2.8.1 : "LDP servers must support the HTTP OPTIONS method."
	 * HTTP/1.1 - 9.2 : The current support for the OPTIONS method
	 * <b>discards</b> request entity-bodies and does not return a response
	 * body.
	 *
	 * @param uriInfo the full URL details of the resource
	 * @param path the path of the resource
	 * @param headers the headers included in the request
	 * @param request the request itself
	 * @return an LDP compliant response to the OPTIONS request
	 */
	@OPTIONS
	@Path(ENDPOINT_PATH)
	public Response options(
		@Context UriInfo uriInfo,
		@PathParam(ENDPOINT_PATH_PARAM) String path,
		@Context HttpHeaders headers,
		@Context Request request) {
		OperationContext context =
			newOperationBuilder(HttpMethod.OPTIONS).
				withEndpointPath(path).
				withEndpointPath(path).
				withUriInfo(uriInfo).
				withHeaders(headers).
				withRequest(request).
				build();
		return
			EndpointControllerFactory.
				newController().
					options(context);
	}

	/**
	 * LDP 1.0 - 4.2.6.1 LDP servers must support the HTTP HEAD method.
	 * @param uriInfo the full URL details of the resource
	 * @param path the path of the resource
	 * @param headers the headers included in the request
	 * @param request the request itself
	 * @return an LDP compliant response to the HEAD request
	 */
	@HEAD
	@Path(ENDPOINT_PATH)
	public Response head(
		@Context UriInfo uriInfo,
		@PathParam(ENDPOINT_PATH_PARAM) String path,
		@Context HttpHeaders headers,
		@Context Request request) {
		OperationContext context =
			newOperationBuilder(HttpMethod.HEAD).
				withEndpointPath(path).
				withUriInfo(uriInfo).
				withHeaders(headers).
				withRequest(request).
				build();
		return
			EndpointControllerFactory.
				newController().
					head(context);
	}

	@GET
	@Path(ENDPOINT_PATH)
	public Response get(
		@Context UriInfo uriInfo,
		@PathParam(ENDPOINT_PATH_PARAM) String path,
		@Context HttpHeaders headers,
		@Context Request request) {
		OperationContext context =
			newOperationBuilder(HttpMethod.GET).
				withEndpointPath(path).
				withUriInfo(uriInfo).
				withHeaders(headers).
				withRequest(request).
				build();
		return
			EndpointControllerFactory.
				newController().
					getResource(context);
	}

	@PUT
	@Path(ENDPOINT_PATH)
	public Response put(
		@Context UriInfo uriInfo,
		@PathParam(ENDPOINT_PATH_PARAM) String path,
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		OperationContext context =
			newOperationBuilder(HttpMethod.PUT).
				withEndpointPath(path).
				withUriInfo(uriInfo).
				withHeaders(headers).
				withRequest(request).
				withEntity(entity).
				build();
		return
			EndpointControllerFactory.
				newController().
					modifyResource(context);
	}

	@POST
	@Path(ENDPOINT_PATH)
	public Response post(
		@Context UriInfo uriInfo,
		@PathParam(ENDPOINT_PATH_PARAM) String path,
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		OperationContext context =
			newOperationBuilder(HttpMethod.POST).
				withEndpointPath(path).
				withUriInfo(uriInfo).
				withHeaders(headers).
				withRequest(request).
				withEntity(entity).
				build();
		return
			EndpointControllerFactory.
				newController().
					createResource(context);
	}

	@DELETE
	@Path(ENDPOINT_PATH)
	public Response delete(
		@Context UriInfo uriInfo,
		@PathParam(ENDPOINT_PATH_PARAM) String path,
		@Context HttpHeaders headers,
		@Context Request request) {
		OperationContext context =
			newOperationBuilder(HttpMethod.DELETE).
				withEndpointPath(path).
				withUriInfo(uriInfo).
				withHeaders(headers).
				withRequest(request).
				build();
		return
			EndpointControllerFactory.
				newController().
					deleteResource(context);
	}

	@PATCH
	@Path(ENDPOINT_PATH)
	public Response patch(
		@Context UriInfo uriInfo,
		@PathParam(ENDPOINT_PATH_PARAM) String path,
		@Context HttpHeaders headers,
		@Context Request request,
		String entity) {
		OperationContext context =
			newOperationBuilder(HttpMethod.PATCH).
				withEndpointPath(path).
				withUriInfo(uriInfo).
				withHeaders(headers).
				withRequest(request).
				withEntity(entity).
				build();
		return
			EndpointControllerFactory.
				newController().
					patchResource(context);
	}

}
