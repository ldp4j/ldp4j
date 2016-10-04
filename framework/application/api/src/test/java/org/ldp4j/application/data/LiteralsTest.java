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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiteralsTest {

	private static class SerializableData implements Serializable {

		private static final long serialVersionUID = 6185440942019142775L;

	}

	private static class NotSerializableData {

	}

	private static Logger LOGGER=LoggerFactory.getLogger(LiteralsTest.class);

	private static final Date NOW = new Date();

	private DateTimeLiteral dateTime;

	@Before
	public void setUp() {
		this.dateTime = Literals.of(NOW).dateTime();
	}

	@Test
	public void verifyIsUtilityClass() {
		assertThat(Utils.isUtilityClass(Literals.class),equalTo(true));
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
	public void testNewLiteral$nonTemporal$serializable() {
		SerializableData data = new SerializableData();
		Literal<? extends Serializable> literal = Literals.newLiteral(data);
		assertThat(literal.get(),sameInstance((Serializable)data));
		if(literal instanceof TypedLiteral<?>) {
			assertThat(((TypedLiteral<?>)literal).type(),equalTo(Datatypes.STRING));
		}
	}

	@Test
	public void testNewLiteral$nonTemporal$notSerializable() {
		NotSerializableData data = new NotSerializableData();
		Literal<? extends Serializable> literal = Literals.newLiteral(data);
		assertThat(literal.get(),equalTo((Serializable)data.toString()));
		if(literal instanceof TypedLiteral<?>) {
			assertThat(((TypedLiteral<?>)literal).type(),equalTo(Datatypes.STRING));
		}
	}

	@Test
	public void testNewLiteral$duration$jodaDuration() {
		checkDurationLiteral(Literals.newLiteral(jodaDuration()));
	}

	@Test
	public void testNewLiteral$duration$javaDuration() throws DatatypeConfigurationException {
		checkDurationLiteral(Literals.newLiteral(javaDuration()));
	}

	private Duration jodaDuration() {
		return new Duration(1000);
	}

	private javax.xml.datatype.Duration javaDuration() throws DatatypeConfigurationException {
		return DatatypeFactory.newInstance().newDuration(1000);
	}

	@Test
	public void testNewTypedLiteral$duration$jodaDuration() {
		checkDurationLiteral(Literals.newTypedLiteral(jodaDuration(),Datatypes.DURATION));
	}

	@Test
	public void testNewTypedLiteral$duration$javaDuration() throws DatatypeConfigurationException {
		checkDurationLiteral(Literals.newTypedLiteral(javaDuration(),Datatypes.DURATION));
	}

	@Test
	public void testNewTypedLiteral$duration$string() throws DatatypeConfigurationException {
		checkDurationLiteral(Literals.newTypedLiteral(javaDuration().toString(),Datatypes.DURATION));
	}

	@Test(expected=DatatypeCohercionException.class)
	public void testNewTypedLiteral$duration$cohercionFailure$string() throws DatatypeConfigurationException {
		checkDurationLiteral(Literals.newTypedLiteral("invalid",Datatypes.DURATION));
	}

	@Test(expected=DatatypeCohercionException.class)
	public void testNewTypedLiteral$duration$cohercionFailure$notString() throws DatatypeConfigurationException {
		checkDurationLiteral(Literals.newTypedLiteral(new Date(),Datatypes.DURATION));
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

	@Test
	public void testNewTypeLiteral$temporal$cohercionFailure$notString() {
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
	public void testNewTypeLiteral$temporal$cohercionFailure$string() {
		Object value = "invalid";
		for(URI datatype:Datatypes.temporalDatatypes()) {
			LOGGER.debug("Trying to coherce not temporal string using {} ({}) with {}...",value,value.getClass().getName(),datatype);
			try {
				Literals.newTypedLiteral(value, datatype);
				fail("Should fail when cohercion is not possible");
			} catch (DatatypeCohercionException e) {
				LOGGER.debug("Failed as expected: {}",e.getMessage());
			}
		}
	}

	@Test
	public void testNewTypedLiteral$nonTemporal$serializable() {
		SerializableData data = new SerializableData();
		URI datatype = URI.create("urn:my:datatype");
		TypedLiteral<? extends Serializable> literal = Literals.newTypedLiteral(data, datatype);
		assertThat(literal.get(),sameInstance((Serializable)data));
		assertThat(literal.type(),equalTo(datatype));
	}

	@Test
	public void testNewTypedLiteral$nonTemporal$notSerializable() {
		NotSerializableData data = new NotSerializableData();
		URI datatype = URI.create("urn:my:datatype");
		TypedLiteral<? extends Serializable> literal = Literals.newTypedLiteral(data, datatype);
		assertThat(literal.get(),equalTo((Serializable)data.toString()));
		assertThat(literal.type(),equalTo(Datatypes.STRING));
	}

	@Test
	public void testCreationFromXMLGregorianCalendar$customLocale() {
		XMLGregorianCalendar xgc = xmlGregorianCalendar();
		DateTimeLiteral dateTimeLiteral=
			Literals.
				of(xgc).
					withLocale(Locale.GERMANY).
						dateTime();
		assertThat(this.dateTime,equalTo(dateTimeLiteral));
	}

	@Test
	public void testCreationFromXMLGregorianCalendar$customTimeZone() {
		XMLGregorianCalendar xgc = xmlGregorianCalendar();

		TimeZone timeZone = anotherTimeZone(this.dateTime.get());

		DateTimeLiteral dateTimeLiteral=
			Literals.
				of(xgc).
					withTimeZone(timeZone).
						dateTime();
		assertThat(this.dateTime,not(equalTo(dateTimeLiteral)));
		assertThat(this.dateTime.get().compareTo(dateTimeLiteral.get()),not(equalTo(0)));
	}

	@Test
	public void testCreationFromXMLGregorianCalendar$customDefaults() throws DatatypeConfigurationException {
		XMLGregorianCalendar xgc = xmlGregorianCalendar();

		XMLGregorianCalendar nxgc =
			DatatypeFactory.
				newInstance().
					newXMLGregorianCalendar(
						new GregorianCalendar(anotherTimeZone(this.dateTime.get())));
		DateTimeLiteral dateTimeLiteral=
			Literals.
				of(xgc).
					withDefaults(nxgc).
						dateTime();

		assertThat(this.dateTime,equalTo(dateTimeLiteral));
	}

	@Test(expected=NullPointerException.class)
	public void testNewLanguageLiteral$nullValue() {
		Literals.newLanguageLiteral(null, "not null");
	}

	@Test(expected=NullPointerException.class)
	public void testNewLanguageLiteral$nullLanguage() {
		Literals.newLanguageLiteral("not null",null);
	}

	@Test
	public void testNewLanguage$regular() {
		LanguageLiteral nwl = Literals.newLanguageLiteral("value","language");
		assertThat(nwl,notNullValue());
		assertThat(nwl.get(),equalTo("value"));
		assertThat(nwl.language(),equalTo("language"));
	}

	@Test(expected=NullPointerException.class)
	public void testDuration$nullUnit() {
		Literals.duration(0l, null);
	}

	@Test
	public void testDuration$negativeDuration() {
		DurationLiteral nwl=Literals.duration(-1,TimeUnit.DAYS);
		assertThat(nwl,notNullValue());
		assertThat(nwl.get(),equalTo(new Duration(-24*60*60*1000)));
	}

	@Test
	public void testDuration$regularDuration() {
		DurationLiteral nwl = Literals.duration(1,TimeUnit.DAYS);
		assertThat(nwl,notNullValue());
		assertThat(nwl.get(),equalTo(new Duration(24*60*60*1000)));
	}

	@Test(expected=NullPointerException.class)
	public void testOf$duration$null() {
		Literals.of((javax.xml.datatype.Duration)null);
	}

	@Test
	public void testOf$duration$notNull() throws DatatypeConfigurationException {
		javax.xml.datatype.Duration dr = (javax.xml.datatype.Duration)javaDuration();
		DurationLiteral literal = Literals.of(dr);
		assertThat(literal,notNullValue());
		assertThat(literal.get(),equalTo(jodaDuration()));
	}

	@Test(expected=NullPointerException.class)
	public void testOf$string$nullValue() {
		Literals.of((String)null);
	}

	@Test
	public void testOf$string$notNull() {
		String data = "value";
		Literal<String> literal=Literals.of(data);
		assertThat(literal.get(),equalTo((Serializable)data.toString()));
		if(literal instanceof TypedLiteral<?>) {
			assertThat(((TypedLiteral<?>)literal).type(),equalTo(Datatypes.STRING));
		}
	}

	private void checkDurationLiteral(Object nl) {
		assertThat(nl,instanceOf(DurationLiteral.class));
		DurationLiteral literal = (DurationLiteral)nl;
		assertThat(literal,notNullValue());
		assertThat(literal.get(),equalTo(jodaDuration()));
	}

	private XMLGregorianCalendar xmlGregorianCalendar() {
		return
			TimeUtils.
				newInstance().
					from(this.dateTime.get()).
						toXMLGregorianCalendar();
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

	private TimeZone anotherTimeZone(DateTime dateTime) {
		String tzID = dateTime.getZone().getID();
		String[] tzIDs = TimeZone.getAvailableIDs();
		TimeZone timeZone = null;
		for(int i=0;i<tzIDs.length;i++) {
			if(!tzIDs[i].equals(tzID)) {
				timeZone=TimeZone.getTimeZone(tzIDs[i]);
				break;
			}
		}
		return timeZone;
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
