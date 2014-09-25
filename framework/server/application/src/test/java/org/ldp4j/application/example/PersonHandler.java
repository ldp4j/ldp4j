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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-application:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-application-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.example;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.ext.Deletable;
import org.ldp4j.application.ext.Modifiable;
import org.ldp4j.application.ext.annotations.Attachment;
import org.ldp4j.application.ext.annotations.Resource;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;

@Resource(
	id=PersonHandler.ID,
	attachments={
		@Attachment(
			id="address",
			path="address",
			predicate="http://www.ldp4j.org/vocabularies/example#address",
			handler=AddressHandler.class),
		@Attachment(
			id="books",
			path="books",
			handler=BookContainerHandler.class),	
		@Attachment(
			id="personRelatives",
			path="relatives",
			handler=RelativeContainerHandler.class)	
	}
)
public class PersonHandler extends InMemoryResourceHandler implements Modifiable, Deletable {

	public static final String ID="personTemplate";
	
	public PersonHandler() {
		super("Person");
	}

	@Override
	public void delete(ResourceSnapshot resource, WriteSession session) {
		DataSet dataSet = get(resource);
		try {
			remove(resource.name());
			session.delete(resource);
			session.saveChanges();
		} catch (WriteSessionException e) {
			// Recover if failed
			add(resource.name(),dataSet);
			throw new IllegalStateException("Deletion failed",e);
		}
	}

	@Override
	public void update(ResourceSnapshot resource, DataSet content, WriteSession session) {
		DataSet dataSet = get(resource);
		try {
			add(resource.name(),content);
			session.modify(resource);
			session.saveChanges();
		} catch (WriteSessionException e) {
			// Recover if failed
			add(resource.name(),dataSet);
			throw new IllegalStateException("Update failed",e);
		}
	}

}