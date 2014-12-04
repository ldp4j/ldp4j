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
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ldp4j.application.config.Configurable;
import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.Setting;
import org.ldp4j.application.util.MetaClass;
import org.ldp4j.application.util.TypeVisitor.TypeFunction;
import org.ldp4j.application.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;


public final class SettingRegistry {

	private static final class Transaction {

		private final List<SettingDefinition<?>> definitions;
		private final Map<MetaClass,Multimap<Field,ConfigurationFailure>> failures;

		private Transaction() {
			this.definitions=Lists.newArrayList();
			this.failures=Maps.newLinkedHashMap();
		}

		void registerFailures(MetaClass metaClass, Field field, List<ConfigurationFailure> failures) {
			Multimap<Field, ConfigurationFailure> metaClassFailures = this.failures.get(metaClass);
			if(metaClassFailures==null) {
				metaClassFailures=LinkedHashMultimap.create();
				this.failures.put(metaClass, metaClassFailures);
			}
			metaClassFailures.putAll(field, failures);
		}

		void registerDefinition(SettingDefinition<?> definition) {
			this.definitions.add(definition);
		}

		Set<Setting<?>> commit(Class<?> clazz) throws InvalidSettingDefinitionException {
			Set<SettingId> ids=Sets.newLinkedHashSet();
			Set<Setting<?>> settings=Sets.newLinkedHashSet();
			synchronized(SETTING_MANAGERS) {
				for(SettingDefinition<?> definitions:this.definitions) {
					ids.add(definitions.id());
					settings.add(definitions.setting());
					SettingRegistry.registerSettingDefinition(definitions);
				}
				SettingRegistry.registerClassSettings(clazz, ids);
			}
			if(!failures.isEmpty()) {
				throw new InvalidSettingDefinitionException(failures);
			}
			return settings;
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(SettingRegistry.class);

	private static final Map<Class<?>,Set<SettingId>> LOADED_SETTINGS=Maps.newIdentityHashMap();
	private static final Map<SettingId,SettingDefinition<?>> SETTING_MANAGERS=Maps.newIdentityHashMap();
	private static final Map<Setting<?>,SettingId> SETTING_IDS=Maps.newIdentityHashMap();

	private SettingRegistry() {
	}

	private static void registerSettingDefinition(SettingDefinition<?> manager) {
		LOGGER.debug("Registering setting definition identified by {} for setting with hash {}...",manager.id(),Integer.toHexString(manager.setting().hashCode()));
		SETTING_MANAGERS.put(manager.id(), manager);
		SETTING_IDS.put(manager.setting(),manager.id());
	}

	private static void registerClassSettings(Class<?> clazz, Set<SettingId> ids) {
		LOGGER.debug("Registered setting definitions {} for class {}",ids,Types.toString(clazz));
		LOADED_SETTINGS.put(clazz, ids);
	}

	private static void loadSettings(MetaClass metaClass, Transaction transaction) {
		LOGGER.info("Loading settings for '{}'",Types.toString(metaClass.type()));
		for(Field field:metaClass.rawType().getDeclaredFields()) {
			try {
				SettingController controller = SettingController.create(metaClass,field);
				if(controller!=null) {
					if(controller.isValid()) {
						transaction.registerDefinition(controller.settingDefinition());
					} else {
						transaction.registerFailures(metaClass,field,controller.configurationFailures());
					}
				}
			} catch (IllegalDeclarationException e) {
				String errorMessage = String.format("Field %s.%s is annotated as an option but does not implement %s (%s)",Types.toString(metaClass.type()),field.getName(),Types.toString(Setting.class),Types.toString(field.getGenericType()));
				LOGGER.warn(errorMessage);
				transaction.registerFailures(metaClass,field,Arrays.asList(DefaultConfigurationFailure.create(errorMessage,e)));
			}
		}
	}

	@SuppressWarnings("unchecked") // Guarded by class invariant
	public static <T> SettingDefinition<T> getSettingDefinition(Setting<T> setting) {
		synchronized(SETTING_MANAGERS) {
			SettingId id=SETTING_IDS.get(setting);
			if(id==null) {
				LOGGER.debug("No definition registered for setting '{}'",setting);
				SettingDefinition<T> definition = SettingDefinitionFactory.create(setting);
				registerSettingDefinition(definition);
				return definition;
			} else {
				LOGGER.debug("Definition {} already registered for setting '{}'",id,setting);
				return (SettingDefinition<T>)SETTING_MANAGERS.get(id);
			}
		}
	}

	public static <C extends Configuration, T extends Configurable<C>> Set<Setting<?>> getSettings(Class<? extends T> clazz) throws InvalidSettingDefinitionException {
		synchronized(clazz) {
			Set<SettingId> ids=LOADED_SETTINGS.get(clazz);
			if(ids!=null) {
				Builder<Setting<?>> builder = ImmutableSet.builder();
				synchronized(SETTING_MANAGERS) {
					for(SettingId id:ids) {
						builder.add(SETTING_MANAGERS.get(id).setting());
					}
				}
				return builder.build();
			} else {
				Transaction transaction=new Transaction();
				MetaClass configurableClass=MetaClass.create(clazz);
				MetaClass configurationClass=MetaClass.create(configurableClass.resolve(Configurable.class).typeArguments()[0]);
				loadSettings(configurableClass,transaction);
				loadSettings(configurationClass,transaction);
				return Collections.unmodifiableSet(transaction.commit(clazz));
			}
		}
	}

	@SuppressWarnings("unused")
	private static boolean isResolved(Type type) {
		TypeFunction<Boolean> resolver = new TypeFunction<Boolean>() {
			private Boolean areResolved(Type[] types) {
				boolean result=true;
				for(int i=0;i<types.length && result;i++) {
					result=apply(types[i]);
				}
				return result;
			}
			@Override
			protected <S, E extends Exception> Boolean visitClass(Class<S> t, E exception) throws E {
				return true;
			}
			@Override
			protected <E extends Exception> Boolean visitGenericArrayType(GenericArrayType t, E exception) throws E {
				return apply(t.getGenericComponentType());
			}
			@Override
			protected <E extends Exception> Boolean visitParameterizedType(ParameterizedType t, E exception) throws E {
				return areResolved(t.getActualTypeArguments());
			}
			@Override
			protected <D extends GenericDeclaration, E extends Exception> Boolean visitTypeVariable(TypeVariable<D> t, E exception) throws E {
				return false;
			}
			@Override
			protected <E extends Exception> Boolean visitWildcardType(WildcardType t, E exception) throws E {
				return areResolved(t.getLowerBounds()) && areResolved(t.getUpperBounds());
			}
		};
		return resolver.apply(type);
	}

}