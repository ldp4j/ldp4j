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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons.net;

import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class URLStreamHandlerFactory {

	public static final class ClassInstantiationException extends Exception {

		private static final long serialVersionUID = 2720228808686747756L;

		private final String className;

		public ClassInstantiationException(String className,Throwable cause) {
			super(String.format("Could not instantiate class '%s'",className),cause);
			this.className = className;
		}

		public String getClassName() {
			return className;
		}

	}

	interface ClassInstantiator {

		<T> T instantiateAs(String clsName, Class<? extends T> clazz) throws ClassInstantiationException;

		String getDescription();

	}

	private static final class HandlerCreator {

		private static final Logger LOGGER=LoggerFactory.getLogger(HandlerCreator.class);

		private final URLStreamHandlerFactory.ClassInstantiator loader;
		private final boolean logFailure;

		HandlerCreator(ClassInstantiator instantiator, boolean logFailure) {
			this.loader=instantiator;
			this.logFailure=logFailure;
		}

		URLStreamHandler createHandler(String scheme, String provider) {
			String clsName = provider + "." + scheme + ".Handler";
			try {
				return loader.instantiateAs(clsName, URLStreamHandler.class);
			} catch (ClassInstantiationException e) {
				return handleFailure(scheme, provider, e);
			}
		}

		private URLStreamHandler handleFailure(String scheme, String provider, ClassInstantiationException e) {
			URLStreamHandler result=null;
			if(logFailure && LOGGER.isDebugEnabled()) {
				LOGGER.debug(
					"Could not instantiate provider '{}' handler class for protocol '{}' using {} : {}",
					provider,
					scheme,
					loader.getDescription(),
					e.getMessage());
			}
			return result;
		}

	}

	private static final class DefaultInstantiator implements ClassInstantiator {

		@Override
		public String getDescription() {
			return "current classloader";
		}

		@Override
		public <T> T instantiateAs(String clsName, Class<? extends T> clazz) throws ClassInstantiationException {
			try {
				return clazz.cast(Class.forName(clsName).newInstance());
			} catch (Exception e) {
				throw new ClassInstantiationException(clsName,e);
			}
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(URLStreamHandlerFactory.class);

	private final boolean logSupport;
	private final boolean logCreation;

	private final HandlerCreator creator;

	private URLStreamHandlerFactory(boolean debug, boolean trace, ClassInstantiator instantiator) {
		this.logSupport = debug;
		this.logCreation = trace;
		this.creator = new HandlerCreator(instantiator, trace);
	}

	URLStreamHandlerFactory(boolean debug, boolean trace) {
		this(debug,trace,new DefaultInstantiator());
	}

	URLStreamHandlerFactory(ClassInstantiator instantiator) {
		this(true,true,instantiator);
	}

	URLStreamHandler createForScheme(String scheme, String... providers) {
		return createForScheme(scheme,Arrays.asList(providers));
	}

	URLStreamHandler createForScheme(String scheme, List<String> providers) {
		if(logCreation && LOGGER.isDebugEnabled()) {
			LOGGER.debug("Creating handler for protocol '{}'...",scheme);
		}
		for(String provider:providers) {
			URLStreamHandler handler = creator.createHandler(scheme, provider);
			logHandlerCreation(scheme, provider, handler);
			if(handler!=null) {
				return handler;
			}
		}
		if(logSupport && LOGGER.isDebugEnabled()) {
			LOGGER.debug("Protocol '{}' is not supported.",scheme);
		}
		return null;
	}

	private void logHandlerCreation(String scheme, String provider,URLStreamHandler handler) {
		if(LOGGER.isDebugEnabled()) {
			if(handler!=null) {
				if(logSupport) {
					LOGGER.debug("Provider '{}' supports protocol '{}'.",provider,scheme);
				}
				if(logCreation) {
					LOGGER.debug("Created handler '{}' from provider '{}'.",handler.getClass().getCanonicalName(),provider);
				}
			} else if(logSupport) {
				LOGGER.debug("Provider '{}' does not support protocol '{}'.",provider,scheme);
			}
		}
	}

}