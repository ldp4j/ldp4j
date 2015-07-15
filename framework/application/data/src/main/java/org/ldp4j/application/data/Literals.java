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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-data-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import static com.google.common.base.Preconditions.*;

import java.io.Serializable;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public final class Literals {

	private static final String DATATYPE_CANNOT_BE_NULL = "Datatype cannot be null";
	private static final String LANGUAGE_CANNOT_BE_NULL = "Language cannot be null";
	private static final String STRING_CANNOT_BE_NULL = "String cannot be null";
	private static final String TIME_UNIT_CANNOT_BE_NULL = "Time unit cannot be null";
	private static final String DATE_TIME_CANNOT_BE_NULL = "Date-time cannot be null";
	private static final String LITERAL_VALUE_CANNOT_BE_NULL = "Literal value cannot be null";
	private static final String CALENDAR_CANNOT_BE_NULL = "Calendar cannot be null";
	private static final String TIME_CANNOT_BE_NULL = "Time cannot be null";
	private static final String DATE_CANNOT_BE_NULL = "Date cannot be null";
	private static final String DURATION_CANNOT_BE_NULL = "Duration cannot be null";

	public abstract static class AbstractDateTimeLiteralBuilder {

		private AbstractDateTimeLiteralBuilder() {
		}

		public final DateTimeLiteral dateTime() {
			return new ImmutableDateTimeLiteral(getDateTime(),Literals.DATE_TIME);
		}

		public final DateTimeLiteral date() {
			return new ImmutableDateTimeLiteral(getDateTime(),Literals.DATE);
		}

		public final DateTimeLiteral time() {
			return new ImmutableDateTimeLiteral(getDateTime(),Literals.TIME);
		}

		public final DateTimeLiteral year() {
			return new ImmutableDateTimeLiteral(getDateTime(),Literals.GYEAR);
		}

		public final DateTimeLiteral month() {
			return new ImmutableDateTimeLiteral(getDateTime(),Literals.GMONTH);
		}

		public final DateTimeLiteral day() {
			return new ImmutableDateTimeLiteral(getDateTime(),Literals.GDAY);
		}

		public final DateTimeLiteral yearAndMonth() {
			return new ImmutableDateTimeLiteral(getDateTime(),Literals.GYEARMONTH);
		}

		public final DateTimeLiteral monthAndDay() {
			return new ImmutableDateTimeLiteral(getDateTime(),Literals.GMONTHDAY);
		}

		protected abstract DateTime getDateTime();

	}

	public static final class DateTimeLiteralBuilder extends AbstractDateTimeLiteralBuilder {

		private final DateTime dateTime;

		private DateTimeLiteralBuilder(DateTime dateTime) {
			super();
			this.dateTime = dateTime;
		}

		@Override
		protected DateTime getDateTime() {
			return dateTime;
		}

	}

	public static final class XMLGregorianCalendarDateTimeLiteralBuilder extends AbstractDateTimeLiteralBuilder {

		private final XMLGregorianCalendar calendar;

		private TimeZone timezone;
		private Locale locale;
		private XMLGregorianCalendar defaults;

		private XMLGregorianCalendarDateTimeLiteralBuilder(XMLGregorianCalendar calendar) {
			super();
			this.calendar = calendar;
		}

		public XMLGregorianCalendarDateTimeLiteralBuilder withTimeZone(TimeZone timezone) {
			this.timezone = timezone;
			return this;
		}

		public XMLGregorianCalendarDateTimeLiteralBuilder withLocale(Locale locale) {
			this.locale = locale;
			return this;
		}

		public XMLGregorianCalendarDateTimeLiteralBuilder withDefaults(XMLGregorianCalendar defaults) {
			this.defaults = defaults;
			return this;
		}

		@Override
		protected DateTime getDateTime() {
			return new DateTime(this.calendar.toGregorianCalendar(this.timezone, this.locale, this.defaults));
		}

	}

	private static final URI STRING = URI.create("http://www.w3.org/2001/XMLSchema#string");
	private static final URI DATE_TIME=toURI(DatatypeConstants.DATETIME);
	private static final URI DATE=toURI(DatatypeConstants.DATE);
	private static final URI TIME=toURI(DatatypeConstants.TIME);
	private static final URI GYEAR=toURI(DatatypeConstants.GYEAR);
	private static final URI GMONTH=toURI(DatatypeConstants.GMONTH);
	private static final URI GDAY=toURI(DatatypeConstants.GDAY);
	private static final URI GYEARMONTH=toURI(DatatypeConstants.GYEARMONTH);
	private static final URI GMONTHDAY=toURI(DatatypeConstants.GMONTHDAY);
	private static final URI DURATION=toURI(DatatypeConstants.DURATION);

	private static final Class<?>[] DATE_TIME_CLASSES={
		java.sql.Date.class,
		Date.class,
		java.sql.Time.class,
		DateTime.class,
		GregorianCalendar.class,
		Calendar.class,
		XMLGregorianCalendar.class
	};
	private static final Class<?>[] DURATION_CLASSES={
		Duration.class,
		javax.xml.datatype.Duration.class
	};

	private Literals() {
	}

	private static URI toURI(QName qName) {
		return URI.create(qName.getNamespaceURI()+"#"+qName.getLocalPart());
	}

	private static boolean isDurationDatetype(URI datatype) {
		return Literals.DURATION.equals(datatype);
	}

	private static boolean isTemporalDatatype(URI datatype) {
		return
			Literals.DATE_TIME.equals(datatype) ||
			Literals.DATE.equals(datatype) ||
			Literals.TIME.equals(datatype) ||
			Literals.GYEAR.equals(datatype) ||
			Literals.GMONTH.equals(datatype) ||
			Literals.GDAY.equals(datatype) ||
			Literals.GYEARMONTH.equals(datatype) ||
			Literals.GMONTHDAY.equals(datatype);
	}

	private static boolean isDateTime(Object obj) {
		return isInstanceOf(obj,Literals.DATE_TIME_CLASSES);
	}

	private static boolean isDuration(Object obj) {
		return isInstanceOf(obj,Literals.DURATION_CLASSES);
	}

	private static boolean isInstanceOf(Object obj, Class<?>[] classes) {
		for(Class<?> clazz:classes) {
			if(clazz.isInstance(obj)) {
				return true;
			}
		}
		return false;
	}

	private static DateTimeLiteral coherceDateTime(Object value, URI datatype) throws AssertionError {
		DateTimeLiteral dateTime=null;
		if(value instanceof java.sql.Date) {
			dateTime=of((java.sql.Date)value);
		} else if(value instanceof java.sql.Time) {
			dateTime=of((java.sql.Time)value);
		} else {
			dateTime=coherceVariableDateTime(value,datatype);
		}
		return dateTime;
	}

	private static DateTimeLiteral coherceVariableDateTime(Object value, URI datatype) {
		if(Literals.DATE_TIME.equals(datatype)) {
			return getBuilder(value,datatype).dateTime();
		} else if(Literals.DATE.equals(datatype)) {
			return getBuilder(value,datatype).date();
		} else if(Literals.TIME.equals(datatype)) {
			return getBuilder(value,datatype).time();
		} else if(Literals.GYEAR.equals(datatype)) {
			return getBuilder(value,datatype).year();
		} else if(Literals.GMONTH.equals(datatype)) {
			return getBuilder(value,datatype).month();
		} else if(Literals.GDAY.equals(datatype)) {
			return getBuilder(value,datatype).day();
		} else if(Literals.GYEARMONTH.equals(datatype)) {
			return getBuilder(value,datatype).yearAndMonth();
		} else if(Literals.GMONTHDAY.equals(datatype)) {
			return getBuilder(value,datatype).monthAndDay();
		}
		throw new AssertionError(datatype.toString()+" is not a date-time compatible datype");
	}

	private static AbstractDateTimeLiteralBuilder getBuilder(Object value, URI datatype) {
		if(value instanceof Date) {
			return of((Date)value);
		} else if(value instanceof Calendar) {
			return of((Calendar)value);
		} else if(value instanceof GregorianCalendar) {
			return of((GregorianCalendar)value);
		} else if(value instanceof DateTime) {
			return of((DateTime)value);
		} else if(value instanceof javax.xml.datatype.XMLGregorianCalendar) {
			return of((javax.xml.datatype.XMLGregorianCalendar)value);
		} else if(value instanceof String) {
			try {
				return of(new DateTime(value));
			} catch (Exception e) {
				throw new DatatypeCohercionException(value,datatype,e);
			}
		} else {
			throw new DatatypeCohercionException(value,datatype);
		}
	}

	private static <T> DurationLiteral coherceDuration(T value) {
		DurationLiteral duration=null;
		if(value instanceof Duration) {
			duration=of((Duration)value);
		} else if(value instanceof javax.xml.datatype.Duration) {
			duration=of((javax.xml.datatype.Duration)value);
		} else if(value instanceof String) {
			duration=of(new Duration(value));
		} else {
			throw new DatatypeCohercionException(value,Literals.DURATION);
		}
		return duration;
	}

	public static Literal<String> of(String value) {
		checkNotNull(value,STRING_CANNOT_BE_NULL);
		return new ImmutableLiteral<String>(value);
	}

	public static DurationLiteral of(javax.xml.datatype.Duration duration) {
		checkNotNull(duration,DURATION_CANNOT_BE_NULL);
		return new ImmutableDurationLiteral(new Duration(duration.toString()),Literals.DURATION);
	}

	public static DurationLiteral of(Duration duration) {
		checkNotNull(duration,DURATION_CANNOT_BE_NULL);
		return new ImmutableDurationLiteral(duration,Literals.DURATION);
	}

	public static DateTimeLiteralBuilder of(Date date) {
		checkNotNull(date,DATE_CANNOT_BE_NULL);
		return new DateTimeLiteralBuilder(new DateTime(date));
	}

	public static DateTimeLiteral of(java.sql.Date date) {
		checkNotNull(date,DATE_CANNOT_BE_NULL);
		return new ImmutableDateTimeLiteral(new DateTime(date), Literals.DATE);
	}

	public static DateTimeLiteral of(java.sql.Time time) {
		checkNotNull(time,TIME_CANNOT_BE_NULL);
		return new ImmutableDateTimeLiteral(new DateTime(time.getTime()), Literals.TIME);
	}

	public static DateTimeLiteralBuilder of(Calendar calendar) {
		checkNotNull(calendar,CALENDAR_CANNOT_BE_NULL);
		return new DateTimeLiteralBuilder(new DateTime(calendar));
	}

	public static DateTimeLiteralBuilder of(GregorianCalendar calendar) {
		checkNotNull(calendar,CALENDAR_CANNOT_BE_NULL);
		return new DateTimeLiteralBuilder(new DateTime(calendar));
	}

	public static XMLGregorianCalendarDateTimeLiteralBuilder of(XMLGregorianCalendar calendar) {
		checkNotNull(calendar,CALENDAR_CANNOT_BE_NULL);
		return new XMLGregorianCalendarDateTimeLiteralBuilder(calendar);
	}

	public static DateTimeLiteralBuilder of(DateTime dateTime) {
		checkNotNull(dateTime,DATE_TIME_CANNOT_BE_NULL);
		return new DateTimeLiteralBuilder(dateTime);
	}

	public static DurationLiteral duration(long time, TimeUnit unit) {
		checkNotNull(time,TIME_CANNOT_BE_NULL);
		checkNotNull(unit,TIME_UNIT_CANNOT_BE_NULL);
		return of(new Duration(TimeUnit.MILLISECONDS.convert(time, unit)));
	}

	public static Literal<? extends Serializable> newLiteral(Object value) {
		checkNotNull(value,LITERAL_VALUE_CANNOT_BE_NULL);
		Literal<? extends Serializable> result=null;
		if(isDuration(value)) {
			result=coherceDuration(value);
		} else if(isDateTime(value)) {
			result=coherceDateTime(value,Literals.DATE_TIME);
		} else if(value instanceof Serializable) {
			result=new ImmutableLiteral<Serializable>((Serializable)value);
		} else {
			result=new ImmutableTypedLiteral<String>(value.toString(),Literals.STRING);
		}
		return result;
	}

	public static TypedLiteral<? extends Serializable> newTypedLiteral(Object value, URI datatype) {
		checkNotNull(value,LITERAL_VALUE_CANNOT_BE_NULL);
		checkNotNull(datatype,DATATYPE_CANNOT_BE_NULL);
		TypedLiteral<? extends Serializable> result=null;
		if(isDurationDatetype(datatype)) {
			result=coherceDuration(value);
		} else if(isTemporalDatatype(datatype)) {
			result=coherceDateTime(value, datatype);
		} else if(value instanceof Serializable){
			result=new ImmutableTypedLiteral<Serializable>((Serializable)value,datatype);
		} else if(STRING.equals(datatype)) {
			result=new ImmutableTypedLiteral<String>(value.toString(),datatype);
		} else {
			result=new ImmutableTypedLiteral<String>(value.toString(),Literals.STRING);
		}
		return result;
	}

	public static LanguageLiteral newLanguageLiteral(String value, String language) {
		checkNotNull(value,STRING_CANNOT_BE_NULL);
		checkNotNull(language,LANGUAGE_CANNOT_BE_NULL);
		return new ImmutableLanguageLiteral(value,language);
	}

}
