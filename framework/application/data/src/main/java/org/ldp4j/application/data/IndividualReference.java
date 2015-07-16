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

import java.io.Serializable;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;


public abstract class IndividualReference<T extends Serializable, S extends Individual<T,S>> {

	public static class ManagedIndividualReference extends IndividualReference<ManagedIndividualId,ManagedIndividual> {

		private ManagedIndividualReference(ManagedIndividualId resourceId) {
			super(resourceId,ManagedIndividual.class);
		}

		@Override
		public void accept(IndividualReferenceVisitor visitor) {
			visitor.visitManagedIndividualReference(this);
		}

	}

	public static class RelativeIndividualReference extends IndividualReference<RelativeIndividualId,RelativeIndividual> {

		private RelativeIndividualReference(RelativeIndividualId resourceId) {
			super(resourceId,RelativeIndividual.class);
		}

		@Override
		public void accept(IndividualReferenceVisitor visitor) {
			visitor.visitRelativeIndividualReference(this);
		}

	}

	@SuppressWarnings("rawtypes")
	public static final class LocalIndividualReference extends IndividualReference<Name,LocalIndividual> {

		private LocalIndividualReference(Name<?> name) {
			super(name,LocalIndividual.class);
		}

		@Override
		public void accept(IndividualReferenceVisitor visitor) {
			visitor.visitLocalIndividualReference(this);
		}

	}

	public static final class ExternalIndividualReference extends IndividualReference<URI,ExternalIndividual> {

		private ExternalIndividualReference(URI location) {
			super(location,ExternalIndividual.class);
		}

		@Override
		public void accept(IndividualReferenceVisitor visitor) {
			visitor.visitExternalIndividualReference(this);
		}

	}

	public static final class NewIndividualReference extends IndividualReference<URI,NewIndividual> {

		private NewIndividualReference(URI location) {
			super(location,NewIndividual.class);
		}

		@Override
		public void accept(IndividualReferenceVisitor visitor) {
			visitor.visitNewIndividualReference(this);
		}

	}

	private final T id;
	private final Class<? extends S> clazz;

	private IndividualReference(T id, Class<? extends S> clazz) {
		this.id = id;
		this.clazz = clazz;
	}

	public T ref() {
		return id;
	}

	public boolean isPresent(DataSet dataSet) {
		return dataSet.hasIndividual(ref());
	}

	public Individual<T,S> resolve(DataSet dataSet) {
		Individual<T, ?> resolvedIndividual = dataSet.individualOfId(ref());
		if(!clazz.isInstance(resolvedIndividual)) {
			throw new IllegalStateException("Unexpected type referred individual");
		}
		return clazz.cast(resolvedIndividual);
	}

	public Individual<T,S> realize(DataSet dataSet) {
		return dataSet.individual(ref(),clazz);
	}

	abstract void accept(IndividualReferenceVisitor visitor);

	@SuppressWarnings("rawtypes")
	public static IndividualReference<Name,LocalIndividual> anonymous(Name<?> name) {
		return new LocalIndividualReference(name);
	}

	public static IndividualReference<ManagedIndividualId,ManagedIndividual> managed(ManagedIndividualId individualId) {
		return new ManagedIndividualReference(individualId);
	}

	public static IndividualReference<ManagedIndividualId,ManagedIndividual> managed(Name<?> name, String templateId) {
		return managed(ManagedIndividualId.createId(name, templateId));
	}

	public static IndividualReference<ManagedIndividualId,ManagedIndividual> managed(Name<?> name, String templateId, URI indirectId) {
		return managed(indirectId,ManagedIndividualId.createId(name, templateId));
	}

	public static IndividualReference<ManagedIndividualId,ManagedIndividual> managed(URI indirectId, ManagedIndividualId parent) {
		return managed(ManagedIndividualId.createId(indirectId, parent));
	}

	public static IndividualReference<RelativeIndividualId,RelativeIndividual> relative(ManagedIndividualId parentId, URI path) {
		return relative(RelativeIndividualId.createId(parentId,path));
	}

	public static IndividualReference<RelativeIndividualId,RelativeIndividual> relative(RelativeIndividualId individualId) {
		return new RelativeIndividualReference(individualId);
	}

	public static IndividualReference<URI,ExternalIndividual> external(URI location) {
		return new ExternalIndividualReference(location);
	}

	public static IndividualReference<URI,NewIndividual> newIndividual(URI location) {
		return new NewIndividualReference(location);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable,S extends Individual<T,S>> IndividualReference<T,S> fromIndividual(Individual<T,S> value) {
		final AtomicReference<IndividualReference<?,?>> result=new AtomicReference<IndividualReference<?,?>>();
		value.accept(
			new IndividualVisitor() {
				@Override
				public void visitManagedIndividual(ManagedIndividual individual) {
					result.set(IndividualReference.managed(individual.id()));
				}
				@Override
				public void visitLocalIndividual(LocalIndividual individual) {
					result.set(IndividualReference.anonymous(individual.id()));
				}
				@Override
				public void visitExternalIndividual(ExternalIndividual individual) {
					result.set(IndividualReference.external(individual.id()));
				}
				@Override
				public void visitRelativeIndividual(RelativeIndividual individual) {
					result.set(IndividualReference.relative(individual.id()));
				}
				@Override
				public void visitNewIndividual(NewIndividual individual) {
					result.set(IndividualReference.newIndividual(individual.id()));
				}
			}
		);
		return (IndividualReference<T, S>)result.get();
	}
}