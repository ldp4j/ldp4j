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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.server.resources.Container;
import org.ldp4j.server.resources.Resource;
import org.ldp4j.server.resources.ResourceType;

public class ContainerImpl extends ResourceImpl implements Container {
	
	private List<Resource> members=new CopyOnWriteArrayList<Resource>();
	
	protected ContainerImpl(ResourceType type) {
		super(type);
	}
	
	public ContainerImpl() {
		this(ResourceType.CONTAINER);
	}
	
	protected void fillInMetadata(Individual<?, ?> individual, Context ctx) {
		super.fillInMetadata(individual, ctx);
		individual.
			addValue(
				ctx.property(RDF.TYPE), 
				ctx.reference(LDP.CONTAINER));
		for(Resource member:members) {
			individual.addValue(
				ctx.property(LDP.CONTAINS), 
				ctx.resourceSurrogate(member));
		}
	}

	@Override
	public Collection<Resource> members() {
		return Collections.unmodifiableList(new ArrayList<Resource>(members));
	}
	
	@Override
	public void addMember(Resource resource) {
		members.add(resource);
	}
	
	@Override
	public void deleteMember(Resource resource) {
		members.remove(resource);
	}

}