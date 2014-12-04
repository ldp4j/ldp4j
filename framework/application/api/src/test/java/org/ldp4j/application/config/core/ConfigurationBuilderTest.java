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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.ldp4j.application.config.Configurable;
import org.ldp4j.application.config.Configurable.Option;
import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.ConfigurationException;
import org.ldp4j.application.config.MutableConfiguration;
import org.ldp4j.application.config.Setting;
import org.ldp4j.application.entity.spi.ObjectFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class ConfigurationBuilderTest  {

	public static class CustomSetting<T> implements Setting<T> {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private String key;
		private String description;
		private T defaultValue;

		public <E extends T> CustomSetting(String key, String description, E defaultValue) {
			this.key = key;
			this.description = description;
			this.defaultValue = defaultValue;
		}

		public static <T, E extends T> Setting<T> create(String key, String description, E defaultValue) {
			return new CustomSetting<T>(key,description,defaultValue);
		}

		@Override
		public String getKey() {
			return this.key;
		}

		@Override
		public String getDescription() {
			return this.description;
		}

		@Override
		public T getDefaultValue() {
			return this.defaultValue;
		}

		public String toString() {
			return
				Objects.
					toStringHelper(getClass()).
						omitNullValues().
						add("key",this.key).
						add("description",this.description).
						add("defaultValue",this.defaultValue).
						toString();
		}

	}

	private static class ListSetting<T> implements Setting<List<T>> {

		/**
		 *
		 */
		private static final long serialVersionUID = 3390265100159014151L;
		private final String key;
		private final String description;
		private List<T> defaultValue;

		private ListSetting(String key, String description, List<T> defaultValue) {
			this.key = key;
			this.description = description;
			this.defaultValue = ImmutableList.copyOf(defaultValue);
		}

		@Override
		public String getKey() {
			return this.key;
		}

		@Override
		public String getDescription() {
			return this.description;
		}

		@Override
		public List<T> getDefaultValue() {
			return this.defaultValue;
		}

		public String toString() {
			return
				Objects.
					toStringHelper(getClass()).
						omitNullValues().
						add("key",this.key).
						add("description",this.description).
						add("defaultValue",this.defaultValue).
						toString();
		}
		public static <T> ListSetting<T> create(String key, String description, T... values) {
			return new ListSetting<T>(key,description,Arrays.asList(values));
		}

	}

	public static interface MyConfiguration extends Configuration {

		@Option
		static final Setting<String> VERSION=CustomSetting.create("settings.version", "The version of the application", "1.0");

	}

	public interface MyConfigurable extends Configurable<MyConfiguration> {

		@Option
		static final ListSetting<String> OTHER_NAMES=ListSetting.create("mysettings.friend_names", "The names of my friends", "Miguel");

	}

	public interface SubClass extends MyConfigurable {

	}

	public static class Implementation implements SubClass {

		@Option
		static final Setting<String> PACKAGE_PRIVATE=CustomSetting.create("settings.package_private", "Package private setting", "Miguel");

		@Option
		private static final Setting<String> PRIVATE=CustomSetting.create("settings.private", "Private setting", "Miguel");

		@Option
		public final Setting<String> NON_STATIC=CustomSetting.create("settings.non_static", "Non static setting", "Miguel");

		@Option
		public static Setting<String> NON_FINAL=CustomSetting.create("settings.non_final", "Non final setting", "Miguel");

		@Option
		public static final Map<String,Set<Collection<Class<?>>>> NO_SETTING=Maps.newLinkedHashMap();

		@Option
		public static final ListSetting<String> UNSUPPORTED_PARAMETERIZED_TYPE_NF=ListSetting.create("settings.no_factory.unsupported_type.parameterized", "Unsupported setting (parameterized type)", "Miguel");

		@Option
		public static final Setting<RuntimeException> UNSUPPORTED_CLASS_TYPE_NF=CustomSetting.create("settings.no_factory.unsupported_type.class", "Unsupported setting (class)", new RuntimeException());

		@Option
		public static final Setting<? extends RuntimeException> UNSUPPORTED_WILDCARD_NF=CustomSetting.create("settings.no_factory.unsupported_type.wildcard", "Unsupported setting (wildcard)", new RuntimeException());

		@Option
		public static final Setting<String[]> UNSUPPORTED_CLASS_ARRAY_NF=CustomSetting.create("settings.no_factory.unsupported_type.class_array", "Unsupported setting (class array)", new String[]{});

		private static class ListString extends ArrayList<String> {
			private static final long serialVersionUID = 1L;
		}

		@Option
		public static final Setting<List<String>[]> UNSUPPORTED_PARAMETERIZED_ARRAY_NF=CustomSetting.create("settings.no_factory.unsupported_type.parameterized_array", "Unsupported setting (class array)", new ListString[0]);

		@Option(factory=CustomTypeFactory.class)
		public static final Setting<String> INVALID_FACTORY_TYPE=CustomSetting.create("settings.factory.invalid_type.class", "Invalid factory class", "Miguel");

		@Option(factory=CustomTypeFactory.class)
		public static final Setting<CustomType> SETTING_WITH_FACTORY=CustomSetting.create("settings.custom_type.withFactory", "Setting with custom type and factory", new CustomType());

		@Option
		public static final Setting<EnumType> ENUM_SETTING_NF=CustomSetting.create("settings.no_factory.enum_type", "Enum Setting without factory", EnumType.VALUE1);

		enum EnumType {
			VALUE1,
			VALUE2
		}

		public static class CustomType {

			public static CustomType valueOf(String string) {
				return new CustomTypeFactory().fromString(string);
			}

		}

		public static final class CustomTypeFactory implements ObjectFactory<CustomType> {

			@Override
			public Class<? extends CustomType> targetClass() {
				return CustomType.class;
			}

			@Override
			public CustomType fromString(String rawValue) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String toString(CustomType value) {
				// TODO Auto-generated method stub
				return null;
			}

		}


		private MyConfiguration configuration;

		@Override
		public Set<Setting<?>> settings() {
			return SettingRegistry.getSettings(getClass());
		}

		@Override
		public Class<? extends MyConfiguration> configType() {
			return MyConfiguration.class;
		}

		@Override
		public boolean canConfigure() {
			return true;
		}

		@Override
		public void configure(MyConfiguration configuration) throws ConfigurationException {
			this.configuration=configuration;
		}

	}

	@Test
	public void testSettingsManager$failures() {
		try {
			Set<Setting<?>> settings = SettingRegistry.getSettings(Implementation.class);
			for(Setting<?> setting:settings) {
				System.out.println(setting);
			}
		} catch (InvalidConfigurableDefinitionException e) {
			e.toString(System.err);
		}
	}

	@Test
	public void testCreateFromClass() throws Exception {
		Setting<Number> subclassSetting =
			SettingBuilder.
				create(Number.class).
					withKey("config.subclass").
					withDefaultValue(3).
					build();
		Setting<String[]> arraySetting =
				SettingBuilder.
					create(String[].class).
						withKey("config.array").
						withDefaultValue(new String[]{"1","2","3"}).
						build();
		Configuration build =
			ConfigurationBuilder.
				create().
					withUserSetting(subclassSetting,15).
					withUserSetting(arraySetting,new String[]{"4","5","6"}).
					build();
		System.out.println(subclassSetting.getKey()+" : "+build.get(subclassSetting));
		System.out.println(arraySetting.getKey()+" : "+Arrays.toString(build.get(arraySetting)));
	}

	@Test
	public void testCreateFromValue() throws Exception {
		List<String> defaultValue = Arrays.asList("1","2","3");
		List<String> customValue = Arrays.asList("1","2","3","4");
		Setting<List<String>> genericSetting =
				SettingBuilder.
					create(defaultValue).
						withKey("config.generic").
						withDescription("Generic List<String>").
						build();
		MutableConfiguration build =
			ConfigurationBuilder.
				createMutable().
					withUserSetting(genericSetting,customValue).
					build();
		List<String> v = build.get(genericSetting);
		System.out.println(genericSetting.getKey()+" : "+v);
		System.out.println(genericSetting.getKey()+" : "+v.get(0));
		System.out.println(build);
		List<String> newDefaultValue = Arrays.asList("4","5","6");
		build.set(genericSetting,newDefaultValue);
		System.out.println(genericSetting.getKey()+" : "+v);
		System.out.println(build);
	}

}
