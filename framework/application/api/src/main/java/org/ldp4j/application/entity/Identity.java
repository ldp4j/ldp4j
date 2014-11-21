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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.entity;

import java.net.URI;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public abstract class Identity implements Comparable<Identity> {

	protected static final class StringHelper {

		private final ToStringHelper helper;

		private StringHelper(ToStringHelper helper) {
			this.helper = helper;
		}

		protected StringHelper add(String name, Object value) {
			this.helper.add(name, value);
			return this;
		}

	}

	private final URI identifier;

	Identity(URI identifier) {
		this.identifier = identifier;
	}

	public final URI identifier() {
		return this.identifier;
	}

	public abstract void accept(IdentityVisitor visitor);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int compareTo(Identity that) {
		if(that==null) {
			return -1;
		}
		if(this==that || this.equals(that)) {
			return 0;
		}
		return this.identifier().toString().compareTo(this.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Identity) {
			Identity that = (Identity) obj;
			result=Objects.equal(this.identifier, that.identifier);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		ToStringHelper helper=
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
					add("identifier",this.identifier);
		toString(new StringHelper(helper));
		return helper.toString();
	}

	protected void toString(StringHelper helper) {
		// To be overriden by implementations
	}

}