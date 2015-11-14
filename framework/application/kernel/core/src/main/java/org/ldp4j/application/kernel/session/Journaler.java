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

import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.resource.Attachment;
import org.ldp4j.application.kernel.resource.Member;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.session.AttachmentSnapshotCollection.DelegatedAttachmentSnapshot;

interface Journaler {

	void saveChanges();

	void discardChanges();

	void close();

	void loadResource(ResourceId resourceId, Resource resource);

	void resolveSnapshot(ResourceId resourceId, DelegatedResourceSnapshot resource);

	void createTransientSnapshot(DelegatedResourceSnapshot snapshot, ResourceId id, DelegatedResourceSnapshot parent);

	void createPersistentSnapshot(DelegatedResourceSnapshot snapshot, Resource resource);

	void createMemberSnapshot(DelegatedContainerSnapshot snapshot, DelegatedResourceSnapshot member);

	void createAttachmentSnapshot(DelegatedResourceSnapshot snapshot, DelegatedAttachmentSnapshot attachment);

	void deleteMemberSnapshot(DelegatedContainerSnapshot snapshot, DelegatedResourceSnapshot member);

	void deleteAttachmentSnapshot(DelegatedResourceSnapshot snapshot, DelegatedAttachmentSnapshot attachment);

	void deleteSnapshot(DelegatedResourceSnapshot snapshot);

	void modifySnapshot(DelegatedResourceSnapshot snapshot);

	void createAttachment(DelegatedResourceSnapshot snapshot, DelegatedAttachmentSnapshot attachment, Attachment createdAttachment);

	void deleteAttachment(DelegatedContainerSnapshot snapshot, Attachment attachment);

	void createMember(DelegatedContainerSnapshot snapshot, DelegatedResourceSnapshot member, Member createdMember);

	void deleteMember(DelegatedContainerSnapshot snapshot, Member member);

	void createResource(Resource resource, Endpoint endpoint);

	void modifyResource(Resource resource, Endpoint endpoint);

	void deleteResource(Resource resource, Endpoint endpoint);

}