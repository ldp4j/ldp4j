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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-persistency:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-persistency-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.persistence.jpa;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.engine.resource.ResourceId;

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
	}

	public synchronized ResourceId resourceId() {
		if(!this.cacheAvailable) {
			assemble();
		}
		return this.cachedId;
	}

	public String templateId() {
		return this.templateId;
	}

	public String nameType() {
		return this.nameType;
	}

	public String nameValue() {
		return this.nameValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.templateId,this.nameType,this.nameValue);
	}

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

	public static Key newInstance(String templateId, String nameType, String nameValue) {
		return new Key(null,templateId,nameType,nameValue);
	}

	public static Key newInstance(ResourceId id) {
		Name<?> name = id.name();
		String nameType=name.id().getClass().getCanonicalName();
		String nameValue=Key.toBase64(name);
		return new Key(id,id.templateId(),nameType,nameValue);
	}

	private static String toBase64(Name<?> name) {
		if(name==null) {
			return null;
		}
		try {
			byte[] serializedData=SerializationUtils.serialize(name);
			return Base64.encodeBase64String(serializedData);
		} catch (IOException e) {
			throw new AssertionError("Serialization should not fail",e);
		}
	}

	private static Name<?> fromBase64(String data) {
		if(data==null) {
			return null;
		}
		try {
			byte[] serializedData=Base64.decodeBase64(data);
			return SerializationUtils.deserialize(serializedData,Name.class);
		} catch (IOException e) {
			throw new AssertionError("Deserialization should not fail",e);
		}
	}

}
