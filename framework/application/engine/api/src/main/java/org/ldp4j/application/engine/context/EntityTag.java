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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-api:0.2.2
 *   Bundle      : ldp4j-application-engine-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.context;

import java.io.Serializable;
import java.util.Objects;

public final class EntityTag implements Serializable {

	private static final long serialVersionUID = 1537356414568176655L;

	private final boolean weak;
	private final String value;

	EntityTag(String value, boolean weak) {
		this.value=Objects.requireNonNull(value);
		this.weak=weak;
	}

	public String getValue() {
		return this.value;
	}

	public boolean isWeak() {
		return this.weak;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.value,this.weak);
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		}

		if(!(obj instanceof EntityTag)) {
			return false;
		}

		EntityTag that=(EntityTag) obj;
		return
			this.weak==that.weak &&
			this.value.equals(that.value);
	}

	@Override
	public String toString() {
		return EntityTagHelper.toString(this);
	}

	public static EntityTag valueOf(String value) {
		return EntityTagHelper.fromString(value);
	}

	public static EntityTag createWeak(String value) {
		return create(value, true);
	}

	public static EntityTag createStrong(String value) {
		return create(value,false);
	}

	private static EntityTag create(String value, boolean weak) {
		return new EntityTag(EntityTagHelper.normalizeValue(value),weak);
	}

}