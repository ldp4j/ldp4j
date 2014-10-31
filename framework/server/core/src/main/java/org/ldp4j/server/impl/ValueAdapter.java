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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.data.Value;
import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.LanguageLiteral;
import org.ldp4j.rdf.Literal;
import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.NodeVisitor;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.TypedLiteral;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.server.data.ResourceResolver;

final class ValueAdapter {

	private final class NameGenerator extends NodeVisitor<Individual<?,?>> {

		@Override
		public Individual<?,?> visitURIRef(URIRef node, Individual<?,?> defaultResult) {
			ManagedIndividualId resourceId = resourceResolver.resolveLocation(node.getIdentity());
			if(resourceId==null) {
				return dataSet.individual(base.relativize(node.getIdentity()),ExternalIndividual.class);
			}
			return dataSet.individual(resourceId, ManagedIndividual.class);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Individual<?,?> visitBlankNode(BlankNode node, Individual<?,?> defaultResult) {
			return dataSet.individual((Name)NamingScheme.getDefault().name(node.getIdentity()), LocalIndividual.class);
		}

	}

	private final class ObjectGenerator extends NodeVisitor<Value> {

		@Override
		public Value visitLanguageLiteral(LanguageLiteral node, Value defaultResult) {
			return DataSetUtils.newLiteral(node.getValue());
		}

		@Override
		public Value visitTypedLiteral(TypedLiteral<?> node, Value defaultResult) {
			return DataSetUtils.newLiteral(node.getValue());
		}

		@Override
		public Value visitLiteral(Literal<?> node, Value defaultResult) {
			return DataSetUtils.newLiteral(node.getValue());
		}

		@Override
		public Value visitURIRef(URIRef node, Value defaultResult) {
			ManagedIndividualId resourceId = resourceResolver.resolveLocation(node.getIdentity());
			if(resourceId==null) {
				return dataSet.individual(base.relativize(node.getIdentity()),ExternalIndividual.class);
			}
			return dataSet.individual(resourceId, ManagedIndividual.class);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Value visitBlankNode(BlankNode node, Value defaultResult) {
			return dataSet.individual((Name)NamingScheme.getDefault().name(node.getIdentity()), LocalIndividual.class);
		}

	}

	private final ResourceResolver resourceResolver;

	private final ObjectGenerator objectGenerator;
	private final NameGenerator nameGenerator;

	private final DataSet dataSet;

	private final URI base;

	ValueAdapter(ResourceResolver resourceResolver, DataSet dataSet, URI base) {
		this.resourceResolver = resourceResolver;
		this.dataSet = dataSet;
		this.base = base;
		this.nameGenerator = new NameGenerator();
		this.objectGenerator = new ObjectGenerator();
	}

	Individual<?,?> getIndividual(Resource<?> resource) {
		return resource.accept(nameGenerator);
	}

	Value getValue(Node object) {
		return object.accept(objectGenerator);
	}

}