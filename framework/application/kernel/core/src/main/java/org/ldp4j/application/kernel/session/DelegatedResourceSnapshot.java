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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ldp4j.application.session.AttachmentSnapshot;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SnapshotVisitor;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.session.AttachmentSnapshotCollection.DelegatedAttachmentSnapshot;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

class DelegatedResourceSnapshot implements ResourceSnapshot {

	private final class ChildrenCollector {

		private final class Walker implements DelegatedSnapshotVisitor {

			private List<DelegatedResourceSnapshot> children=new ArrayList<DelegatedResourceSnapshot>();

			private void traverseAttachments(DelegatedResourceSnapshot resource) {
				for(DelegatedAttachmentSnapshot attachment:resource.attachments()) {
					attachment.resource().accept(this);
				}
			}

			@Override
			public void visitDelegatedResourceSnapshot(DelegatedResourceSnapshot resource) {
				traverseAttachments(resource);
				children.add(resource);
			}

			@Override
			public void visitDelegatedContainerSnapshot(DelegatedContainerSnapshot resource) {
				traverseAttachments(resource);
				for(DelegatedResourceSnapshot member:resource.members()) {
					member.accept(this);
				}
				children.add(resource);
			}

		}

		public List<DelegatedResourceSnapshot> getChildren() {
			Walker walker = new Walker();
			DelegatedResourceSnapshot.this.accept(walker);
			return new ArrayList<DelegatedResourceSnapshot>(walker.children);
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(DelegatedResourceSnapshot.class);

	private ParentState parentState;

	private DelegatedWriteSession session;

	private PersistencyState persistencyState;

	private final ResourceId resourceId;

	private final Class<? extends ResourceHandler> handlerClass;

	protected DelegatedResourceSnapshot(ResourceId resourceId, Class<? extends ResourceHandler> handlerClass) {
		this.resourceId = resourceId;
		this.handlerClass = handlerClass;
	}

	private final <T> T logTransition(String concern, T oldState, T newState) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(String.format("%s(%s).%s [from:%s, to:%s]",getClass().getSimpleName(),resourceId(),concern,oldState!=null?oldState.getClass().getSimpleName():null,newState.getClass().getSimpleName()));
		}
		return newState;
	}

	private final boolean softDetach(AttachmentSnapshot attachment) {
		if(attachment==null) {
			return false;
		}
		checkArgument(attachment instanceof DelegatedAttachmentSnapshot,"Unsupported attachment type");
		return this.persistencyState.softDetach((DelegatedAttachmentSnapshot)attachment, this);
	}

	private final <T extends ResourceSnapshot> Class<? extends DelegatedResourceSnapshot> getDelegatedClass(Class<? extends T> clazz) {
		checkArgument(clazz==ResourceSnapshot.class || clazz==ContainerSnapshot.class,"Unsupported snapshot class");
		Class<? extends DelegatedResourceSnapshot> delegatedClass=DelegatedResourceSnapshot.class;
		if(clazz==ContainerSnapshot.class) {
			delegatedClass=DelegatedContainerSnapshot.class;
		}
		return delegatedClass;
	}

	protected final void setDelegate(Resource delegate) {
		this.persistencyState.setDelegate(delegate);
	}

	protected final PersistencyState persistencyState() {
		return persistencyState;
	}

	protected final void markNew() {
		UnitOfWork.getCurrent().registerNew(this);
	}

	protected final void markDirty() {
		UnitOfWork.getCurrent().registerDirty(this);
	}

	protected final void deleteResource() {
		if(!isRoot()) {
			DelegatedResourceSnapshot snapshot=parent();
			AttachmentSnapshot attachment = snapshot.attachmentByResource(this);
			if(attachment!=null) {
				snapshot.softDetach(attachment);
				UnitOfWork.getCurrent().registerDirty(snapshot);
			} else {
				deleteAttachedResource(snapshot);
			}
		}
		ChildrenCollector collector = new ChildrenCollector();
		for(DelegatedResourceSnapshot toRemove:collector.getChildren()) {
			toRemove.setParentState(ParentState.orphan());
			toRemove.setPersistencyState(PersistencyState.deleted(toRemove));
			UnitOfWork.getCurrent().registerDeleted(toRemove);
		}
	}

