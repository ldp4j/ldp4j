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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.config.core;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ldp4j.application.config.Configurable;
import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.util.MetaClass;
import org.ldp4j.application.util.Types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class InvalidSettingDefinitionException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 8048090612132552566L;

	private final String configurableClass;
	private final Multimap<String, String> configurableFailures;

	private final String configurationClass;
	private final Multimap<String, String> configurationFailures;

	public InvalidSettingDefinitionException(Map<MetaClass, Multimap<Field, ConfigurationFailure>> failures) {
		this.configurableClass = findClass(failures,Configurable.class);
		this.configurationClass = findClass(failures,Configuration.class);
		this.configurableFailures = flattenFailures(failures,Configurable.class);
		this.configurationFailures = flattenFailures(failures,Configuration.class);
	}

	private static String findClass(Map<MetaClass, Multimap<Field, ConfigurationFailure>> failures, Class<?> clazz) {
		Entry<MetaClass, Multimap<Field, ConfigurationFailure>> found = search(failures,clazz);
		if(found==null) {
			return null;
		}
		return Types.toString(found.getKey().type());
	}

	private static Multimap<String, String> flattenFailures(Map<MetaClass, Multimap<Field, ConfigurationFailure>> failures, Class<?> clazz) {
		Multimap<String, String> result=LinkedHashMultimap.create();
		Entry<MetaClass, Multimap<Field, ConfigurationFailure>> found = search(failures,clazz);
		if(found!=null) {
			for(Entry<Field,ConfigurationFailure> entry:found.getValue().entries()) {
				result.put(entry.getKey().getName(), entry.getValue().description());
			}
		}
		return result;
	}

	private static Entry<MetaClass, Multimap<Field, ConfigurationFailure>> search(Map<MetaClass, Multimap<Field, ConfigurationFailure>> failures,Class<?> clazz) {
		for(Entry<MetaClass, Multimap<Field, ConfigurationFailure>> entry:failures.entrySet()) {
			if(clazz.isAssignableFrom(entry.getKey().rawType())) {
				return entry;
			}
 		}
		return null;
	}

	private List<String> failures(String setting, Multimap<String, String> src) {
		Collection<String> collection = src.get(setting);
		if(collection==null) {
			return Collections.emptyList();
		} else {
			return ImmutableList.copyOf(collection);
		}
	}

	public String getConfigurableClass() {
		return configurableClass;
	}

	public Set<String> invalidConfigurableSettings() {
		return Collections.unmodifiableSet(this.configurableFailures.keySet());
	}

	public List<String> invalidConfiguratableSettingFailures(String setting) {
		return failures(setting, this.configurableFailures);
	}

	public String getConfigurationClass() {
		return configurationClass;
	}

	public Set<String> invalidConfigurationSettings() {
		return Collections.unmodifiableSet(this.configurationFailures.keySet());
	}

	public List<String> invalidConfigurationSettingFailures(String setting) {
		return failures(setting, this.configurationFailures);
	}

	public void toString(PrintStream out) {
		out.printf("Invalid definition {%n");
		toString(out, this.configurableClass, configurableFailures);
		toString(out, this.configurationClass, configurationFailures);
		out.printf("}%n");
	}

	private void toString(PrintStream out, String title, Multimap<String, String> src) {
		if(!src.isEmpty()) {
			out.printf("\t- %s settings:%n",title);
			for(Entry<String,Collection<String>> entry:src.asMap().entrySet()) {
				out.printf("\t\t- %s:%n",entry.getKey());
				for(String failure:entry.getValue()) {
					out.printf("\t\t\t+ %s%n",failure);
				}
			}
		}
	}

}
