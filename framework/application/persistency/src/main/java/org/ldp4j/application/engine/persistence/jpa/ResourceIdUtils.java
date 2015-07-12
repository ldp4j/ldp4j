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

import javax.persistence.AttributeConverter;

import org.apache.commons.codec.binary.Base64;
import org.ldp4j.application.engine.resource.ResourceId;


public final class ResourceIdUtils implements AttributeConverter<ResourceId,String> {

	@Override
	public String convertToDatabaseColumn(ResourceId attribute) {
		if(attribute==null) {
			return null;
		}
		try {
			byte[] serializedData=SerializationUtils.serialize(attribute);
			return Base64.encodeBase64String(serializedData);
		} catch (IOException e) {
			throw new AssertionError("Serialization should not fail",e);
		}
	}

	@Override
	public ResourceId convertToEntityAttribute(String dbData) {
		if(dbData==null) {
			return null;
		}
		try {
			byte[] serializedData=Base64.decodeBase64(dbData);
			return SerializationUtils.deserialize(serializedData,ResourceId.class);
		} catch (IOException e) {
			throw new AssertionError("Deserialization should not fail",e);
		}
	}

}
