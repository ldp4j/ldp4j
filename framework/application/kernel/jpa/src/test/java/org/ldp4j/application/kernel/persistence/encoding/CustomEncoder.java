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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-kernel-jpa-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.encoding;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.sdk.Proxy;

public class CustomEncoder extends Encoder {

	@Override
	public String encode(Name<?> name) {
		Serializable value = name.id();
		String result=null;
		if(Proxy.isSupported(value.getClass())) {
			result=Proxy.toString(value);
		} else {
			result=Encoder.valueEncoder().encode(name);
		}
		return result;
	}

	@Override
	public <T extends Serializable> Name<T> decode(String typeName,String data) {
		checkNotNull(typeName,"Type name cannot be null");
		try {
			Class<?> clazz=Class.forName(typeName);
			Class<? extends Serializable> serializableClass = clazz.asSubclass(Serializable.class);
			Name<T> result=null;
			if(Proxy.isSupported(clazz)) {
				@SuppressWarnings("unchecked")
				T value = (T)Proxy.fromString(serializableClass,data);
				result=NamingScheme.getDefault().name(value);
			} else {
				result=Encoder.valueEncoder().decode(typeName, data);
			}
			return result;
		} catch (ClassNotFoundException e) {
			throw new AssertionError("Cannot deserialize value '"+data+"' of type '"+typeName+"'",e);
		}
	}

}