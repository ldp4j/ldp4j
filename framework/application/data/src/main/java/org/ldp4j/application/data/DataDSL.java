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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.io.Serializable;
import java.net.URI;

public final class DataDSL {

	//--------------------------------------------------------------------------
	// DataDSL builders
	//--------------------------------------------------------------------------

	public interface RecursiveDataSetBuilder extends DataSetBuilder {

		DataSet build();
	}

	public interface RecursiveIndividualBuilder
		extends IndividualBuilder, RecursiveDataSetBuilder {
	}

	public interface RecursiveObjectPropertyBuilder
		extends ObjectPropertyBuilder, RecursiveIndividualBuilder {
	}

	public interface RecursiveDatatypePropertyBuilder
		extends DatatypePropertyBuilder, RecursiveIndividualBuilder {
	}

	public interface ObjectPropertyBuilder {

		<T extends Serializable, S extends Individual<T,S>> IndividualBuilder toIndividual(IndividualReference<T,S> reference);
		<T extends Serializable, S extends Individual<T,S>> RecursiveObjectPropertyBuilder referringTo(IndividualReference<T,S> reference);

	}

	public interface DatatypePropertyBuilder {

		RecursiveDatatypePropertyBuilder withValue(Object value);

	}

	public interface IndividualBuilder {

		ObjectPropertyBuilder hasLink(String id);
		DatatypePropertyBuilder hasProperty(String id);

	}

	public interface DataSetBuilder {

		<T extends Serializable, S extends Individual<T,S>> IndividualBuilder individual(IndividualReference<T,S> reference);

	}

	//--------------------------------------------------------------------------
	// Default DataDSL implementation
	//--------------------------------------------------------------------------

	private abstract static class AbstractIndividualBuilder
		extends Configurable
		implements IndividualBuilder {

		protected AbstractIndividualBuilder(Configurable configurable) {
			super(configurable);
		}

		@Override
		public final ObjectPropertyBuilder hasLink(final String id) {
			class CustomInjector implements Injector {

				private ObjectPropertyBuilder builder=null;

				@Override
				public <T extends Serializable, S extends Individual<T, S>> void injectReference(IndividualReference<T, S> reference) {
					this.builder=new ObjectPropertyBuilderImpl<T,S>(AbstractIndividualBuilder.this,reference, id);
				}

			}
			CustomInjector injector=new CustomInjector();
			accept(injector);
			return injector.builder;
		}

		@Override
		public final DatatypePropertyBuilder hasProperty(final String id) {
			class CustomInjector implements Injector {

				private DatatypePropertyBuilder builder=null;

				@Override
				public <T extends Serializable, S extends Individual<T, S>> void injectReference(IndividualReference<T, S> reference) {
					this.builder=new DatatypePropertyBuilderImpl<T,S>(AbstractIndividualBuilder.this,reference, id);
				}

			}
			CustomInjector injector=new CustomInjector();
			accept(injector);
			return injector.builder;
		}

		protected abstract void accept(Injector injector);

		protected interface Injector {

			<T extends Serializable, S extends Individual<T,S>> void injectReference(IndividualReference<T,S> reference);

		}

	}

	private abstract static class AbstractRecursiveIndividualBuilder
		extends AbstractIndividualBuilder
		implements RecursiveIndividualBuilder {

		protected AbstractRecursiveIndividualBuilder(Configurable configurable) {
			super(configurable);
		}

		@Override
		public final <T extends Serializable, S extends Individual<T,S>> IndividualBuilder individual(IndividualReference<T,S> individualReference) {
			return new IndividualBuilderImpl<T,S>(this, individualReference);
		}

		@Override
		public final DataSet build() {
			return getStore().serialize();
		}

	}

	private static final class ObjectPropertyBuilderImpl<T extends Serializable, S extends Individual<T,S>> extends Configurable implements ObjectPropertyBuilder {

