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

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;



/**
 * Resource locator implementation that generates resource URIs using the
 * following template: <br />
 * <code>&lt;baseUri&gt;</code><b> / resources /
 * <code></b>&lt;containerId&gt;</code><b> / </b><code>&lt;resourceId&gt;</code><br />
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 * @see org.ldp4j.server.impl.IResourceLocator
 */
public class ResourceLocator implements IResourceLocator {

	private static final String PATH_SEGMENT_SEPARATOR = "/";
	private static final String CONTAINERS_PATH_SEGMENT = "containers/";
	private static final String RESOURCES_PATH_SEGMENT = "resources/";

	private String createRelativeResource(String containerId, String resourceId) {
		return RESOURCES_PATH_SEGMENT+containerId+PATH_SEGMENT_SEPARATOR+resourceId;
	}

	private String createRelativeContainer(String containerId) {
		return CONTAINERS_PATH_SEGMENT+containerId;
	}

	private String getPart(URI location, int part) {
		String result=null;
		String path = location.getPath();
		int lastIndexOf = path.lastIndexOf(RESOURCES_PATH_SEGMENT);
		if(lastIndexOf>=0) {
			String candidate = path.substring(lastIndexOf);
			String[] split = candidate.split(PATH_SEGMENT_SEPARATOR);
			if(split.length==3) {
				result=split[part];
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI createResourceLocation(UriInfo context, String containerId, String resourceId) {
		return context.getBaseUriBuilder().path(createRelativeResource(containerId, resourceId)).build();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI createResourceLocation(String containerId, String resourceId) {
		return UriBuilder.fromUri(createRelativeResource(containerId,resourceId)).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String resolveContainerFromLocation(URI location) {
		return getPart(location, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI createContainerLocation(UriInfo context, String containerId) {
		return context.getBaseUriBuilder().path(createRelativeContainer(containerId)).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI createContainerLocation(String containerId) {
		return UriBuilder.fromUri(createRelativeContainer(containerId)).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String resolveResourceFromLocation(URI location) {
		return getPart(location, 2);
	}

}