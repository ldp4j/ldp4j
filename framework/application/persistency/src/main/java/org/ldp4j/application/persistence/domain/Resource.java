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
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Lists;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(
	uniqueConstraints=
		@UniqueConstraint(name="uniqueBusinessKeyPerTemplate",columnNames={"template_id","businessKey"})
)
@NamedQueries({
	@NamedQuery(
		name=Resource.FIND_TEMPLATE_RESOURCE_BY_BUSINESS_KEY,
		query="SELECT r FROM Resource r WHERE r.definedBy = :"+Resource.TEMPLATE+" AND r.businessKey = :"+Resource.BUSINESS_KEY
	),
	@NamedQuery(
		name=Resource.FIND_APPLICATION_RESOURCE_BY_PATH,
		query="SELECT r FROM Resource r WHERE r.endpoint.application = :"+Resource.APPLICATION+" AND r.endpoint.path = :"+Resource.PATH
	)
})
public abstract class Resource implements Serializable {

	public static final String FIND_TEMPLATE_RESOURCE_BY_BUSINESS_KEY="findTemplateResourceByBusinessKey";
	public static final String FIND_APPLICATION_RESOURCE_BY_PATH        ="findTemplateionResourceByPath";
	public static final String APPLICATION                           ="application";
	public static final String TEMPLATE                              ="template";
	public static final String BUSINESS_KEY                          ="businessKey";
	public static final String PATH                                  ="path";

	private long id;
	private Template definedBy;
	private Serializable businessKey;
	private URI indirectId;
	private Endpoint endpoint;

	private final Lock writeLock;
	private final Lock readLock;

	private ResourceAttachment attachment;
	private ResourceMembership membership;

	private List<ResourceAttachment> resourceAttachments;
	private List<Failure> failures;

	private static final long serialVersionUID = 1L;

	public Resource() {
		super();
		this.resourceAttachments=Lists.newArrayList();
		this.failures=Lists.newArrayList();
		ReadWriteLock lock=new ReentrantReadWriteLock();
		writeLock = lock.writeLock();
		readLock = lock.readLock();
	}

	@Id
	@GeneratedValue
	@Column(name="resource_id")
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(optional=false)
	@JoinColumn(name="template_id")
	public Template getDefinedBy() {
		return definedBy;
	}

	public void setDefinedBy(Template definedBy) {
		this.definedBy = definedBy;
	}

	@Column(nullable=false,updatable=false)
	public Serializable getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(Serializable businessKey) {
		this.businessKey = businessKey;
	}

	@OneToOne
	@JoinColumn(name="endpoint_id",nullable=false,updatable=false)
	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	// This only makes sense in resources that are members of indirect containers
	@Column(nullable=true,unique=true,name="indirect_id")
	public URI getIndirectId() {
		return this.indirectId;
	}

	public void setIndirectId(URI indirectId) {
		this.indirectId = indirectId;
	}

	@OneToOne(optional=true,mappedBy="attachedResource")
	public ResourceAttachment getAttachment() {
		this.readLock.lock();
		try {
			return this.attachment;
		} finally {
			this.readLock.unlock();
		}
	}

	public void setAttachment(ResourceAttachment attachment) {
		this.writeLock.lock();
		try {
			if(attachment!=null && this.membership!=null) {
				throw new IllegalStateException("A member resource cannot be attached to another resource");
			}
			this.attachment=attachment;
		} finally {
			this.writeLock.unlock();
		}
	}

	@OneToOne(optional=true,mappedBy="member")
	public ResourceMembership getMembership() {
		this.readLock.lock();
		try {
			return this.membership;
		} finally {
			this.readLock.unlock();
		}
	}

	public void setMembership(ResourceMembership membership) {
		this.writeLock.lock();
		try {
			if(membership!=null && this.attachment!=null) {
				throw new IllegalStateException("An attached resource cannot be a member of a container");
			}
			this.membership=membership;
		} finally {
			this.writeLock.unlock();
		}
	}

	@OneToMany(mappedBy="ownerResource")
	public List<ResourceAttachment> getResourceAttachments() {
		return this.resourceAttachments;
	}

	public void setResourceAttachments(List<ResourceAttachment> attachments) {
		this.resourceAttachments = attachments;
	}

	@Transient
	public boolean isMember() {
		this.readLock.lock();
		try {
			return this.membership!=null;
		} finally {
			this.readLock.unlock();
		}
	}

	@Transient
	public boolean isAttached() {
		this.readLock.lock();
		try {
			return this.attachment!=null;
		} finally {
			this.readLock.unlock();
		}
	}

	@Transient
	public boolean isRoot() {
		this.readLock.lock();
		try {
			return this.attachment==null && this.membership==null;
		} finally {
			this.readLock.unlock();
		}
	}

	@Transient
	public Resource getParent() {
		this.readLock.lock();
		try {
			if(this.membership!=null) {
				return this.membership.getContainer();
			}
			if(this.attachment!=null) {
				return this.attachment.getOwnerResource();
			}
			return null;
		} finally {
			this.readLock.unlock();
		}
	}
	@OneToMany(mappedBy="resource")
	public List<Failure> getFailures() {
		return this.failures;
	}

	public void setFailures(List<Failure> failures) {
		this.failures = failures;
	}


	public abstract void accept(ResourceVisitor visitor);

	@Override
	public final String toString() {
		ToStringHelper helper = Objects.toStringHelper(getClass()).omitNullValues();
		toString(helper);
		return helper.toString();
	}

	protected void toString(ToStringHelper helper) {
		helper.
			add("id",this.id).
			add("definedBy",DomainHelper.identifyEntity(this.definedBy)).
			add("businessKey",this.businessKey).
			add("endpoint",DomainHelper.identifyEntity(this.endpoint)).
			add("indirectId",this.indirectId).
			add("attachment",DomainHelper.identifyEntity(this.attachment)).
			add("membership",DomainHelper.identifyEntity(this.membership)).
			add("resourceAttachments",DomainHelper.identifyEntities(this.resourceAttachments)).
			add("failures",DomainHelper.identifyEntities(this.failures));
	}

}
