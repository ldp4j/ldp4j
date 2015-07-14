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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-engine-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.context;

import java.io.Serializable;

public final class EntityTag implements Serializable {

	private static final long serialVersionUID = 1537356414568176655L;

	private final boolean isWeak;
	private final String value;

	public EntityTag(String value) {
		this(value, false);
	}

	public EntityTag(String value, boolean weak) {
		if (value == null) {
			throw new IllegalArgumentException("Entity tag value cannot be null");
		}
		this.value = value;
		this.isWeak = weak;
	}

	@Override
	public boolean equals(java.lang.Object obj) {
		if(this == obj) {
			return true;
		}

		if (!(obj instanceof EntityTag)) {
			return false;
		}

		EntityTag other = (EntityTag) obj;
		if(isWeak!=other.isWeak()) {
			return false;
		}

		if(!value.equals(other.getValue())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = 19;
		result = 17 * result + ((isWeak) ? 1 : 0);
		result = 17 * result + value.hashCode();
		return result;
	}

	public String getValue() {
		return value;
	}

	public boolean isWeak() {
		return isWeak;
	}

	@Override
	public String toString() {
		return EntityTagHelper.toString(this);
	}

	public static EntityTag valueOf(String value) {
		return EntityTagHelper.fromString(value);
	}

}