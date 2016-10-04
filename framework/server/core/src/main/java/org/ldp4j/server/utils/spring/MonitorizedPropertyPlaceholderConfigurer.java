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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;

public class MonitorizedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	public static final String LDP4J_CONFIG_DUMP = "ldp4j.config.dump";

	public static final String LDP4J_CONFIG_MODULES = "ldp4j.config.modules";

	private ConfigurationSummary table;
	private String moduleName;

	public MonitorizedPropertyPlaceholderConfigurer() {
		table = new ConfigurationSummary("<unknown>");
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
		this.table=new ConfigurationSummary(moduleName);
	}

	public String getModuleName() {
		return moduleName;
	}

	// TODO: Enable selecting where the configuration should be logged to: System.out or a Logger
	@Override
	protected Properties mergeProperties() throws IOException {
		Properties result = super.mergeProperties();
		if(canShowConfiguration(getModuleName())) {
			table.setProperties(result);
			table.dump(System.out); // NOSONAR
		}
		return result;
	}

	private boolean canShowConfiguration(String moduleName) {
		boolean canShow=false;
		if(System.getProperty(LDP4J_CONFIG_DUMP)!=null ||
		   System.getenv().get(LDP4J_CONFIG_DUMP)!=null) {
			Set<String> modules=new HashSet<String>();
			splitModules(modules, System.getProperty(LDP4J_CONFIG_MODULES,""));
			splitModules(modules, System.getenv().get(LDP4J_CONFIG_MODULES));
			if(modules.contains(moduleName)) {
				canShow=true;
			}
		}
		return canShow;
	}

	private void splitModules(Set<String> modules, String binModules) {
		if(binModules!=null) {
			String[] rawModules = binModules.split(",");
			for(String rawModule:rawModules) {
				modules.add(rawModule.trim());
			}
		}
	}

	@Override
	public void setLocation(Resource location) {
		table.setResources(new Resource[]{location});
		super.setLocation(location);
	}

	@Override
	public void setLocations(Resource[] locations) {
		table.setResources(locations);
		super.setLocations(locations);
	}

}