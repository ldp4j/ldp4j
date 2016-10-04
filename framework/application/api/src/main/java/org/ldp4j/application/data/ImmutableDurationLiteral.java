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

import org.joda.time.Duration;

import com.google.common.base.MoreObjects;

final class ImmutableDurationLiteral implements DurationLiteral {

	private static final long serialVersionUID = -7312919003663624256L;

	private final Duration duration;
	private final URI dataType;

	ImmutableDurationLiteral(Duration duration, URI dataType) {
		this.duration = duration;
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
	public Duration get() {
		return this.duration;
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
		return Objects.hash(this.duration,this.dataType);
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
				Objects.equals(this.duration, that.get()) &&
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
					add("duration",this.duration).
					add("dataType", this.dataType).
					toString();
	}

}