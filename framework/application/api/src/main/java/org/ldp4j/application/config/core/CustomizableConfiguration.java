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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.ConfigurationException;
import org.ldp4j.application.config.Setting;
import org.ldp4j.application.entity.spi.ObjectParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

class CustomizableConfiguration implements Configuration {

	private static final class LockManager {

		private final Map<SettingId,ReentrantLock> locks;

		LockManager() {
			this.locks=Maps.newLinkedHashMap();
		}

		<T> SettingDefinition<T> lock(Setting<T> setting) {
			SettingDefinition<T> definition = SettingRegistry.getSettingDefinition(setting);
			ReentrantLock settingLock = null;
			synchronized(this) {
				SettingId id=definition.id();
				settingLock=this.locks.get(id);
				if(settingLock==null) {
					settingLock=new ReentrantLock();
					this.locks.put(id, settingLock);
				}
			}
			settingLock.lock();
			return definition;
		}

		<T> void unlock(SettingDefinition<T> definition) {
			SettingId id=definition.id();
			ReentrantLock settingLock = null;
			synchronized(this) {
				settingLock=this.locks.get(id);
				if(settingLock!=null) {
					if(!settingLock.isHeldByCurrentThread()) {
						throw new IllegalStateException("Should not release a lock which is not held");
					}
					if(settingLock.getHoldCount()==1) {
						this.locks.remove(id);
					}
				}
			}
			settingLock.unlock();
		}

	}

	static interface PropertiesProvider<T> {

		T resource();
		Properties properties() throws IOException;

	}

	private final LockManager settingLockManager;
	private final Map<SettingId,Setting<?>> settings;
	private final Map<SettingId,Object> settingConfiguration;
	private final Map<SettingId,ConfigurationSource> settingSource;
	private final Logger logger;

	private Configuration userSettings;
	private Properties environmentProperties;
	private Properties systemProperties;
	private Properties customProperties;
	private List<CustomizableConfiguration.PropertiesProvider<?>> customResources;
	private Set<ConfigurationSource> sourcePrecedence;

	protected CustomizableConfiguration() {
		super();
		this.settingLockManager=new LockManager();
		this.logger=LoggerFactory.getLogger(getClass());
		this.settings=Maps.newLinkedHashMap();
		this.settingConfiguration=Maps.newLinkedHashMap();
		this.settingSource=Maps.newLinkedHashMap();
		setEnvironmentProperties(System.getenv());
		setSystemProperties(System.getProperties());
		setCustomProperties(new Properties());
	}

	protected CustomizableConfiguration(CustomizableConfiguration configuration) {
		super();
		this.settingLockManager=new LockManager();
		this.logger=LoggerFactory.getLogger(getClass());
		synchronized(configuration) {
			this.settings=Maps.newLinkedHashMap(configuration.settings);
			this.settingConfiguration=Maps.newLinkedHashMap(configuration.settingConfiguration);
			this.settingSource=Maps.newLinkedHashMap(configuration.settingSource);
			this.sourcePrecedence=Sets.newLinkedHashSet(configuration.sourcePrecedence);
			setEnvironmentProperties(configuration.environmentProperties);
			setSystemProperties(configuration.systemProperties);
			setCustomProperties(configuration.customProperties);
		}
	}

	private void setEnvironmentProperties(Map<String, String> getenv) {
		this.environmentProperties=new Properties();
		this.environmentProperties.putAll(getenv);
	}

	private void setEnvironmentProperties(Properties properties) {
		this.environmentProperties=new Properties();
		this.environmentProperties.putAll(properties);
	}

	private void setCustomProperties(Properties customProperties) {
		this.customProperties=new Properties();
		this.customProperties.putAll(customProperties);
	}

	private void setSystemProperties(Properties properties) {
		this.systemProperties=new Properties();
		this.systemProperties.putAll(properties);
	}

