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
package org.ldp4j.server.utils.spring;

import java.util.Objects;

final class OrderingEntry implements Comparable<OrderingEntry> {

	private final String value;
	private final int position;

	public OrderingEntry(String value, int position) {
		this.value = value;
		this.position = position;
	}
	public String getValue() {
		return value;
	}
	public int getPosition() {
		return position;
	}
	@Override
	public int compareTo(OrderingEntry o) {
		int compareTo = value.compareTo(o.value);
		if(compareTo==0) {
			compareTo=position-o.position;
		}
		return compareTo;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OrderingEntry [value=");
		builder.append(value);
		builder.append(", position=");
		builder.append(position);
		builder.append("]");
		return builder.toString();
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.position,this.value);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=this==obj;
		if(!result && obj instanceof OrderingEntry) {
			OrderingEntry that=(OrderingEntry)obj;
			result=
				Objects.equals(this.position,that.position) &&
				Objects.equals(this.value,that.value);
		}
		return result;
	}

}