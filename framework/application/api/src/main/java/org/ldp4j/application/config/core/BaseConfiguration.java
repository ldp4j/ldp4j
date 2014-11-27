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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

abstract class BaseConfiguration implements Serializable, Configuration {

	/**
	 *
	 */
	private static final long serialVersionUID = -8811212400844264025L;

	/**
	 * A map containing mappings from settings to their values.
	 */
	private final ConcurrentMap<Setting<?>, Object> settings;

	/**
	 * The logger used for logging events
	 */
	protected final Logger logger;

	protected BaseConfiguration() {
		super();
		this.settings=Maps.newConcurrentMap();
		this.logger=LoggerFactory.getLogger(this.getClass());
	}

	private BaseConfiguration(boolean verified, Map<? extends Setting<?>, ? extends Object> settings) {
		this();
		if(!verified) {
			Map<Setting<?>, Object> invalidSettings = ConfigurationUtil.verify(settings);
			if(!invalidSettings.isEmpty()) {
				logger.trace("Could not initialize configuration with invalid settings {}",invalidSettings);
				throw new IllegalArgumentException("Invalid settings: "+invalidSettings);
			}
		}
		this.settings.putAll(settings);
	}

	protected BaseConfiguration(Map<? extends Setting<?>, ? extends Object> settings) {
		this(false,settings);
	}

	protected BaseConfiguration(BaseConfiguration config) {
		this(true,config.settings);
	}

	protected BaseConfiguration(Configuration config) {
		this(true,ConfigurationUtil.toMap(config));
	}

	/**
	 * Sets a {@link Setting} to have a new value. If the value is null, the
	 * setting is removed and the default will be used instead.
	 *
	 * @param setting
	 *        The setting to set a new value for.
	 * @param value
	 *        The value for the setting, or null to reset the setting to use
	 *        the default value.
	 */
	final <T> void update(Setting<T> setting, T value) {
		if (value == null) {
			this.settings.remove(setting);
		} else {
			Object previous=this.settings.put(setting,value);
			if(previous!=null && this.logger.isTraceEnabled()) {
				this.logger.trace("Overriding previous setting '{}' for '{}'",previous,setting.getKey());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Setting<?>> settings() {
		return ImmutableSet.copyOf(this.settings.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T get(Setting<T> setting) {
		checkNotNull(setting,"Setting cannot be null");
		Object result = this.settings.get(setting);
		if(result!=null) {
			return setting.type().cast(result);
		}
		return setting.getDefaultValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> boolean isSet(Setting<T> setting) {
		return this.settings.containsKey(setting);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void useDefaults() {
		this.settings.clear();
	}

}