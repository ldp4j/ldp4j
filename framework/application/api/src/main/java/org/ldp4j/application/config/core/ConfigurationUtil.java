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

import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.Setting;

import com.google.common.collect.Maps;

public final class ConfigurationUtil {

	private ConfigurationUtil() {
		// Prevent instantiation
	}

	public static Map<? extends Setting<?>, ? extends Object> toMap(Configuration config) {
		Map<Setting<?>,Object> rawSettings=Maps.newLinkedHashMap();
		for(Setting<?> setting:config.settings()) {
			rawSettings.put(setting, config.get(setting));
		}
		return rawSettings;
	}

	public static Map<Setting<?>,Object> verify(Map<? extends Setting<?>, ? extends Object> settings) {
		Map<Setting<?>,Object> invalidSettings=Maps.newLinkedHashMap();
		for(Entry<? extends Setting<?>, ? extends Object> entry:settings.entrySet()) {
			Setting<?> setting = entry.getKey();
			Object value = entry.getValue();
			if(!isValid(setting,value)) {
				invalidSettings.put(entry.getKey(), entry.getValue());
			}
		}
		return invalidSettings;
	}

	private static boolean isValid(Setting<?> key, Object value) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

}