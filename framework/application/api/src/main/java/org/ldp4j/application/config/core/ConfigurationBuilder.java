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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.ConfigurationException;
import org.ldp4j.application.config.ImmutableConfiguration;
import org.ldp4j.application.config.MutableConfiguration;
import org.ldp4j.application.config.Setting;
import org.ldp4j.application.config.core.CustomizableConfiguration.PropertiesProvider;

import com.google.common.collect.Lists;

public final class ConfigurationBuilder<T extends Configuration> {

	private static final class Customizer<T extends Configuration> {

		private final CustomizableConfiguration config;
		private final Class<? extends T> clazz;

		private Customizer(CustomizableConfiguration config, Class<? extends T> clazz) {
			this.config = config;
			this.clazz = clazz;
		}

		private T customize() {
			return this.clazz.cast(this.config);
		}

		private Customizer<T> withSourcePrecedence(List<ConfigurationSource> sourcePrecedence) {
			this.config.setSourcePrecedence(sourcePrecedence);
			return this;
		}

		private Customizer<T> withUserSettings(Configuration userSettings) throws ConfigurationException {
			this.config.setUserSettings(userSettings);
			return this;
		}

		private Customizer<T> withCustomProperties(List<PropertiesProvider<?>> propertiesProviders) throws ConfigurationException {
			this.config.setCustomProperties(propertiesProviders);
			return this;
		}

		private static <T extends Configuration> Customizer<T> create(CustomizableConfiguration config, Class<? extends T> type) {
			checkArgument(type.isInstance(config));
			return new Customizer<T>(config,type);
		}

	}

	private static final class NativePropertiesProvider implements CustomizableConfiguration.PropertiesProvider<Properties> {

		private final Properties customProperties;

		private NativePropertiesProvider(Properties customProperties) {
			this.customProperties=new Properties();
			this.customProperties.putAll(customProperties);;
		}

		@Override
		public Properties resource() {
			return this.customProperties;
		}

		@Override
		public Properties properties() throws IOException {
			return this.customProperties;
		}

		@Override
		public String toString() {
			return this.customProperties.toString();
		}

	}

	private static abstract class LocalizablePropertiesProvider<T> implements CustomizableConfiguration.PropertiesProvider<T>{

		private Properties properties;
		private IOException failure;
		private T resource;

		private LocalizablePropertiesProvider(T resource) {
			this.resource = resource;
		}

		protected abstract URL location() throws IOException;

		private synchronized void loadResource() {
			if(this.properties!=null || this.failure!=null) {
				return;
			}
			InputStream is=null;
			try {
				is=location().openStream();
				Properties properties=new Properties();
				properties.load(is);
				this.properties=properties;
			} catch (IOException e) {
				this.failure=e;
			} finally {
				closeQuietly(is);
			}
		}

