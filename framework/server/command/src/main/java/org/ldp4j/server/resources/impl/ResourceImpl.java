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
import java.util.Date;

import javax.ws.rs.core.EntityTag;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.domain.RDF;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.vocabulary.Term;
import org.ldp4j.server.api.Entity;
import org.ldp4j.server.resources.Resource;
import org.ldp4j.server.resources.ResourceType;

public class ResourceImpl implements Resource {

	protected static final class Context {
		
		private final DataSet dataSet;
	
		private Context(DataSet dataSet) {
			this.dataSet = dataSet;
		}
		
		public URI property(Term term) {
			return term.as(URI.class);
		}
		
		public Value reference(Term term) {
			return dataSet.individual(term.as(URI.class), ExternalIndividual.class);
		}
		
		public Individual<?,?> newIndividual(URI id) {
			return dataSet.individual(id, ExternalIndividual.class);
		}
	
		@SuppressWarnings("rawtypes")
		public Individual<?,?> newIndividual(Name<?> id) {
			return dataSet.individual((Name)id, LocalIndividual.class);
		}
		
		public Individual<?,?> newIndividual(ManagedIndividualId id) {
			return dataSet.individual(id, ManagedIndividual.class);
		}

		public Value resourceSurrogate(Resource member) {
			ResourceId resourceId = member.id();
			ManagedIndividualId surrogateId = ManagedIndividualId.createId(resourceId.name(), resourceId.templateId());
			return dataSet.individualOfId(surrogateId);
		}

		public Value value(Object value) {
			return DataSetUtils.newLiteral(value);
		}
	}

	private final ResourceType type;
	private final Resource parentId;

	private ResourceId id;

	private DataSet content;
	private EntityTag etag;
	private Date lastModified;
	
	protected ResourceImpl(ResourceType type, Resource parent) {
		this.type = type;
		this.parentId = parent;
	}

	protected ResourceImpl(ResourceType type) {
		this(type,null);
	}
	
	public ResourceImpl(Resource parent) {
		this(ResourceType.RESOURCE,parent);
	}
	
	public ResourceImpl() {
		this(ResourceType.RESOURCE,null);
	}
	
	@Override
	public final ResourceType type() {
		return type;
	}
	
	@Override
	public final ResourceId id() {
		return id;
	}

	void setIdentifier(ResourceId id) {
		this.id = id;
	}

	@Override
	public final EntityTag entityTag() {
		return etag;
	}

	void setEntityTag(EntityTag etag) {
		this.etag = etag;
	}
	
	@Override
	public final Date lastModified() {
		return new Date(lastModified.getTime());
	}

	void setLastModified(Date lastModified) {
		this.lastModified = new Date(lastModified.getTime());
	}

	public final DataSet content() {
		if(content==null) {
			this.content=createDataSet();
		}
		return content;
	}

	private DataSet createDataSet() {
		return DataSetFactory.createDataSet(id().name());
	}

	void setContent(DataSet content) {
		if(content!=null) {
			this.content = content;
		}
	}

	public final DataSet metadata() {
		DataSet dataSet=
			createDataSet();
		ResourceId resourceId = id();
		ManagedIndividualId id=ManagedIndividualId.createId(resourceId.name(), resourceId.templateId());
		fillInMetadata(
			dataSet.individual(id,ManagedIndividual.class), 
			new Context(dataSet));
		return dataSet;
	}

	protected void fillInMetadata(Individual<?, ?> individual, Context ctx) {
		individual.
			addValue(
				ctx.property(RDF.TYPE), 
				ctx.reference(LDP.RESOURCE));
	}
	
	@Override
	public final Entity entity() {
		DataSet dataSet=createDataSet();
		DataSetUtils.merge(metadata(), dataSet);
		DataSetUtils.merge(content(), dataSet);
		return EntityFactory.createEntity(dataSet);
		
	}

	@Override
	public final Resource parent() {
		return parentId;
	}

}