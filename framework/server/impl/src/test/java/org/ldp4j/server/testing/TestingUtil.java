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
package org.ldp4j.server.testing;

import java.net.URL;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.ldp4j.server.core.ILinkedDataPlatformContainer;
import org.ldp4j.server.core.ILinkedDataPlatformResourceHandler;
import org.ldp4j.server.impl.LinkedDataPlatformRegistry;
import org.ldp4j.server.spi.ILinkedDataPlatformRegistry;
import org.ldp4j.server.testing.stubs.DeletableContainer;
import org.ldp4j.server.testing.stubs.DeletableResource;
import org.ldp4j.server.testing.stubs.FailingContainer;
import org.ldp4j.server.testing.stubs.WorkingContainer;
import org.ldp4j.server.testing.stubs.WorkingResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TestingUtil {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(TestingUtil.class);

	public static <S> S createServiceClient(URL url, Class<S> serviceClass) {
		String baseAddress = url.toString().concat("ldp");
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Create service client '"+serviceClass.getCanonicalName()+"'");
			LOGGER.debug(String.format("\t- Using base address '%s'...",baseAddress));
		}
		S proxy = JAXRSClientFactory.create(baseAddress, serviceClass);
		WebClient.getConfig(proxy).getBus().setProperty("org.apache.cxf.http.header.split", true);
		return proxy;
	}

	static JavaArchive getServerRuntimeArchive() {
		JavaArchive coreArchive= 
			ShrinkWrap.
				create(JavaArchive.class,"ldp4j-server-rt.jar").
				// Legacy stuff
				addPackages(false, "org.ldp4j.server").
				addPackages(true, "org.ldp4j.server.annotations").
				addPackages(true, "org.ldp4j.server.core").
				addPackages(true, "org.ldp4j.server.spi").
				addPackages(true, "org.ldp4j.server.sdk").
				addPackages(true, "org.ldp4j.server.impl").
				addPackages(true, "org.ldp4j.server.utils").
				addAsServiceProvider(ILinkedDataPlatformRegistry.class, LinkedDataPlatformRegistry.class);
//				// New stuff
//				addPackages(true, "org.ldp4j.model").
//				addPackages(true, "org.ldp4j.sdk").
//				addPackages(true, "org.ldp4j.server.commands").
//				addPackages(true, "org.ldp4j.server.api").
//				addAsServiceProvider(RuntimeInstance.class, RuntimeInstanceImpl.class).
//				addAsServiceProvider(IMediaTypeProvider.class,TurtleMediaTypeProvider.class,RDFXMLMediaTypeProvider.class);
//				addAsResources(
//					VocabularyBasedContainerFrontend.class.getPackage(), 
//					"queries/triples.vm",
//					"queries/typed-individuals.vm",
//					"velocity.cfg"
//				);
		return coreArchive;
	}

	static JavaArchive getServerTestingArchive() {
		JavaArchive testingArchive= 
			ShrinkWrap.
				create(JavaArchive.class,"ldp4j-server-testing.jar").
				addPackages(true, "org.ldp4j.server.testing").
				addAsServiceProvider(
					ILinkedDataPlatformContainer.class, 
					WorkingContainer.class, 
					FailingContainer.class,
					DeletableContainer.class
				).
				addAsServiceProvider(
					ILinkedDataPlatformResourceHandler.class,
					WorkingResource.class,
					DeletableResource.class
				);
		return testingArchive;
	}
/*
	static JavaArchive getServerExtendedTestingArchive() {
		JavaArchive testingArchive= 
			ShrinkWrap.
				create(JavaArchive.class,"ldp4j-server-extended-testing.jar").
				addPackages(true, "org.ldp4j.server.testing").
				addAsServiceProvider(
					ILinkedDataPlatformContainer.class, 
					ExampleContainer.class,
					DefectContainer.class,
					PersonContainer.class,
					ProductContainer.class,
					VersionContainer.class
				).
				addAsServiceProvider(
					ILinkedDataPlatformResourceHandler.class,
					ExampleResourceHandler.class,
					DefectResourceHandler.class,
					PersonResourceHandler.class,
					ProductResourceHandler.class,
					VersionResourceHandler.class
				).
				// TODO: Refactor this to domain dependent part of the test.
				addAsResource(
					ClassLoader.getSystemResource("vocabulary/alm-istack.owl"), "vocabulary/alm-istack.owl"
				);
		return testingArchive;
	}
*/
}
