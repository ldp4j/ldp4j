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
package org.ldp4j.application.persistence.domain;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.base.MoreObjects;

/**
 * This entity reflects an association class, that is, state of the relationship
 * between the attachment template, the owner resource, and the attached
 * resource.
 */
@Entity
@Table(
	uniqueConstraints={
		@UniqueConstraint(name="uniqueTemplateAttachmentPerAttacher",columnNames={"owner_resource_id","attachment_id"}),
		@UniqueConstraint(name="uniqueAttacherPerAttachee",columnNames={"attachment_id","owner_resource_id","attached_resource_id"}),
		@UniqueConstraint(name="uniqueAttachedResource",columnNames="attached_resource_id")
	}
)
public class ResourceAttachment implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private TemplateAttachment templateAttachment;
	private Resource ownerResource;
	private Resource attachedResource;

	private AtomicLong version;

	public ResourceAttachment() {
		super();
		this.version=new AtomicLong();
	}

	@Id
	@GeneratedValue
	@Column(name="attachment_id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getVersion() {
		return this.version.get();
	}

	public void setVersion(long version) {
		this.version.set(version);
	}

	@ManyToOne
	@JoinColumn(name="template_attachment_id",nullable=false,updatable=false)
	public TemplateAttachment getTemplateAttachment() {
		return templateAttachment;
	}

	public void setTemplateAttachment(TemplateAttachment templateAttachment) {
		this.templateAttachment = templateAttachment;
	}

	@ManyToOne
	@JoinColumn(name="owner_resource_id",nullable=false,updatable=false)
	public Resource getOwnerResource() {
		return ownerResource;
	}

	public void setOwnerResource(Resource ownerResource) {
		this.ownerResource = ownerResource;
	}

	@OneToOne(optional=true)
	@JoinColumn(name="attached_resource_id")
	public Resource getAttachedResource() {
		return attachedResource;
	}

	public void setAttachedResource(Resource attachedResource) {
		this.attachedResource = attachedResource;
	}

	public String nextAttachmentPath() {
		long newVersion=this.version.getAndIncrement();
		String suffix="";
		if(newVersion>0) {
			suffix=String.format("_%d/",newVersion);
		}
		String ownerPath = this.ownerResource.getEndpoint().getPath();
		String attachmentPath = this.templateAttachment.getPath();
		return
			new StringBuilder().
				append(ownerPath).
				append(ownerPath.endsWith("/")?"":"/").
				append(attachmentPath).
				append(attachmentPath.endsWith("/")?"":"/").
				append(suffix).
				toString();
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("id", this.id).
					add("version", this.version).
					add("templateAttachment", DomainHelper.identifyEntity(this.templateAttachment)).
					add("ownerResource", DomainHelper.identifyEntity(this.ownerResource)).
					add("attachedResource", DomainHelper.identifyEntity(this.attachedResource)).
					toString();
	}

}