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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.session;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.resource.Attachment;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.Member;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceVisitor;
import org.ldp4j.application.kernel.session.AttachmentSnapshotCollection.DelegatedAttachmentSnapshot;
import org.ldp4j.application.kernel.template.AttachedTemplate;
import org.ldp4j.application.kernel.template.ContainerTemplate;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.session.ResourceSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

abstract class PersistencyState {

	private static final Logger LOGGER=LoggerFactory.getLogger(PersistencyState.class);

	private static final class PersistentResourceReferenceState extends PersistencyState {

		private PersistentResourceReferenceState(ResourceId resourceId, ResourceTemplate template) {
			super(resourceId,template);
		}

		private PersistencyState resolve(DelegatedResourceSnapshot ctx) {
			Resource resource=ctx.session().loadResource(resourceId());
			PersistencyState newPersistencyState=PersistencyState.newPersistentState(resource,template(),ctx.session());
			ctx.setPersistencyState(newPersistencyState);
			return newPersistencyState;
		}

		@Override
		Resource delegate(DelegatedResourceSnapshot ctx) {
			return resolve(ctx).delegate(ctx);
		}

		@Override
		Set<DelegatedAttachmentSnapshot> attachments(DelegatedResourceSnapshot ctx) {
			return resolve(ctx).attachments(ctx);
		}

		@Override
		DelegatedAttachmentSnapshot attachmentByResource(ResourceSnapshot resource, DelegatedResourceSnapshot ctx) {
			return resolve(ctx).attachmentByResource(resource, ctx);
		}

		@Override
		DelegatedAttachmentSnapshot attachmentById(String attachmentId, DelegatedResourceSnapshot ctx) {
			return resolve(ctx).attachmentById(attachmentId, ctx);
		}

		@Override
		<T extends DelegatedResourceSnapshot> T createAttachedResource(
				Class<? extends T> snapshotClass,
				String attachmentId,
				Name<?> name,
				Class<? extends ResourceHandler> handlerClass,
				DelegatedResourceSnapshot ctx) {
			return resolve(ctx).createAttachedResource(snapshotClass,attachmentId,name,handlerClass,ctx);
		}

		@Override
		boolean removeAttachment(DelegatedAttachmentSnapshot attachment, DelegatedResourceSnapshot ctx) {
			return resolve(ctx).removeAttachment(attachment, ctx);
		}

		@Override
		boolean softDetach(DelegatedAttachmentSnapshot attachment, DelegatedResourceSnapshot ctx) {
			return resolve(ctx).softDetach(attachment, ctx);
		}

		@Override
		boolean softRemoveMember(DelegatedResourceSnapshot snapshot, DelegatedResourceSnapshot ctx) {
			return resolve(ctx).softRemoveMember(snapshot, ctx);
		}

		@Override
		Set<DelegatedResourceSnapshot> members(DelegatedResourceSnapshot ctx) {
			return resolve(ctx).members(ctx);
		}

		@Override
		DelegatedResourceSnapshot addMember(Name<?> resourceName, DelegatedResourceSnapshot ctx) {
			return resolve(ctx).addMember(resourceName, ctx);
		}

		@Override
		boolean hasMember(DelegatedResourceSnapshot resource, DelegatedResourceSnapshot ctx) {
			return resolve(ctx).hasMember(resource, ctx);
		}

		@Override
		boolean removeMember(DelegatedResourceSnapshot resource, DelegatedResourceSnapshot ctx) {
			return resolve(ctx).removeMember(resource, ctx);
		}

		@Override
		List<DelegatedResourceSnapshot> newMembers(DelegatedResourceSnapshot ctx) {
			return resolve(ctx).newMembers(ctx);
		}

		@Override
		void saveChanges(DelegatedResourceSnapshot ctx) {
			// Nothing to do
		}

	}

	private abstract static class BasePersistencyState extends PersistencyState {

		private final AttachmentSnapshotCollection attachments;
		private final MemberCollection members;

		private BasePersistencyState(ResourceId resourceId, ResourceTemplate template, AttachmentSnapshotCollection attachmentRepository, MemberCollection memberRepository) {
			super(resourceId,template);
			this.attachments=attachmentRepository;
			this.members=memberRepository;
		}

		private NameFilter nameFilterForAttachment(final String attachmentId) {
			return new NameFilter() {
				@Override
				public boolean isValid(Name<?> name) {
					return attachments.attachability(attachmentId,name).canAttach();
				}
			};
		}

