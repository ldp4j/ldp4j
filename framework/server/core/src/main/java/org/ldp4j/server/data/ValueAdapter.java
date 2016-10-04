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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.data;

import java.net.URI;

import javax.xml.datatype.XMLGregorianCalendar;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.data.NewIndividual;
import org.ldp4j.application.data.RelativeIndividual;
import org.ldp4j.application.data.RelativeIndividualId;
import org.ldp4j.application.data.Value;
import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.LanguageLiteral;
import org.ldp4j.rdf.Literal;
import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.NodeVisitor;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.TypedLiteral;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.server.utils.URIHelper;

final class ValueAdapter {

	private final class IndividualGenerator extends NodeVisitor<Individual<?,?>> {

		@Override
		public Individual<?,?> visitURIRef(URIRef node, Individual<?,?> defaultResult) {
			return resolveURIRef(node);
		}

		@Override
		public Individual<?,?> visitBlankNode(BlankNode node, Individual<?,?> defaultResult) {
			return resolveBlankNode(node);
		}

	}

	private final class ObjectGenerator extends NodeVisitor<Value> {

		@Override
		public Value visitLanguageLiteral(LanguageLiteral node, Value defaultResult) {
			return Literals.newLanguageLiteral(node.getValue(),node.getLanguage());
		}

		@Override
		public Value visitTypedLiteral(TypedLiteral<?> node, Value defaultResult) {
			return Literals.newTypedLiteral(serializable(node.getValue()),node.getType().toURI());
		}

		@Override
		public Value visitLiteral(Literal<?> node, Value defaultResult) {
			return Literals.newLiteral(serializable(node.getValue()));
		}

		@Override
		public Value visitURIRef(URIRef node, Value defaultResult) {
			return resolveURIRef(node);
		}

		@Override
		public Value visitBlankNode(BlankNode node, Value defaultResult) {
			return resolveBlankNode(node);
		}

	}

	private final ResourceResolver resourceResolver;

	private final ObjectGenerator objectGenerator;
	private final IndividualGenerator individualGenerator;

	private final DataSet dataSet;

	private ResourceResolution resolution;

	ValueAdapter(ResourceResolver resourceResolver, DataSet dataSet) {
		this.resourceResolver = resourceResolver;
		this.dataSet = dataSet;
		this.individualGenerator = new IndividualGenerator();
		this.objectGenerator = new ObjectGenerator();
	}

	private Object serializable(Object value) {
		if(value instanceof XMLGregorianCalendar) {
			XMLGregorianCalendar calendar=(XMLGregorianCalendar)value;
			// TODO: Make this configurable --> See Literals class
			return calendar.toGregorianCalendar().getTime();
		}
		return value;
	}

	private Individual<?, ?> resolveURIRef(URIRef node) {
		if(this.resolution.isTransient()) {
			return this.dataSet.individual(this.resolution.realURI(),NewIndividual.class);
		}
		URI location = node.getIdentity();
		for(URI identity:URIHelper.getParents(location)) {
			ManagedIndividualId resourceId = this.resourceResolver.resolveLocation(identity);
			if(resourceId!=null) {
				if(identity.equals(location)) {
					return this.dataSet.individual(resourceId, ManagedIndividual.class);
				} else {
					URI relativePath = identity.relativize(location);
					RelativeIndividualId relativeId = RelativeIndividualId.createId(resourceId, relativePath);
					return this.dataSet.individual(relativeId,RelativeIndividual.class);
				}
			}
		}
		return this.dataSet.individual(location,ExternalIndividual.class);
	}

	@SuppressWarnings("rawtypes")
	private Individual<?, ?> resolveBlankNode(BlankNode node) {
		return
			this.dataSet.individual(
				(Name)NamingScheme.getDefault().name(node.getIdentity()),
				LocalIndividual.class);
	}

	Individual<?,?> getIndividual(Resource<?> resource, ResourceResolution resolution) {
		this.resolution = resolution;
		return resource.accept(this.individualGenerator);
	}

	Value getValue(Node object, ResourceResolution resolution) {
		this.resolution = resolution;
		return object.accept(this.objectGenerator);
	}

}