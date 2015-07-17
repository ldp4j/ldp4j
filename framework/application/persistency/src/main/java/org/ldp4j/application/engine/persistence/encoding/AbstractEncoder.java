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
package org.ldp4j.application.engine.persistence.encoding;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.ldp4j.application.data.Name;


abstract class AbstractEncoder extends Encoder {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String encode(Name<?> name) {
		if(name==null) {
			return null;
		}
		try {
			Serializable target = prepare(name);
			byte[] serializedData=SerializationUtils.serialize(target);
			return Base64.encodeBase64String(serializedData);
		} catch (IOException e) {
			throw new AssertionError("Serialization should not fail",e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final <T extends Serializable> Name<T> decode(String data) {
		if(data==null) {
			return null;
		}
		try {
			byte[] serializedData=Base64.decodeBase64(data);
			Serializable subject=SerializationUtils.deserialize(serializedData, Serializable.class);
			return assemble(subject);
		} catch (IOException e) {
			throw new AssertionError("Deserialization should not fail",e);
		}
	}

	protected abstract Serializable prepare(Name<?> name);

	protected abstract <T extends Serializable> Name<T> assemble(Serializable subject) throws IOException;

}