		@Override
		Set<DelegatedAttachmentSnapshot> attachments(DelegatedResourceSnapshot ctx) {
			return this.attachments.attachments();
		}

		@Override
		DelegatedAttachmentSnapshot attachmentByResource(ResourceSnapshot resource, DelegatedResourceSnapshot ctx) {
			return this.attachments.findByResource(resource);
		}

		@Override
		DelegatedAttachmentSnapshot attachmentById(String attachmentId, DelegatedResourceSnapshot ctx) {
			return this.attachments.findById(attachmentId);
		}

		@Override
		<T extends DelegatedResourceSnapshot> T createAttachedResource(
				Class<? extends T> snapshotClass,
				String attachmentId,
				Name<?> name,
				Class<? extends ResourceHandler> handlerClass,
				DelegatedResourceSnapshot ctx) {
			AttachedTemplate attachedTemplate = template().attachedTemplate(attachmentId);
			checkState(attachedTemplate!=null,"No attachment '%s' is defined for template '%s'",attachmentId,template());
			checkState(attachedTemplate.template().handlerClass().isAssignableFrom(handlerClass),"Attachment '%s' of template '%s' is not compatible with '%s' (%s)",attachmentId,template().id(),handlerClass.getCanonicalName(),attachedTemplate.template().handlerClass().getCanonicalName());
			checkState(nameFilterForAttachment(attachmentId).isValid(name),"Resource name '%s' is already in use",name);
			T newSnapshot= ctx.newChildResource(ResourceId.createId(name,attachedTemplate.template()),snapshotClass);
			DelegatedAttachmentSnapshot newAttachment = AttachmentSnapshotCollection.newAttachment(attachmentId, newSnapshot);
			this.attachments.add(newAttachment);
			UnitOfWork.getCurrent().registerDirty(ctx);
			newSnapshot.markNew();
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Attached '{}' as '{}' to '{}'",newSnapshot.resourceId(),attachmentId,ctx.resourceId());
			}
			return snapshotClass.cast(newSnapshot);
		}


		@Override
		boolean removeAttachment(DelegatedAttachmentSnapshot attachment, DelegatedResourceSnapshot ctx) {
			boolean detached = softDetach(attachment,ctx);
			if(detached) {
				attachment.resource().deleteResource();
			}
			return detached;
		}

