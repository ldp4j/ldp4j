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
package org.ldp4j.client.impl.cxf;

import java.net.URL;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.ldp4j.client.impl.sdk.AbstractRemoteLDPContainer;
import org.ldp4j.client.impl.spi.IRemoteLDPContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CXFRemoteLDPContainer extends AbstractRemoteLDPContainer {

	private static final Logger LOGGER=LoggerFactory.getLogger(CXFRemoteLDPContainer.class);

	private IRemoteLDPContainer gateway;

	public CXFRemoteLDPContainer(URL url) {
		super(url);
		gateway = JAXRSClientFactory.create(getTarget().toString(), IRemoteLDPContainer.class);
	}

	@Override
	public Response createResourceFromTurtle(String content) {
		return gateway.createResourceFromTurtle(content);
	}

	@Override
	public Response createResourceFromRDFXML(String content) {
		return gateway.createResourceFromRDFXML(content);
	}

	@Override
	public Response getResource(String format) {
		return getResource(format,false,false);
	}

	@Override
	public Response getResource(String format, boolean includeMembers, boolean includeSummary) {
		String modifiedTarget = getTarget().toString();
		if(!includeMembers) {
			modifiedTarget=modifiedTarget.concat("?non-member-properties");
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Requested container raw description (only non-member properties):"+getTarget()+" ==> "+modifiedTarget);
			}
		} else if(!includeSummary) {
			modifiedTarget=modifiedTarget.concat("?member-properties");
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Requested container description (member and non member properties):"+getTarget()+" ==> "+modifiedTarget);
			}
		} else {
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Requested container description with resource member summary:"+getTarget());
			}
		}
		return 
			JAXRSClientFactory.
				create(modifiedTarget, IRemoteLDPContainer.class).
					getResource(format);
	}

}