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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.ApplicationExecutionException;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.endpoint.EndpointLifecycleListener;
import org.ldp4j.application.lifecycle.ApplicationLifecycleListener;
import org.ldp4j.application.lifecycle.ApplicationState;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.util.ConcurrentHashSet;
import org.ldp4j.server.api.Entity;
import org.ldp4j.server.api.ImmutableContext;
import org.ldp4j.server.api.spi.ContentTransformationException;
import org.ldp4j.server.api.utils.ProtocolUtils;
import org.ldp4j.server.api.utils.VariantUtils;
import org.ldp4j.server.resources.ResourceType;

import com.google.common.base.Throwables;

public class EndpointControllerFactory {
	
	private static abstract class AbstractEndpointController implements EndpointController {
		
		private final Endpoint endpoint;

		private AbstractEndpointController(Endpoint endpoint) {
			this.endpoint=endpoint;
		}
		
		public final Endpoint endpoint() {
			return this.endpoint;
		}
		
	}

	private static abstract class FixedResponseEndpointController extends AbstractEndpointController {
	
		private FixedResponseEndpointController(Endpoint endpoint) {
			super(endpoint);
		}
	
		protected abstract Response defaultResponse();
		
		@Override
		public final Response getResource(OperationContext context) {
			return defaultResponse();
		}
	
		@Override
		public final Response createResource(OperationContext context) {
			return defaultResponse();
		}
		
	}
	private final class ExistingEndpointController extends AbstractEndpointController {
	
		public ExistingEndpointController(Endpoint endpoint) {
			super(endpoint);
		}

		public Response getResource(OperationContext context) {
			return doGet(context, true);
		}

		protected Response doGet(OperationContext context, boolean includeEntity) {
			// 1. validate output expectations
			Variant variant=context.expectedVariant(supportedVariants());

			// 2. enforce valid state
			context.evaluatePreconditions(endpoint().entityTag(),endpoint().lastModified());

			ResponseBuilder builder=Response.serverError();
			String body=null;
			Status status=null;

			// 3. Determine the body and status of the response
			try {
				// 3.1. retrieve the resource
				DataSet resource = applicationContext.getResource(endpoint());
				// 3.2. prepare the associated entity
				Entity entity=
					context.
						createEntity(
							resource);
				// 3.3. serialize the entity
				body=entity.serialize(variant,ImmutableContext.newInstance(context.base(),context.resourceIndex()));
				status=entity.isEmpty()?Status.NO_CONTENT:Status.OK;
				builder.type(variant.getMediaType());
			} catch (ContentTransformationException e) {
				status=Status.INTERNAL_SERVER_ERROR;
				body=Throwables.getStackTraceAsString(e);
				builder.type(MediaType.TEXT_PLAIN);
			} catch (ApplicationExecutionException e) {
				status=Status.INTERNAL_SERVER_ERROR;
				body=Throwables.getStackTraceAsString(e);
				builder.type(MediaType.TEXT_PLAIN);
			}
			// 4. add protocol endorsed headers
			populateProtocolEndorsedHeaders(builder);
			
			// 5. add protocol specific headers
			populateProtocolSpecificHeaders(builder,context.resourceType());
			
			// 6. set status and attach response entity as required.
			builder.
				status(status.getStatusCode()).
				header(CONTENT_LENGTH_HEADER, body.length());
			if(includeEntity) {
				builder.entity(body);
			}
			return builder.build();
		}

		private void populateProtocolEndorsedHeaders(ResponseBuilder builder) {
			builder.header(LAST_MODIFIED_HEADER,endpoint().lastModified());
			builder.header(ENTITY_TAG_HEADER,endpoint().entityTag());
		}

		private void populateProtocolSpecificHeaders(ResponseBuilder builder, ResourceType type) {
			// LDP 1.0 - 5.2.1.4 : "LDP servers exposing LDPCs must advertise
			// their LDP support by exposing a HTTP Link header with a target
			// URI matching the type of container (see below) the server
			// supports, and a link relation type of type (that is, rel='type')
			// in all responses to requests made to the LDPC's HTTP Request-URI"
			builder.header(LINK_HEADER,ProtocolUtils.createLink(type, "type"));
			if(type.isContainer()) {
				// LDP 1.0 - 5.2.1.4 : "LDP servers may provide additional HTTP
				// Link: rel='type' headers"
				builder.header(LINK_HEADER,ProtocolUtils.createLink(ResourceType.RESOURCE,"type"));
			}
		}

