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
import static com.google.common.base.Preconditions.checkState;

import org.ldp4j.application.config.Setting;

import com.google.common.base.Objects;

public final class SettingBuilder<T> {

	private static final class ImmutableSetting<T> implements Setting<T> {

		private static final long serialVersionUID = 5110195960337804950L;

		private final Setting<T> delegate;

		private ImmutableSetting(Setting<T> delegate) {
			this.delegate = delegate;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getKey() {
			return this.delegate.getKey();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getDescription() {
			return this.delegate.getDescription();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T getDefaultValue() {
			return this.delegate.getDefaultValue();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return this.delegate.hashCode();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			return this.delegate.equals(obj);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return
				Objects.
					toStringHelper(getClass()).
						add("delegate",this.delegate).
						toString();
		}

	}

	private static final class MutableSetting<T> implements Setting<T> {

		private static final long serialVersionUID = 1L;

		private String key;
		private String description;
		private T defaultValue;

		private MutableSetting() {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getKey() {
			return this.key;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getDescription() {
			return this.description;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T getDefaultValue() {
			return this.defaultValue;
		}

		protected Setting<T> clone() {
			MutableSetting<T> beanSetting = new MutableSetting<T>();
			beanSetting.key=this.key;
			beanSetting.defaultValue=this.defaultValue;
			beanSetting.description=this.description;
			return beanSetting;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(this.key,this.defaultValue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj==this) return true;
			if(obj==null) return false;
			if(!(obj instanceof Setting<?>)) {
				return false;
			}
			Setting<?> that=(Setting<?>)obj;
			return
				Objects.equal(this.key, that.getKey()) &&
				Objects.equal(this.defaultValue, that.getDefaultValue());
		}

	}

	private MutableSetting<T> setting;

	private SettingBuilder() {
		this.setting=new MutableSetting<T>();
	}

	public SettingBuilder<T> withKey(String key) {
		checkNotNull(key,"Setting key cannot be null");
		this.setting.key=key;
		return this;
	}

	public SettingBuilder<T> withDescription(String description) {
		this.setting.description=description;
		return this;
	}

	public SettingBuilder<T> withDefaultValue(T defaultValue) {
		checkNotNull(defaultValue,"Setting default value cannot be null");
		this.setting.defaultValue=defaultValue;
		return this;
	}

	public Setting<T> build() {
		checkState(this.setting.key!=null,"Setting key not defined");
		checkState(this.setting.defaultValue!=null,"Setting default value not defined");
		return new ImmutableSetting<T>(this.setting.clone());
	}

	public static <T> SettingBuilder<T> create(Class<? extends T> type) {
		checkNotNull(type,"Setting type cannot be null");
		return new SettingBuilder<T>();
	}

	public static <T> SettingBuilder<T> create(T value) {
		SettingBuilder<T> builder = new SettingBuilder<T>();
		return builder.withDefaultValue(value);
	}

}
