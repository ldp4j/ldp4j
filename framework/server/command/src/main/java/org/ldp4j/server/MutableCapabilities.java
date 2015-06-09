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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class MutableCapabilities implements Capabilities {

	protected boolean deletable  = false;
	protected boolean modifiable = false;
	protected boolean patchable  = false;

	public MutableCapabilities() {

	}

	public MutableCapabilities(boolean deletable, boolean modifiable, boolean patchable) {
		this.deletable = deletable;
		this.modifiable = modifiable;
		this.patchable = patchable;
	}

	@Override
	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean value) {
		this.deletable = value;
	}

	@Override
	public boolean isModifiable() {
		return modifiable;
	}

	public void setModifiable(boolean value) {
		this.modifiable = value;
	}

	@Override
	public boolean isPatchable() {
		return patchable;
	}

	public void setPatchable(boolean value) {
		this.patchable = value;
	}

	public MutableCapabilities withDeletable(boolean value) {
		setDeletable(value);
		return this;
	}

	public MutableCapabilities withModifiable(boolean value) {
		setModifiable(value);
		return this;
	}

	public MutableCapabilities withPatchable(boolean value) {
		setPatchable(value);
		return this;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(MutableCapabilities.class).
					add("deletable", deletable).
					add("modifiable", modifiable).
					add("patchable", patchable).
					toString();
	}

	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof MutableCapabilities) {
			Capabilities typedObject = (Capabilities) obj;
			result =
				Objects.equal(this.isDeletable(), typedObject.isDeletable()) &&
				Objects.equal(this.isModifiable(), typedObject.isModifiable()) &&
				Objects.equal(this.isPatchable(), typedObject.isPatchable());
		}
		return result;

	}

	public int hashCode() {
		return 19 * Objects.hashCode(isDeletable(), isModifiable(),isPatchable());
	}

}
