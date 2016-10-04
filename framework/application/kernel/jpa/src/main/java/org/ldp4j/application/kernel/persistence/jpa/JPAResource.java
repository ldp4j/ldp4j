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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.kernel.constraints.ConstraintReport;
import org.ldp4j.application.kernel.constraints.ConstraintReportId;
import org.ldp4j.application.kernel.resource.Attachment;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.ResourceVisitor;
import org.ldp4j.application.kernel.resource.Resources;
import org.ldp4j.application.kernel.template.AttachedTemplate;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateIntrospector;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

class JPAResource extends AbstractJPAResource implements Resource {

	/**
	 * Persistent key required by JPA
	 */
	private long primaryKey;

	/**
	 * Not final to enable its usage in JPA
	 */
	private List<JPAAttachment> attachments;

	/**
	 * Not final to enable its usage in JPA
	 */
	private Set<String> failures;

	/**
	 * Not final to enable its usage in JPA
	 */
	private ResourceId id;

	/**
	 * Not final to enable its usage in JPA
	 */
	private ResourceId parentId;

	private URI indirectId;

	private final AttachmentCollection attachmentCollection;

	protected JPAResource() {
		this.attachmentCollection=new AttachmentCollection();
		this.failures=Sets.newLinkedHashSet();
		this.attachments=Lists.newArrayList();
	}

	protected JPAResource(ResourceId id, ResourceId parentId) {
		this();
		this.id=id;
		this.parentId = parentId;
	}

	protected JPAResource(ResourceId id) {
		this(id,null);
	}

	@Override
	protected void init() {
		ResourceTemplate template = super.getTemplate(this.id);
		Set<AttachedTemplate> attachedTemplates = template.attachedTemplates();
		if(!attachedTemplates.isEmpty() && this.attachments.isEmpty()) {
			for(AttachedTemplate attachedTemplate:attachedTemplates) {
				JPAAttachment newAttachment = new JPAAttachment(attachedTemplate.id());
				this.attachments.add(newAttachment);
			}
		}
		this.attachmentCollection.init(this.attachments);
	}

	protected final JPAResource createChild(ResourceId resourceId, ResourceTemplate template) {
		JPAResource newResource=null;
		if(!TemplateIntrospector.newInstance(template).isContainer()) {
			newResource=new JPAResource(resourceId,this.id);
		} else {
			newResource=new JPAContainer(resourceId,this.id);
		}
		newResource.setTemplateLibrary(getTemplateLibrary());
		return newResource;
	}

	private synchronized ConstraintReportId nextConstraintReportId() {
		String failureId=null;
		do {
			failureId=UUID.randomUUID().toString();
		} while(this.failures.contains(failureId));
		ConstraintReportId reportId=ConstraintReportId.create(this.id, failureId);
		this.failures.add(failureId);
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
		return this.attachmentCollection.attachmendByResourceId(resourceId);
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
		this.attachmentCollection.checkNotAttached(attachmentId,resourceId);
		ResourceTemplate attachmentTemplate=super.getTemplate(resourceId);
		checkState(Resources.areCompatible(clazz,attachmentTemplate),"Attachment '%s' is not of type '%s' (%s)",attachmentId,clazz.getCanonicalName(),attachmentTemplate.getClass().getCanonicalName());
		JPAResource newResource=createChild(resourceId,attachmentTemplate);
		JPAAttachment newAttachment=this.attachmentCollection.attachmentById(attachmentId);
		newAttachment.bind(resourceId);
		return clazz.cast(newResource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean detach(Attachment attachment) {
		return this.attachmentCollection.removeAttachment(attachment);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<JPAAttachment> attachments() {
		return ImmutableSet.copyOf(this.attachments);
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
		return new JPAConstraintReport(reportId,date, request, constraints);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<ConstraintReportId> constraintReports() {
		Set<String> currentFailures=null;
		synchronized(this) {
			currentFailures=ImmutableSet.copyOf(this.failures);
		}
		Builder<ConstraintReportId> builder=ImmutableSet.builder();
		for(String failure:currentFailures) {
			builder.add(ConstraintReportId.create(this.id,failure));
		}
		return builder.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeFailure(ConstraintReport report) {
		if(report!=null) {
			String failureId = report.id().failureId();
			synchronized(this) {
				this.failures.remove(failureId);
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
				add("primaryKey",this.primaryKey).
				add("id",this.id).
				add("parentId",this.parentId).
				add("attachments",this.attachments).
				add("failures", this.failures);
	}

}