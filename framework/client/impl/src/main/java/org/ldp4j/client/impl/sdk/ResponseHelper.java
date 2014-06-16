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
package org.ldp4j.client.impl.sdk;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.MessageProcessingException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Utility class for helping in the process of consuming JAX-RS responses.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public final class ResponseHelper {

	private static final String NEW_LINE=System.getProperty("line.separator");

	private ResponseHelper() {
	}
	
	private static void addMessage(Map<String, Object> messages, String attributeName,
			Object object) {
		if(object!=null) {
			messages.put(attributeName,object);
		}
	}

	/**
	 * Create a dump of a JAX-RS response.
	 * 
	 * @param response
	 *            The response to be dumped.
	 * @return A string representation of the input response.
	 * @throws IOException
	 *             if any failure happens during the response processing.
	 */
	public static String dumpResponse(Response response) throws IOException {
		Map<String,Object> messages=new HashMap<String,Object>();
		addMessage(messages,"Status", Status.fromStatusCode(response.getStatus()));
		addMessage(messages,"Entity",getEntity(response));
		MultivaluedMap<String, Object> headers=response.getMetadata();
		if(headers!=null && !headers.isEmpty()) {
			StringBuilder builder=new StringBuilder();
			for(Entry<String, List<Object>> entry:headers.entrySet()) {
				builder.append(NEW_LINE).append("\t\t+ ").append(entry.getKey()).append(": ").append(entry.getValue());
			}
			addMessage(messages,"Headers",builder.toString());
		}
		StringBuilder builder=new StringBuilder();
		builder.append("Server response:");
		for(Entry<String, Object> entry:messages.entrySet()) {
			builder.append(NEW_LINE).append("\t- ").append(entry.getKey()).append(": ").append(entry.getValue());
		}
		return builder.toString();
	}

	/**
	 * Get the response entity as a string.
	 * 
	 * @param response
	 *            The input response.
	 * @return A string representation of the response entity.
	 * @throws IOException
	 *             if any failure happens during the response processing.
	 */
	public static String getEntity(Response response) throws IOException {
		String message=null;
		if(response.hasEntity()) {
			try {
				message=response.readEntity(String.class);
			} catch (MessageProcessingException e) {
				throw new IOException("Could not retrieve response entity",e);
			} catch (IllegalStateException e) {
				throw new IOException("Could not retrieve response entity",e);
			}
		}
		return message;
	}
}