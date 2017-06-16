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
 *   Artifact    : org.ldp4j.framework:ldp4j-conformance-validation:0.2.2
 *   Bundle      : ldp4j-conformance-validation-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.conformance.validation;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcceptanceTestDriver {

	interface ArchiveProvider {
		WebArchive provide(String archiveName);
	}

	private abstract static class BaseArchiveProvider implements ArchiveProvider{

		private static final String GROUP_ID    = "org.ldp4j.framework";
		private static final String ARTIFACT_ID = "ldp4j-conformance-fixture";
		private static final String VERSION     = System.getProperty("ldp4j.version");
		private static final String PACKAGING   = "war";

		protected final WebArchive baseApplicationArchive(final String archiveName) {
			final File[] files =
					Maven.
						configureResolver().
							fromClassloaderResource("settings.xml").
							resolve(GROUP_ID+":"+ARTIFACT_ID+":"+PACKAGING+":"+VERSION).
							withoutTransitivity().
							as(File.class);
			final WebArchive archive =
				ShrinkWrap.
					create(WebArchive.class,archiveName).
						merge(ShrinkWrap.createFromZipFile(WebArchive.class,files[0]));
			return archive;
		}
	}


	private static final class ApplicationArchiveProvider extends BaseArchiveProvider {
		@Override
		public WebArchive provide(final String archiveName) {
			try {
				return baseApplicationArchive(archiveName).
						addClass(JaCoCoAgentController.class);
			} catch (final Throwable e) {
				LOGGER.error("Could not create locally backed application archive",e);
				throw new IllegalStateException("Could not create locally backed application archive",e);
			}
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(AcceptanceTestDriver.class);

	private static WebArchive getArchive(final String archiveName, final ArchiveProvider aProvider) {
		final ArchiveProvider theProvider=aProvider;
		return theProvider.provide(archiveName);
	}

	public static WebArchive applicationArchive(final String archiveName) throws Exception {
		return getArchive(archiveName, new ApplicationArchiveProvider());
	}

	protected static void logFailure(final Throwable failure) {
		Throwable cause=failure;
		while(cause!=null) {
			LOGGER.warn(" - "+cause.getClass().getName()+" : "+cause.getMessage());
			cause=cause.getCause();
		}
	}

}
