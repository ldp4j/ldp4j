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

import java.lang.reflect.Type;

import org.ldp4j.application.config.Setting;
import org.ldp4j.application.entity.spi.ObjectFactory;
import org.ldp4j.application.util.MetaClass;

final class SettingDefinitionFactory {

	private static final class SimpleSettingDefinitionWrapper<T> extends AbstractSettingDefinition<T, Setting<T>> {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private SimpleSettingDefinitionWrapper(Setting<T> setting, Type type) {
			super(setting, type);
		}

		@Override
		public String toString(T value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public T valueOf(String rawValue) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private static final class PersistentSettingDefinitionWrapper<T> extends
			AbstractSettingDefinition<T, PersistentSetting<T>> {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private PersistentSettingDefinitionWrapper(
				PersistentSetting<T> setting, Type type) {
			super(setting, type);
		}

		@Override
		public String toString(T value) {
			return nativeSetting().toString(value);
		}

		@Override
		public T valueOf(String rawValue) {
			return nativeSetting().valueOf(rawValue);
		}
	}

	private static abstract class AbstractSettingDefinition<T, E extends Setting<T>> implements SettingDefinition<T> {

		private static final long serialVersionUID = 1L;

		private final E setting;
		private final Type type;

		private final SettingId id;

		private AbstractSettingDefinition(E setting, Type type) {
			this.setting = setting;
			this.type = type;
			this.id = SettingId.create(type,setting);
		}

		@Override
		public String getKey() {
			return this.setting.getKey();
		}

		@Override
		public String getDescription() {
			return this.setting.getDescription();
		}

		@Override
		public T getDefaultValue() {
			return this.setting.getDefaultValue();
		}

		@Override
		public SettingId id() {
			return this.id;
		}

		@Override
		public Type type() {
			return this.type;
		}

		@Override
		public E nativeSetting() {
			return this.setting;
		}

		@Override
		@SuppressWarnings("unchecked")
		public T tryCast(Object object) throws ClassCastException {
			return (T)object;
		}
	}
	private static final class CustomSettingManager<T> implements SettingDefinition<T> {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private final Setting<T> setting;
		private final Type type;
		private final SettingId id;
		private final ObjectFactory<T> factory;

		private CustomSettingManager(Type type, Setting<T> setting, ObjectFactory<T> factory) {
			this.setting = setting;
			this.factory = factory;
			this.type = type;
			this.id = SettingId.create(type,setting);
		}

		@Override
		public Setting<T> nativeSetting() {
			return this.setting;
		}

		@Override
		public SettingId id() {
			return id;
		}

		@Override
		public Type type() {
			return this.type;
		}



		@Override
		public String getKey() {
			return this.setting.getKey();
		}

		@Override
		public String getDescription() {
			return this.setting.getDescription();
		}

		@Override
		public T getDefaultValue() {
			return this.setting.getDefaultValue();
		}

		@Override
		public String toString(T value) {
			return this.factory.toString(value);
		}

		@Override
		public T valueOf(String value) {
			return this.factory.fromString(value);
		}

		@Override
		public T tryCast(Object object) throws ClassCastException {
			checkNotNull(object,"Object cannot be null");
			throw new UnsupportedOperationException("Method not implemented yet");
		}


	}

	private SettingDefinitionFactory() {
	}

	static <T> SettingDefinition<T> create(Type type, Setting<T> setting, ObjectFactory<T> factory) {
		return new CustomSettingManager<T>(type,setting,factory);
	}

	static <T> SettingDefinition<T> create(Setting<T> setting) {
		MetaClass metaClass=MetaClass.create(setting.getClass());
		Type type = metaClass.resolve(Setting.class).typeArguments()[0];
		if(setting instanceof PersistentSetting<?>) {
			return new PersistentSettingDefinitionWrapper<T>((PersistentSetting<T>)setting, type);
		} else {
			return new SimpleSettingDefinitionWrapper<T>(setting, type);
		}

	}

}
