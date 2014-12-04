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
package org.ldp4j.application.entity;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.net.URI;
import java.net.URL;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.junit.Test;


public class ObjectUtilTest {

	private Object[] langPrimitiveObjects = {
		Boolean.valueOf(true),
		Boolean.valueOf(false),
		String.class.getName(),
		Character.valueOf('c'),
	};

	private Object[] langNumberObjects = {
		Byte.valueOf(Byte.MIN_VALUE),
		Byte.valueOf(Byte.MAX_VALUE),
		Short.valueOf(Short.MIN_VALUE),
		Short.valueOf(Short.MAX_VALUE),
		Integer.valueOf(Integer.MIN_VALUE),
		Integer.valueOf(Integer.MAX_VALUE),
		Long.valueOf(Long.MIN_VALUE),
		Long.valueOf(Long.MAX_VALUE),
		Float.valueOf(Float.MIN_VALUE),
		Float.valueOf(Float.MAX_VALUE),
		Double.valueOf(Double.MIN_VALUE),
		Double.valueOf(Double.MAX_VALUE),
	};

	private Object[] xmlObjects = {
		new QName("localPart"),
		new QName("http://www.example.org/","localPart"),
		new QName("http://www.example.org/","localPart","example")
	};

	private Object[] utilObjects = {
		UUID.randomUUID()
	};

	@Test
	public void testIsSupported$lang$primitiveObjects() throws Exception {
		verifySupport(langPrimitiveObjects);
	}

	@Test
	public void testIsSupported$lang$numberObjects() throws Exception {
		verifySupport(langNumberObjects);
	}

	@Test
	public void testIsSupported$utilObjects() throws Exception {
		verifySupport(utilObjects);
	}

	@Test
	public void testIsSupported$xmlObjects() throws Exception {
		verifySupport(xmlObjects);
	}

	@Test
	public void testIsSupported$netObjects() throws Exception {
		URI uri=URI.create("http://www.example.org/");
		URL url=uri.toURL();
		verifySupport(uri,url);
	}

	@Test
	public void testRoundtrip$lang$primitiveObjects() throws Exception {
		verifyRoundtrip(langPrimitiveObjects);
	}

	@Test
	public void testRoundtrip$lang$numberObjects() throws Exception {
		verifyRoundtrip(langNumberObjects);
	}

	@Test
	public void testRoundtrip$utilObjects() throws Exception {
		verifyRoundtrip(utilObjects);
	}

	@Test
	public void testRoundtrip$xmlObjects() throws Exception {
		verifyRoundtrip(xmlObjects);
	}

	@Test
	public void testRoundtrip$netObjects() throws Exception {
		URI uri=URI.create("http://www.example.org/");
		URL url=uri.toURL();
		verifyRoundtrip(uri,url);
	}

	private void verifySupport(Object... rawObjects) {
		for(Object obj:rawObjects) {
			assertThat("Should support "+obj.getClass().getName(),ObjectUtil.isSupported(obj.getClass()),equalTo(true));
		}
	}

	private void verifyRoundtrip(Object... rawObjects) {
		for(Object obj:rawObjects) {
			String string = ObjectUtil.toString(obj);
			assertThat("Should marshal "+obj+" ("+obj.getClass().getName()+")",string,notNullValue());
			Object other=ObjectUtil.fromString(obj.getClass(), string);
			assertThat("Should unmarshal "+string+" ("+obj.getClass().getName()+")",other,notNullValue());
			assertThat("Should unmarshal "+string+" to "+obj+" ("+obj.getClass().getName()+")",other,equalTo(obj));
		}
	}

}
