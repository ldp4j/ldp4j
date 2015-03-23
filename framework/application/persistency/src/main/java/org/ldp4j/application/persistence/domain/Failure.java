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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.common.base.Objects;

@Entity
@NamedQueries({
	@NamedQuery(
		name=Failure.DELETE_APPLICATION_FAILURES_BY_DATE,
		query="DELETE FROM Failure f WHERE f.occurredOn <= :"+Failure.OCCURRED_ON+" AND f.endpoint IN (SELECT e FROM Endpoint e WHERE e.application = :"+Failure.APPLICATION+")"
	),
	@NamedQuery(
		name=Failure.DELETE_ALL_APPLICATION_FAILURES,
		query="DELETE FROM Failure f WHERE f.endpoint IN (SELECT e FROM Endpoint e WHERE e.application = :"+Failure.APPLICATION+")"
	)
})
public class Failure {

	public static final String DELETE_APPLICATION_FAILURES_BY_DATE="deleteApplicationFailuresByDate";
	public static final String DELETE_ALL_APPLICATION_FAILURES="deleteAllApplicationFailures";
	public static final String OCCURRED_ON="ocurredOn";
	public static final String APPLICATION="application";

	private long id;
	private Endpoint endpoint;
	private String request;
	private String message;

	private Date occurredOn;

	@Id
	@GeneratedValue
	@Column(name="failure_id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name="endpoint_id",nullable=false,updatable=false)
	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(Endpoint resource) {
		this.endpoint = resource;
	}

	@Column(nullable=false,updatable=false)
	public String getRequest() {
		return request;
	}

	public void setRequest(String operation) {
		this.request = operation;
	}

	@Column(nullable=false,updatable=false)
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Column(nullable=false,updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getOccurredOn() {
		return occurredOn;
	}

	public void setOccurredOn(Date occurredOn) {
		this.occurredOn = occurredOn;
	}

	@Override
	public final String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
					add("id",this.id).
					add("endpoint",DomainHelper.identifyEntity(this.endpoint)).
					add("operation",this.request).
					add("message",this.message).
					add("ocurredOn",this.message).
					toString();
	}
}
