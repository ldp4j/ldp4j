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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;
import java.util.Objects;

import org.joda.time.DateTime;

import com.google.common.base.MoreObjects;

final class ImmutableDateTimeLiteral implements DateTimeLiteral {

	/**
	 *
	 */
	private static final long serialVersionUID = 2311445959339832121L;

	private final DateTime dateTime;
	private final URI dataType;

	ImmutableDateTimeLiteral(DateTime dateTime, URI dataType) {
		this.dateTime = dateTime;
		this.dataType = dataType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(ValueVisitor visitor) {
		visitor.visitLiteral(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DateTime get() {
		return this.dateTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(LiteralVisitor visitor) {
		visitor.visitTypedLiteral(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI type() {
		return this.dataType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.dateTime,this.dataType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof TypedLiteral) {
			TypedLiteral<?> that=(TypedLiteral<?>)obj;
			result=
				Objects.equals(this.dateTime, that.get()) &&
				Objects.equals(this.dataType, that.type()) &&
				!(obj instanceof LanguageLiteral);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("dateTime",this.dateTime).
					add("dataType", this.dataType).
					toString();
	}

}