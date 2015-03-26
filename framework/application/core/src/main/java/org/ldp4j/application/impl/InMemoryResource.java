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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.resource.Attachment;
import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.resource.ResourceVisitor;
import org.ldp4j.application.template.BasicContainerTemplate;
import org.ldp4j.application.template.ContainerTemplate;
import org.ldp4j.application.template.DirectContainerTemplate;
import org.ldp4j.application.template.IndirectContainerTemplate;
import org.ldp4j.application.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateIntrospector;
import org.ldp4j.application.template.TemplateVisitor;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

class InMemoryResource extends AbstractInMemoryResource implements Resource {

	private static final class AttachmentImpl implements Attachment {

		private final AttachmentId attachmentId;

		private AttachmentImpl(AttachmentId attachmentId) {
			this.attachmentId = attachmentId;
		}

		@Override
		public String toString() {
			return
				Objects.
					toStringHelper(getClass()).
						omitNullValues().
						add("id", id()).
						add("resourceId", resourceId()).
						toString();
		}

		private AttachmentId attachmentId() {
			return this.attachmentId;
		}

		@Override
		public String id() {
			return this.attachmentId.id();
		}


		@Override
		public ResourceId resourceId() {
			return this.attachmentId.resourceId();
		}

	}

	private final Map<AttachmentId, AttachmentImpl> attachments;
	private final Map<String, AttachmentId> attachmentsById;
	private final Map<ResourceId, AttachmentId> attachmentsByResourceId;

	private final ResourceId id;
	private final ResourceId parentId;
	private URI indirectId;

	protected InMemoryResource(ResourceId id, ResourceId parentId) {
		this.id=id;
		this.parentId = parentId;
		this.attachments=new LinkedHashMap<AttachmentId, AttachmentImpl>();
		this.attachmentsById=new LinkedHashMap<String,AttachmentId>();
		this.attachmentsByResourceId=new LinkedHashMap<ResourceId,AttachmentId>();
	}

	protected InMemoryResource(ResourceId id) {
		this(id,null);
	}

	protected final InMemoryResource createChild(ResourceId resourceId, ResourceTemplate template) {
		InMemoryResource newResource=null;
		if(!TemplateIntrospector.newInstance(template).isContainer()) {
			newResource=new InMemoryResource(resourceId,this.id);
		} else {
			newResource=new InMemoryContainer(resourceId,this.id);
		}
		newResource.setPersistencyManager(getPersistencyManager());
		return newResource;
	}

	private boolean areCompatible(final Class<? extends Resource> clazz, ResourceTemplate template) {
		final AtomicReference<Boolean> result=new AtomicReference<Boolean>();
		template.accept(
			new TemplateVisitor() {
				@Override
				public void visitResourceTemplate(ResourceTemplate template) {
					result.set(clazz.isAssignableFrom(Resource.class));
				}
				@Override
				public void visitContainerTemplate(ContainerTemplate template) {
					result.set(clazz.isAssignableFrom(Container.class));
				}
				@Override
				public void visitBasicContainerTemplate(BasicContainerTemplate template) {
					visitContainerTemplate(template);
				}
				@Override
				public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
					visitContainerTemplate(template);
				}
				@Override
				public void visitDirectContainerTemplate(DirectContainerTemplate template) {
					visitContainerTemplate(template);
				}
				@Override
				public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
					visitContainerTemplate(template);
				}
			}
		);
		return result.get();
	}

	@Override
	public ResourceId id() {
		return this.id;
	}

	@Override
	public void setIndirectId(URI indirectId) {
		this.indirectId=indirectId;
	}

	@Override
	public URI indirectId() {
		return this.indirectId;
	}

	@Override
	public boolean isRoot() {
		return this.parentId==null;
	}

	@Override
	public ResourceId parentId() {
		return this.parentId;
	}

	@Override
	public Attachment findAttachment(ResourceId resourceId) {
		checkNotNull(resourceId,"Attached resource identifier cannot be null");
		return this.attachments.get(this.attachmentsByResourceId.get(resourceId));
	}

	@Override
	public Resource attach(String attachmentId, ResourceId resourceId) {
		return attach(attachmentId,resourceId,Resource.class);
	}

	@Override
	public <T extends Resource> T attach(String attachmentId, ResourceId resourceId, Class<? extends T> clazz) {
		checkNotNull(attachmentId,"Attachment identifier cannot be null");
		checkNotNull(resourceId,"Attached resource identifier cannot be null");
		checkNotNull(clazz,"Attached resource class cannot be null");
		AttachmentId aId = AttachmentId.createId(attachmentId,resourceId);
		checkState(!attachments.containsKey(aId),"Resource '%s' is already attached as '%s'",resourceId,attachmentId);
		checkState(!attachmentsById.containsKey(attachmentId),"A resource is already attached as '%s'",attachmentId);
		checkState(!attachmentsByResourceId.containsKey(resourceId),"Resource '%s' is already attached",resourceId);
		ResourceTemplate attachmentTemplate=super.getTemplate(resourceId);
		checkState(areCompatible(clazz,attachmentTemplate),"Attachment '%s' is not of type '%s' (%s)",attachmentId,clazz.getCanonicalName(),attachmentTemplate.getClass().getCanonicalName());
		InMemoryResource newResource=createChild(resourceId,attachmentTemplate);
		AttachmentImpl newAttachment = new AttachmentImpl(aId);
		attachments.put(newAttachment.attachmentId(),newAttachment);
		attachmentsById.put(aId.id(),aId);
		attachmentsByResourceId.put(aId.resourceId(),aId);
		return clazz.cast(newResource);
	}

	@Override
	public boolean detach(Attachment attachment) {
		boolean found=this.attachments.containsValue(attachment);
		if(found) {
			AttachmentId remove = this.attachmentsById.remove(attachment.id());
			this.attachmentsByResourceId.remove(attachment.resourceId());
			this.attachments.remove(remove);
		}
		return found;
	}
	@Override
	public Set<AttachmentImpl> attachments() {
		return Collections.unmodifiableSet(new LinkedHashSet<AttachmentImpl>(this.attachments.values()));
	}

	@Override
	public void accept(ResourceVisitor visitor) {
		visitor.visitResource(this);
	}

	@Override
	public String toString() {
		return stringHelper().toString();
	}

	protected ToStringHelper stringHelper() {
		return
			super.stringHelper().
				add("id",this.id).
				add("parentId",this.parentId).
				add("attachments",this.attachments);
	}

}