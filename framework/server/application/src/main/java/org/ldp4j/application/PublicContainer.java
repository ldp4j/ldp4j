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
package org.ldp4j.application;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.application.endpoint.Endpoint;
import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;

import com.google.common.collect.Lists;

public abstract class PublicContainer extends PublicRDFSource {

	protected PublicContainer(ApplicationContext applicationContext, Endpoint endpoint) {
		super(applicationContext, endpoint);
	}
	
	public final Collection<PublicResource> members() {
		List<PublicResource> members=Lists.newArrayList();
		for(ResourceId memberId:resolveAs(Container.class).memberIds()) {
			members.add(createResource(memberId));
		}
		return Collections.unmodifiableList(members);
	}

	@Override
	protected void fillInMetadata(Individual<?, ?> individual, Context ctx) {
		super.fillInMetadata(individual, ctx);
		individual.
			addValue(
				ctx.property(RDF.TYPE), 
				ctx.reference(LDP.CONTAINER));
		for(PublicResource member:members()) {
			individual.addValue(
				ctx.property(LDP.CONTAINS), 
				ctx.newIndividual(member));
		}
	}

	public PublicResource createResource(DataSet dataSet) throws ApplicationExecutionException {
		Resource resource=applicationContext().createResource(endpoint(), dataSet);
		return createResource(resource.id());
	}

}