		private final class InnerObjectPropertyBuilder
			extends AbstractRecursiveIndividualBuilder
			implements RecursiveObjectPropertyBuilder {

			protected InnerObjectPropertyBuilder() {
				super(ObjectPropertyBuilderImpl.this);
			}

			@Override
			public <K extends Serializable, V extends Individual<K,V>> RecursiveObjectPropertyBuilder referringTo(IndividualReference<K,V> id) {
				return ObjectPropertyBuilderImpl.this.referringTo(id);
			}

			@Override
			public <K extends Serializable, V extends Individual<K,V>> IndividualBuilder toIndividual(IndividualReference<K,V> id) {
				return ObjectPropertyBuilderImpl.this.toIndividual(id);
			}

			@Override
			protected void accept(Injector injector) {
				injector.injectReference(individualId);
			}

		}

		private final IndividualReference<T,S> individualId;
		private final String propertyId;
		private final InnerObjectPropertyBuilder recursive;

		private ObjectPropertyBuilderImpl(Configurable ctx, IndividualReference<T,S> individualId, String propertyId) {
			super(ctx);
			this.individualId = individualId;
			this.propertyId = propertyId;
			this.recursive = new InnerObjectPropertyBuilder();
		}

		private <K extends Serializable, V extends Individual<K,V>> void addLinkToStore(IndividualReference<K,V> reference) {
			getStore().addLink(individualId, URI.create(propertyId), reference);
		}

		@Override
		public <K extends Serializable, V extends Individual<K,V>> RecursiveObjectPropertyBuilder referringTo(IndividualReference<K,V> targetReference) {
			addLinkToStore(targetReference);
			return recursive;
		}

		@Override
		public <K extends Serializable, V extends Individual<K,V>> IndividualBuilder toIndividual(IndividualReference<K,V> targetReference) {
			addLinkToStore(targetReference);
			return new IndividualBuilderImpl<K,V>(this,targetReference);
		}

	}

	private static final class DatatypePropertyBuilderImpl<T extends Serializable, S extends Individual<T,S>> extends Configurable implements DatatypePropertyBuilder {

		private final class InnerDatatypePropertyBuilder
			extends AbstractRecursiveIndividualBuilder
			implements RecursiveDatatypePropertyBuilder {

			protected InnerDatatypePropertyBuilder() {
				super(DatatypePropertyBuilderImpl.this);
			}

			@Override
			public RecursiveDatatypePropertyBuilder withValue(Object value) {
				return DatatypePropertyBuilderImpl.this.withValue(value);
			}

			@Override
			protected void accept(Injector injector) {
				injector.injectReference(individualId);
			}

		}

		private final String propertyId;
		private final IndividualReference<T,S> individualId;
		private final InnerDatatypePropertyBuilder recursive;

		private DatatypePropertyBuilderImpl(Configurable ctx, IndividualReference<T,S> individualId, String propertyId) {
			super(ctx);
			this.individualId = individualId;
			this.propertyId = propertyId;
			this.recursive = new InnerDatatypePropertyBuilder();
		}

		@Override
		public RecursiveDatatypePropertyBuilder withValue(Object value) {
			getStore().addValue(this.individualId, URI.create(this.propertyId), value);
			return recursive;
		}

	}

	private static final class IndividualBuilderImpl<T extends Serializable, S extends Individual<T,S>> extends AbstractIndividualBuilder {

		private final IndividualReference<T,S> individualId;

		private IndividualBuilderImpl(Configurable configurable, IndividualReference<T,S> individualReference) {
			super(configurable);
			this.individualId = individualReference;
		}

		@Override
		protected void accept(Injector injector) {
			injector.injectReference(this.individualId);
		}

	}

	private static final class DataSetBuilderImpl extends Configurable implements DataSetBuilder {

		private DataSetBuilderImpl(Store store) {
			super(store);
		}

		@Override
		public <T extends Serializable, S extends Individual<T,S>> IndividualBuilder individual(IndividualReference<T,S> reference) {
			return new IndividualBuilderImpl<T,S>(this,reference);
		}
	}

	private abstract static class Configurable {

		private final Store store;

		private Configurable(Store store) {
			this.store = store;
		}

		protected Configurable(Configurable configurable) {
			this(configurable.store);
		}

		public final Store getStore() {
			return store;
		}

	}

	private DataDSL() {
	}

	public static DataSetBuilder dataSet() {
		return dataSet(null);
	}

	public static <T extends Serializable> DataSetBuilder dataSet(Name<T> name) {
		return new DataSetBuilderImpl(new InMemStore(name));
	}

}
