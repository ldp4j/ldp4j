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
package org.ldp4j.server.commands;


import java.net.URI;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.server.api.ComponentRegistryFactory;
import org.ldp4j.server.api.Endpoint;
import org.ldp4j.server.api.EndpointRegistry;
import org.ldp4j.server.api.ResourceIndex;
import org.ldp4j.server.blueprint.ComponentRegistry;
import org.ldp4j.server.commands.xml.CreateEndpoint;
import org.ldp4j.server.commands.xml.DeleteEndpoint;
import org.ldp4j.server.commands.xml.EndpointConfiguration;

public class CommandProcessingServiceTest {

	private CommandProcessingService sut;
	private static CommandDescriptionUtil commandUtil;
	private ComponentRegistry registry;
	private ResourceIndex resourceIndex;
	private EndpointRegistry endpointIndex;

	@BeforeClass
	public static void setUpBefore() throws JAXBException {
		commandUtil = CommandDescriptionUtil.newInstance();
	}

	@Before
	public void setUp() throws Exception {
		registry = ComponentRegistryFactory.newInstance();
		resourceIndex = registry.getComponent(ResourceIndex.class);
		endpointIndex = registry.getComponent(EndpointRegistry.class);
		sut=new CommandProcessingService(registry);
	}

	@Test
	public void testProcess$creation() throws Exception {
		CreateEndpoint command=
			new CreateEndpoint().
				withPath("resource/path").
				withTemplateId("templateId").
				withCapabilities(new EndpointConfiguration.Capabilities());
		sut.process(commandUtil.toString(CommandDescription.newInstance(command)));
		URI path = URI.create("resource/path");
		Endpoint endpoint=endpointIndex.findEndpoint(path);
		System.out.println(endpoint);
		ResourceId resolveLocation = resourceIndex.resolveLocation(path);
		System.out.println(resolveLocation);
	}

	@Test
	public void testProcess$deletion() throws Exception {
		testProcess$creation();
		DeleteEndpoint command=
				new DeleteEndpoint().
					withPath("resource/path");
		sut.process(commandUtil.toString(CommandDescription.newInstance(command)));
	}

}
