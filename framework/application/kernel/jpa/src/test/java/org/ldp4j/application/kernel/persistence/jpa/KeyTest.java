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

import static mockit.Deencapsulation.getField;
import static mockit.Deencapsulation.invoke;
import static mockit.Deencapsulation.newInstance;
import static mockit.Deencapsulation.setField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.kernel.resource.ResourceId;

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

	@Test
	public void testNewInstance$null() {
		Key key=Key.newInstance(null);
		assertThat(key,nullValue());
	}

	@Test
	public void testEquals$sameInstance() {
		Key key=Key.newInstance(RESOURCE_ID);
		assertThat(key,equalTo(key));
	}

	@Test
	public void testEquals$differentInstance$sameResourceId() {
		Key k1=Key.newInstance(RESOURCE_ID);
		Key k2=Key.newInstance(RESOURCE_ID);
		assertThat(k1,equalTo(k2));
	}

	@Test
	public void testEquals$differentInstance$differentTemplateId() {
		Key k1=Key.newInstance(RESOURCE_ID);
		Key k2=Key.newInstance("random",k1.nameType(),k1.nameValue());
		assertThat(k1,not(equalTo(k2)));
	}

	@Test
	public void testEquals$differentInstance$differentNameType() {
		Key k1=Key.newInstance(RESOURCE_ID);
		Key k2=Key.newInstance(k1.templateId(),"random",k1.nameValue());
		assertThat(k1,not(equalTo(k2)));
	}

	@Test
	public void testEquals$differentInstance$differentNameValue() {
		Key k1=Key.newInstance(RESOURCE_ID);
		Key k2=Key.newInstance(k1.templateId(),k1.nameType(),"random");
		assertThat(k1,not(equalTo(k2)));
	}

	@Test
	public void testEquals$differentType() {
		Key k1=Key.newInstance(RESOURCE_ID);
		assertThat((Object)k1,not(equalTo((Object)"random")));
	}

	@Test
	public void testAssembly$regular() throws Exception {
		Key sut=unassambledKey();

		Key key=Key.newInstance(RESOURCE_ID);
		setField(sut, "templateId", key.templateId());
		setField(sut, "nameType", key.nameType());
		setField(sut, "nameValue", key.nameValue());

		ResourceId id=sut.resourceId();

		assertThat(id,equalTo(key.resourceId()));
		assertThat(sut,equalTo(key));
	}

	@Test
	public void testAssembly$corruption() throws Exception {
		Key sut=unassambledKey();

		Key key=Key.newInstance(RESOURCE_ID);
		setField(sut, "templateId", key.templateId());
		setField(sut, "nameType", "bad type");
		setField(sut, "nameValue", key.nameValue());

		ResourceId id=sut.resourceId();

		assertThat(id,equalTo(key.resourceId()));
		assertThat(sut,equalTo(key));
		assertThat(sut.nameType(),equalTo(key.nameType()));
	}

	@Test
	public void testAssembly$noTemplateId() throws Exception {
		Key sut=unassambledKey();

		Key key=Key.newInstance(RESOURCE_ID);
		setField(sut, "nameType", key.nameType());
		setField(sut, "nameValue", key.nameValue());

		ResourceId id=sut.resourceId();

		assertThat(id,nullValue());
		assertThat(sut.resourceId(),nullValue());
	}

	@Test
	public void testAssembly$noNameType() throws Exception {
		Key sut=unassambledKey();

		Key key=Key.newInstance(RESOURCE_ID);
		setField(sut, "templateId", key.templateId());
		setField(sut, "nameValue", key.nameValue());

		ResourceId id=sut.resourceId();

		assertThat(id,notNullValue());
		assertThat(sut.resourceId(),equalTo(key.resourceId()));
	}

	@Test
	public void testAssembly$noNameValue() throws Exception {
		Key sut=unassambledKey();

		Key key=Key.newInstance(RESOURCE_ID);
		setField(sut, "templateId", key.templateId());
		setField(sut, "nameType", key.nameType());

		ResourceId id=sut.resourceId();

		assertThat(id,nullValue());
		assertThat(sut.resourceId(),nullValue());
	}

	@Test
	public void testAssembly$noCacheOverwrite() {
		Key k1=Key.newInstance(RESOURCE_ID);
		ResourceId id1=k1.resourceId();
		invoke(k1, "assemble");
		ResourceId id2=k1.resourceId();
		assertThat(id2,sameInstance(id1));
	}

	private Key unassambledKey() {
		Key sut=newInstance(Key.class.getName());
		assertCacheIsEmpty(sut);
		assertThat(getField(sut,"templateId"),nullValue());
		assertThat(getField(sut,"nameType"),nullValue());
		assertThat(getField(sut,"nameValue"),nullValue());
		return sut;
	}

	private void assertCacheIsEmpty(Key sut) {
		assertThat((boolean)getField(sut,"cacheAvailable"),equalTo(false));
		assertThat(getField(sut,"cachedId"),nullValue());
	}

}
