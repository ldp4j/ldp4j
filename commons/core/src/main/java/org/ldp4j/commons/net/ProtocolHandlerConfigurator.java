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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProtocolHandlerConfigurator {

	private abstract static class Loader {

		static final ProtocolHandlerConfigurator SINGLETON=new ProtocolHandlerConfigurator();

		private Loader() {
		}

	}

	private static final String SCHEME_PARAM = "Scheme cannot be null";

	private static final String CHECKING_SUPPORT_FOR_CUSTOM_PROTOCOL = "Checking support for custom '%s' protocol...";

	private static final Logger LOGGER=LoggerFactory.getLogger(ProtocolHandlerConfigurator.class);

	private final Map<String,URLStreamHandler> handlerCache;

	private Set<String> packagesAdded;

	private ProtocolHandlerConfigurator() {
		handlerCache=new WeakHashMap<String, URLStreamHandler>();
		packagesAdded = new HashSet<String>();
	}

	private List<String> getProviders() {
		List<String> providers = URLSupport.getProtocolHandlerProviders();
		providers.addAll(packagesAdded);
		return providers;
	}

	private synchronized URLStreamHandler getHandler(String scheme, boolean trace) {
		URLStreamHandler handler = handlerCache.get(scheme);
		if(handler==null) {
			handler=new URLStreamHandlerFactory(true,trace).createForScheme(scheme, getProviders());
			if(handler!=null) {
				handlerCache.put(scheme, handler);
			}
		}
		return handler;
	}

	private synchronized boolean addProvider(String baseProtocolPackage, String... schemes) {
		trace(String.format("Configuring provider '%s' for custom protocol handlers %s",baseProtocolPackage,Arrays.asList(schemes)));
		List<String> providers = getProviders();
		boolean needUpdate=!providers.contains(baseProtocolPackage);
		if(needUpdate) {
			packagesAdded.add(baseProtocolPackage);
		}
		trace(
			!needUpdate?
				"Custom protocol handler provider '%s' already configured.":
				"Registered custom protocol handler provider '%s'.",
				baseProtocolPackage);
		return needUpdate;
	}

	private synchronized void clear() {
		handlerCache.clear();
		packagesAdded.clear();
	}

	private static void debug(String format, Object... args) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format(format,args));
		}
	}

	private static void trace(String format, Object... args) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(String.format(format,args));
		}
	}

	public static boolean isSupported(String scheme) {
		Objects.requireNonNull(scheme, SCHEME_PARAM);
		debug(CHECKING_SUPPORT_FOR_CUSTOM_PROTOCOL, scheme);
		boolean result=Loader.SINGLETON.getHandler(scheme,false)!=null;
		debug("Protocol '%s' is %s.",scheme,result?"supported":"not supported");
		return result;
	}

	public static URLStreamHandler getHandler(String scheme) {
		Objects.requireNonNull(scheme, SCHEME_PARAM);
		return Loader.SINGLETON.getHandler(scheme,true);
	}

	public static <T extends URLStreamHandler> boolean addProvider(Class<T> handlerClass) {
		Objects.requireNonNull(handlerClass,"Handler class cannot be null");
		if(!"Handler".equals(handlerClass.getSimpleName())) { // NOSONAR
			throw new IllegalArgumentException("Handler class '"+handlerClass.getCanonicalName()+"' is not a valid URLStreamHandler class: name does meet specification");
		}
		String protocolPackage=handlerClass.getPackage().getName();
		int pointBreak = protocolPackage.lastIndexOf('.');
		String providerPackage=protocolPackage.substring(0,pointBreak);
		String scheme=protocolPackage.substring(pointBreak+1);
		return Loader.SINGLETON.addProvider(providerPackage,scheme);
	}

	public static void reset() {
		Loader.SINGLETON.clear();
	}

}