		public Response createResource(OperationContext context) {
			// Validate input request
			final DataSet dataSet=context.dataSet();

			// Enforce valid state
			context.evaluatePreconditions(endpoint().entityTag(),endpoint().lastModified());

			Response response=null;
			try {
				Resource newResource = applicationContext.createResource(endpoint(),dataSet);
				URI location = context.resolve(newResource);
				response=Response.created(location).entity(location).type(MediaType.TEXT_PLAIN).build();
			} catch (ApplicationExecutionException e) {
				response=Response.serverError().entity(Throwables.getStackTraceAsString(e)).build();
			}
			return response;
		}

		private List<Variant> supportedVariants() {
			return VariantUtils.defaultVariants();
		}
	
	}

	
	
	private static final class NotFoundEndpointController extends FixedResponseEndpointController {

		private NotFoundEndpointController(Endpoint endpoint) {
			super(endpoint);
		}

		protected Response defaultResponse() {
			return 
				Response.
					status(Status.NOT_FOUND).
					entity(endpoint().path()+" not found").
					build();
		}
		
	}

	private static final class GoneEndpointController extends FixedResponseEndpointController {

		private GoneEndpointController(Endpoint endpoint) {
			super(endpoint);
		}

		protected Response defaultResponse() {
			return 
				Response.
					status(Status.GONE).
					entity(endpoint().path()+" is gone").
					build();
		}
		
	}

	private final class LocalEndpointLifecycleListener implements EndpointLifecycleListener {
		@Override
		public void endpointCreated(Endpoint endpoint) {
		}
		@Override
		public void endpointDeleted(Endpoint endpoint) {
			EndpointControllerFactory.this.goneEndpoints.add(endpoint.path());
		}
	}

	private final class LocalApplicationLifecycleListener implements ApplicationLifecycleListener {

		@Override
		public void applicationStateChanged(ApplicationState newState) {
			switch(newState) {
			case AVAILABLE:
				applicationContext().
					registerEndpointLifecyleListener(endpointLifecycleListener());
				break;
			case SHUTDOWN:
				applicationContext().
					deregisterEndpointLifecycleListener(endpointLifecycleListener());
				applicationContext().
					deregisterApplicationLifecycleListener(this);
				break;
			case UNAVAILABLE:
				break;
			case UNDEFINED:
				break;
			}
		}

		private EndpointLifecycleListener endpointLifecycleListener() {
			return EndpointControllerFactory.this.endpointLifecyleListener;
		}

		private ApplicationContext applicationContext() {
			return EndpointControllerFactory.this.applicationContext;
		}

	}

	private static final String ENTITY_TAG_HEADER     = "ETag";
	private static final String LAST_MODIFIED_HEADER  = "Last-Modified";
	private static final String CONTENT_LENGTH_HEADER = "Content-Length";
	private static final String LINK_HEADER           = "Link";
	private final Set<String> goneEndpoints;

	private final LocalApplicationLifecycleListener applicationLifecyleListener;

	private final LocalEndpointLifecycleListener endpointLifecyleListener;

	private final ApplicationContext applicationContext;
	
	private EndpointControllerFactory(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.applicationLifecyleListener = new LocalApplicationLifecycleListener();
		this.endpointLifecyleListener = new LocalEndpointLifecycleListener();
		this.goneEndpoints=new ConcurrentHashSet<String>();
		this.applicationContext.registerApplicationLifecycleListener(this.applicationLifecyleListener);
	}
	
	private String normalizePath(String path) {
		String tPath=path;
		if(tPath==null) {
			tPath="";
		} else {
			tPath = tPath.trim();
		}
		return tPath;
	}

	private Endpoint findEndpoint(String path) {
		return this.applicationContext.resolveEndpoint(normalizePath(path));
	}

	private boolean isGone(String path) {
		return this.goneEndpoints.contains(path);
	}

	public EndpointController createController(String path) {
		Endpoint endpoint=findEndpoint(path);
		EndpointController result=null;
		if(endpoint!=null) {
			result=new ExistingEndpointController(endpoint);
		} else if(isGone(path)) {
			result=new GoneEndpointController(endpoint);
		} else {
			result=new NotFoundEndpointController(endpoint);
		}
		return result;
	}
	
	public static EndpointControllerFactory newInstance(ApplicationContext context) {
		checkNotNull(context,"Application context cannot be null");
		return new EndpointControllerFactory(context);
	}

}
