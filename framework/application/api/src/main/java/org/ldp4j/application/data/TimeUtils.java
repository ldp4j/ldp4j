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

import static com.google.common.base.Preconditions.checkNotNull;

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

public final class TimeUtils {

	public final class DateTimeConverter {

		private final DateTime literal;

		private DateTimeConverter(DateTime literal) {
			this.literal = literal;
		}

		public long to(TimeUnit unit) {
			return unit.convert(toDate().getTime(),TimeUnit.MILLISECONDS);
		}

		public Date toDate() {
			return this.literal.toDate();
		}

		public java.sql.Date toSqlDate() {
			return new java.sql.Date(toDate().getTime());
		}

		public java.sql.Time toSqlTime() {
			return new java.sql.Time(toDate().getTime());
		}

		public Calendar toCalendar() {
			return this.literal.toCalendar(TimeUtils.this.locale);
		}

		public GregorianCalendar toGregorianCalendar() {
			return this.literal.toGregorianCalendar();
		}

		public XMLGregorianCalendar toXMLGregorianCalendar() {
			return TimeUtils.this.datatypeFactory.newXMLGregorianCalendar(toGregorianCalendar());
		}

	}

	public final class DurationConverter {

		private final Duration duration;

		private DurationConverter(Duration literal) {
			this.duration=literal;
		}

		public long millis() {
			return this.duration.getMillis();
		}

		public long to(TimeUnit unit) {
			return unit.convert(millis(),TimeUnit.MILLISECONDS);
		}

		public javax.xml.datatype.Duration toDuration() {
			return TimeUtils.this.datatypeFactory.newDuration(millis());
		}

	}

	private final TimeZone timezone;
	private final Locale locale;
	private final XMLGregorianCalendar defaults;
	private final DatatypeFactory datatypeFactory;

	private TimeUtils(DatatypeFactory datatypeFactory, TimeZone timezone, Locale locale, XMLGregorianCalendar defaults) {
		this.datatypeFactory = datatypeFactory;
		this.timezone = timezone;
		this.locale = locale;
		this.defaults = defaults;
	}

	public TimeUtils withTimeZone(TimeZone timezone) {
		return new TimeUtils(this.datatypeFactory,timezone,this.locale,this.defaults);
	}

	public TimeUtils withLocale(Locale locale) {
		return new TimeUtils(this.datatypeFactory,this.timezone,locale,this.defaults);
	}

	public TimeUtils withDefaults(XMLGregorianCalendar defaults) {
		return new TimeUtils(this.datatypeFactory,this.timezone,this.locale,defaults);
	}

	public DateTimeConverter from(DateTime literal) {
		checkNotNull(literal,"Literal cannot be null");
		return new DateTimeConverter(literal);
	}

	public DurationConverter from(Duration literal) {
		checkNotNull(literal,"Literal cannot be null");
		return new DurationConverter(literal);
	}

	public static TimeUtils newInstance() {
		try {
			return new TimeUtils(
				DatatypeFactory.newInstance(),
				TimeZone.getDefault(),
				Locale.getDefault(),
				null);
		} catch (DatatypeConfigurationException e) {
			throw new AssertionError("Could not create factory",e);
		}
	}

}