		@Override
		boolean softDetach(DelegatedAttachmentSnapshot delegatedAttachment, DelegatedResourceSnapshot ctx) {
			if(delegatedAttachment==null) {
				return false;
			}
			boolean result=this.attachments.remove(delegatedAttachment);
			if(result) {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Dettached '{}' ({}) from '{}'",delegatedAttachment.resource().resourceId(),delegatedAttachment.id(),ctx.resourceId());
				}
				UnitOfWork.getCurrent().registerDirty(ctx);
			}
			return result;
		}

		@Override
		Set<DelegatedResourceSnapshot> members(DelegatedResourceSnapshot ctx) {
			return this.members.members();
		}

		@Override
		boolean hasMember(DelegatedResourceSnapshot resource, DelegatedResourceSnapshot ctx) {
			return this.members.hasMember(resource);
		}

		@Override
		DelegatedResourceSnapshot addMember(Name<?> resourceName, DelegatedResourceSnapshot ctx) {
			checkNotNull(resourceName,"Member resource name cannot be null");
			ContainerTemplate containerTemplate=(ContainerTemplate)template();
			DelegatedResourceSnapshot newMember=ctx.newChildResource(ResourceId.createId(resourceName, containerTemplate.memberTemplate()),DelegatedResourceSnapshot.class);
			this.members.addMember(newMember);
			newMember.markNew();
			ctx.markDirty();
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Added member '{}' to '{}'",newMember.resourceId(),ctx.resourceId());
			}
			return newMember;
		}

		@Override
		boolean removeMember(DelegatedResourceSnapshot snapshot, DelegatedResourceSnapshot ctx) {
			boolean removed = softRemoveMember(snapshot,ctx);
			if(removed) {
				snapshot.deleteResource();
			}
			return removed;
		}

		@Override
		boolean softRemoveMember(DelegatedResourceSnapshot delegatedSnapshot, DelegatedResourceSnapshot ctx) {
			if(delegatedSnapshot==null) {
				return false;
			}
			boolean result = this.members.removeMember(delegatedSnapshot);
			if(result) {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Removed member '{}' from '{}'",delegatedSnapshot.resourceId(),ctx.resourceId());
				}
				UnitOfWork.getCurrent().registerDirty(ctx);
			}
			return result;
		}

		@Override
		protected void toString(ToStringHelper helper) {
			super.toString(helper);
			helper.
				add("attachments",this.attachments).
				add("members",this.members);
		}

		@Override
		List<DelegatedResourceSnapshot> newMembers(DelegatedResourceSnapshot ctx) {
			return this.members.newMembers();
		}

	}

	private static final class PersistentResourceState extends BasePersistencyState {

		private final class ResourceSaver implements ResourceVisitor {

			@Override
			public void visitResource(Resource resource) {
				for(DelegatedAttachmentSnapshot deleted:PersistentResourceState.this.deletedAttachments.values()) {
					Attachment attachment = resource.findAttachment(deleted.resource().resourceId());
					resource.detach(attachment);
				}
				for(DelegatedAttachmentSnapshot attachment:PersistentResourceState.this.newAttachments.values()) {
					DelegatedResourceSnapshot attachedResource = attachment.resource();
					Resource attach = resource.attach(attachment.id(), attachedResource.resourceId());
					attachedResource.setDelegate(attach);
				}
			}

			@Override
			public void visitContainer(Container resource) {
				visitResource(resource);
				for(ResourceId id:PersistentResourceState.this.deletedMembers.keySet()) {
					Member member=resource.findMember(id);
					resource.removeMember(member);
				}
				for(DelegatedResourceSnapshot member:PersistentResourceState.this.newMembers.values()) {
					Resource addedResource = resource.addMember(member.resourceId());
					member.setDelegate(addedResource);
				}
			}

		}

		private final Map<String,DelegatedAttachmentSnapshot> newAttachments;
		private final Map<String,DelegatedAttachmentSnapshot> deletedAttachments;
		private final Map<ResourceId,DelegatedResourceSnapshot> newMembers;
		private final Map<ResourceId,DelegatedResourceSnapshot> deletedMembers;

		private PersistentResourceState(Resource delegate, ResourceTemplate template, AttachmentSnapshotCollection attachmentRepository, MemberCollection memberRepository) {
			super(delegate.id(),template,attachmentRepository,memberRepository);
			setDelegate(delegate);
			this.newAttachments=new LinkedHashMap<String,DelegatedAttachmentSnapshot>();
			this.deletedAttachments=new LinkedHashMap<String,DelegatedAttachmentSnapshot>();
			this.newMembers=new LinkedHashMap<ResourceId, DelegatedResourceSnapshot>();
			this.deletedMembers=new LinkedHashMap<ResourceId, DelegatedResourceSnapshot>();
		}

		private void registerNewAttachment(String attachmentId, DelegatedResourceSnapshot ctx) {
			DelegatedAttachmentSnapshot attachment = super.attachmentById(attachmentId, ctx);
			this.newAttachments.put(attachmentId, attachment);
		}

		private void registerDeletedAttachment(DelegatedAttachmentSnapshot attachment) {
			if(this.newAttachments.containsKey(attachment.id())) {
				this.newAttachments.remove(attachment.id());
			} else {
				this.deletedAttachments.put(attachment.id(),attachment);
			}
		}

		private void registerNewMember(DelegatedResourceSnapshot newResource) {
			this.newMembers.put(newResource.resourceId(), newResource);
		}

		private void registerDeletedMember(DelegatedResourceSnapshot snapshot) {
			if(this.newMembers.containsKey(snapshot)) {
				this.newMembers.remove(snapshot.resourceId());
			} else {
				this.deletedMembers.put(snapshot.resourceId(), snapshot);
			}
		}

		@Override
		<T extends DelegatedResourceSnapshot> T createAttachedResource(
				Class<? extends T> snapshotClass,
				String attachmentId,
				Name<?> name,
				Class<? extends ResourceHandler> handlerClass,
				DelegatedResourceSnapshot ctx) {
			T attachment = super.createAttachedResource(snapshotClass,attachmentId,name,handlerClass,ctx);
			registerNewAttachment(attachmentId, ctx);
			return attachment;
		}

		@Override
		boolean removeAttachment(DelegatedAttachmentSnapshot attachment, DelegatedResourceSnapshot ctx) {
			boolean deleted = super.removeAttachment(attachment, ctx);
			if(deleted) {
				registerDeletedAttachment(attachment);
			}
			return deleted;
		}

		@Override
		boolean softDetach(DelegatedAttachmentSnapshot attachment, DelegatedResourceSnapshot ctx) {
			boolean deleted = super.softDetach(attachment, ctx);
			if(deleted) {
				registerDeletedAttachment(attachment);
			}
			return deleted;
		}

		@Override
		DelegatedResourceSnapshot addMember(Name<?> resourceName, DelegatedResourceSnapshot ctx) {
			DelegatedResourceSnapshot newResource = super.addMember(resourceName, ctx);
			registerNewMember(newResource);
			return newResource;
		}

		@Override
		boolean removeMember(DelegatedResourceSnapshot snapshot, DelegatedResourceSnapshot ctx) {
			boolean deleted = super.removeMember(snapshot, ctx);
			if(deleted) {
				registerDeletedMember(snapshot);
			}
			return deleted;
		}

		@Override
		boolean softRemoveMember(DelegatedResourceSnapshot snapshot, DelegatedResourceSnapshot ctx) {
			boolean deleted = super.softRemoveMember(snapshot, ctx);
			if(deleted) {
				registerDeletedMember(snapshot);
			}
			return deleted;
		}

		@Override
		void saveChanges(final DelegatedResourceSnapshot ctx) {
			delegate(ctx).accept(new ResourceSaver());
			this.deletedAttachments.clear();
			this.newAttachments.clear();
			this.deletedMembers.clear();
			this.newMembers.clear();
		}

		@Override
		protected void toString(ToStringHelper helper) {
			super.toString(helper);
			helper.
				add("newAttachments",this.newAttachments).
				add("deletedAttachments",this.deletedAttachments).
				add("newMembers",this.newMembers).
				add("deletedMembers",this.deletedMembers);
		}

	}

	private static final class TransientResourceState extends BasePersistencyState {

		private final class ResourceSaver implements ResourceVisitor {

			private final DelegatedResourceSnapshot ctx;

			private ResourceSaver(DelegatedResourceSnapshot ctx) {
				this.ctx = ctx;
			}

			@Override
			public void visitResource(Resource resource) {
				for(DelegatedAttachmentSnapshot attachment:TransientResourceState.this.attachments(ctx)) {
					DelegatedResourceSnapshot attachedResource = attachment.resource();
					Resource attach = resource.attach(attachment.id(), attachedResource.resourceId());
					attachedResource.setDelegate(attach);
				}
			}

			@Override
			public void visitContainer(Container resource) {
				visitResource(resource);
				for(DelegatedResourceSnapshot member:TransientResourceState.this.members(ctx)) {
					Resource newResource = resource.addMember(member.resourceId());
					member.setDelegate(newResource);
				}
			}

		}

		private TransientResourceState(ResourceId resourceId, ResourceTemplate template) {
			super(resourceId,template,AttachmentSnapshotCollection.newInstance(),MemberCollection.newInstance());
		}

		@Override
		void saveChanges(final DelegatedResourceSnapshot ctx) {
			delegate(ctx).accept(new ResourceSaver(ctx));
		}

	}

	private static final class DeletedResourceState extends PersistencyState {

		private final String failureMessage;

		private DeletedResourceState(ResourceId resourceId, ResourceTemplate template, Resource delegate) {
			super(resourceId,template);
			setDelegate(delegate);
			this.failureMessage = "Resource '"+resourceId+"' has been deleted";
		}


		@Override
		Set<DelegatedAttachmentSnapshot> attachments(DelegatedResourceSnapshot ctx) {
			return Collections.emptySet();
		}

		@Override
		DelegatedAttachmentSnapshot attachmentByResource(ResourceSnapshot resource, DelegatedResourceSnapshot ctx) {
			throw new IllegalStateException(this.failureMessage);
		}

		@Override
		DelegatedAttachmentSnapshot attachmentById(String attachmentId, DelegatedResourceSnapshot ctx) {
			throw new IllegalStateException(this.failureMessage);
		}


		@Override
		<T extends DelegatedResourceSnapshot> T createAttachedResource(
				Class<? extends T> snapshotClass,
				String attachmentId,
				Name<?> name,
				Class<? extends ResourceHandler> handlerClass,
				DelegatedResourceSnapshot ctx) {
			throw new IllegalStateException(this.failureMessage);
		}

		@Override
		boolean removeAttachment(DelegatedAttachmentSnapshot attachment, DelegatedResourceSnapshot ctx) {
			return false;
		}

		@Override
		boolean softDetach(DelegatedAttachmentSnapshot attachment, DelegatedResourceSnapshot delegatedResourceSnapshot) {
			return false;
		}

		@Override
		Set<DelegatedResourceSnapshot> members(DelegatedResourceSnapshot ctx) {
			return Collections.emptySet();
		}

		@Override
		DelegatedResourceSnapshot addMember(Name<?> resourceName, DelegatedResourceSnapshot ctx) {
			throw new IllegalStateException(this.failureMessage);
		}

		@Override
		boolean hasMember(DelegatedResourceSnapshot resource, DelegatedResourceSnapshot ctx) {
			return false;
		}

		@Override
		boolean removeMember(DelegatedResourceSnapshot resource, DelegatedResourceSnapshot ctx) {
			return false;
		}

		@Override
		boolean softRemoveMember(DelegatedResourceSnapshot snapshot, DelegatedResourceSnapshot ctx) {
			return false;
		}

		@Override
		void saveChanges(DelegatedResourceSnapshot ctx) {
			// Nothing to do
		}

		@Override
		List<DelegatedResourceSnapshot> newMembers(DelegatedResourceSnapshot ctx) {
			throw new IllegalStateException(this.failureMessage);
		}

	}

	private final ResourceId resourceId;
	private final ResourceTemplate template;
	private Resource delegate;

	private PersistencyState(ResourceId resourceId, ResourceTemplate resourceTemplate) {
		this.resourceId = resourceId;
		template = resourceTemplate;
	}

	final ResourceId resourceId() {
		return this.resourceId;
	}

	final ResourceTemplate template() {
		return this.template;
	}

	final Resource delegate() {
		checkState(this.delegate!=null,"Delegate not initialized yet");
		return this.delegate;
	}

	Resource delegate(DelegatedResourceSnapshot ctx) { // NOSONAR
		return delegate();
	}

	final void setDelegate(Resource delegate) {
		checkState(this.delegate==null,"Delegate already initialized");
		this.delegate=delegate;
	}

	abstract Set<DelegatedAttachmentSnapshot> attachments(DelegatedResourceSnapshot ctx);

	abstract DelegatedAttachmentSnapshot attachmentByResource(ResourceSnapshot resource, DelegatedResourceSnapshot ctx);

	abstract DelegatedAttachmentSnapshot attachmentById(String attachmentId, DelegatedResourceSnapshot ctx);

	abstract <T extends DelegatedResourceSnapshot> T createAttachedResource(
			Class<? extends T> snapshotClass,
			String attachmentId,
			Name<?> name,
			Class<? extends ResourceHandler> handlerClass,
			DelegatedResourceSnapshot ctx);

	abstract boolean removeAttachment(DelegatedAttachmentSnapshot attachment, DelegatedResourceSnapshot ctx);

	abstract Set<DelegatedResourceSnapshot> members(DelegatedResourceSnapshot ctx);

	abstract DelegatedResourceSnapshot addMember(Name<?> resourceName, DelegatedResourceSnapshot ctx);

	abstract boolean hasMember(DelegatedResourceSnapshot resource, DelegatedResourceSnapshot ctx);

	abstract boolean removeMember(DelegatedResourceSnapshot resource, DelegatedResourceSnapshot ctx);

	abstract void saveChanges(DelegatedResourceSnapshot ctx);

	abstract boolean softDetach(DelegatedAttachmentSnapshot attachment, DelegatedResourceSnapshot delegatedResourceSnapshot);

	abstract boolean softRemoveMember(DelegatedResourceSnapshot snapshot, DelegatedResourceSnapshot ctx);

	@Override
	public final String toString() {
		ToStringHelper helper=MoreObjects.toStringHelper(getClass());
		toString(helper);
		return helper.toString();
	}

	protected void toString(ToStringHelper helper) {
		helper.
			omitNullValues().
			add("resourceId", resourceId).
			add("template().handlerClass()",template().handlerClass().getCanonicalName());
	}

	static PersistencyState newPersistentReferenceState(ResourceId resourceId, ResourceTemplate template) {
		return new PersistentResourceReferenceState(resourceId, template);
	}

	static PersistencyState newPersistentState(Resource resource, ResourceTemplate template, DelegatedWriteSession session) {
		AttachmentSnapshotCollection attachments = AttachmentSnapshotCollection.createFromResource(resource, session);
		MemberCollection members=MemberCollection.createFromResource(resource, session);
		return new PersistentResourceState(resource,template,attachments,members);
	}

	static PersistencyState newTransientState(ResourceId resourceId, ResourceTemplate template) {
		return new TransientResourceState(resourceId, template);
	}

	static PersistencyState deleted(DelegatedResourceSnapshot snapshot) {
		return new DeletedResourceState(snapshot.resourceId(),snapshot.template(),snapshot.delegate());
	}

	abstract List<DelegatedResourceSnapshot> newMembers(DelegatedResourceSnapshot ctx);

}