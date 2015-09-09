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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-engine-sdk-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.ldp4j.application.data.TimeUtils;
import org.ldp4j.application.sdk.spi.ObjectParseException;
import org.ldp4j.application.sdk.spi.ObjectTransformationException;

import com.google.common.collect.Lists;


public class ObjectUtilTest {


	private Class<?>[] primitiveClasses = {
		boolean.class,
		char.class,
		byte.class,
		short.class,
		int.class,
		long.class,
		double.class,
		float.class,
	};

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

	private DateTime now;

	public enum DefaultEnumType {
		V1,
		V2,
		;
	}

	private String dateTimeAsISOString() {
		return now.toString();
	}

	private String durationAsISOString() {
		return jodaDuration().toString();
	}

	private Duration jodaDuration() {
		return new Duration(1000);
	}

	private javax.xml.datatype.Duration jxmlDuration() throws DatatypeConfigurationException {
		return DatatypeFactory.newInstance().newDuration(1000);
	}

	private void verifyTypeSupport(Class<?> clazz) {
		assertThat("Should support "+clazz.getName(),ObjectUtil.isSupported(clazz),equalTo(true));
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

	@Before
	public void setUp() {
		this.now=new DateTime(new Date());
	}

	@Test
	public void rejectUnsupportedTypes() {
		assertThat(ObjectUtil.isSupported(ObjectUtil.class),equalTo(false));
	}

	@Test
	public void rejectUnsupportedTypes$parseFailure() {
		try {
			ObjectUtil.fromString(ObjectUtil.class,"random data");
			fail("Should fail on unsupported types");
		} catch (ObjectTransformationException e) {
			assertThat((Object)e.getValueClass(),equalTo((Object)ObjectUtil.class));
		}
	}

	@Test
	public void rejectUnsupportedTypes$toStringFailure() {
		try {
			ObjectUtil.toString(Lists.newArrayList());
			fail("Should fail on unsupported types");
		} catch (ObjectTransformationException e) {
			assertThat((Object)e.getValueClass(),equalTo((Object)ArrayList.class));
		}
	}

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
	public void testIsSupported$enumObjects() throws Exception {
		verifySupport(DefaultEnumType.V1,DefaultEnumType.V2);
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

	@Test
	public void testRoundtrip$enumObjects() throws Exception {
		verifyRoundtrip(DefaultEnumType.V1,DefaultEnumType.V2);
	}

	@Test
	public void testPrimitiveSupport() throws Exception {
		for(Class<?> clazz:primitiveClasses) {
			verifyTypeSupport(clazz);
		}
	}

	@Test
	public void testBytePrimitiveRoundtrip() throws Exception {
		verifyPrimitiveRoundtrip(byte.class,(byte)5);
	}

	@Test
	public void testShortPrimitiveRoundtrip() throws Exception {
		verifyPrimitiveRoundtrip(short.class,(short)5);
	}

	@Test
	public void testIntPrimitiveRoundtrip() throws Exception {
		verifyPrimitiveRoundtrip(int.class,(int)5);
	}

	@Test
	public void testLongPrimitiveRoundtrip() throws Exception {
		verifyPrimitiveRoundtrip(long.class,(long)5);
	}

	@Test
	public void testFloatPrimitiveRoundtrip() throws Exception {
		verifyPrimitiveRoundtrip(float.class,(float)5);
	}

	@Test
	public void testDoublePrimitiveRoundtrip() throws Exception {
		verifyPrimitiveRoundtrip(double.class,(double)5);
	}

	@Test
	public void testBooleanPrimitiveRoundtrip() throws Exception {
		verifyPrimitiveRoundtrip(boolean.class,true);
		verifyPrimitiveRoundtrip(boolean.class,false);
	}

	@Test
	public void testCharPrimitiveRoundtrip() throws Exception {
		verifyPrimitiveRoundtrip(char.class,'5');
	}

	@Test
	public void testCharPrimitiveRoundtrip$not_a_char() throws Exception {
		try {
			ObjectUtil.fromString(char.class, "not char");
			fail("Should not accept strings with more than one character");
		} catch (ObjectParseException e) {
			assertThat(e.getRawValue(),equalTo("not char"));
			assertThat((Object)e.getValueClass(),equalTo((Object)char.class));
		}
	}

	@Test
	public void testCharactereRoundtrip$not_a_char() throws Exception {
		try {
			ObjectUtil.fromString(Character.class, "not char");
			fail("Should not accept strings with more than one character");
		} catch (ObjectParseException e) {
			assertThat(e.getRawValue(),equalTo("not char"));
			assertThat((Object)e.getValueClass(),equalTo((Object)Character.class));
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void verifyPrimitiveRoundtrip(Class<T> type, T value) {
		String rawValue=ObjectUtil.toString(value);
		Object obj = ObjectUtil.fromString(type, rawValue);
		assertThat(obj,instanceOf(type));
		assertThat((T)obj,equalTo(value));
	}

	@Test
	public void testDateTimeSupport() throws Exception {
		verifyTypeSupport(DateTime.class);
	}

	@Test
	public void testDateTimeRoundtrip() throws Exception {
		verifyRoundtrip(this.now);
	}

	@Test
	public void testXMLGregorianCalendarSupport() throws Exception {
		verifyTypeSupport(javax.xml.datatype.XMLGregorianCalendar.class);
	}

	@Test
	public void testXMLGregorianCalendarRoundtrip() throws Exception {
		XMLGregorianCalendar date = TimeUtils.newInstance().from(this.now).toXMLGregorianCalendar();
		assertThat(ObjectUtil.fromString(XMLGregorianCalendar.class, dateTimeAsISOString()),equalTo(date));
	}

	@Test
	public void testUtilDateSupport() throws Exception {
		verifyTypeSupport(java.util.Date.class);
	}

	@Test
	public void testUtilDateRoundtrip() throws Exception {
		Date date = TimeUtils.newInstance().from(this.now).toDate();
		assertThat(ObjectUtil.fromString(Date.class, dateTimeAsISOString()),equalTo(date));
	}

	@Test
	public void testSqlDateSupport() throws Exception {
		verifyTypeSupport(java.sql.Date.class);
	}

	@Test
	public void testSqlDateRoundtrip() throws Exception {
		Date date = TimeUtils.newInstance().from(this.now).toSqlDate();
		assertThat(ObjectUtil.fromString(java.sql.Date.class, dateTimeAsISOString()),equalTo(date));
	}

	@Test
	public void testJodaDurationSupport() throws Exception {
		verifyTypeSupport(Duration.class);
	}

	@Test
	public void testJodaDurationRoundtrip() throws Exception {
		assertThat(ObjectUtil.fromString(Duration.class, durationAsISOString()),equalTo(jodaDuration()));
	}

	@Test
	public void testXmlDurationSupport() throws Exception {
		verifyTypeSupport(javax.xml.datatype.Duration.class);
	}

	@Test
	public void testXmlDurationRoundtrip() throws Exception {
		assertThat(ObjectUtil.fromString(javax.xml.datatype.Duration.class, durationAsISOString()),equalTo(jxmlDuration()));
	}
}
