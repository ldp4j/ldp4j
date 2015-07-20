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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-examples-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import static org.ldp4j.application.data.IndividualReferenceBuilder.newReference;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;

import org.ldp4j.application.data.DataDSL;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.ApplicationInitializationException;
import org.ldp4j.application.ext.ApplicationSetupException;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.ldp4j.application.setup.Bootstrap;
import org.ldp4j.application.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyApplication extends Application<Configuration> {

	private static final String PERSON_CONTAINER_NAME    = "PersonContainer";
	private static final String PERSON_RESOURCE_NAME     = "PersonResource";
	private static final String RELATIVE_CONTAINER_NAME  = "RelativeContainer";

	public static final String ROOT_PERSON_RESOURCE_PATH  = "rootPersonResource/";
	public static final String ROOT_PERSON_CONTAINER_PATH = "rootPersonContainer/";

	private static final Logger LOGGER=LoggerFactory.getLogger(MyApplication.class);

	private final Name<String> personResourceName;
	private final Name<String> personContainerName;
	private final Name<String> relativeContainerName;

	public MyApplication() {
		this.personResourceName = NamingScheme.getDefault().name(PERSON_RESOURCE_NAME);
		this.personContainerName = NamingScheme.getDefault().name(PERSON_CONTAINER_NAME);
		this.relativeContainerName = NamingScheme.getDefault().name(RELATIVE_CONTAINER_NAME);
	}

	private DataSet getInitialData(String templateId, String name) throws DatatypeConfigurationException {
		return
			DataDSL.
				dataSet().
					individual(newReference().toManagedIndividual(templateId).named(name)).
						hasProperty("http://www.ldp4j.org/vocabulary/example#creationDate").
							withValue(new Date()).
						hasProperty("http://www.ldp4j.org/vocabulary/example#age").
							withValue(34).
						hasLink("http://www.ldp4j.org/vocabulary/example#hasFather").
							toIndividual(newReference().toLocalIndividual().named("Michel")).
								hasLink("http://www.ldp4j.org/vocabulary/example#hasWife").
									referringTo(newReference().toLocalIndividual().named("Consuelo")).
						build();
	}

	@Override
	public void setup(Environment environment, Bootstrap<Configuration> bootstrap) throws ApplicationSetupException {
		LOGGER.info("Configuring application: {}, {}",environment,bootstrap);
		try {
			PersonHandler resourceHandler = new PersonHandler();
			PersonContainerHandler containerHandler=new PersonContainerHandler();
			RelativeContainerHandler relativesHandler=new RelativeContainerHandler();

			containerHandler.setHandler(resourceHandler);
			relativesHandler.setHandler(resourceHandler);

			resourceHandler.add(this.personResourceName, getInitialData(PersonHandler.ID,PERSON_RESOURCE_NAME));
			containerHandler.add(this.personContainerName, getInitialData(PersonContainerHandler.ID,PERSON_CONTAINER_NAME));
			relativesHandler.add(this.relativeContainerName, getInitialData(RelativeContainerHandler.ID,RELATIVE_CONTAINER_NAME));

			bootstrap.addHandler(resourceHandler);
			bootstrap.addHandler(containerHandler);
			bootstrap.addHandler(relativesHandler);

			environment.publishResource(this.personResourceName, PersonHandler.class, ROOT_PERSON_RESOURCE_PATH);
			environment.publishResource(this.personContainerName, PersonContainerHandler.class, ROOT_PERSON_CONTAINER_PATH);
			LOGGER.info("Configuration completed.");
		} catch (DatatypeConfigurationException e) {
			throw new ApplicationSetupException("Could not setup application",e);
		}
	}

	@Override
	public void initialize(WriteSession session) throws ApplicationInitializationException {
		LOGGER.info("Initializing application: {}",session);
		ResourceSnapshot person = session.find(ResourceSnapshot.class,this.personResourceName,PersonHandler.class);
		LOGGER.info("Root resource.......: "+person);
		LOGGER.info("Root basic container: "+session.find(ResourceSnapshot.class,this.personContainerName,PersonContainerHandler.class));

		ContainerSnapshot relativesContainer = person.createAttachedResource(ContainerSnapshot.class, PersonHandler.RELATIVES_ID, this.relativeContainerName, RelativeContainerHandler.class);
		LOGGER.info("Attached resource...: "+relativesContainer);
		try {
			session.saveChanges();
		} catch (WriteSessionException e) {
			throw new ApplicationInitializationException("Could not initialize application",e);
		}
		LOGGER.info("Initialization completed.");
	}

	@Override
	public void shutdown() {
		LOGGER.info("Shutting down application");
	}

}
