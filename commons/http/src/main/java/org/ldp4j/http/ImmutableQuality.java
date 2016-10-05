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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static java.util.Objects.requireNonNull;
import static org.ldp4j.http.HttpUtils.checkQualityRange;

import java.util.Objects;

import com.google.common.collect.ComparisonChain;

final class ImmutableQuality implements Quality {

	private final double weight;
	private final Type type;

	private ImmutableQuality(double value, Type type) {
		this.weight = value;
		this.type = type;
	}

	@Override
	public Type type() {
		return this.type;
	}

	@Override
	public double weight() {
		return this.weight;
	}

	@Override
	public int compareTo(Quality that) {
		if(that==null) {
			return 1;
		}
		return
			ComparisonChain.
				start().
					compare(this.type,that.type()).
					compare(this.weight,that.weight()).
					result();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.type,this.weight);
	}

	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof Quality) {
			final Quality that=(Quality)obj;
			result=
				Objects.equals(this.type,that.type()) &&
				Objects.equals(this.weight,that.weight());
		}
		return result;
	}

	@Override
	public String toString() {
		return this.weight+" ["+this.type+"]";
	}

	static ImmutableQuality copyOf(Quality quality) {
		ImmutableQuality result=null;
		if(quality instanceof ImmutableQuality) {
			result=(ImmutableQuality)quality;
		} else {
			result=create(quality.type(),quality.weight());
		}
		return result;
	}

	static ImmutableQuality create(final Type type, final double weight) {
		requireNonNull(type,"Quality type cannot be null");
		checkQualityRange(weight, "Quality weight");
		return new ImmutableQuality(weight, type);
	}
}