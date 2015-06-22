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
import java.net.URI;

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
 * between a template and its attached templates.
 */
@Entity
@Table(
	uniqueConstraints={
		@UniqueConstraint(name="uniquePathsPerOwnerTemplate",columnNames={"path","owner_template_id"}),
		@UniqueConstraint(name="uniqueNamesPerOwnerTemplate",columnNames={"name","owner_template_id"}),
	}
)
public class TemplateAttachment implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String path;
	private String name;
	private String description;
	private URI predicate;
	private Template ownerTemplate;
	private Template attachedTemplate;

	public TemplateAttachment() {
		super();
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


	@Column(nullable=false,updatable=false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(nullable=true,updatable=false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(nullable=true,updatable=false)
	public URI getPredicate() {
		return predicate;
	}

	public void setPredicate(URI predicate) {
		this.predicate = predicate;
	}

	@ManyToOne
	@JoinColumn(name="owner_template_id",nullable=false,updatable=false)
	public Template getOwnerTemplate() {
		return ownerTemplate;
	}

	public void setOwnerTemplate(Template owner) {
		this.ownerTemplate = owner;
	}

	@OneToOne
	@JoinColumn(name="attached_template_id",nullable=false,updatable=false)
	public Template getAttachedTemplate() {
		return attachedTemplate;
	}

	public void setAttachedTemplate(Template attached) {
		this.attachedTemplate = attached;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public final String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("id",this.id).
					add("name",this.name).
					add("description",this.description).
					add("predicate",this.predicate).
					add("path",this.path).
					add("ownerTemplate",DomainHelper.identifyEntity(this.ownerTemplate)).
					add("attachedTemplate",DomainHelper.identifyEntity(this.attachedTemplate)).
					toString();
	}

}