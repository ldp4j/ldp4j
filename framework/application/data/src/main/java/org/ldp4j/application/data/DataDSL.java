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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-data-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

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

		IndividualBuilder toIndividual(IndividualReference<?,?> reference);
		RecursiveObjectPropertyBuilder referringTo(IndividualReference<?,?> reference);

	}

	public interface DatatypePropertyBuilder {

		RecursiveDatatypePropertyBuilder withValue(Object value);

	}

	public interface IndividualBuilder {

		ObjectPropertyBuilder hasLink(String id);
		DatatypePropertyBuilder hasProperty(String id);

	}

	public interface DataSetBuilder {

		IndividualBuilder individual(IndividualReference<?,?> reference);

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
		public final ObjectPropertyBuilder hasLink(String id) {
			return new ObjectPropertyBuilderImpl(this,getIndividualReference(), id);
		}

		@Override
		public final DatatypePropertyBuilder hasProperty(String id) {
			return new DatatypePropertyBuilderImpl(this,getIndividualReference(),id);
		}

		protected abstract IndividualReference<?,?> getIndividualReference();

	}

	private abstract static class AbstractRecursiveIndividualBuilder
		extends AbstractIndividualBuilder
		implements RecursiveIndividualBuilder {

		protected AbstractRecursiveIndividualBuilder(Configurable configurable) {
			super(configurable);
		}

		@Override
		public final IndividualBuilder individual(IndividualReference<?,?> individualReference) {
			return new IndividualBuilderImpl(this, individualReference);
		}

		@Override
		public final DataSet build() {
			return getStore().serialize();
		}

	}

	private static final class ObjectPropertyBuilderImpl extends Configurable implements ObjectPropertyBuilder {

		private final class InnerObjectPropertyBuilder
			extends AbstractRecursiveIndividualBuilder
			implements RecursiveObjectPropertyBuilder {

			protected InnerObjectPropertyBuilder() {
				super(ObjectPropertyBuilderImpl.this);
			}

			@Override
			public RecursiveObjectPropertyBuilder referringTo(IndividualReference<?,?> id) {
				return ObjectPropertyBuilderImpl.this.referringTo(id);
			}

			@Override
			public IndividualBuilder toIndividual(IndividualReference<?,?> id) {
				return ObjectPropertyBuilderImpl.this.toIndividual(id);
			}

			@Override
			protected IndividualReference<?,?> getIndividualReference() {
				return individualId;
			}

		}

		private final IndividualReference<?,?> individualId;
		private final String propertyId;
		private final InnerObjectPropertyBuilder recursive;

		private ObjectPropertyBuilderImpl(Configurable ctx, IndividualReference<?,?> individualId, String propertyId) {
			super(ctx);
			this.individualId = individualId;
			this.propertyId = propertyId;
			this.recursive = new InnerObjectPropertyBuilder();
		}

		private void addLinkToStore(IndividualReference<?,?> reference) {
			getStore().addLink(individualId, URI.create(propertyId), reference);
		}

		@Override
		public RecursiveObjectPropertyBuilder referringTo(IndividualReference<?,?> targetReference) {
			addLinkToStore(targetReference);
			return recursive;
		}

		@Override
		public IndividualBuilder toIndividual(IndividualReference<?,?> targetReference) {
			addLinkToStore(targetReference);
			return new IndividualBuilderImpl(this,targetReference);
		}

	}

	private static final class DatatypePropertyBuilderImpl extends Configurable implements DatatypePropertyBuilder {

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
			protected IndividualReference<?,?> getIndividualReference() {
				return individualReference;
			}

		}

		private final String propertyId;
		private final IndividualReference<?,?> individualReference;
		private final InnerDatatypePropertyBuilder recursive;

		private DatatypePropertyBuilderImpl(Configurable ctx, IndividualReference<?,?> individualId, String propertyId) {
			super(ctx);
			this.individualReference = individualId;
			this.propertyId = propertyId;
			this.recursive = new InnerDatatypePropertyBuilder();
		}

		@Override
		public RecursiveDatatypePropertyBuilder withValue(Object value) {
			getStore().addValue(this.individualReference, URI.create(this.propertyId), value);
			return recursive;
		}

	}

	private static final class IndividualBuilderImpl extends AbstractIndividualBuilder {

		private final IndividualReference<?,?> individualReference;

		private IndividualBuilderImpl(Configurable configurable, IndividualReference<?,?> individualReference) {
			super(configurable);
			this.individualReference = individualReference;
		}

		@Override
		protected IndividualReference<?,?> getIndividualReference() {
			return individualReference;
		}

	}

	private static final class DataSetBuilderImpl extends Configurable implements DataSetBuilder {

		private DataSetBuilderImpl(Store store) {
			super(store);
		}

		@Override
		public IndividualBuilder individual(IndividualReference<?,?> reference) {
			return new IndividualBuilderImpl(this,reference);
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

	public static DataSetBuilder dataSet(Name<?> name) {
		return new DataSetBuilderImpl(new InMemStore(name));
	}

}
