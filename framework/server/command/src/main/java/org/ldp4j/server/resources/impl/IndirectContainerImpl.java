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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.resources.impl;

import java.net.URI;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.server.resources.IndirectContainer;
import org.ldp4j.server.resources.ResourceType;


public class IndirectContainerImpl extends MembershipAwareContainerImpl implements IndirectContainer {

	private URI insertedContentRelation=null;

	public IndirectContainerImpl() {
		super(ResourceType.INDIRECT_CONTAINER);
	}
	protected void fillInMetadata(Individual<?, ?> individual, Context ctx) {
		super.fillInMetadata(individual, ctx);
		individual.
			addValue(
				ctx.property(LDP.INSERTED_CONTENT_RELATION), 
				ctx.value(insertedContentRelation));
	}
	
	@Override
	public URI insertedContentRelation() {
		return insertedContentRelation;
	}

	void setInsertedContentRelation(URI insertedContentRelation) {
		this.insertedContentRelation = insertedContentRelation;
	}

}