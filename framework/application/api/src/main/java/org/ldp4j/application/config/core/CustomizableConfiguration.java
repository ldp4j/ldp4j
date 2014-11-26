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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.ConfigurationException;
import org.ldp4j.application.config.Setting;
import org.ldp4j.application.entity.ObjectUtil;
import org.ldp4j.application.entity.spi.ObjectParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

class CustomizableConfiguration implements Configuration {

	private static final class SettingId {

		private final String typeName;
		private final String key;

		private SettingId(String typeName, String key) {
			this.typeName = typeName;
			this.key = key;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.typeName,this.key);
		}

		@Override
		public boolean equals(Object obj) {
			boolean result = false;
			if(obj instanceof SettingId) {
				SettingId that=(SettingId)obj;
				result=
					Objects.equal(this.typeName,that.typeName) &&
					Objects.equal(this.key, that.key);
			}
			return result;
		}

		@Override
		public String toString() {
			return
				Objects.
					toStringHelper(getClass()).
						add("typeName", this.typeName).
						add("key",this.key).
						toString();
		}

		static SettingId create(Setting<?> setting) {
			return new SettingId(setting.type().getName(),setting.getKey());
		}

	}

	private static final class LockManager {

		private final Map<SettingId,ReentrantLock> locks;

		LockManager() {
			this.locks=Maps.newLinkedHashMap();
		}

		void lock(Setting<?> setting) {
			SettingId id=SettingId.create(setting);
			ReentrantLock settingLock = null;
			synchronized(this) {
				settingLock=this.locks.get(id);
				if(settingLock==null) {
					settingLock=new ReentrantLock();
					this.locks.put(id, settingLock);
				}
			}
			settingLock.lock();
		}

		void unlock(Setting<?> setting) {
			SettingId id=SettingId.create(setting);
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

	private final LockManager manager;
	private final Map<SettingId,Setting<?>> settings;
	private final Map<SettingId,Object> settingConfiguration;
	private final Map<SettingId,ConfigurationSource> settingSource;
	private final Logger logger;

	private Map<SettingId, Object> userSettings;
	private Properties environmentProperties;
	private Properties systemProperties;
	private Properties customProperties;
	private List<CustomizableConfiguration.PropertiesProvider<?>> customResources;
	private Set<ConfigurationSource> sourcePrecedence;

	protected CustomizableConfiguration() {
		super();
		this.manager=new LockManager();
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
		this.manager=new LockManager();
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

	private <T> String toString(Setting<T> setting) {
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

	private <T> T loadFromProperties(Setting<T> setting, ConfigurationSource source, Properties properties) {
		T loadedValue=null;
		String property = properties.getProperty(setting.getKey());
		if(property!=null) {
			try {
				loadedValue=ObjectUtil.fromString(setting.type(),property);
				updateSetting(setting,source,loadedValue);
			} catch (ObjectParseException e) {
				logger.debug(String.format("Could not configure setting %s with value %s defined in source %s. Full stacktrace follows",toString(setting),source),e);
			}
		} else {
			logger.debug("Setting {} not defined in source {}",toString(setting),source);
		}
		return loadedValue;
	}

	private <T> T loadFromUserSettings(Setting<T> setting, ConfigurationSource source) {
		T loadedValue=null;
		SettingId id = SettingId.create(setting);
		Object tmp=this.userSettings.get(id);
		if(setting.type().isInstance(tmp)) {
			loadedValue=setting.type().cast(tmp);
			updateSetting(setting,source,loadedValue);
		} else {
			String errorMessage =
				String.format(
					"Invalid user provided value '%s' for setting %s. Expected a instance of type '%s' but got an instance of type ''",
					tmp,
					toString(setting),
					setting.type().getName(),
					tmp.getClass().getName());
			this.logger.error(errorMessage);
			throw new IllegalStateException(errorMessage);
		}
		return loadedValue;
	}

	private <T> T loadSettingConfiguration(Setting<T> setting) {
		T value=null;
		for(ConfigurationSource source:this.sourcePrecedence) {
			switch(source) {
				case CUSTOM_PROPERTIES:
					value=loadFromProperties(setting,source,this.customProperties);
					break;
				case ENVIRONMENT_PROPERTIES:
					value=loadFromProperties(setting,source,this.environmentProperties);
					break;
				case SYSTEM_PROPERTIES:
					value=loadFromProperties(setting,source,this.systemProperties);
					break;
				case USER_SETTINGS:
					value=loadFromUserSettings(setting,source);
					break;
				case DEFAULTS:
					value=loadDefaultSettingConfiguration(setting);
				default:
					throw new IllegalStateException("Unsupported configuration source '"+source+"'");
			}
			if(value!=null) {
				break;
			}
		}
		return value;
	}

	private <T> T loadDefaultSettingConfiguration(Setting<T> setting) {
		T loadedValue=setting.getDefaultValue();
		updateSetting(setting, ConfigurationSource.DEFAULTS, loadedValue);
		return loadedValue;
	}

	private synchronized <T> void updateSetting(Setting<T> setting, ConfigurationSource source, T value) {
		SettingId id=SettingId.create(setting);
		this.settingConfiguration.put(id,value);
		this.settingSource.put(id,source);
		this.settings.put(id,setting);
		logger.debug("Configured setting {} with value {} from source {}",setting,value,source);
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

	protected synchronized void setUserSettings(Map<Setting<?>, Object> userSettings) throws ConfigurationException {
		this.userSettings=Maps.newLinkedHashMap();
		for(Entry<Setting<?>, Object> entry:userSettings.entrySet()) {
			Setting<?> setting = entry.getKey();
			Object value = entry.getValue();
			if(!setting.type().isInstance(value)) {
				throw new ConfigurationException("Invalid value '"+value+"' for setting "+setting.getKey()+" ("+setting.getDescription()+") ");
			}
			this.userSettings.put(SettingId.create(setting), value);
		}
		clear();
	}

	protected synchronized <T> T getSettingConfiguration(Setting<T> setting) {
		SettingId id=SettingId.create(setting);
		ConfigurationSource source = this.settingSource.get(id);
		// If the setting has been tried before...
		if(source!=null) {
			// ... return whatever was configured before ...
			Object cachedValue = this.settingConfiguration.get(id);
			// ... unless we had to resort to the default value
			if(source.equals(ConfigurationSource.DEFAULTS)) {
				if(logger.isDebugEnabled()) {
					if(!cachedValue.equals(setting.getDefaultValue())) {
						logger.debug("Overriding default cached value '{}' of setting {} with value '{}'",cachedValue,toString(setting),setting.getDefaultValue());
					}
				}
				// ... in which case we'll use the specific default value
				cachedValue=setting.getDefaultValue();
			}
			logger.debug("Found value '{}' for setting {} defined by source {}",cachedValue,toString(setting),source);
			return setting.type().cast(cachedValue);
		}
		// ... otherwise, return null
		return null;
	}

	protected synchronized <T> ConfigurationSource getSettingSource(Setting<T> setting) {
		return this.settingSource.get(SettingId.create(setting));
	}

	protected <T> void update(Setting<T> setting, T value) {
		updateSetting(setting,ConfigurationSource.USER_SETTINGS,value);
	}

	@Override
	public Set<Setting<?>> settings() {
		synchronized(this) {
			return Collections.unmodifiableSet(Sets.newLinkedHashSet(this.settings.values()));
		}
	}

	@Override
	public <T> T get(Setting<T> setting) {
		this.manager.lock(setting);
		try {
			// If the setting value is cached...
			T value=getSettingConfiguration(setting);
			if(value==null) {
				// ... else, try and load it from the configuration specification
				value=loadSettingConfiguration(setting);
				if(value==null) {
					// ... if no configuration is specified, use the default
					value=loadDefaultSettingConfiguration(setting);
				}
			}
			return value;
		} finally {
			this.manager.unlock(setting);
		}
	}

	@Override
	public <T> boolean isSet(Setting<T> setting) {
		ConfigurationSource source=null;
		this.manager.lock(setting);
		try {
			source=getSettingSource(setting);
			if(source==null) {
				loadSettingConfiguration(setting);
				source=getSettingSource(setting);
			}
		} finally {
			this.manager.unlock(setting);
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