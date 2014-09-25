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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.xmlmatchers.XmlMatchers.conformsTo;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.validation.SchemaFactory.w3cXmlSchemaFromClasspath;

import javax.xml.namespace.NamespaceContext;
import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.ldp4j.server.commands.xml.CreateEndpoint;
import org.ldp4j.server.commands.xml.EntityType;
import org.ldp4j.server.commands.xml.ResourceStateType;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

public class CommandDescriptionUtilTest {

	private static Schema COMMAND_SCHEMA;
	private CommandDescriptionUtil sut;

	@BeforeClass
	public static void setUpBefore() throws Exception {
		COMMAND_SCHEMA = w3cXmlSchemaFromClasspath("org/ldp4j/server/commands/ldp4j-command-api.xsd");
	}
	
	@Before
	public void setUp() throws Exception {
		sut=CommandDescriptionUtil.newInstance();
	}

	@Test
	public void testToString() throws Exception {
		String path = "example/path";
		String entity = "Example body";
		String templateId ="templateId";
		
		CreateEndpoint command=
			new CreateEndpoint().
				withPath(path).
				withTemplateId(templateId).
				withResourceState(
					new ResourceStateType().
						withEntity(
							new EntityType().
								withValue(entity)));
		CommandDescription description = CommandDescription.newInstance(command);
		String xml = sut.toString(description);

		assertThat(xml,notNullValue());
		assertThat(the(xml),conformsTo(COMMAND_SCHEMA));

		NamespaceContext usingNamespaces = 
				new SimpleNamespaceContext().
					withBinding("ldp4j", "http://www.ldp4j.org/schemas/command-api/1.0");

		assertThat(
			the(xml),
			hasXPath(
				"/ldp4j:create-endpoint/@path",
				usingNamespaces,
				equalTo(path)
			)
		);
		assertThat(
			the(xml),
			hasXPath(
				"/ldp4j:create-endpoint/@templateId",
				usingNamespaces,
				equalTo(templateId)
			)
		);
		assertThat(
			the(xml),
			hasXPath(
				"/ldp4j:create-endpoint/ldp4j:resource-state/ldp4j:entity/text()",
				usingNamespaces,
				equalTo(entity)
			)
		);
	}

	@Ignore("Nothing to test by now")
	@Test
	public void testFromString() throws Exception {
		throw new RuntimeException("not yet implemented");
	}


}
