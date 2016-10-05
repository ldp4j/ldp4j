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

import java.io.Serializable;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.ISOPeriodFormat;

import com.google.common.collect.ImmutableMap;

public final class Literals {

	public abstract static class DateTimeLiteralBuilder {

		private DateTimeLiteralBuilder() {
		}

		public final DateTimeLiteral dateTime() {
			return new ImmutableDateTimeLiteral(getDateTime(),Datatypes.DATE_TIME);
		}

		public final DateTimeLiteral date() {
			return new ImmutableDateTimeLiteral(getDateTime(),Datatypes.DATE);
		}

		public final DateTimeLiteral time() {
			return new ImmutableDateTimeLiteral(getDateTime(),Datatypes.TIME);
		}

		public final DateTimeLiteral year() {
			return new ImmutableDateTimeLiteral(getDateTime(),Datatypes.GYEAR);
		}

		public final DateTimeLiteral month() {
			return new ImmutableDateTimeLiteral(getDateTime(),Datatypes.GMONTH);
		}

		public final DateTimeLiteral day() {
			return new ImmutableDateTimeLiteral(getDateTime(),Datatypes.GDAY);
		}

		public final DateTimeLiteral yearAndMonth() {
			return new ImmutableDateTimeLiteral(getDateTime(),Datatypes.GYEARMONTH);
		}

		public final DateTimeLiteral monthAndDay() {
			return new ImmutableDateTimeLiteral(getDateTime(),Datatypes.GMONTHDAY);
		}

		protected abstract DateTime getDateTime();

	}

	public static final class DateTimeDateTimeLiteralBuilder extends DateTimeLiteralBuilder {

		private final DateTime dateTime;

		private DateTimeDateTimeLiteralBuilder(DateTime dateTime) {
			super();
			this.dateTime = dateTime;
		}

		@Override
		protected DateTime getDateTime() {
			return dateTime;
		}

	}

