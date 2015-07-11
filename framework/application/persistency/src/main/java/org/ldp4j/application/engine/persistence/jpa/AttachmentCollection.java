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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-persistency:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-persistency-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.persistence.jpa;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Map;

import org.ldp4j.application.engine.resource.Attachment;
import org.ldp4j.application.engine.resource.ResourceId;

import com.google.common.collect.Maps;

final class AttachmentCollection {

	private final Map<AttachmentId,JPAAttachment> indexById;

	private final Map<String,AttachmentId> indexByAttachmentId;

	private final Map<ResourceId,AttachmentId> indexByResourceId;

	private List<JPAAttachment> collection;


	AttachmentCollection() {
		this.indexById=Maps.newLinkedHashMap();
		this.indexByAttachmentId=Maps.newLinkedHashMap();
		this.indexByResourceId=Maps.newLinkedHashMap();
	}

	private void indexAttachment(JPAAttachment attachment) {
		AttachmentId aId = attachment.attachmentId();
		this.indexById.put(aId,attachment);
		this.indexByAttachmentId.put(aId.id(),aId);
		this.indexByResourceId.put(aId.resourceId(),aId);
	}

	void addAttachment(JPAAttachment attachment) {
		this.collection.add(attachment);
		indexAttachment(attachment);
	}

	boolean removeAttachment(Attachment attachment) {
		boolean removed = this.collection.remove(attachment);
		if(removed) {
			AttachmentId remove = this.indexByAttachmentId.remove(attachment.id());
			this.indexByResourceId.remove(attachment.resourceId());
			this.indexById.remove(remove);
		}
		return removed;
	}

	JPAAttachment attachmendByResourceId(ResourceId resourceId) {
		return this.indexById.get(this.indexByResourceId.get(resourceId));
	}

	void checkNotAttached(AttachmentId aId) {
		checkState(!this.indexById.containsKey(aId),"Resource '%s' is already attached as '%s'",aId.resourceId(),aId.id());
		checkState(!this.indexByAttachmentId.containsKey(aId.id()),"A resource is already attached as '%s'",aId.id());
		checkState(!this.indexByResourceId.containsKey(aId.resourceId()),"Resource '%s' is already attached",aId.resourceId());
	}

	void init(List<JPAAttachment> resourceAttachments) {
		this.collection=resourceAttachments;
		for(JPAAttachment attachment:this.collection) {
			indexAttachment(attachment);
		}
	}

}