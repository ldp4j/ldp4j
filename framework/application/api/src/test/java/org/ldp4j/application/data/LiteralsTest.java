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
package org.ldp4j.application.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiteralsTest {

	private static Logger LOGGER=LoggerFactory.getLogger(LiteralsTest.class);

	private static final Date NOW = new Date();

	private DateTimeLiteral dateTime;

	@Before
	public void setUp() {
		this.dateTime = Literals.of(NOW).dateTime();
	}

	@Test
	public void testNewLiteral$temporal$date() {
		checkNewLiteral(
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toDate());
	}

	@Test
	public void testNewLiteral$temporal$sqlDate() {
		checkNewLiteral(
			Datatypes.DATE,
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toSqlDate());
	}

	@Test
	public void testNewLiteral$temporal$sqlTime() {
		checkNewLiteral(
			Datatypes.TIME,
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toSqlTime());
	}

	@Test
	public void testNewLiteral$temporal$calendar() {
		checkNewLiteral(nonGregorianCalendar());
	}

	private Calendar nonGregorianCalendar() {
		Locale locale=null;
		for(Locale aLocale:Locale.getAvailableLocales()) {
			String unicodeLocaleType = aLocale.getUnicodeLocaleType("ca");
			if(unicodeLocaleType!=null) {
				locale=aLocale;
				break;
			}
		}
		Calendar calendar=Calendar.getInstance(locale);
		calendar.setTime(NOW);
		return calendar;
	}

	@Test
	public void testNewLiteral$temporal$gregorianCalendar() {
		checkNewLiteral(
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toGregorianCalendar());
	}

	@Test
	public void testNewLiteral$temporal$xmlGregorianCalendar() {
		checkNewLiteral(
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toXMLGregorianCalendar());
	}

	@Test
	public void testNewLiteral$temporal$dateTime() {
		checkNewLiteral(this.dateTime.get());
	}

	@Test
	public void testNewTypeLiteral$temporalCohercionFailure() {
		Object value = new Object();
		for(URI datatype:Datatypes.temporalDatatypes()) {
			LOGGER.debug("Trying to coherce not temporal object using {} ({}) with {}...",value,value.getClass().getName(),datatype);
			try {
				Literals.newTypedLiteral(value, datatype);
				fail("Should fail when cohercion is not possible");
			} catch (DatatypeCohercionException e) {
				LOGGER.debug("Failed as expected: {}",e.getMessage());
			}
		}
	}

	@Test
	public void testNewTypeLiteral$temporal$date() {
		checkNewTypeLiteral(
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toDate());
	}

	@Test
	public void testNewTypeLiteral$temporal$sqlDate() {
		checkNewTypeLiteral(
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toSqlDate());
	}

	@Test
	public void testNewTypeLiteral$temporal$sqlTime() {
		checkNewTypeLiteral(
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toSqlTime());
	}

	@Test
	public void testNewTypeLiteral$temporal$calendar() {
		checkNewTypeLiteral(nonGregorianCalendar());
	}

	@Test
	public void testNewTypeLiteral$temporal$gregorianCalendar() {
		checkNewTypeLiteral(
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toGregorianCalendar());
	}

	@Test
	public void testNewTypeLiteral$temporal$xmlGregorianCalendar() {
		checkNewTypeLiteral(
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toXMLGregorianCalendar());
	}

	@Test
	public void testNewTypeLiteral$temporal$dateTime() {
		checkNewTypeLiteral(this.dateTime.get());
	}

	@Test
	public void testNewTypeLiteral$temporal$string() {
		checkNewTypeLiteral(this.dateTime.get().toString());
	}

	private void checkNewLiteral(URI datatype, Object value) {
		LOGGER.debug("Trying literal creation using temporal value {} ({})...",value,value.getClass().getName());
		Literal<?> literal=Literals.newLiteral(value);
		LOGGER.debug("Got: {}",literal);
		assertThat(
			temporalAssertionMessage(value),
			literal,instanceOf(DateTimeLiteral.class));
		assertThat(((DateTimeLiteral)literal).type(),equalTo(datatype));
		assertThat(((DateTimeLiteral)literal).get(),equalTo(this.dateTime.get()));
	}

	private void checkNewLiteral(Object value) {
		checkNewLiteral(Datatypes.DATE_TIME,value);
	}

	private void checkNewTypeLiteral(Object value) {
		for(URI datatype:Datatypes.temporalDatatypes()) {
			LOGGER.debug("Trying typed literal creation using {} ({}) with {}...",value,value.getClass().getName(),datatype);
			Literal<?> literal=Literals.newTypedLiteral(value, datatype);
			LOGGER.debug("Got: {}",literal);
			assertThat(
				temporalAssertionMessage(datatype, value),
				literal,instanceOf(DateTimeLiteral.class));
			assertThat(((DateTimeLiteral)literal).get(),equalTo(this.dateTime.get()));
		}
	}

	private String temporalAssertionMessage(Object value) {
		return
			String.format("An instance of temporal type %s should be wrapped with a DateTimeLiteral instance",value.getClass().getName());
	}

	private String temporalAssertionMessage(URI datatype, Object value) {
		return
			String.format("An instance of type %s should be wrapped with a DateTimeLiteral instance when using temporal datatype %s",value.getClass().getName(),datatype);
	}

}
