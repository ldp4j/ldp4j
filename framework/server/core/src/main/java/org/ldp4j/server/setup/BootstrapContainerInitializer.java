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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.setup;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.ldp4j.application.ext.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HandlesTypes(Application.class)
public class BootstrapContainerInitializer implements ServletContainerInitializer {

	private static final Logger LOGGER=LoggerFactory.getLogger(BootstrapContainerInitializer.class);

	private final AtomicBoolean initialized=new AtomicBoolean();

	@Override
	public void onStartup(Set<Class<?>> classes, final ServletContext context) throws ServletException {
		initializeContainer(context);
		LOGGER.debug("* Context started:");
		LOGGER.debug("- Name...........: "+context.getServletContextName());
		LOGGER.debug("- Public path....: "+context.getContextPath());
		LOGGER.debug("- Deployment path: "+context.getRealPath("/"));
		LOGGER.debug("- Applications...: "+classes);
		context.addListener(
			new ServletContextListener() {
				@Override
				public void contextInitialized(ServletContextEvent arg0) {
					LOGGER.debug("Context at '{}' initialized...",context.getContextPath());
				}
				@Override
				public void contextDestroyed(ServletContextEvent arg0) {
					LOGGER.debug("Context at '{}' destroyed...",context.getContextPath());
				}
			}
		);
	}

	private void initializeContainer(final ServletContext context) {
		if(!initialized.compareAndSet(false, true)) {
			return;
		}
		final String serverInfo = context.getServerInfo();
		final int majorVersion = context.getMajorVersion();
		final int minorVersion = context.getMinorVersion();
		LOGGER.debug("Starting container {} {}.{}",serverInfo,majorVersion,minorVersion);
		Runtime.
			getRuntime().
				addShutdownHook(
					new Thread() {
						@Override
						public void run() {
							LOGGER.debug("Shutting down container {} {}.{}",serverInfo,majorVersion,minorVersion);
						}
					}
				);
	}

}
