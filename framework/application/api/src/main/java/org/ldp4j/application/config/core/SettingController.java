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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.List;

import org.ldp4j.application.config.Configurable;
import org.ldp4j.application.config.Setting;
import org.ldp4j.application.entity.ObjectUtil;
import org.ldp4j.application.entity.spi.ObjectFactory;
import org.ldp4j.application.entity.spi.ObjectTransformationException;
import org.ldp4j.application.util.MetaClass;
import org.ldp4j.application.util.TypeVisitor.TypeFunction;
import org.ldp4j.application.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

final class SettingController {

	private static final class EnumObjectFactory<S> implements ObjectFactory<S> {

		private final Method valueOf;
		private final Class<? extends S> targetClass;

		private EnumObjectFactory(Class<S> targetClass, Method valueOf) {
			this.valueOf = valueOf;
			this.targetClass = targetClass;
		}

		@Override
		public Class<? extends S> targetClass() {
			return targetClass;
		}

		@Override
		public S fromString(final String rawValue) {
			try {
				return AccessController.doPrivileged(
					new PrivilegedExceptionAction<S>() {
						@Override
						public S run() throws Exception {
							Object object = valueOf.invoke(null,rawValue);
							@SuppressWarnings("unchecked") // Guarded by Enum invariant
							S result = (S)object;
							return result;
						}
					}
				);
			} catch (PrivilegedActionException e) {
				throw new ObjectTransformationException("Could not parse enum "+Types.toString(targetClass()),e.getCause(),targetClass());
			}
		}

		@Override
		public String toString(S value) {
			return value.toString();
		}
	}

	private static final class SystemObjectFactory<S> implements ObjectFactory<S> {

		private final Class<S> t;

		private SystemObjectFactory(Class<S> t) {
			this.t = t;
		}

		@Override
		public Class<? extends S> targetClass() {
			return t;
		}

		@Override
		public S fromString(String rawValue) {
			return ObjectUtil.fromString(targetClass(), rawValue);
		}

		@Override
		public String toString(S value) {
			return ObjectUtil.toString(value);
		}
	}

	private final class DefaultObjectFactoryFunction extends TypeFunction<ObjectFactory<?>> {
		private DefaultObjectFactoryFunction() {
			super(null);
		}
		@Override
		protected <S, E extends Exception> ObjectFactory<?> visitClass(final Class<S> t, E exception) throws E {
			ObjectFactory<?> result=null;
			if(ObjectUtil.isSupported(t)) {
				result=new SettingController.SystemObjectFactory<S>(t);
			} else if(Enum.class.isAssignableFrom(t)) {
				result=
					AccessController.doPrivileged(
						new PrivilegedAction<ObjectFactory<?>>() {
							@Override
							public ObjectFactory<?> run() {
								try {
									final Method method = t.getMethod("valueOf",String.class);
									if(!Modifier.isPublic(method.getModifiers())) {
										String errorMessage = "Synthetic  "+Types.toString(t)+".valueOf("+Types.toString(String.class)+") could not be found";
										LOGGER.error(errorMessage);
										logFailure(errorMessage);
									} else if (!Modifier.isStatic(method.getModifiers())) {
										String errorMessage = "Synthetic  "+Types.toString(t)+".valueOf("+Types.toString(String.class)+") could not be found";
										LOGGER.error(errorMessage);
										logFailure(errorMessage);
									} else {
										return new SettingController.EnumObjectFactory<S>(t, method);
									}
								} catch (Exception e) {
									String errorMessage = "Synthetic  "+Types.toString(t)+".valueOf("+Types.toString(String.class)+") could not be found";
									LOGGER.error(errorMessage.concat(". Full stacktrace follows"),e);
									logFailure(errorMessage);
								}
								return null;
							}
						}
					);
			} else {
				// TODO: What to we do here?
			}
			return result;
		}

