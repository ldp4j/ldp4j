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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-kernel-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.session;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.resource.Attachment;
import org.ldp4j.application.kernel.resource.Member;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.session.AttachmentSnapshotCollection.DelegatedAttachmentSnapshot;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

final class DefaultJournal implements Journal {

	private static final class DefaultRecord implements Record {

		private final long id;
		private final long timestamp;
		private final Map<String,Object> fields;

		private DefaultRecord(long id, long timestamp, Map<String,Object> fields) {
			this.id=id;
			this.timestamp=timestamp;
			this.fields=fields;
		}

		@Override
		public long id() {
			return this.id;
		}

		@Override
		public long timestamp() {
			return this.timestamp;
		}

		private DefaultRecord withField(String key, Object value) {
			this.fields.put(key, value);
			return this;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						add("id", this.id).
						add("timestamp",timestamp).
						add("fields", this.fields).
						toString();
		}

		private static DefaultRecord newInstance(long id) {
			return new DefaultRecord(id,System.currentTimeMillis(),Maps.<String,Object>newLinkedHashMap());
		}

	}

	private final class DefaultJournaler implements Journaler {

		private Record populateMemberRecord(DefaultRecord record, DelegatedContainerSnapshot snapshot, DelegatedResourceSnapshot member) {
			return
				populateSnapshotInformation(record, snapshot).
					withField("member.id",id(member.resourceId())).
					withField("member.object",member.hashCode());
		}

		private Record populateAttachmentRecord(DefaultRecord record, DelegatedResourceSnapshot snapshot, DelegatedAttachmentSnapshot attachment) {
			return
				populateSnapshotInformation(record, snapshot).
					withField("attachment.id",attachment.id()).
					withField("attachment.object",attachment.hashCode()).
					withField("attachment.snapshot.id",id(attachment.resource().resourceId())).
					withField("attachment.snapshot.object",attachment.resource().hashCode());
		}

		private DefaultRecord populateSnapshotInformation(DefaultRecord record, DelegatedResourceSnapshot snapshot) {
			return
				record.
					withField("snapshot.id",id(snapshot.resourceId())).
					withField("snapshot.object",snapshot.hashCode());
		}

		private void populateResourceRecord(String operation, Resource resource, Endpoint endpoint) {
			newRecord(operation).
				withField("resource.id", id(resource.id())).
				withField("resource.object", resource.hashCode()).
				withField("endpoint.path", endpoint.path()).
				withField("endpoint.entityTag", endpoint.entityTag()).
				withField("endpoint.lastModified", endpoint.lastModified()).
				withField("endpoint.created", endpoint.created()).
				withField("endpoint.deleted", endpoint.deleted());
		}

		private void populateLoadRecord(String operation, ResourceId resourceId, Object resource) {
			DefaultRecord record=
				newRecord(operation).
					withField("id",id(resourceId));
			if(resource!=null) {
				record.withField("object",resource.hashCode());
			}
		}

		private String id(ResourceId id) {
			return id.templateId()+"{"+id.name().id()+"}";
		}

		@Override
		public void saveChanges() {
			newRecord("saveChanges");
		}

		@Override
		public void discardChanges() {
			newRecord("discardChanges");
		}

		@Override
		public void close() {
			newRecord("close");
		}

		@Override
		public void loadResource(ResourceId resourceId, Resource resource) {
			populateLoadRecord("loadResource", resourceId, resource);
		}

		@Override
		public void resolveSnapshot(ResourceId resourceId, DelegatedResourceSnapshot resource) {
			populateLoadRecord("resolveSnapshot", resourceId, resource);
		}

		@Override
		public void createTransientSnapshot(DelegatedResourceSnapshot snapshot, ResourceId id, DelegatedResourceSnapshot parent) {
			populateSnapshotInformation(newRecord("createdTransientSnapshot"),snapshot).
				withField("snapshot.parent.id",id(snapshot.resourceId())).
				withField("snapshot.parent.object",parent.hashCode());
		}

		@Override
		public void createPersistentSnapshot(DelegatedResourceSnapshot snapshot, Resource resource) {
			populateSnapshotInformation(newRecord("createdPersistentSnapshot"),snapshot).
				withField("snapshot.delegate",resource.hashCode());
		}

		@Override
		public void createMemberSnapshot(DelegatedContainerSnapshot snapshot, DelegatedResourceSnapshot member) {
			populateMemberRecord(newRecord("createMemberSnapshot"), snapshot, member);
		}

		@Override
		public void createAttachmentSnapshot(DelegatedResourceSnapshot snapshot, DelegatedAttachmentSnapshot attachment) {
			populateAttachmentRecord(newRecord("createAttachmentSnapshot"), snapshot, attachment);
		}

