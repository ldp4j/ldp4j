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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.2.2
 *   Bundle      : ldp4j-application-kernel-jpa-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.jpa;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Map;

import org.ldp4j.application.kernel.resource.Attachment;
import org.ldp4j.application.kernel.resource.ResourceId;

import com.google.common.collect.Maps;

final class AttachmentCollection {

	private final Map<String,JPAAttachment> indexByAttachmentId;

	private final Map<ResourceId,JPAAttachment> indexByResourceId;

	private List<JPAAttachment> collection;


	AttachmentCollection() {
		this.indexByAttachmentId=Maps.newLinkedHashMap();
		this.indexByResourceId=Maps.newLinkedHashMap();
	}

	private void indexAttachment(JPAAttachment attachment) {
		this.indexByAttachmentId.put(attachment.id(),attachment);
		ResourceId resourceId = attachment.resourceId();
		if(resourceId!=null) {
			this.indexByResourceId.put(resourceId,attachment);
		}
	}

	void addAttachment(JPAAttachment attachment) {
		this.collection.add(attachment);
		indexAttachment(attachment);
	}

	boolean removeAttachment(Attachment attachment) {
		JPAAttachment jpaAttachment = this.indexByAttachmentId.get(attachment.id());
		if(jpaAttachment!=null) {
			jpaAttachment.unbind();
		}
		return jpaAttachment!=null;
	}

	JPAAttachment attachmendByResourceId(ResourceId resourceId) {
		return this.indexByResourceId.get(resourceId);
	}

	JPAAttachment attachmentById(String attachmentId) {
		return this.indexByAttachmentId.get(attachmentId);
	}

	void checkNotAttached(String attachmentId, ResourceId resourceId) {
		checkState(!this.indexByResourceId.containsKey(resourceId),"Resource '%s' is already attached",resourceId);
		JPAAttachment attachment = this.indexByAttachmentId.get(attachmentId);
		checkState(attachment!=null,"Unknown attachment '%s'",attachmentId);
		checkState(attachment.resourceId()==null,"A resource is already attached as '%s'",attachmentId);
	}

	void init(List<JPAAttachment> resourceAttachments) {
		this.collection=resourceAttachments;
		for(JPAAttachment attachment:this.collection) {
			indexAttachment(attachment);
		}
	}


}