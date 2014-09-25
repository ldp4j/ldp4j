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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.api.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class HttpDateUtilsTest {

	private static final RuntimeException DEFAULT_EXCEPTION = new RuntimeException("Default exception");
	private static final Date             DEFAULT_DATE      = new Date();

	private static final String RAW_INVALID_DATE  = new Date().toString();
	private static final String RAW_EXPECTED_DATE = "Sun, 06 Nov 1994 08:49:37 GMT";

	private static final String           NULL_STRING   =null;
	private static final Date             NULL_DATE     =null;
	private static final RuntimeException NULL_EXCEPTION=null;
	
	private static Date EXPECTED_DATE;

	private static Map<String,String> VALID_VALUES=
		ImmutableMap.<String,String>builder().
				put("RFC 822, updated by RFC 1123",RAW_EXPECTED_DATE).
				put("RFC 850, obsoleted by RFC 1036","Sunday, 06-Nov-94 08:49:37 GMT").
				put("ANSI C's asctime() format","Sun Nov  6 08:49:37 1994").build();

	@BeforeClass
	public static void setUpClass() throws Exception {
		Calendar calendar=new GregorianCalendar(Locale.US);
		calendar.setLenient(false);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		calendar.set(Calendar.DAY_OF_MONTH, 6);
		calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
		calendar.set(Calendar.YEAR, 1994);
		calendar.set(Calendar.HOUR_OF_DAY, 8);
		calendar.set(Calendar.MINUTE, 49);
		calendar.set(Calendar.SECOND, 37);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.ERA,GregorianCalendar.AD);
		EXPECTED_DATE=calendar.getTime();
	}

	private void validateValidValueParsedResult(Date parsedValue, String knownFormat) {
		assertThat("Format "+knownFormat+" not supported",parsedValue,is(notNullValue()));
		assertThat("Invalid parsed date",parsedValue,is(equalTo(EXPECTED_DATE)));
	}

	@Test
	public void testFormat_happyPath() throws Exception {
		assertThat(HttpDateUtils.format(EXPECTED_DATE),is(equalTo(RAW_EXPECTED_DATE)));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFormat_exceptionPath() throws Exception {
		HttpDateUtils.format(NULL_DATE);
	}

	@Test
	public void testParse_happyPath() throws Exception {
		for(Entry<String,String> entry:VALID_VALUES.entrySet()) {
			validateValidValueParsedResult(
				HttpDateUtils.parse(entry.getValue()), 
				entry.getKey());
		}
	}

	@Test
	public void testParse_exceptionPath_invalidArgument() throws Exception {
		try {
			HttpDateUtils.parse(NULL_STRING);
			fail("Null value should not be accepted");
		} catch (UnknownHttpDateFormatException e) {
			assertThat(e.getCause(),is(nullValue()));
		}
	}

	@Test
	public void testParse_exceptionPath_parseException() throws Exception {
		try {
			HttpDateUtils.parse(RAW_INVALID_DATE);
			fail("Invalid date formatted string should not be accepted");
		} catch (UnknownHttpDateFormatException e) {
			assertThat(e.getCause(),is(instanceOf(ParseException.class)));
		}
	}

	@Test
	public void testParseWithDefaultDate_happyPath_withoutDefault() throws Exception {
		for(Entry<String,String> entry:VALID_VALUES.entrySet()) {
			validateValidValueParsedResult(
				HttpDateUtils.parse(entry.getValue(),NULL_DATE), 
				entry.getKey());
		}
	}

	@Test
	public void testParseWithDefaultDate_happyPath_withDefault() throws Exception {
		for(Entry<String,String> entry:VALID_VALUES.entrySet()) {
			validateValidValueParsedResult(
				HttpDateUtils.parse(entry.getValue(),DEFAULT_DATE), 
				entry.getKey());
		}
	}

	@Test
	public void testParseWithDefaultDate_exceptionPath_returnDefault() throws Exception {
		Date date = HttpDateUtils.parse(NULL_STRING,EXPECTED_DATE);
		assertThat(date,is(equalTo(EXPECTED_DATE)));
	}

	@Test
	public void testParseWithDefaultDate_exceptionPath_throwException() throws Exception {
		try {
			HttpDateUtils.parse(NULL_STRING,NULL_DATE);
			fail("Null value with null default date should not be accepted");
		} catch (UnknownHttpDateFormatException e) {
			assertThat(e.getCause(),is(nullValue()));
		}
	}

	@Test
	public void testParseWithDefaultDate_exceptionPath_whenInvalidValue_returnDefaultDate() throws Exception {
		assertThat(
			HttpDateUtils.parse(RAW_INVALID_DATE,DEFAULT_DATE),
			is(equalTo(DEFAULT_DATE)));
	}

	@Test
	public void testParseWithDefaultException_happyPath_withoutDefault() throws Exception {
		for(Entry<String,String> entry:VALID_VALUES.entrySet()) {
			validateValidValueParsedResult(
				HttpDateUtils.parse(entry.getValue(),NULL_EXCEPTION), 
				entry.getKey());
		}
	}

	@Test
	public void testParseWithDefaultException_happyPath_withDefault() throws Exception {
		for(Entry<String,String> entry:VALID_VALUES.entrySet()) {
			validateValidValueParsedResult(
				HttpDateUtils.parse(entry.getValue(),DEFAULT_EXCEPTION), 
				entry.getKey());
		}
	}

	@Test
	public void testParseWithDefaultException_exceptionPath_whenNullValue_throwDefaultException() throws Exception {
		try {
			HttpDateUtils.parse(NULL_STRING,DEFAULT_EXCEPTION);
			fail("Null value should not be accepted");
		} catch (Exception e) {
			assertThat(e,is(sameInstance((Exception)DEFAULT_EXCEPTION)));
			assertThat(e.getCause(),is(nullValue()));
		}
	}

	@Test
	public void testParseWithDefaultException_exceptionPath_whenInvalidValue_throwDefaultException() throws Exception {
		try {
			HttpDateUtils.parse(RAW_INVALID_DATE,DEFAULT_EXCEPTION);
			fail("Invalid value should not be accepted");
		} catch (Exception e) {
			assertThat(e,is(sameInstance((Exception)DEFAULT_EXCEPTION)));
			assertThat(e.getCause(),is(instanceOf(ParseException.class)));
		}
	}

	@Test
	public void testParseWithNullDefaultException_exceptionPath_whenNullValue_throwRuntimeException() throws Exception {
		try {
			HttpDateUtils.parse(NULL_STRING,NULL_EXCEPTION);
			fail("Null value with null default date should not be accepted");
		} catch (IllegalArgumentException e) {
			assertThat(e.getCause(),is(nullValue()));
		}
	}

	@Test
	public void testParseWithNullDefaultException_exceptionPath_whenInvalidValue_throwRuntimeException() throws Exception {
		try {
			HttpDateUtils.parse(RAW_INVALID_DATE,NULL_EXCEPTION);
			fail("Invalid value with null default exception should not be accepted");
		} catch (UnknownHttpDateFormatException e) {
			assertThat(e.getCause(),is(instanceOf(ParseException.class)));
		}
	}
}