		private void closeQuietly(InputStream is) {
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					// We can do nothing but log the exception
				}
			}
		}

		@Override
		public final Properties properties() throws IOException {
			loadResource();
			if(this.failure!=null) {
				throw failure;
			}
			return this.properties;
		}

		@Override
		public final T resource() {
			return this.resource;
		}

		@Override
		public String toString() {
			return this.resource.toString();
		}

	}

	private static final class FilePropertiesProvider extends LocalizablePropertiesProvider<File> {

		private FilePropertiesProvider(File resource) {
			super(resource);
		}

		@Override
		protected URL location() throws IOException {
			return resource().toURI().toURL();
		}

		@Override
		public String toString() {
			return resource().getAbsolutePath();
		}

	}

	private static final class URIPropertiesProvider extends LocalizablePropertiesProvider<URI> {

		private URIPropertiesProvider(URI resource) {
			super(resource);
		}

		@Override
		protected URL location() throws IOException {
			return resource().toURL();
		}

	}

	private static final class URLPropertiesProvider extends LocalizablePropertiesProvider<URL> {

		private URLPropertiesProvider(URL resource) {
			super(resource);
		}

		@Override
		protected URL location() {
			return resource();
		}

	}

	private final List<ConfigurationSource> sourcePrecedence;
	private final MutableConfiguration userSettings;
	private final List<PropertiesProvider<?>> propertiesProviders;
	private final Customizer<? extends T> customizer;

	private ConfigurationBuilder(Customizer<? extends T> customizer) {
		this.customizer = customizer;
		this.userSettings=new DefaultMutableConfiguration();
		this.sourcePrecedence=Lists.newArrayList();
		this.propertiesProviders=Lists.newArrayList();
	}

	private Customizer<? extends T> customizer() {
		return this.customizer;
	}

	private void addConfigurationSource(ConfigurationSource source) {
		this.sourcePrecedence.add(source);
	}

	private void addResourceProvider(PropertiesProvider<?> provider) {
		this.propertiesProviders.add(provider);
		addConfigurationSource(ConfigurationSource.CUSTOM_PROPERTIES);
	}

	public ConfigurationBuilder<T> withEnvironmentProperties() {
		addConfigurationSource(ConfigurationSource.ENVIRONMENT_PROPERTIES);
		return this;
	}

	public ConfigurationBuilder<T> withSystemProperties() {
		addConfigurationSource(ConfigurationSource.SYSTEM_PROPERTIES);
		return this;
	}

	public ConfigurationBuilder<T> withCustomProperties(File file) {
		checkNotNull(file,"File cannot be null");
		checkArgument(file.isFile(),"Path %s should be a file",file.getAbsolutePath());
		checkArgument(file.canRead(),"File %s cannot be read",file.getAbsolutePath());
		addResourceProvider(new FilePropertiesProvider(file));
		return this;
	}

	public ConfigurationBuilder<T> withCustomProperties(URL resource) {
		checkNotNull(resource,"Resource cannot be null");
		addResourceProvider(new URLPropertiesProvider(resource));
		return this;
	}

	public ConfigurationBuilder<T> withCustomProperties(URI resource) {
		checkNotNull(resource,"Resource cannot be null");
		addResourceProvider(new URIPropertiesProvider(resource));
		return this;
	}

	public ConfigurationBuilder<T> withCustomProperties(Properties properties) {
		checkNotNull(properties,"Properties cannot be null");
		addResourceProvider(new NativePropertiesProvider(properties));
		return this;
	}

	public <E> ConfigurationBuilder<T> withUserSetting(Setting<? super E> setting, E value) throws ConfigurationException {
		checkNotNull(setting,"Setting cannot be null");
		checkNotNull(value,"Setting value cannot be null");
		this.userSettings.set(setting, value);
		addConfigurationSource(ConfigurationSource.USER_SETTINGS);
		return this;
	}

	public ConfigurationBuilder<T> withCustomSourcePrecedence(ConfigurationSource first, ConfigurationSource... rest) {
		List<ConfigurationSource> sourcePrecedence=Lists.newArrayList(first);
		sourcePrecedence.addAll(Arrays.asList(rest));
		this.sourcePrecedence.clear();
		this.sourcePrecedence.addAll(sourcePrecedence);
		return this;
	}

	public ConfigurationBuilder<T> withDefaultSourcePrecedence() {
		return
			withCustomSourcePrecedence(
				ConfigurationSource.USER_SETTINGS,
				ConfigurationSource.SYSTEM_PROPERTIES,
				ConfigurationSource.CUSTOM_PROPERTIES);
	}

	public T build() throws ConfigurationException {
		return
			customizer().
				withSourcePrecedence(this.sourcePrecedence).
				withUserSettings(this.userSettings).
				withCustomProperties(this.propertiesProviders).
				customize();
	}

	public static ConfigurationBuilder<Configuration> create() {
		return
			new ConfigurationBuilder<Configuration>(
				Customizer.create(
					new CustomizableConfiguration(),
					Configuration.class));
	}

	public static ConfigurationBuilder<MutableConfiguration> createMutable() {
		return
			new ConfigurationBuilder<MutableConfiguration>(
				Customizer.create(
					new CustomizableMutableConfiguration(),
					MutableConfiguration.class));
	}

	public static ConfigurationBuilder<ImmutableConfiguration> createImmutable() {
		return
			new ConfigurationBuilder<ImmutableConfiguration>(
				Customizer.create(
					new CustomizableImmutableConfiguration(),
					ImmutableConfiguration.class));
	}
}