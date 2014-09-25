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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-application:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-application-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.ldp4j.application.vocabulary.Term;

public final class IndividualReferenceBuilder {
	
	private static final class LocalIndividualReferenceBuilder implements NamedIndividualReferenceBuilder.NameConsumer {

		@Override
		public IndividualReference<?,?> useName(Name<?> name) {
			return IndividualReference.anonymous(name);
		}
		
	}

	private static final class ManagedIndividualReferenceBuilder implements NamedIndividualReferenceBuilder.NameConsumer {

		private final String managerId;

		private ManagedIndividualReferenceBuilder(String managerId) {
			this.managerId = managerId;
		}

		@Override
		public IndividualReference<?,?> useName(Name<?> name) {
			return IndividualReference.managed(ManagedIndividualId.createId(name, this.managerId));
		}
		
	}
	
	public static final class NamedIndividualReferenceBuilder {
		
		private final NameConsumer consumer;
	
		public interface NameConsumer {
			IndividualReference<?,?> useName(Name<?> name);
		}
		
		private NamedIndividualReferenceBuilder(NameConsumer consumer) {
			this.consumer = consumer;
		}
		
		private IndividualReference<?,?> createName(Name<?> name) {
			return this.consumer.useName(name);
		}
	
		public IndividualReference<?,?> named(URI id) {
			return createName(NamingScheme.getDefault().name(id));
		}
	
		public IndividualReference<?,?> named(QName id) {
			return createName(NamingScheme.getDefault().name(id));
		}
	
		public IndividualReference<?,?> named(Term id) {
			return createName(NamingScheme.getDefault().name(id));
		}
	
		public IndividualReference<?,?> named(String name, String... names) {
			return createName(NamingScheme.getDefault().name(name, names));
		}
		
		public IndividualReference<?,?> named(Class<?> clazz, String... names) {
			return createName(NamingScheme.getDefault().name(clazz, names));
		}
	
		public <T extends Number> IndividualReference<?,?> named(T id) {
			return createName(NamingScheme.getDefault().name(id));
		}
		
	}

	public static final class ExternalIndividualReferenceBuilder {
		
		IndividualReference<?,?> atLocation(URI location) {
			return IndividualReference.external(location);
		}
		
		IndividualReference<?,?> atLocation(String location) {
			try {
				return atLocation(new URI(location));
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Could not encode URI from String '"+location+"'",e);
			}
		}
		IndividualReference<?,?> atLocation(QName location) {
			try {
				return atLocation(new URI(location.getNamespaceURI()+""+location.getLocalPart()));
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Could not encode URI from QName '"+location+"'",e);
			}
		}
	
		IndividualReference<?,?> atLocation(URL location) {
			try {
				return atLocation(location.toURI());
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Could not encode URI from URL '"+location+"'",e);
			}
		}
		
	}

	private IndividualReferenceBuilder() {
		
	}
	
	public NamedIndividualReferenceBuilder toLocalIndividual() {
		return new NamedIndividualReferenceBuilder(new LocalIndividualReferenceBuilder());
		
	}

	public NamedIndividualReferenceBuilder toManagedIndividual(String templateId) {
		return new NamedIndividualReferenceBuilder(new ManagedIndividualReferenceBuilder(templateId));
	}
	
	public ExternalIndividualReferenceBuilder toExternalIndividual() {
		return new ExternalIndividualReferenceBuilder();
	}

	public static IndividualReferenceBuilder newReference() {
		return new IndividualReferenceBuilder();
	}
	
	
}