	private Properties flatten(List<CustomizableConfiguration.PropertiesProvider<?>> providers) throws ConfigurationException {
		Properties properties=new Properties();
		for(CustomizableConfiguration.PropertiesProvider<?> provider:providers) {
			try {
				properties.putAll(provider.properties());
			} catch (IOException e) {
				logger.debug("Could not load configuration resource '{}'",provider.resource());
				throw new ConfigurationException("Could not load configuration resource '"+provider.resource()+"'",e);
			}
		}
		return properties;
	}

	private <T> String toString(SettingDefinition<T> definition) {
		Setting<T> setting=definition.setting();
		StringBuilder builder=new StringBuilder();
		builder.append("'").append(setting.getKey()).append("'");
		if(setting.getDescription()!=null) {
			String description=setting.getDescription().trim();
			if(!description.isEmpty()) {
				builder.append(" (").append(description).append(")");
			}
		}
		return builder.toString();
	}

	private void clear() {
		this.settings.clear();
		this.settingSource.clear();
		this.settingConfiguration.clear();
	}

	private <T> T loadFromProperties(SettingDefinition<T> definition, ConfigurationSource source, Properties properties) {
		T loadedValue=null;
		String property = properties.getProperty(definition.setting().getKey());
		if(property!=null) {
			try {
				loadedValue=definition.factory().fromString(property);
				updateSettingDefinition(definition,loadedValue,source);
			} catch (ObjectParseException e) {
				logger.debug(String.format("Could not configure setting %s with value %s defined in source %s. Full stacktrace follows",toString(definition),source),e);
			}
		} else {
			logger.debug("Setting {} not defined in source {}",toString(definition),source);
		}
		return loadedValue;
	}

	private <T> T loadFromUserSettings(SettingDefinition<T> definition, ConfigurationSource source) {
		T loadedValue=this.userSettings.get(definition.setting());
		if(loadedValue==null) {
			String message =
				String.format(
					"No user provided value for setting %s",
					toString(definition)
				);
			this.logger.debug(message);
		} else {
			updateSettingDefinition(definition,loadedValue,source);
		}
		return loadedValue;
	}

	private <T> T loadSettingConfiguration(SettingDefinition<T> SettingDefinition) {
		T value=null;
		for(ConfigurationSource source:this.sourcePrecedence) {
			switch(source) {
				case CUSTOM_PROPERTIES:
					value=loadFromProperties(SettingDefinition,source,this.customProperties);
					break;
				case ENVIRONMENT_PROPERTIES:
					value=loadFromProperties(SettingDefinition,source,this.environmentProperties);
					break;
				case SYSTEM_PROPERTIES:
					value=loadFromProperties(SettingDefinition,source,this.systemProperties);
					break;
				case USER_SETTINGS:
					value=loadFromUserSettings(SettingDefinition,source);
					break;
				case DEFAULTS:
					value=loadDefaultSettingConfiguration(SettingDefinition);
				default:
					throw new IllegalStateException("Unsupported configuration source '"+source+"'");
			}
			if(value!=null) {
				break;
			}
		}
		return value;
	}

	private <T> T loadDefaultSettingConfiguration(SettingDefinition<T> definition) {
		T loadedValue=definition.setting().getDefaultValue();
		updateSettingDefinition(definition, loadedValue, ConfigurationSource.DEFAULTS);
		return loadedValue;
	}

	private synchronized <T> void updateSettingDefinition(SettingDefinition<T> definition, T value, ConfigurationSource source) {
		SettingId id=definition.id();
		this.settingConfiguration.put(id,value);
		this.settingSource.put(id,source);
		this.settings.put(id,definition.setting());
		this.logger.debug("Configured setting {} with value {} from {}",toString(definition),value,source);
	}

	protected synchronized void setSourcePrecedence(Iterable<ConfigurationSource> sourcePrecedence) {
		this.sourcePrecedence=Sets.newLinkedHashSet(sourcePrecedence);
		clear();
	}

	protected synchronized void setCustomProperties(List<CustomizableConfiguration.PropertiesProvider<?>> providers) throws ConfigurationException {
		this.customResources=Lists.newArrayList(providers);
		setCustomProperties(flatten(providers));
		clear();
	}

