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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.List;

import org.ldp4j.application.config.Configurable;
import org.ldp4j.application.config.Setting;
import org.ldp4j.application.entity.ObjectUtil;
import org.ldp4j.application.entity.spi.ObjectFactory;
import org.ldp4j.application.util.MetaClass;
import org.ldp4j.application.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

final class SettingController {

	private static final class PluggableObjectFactory<T> implements ObjectFactory<T> {

		private final Class<T> targetClass;

		private PluggableObjectFactory(Class<T> t) {
			this.targetClass = t;
		}

		@Override
		public Class<? extends T> targetClass() {
			return targetClass;
		}

		@Override
		public T fromString(String rawValue) {
			return ObjectUtil.fromString(this.targetClass, rawValue);
		}

		@Override
		public String toString(T value) {
			return ObjectUtil.toString(value);
		}

		public static <T> ObjectFactory<T> create(Class<T> settingClass) {
			return new PluggableObjectFactory<T>(settingClass);
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(SettingController.class);

	private final List<ConfigurationFailure> failures=Lists.newArrayList();
	private MetaClass metaClass;

	private Field field;
	private boolean _public;
	private boolean _static;
	private boolean _final;

	private Type settingType;
	private SettingDefinition<?> definition;
	private Setting<?> setting;

	private ObjectFactory<?> factory;

	private SettingController(MetaClass metaClass, Field field) {
		setMetaClass(metaClass);
		setField(field);
		setPublic(isPublic(field));
		setStatic(isStatic(field));
		setFinal(isFinal(field));
		setSettingType(getSettingType(field));
		try {
			setSetting(isStatic()?getSetting(field):null);
		} catch (IllegalStateException e) {
			logFailure(e.getMessage());
		}
		try {
			setObjectFactory(
				createObjectFactory(
					field.getAnnotation(Configurable.Option.class).factory(),
					settingType()));
		} catch (IllegalArgumentException e) {
			logFailure(e.getMessage());
		}
		if(isPublicConstant() && this.setting!=null && this.factory!=null) {
			@SuppressWarnings("unchecked")
			SettingDefinition<Object> definition =
				SettingDefinitionFactory.
					create(
						field.getGenericType(),
						(Setting<Object>)settingValue(),
						(ObjectFactory<Object>)objectFactory());
			setSettingDefinition(definition);
		}
	}

	boolean isStatic() {
		return this._static;
	}

	private static Type getSettingType(Field field) {
		MetaClass metaClass=MetaClass.create(field.getType(),field.getGenericType());
		return metaClass.resolve(Setting.class).typeArguments()[0];
	}

	private static Setting<?> getSetting(final Field field) {
		try {
			return AccessController.doPrivileged(
				new PrivilegedExceptionAction<Setting<?>>() {
					@Override
					public Setting<?> run() throws Exception {
						field.setAccessible(true);
						return (Setting<?>)field.get(null);
					}
				}
			);
		} catch (PrivilegedActionException e) {
			throw new IllegalStateException("Could not retrieve setting value",e.getCause());
		}
	}

	private static boolean isFinal(Field field) {
		int modifiers=field.getModifiers();
		boolean value = Modifier.isFinal(modifiers);
		return value;
	}

	private static boolean isStatic(Field field) {
		int modifiers=field.getModifiers();
		boolean value = Modifier.isStatic(modifiers);
		return value;
	}

	private static boolean isPublic(Field field) {
		int modifiers=field.getModifiers();
		boolean v = Modifier.isPublic(modifiers);
		return v;
	}

	private static ObjectFactory<?> createObjectFactory(final Class<? extends ObjectFactory<?>> factoryClass, Type settingType) {
		if(factoryClass!=Configurable.DEFAULT_OBJECT_FACTORY) {
			return SettingController.instantiateProvidedObjectFactory(factoryClass,settingType);
		} else {
			return SettingController.instantiateDefaultObjectFactory(settingType);
		}
	}

	private static ObjectFactory<?> instantiateDefaultObjectFactory(Type settingType) {
		if(!(settingType instanceof Class<?>)) {
			throw new IllegalArgumentException("Generic setting type "+Types.toString(settingType)+" is not supported");
		}
		Class<?> settingClass=(Class<?>)settingType;
		if(!ObjectUtil.isSupported(settingClass)) {
			throw new IllegalArgumentException("Setting type "+Types.toString(settingType)+" is not supported");
		}
		return PluggableObjectFactory.create(settingClass);
	}

	private static ObjectFactory<?> instantiateProvidedObjectFactory(
			final Class<? extends ObjectFactory<?>> factoryClass,
			Type settingType) {
		MetaClass fType=MetaClass.create(factoryClass);
		Type factoryType = fType.resolve(ObjectFactory.class).typeArguments()[0];
		if(!factoryType.equals(settingType)) {
			throw new IllegalArgumentException("Invalid factory: factory type "+Types.toString(factoryType)+" does not match setting type "+Types.toString(settingType));
		}
		try {
			return
				AccessController.doPrivileged(
				new PrivilegedExceptionAction<ObjectFactory<?>>() {
					@Override
					public ObjectFactory<?> run() throws Exception {
						ObjectFactory<?> factory=null;
							factory=factoryClass.newInstance();
						return factory;
					}
				}
			);
		} catch (PrivilegedActionException e) {
			throw new IllegalArgumentException("Could not instantiate factory '"+Types.toString(factoryClass)+"': "+e.getCause().getMessage(),e.getCause());
		}
	}

	private void setMetaClass(MetaClass metaClass) {
		this.metaClass = metaClass;
	}

	private void setField(Field field) {
		this.field=field;
	}

	private void setPublic(boolean value) {
		this._public=value;
		if(!value) {
			logFailure("is not public");
		}
	}

	private void setStatic(boolean value) {
		this._static=value;
		if(!value) {
			logFailure("is not static");
		}
	}

	private void setFinal(boolean value) {
		this._final=value;
		if(!value) {
			logFailure("is not final");
		}
	}

	private void setSetting(Setting<?> setting) {
		this.setting=setting;
	}

	private void setSettingType(Type type) {
		this.settingType=type;
	}

	private void setSettingDefinition(SettingDefinition<?> definition) {
		this.definition=definition;
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Found setting {}",toString());
		}
	}

