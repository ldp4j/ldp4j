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

import org.ldp4j.application.config.Setting;

import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.*;

public final class SettingBuilder<T> {

	private static final class BeanSetting<T> implements Setting<T> {

		/**
		 *
		 */
		private static final long serialVersionUID = 873442054325464078L;

		private Class<? extends T> type;
		private String key;
		private String description;
		private T defaultValue;

		private BeanSetting(Class<? extends T> type) {
			this.type = type;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<? extends T> type() {
			return this.type;
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
			BeanSetting<T> beanSetting = new BeanSetting<T>(this.type);
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
			return Objects.hashCode(this.type,this.key);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Setting<?>)) {
				return false;
			}
			Setting<?> that=(Setting<?>)obj;
			return
				this.type==that.type() &&
				Objects.equal(this.key, that.getKey());
		}

	}

	private BeanSetting<T> setting;

	private SettingBuilder(Class<? extends T> type) {
		this.setting=new BeanSetting<T>(type);
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
		checkState(this.setting.defaultValue!=null,"Setting default value not defined");
		checkState(this.setting.key!=null,"Setting key not defined");
		return this.setting.clone();
	}

	public static <T> SettingBuilder<T> create(Class<? extends T> type) {
		checkNotNull(type,"Setting type cannot be null");
		return new SettingBuilder<T>(type);
	}

}
