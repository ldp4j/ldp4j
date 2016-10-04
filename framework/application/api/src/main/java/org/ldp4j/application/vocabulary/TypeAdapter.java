/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.vocabulary;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

final class TypeAdapter<S,T> {

	static final class QNameAdapter {
		private QNameAdapter() {
		}
		/**
		 * Transform a term to a {@code QName}.
		 *
		 * @param term
		 *            the term to adapt.
		 * @return a {@code QName} object that represents the specified term.
		 */
		public static QName adaptTo(Term term) {
			Vocabulary vocabulary = term.getDeclaringVocabulary();
			return
				new QName(
					vocabulary.getNamespace(),
					term.entityName(),
					vocabulary.getPreferredPrefix());
		}
	}

	static final class URIAdapter {
		private URIAdapter(){
		}
		/**
		 * Transform a term to a {@code URI}.
		 *
		 * @param term
		 *            the term to adapt.
		 * @return a {@code URI} object that represents the specified term.
		 */
		public static URI adaptTo(Term term) {
			return
				URI.
					create(
						term.getDeclaringVocabulary().getNamespace()+
						term.entityName());
		}
	}

	static final String ADAPTER_NAME_CONVENTION = "adaptTo";

	static final CopyOnWriteArrayList<Class<?>> ADAPTER_CLASSES=Lists.newCopyOnWriteArrayList(); // NOSONAR

	static {
		registerAdapterClass(URIAdapter.class);
		registerAdapterClass(QNameAdapter.class);
	}

	private final Class<? extends T> resultClass;
	private final Method adapterMethod;

	private TypeAdapter(Method adapterMethod, Class<? extends T> resultClass) {
		this.adapterMethod = adapterMethod;
		this.resultClass = resultClass;
	}

	private String getAdapterFailureMessage(String messageTemplate, Object... args) {
		return String.format("Chosen adapter method '%s' ",adapterMethod).concat(String.format(messageTemplate,args));
	}

	T adapt(S value) {
		try {
			T result = resultClass.cast(adapterMethod.invoke(null, value));
			if(result==null) {
				throw new IllegalStateException(getAdapterFailureMessage("could not adapt value '%s",value));
			}
			return result;
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(getAdapterFailureMessage("should accept '%s'",value.getClass().getCanonicalName()),e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(getAdapterFailureMessage("should be accesible"),e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(getAdapterFailureMessage("failed while adapting value '%s'",value),e);
		}
	}

	private static Collection<Class<?>> findTypeAdapters() {
		return ImmutableList.copyOf(TypeAdapter.ADAPTER_CLASSES);
	}

	private static <S,T> TypeAdapter<S,T> doCreateAdapter(Class<? extends T> targetType, AdapterMethodValidator validator) {
		for(Class<?> termAdapter:findTypeAdapters()) {
			for(Method candidate:termAdapter.getMethods()) {
				if(validator.isValid(candidate)) {
					return new TypeAdapter<S,T>(candidate,targetType);
				}
			}
		}
		throw new CannotAdaptClassesException(
			"Could not find adapter of adapting class '" +
			validator.getTargetClass().getCanonicalName() +
			"' to '" + targetType.getCanonicalName() + "'");
	}

	/**
	 * Register a new adapter class.
	 *
	 * @param clazz
	 *            the adapter class.
	 * @throws NullPointerException
	 *             if the adapter class is {@code null}.
	 */
	static void registerAdapterClass(Class<?> clazz) {
		Objects.requireNonNull(clazz,"Adapter class cannot be null");
		TypeAdapter.ADAPTER_CLASSES.addIfAbsent(clazz);
	}

	/**
	 * Get an adapter capable of transforming instances of a source type into
	 * instances of a target type.
	 *
	 * @param sourceType
	 *            The type of the instances that the adapter should be able to
	 *            transform.
	 * @param targetType
	 *            The type of instances that the adapter should be able to
	 *            create.
	 * @return An adapter capable of transforming instances of the {@code sourceType}
	 *         into instances of the {@code targetType}.
	 * @throws CannotAdaptClassesException
	 *             when no adapter capable of carrying out such transformation
	 *             is available.
	 */
	static <S,T> TypeAdapter<S,T> createAdapter(Class<? extends S> sourceType, Class<? extends T> targetType) {
		return doCreateAdapter(targetType,AdapterMethodValidator.newInstance(targetType, sourceType));
	}

	/**
	 * Transform a given object into an instance of the specified type.
	 *
	 * @param object
	 *            The object that is to be transformed.
	 * @param resultClass
	 *            The type of instance that is to be created.
	 * @return An instance of type {@code resultClass} created upon the
	 *         specified @ object} .
	 * @throws CannotAdaptClassesException
	 *             when no adapter capable of carrying out such transformation
	 *             is available.
	 */
	static <S,T> T adapt(S object, Class<? extends T> resultClass) {
		return TypeAdapter.<S,T>doCreateAdapter(resultClass, AdapterMethodValidator.newInstance(resultClass, object)).adapt(object);
	}
}