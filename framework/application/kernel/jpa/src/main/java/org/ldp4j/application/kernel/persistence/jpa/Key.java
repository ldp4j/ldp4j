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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.2.2
 *   Bundle      : ldp4j-application-kernel-jpa-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.jpa;

import java.io.Serializable;
import java.util.Objects;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.kernel.persistence.encoding.Encoder;
import org.ldp4j.application.kernel.resource.ResourceId;

import com.google.common.base.MoreObjects;

final class Key implements Serializable {

	private static final long serialVersionUID = 8863328550780382031L;

	static final Key NULL=new Key();

	private boolean cacheAvailable;
	private ResourceId cachedId;

	private String nameValue;
	private String nameType;
	private String templateId;

	private Key() {
		this.cacheAvailable=false;
	}

	private Key(ResourceId id, String templateId, String nameClass, String nameValue) {
		this();
		this.cacheAvailable=id!=null;
		this.cachedId = id;
		this.templateId = templateId;
		this.nameType = nameClass;
		this.nameValue = nameValue;
	}

	private void assemble() {
		if(this.cacheAvailable) {
			return;
		}
		this.cacheAvailable=true;
		if(this.nameValue==null || this.templateId==null) {
			return;
		}
		this.cachedId=
			ResourceId.
				createId(
					Key.fromBase64(this.nameValue),
					this.templateId);
		// Self healing...
		this.nameType=nameTypeOf(this.cachedId.name());
	}

	synchronized ResourceId resourceId() {
		if(!this.cacheAvailable) {
			assemble();
		}
		return this.cachedId;
	}

	String templateId() {
		return this.templateId;
	}

	String nameType() {
		return this.nameType;
	}

	String nameValue() {
		return this.nameValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.templateId,this.nameType,this.nameValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Key) {
			Key that=(Key)obj;
			result=
				Objects.equals(this.templateId, that.templateId) &&
				Objects.equals(this.nameType, that.nameType) &&
				Objects.equals(this.nameValue,that.nameValue);
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
					add("templateId", this.templateId).
					add("nameType", this.nameType).
					add("nameValue", shorten()).
					toString();
	}

	private String shorten() {
		return Integer.toHexString(this.nameValue.hashCode());
	}

	static Key newInstance(String templateId, String nameType, String nameValue) {
		return new Key(null,templateId,nameType,nameValue);
	}

	static Key newInstance(ResourceId id) {
		if(id==null) {
			return null;
		}
		Name<?> name = id.name();
		String nameType=nameTypeOf(name);
		String nameValue=Key.toBase64(name);
		return new Key(id,id.templateId(),nameType,nameValue);
	}

	private static String nameTypeOf(Name<?> name) {
		return name.id().getClass().getCanonicalName();
	}

	static String toBase64(Name<?> name) {
		return Encoder.valueEncoder().encode(name);
	}

	static <T extends Serializable> Name<T> fromBase64(String data) {
		return Encoder.valueEncoder().decode(data);
	}

}