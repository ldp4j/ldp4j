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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class URLSupport {

	private static final Logger LOGGER=LoggerFactory.getLogger(URLSupport.class);

	private static final String URL_PROTOCOL_HANDLER_PACKAGES = "java.protocol.handler.pkgs";

	private static final class ProtocolHandlerProviderUpdater implements PrivilegedAction<Void> {

		private final List<String> providers;

		private ProtocolHandlerProviderUpdater(List<String> providers) {
			this.providers = providers;
		}

		private String formatProtocolHandlerProviders(List<String> providers) {
			StringBuilder out=new StringBuilder();
			for(Iterator<String> it=providers.iterator();it.hasNext();) {
				String provider=it.next();
				out.append(provider);
				if(it.hasNext()) {
					out.append(" | ");
				}
			}
			return out.toString();
		}

		@Override
		public Void run() {
			final String value=formatProtocolHandlerProviders(providers);
			System.setProperty(URL_PROTOCOL_HANDLER_PACKAGES,value);
			trace("Protocol handler providers configured: "+value);
			return null;
		}
	}

	private static final class ProtocolHandlerProviderParser implements PrivilegedAction<List<String>> {
		private List<String> parseProtocolHandlerProviders(String protocolHandlerPackages) {
			List<String> providers=new ArrayList<String>();
			StringTokenizer packagePrefixIter=new StringTokenizer(protocolHandlerPackages.trim(), "|");
			while(packagePrefixIter.hasMoreTokens()) {
				String provider = packagePrefixIter.nextToken().trim();
				if(!providers.contains(provider)) {
					providers.add(provider);
				}
			}
			return providers;
		}

		@Override
		public List<String> run() {
			String protocolHandlerPackages = System.getProperty(URL_PROTOCOL_HANDLER_PACKAGES);
			if(protocolHandlerPackages==null) {
				protocolHandlerPackages="";
			}
			return parseProtocolHandlerProviders(protocolHandlerPackages);
		}
	}

	private URLSupport() {
	}

	private static void trace(String format, Object... args) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(String.format(format,args));
		}
	}

	static List<String> getProtocolHandlerProviders() {
		return AccessController.doPrivileged(new ProtocolHandlerProviderParser());
	}

	static void setProtocolHandlerProviders(final List<String> providers) {
		AccessController.doPrivileged(new ProtocolHandlerProviderUpdater(providers));
	}

}