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
package org.ldp4j.server.api;

import java.net.URI;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.ldp4j.server.api.blueprint.ComponentRegistry;
import org.ldp4j.server.resources.Resource;


public final class EndpointFactory {
	
	private abstract static class FixedBehaviourEndpoint implements Endpoint {

		private final URI path;

		protected FixedBehaviourEndpoint(URI path) {
			this.path = path;
		}

		@Override
		public URI path() {
			return path;
		}

		@Override
		public Response retrieveResource(
				UriInfo uriInfo, 
				HttpHeaders headers,
				Request request) {
			return getDefaultResponse();
		}

		@Override
		public Response updateResource(
				UriInfo uriInfo, 
				HttpHeaders headers,
				Request request, 
				String entity) {
			return getDefaultResponse();
		}

		@Override
		public Response createResource(
				UriInfo uriInfo, 
				HttpHeaders headers,
				Request request, 
				String entity) {
			return getDefaultResponse();
		}

		@Override
		public Response getHeader(
				UriInfo uriInfo, 
				HttpHeaders headers,
				Request request) {
			return getDefaultResponse();
		}

		@Override
		public Response patchResource(
				UriInfo uriInfo, 
				HttpHeaders headers,
				Request request, 
				String entity) {
			return getDefaultResponse();
		}

		@Override
		public Response getOptions(
				UriInfo uriInfo, 
				HttpHeaders headers,
				Request request) {
			return getDefaultResponse();
		}

		@Override
		public Response deleteResource(
				UriInfo uriInfo, 
				HttpHeaders headers,
				Request request) {
			return getDefaultResponse();
		}

		protected abstract Response getDefaultResponse();
		
	}

	private static final class NotFoundEndpoint extends EndpointFactory.FixedBehaviourEndpoint {

		private NotFoundEndpoint(URI path) {
			super(path);
		}

		@Override
		protected Response getDefaultResponse() {
			return Response.status(Status.NOT_FOUND).build();
		}
		
	}
	
	private static final class GoneEndpoint extends EndpointFactory.FixedBehaviourEndpoint {

		private GoneEndpoint(URI path) {
			super(path);
		}

		@Override
		protected Response getDefaultResponse() {
			return Response.status(Status.GONE).build();
		}
		
	}
	
	private static final class UnavailableEndpoint extends EndpointFactory.FixedBehaviourEndpoint {

		private UnavailableEndpoint(URI path) {
			super(path);
		}

		@Override
		protected Response getDefaultResponse() {
			return Response.status(Status.SERVICE_UNAVAILABLE).build();
		}
		
	}
	
	private static final class ServerErrorEndpoint extends EndpointFactory.FixedBehaviourEndpoint {

		private ServerErrorEndpoint(URI path) {
			super(path);
		}

		@Override
		protected Response getDefaultResponse() {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
	}

	private EndpointFactory() {
	}
	
	static Endpoint goneEndpoint(URI path) {
		return new GoneEndpoint(path);
	}
	
	static Endpoint notFoundEndpoint(URI path) {
		return new NotFoundEndpoint(path);
	}
	
	public static Endpoint unavailable(URI path) {
		return new UnavailableEndpoint(path);
	}

	public static Endpoint serverError(URI path) {
		return new ServerErrorEndpoint(path);
	}
	
	public static <R extends Resource> Endpoint newEndpoint(URI path, Configuration<R> config, ComponentRegistry registry) {
		return new BaseEndpoint<R>(path,config,registry);
	}

}