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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Lists;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(
	uniqueConstraints=
		@UniqueConstraint(
			name="uniqueHandlerPerApplication",
			columnNames={"app_path","handlerClassName"}
		)
)
@NamedQueries({
	@NamedQuery(
		name="findApplicationTemplateByHandler",
		query="SELECT t FROM Template t WHERE t.handlerClassName = :"+Template.HANDLER_CLASS_NAME+" AND t.application = :"+Template.APPLICATION
	)
})
public abstract class Template implements Serializable {

	public static final String FIND_APPLICATION_TEMPLATE_BY_HANDLER="findApplicationTemplateByHandler";

	public static final String HANDLER_CLASS_NAME="handlerClassName";
	public static final String APPLICATION       ="application";

	private long id;
	private String name;
	private String description;
	private String handlerClassName;

	private Application application;
	private List<TemplateAttachment> templateAttachments;
	private List<Resource> resources;

	private static final long serialVersionUID = 1L;

	public Template() {
		super();
		this.templateAttachments=Lists.newArrayList();
		this.resources=Lists.newArrayList();
	}

	@Id
	@GeneratedValue
	@Column(name="template_id")
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
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(nullable=false,updatable=false)
	public String getHandlerClassName() {
		return handlerClassName;
	}

	public void setHandlerClassName(String handlerClassName) {
		this.handlerClassName = handlerClassName;
	}

	@ManyToOne
	@JoinColumn(name="app_path",nullable=false,updatable=false)
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@OneToMany(mappedBy="ownerTemplate")
	public List<TemplateAttachment> getTemplateAttachments() {
		return templateAttachments;
	}

	public void setTemplateAttachments(List<TemplateAttachment> templateAttachments) {
		this.templateAttachments = templateAttachments;
	}

	@OneToMany(mappedBy="definedBy")
	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	public abstract void accept(TemplateVisitor visitor);

	@Override
	public final String toString() {
		ToStringHelper helper = MoreObjects.toStringHelper(getClass()).omitNullValues();
		toString(helper);
		return helper.toString();
	}

	protected void toString(ToStringHelper helper) {
		helper.
			add("id",this.id).
			add("name",this.name).
			add("description",this.description).
			add("handlerClassName",this.handlerClassName).
			add("application",DomainHelper.identifyEntity(this.application)).
			add("templateAttachment",DomainHelper.identifyEntities(this.templateAttachments)).
			add("resources",DomainHelper.identifyEntities(this.resources));
	}

}