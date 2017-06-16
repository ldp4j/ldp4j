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
package org.ldp4j.application.data;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;



import com.google.common.collect.ImmutableMap;

final class IndividualFactory {

	private interface Factory<T extends Individual<?,?>> {

		Class<?> idClass();

		T newIndividual(Object id, MutableDataSet context);

	}


	private abstract static class SimpleFactory<S,T extends Individual<?,?>> implements Factory<T> {

		private final Class<? extends S> clazz;

		private SimpleFactory(Class<? extends S> clazz) {
			this.clazz = clazz;
		}

		@Override
		public Class<? extends S> idClass() {
			return clazz;
		}

		@Override
		public T newIndividual(Object id, MutableDataSet context) {
			return createIndividual(idClass().cast(id), context);
		}

		protected abstract T createIndividual(S id, MutableDataSet context);

	}

	private static final Map<Class<? extends Individual<?,?>>, Factory<?>> FACTORIES;
	private final MutableDataSet dataSet;

	static {
		FACTORIES=
			ImmutableMap.<Class<? extends Individual<?,?>>, Factory<?>>
				builder().
					put(
						LocalIndividual.class,
						new Factory<MutableLocalIndividual>() {
							@Override
							public Class<?> idClass() {
								return Name.class;
							}
							@Override
							public MutableLocalIndividual newIndividual(Object id, MutableDataSet context) {
								return new MutableLocalIndividual((Name<?>)id, context);
							}
						}
					).
					put(
						ManagedIndividual.class,
						new SimpleFactory<ManagedIndividualId,MutableManagedIndividual>(ManagedIndividualId.class) {
							@Override
							protected MutableManagedIndividual createIndividual(ManagedIndividualId id, MutableDataSet context) {
								return new MutableManagedIndividual(id,context);
							}
						}
					).
					put(
						RelativeIndividual.class,
						new SimpleFactory<RelativeIndividualId,MutableRelativeIndividual>(RelativeIndividualId.class) {
							@Override
							protected MutableRelativeIndividual createIndividual(RelativeIndividualId id, MutableDataSet context) {
								return new MutableRelativeIndividual(id,context);
							}
						}
					).
					put(
						ExternalIndividual.class,
						new SimpleFactory<URI,MutableExternalIndividual>(URI.class) {
							@Override
							protected MutableExternalIndividual createIndividual(URI id, MutableDataSet context) {
								return new MutableExternalIndividual(id,context);
							}
						}
					).
					put(
						NewIndividual.class,
						new SimpleFactory<URI,MutableNewIndividual>(URI.class) {
							@Override
							protected MutableNewIndividual createIndividual(URI id, MutableDataSet context) {
								return new MutableNewIndividual(id,context);
							}
						}
					).
					build();
	}

	IndividualFactory(MutableDataSet dataSet) {
		this.dataSet = dataSet;
	}

	<T extends Serializable, S extends Individual<T,S>> Individual<T,S> newIndividual(Class<? extends S> clazz, T id) {
		Factory<?> factory=FACTORIES.get(clazz);
		if(factory==null) {
			throw new IllegalStateException("Unsupported individual type '"+clazz.getCanonicalName()+"'");
		}
		Class<?> idClass = factory.idClass();
		if(!idClass.isInstance(id)) {
			throw new IllegalStateException("Could not create an individual of type '"+clazz.getCanonicalName()+"' with identifier '"+id+"'. Expected an identifier of type '"+idClass.getCanonicalName()+"' not '"+id.getClass().getCanonicalName()+"'");
		}
		return clazz.cast(factory.newIndividual(id,dataSet));
	}

}
