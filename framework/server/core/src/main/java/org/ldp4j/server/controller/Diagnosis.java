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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import java.io.Serializable;

import javax.ws.rs.core.Response.Status;

import com.google.common.base.Throwables;

final class Diagnosis implements Serializable {

	private static final long serialVersionUID = -5560000103337463241L;

	private final String diagnostic;
	private final int statusCode;
	private final boolean mandatory;

	private Diagnosis(int statusCode, String diagnostic, boolean mandatory) {
		this.statusCode = statusCode;
		this.diagnostic = diagnostic;
		this.mandatory = mandatory;
	}

	public String diagnostic() {
		return this.diagnostic;
	}
	public int statusCode() {
		return this.statusCode;
	}
	public boolean mandatory() {
		return this.mandatory;
	}

	Diagnosis statusCode(int statusCode) {
		return new Diagnosis(statusCode,this.diagnostic,this.mandatory);
	}

	Diagnosis statusCode(Status status) {
		return new Diagnosis(status.getStatusCode(),this.diagnostic,this.mandatory);
	}

	Diagnosis diagnostic(String format, Object... args) {
		return new Diagnosis(this.statusCode,String.format(format,args),this.mandatory);
	}

	Diagnosis diagnostic(Throwable rootCause) {
		return new Diagnosis(this.statusCode,Throwables.getStackTraceAsString(rootCause),this.mandatory);
	}

	Diagnosis mandatory(boolean mandatory) {
		return new Diagnosis(this.statusCode,this.diagnostic,mandatory);
	}

	static Diagnosis create(Throwable rootCause) {
		return new Diagnosis(
				Status.INTERNAL_SERVER_ERROR.getStatusCode(),
				Throwables.getStackTraceAsString(rootCause),
				true);
	}

	public static Diagnosis create() {
		return
			new Diagnosis(
				Status.INTERNAL_SERVER_ERROR.getStatusCode(),
				Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				true);
	}


}