	public static final class XMLGregorianCalendarDateTimeLiteralBuilder extends DateTimeLiteralBuilder {

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
			return normalizeChronology(new DateTime(this.calendar.toGregorianCalendar(this.timezone, this.locale, this.defaults)));
		}

	}

	private interface DateTimeLiteralBuilderFactory {

		DateTimeLiteralBuilder createBuilder(Object value, URI datatype);

	}

	private interface DateTimeLiteralBuilderAdapter {

		DateTimeLiteral adapt(DateTimeLiteralBuilder builder);

	}

	private static final ImmutableMap<Class<?>,DateTimeLiteralBuilderFactory> FACTORIES=
		ImmutableMap.
			<Class<?>,DateTimeLiteralBuilderFactory>builder().
				put(
					Date.class,
					new DateTimeLiteralBuilderFactory() {
						@Override
						public DateTimeLiteralBuilder createBuilder(Object value, URI datatype) {
							return of((Date)value);
						}
					}
				).
				put(
					GregorianCalendar.class,
					new DateTimeLiteralBuilderFactory() {
						@Override
						public DateTimeLiteralBuilder createBuilder(Object value, URI datatype) {
							return of((GregorianCalendar)value);
						}
					}
				).
				put(
					Calendar.class,
					new DateTimeLiteralBuilderFactory() {
						@Override
						public DateTimeLiteralBuilder createBuilder(Object value, URI datatype) {
							return of((Calendar)value);
						}
					}
				).
				put(
					DateTime.class,
					new DateTimeLiteralBuilderFactory() {
						@Override
						public DateTimeLiteralBuilder createBuilder(Object value, URI datatype) {
							return of((DateTime)value);
						}
					}
				).
				put(
					XMLGregorianCalendar.class,
					new DateTimeLiteralBuilderFactory() {
						@Override
						public DateTimeLiteralBuilder createBuilder(Object value, URI datatype) {
							return of((XMLGregorianCalendar)value);
						}
					}
				).
				put(
					String.class,
					new DateTimeLiteralBuilderFactory() {
						@Override
						public DateTimeLiteralBuilder createBuilder(Object value, URI datatype) {
							try {
								return of(new DateTime(value));
							} catch (Exception e) {
								throw new DatatypeCohercionException(value,datatype,e);
							}
						}
					}
				).
				build();

	private static final ImmutableMap<URI,DateTimeLiteralBuilderAdapter> ADAPTERS=
		ImmutableMap.
			<URI,DateTimeLiteralBuilderAdapter>builder().
				put(
					Datatypes.DATE_TIME,
					new DateTimeLiteralBuilderAdapter() {
						@Override
						public DateTimeLiteral adapt(DateTimeLiteralBuilder builder) {
							return builder.dateTime();
						}
					}
				).
				put(
					Datatypes.DATE,
					new DateTimeLiteralBuilderAdapter() {
						@Override
						public DateTimeLiteral adapt(DateTimeLiteralBuilder builder) {
							return builder.date();
						}
					}
				).
				put(
					Datatypes.TIME,
					new DateTimeLiteralBuilderAdapter() {
						@Override
						public DateTimeLiteral adapt(DateTimeLiteralBuilder builder) {
							return builder.time();
						}
					}
				).
				put(
					Datatypes.GYEAR,
					new DateTimeLiteralBuilderAdapter() {
						@Override
						public DateTimeLiteral adapt(DateTimeLiteralBuilder builder) {
							return builder.year();
						}
					}
				).
				put(
					Datatypes.GMONTH,
					new DateTimeLiteralBuilderAdapter() {
						@Override
						public DateTimeLiteral adapt(DateTimeLiteralBuilder builder) {
							return builder.month();
						}
					}
				).
				put(
					Datatypes.GDAY,
					new DateTimeLiteralBuilderAdapter() {
						@Override
						public DateTimeLiteral adapt(DateTimeLiteralBuilder builder) {
							return builder.day();
						}
					}
				).
				put(
					Datatypes.GYEARMONTH,
					new DateTimeLiteralBuilderAdapter() {
						@Override
						public DateTimeLiteral adapt(DateTimeLiteralBuilder builder) {
							return builder.yearAndMonth();
						}
					}
				).
				put(
					Datatypes.GMONTHDAY,
					new DateTimeLiteralBuilderAdapter() {
						@Override
						public DateTimeLiteral adapt(DateTimeLiteralBuilder builder) {
							return builder.monthAndDay();
						}
					}
				).
				build();

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

	private static DateTime normalizeChronology(DateTime dateTime) {
		return dateTime.withChronology(ISOChronology.getInstance());
	}

	private static <T> DurationLiteral coherceDuration(T value) {
		DurationLiteral duration=null;
		if(value instanceof Duration) {
			duration=of((Duration)value);
		} else if(value instanceof javax.xml.datatype.Duration) {
			duration=of((javax.xml.datatype.Duration)value);
		} else if(value instanceof String) {
			try {
				Period period = ISOPeriodFormat.standard().parsePeriod((String)value);
				duration=of(period.toStandardDuration());
			} catch (Exception e) {
				throw new DatatypeCohercionException(value,Datatypes.DURATION,e);
			}
		} else {
			throw new DatatypeCohercionException(value,Datatypes.DURATION);
		}
		return duration;
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
		return Literals.ADAPTERS.get(datatype).adapt(getBuilder(value,datatype));
	}

	private static DateTimeLiteralBuilder getBuilder(Object value, URI datatype) {
		for(Entry<Class<?>,DateTimeLiteralBuilderFactory> entry:Literals.FACTORIES.entrySet()) {
			if(entry.getKey().isInstance(value)) {
				return entry.getValue().createBuilder(value, datatype);
			}
		}
		throw new DatatypeCohercionException(value,datatype);
	}

	public static Literal<String> of(String value) {
		checkNotNull(value,STRING_CANNOT_BE_NULL);
		return new ImmutableLiteral<String>(value);
	}

	public static DurationLiteral of(javax.xml.datatype.Duration duration) {
		checkNotNull(duration,DURATION_CANNOT_BE_NULL);
		Period period = ISOPeriodFormat.standard().parsePeriod(duration.toString());
		return new ImmutableDurationLiteral(period.toStandardDuration(),Datatypes.DURATION);
	}

	public static DurationLiteral of(Duration duration) {
		checkNotNull(duration,DURATION_CANNOT_BE_NULL);
		return new ImmutableDurationLiteral(duration,Datatypes.DURATION);
	}

	public static DateTimeDateTimeLiteralBuilder of(Date date) {
		checkNotNull(date,DATE_CANNOT_BE_NULL);
		return new DateTimeDateTimeLiteralBuilder(new DateTime(date));
	}

	public static DateTimeLiteral of(java.sql.Date date) {
		checkNotNull(date,DATE_CANNOT_BE_NULL);
		return new ImmutableDateTimeLiteral(new DateTime(date), Datatypes.DATE);
	}

	public static DateTimeLiteral of(java.sql.Time time) {
		checkNotNull(time,TIME_CANNOT_BE_NULL);
		return new ImmutableDateTimeLiteral(new DateTime(time.getTime()), Datatypes.TIME);
	}

	public static DateTimeDateTimeLiteralBuilder of(Calendar calendar) {
		checkNotNull(calendar,CALENDAR_CANNOT_BE_NULL);
		return new DateTimeDateTimeLiteralBuilder(normalizeChronology(new DateTime(calendar)));
	}

	public static DateTimeDateTimeLiteralBuilder of(GregorianCalendar calendar) {
		checkNotNull(calendar,CALENDAR_CANNOT_BE_NULL);
		return new DateTimeDateTimeLiteralBuilder(normalizeChronology(new DateTime(calendar)));
	}

	public static XMLGregorianCalendarDateTimeLiteralBuilder of(XMLGregorianCalendar calendar) {
		checkNotNull(calendar,CALENDAR_CANNOT_BE_NULL);
		return new XMLGregorianCalendarDateTimeLiteralBuilder(calendar);
	}

	public static DateTimeDateTimeLiteralBuilder of(DateTime dateTime) {
		checkNotNull(dateTime,DATE_TIME_CANNOT_BE_NULL);
		return new DateTimeDateTimeLiteralBuilder(dateTime);
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
			result=coherceDateTime(value,Datatypes.DATE_TIME);
		} else if(value instanceof Serializable) {
			result=new ImmutableLiteral<Serializable>((Serializable)value);
		} else {
			result=new ImmutableTypedLiteral<String>(value.toString(),Datatypes.STRING);
		}
		return result;
	}

	public static TypedLiteral<? extends Serializable> newTypedLiteral(Object value, URI datatype) {
		checkNotNull(value,LITERAL_VALUE_CANNOT_BE_NULL);
		checkNotNull(datatype,DATATYPE_CANNOT_BE_NULL);
		TypedLiteral<? extends Serializable> result=null;
		if(Datatypes.isDuration(datatype)) {
			result=coherceDuration(value);
		} else if(Datatypes.isTemporal(datatype)) {
			result=coherceDateTime(value, datatype);
		} else if(value instanceof Serializable){
			result=new ImmutableTypedLiteral<Serializable>((Serializable)value,datatype);
		} else {
			result=new ImmutableTypedLiteral<String>(value.toString(),Datatypes.STRING);
		}
		return result;
	}

	public static LanguageLiteral newLanguageLiteral(String value, String language) {
		checkNotNull(value,STRING_CANNOT_BE_NULL);
		checkNotNull(language,LANGUAGE_CANNOT_BE_NULL);
		return new ImmutableLanguageLiteral(value,language);
	}

}
