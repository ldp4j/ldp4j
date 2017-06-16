/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-conformance-fixture:0.2.2
 *   Bundle      : ldp4j-conformance-fixture-0.2.2.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.conformance.fixture;

import static org.ldp4j.application.data.IndividualReferenceBuilder.newReference;

import java.net.URI;
import java.util.Date;

import org.ldp4j.application.data.DataDSL;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.ldp4j.application.setup.Bootstrap;
import org.ldp4j.application.setup.Environment;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCKFApplication extends Application<Configuration> {

	private static final String RESOURCE_NAME          = "Resource";
	private static final String BASIC_CONTAINER_NAME   = "BasicContainer";
	private static final String DIRECT_CONTAINER_NAME  = "DirectContainer";
	private static final String INDIRECT_CONTAINER_NAME= "IndirectContainer";

	public static final String ROOT_RESOURCE_PATH           = "resource/";
	public static final String ROOT_BASIC_CONTAINER_PATH    = "basic_container/";
	public static final String ROOT_DIRECT_CONTAINER_PATH   = "direct_container/";
	public static final String ROOT_INDIRECT_CONTAINER_PATH = "indirect_container/";

	private static final Logger LOGGER=LoggerFactory.getLogger(TCKFApplication.class);

	private final Name<String> resourceName;
	private final Name<String> basicContainerName;
	private final Name<String> directContainerName;
	private final Name<String> indirectContainerName;


	public TCKFApplication() {
		this.resourceName = NamingScheme.getDefault().name(RESOURCE_NAME);
		this.basicContainerName = NamingScheme.getDefault().name(BASIC_CONTAINER_NAME);
		this.directContainerName = NamingScheme.getDefault().name(DIRECT_CONTAINER_NAME);
		this.indirectContainerName = NamingScheme.getDefault().name(INDIRECT_CONTAINER_NAME);
	}

	private DataSet getInitialData(String templateId, String name, boolean markContainer) {
		DataSet initial=null;
		if(!markContainer) {
			initial=
				DataDSL.
					dataSet().
						individual(newReference().toManagedIndividual(templateId).named(name)).
							hasProperty(TCKFHelper.READ_ONLY_PROPERTY.toString()).
								withValue(new Date()).
							build();
		} else {
			initial=
				DataDSL.
					dataSet().
						individual(newReference().toManagedIndividual(templateId).named(name)).
							hasProperty(TCKFHelper.READ_ONLY_PROPERTY.toString()).
								withValue(new Date()).
							hasLink(RDF.TYPE.qualifiedEntityName()).
								referringTo(newReference().toExternalIndividual().atLocation(LDP.BASIC_CONTAINER.as(URI.class))).
							build();
		}
		return initial;
	}

	@Override
	public void setup(Environment environment, Bootstrap<Configuration> bootstrap) {
		LOGGER.info("Configuring application: {}, {}",environment,bootstrap);

		TCKFResourceHandler resourceHandler = new TCKFResourceHandler();
		TCKFBasicContainerHandler basicContainer=new TCKFBasicContainerHandler();
		TCKFDirectContainerHandler directContainer=new TCKFDirectContainerHandler();
		TCKFIndirectContainerHandler indirectContainer=new TCKFIndirectContainerHandler();

		basicContainer.setHandler(resourceHandler);
		directContainer.setHandler(resourceHandler);
		indirectContainer.setHandler(resourceHandler);

		resourceHandler.add(this.resourceName, getInitialData(TCKFResourceHandler.ID, RESOURCE_NAME, true));
		basicContainer.add(this.basicContainerName, getInitialData(TCKFBasicContainerHandler.ID,BASIC_CONTAINER_NAME, false));
		directContainer.add(this.directContainerName, getInitialData(TCKFDirectContainerHandler.ID,DIRECT_CONTAINER_NAME, false));
		indirectContainer.add(this.indirectContainerName, getInitialData(TCKFIndirectContainerHandler.ID,INDIRECT_CONTAINER_NAME, false));

		bootstrap.addHandler(resourceHandler);
		bootstrap.addHandler(basicContainer);
		bootstrap.addHandler(directContainer);
		bootstrap.addHandler(indirectContainer);

		environment.publishResource(this.resourceName, TCKFResourceHandler.class, ROOT_RESOURCE_PATH);
		environment.publishResource(this.basicContainerName, TCKFBasicContainerHandler.class, ROOT_BASIC_CONTAINER_PATH);

		LOGGER.info("Configuration completed.");
	}

	@Override
	public void initialize(WriteSession session) {
		LOGGER.info("Initializing application: {}",session);
		try {
			ResourceSnapshot rootResource = session.find(ResourceSnapshot.class, this.resourceName, TCKFResourceHandler.class);
			rootResource.createAttachedResource(ContainerSnapshot.class, "directContainer", this.directContainerName, TCKFDirectContainerHandler.class);
			rootResource.createAttachedResource(ContainerSnapshot.class, "indirectContainer", this.indirectContainerName, TCKFIndirectContainerHandler.class);
			session.saveChanges();
			LOGGER.info("Initialization completed.");
		} catch (WriteSessionException e) {
			LOGGER.warn("Initialization failed.",e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void shutdown() {
		LOGGER.info("Shutting down application...");
		LOGGER.info("Application terminated");
	}

}