		public ObjectFactory<?> create() {
			return apply(settingType());
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(SettingController.class);

	private	final List<ConfigurationFailure> failures;
	private final MetaClass metaClass;

	private Field field;
	private Boolean validType;
	private Boolean publicConstant;
	private Type settingType;
	private SettingDefinition<?> manager;
	private Setting<?> setting;

	private SettingController(MetaClass metaClass, Field field) {
		this.metaClass = metaClass;
		this.field = field;
		this.failures=Lists.newArrayList();
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

	private boolean hasValidType() {
		if(this.validType==null) {
			this.validType = Setting.class.isAssignableFrom(this.field.getType());
			if(!validType) {
				logFailure("Does not implement "+Types.toString(Setting.class)+" ("+Types.toString(this.field.getGenericType())+")");
			}
		}
		return this.validType;
	}

	private boolean isPublicConstant() {
		if(this.publicConstant==null) {
			this.publicConstant=true;
			int modifiers = this.field.getModifiers();
			if(!Modifier.isPublic(modifiers)) {
				logFailure("is not public");
				this.publicConstant=false;
			}
			if(!Modifier.isStatic(modifiers)) {
				logFailure("is not static");
				this.publicConstant=false;
			}
			if(!Modifier.isFinal(modifiers)) {
				logFailure("is not final");
				this.publicConstant=false;
			}
		}
		return this.publicConstant;
	}

	private ObjectFactory<?> instantiateCustomObjectFactory(final Class<? extends ObjectFactory<?>> factoryClass) {
		return AccessController.doPrivileged(
			new PrivilegedAction<ObjectFactory<?>>() {
				@Override
				public ObjectFactory<?> run() {
					ObjectFactory<?> factory=null;
					try {
						factory=factoryClass.newInstance();
					} catch (InstantiationException e) {
						logFailure("Could not instantiate factory '"+Types.toString(factoryClass)+"': "+e.getCause().getMessage());
					} catch (IllegalAccessException e) {
						logFailure("Could not instantiate factory '"+Types.toString(factoryClass)+"': "+e.getCause().getMessage());
					}
					return factory;
				}
			}
		);
	}

	private Type settingType() {
		if(this.settingType==null) {
			MetaClass type=MetaClass.create(field.getType(),field.getGenericType());
			this.settingType=type.resolve(Setting.class).typeArguments()[0];
		}
		return this.settingType;
	}

	private ObjectFactory<?> createObjectFactory(Class<? extends ObjectFactory<?>> factoryClass) {
		ObjectFactory<?> factory=null;
		if(factoryClass!=Configurable.DEFAULT_OBJECT_FACTORY) {
			MetaClass fType=MetaClass.create(factoryClass);
			Type factoryType = fType.resolve(ObjectFactory.class).typeArguments()[0];
			if(!factoryType.equals(settingType())) {
				logFailure("Invalid factory: factory type "+Types.toString(factoryType)+" does not match setting type "+Types.toString(settingType()));
			} else {
				factory=instantiateCustomObjectFactory(factoryClass);
			}
		} else {
			factory=getDefaultObjectFactory();
		}
		return factory;
	}

	private ObjectFactory<?> getDefaultObjectFactory() {
		ObjectFactory<?> factory=new DefaultObjectFactoryFunction().create();
		if(factory==null) {
			logFailure("Setting type "+Types.toString(settingType())+" is not supported");
		}
		return factory;
	}

	private Setting<?> getSetting() {
		if(this.setting==null) {
			this.setting =
				AccessController.doPrivileged(
					new PrivilegedAction<Setting<?>>() {
						@Override
						public Setting<?> run() {
							try {
								return (Setting<?>)field.get(null);
							} catch (IllegalArgumentException e) {
								throw new InternalError("Should not fail when retrieving an static field");
							} catch (IllegalAccessException e) {
								throw new InternalError("Should not fail when retrieving an public field");
							}
						}
					}
				);
		}
		return this.setting;
	}

	boolean isValid() {
		return settingDefinition()!=null;
	}

	List<ConfigurationFailure> configurationFailures() {
		return Collections.unmodifiableList(this.failures);
	}

	@SuppressWarnings("unchecked") // Guarded by construction
	SettingDefinition<?> settingDefinition() {
		if(this.manager==null && failures.isEmpty()) {
			if(isPublicConstant()) {
				ObjectFactory<?> factory=createObjectFactory(this.field.getAnnotation(Configurable.Option.class).factory());
				if(factory!=null) {
					this.manager=SettingDefinitionFactory.create(this.field.getGenericType(),(Setting<Object>)getSetting(),(ObjectFactory<Object>)factory);
				}
				if(LOGGER.isDebugEnabled()) {
					if(factory!=null) {
						LOGGER.debug("Found setting {}",toString());
					} else {
						LOGGER.debug("Discarded setting {}: Could not create object factory",toString());
					}
				}
			}
			if(LOGGER.isDebugEnabled()) {
				if(!isPublicConstant()) {
					LOGGER.debug("Discarded setting {}: Not accessible ({})",toString(),Modifier.toString(this.field.getModifiers()));
				}
			}
		}
		return this.manager;
	}

	@Override
	public String toString() {
		return
			String.format(
				hasValidType()?
					"%s.%s (%s)":
					"%s.%s",
				Types.toString(this.metaClass.type()),
				this.field.getName(),
				Types.toString(this.field.getGenericType()));
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