	protected synchronized void setUserSettings(Configuration userSettings) throws ConfigurationException {
		this.userSettings=new DefaultImmutableConfiguration(userSettings);
		clear();
	}

	protected synchronized <T> T getSettingConfiguration(SettingDefinition<T> definition) {
		SettingId id=definition.id();
		ConfigurationSource source = this.settingSource.get(id);
		// If the setting has been tried before...
		if(source!=null) {
			// ... return whatever was configured before ...
			Object cachedValue = this.settingConfiguration.get(id);
			// ... unless we had to resort to the default value
			if(source.equals(ConfigurationSource.DEFAULTS)) {
				if(logger.isDebugEnabled()) {
					if(!cachedValue.equals(definition.setting().getDefaultValue())) {
						logger.debug("Overriding default cached value '{}' of setting {} with value '{}'",cachedValue,toString(definition),definition.setting().getDefaultValue());
					}
				}
				// ... in which case we'll use the specific default value
				cachedValue=definition.setting().getDefaultValue();
			}
			logger.debug("Found value '{}' for setting {} defined by {}",cachedValue,toString(definition),source);
			return definition.tryCast(cachedValue);
			// TODO: Check what happens with the silent class cast exception that can be thrown from the tryCast method
		}
		// ... otherwise, return null
		return null;
	}

	protected synchronized <T> ConfigurationSource getSettingSource(SettingDefinition<T> definition) {
		return this.settingSource.get(definition.id());
	}

	protected <T> void update(Setting<? super T> setting, T value) {
		updateSettingDefinition(SettingRegistry.getSettingDefinition(setting),value,ConfigurationSource.USER_SETTINGS);
	}

	@Override
	public Set<Setting<?>> settings() {
		synchronized(this) {
			return Collections.unmodifiableSet(Sets.newLinkedHashSet(this.settings.values()));
		}
	}

	@Override
	public <T> T get(Setting<T> setting) {
		SettingDefinition<T> definition = this.settingLockManager.lock(setting);
		try {
			// If the setting value is cached...
			T value=getSettingConfiguration(definition);
			if(value==null) {
				// ... else, try and load it from the configuration specification
				value=loadSettingConfiguration(definition);
				if(value==null) {
					// ... if no configuration is specified, use the default
					value=loadDefaultSettingConfiguration(definition);
				}
			}
			return value;
		} finally {
			this.settingLockManager.unlock(definition);
		}
	}

	@Override
	public <T> boolean isSet(Setting<T> setting) {
		ConfigurationSource source=null;
		SettingDefinition<T> definition = this.settingLockManager.lock(setting);
		try {
			source=getSettingSource(definition);
			if(source==null) {
				loadSettingConfiguration(definition);
				source=getSettingSource(definition);
			}
		} finally {
			this.settingLockManager.unlock(definition);
		}
		return source!=null && !source.equals(ConfigurationSource.DEFAULTS);
	}

	@Override
	public void useDefaults() {
		synchronized(this) {
			clear();
		}
	}

	@Override
	public String toString() {
		ToStringHelper helper = Objects.
			toStringHelper(getClass()).
				add("sourcePrecedence", this.sourcePrecedence);
		synchronized(this) {
			helper.
				add("settings", this.settings).
				add("settingConfiguration", this.settingConfiguration).
				add("settingSource",this.settingSource);
		}
		if(this.sourcePrecedence.contains(ConfigurationSource.ENVIRONMENT_PROPERTIES)) {
			helper.add("environmentProperties",this.environmentProperties);
		}
		if(this.sourcePrecedence.contains(ConfigurationSource.SYSTEM_PROPERTIES)) {
			helper.add("systemProperties",this.systemProperties);
		}
		if(this.sourcePrecedence.contains(ConfigurationSource.CUSTOM_PROPERTIES)) {
			helper.add("customProperties",this.customProperties);
			helper.add("customResources",this.customResources);
		}
		if(this.sourcePrecedence.contains(ConfigurationSource.USER_SETTINGS)) {
			helper.add("userSettings",this.userSettings);
		}
		return helper.toString();
	}
}