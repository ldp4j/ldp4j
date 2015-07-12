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

import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.resource.ResourceId;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class KeyTest {

	private static final Name<String> NAME = NamingScheme.getDefault().name("value");
	private static final String TEMPLATE_ID = "templateId";
	private static final ResourceId RESOURCE_ID = ResourceId.createId(NAME,TEMPLATE_ID);

	@Test
	public void testNewInstance() {
		Key k1=Key.newInstance(RESOURCE_ID);
		assertThat(k1,notNullValue());
		assertThat(k1.templateId(),equalTo(TEMPLATE_ID));
		assertThat(k1.nameType(),equalTo(String.class.getName()));
		assertThat(k1.nameValue(),notNullValue());
		assertThat(k1.resourceId(),equalTo(RESOURCE_ID));
	}

	@Test
	public void testNewInstance$parts() {
		Key k1=Key.newInstance(RESOURCE_ID);
		Key k2=Key.newInstance(k1.templateId(),k1.nameType(),k1.nameValue());
		assertThat(k2,notNullValue());
		assertThat(k2.templateId(),equalTo(k1.templateId()));
		assertThat(k2.nameType(),equalTo(k1.nameType()));
		assertThat(k2.nameValue(),equalTo(k1.nameValue()));
		assertThat(k2.resourceId(),equalTo(k1.resourceId()));
	}

}
