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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Date;

import javax.ws.rs.core.EntityTag;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.server.resources.IndirectContainer;
import org.ldp4j.server.resources.MembershipAwareContainer;
import org.ldp4j.server.resources.MembershipRelation;

public abstract class ResourceBuilder<T extends ResourceImpl> {

	public static class SimpleResourceBuilder extends ResourceBuilder<ResourceImpl> {

		private SimpleResourceBuilder(ResourceId name) {
			super(name);
		}
		
		@Override
		protected void postProcess(ResourceImpl resource) {
			// Nothing to do
		}

		@Override
		protected ResourceImpl createResource() {
			return new ResourceImpl();
		}
		
	}
	
	public static class ContainerBuilder extends ResourceBuilder<ContainerImpl> {
		
		private URI membershipPredicate;
		private MembershipRelation membershipRelation;
		private Name<?> membershipTarget;
		private URI insertedContentRelation;

		private ContainerBuilder(ResourceId name) {
			super(name);
		}
		
		public ContainerBuilder withMembershipPredicate(String membershipPredicate) {
			return withMembershipPredicate(URI.create(filterString(membershipPredicate)));
		}

		public ContainerBuilder withMembershipPredicate(URI membershipPredicate) {
			this.membershipPredicate = membershipPredicate;
			return this;
		}
		
		public ContainerBuilder withMembershipRelation(MembershipRelation membershipRelation) {
			this.membershipRelation = membershipRelation;
			return this;
		}
		
		public ContainerBuilder withMembershipTarget(String membershipTarget) {
			return withMembershipTarget(NamingScheme.getDefault().name(filterString(membershipTarget)));
		}
		
		public ContainerBuilder withMembershipTarget(Name<?> membershipTarget) {
			this.membershipTarget = membershipTarget;
			return this;
		}

		public ContainerBuilder withInsertedContentRelation(String insertedContentRelation) {
			return withInsertedContentRelation(URI.create(filterString(insertedContentRelation)));
		}

		public ContainerBuilder withInsertedContentRelation(URI insertedContentRelation) {
			this.insertedContentRelation = insertedContentRelation;
			return this;
		}

		@Override
		protected ContainerImpl createResource() {
			ContainerImpl result=null;
			if(insertedContentRelation!=null) {
				result=new IndirectContainerImpl();
			} else if(membershipTarget!=null) {
				result=new DirectContainerImpl();
			} else  {
				result=new BasicContainerImpl();
			}
			return result;
		}

		@Override
		protected void postProcess(ContainerImpl resource) {
			if(resource instanceof MembershipAwareContainer) {
				MembershipAwareContainerImpl container=(MembershipAwareContainerImpl)resource;
				container.setMembershipPredicate(membershipPredicate);
				container.setMembershipRelation(membershipRelation);
				container.setMembershipTarget(membershipTarget);
				if(container instanceof IndirectContainer) {
					IndirectContainerImpl indirectContainer=(IndirectContainerImpl)resource;
					indirectContainer.setInsertedContentRelation(insertedContentRelation);
				}
			}
		}

	}
	
	private final ResourceId id;

	private EntityTag etag;
	private Date lastModified;
	private DataSet content;

	private ResourceBuilder(ResourceId id) {
		this.id = id;
	}
	
	public ResourceBuilder<T> withEntityTag(EntityTag etag) {
		this.etag = etag;
		return this;
	}
	
	public ResourceBuilder<T> withLastModified(Date date) {
		this.lastModified = new Date(date.getTime());
		return this;
	}
	
	public ResourceBuilder<T> withContent(DataSet content) {
		this.content = content;
		return this;
	}

	public T build() {
		T resource=createResource();
		resource.setIdentifier(id);
		setContent(resource);
		setEntityTag(resource);
		setLastModified(resource);
		postProcess(resource);
		return resource;
	}

	protected abstract void postProcess(T resource);

	protected abstract T createResource();

	private static String filterString(String string) {
		String result=null;
		if(string!=null) {
			result=string.trim();
			if(result.isEmpty()) {
				result=null;
			}
		}
		return result;
	}

	private void setEntityTag(ResourceImpl resource) {
		EntityTag realEntityTag = 
			etag!=null?
				etag :
				new EntityTag(
					Integer.toHexString(resource.hashCode()),true) ;
		resource.setEntityTag(realEntityTag);
	}

	private void setContent(ResourceImpl resource) {
		DataSet realContent = 
			content!=null?
				content:
				DataSetFactory.createDataSet(resource.id().name());
		resource.setContent(realContent);
	}
	
	private void setLastModified(ResourceImpl resource) {
		Date realLastModified=
			lastModified!=null?
				lastModified:
				new Date();
		resource.setLastModified(realLastModified);
	}

	public static SimpleResourceBuilder newResource(Name<?> name, String templateId) {
		checkNotNull(name,"Object 'id' cannot be null");
		return new SimpleResourceBuilder(ResourceId.createId(name, templateId));
	}

	public static ContainerBuilder newContainer(Name<?> name, String templateId) {
		checkNotNull(name,"Object 'id' cannot be null");
		return new ContainerBuilder(ResourceId.createId(name, templateId));
	}
}
