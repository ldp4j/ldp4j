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

import java.util.Objects;

import org.ldp4j.application.session.WriteSession;

public final class SessionId {

	private final int hash;
	private final String className;

	private SessionId(WriteSession session) {
		this.hash=session.hashCode();
		this.className=session.getClass().getCanonicalName();
	}

	public int hash() {
		return this.hash;
	}

	public String className() {
		return this.className;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.hash,this.className);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof SessionId) {
			SessionId that=(SessionId)obj;
			result=
				Objects.equals(this.hash,that.hash) &&
				Objects.equals(this.className, that.className);
		}
		return result;
	}

	@Override
	public String toString() {
		return this.className+"@"+Integer.toHexString(this.hash);
	}

	static SessionId create(WriteSession session) {
		return new SessionId(session);
	}

}