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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.base.Objects;

/**
 * This entity reflects an association class, that is, state of the relationship
 * between the member resource and the container that owns the resource.
 */
@Entity
@Table(
	uniqueConstraints={
		@UniqueConstraint(name="uniqueMembers",columnNames={"member_id"}),
		@UniqueConstraint(name="uniquePositions",columnNames={"container_id","position"}),
	}
)
public class ResourceMembership implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private Container container;
	private Resource member;
	private long order;

	public ResourceMembership() {
		super();
	}

	@Id
	@GeneratedValue
	@Column(name="membership_id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name="position",nullable=false,updatable=false)
	public long getOrder() {
		return this.order;
	}

	public void setOrder(long order) {
		this.order=order;
	}

	@ManyToOne
	@JoinColumn(name="container_id",nullable=false,updatable=false)
	public Container getContainer() {
		return this.container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	@OneToOne
	@JoinColumn(name="member_id",nullable=false,updatable=false)
	public Resource getMember() {
		return member;
	}

	public void setMember(Resource member) {
		this.member = member;
	}

	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
					add("id", this.id).
					add("order", this.order).
					add("container", DomainHelper.identifyEntity(this.container)).
					add("member", DomainHelper.identifyEntity(this.member)).
					toString();
	}

}