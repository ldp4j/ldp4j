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
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.ldp4j.application.vocabulary.Term;

public final class IndividualReferenceBuilder {

	@SuppressWarnings("rawtypes")
	private static final class LocalIndividualReferenceBuilder implements NameConsumer<Name,LocalIndividual> {

		@Override
		public IndividualReference<Name,LocalIndividual> useName(Name<?> name) {
			return IndividualReference.anonymous(name);
		}

	}

	private static final class ManagedIndividualReferenceBuilder implements NameConsumer<ManagedIndividualId,ManagedIndividual> {

		private final String managerId;

		private ManagedIndividualReferenceBuilder(String managerId) {
			this.managerId = managerId;
		}

		@Override
		public IndividualReference<ManagedIndividualId,ManagedIndividual> useName(Name<?> name) {
			return IndividualReference.managed(ManagedIndividualId.createId(name, this.managerId));
		}

	}

	private static final class RelativeManagedIndividualReferenceBuilder implements NameConsumer<RelativeIndividualId,RelativeIndividual> {

		private final String managerId;
		private URI path;

		private RelativeManagedIndividualReferenceBuilder(URI location, String managerId) {
			this.path = location;
			this.managerId = managerId;
		}

		@Override
		public IndividualReference<RelativeIndividualId,RelativeIndividual> useName(Name<?> name) {
			ManagedIndividualId parent = ManagedIndividualId.createId(name, this.managerId);
			return IndividualReference.relative(parent,this.path);
		}

	}

	public static final class RelativeIndividualReferenceBuilder {

		public static final class ParentIndividualBuilder {

			private URI location;

			private ParentIndividualBuilder(URI location) {
				this.location = location;
			}

			public NamedIndividualReferenceBuilder<RelativeIndividualId,RelativeIndividual> ofIndividualManagedBy(String managerId) {
				return new NamedIndividualReferenceBuilder<RelativeIndividualId,RelativeIndividual>(new RelativeManagedIndividualReferenceBuilder(this.location,managerId));
			}

		}

		public ParentIndividualBuilder atLocation(URI location) {
			return new ParentIndividualBuilder(location);
		}

		public ParentIndividualBuilder atLocation(String location) {
			try {
				return atLocation(new URI(location));
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Could not encode URI from String '"+location+"'",e);
			}
		}
		public ParentIndividualBuilder atLocation(QName location) {
			try {
				return atLocation(new URI(location.getNamespaceURI()+""+location.getLocalPart()));
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Could not encode URI from QName '"+location+"'",e);
			}
		}

		public ParentIndividualBuilder atLocation(URL location) {
			try {
				return atLocation(location.toURI());
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Could not encode URI from URL '"+location+"'",e);
			}
		}

	}

	private interface NameConsumer<T extends Serializable, S extends Individual<T,S>> {
		IndividualReference<T,S> useName(Name<?> name);
	}

	public static final class NamedIndividualReferenceBuilder<T extends Serializable, S extends Individual<T,S>> {

		private final NameConsumer<T,S> consumer;

		private NamedIndividualReferenceBuilder(NameConsumer<T,S> consumer) {
			this.consumer = consumer;
		}

		private IndividualReference<T,S> createName(Name<?> name) {
			return this.consumer.useName(name);
		}

		public IndividualReference<T,S> named(URI id) {
			return createName(NamingScheme.getDefault().name(id));
		}

		public IndividualReference<T,S> named(QName id) {
			return createName(NamingScheme.getDefault().name(id));
		}

		public IndividualReference<T,S> named(Term id) {
			return createName(NamingScheme.getDefault().name(id));
		}

		public IndividualReference<T,S> named(String name, String... names) {
			return createName(NamingScheme.getDefault().name(name, names));
		}

		public IndividualReference<T,S> named(Class<?> clazz, String... names) {
			return createName(NamingScheme.getDefault().name(clazz, names));
		}

		public <V extends Number> IndividualReference<T,S> named(V id) {
			return createName(NamingScheme.getDefault().name(id));
		}

	}

	public static final class ExternalIndividualReferenceBuilder {

		public IndividualReference<URI,ExternalIndividual> atLocation(URI location) {
			return IndividualReference.external(location);
		}

		public IndividualReference<URI,ExternalIndividual> atLocation(String location) {
			try {
				return atLocation(new URI(location));
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Could not encode URI from String '"+location+"'",e);
			}
		}
		public IndividualReference<URI,ExternalIndividual> atLocation(QName location) {
			try {
				return atLocation(new URI(location.getNamespaceURI()+""+location.getLocalPart()));
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Could not encode URI from QName '"+location+"'",e);
			}
		}

		public IndividualReference<URI,ExternalIndividual> atLocation(URL location) {
			try {
				return atLocation(location.toURI());
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Could not encode URI from URL '"+location+"'",e);
			}
		}

	}

	private IndividualReferenceBuilder() {

	}

	@SuppressWarnings("rawtypes")
	public NamedIndividualReferenceBuilder<Name,LocalIndividual> toLocalIndividual() {
		return new NamedIndividualReferenceBuilder<Name,LocalIndividual>(new LocalIndividualReferenceBuilder());
	}

	public NamedIndividualReferenceBuilder<ManagedIndividualId,ManagedIndividual> toManagedIndividual(String templateId) {
		return new NamedIndividualReferenceBuilder<ManagedIndividualId,ManagedIndividual>(new ManagedIndividualReferenceBuilder(templateId));
	}

	public RelativeIndividualReferenceBuilder toRelativeIndividual() {
		return new RelativeIndividualReferenceBuilder();
	}

	public ExternalIndividualReferenceBuilder toExternalIndividual() {
		return new ExternalIndividualReferenceBuilder();
	}

	public static IndividualReferenceBuilder newReference() {
		return new IndividualReferenceBuilder();
	}

}