	private void deleteAttachedResource(DelegatedResourceSnapshot snapshot) {
		TemplateIntrospector introspector = TemplateIntrospector.newInstance(parent().template());
		if(!introspector.isContainer()) {
			return;
		}
		DelegatedContainerSnapshot parent=(DelegatedContainerSnapshot)snapshot;
		if(parent.softRemoveMember(this)) {
			UnitOfWork.getCurrent().registerDirty(parent);
			if(introspector.isMembershipAwareContainer() && !parent.isRoot()) {
				UnitOfWork.getCurrent().registerDirty(parent.parent());
			}
		}
	}

	protected final <T extends DelegatedResourceSnapshot> T newChildResource(ResourceId resourceId, Class<? extends T> clazz) {
		return session().newTransient(resourceId,this,clazz);
	}

	final void setSession(DelegatedWriteSession session) {
		checkNotNull(session,"Write session cannot be null");
		checkState(this.session==null || this.session==session,"Session already initialized");
		this.session = session;
	}

	final void setPersistencyState(PersistencyState persistencyState) {
		logTransition("persistency",this.persistencyState, persistencyState);
		this.persistencyState=persistencyState;
	}

	final void setParentState(ParentState parentState) {
		logTransition("parent", this.parentState, parentState);
		this.parentState = parentState;
	}

	final ResourceId resourceId() {
		return this.resourceId;
	}

	ResourceTemplate template() {
		return this.persistencyState.template();
	}

	final DelegatedWriteSession session() {
		checkState(session!=null,"Write session not initialized yet");
		return session;
	}

	Resource delegate() {
		return this.persistencyState.delegate(this);
	}

	final void modify() {
		markDirty();
	}

	final void delete() {
		deleteResource();
	}

	final void saveChanges() {
		this.persistencyState.saveChanges(this);
	}

	void accept(DelegatedSnapshotVisitor visitor) {
		visitor.visitDelegatedResourceSnapshot(this);
	}

	@Override
	public void accept(SnapshotVisitor visitor) {
		visitor.visitResourceSnapshot(this);
	}

	@Override
	public final Name<?> name() {
		return resourceId().name();
	}

	@Override
	public final String templateId() {
		return resourceId().templateId();
	}

	@Override
	public Class<? extends ResourceHandler> handlerClass() {
		return this.handlerClass;
	}

	@Override
	public final boolean isRoot() {
		return parentState.isRoot(this);
	}

	@Override
	public final DelegatedResourceSnapshot parent() {
		return parentState.parent(this);
	}

	@Override
	public final Set<? extends DelegatedAttachmentSnapshot> attachments() {
		return this.persistencyState.attachments(this);
	}

	@Override
	public final AttachmentSnapshot attachmentByResource(ResourceSnapshot resource) {
		return this.persistencyState.attachmentByResource(resource, this);
	}

	@Override
	public final AttachmentSnapshot attachmentById(String attachmentId) {
		return this.persistencyState.attachmentById(attachmentId, this);
	}

	@Override
	public final <T extends ResourceSnapshot> T createAttachedResource(Class<? extends T> clazz, String attachmentId, Name<?> name, Class<? extends ResourceHandler> handlerClass) {
		checkNotNull(clazz,"Snapshot class cannot be null");
		return clazz.cast(this.persistencyState.createAttachedResource(getDelegatedClass(clazz),attachmentId,name,handlerClass,this));
	}

	@Override
	public final boolean removeAttachment(AttachmentSnapshot attachment) {
		if(attachment==null) {
			return false;
		}
		checkArgument(attachment instanceof DelegatedAttachmentSnapshot,"Unsupported attachment type");
		DelegatedAttachmentSnapshot delegatedAttachment = (DelegatedAttachmentSnapshot)attachment;
		return this.persistencyState.removeAttachment(delegatedAttachment,this);
	}

	@Override
	public String toString() {
		return stringHelper().toString();
	}

	protected ToStringHelper stringHelper() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("resourceId",this.resourceId).
					add("handlerClass",this.handlerClass.getCanonicalName()).
					add("persistencyState",this.persistencyState).
					add("parentState",this.parentState);
	}

}