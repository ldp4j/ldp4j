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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ldp4j.client.Content;
import org.ldp4j.client.Format;
import org.ldp4j.client.IContent;
import org.ldp4j.client.ILDPContainer;
import org.ldp4j.client.LDPContainerException;
import org.ldp4j.client.impl.sdk.ResponseHelper;
import org.ldp4j.client.impl.spi.IRemoteLDPContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CoreLDPContainer implements ILDPContainer {

	private static final Logger LOGGER=LoggerFactory.getLogger(CoreLDPContainer.class);
	private static final String NEW_LINE=System.getProperty("line.separator");

	private final IRemoteLDPContainer serviceClient;

	private void logResponse(Response response) {
		if(LOGGER.isDebugEnabled()) {
			try {
				LOGGER.debug(ResponseHelper.dumpResponse(response));
			} catch (IOException e) {
				LOGGER.warn(String.format("Could not process server '%s' response. Full stacktrace follows",getIdentity()),e);
			}
		}
	}

	private LDPContainerException getOperationException(Throwable t, String errorTemplate, Object... args) {
		String errorMessage = String.format(errorTemplate,args);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(errorMessage.concat(". Full stacktrace follows"),t);
		}
		return new LDPContainerException(errorMessage,t);
	}

	private LDPContainerException getOperationException(Response response, String errorMessageTemplate, Object... args) {
		String responseMessage = String.format(errorMessageTemplate,args);
		try {
			responseMessage=responseMessage.concat(String.format(": %s%s",NEW_LINE,ResponseHelper.getEntity(response)));
		} catch (IOException e) {
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format("Could not process server '%s' response. Full stacktrace follows",getIdentity()),e);
			}
		}
		return new LDPContainerException(responseMessage);
	}

	private Response sendRequest(IContent content, Format format) throws LDPContainerException {
		if(content==null) {
			throw new IllegalArgumentException("Object 'content' cannot be null");
		}
		if(format==null) {
			throw new IllegalArgumentException("Object 'format' cannot be null");
		}

		String body = null;
		try {
			body=content.serialize(String.class);
		} catch (IOException ie) {
			throw new LDPContainerException("Could not process content",ie);
		}

		try {
			Response response=null;
			switch(format) {
				case RDFXML:
					response = serviceClient.createResourceFromRDFXML(body);
					break;
				case TURTLE:
					response = serviceClient.createResourceFromTurtle(body);
					break;
				default:
					throw new IllegalArgumentException(String.format("Unsupported format '%s'",format));
			}
			logResponse(response);
			return response;
		} catch (WebApplicationException e) {
			throw getOperationException(e, "Failed to create resource in container '%s'", getIdentity());
		}
	}

	private Response sendRequest(Format format, boolean exclude_members, boolean exclude_member_properties) throws LDPContainerException {
		if(format==null) {
			throw new IllegalArgumentException("Object 'format' cannot be null");
		}
		try {
			Response response=serviceClient.getResource(format.getMime(), !exclude_members, !exclude_member_properties);
			response.bufferEntity();
			logResponse(response);
			return response;
		} catch (WebApplicationException e) {
			throw getOperationException(e, "Failed to retrieve container '%s' description",getIdentity());
		}
	}

	/**
	 * @param response
	 * @return
	 * @throws LDPContainerException
	 */
	private URL getLocation(Response response) throws LDPContainerException {
		MultivaluedMap<String, Object> headers = response.getMetadata();
		if(headers==null) {
			throw new LDPContainerException("Invalid JAX-RS response: no headers found");
		}
		List<Object> locations = headers.get("Location");
		if(locations==null || locations.isEmpty()) {
			throw new LDPContainerException("Invalid response: no resource location returned");
		}
		for(Object rawLocation:locations) {
			try {
				return new URL(rawLocation.toString());
			} catch (MalformedURLException e) {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace(String.format("Discarding invalid returned resource location '%s'. Full stacktrace follows",rawLocation),e);
				}
			}
		}
		String errorMessage = "Invalid response: no valid resource location returned";
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format(errorMessage));
		}
		throw new LDPContainerException(errorMessage);
	}

	public CoreLDPContainer(IRemoteLDPContainer gateway) {
		if(gateway==null) {
			throw new IllegalArgumentException("Object 'gateway' cannot be null");
		}
		this.serviceClient = gateway;
	}

	@Override
	public URL getIdentity() {
		return serviceClient.getTarget();
	}

	@Override
	public URL createResource(IContent content, Format format) throws LDPContainerException {
		Response response = sendRequest(content, format);
		Status status = Status.fromStatusCode(response.getStatus());
		switch(status) {
			case CREATED:
				return getLocation(response);
			case INTERNAL_SERVER_ERROR:
				throw getOperationException(response, "Resource creation failed");
			default:
				throw getOperationException(response, "Unexpected container response");
		}
	}

	@Override
	public IContent getDescription(Format format, boolean excludeMembers, boolean excludeMemberProperties) throws LDPContainerException {
		Response response = sendRequest(format,excludeMembers,excludeMemberProperties);
		Status status = Status.fromStatusCode(response.getStatus());
		try {
			switch(status) {
				case OK:
					return 
						Content.
							newInstance(
								ResponseHelper.getEntity(response));
				case INTERNAL_SERVER_ERROR:
					throw getOperationException(response, "Container '%s' description retrieval failed", getIdentity());
				default:
					throw getOperationException(response, "Unexpected container response");
			}
		} catch (IOException e) {
			throw getOperationException(e, "Container '%s' description retrieval failed: %s",getIdentity(),e.getMessage());
		}
	}

	@Override
	public IContent searchResources(Format format, int page, int count) throws LDPContainerException {
		return getDescription(format,false,true);
	}
	
}