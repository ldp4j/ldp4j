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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client.impl;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ldp4j.client.Content;
import org.ldp4j.client.DeletionResult;
import org.ldp4j.client.Format;
import org.ldp4j.client.IContent;
import org.ldp4j.client.ILDPResource;
import org.ldp4j.client.IRepresentation;
import org.ldp4j.client.LDPResourceException;
import org.ldp4j.client.impl.sdk.ResponseHelper;
import org.ldp4j.client.impl.spi.IRemoteLDPResource;
import org.ldp4j.client.impl.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CoreLDPResource implements ILDPResource {

	private static final class ResponseWrapperRepresentation implements IRepresentation {

		private final Response response;

		private ResponseWrapperRepresentation(Response response) {
			this.response = response;
		}

		@Override
		public IContent getContent() throws IOException {
			return Content.newInstance(response.getEntity());
		}

		@Override
		public String getEntityTag() {
			Object etag = response.getMetadata().getFirst("ETag");
			return etag==null?null:etag.toString();
		}

		@Override
		public Date getLastModified() {
			Object etag = response.getMetadata().getFirst("Last-Modified");
			return HttpUtil.getHttpDate(etag==null?null:etag.toString());
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(CoreLDPResource.class);
	private static final String NEW_LINE=System.getProperty("line.separator");

	private final IRemoteLDPResource serviceClient;

	public CoreLDPResource(IRemoteLDPResource serviceClient) {
		if(serviceClient==null) {
			throw new IllegalArgumentException("Object 'serviceClient' cannot be null");
		}
		this.serviceClient = serviceClient;
	}

	private LDPResourceException getOperationException(Throwable e, String errorTemplate, Object... args) {
		String errorMessage = String.format(errorTemplate,args);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(errorMessage.concat(". Full stacktrace follows"),e);
		}
		return new LDPResourceException(errorMessage,e);
	}

	private LDPResourceException getOperationException(Response response, String responseMessage) {
		String result=responseMessage;
		try {
			result=responseMessage.concat(String.format(": %s%s",NEW_LINE,ResponseHelper.getEntity(response)));
		} catch (IOException e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format("Could not process server '%s' response. Full stacktrace follows",getIdentity()),e);
			}
		}
		return new LDPResourceException(result);
	}

	@Override
	public URL getIdentity() {
		return serviceClient.getTarget();
	}

	@Override
	public IRepresentation getContent(Format format) throws LDPResourceException {
		if(format==null) {
			throw new IllegalArgumentException("Object 'format' cannot be null");
		}
		try {
			final Response response = serviceClient.getResource(format.getMime());
			Status status = Status.fromStatusCode(response.getStatus());
			switch(status) {
				case OK:
					return new ResponseWrapperRepresentation(response);
				case INTERNAL_SERVER_ERROR:
					throw getOperationException(response, "Resource retrieval failed");
				default:
					throw getOperationException(response, "Unexpected resource response");
			}
		} catch (WebApplicationException e) {
			String errorMessage = String.format("Failed to retrieve LDP resource '%s'",getIdentity());
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(errorMessage.concat(". Full stacktrace follows"),e);
			}
			throw new LDPResourceException(errorMessage,e);
		}
	}

	@Override
	public IRepresentation updateContent(IContent content, Format format) throws LDPResourceException {
		if(content==null) {
			throw new IllegalArgumentException("Object 'content' cannot be null");
		}
		if(format==null) {
			throw new IllegalArgumentException("Object 'format' cannot be null");
		}
		try {
			String body = content.serialize(String.class);
			final Response response = serviceClient.updateResource(body, format.getMime());
			Status status = Status.fromStatusCode(response.getStatus());
			switch(status) {
				case NO_CONTENT:
					return new ResponseWrapperRepresentation(response);
				case INTERNAL_SERVER_ERROR:
					throw getOperationException(response, "Resource update failed");
				default:
					throw getOperationException(response, "Unexpected resource response");
			}
		} catch (IOException e) {
			throw getOperationException(e, "Failed to update LDP resource '%s'", getIdentity());
		} catch (WebApplicationException e) {
			throw getOperationException(e, "Failed to update LDP resource '%s'", getIdentity());
		}
	}

	@Override
	public DeletionResult delete() throws LDPResourceException {
		try {
			final Response response = serviceClient.delete();
			Status status = Status.fromStatusCode(response.getStatus());
			switch(status) {
				case OK:
					return DeletionResult.newBuilder().enacted(true).withMessage(ResponseHelper.getEntity(response)).build();
				case NO_CONTENT:
					return DeletionResult.newBuilder().enacted(true).build();
				case ACCEPTED:
					return DeletionResult.newBuilder().enacted(false).build();
				case INTERNAL_SERVER_ERROR:
					throw getOperationException(response, "Resource deletion failed");
				default:
					throw getOperationException(response, "Unexpected resource response");
			}
		} catch (IOException e) {
			throw getOperationException(e, "Failed to delete LDP resource '%s'", getIdentity());
		} catch (WebApplicationException e) {
			throw getOperationException(e, "Failed to delete LDP resource '%s'", getIdentity());
		}
	}

}