	private void setObjectFactory(ObjectFactory<?> factory) {
		this.factory=factory;
	}

	private SettingController logFailure(String errorMessage, Object... args) {
		Throwable cause=null;
		if(args.length>0) {
			Object last = args[args.length-1];
			if(last instanceof Throwable) {
				cause=(Throwable)last;
			}
		}
		this.failures.add(DefaultConfigurationFailure.create(String.format(errorMessage, args), cause));
		return this;
	}

	Field field() {
		return this.field;
	}

	boolean isPublicConstant() {
		return this._public && this._static && this._final;
	}

	Type settingType() {
		return this.settingType;
	}

	Setting<?> settingValue() {
		return this.setting;
	}

	SettingDefinition<?> settingDefinition() {
		return this.definition;
	}

	ObjectFactory<?> objectFactory() {
		return this.factory;
	}

	boolean isValid() {
		return this.definition!=null;
	}

	List<ConfigurationFailure> configurationFailures() {
		return Collections.unmodifiableList(this.failures);
	}

	@Override
	public String toString() {
		return
			String.format(
				"%s.%s (%s)",
				Types.toString(this.metaClass.type()),
				field().getName(),
				Types.toString(field().getGenericType()));
	}

	static SettingController create(MetaClass metaClass, Field field) throws IllegalDeclarationException {
		SettingController result=null;
		if(field.getAnnotation(Configurable.Option.class)!=null) {
			if(Setting.class.isAssignableFrom(field.getType())) {
				result = new SettingController(metaClass,field);
			} else {
				throw new IllegalDeclarationException();
			}
		}
		return result;
	}

}