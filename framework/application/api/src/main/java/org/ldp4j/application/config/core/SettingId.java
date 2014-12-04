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
package org.ldp4j.application.config.core;

import java.lang.reflect.Type;

import org.ldp4j.application.config.Setting;
import org.ldp4j.application.util.Types;

import com.google.common.base.Objects;

public final class SettingId {

	private final String typeName;
	private final String key;

	private SettingId(String typeName, String key) {
		this.typeName = typeName;
		this.key = key;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.typeName,this.key);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof SettingId) {
			SettingId that=(SettingId)obj;
			result=
				Objects.equal(this.typeName,that.typeName) &&
				Objects.equal(this.key, that.key);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					add("typeName", this.typeName).
					add("key",this.key).
					toString();
	}

	static SettingId create(Type type, Setting<?> setting) {
		return new SettingId(Types.toString(type),setting.getKey());
	}

}