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
package org.ldp4j.server.example;

import static org.ldp4j.application.data.IndividualReferenceBuilder.newReference;

import org.ldp4j.application.data.DataDSL;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.example.PersonContainerHandler;
import org.ldp4j.application.example.PersonHandler;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.setup.Bootstrap;
import org.ldp4j.application.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyApplication extends Application<Configuration> {

	public static final String ROOT_PERSON_PATH = "rootPerson";
	public static final String ROOT_PERSON_CONTAINER_PATH = "rootPersonContainer";

	private static final Logger LOGGER=LoggerFactory.getLogger(MyApplication.class);

	private final Name<String> personResourceName;
	private final Name<String> personContainerName;
	
	public MyApplication() {
		this.personResourceName = NamingScheme.getDefault().name("Miguel");
		this.personContainerName = NamingScheme.getDefault().name("PersonContainer");
	}

	private DataSet getInitialData(String templateId, String name) {
		DataSet initial=
			DataDSL.
				dataSet().
					individual(newReference().toManagedIndividual(templateId).named(name)). // Initial context
						hasProperty("http://www.ldp4j.org/vocabulary/example#age").
							withValue(34).
						hasLink("http://www.ldp4j.org/vocabulary/example#hasFather").
							toIndividual(newReference().toLocalIndividual().named("Michel")). // New individual and context
								hasLink("http://www.ldp4j.org/vocabulary/example#hasWife").
									referringTo(newReference().toLocalIndividual().named("Consuelo")). // New individual, same context
						build(); // Create data set
		return initial;
	}

	@Override
	public void setup(Environment environment, Bootstrap<Configuration> bootstrap) {
		LOGGER.info("Configuring application: "+bootstrap);

		PersonHandler resourceHandler = new PersonHandler();
		PersonContainerHandler containerHandler=new PersonContainerHandler();
		containerHandler.setHandler(resourceHandler);

		resourceHandler.add(this.personResourceName, getInitialData(PersonHandler.ID,"Miguel"));
		containerHandler.add(this.personContainerName, getInitialData(PersonContainerHandler.ID,"PersonContainer"));

		bootstrap.addHandler(resourceHandler);
		bootstrap.addHandler(containerHandler);

		environment.publishResource(this.personResourceName, PersonHandler.class, ROOT_PERSON_PATH);
		environment.publishResource(this.personContainerName, PersonContainerHandler.class, ROOT_PERSON_CONTAINER_PATH);
	}

	@Override
	public void initialize(WriteSession session) {
		LOGGER.info("Initializing application: "+session);
		LOGGER.info("Root resource........: "+session.find(ResourceSnapshot.class,this.personResourceName,PersonHandler.class));
		LOGGER.info("Root direct container: "+session.find(ResourceSnapshot.class,this.personContainerName,PersonContainerHandler.class));
	}

	@Override
	public void shutdown() {
		LOGGER.info("Shutting down application");
	}

}
