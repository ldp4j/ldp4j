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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-mem:0.2.2
 *   Bundle      : ldp4j-application-kernel-mem-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.kernel.constraints.ConstraintReport;
import org.ldp4j.application.kernel.constraints.ConstraintReportId;
import org.ldp4j.application.kernel.resource.Attachment;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceVisitor;
import org.ldp4j.application.kernel.resource.Resources;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateIntrospector;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

class InMemoryResource extends AbstractInMemoryResource implements Resource {

	private static final class VersionGenerator {

		private final ConcurrentMap<String,AtomicLong> attachmentCounter=new ConcurrentHashMap<String, AtomicLong>();

		long nextVersion(String id) {
			AtomicLong counter=this.attachmentCounter.putIfAbsent(id, new AtomicLong(-1));
			if(counter==null) {
				counter=this.attachmentCounter.get(id);
			}
			return counter.incrementAndGet();
		}

	}

	private static final class InMemoryAttachment implements Attachment {

		private final AttachmentId attachmentId;
		private final long version;

		private InMemoryAttachment(AttachmentId attachmentId, long version) {
			this.attachmentId = attachmentId;
			this.version = version;
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

		@Override
		public long version() {
			return this.version;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						omitNullValues().
						add("id", id()).
						add("resourceId", resourceId()).
						add("version", this.version).
						toString();
		}

	}

	private final Map<AttachmentId, InMemoryAttachment> attachments;
	private final Map<String, AttachmentId> attachmentsById;
	private final Map<ResourceId, AttachmentId> attachmentsByResourceId;
	private final VersionGenerator versionGenerator;

	private final Map<String,ConstraintReportId> reports;

	private final ResourceId id;
	private final ResourceId parentId;

	private URI indirectId;

	protected InMemoryResource(ResourceId id, ResourceId parentId) {
		this.id=id;
		this.parentId = parentId;
		this.attachments=new LinkedHashMap<AttachmentId, InMemoryAttachment>();
		this.attachmentsById=new LinkedHashMap<String,AttachmentId>();
		this.attachmentsByResourceId=new LinkedHashMap<ResourceId,AttachmentId>();
		this.versionGenerator = new VersionGenerator();
		this.reports=Maps.newLinkedHashMap();
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
		newResource.setTemplateLibrary(getTemplateLibrary());
		return newResource;
	}

	private synchronized ConstraintReportId nextConstraintReportId() {
		String failureId=null;
		do {
			failureId=UUID.randomUUID().toString();
		} while(this.reports.containsKey(failureId));
		ConstraintReportId reportId=ConstraintReportId.create(this.id, failureId);
		this.reports.put(failureId, reportId);
		return reportId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceId id() {
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIndirectId(URI indirectId) {
		this.indirectId=indirectId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI indirectId() {
		return this.indirectId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRoot() {
		return this.parentId==null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceId parentId() {
		return this.parentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attachment findAttachment(ResourceId resourceId) {
		checkNotNull(resourceId,"Attached resource identifier cannot be null");
		return this.attachments.get(this.attachmentsByResourceId.get(resourceId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Resource attach(String attachmentId, ResourceId resourceId) {
		return attach(attachmentId,resourceId,Resource.class);
	}

	/**
	 * {@inheritDoc}
	 */
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
		checkState(Resources.areCompatible(clazz,attachmentTemplate),"Attachment '%s' is not of type '%s' (%s)",attachmentId,clazz.getCanonicalName(),attachmentTemplate.getClass().getCanonicalName());
		InMemoryResource newResource=createChild(resourceId,attachmentTemplate);
		InMemoryAttachment newAttachment = new InMemoryAttachment(aId,this.versionGenerator.nextVersion(attachmentId));
		attachments.put(newAttachment.attachmentId(),newAttachment);
		attachmentsById.put(aId.id(),aId);
		attachmentsByResourceId.put(aId.resourceId(),aId);
		return clazz.cast(newResource);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<InMemoryAttachment> attachments() {
		return Collections.unmodifiableSet(new LinkedHashSet<InMemoryAttachment>(this.attachments.values()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(ResourceVisitor visitor) {
		visitor.visitResource(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConstraintReport addConstraintReport(Constraints constraints, Date date, HttpRequest request) {
		ConstraintReportId reportId = nextConstraintReportId();
		return new InMemoryConstraintReport(reportId,date, request, constraints);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<ConstraintReportId> constraintReports() {
		Set<ConstraintReportId> currentReports=null;
		synchronized(this) {
			currentReports=ImmutableSet.copyOf(this.reports.values());
		}
		return currentReports;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeFailure(ConstraintReport report) {
		if(report!=null) {
			String failureId = report.id().failureId();
			synchronized(this) {
				this.reports.remove(failureId);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return stringHelper().toString();
	}

	@Override
	protected ToStringHelper stringHelper() {
		return
			super.stringHelper().
				add("id",this.id).
				add("parentId",this.parentId).
				add("attachments",this.attachments);
	}

}