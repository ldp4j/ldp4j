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
package org.ldp4j.server.utils.spring;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

final class CustomBundle implements Bundle {
	private final int bundleId;
	private final String bundleSymbolicName;

	CustomBundle(int bundleId, String bundleSymbolicName) {
		this.bundleId = bundleId;
		this.bundleSymbolicName = bundleSymbolicName;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration findEntries(String arg0, String arg1, boolean arg2) {
		return null;
	}

	@Override
	public BundleContext getBundleContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getBundleId() {
		return bundleId;
	}

	@Override
	public URL getEntry(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getEntryPaths(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Dictionary getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Dictionary getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLastModified() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceReference[] getRegisteredServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getResources(String arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceReference[] getServicesInUse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getState() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getSymbolicName() {
		return bundleSymbolicName;
	}

	@Override
	public boolean hasPermission(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class loadClass(String arg0) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start() throws BundleException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() throws BundleException {
		// TODO Auto-generated method stub

	}

	@Override
	public void uninstall() throws BundleException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update() throws BundleException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(InputStream arg0) throws BundleException {
		// TODO Auto-generated method stub

	}
}