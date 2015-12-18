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
 *   Artifact    : org.ldp4j.framework:ldp4j-conformance-validation:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-conformance-validation-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.conformance.validation;

import java.io.File;
import java.net.URL;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ConformanceUtil {

	private static final Logger LOGGER=LoggerFactory.getLogger(ConformanceUtil.class);

	static WebArchive createWebArchive(String archiveName) throws Exception {
		try {
			File[] files =
				Maven.
					configureResolver().
						loadPomFromFile("target/test-classes/pom.xml").
						importCompileAndRuntimeDependencies().
						resolve().
						withTransitivity().
						asFile();
			return
				ShrinkWrap.
					create(WebArchive.class,archiveName).
						addClass(JaCoCoAgentController.class).
						addAsLibraries(files).
						addAsResource("log4j.properties").
						setWebXML(new File("src/test/resources/web.xml"));
		} catch (Exception e) {
			LOGGER.error("Could not create archive",e);
			throw e;
		}
	}

	static String resolve(URL base, String path) {
		return base.toString()+path;
	}

}