		@Override
		public void deleteMemberSnapshot(DelegatedContainerSnapshot snapshot, DelegatedResourceSnapshot member) {
			populateMemberRecord(newRecord("deleteMemberSnapshot"), snapshot, member);
		}

		@Override
		public void deleteAttachmentSnapshot(DelegatedResourceSnapshot snapshot, DelegatedAttachmentSnapshot attachment) {
			populateAttachmentRecord(newRecord("deleteAttachmentSnapshot"), snapshot, attachment);
		}

		@Override
		public void deleteSnapshot(DelegatedResourceSnapshot snapshot) {
			populateSnapshotInformation(newRecord("deleteSnapshot"), snapshot);
		}

		@Override
		public void modifySnapshot(DelegatedResourceSnapshot snapshot) {
			populateSnapshotInformation(newRecord("modifySnapshot"), snapshot);
		}

		@Override
		public void createAttachment(DelegatedResourceSnapshot snapshot, DelegatedAttachmentSnapshot attachment, Attachment createdAttachment) {
			Resource resource=snapshot.delegate();
			DelegatedResourceSnapshot attachedSnapshot = attachment.resource();
			Resource attachedResource=attachedSnapshot.delegate();
			populateSnapshotInformation(newRecord("createAttachment"),snapshot).
				withField("snapshot.delegate",resource.hashCode()).
				withField("attachment.id",attachment.id()).
				withField("attachment.object",attachment.hashCode()).
				withField("attachment.snapshot.id",id(attachedSnapshot.resourceId())).
				withField("attachment.snapshot.object",attachedSnapshot.hashCode()).
				withField("attachment.resource",createdAttachment.hashCode()).
				withField("attachment.resource.version",createdAttachment.version()).
				withField("attachment.resource.delegate",attachedResource.hashCode());
		}

		@Override
		public void deleteAttachment(DelegatedContainerSnapshot snapshot, Attachment attachment) {
			Resource resource=snapshot.delegate();
			populateSnapshotInformation(newRecord("deleteAttachment"),snapshot).
				withField("snapshot.delegate",resource.hashCode()).
				withField("attachment.resource.id",attachment.id()).
				withField("attachment.resource.object",attachment.hashCode()).
				withField("attachment.resource.version",attachment.version()).
				withField("attachment.resource.delegate.id",id(attachment.resourceId()));
		}

		@Override
		public void createMember(DelegatedContainerSnapshot snapshot, DelegatedResourceSnapshot member, Member createdMember) {
			Resource resource=snapshot.delegate();
			Resource memberResource=member.delegate();
			populateSnapshotInformation(newRecord("createMemberResource"),snapshot).
				withField("snapshot.delegate",resource.hashCode()).
				withField("member.id",id(member.resourceId())).
				withField("member.object",member.hashCode()).
				withField("member.resource",createdMember.hashCode()).
				withField("member.resource.number",createdMember.number()).
				withField("member.resource.delegate",memberResource.hashCode());
		}

		@Override
		public void deleteMember(DelegatedContainerSnapshot snapshot, Member member) {
			populateSnapshotInformation(newRecord("deleteMemberResource"),snapshot).
				withField("snapshot.delegate",snapshot.delegate().hashCode()).
				withField("member.resource",member.hashCode()).
				withField("member.resource.number",member.number()).
				withField("member.resource.delegate.id",id(member.memberId()));
		}

		@Override
		public void createResource(Resource resource, Endpoint endpoint) {
			populateResourceRecord("createResource", resource, endpoint);
		}

		@Override
		public void modifyResource(Resource resource, Endpoint endpoint) {
			populateResourceRecord("modifyResource", resource, endpoint);
		}

		@Override
		public void deleteResource(Resource resource, Endpoint endpoint) {
			populateResourceRecord("deleteResource", resource, endpoint);
		}

	}

	private final CopyOnWriteArrayList<Record> records;
	private final AtomicLong recordCount;
	private final SessionId sessionId;
	private final Journaler journaler;

	private DefaultJournal(SessionId sessionId) {
		this.sessionId = sessionId;
		this.records=Lists.newCopyOnWriteArrayList();
		this.recordCount=new AtomicLong();
		this.journaler=new DefaultJournaler();
	}

	private DefaultRecord newRecord(String string) {
		DefaultRecord record = DefaultRecord.newInstance(this.recordCount.incrementAndGet());
		this.records.add(record);
		return record.withField("action", string);
	}

	Journaler journaler() {
		return this.journaler;
	}

	@Override
	public SessionId sessionId() {
		return this.sessionId;
	}

	@Override
	public int size() {
		return this.records.size();
	}

	@Override
	public Iterator<Record> iterator() {
		return this.records.iterator();
	}

	static DefaultJournal newInstance(SessionId sessionId) {
		return new DefaultJournal(sessionId);
	}

}