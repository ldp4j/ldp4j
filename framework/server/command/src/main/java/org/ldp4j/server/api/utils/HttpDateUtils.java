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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class HttpDateUtils {

	private static final Locale DEFAULT_LOCALE = Locale.US;
	@SuppressWarnings("unused")
	private static final String DEFAULT_LOCALE_STR = "US";
	private static final String DEFAULT_TIME_ZONE = "GMT";

	private static final String RFC_822_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	private static final String RFC_850_DATE_FORMAT = "EEEE, dd-MMM-yy HH:mm:ss zzz";
	private static final String ANSI_C_DATE_FORMAT = "EEE MMM d HH:mm:ss yyyy";
	private static final String[] KNOWN_FORMATS = new String[]{RFC_822_DATE_FORMAT,RFC_850_DATE_FORMAT,ANSI_C_DATE_FORMAT};

	private HttpDateUtils() {
	}
	
	/**
	 * Create a {@code SimpleDateFormat} instance given the specified template,
	 * locale, and time zone.
	 * 
	 * @param dataFormat
	 *            the template to be used
	 * @param defaultLocale
	 *            the local to be used
	 * @param defaultTimeZone
	 *            the time zone to be used
	 * @return A configured {@code SimpleDataFormat} instance.
	 * @see {@link java.text.SimpleDateFormat}
	 */
	private static SimpleDateFormat getHttpDateFormat(String dataFormat, Locale defaultLocale, String defaultTimeZone) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(dataFormat,defaultLocale);
		TimeZone tZone = TimeZone.getTimeZone(defaultTimeZone);
		dateFormat.setTimeZone(tZone);
		dateFormat.setLenient(false);
		return dateFormat;
	}

	/**
	 * Get a date format template for managing the conversion of dates as
	 * required by HTTP/1.1. The template uses {@value #DEFAULT_LOCALE_STR} as locale 
	 * and {@value #DEFAULT_TIME_ZONE} as time-zone.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.4.3">RFC 2616 &mdash; Hypertext Transfer Protocol -- HTTP/1.1, 19.4.3 Conversion of Date Formats</a>
	 * @see <a
	 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1">RFC 2616 &mdash; Hypertext Transfer Protocol -- HTTP/1.1, 3.3.1 Full Date</a>
	 */
	private static SimpleDateFormat getHttpDateFormat() {
		return getHttpDateFormat(RFC_822_DATE_FORMAT, DEFAULT_LOCALE, DEFAULT_TIME_ZONE);
	}

	private static <E extends Exception> Date parse(String value, Date defaultDate, E exception) throws E {
		Date result = validateInput(value, defaultDate, exception);
		if(result==null) {
			List<ParseException> parsingExceptions=new ArrayList<ParseException>();
			for(int i=0;i<KNOWN_FORMATS.length && result==null ;i++) {
				try {
					result=HttpDateUtils.getHttpDateFormat(KNOWN_FORMATS[i],DEFAULT_LOCALE,DEFAULT_TIME_ZONE).parse(value);
				} catch (ParseException e) {
					parsingExceptions.add(e);
				}
			}
			result=validateParsingResult(result, parsingExceptions, defaultDate, exception);
		}
		return result;
	}

	private static <E extends Exception> Date validateParsingResult(Date result, List<ParseException> exceptions, Date defaultDate, E defaultException) throws E {
		Date tmpResult=result;
		if(tmpResult==null) {
			if(defaultDate!=null) {
				tmpResult=defaultDate;
			} else if(defaultException!=null) {
				defaultException.initCause(exceptions.get(0));
				throw defaultException;
			} else {
				throw new UnknownHttpDateFormatException("Could not parse raw date",exceptions.get(0));
			}
		}
		return tmpResult;
	}

	private static <E extends Exception> Date validateInput(String value, Date defaultDate, E exception) throws E {
		Date result=null;
		if(value == null) {
			if(defaultDate != null) {
				result=defaultDate;
			} else if(exception!=null) {
				throw exception;
			} else {
				throw new IllegalArgumentException("Object 'value' cannot be null if neither 'defaultDate' nor 'exception' are defined");
			}
		}
		return result;
	}

	public static String format(Date value) {
		if(value==null) {
			throw new IllegalArgumentException("Object 'value' cannot be null");
		}
		return HttpDateUtils.getHttpDateFormat().format(value);
	}

	/**
	 * Get a {@code java.util.Date} instance for a properly formated date.
	 * 
	 * @param value
	 *            the date to be parsed
	 * @return A {@code Date} instance for the specified {@code value}
	 * @throws UnknownHttpDateFormatException
	 *             if the specified {@code value} is not formated according any
	 *             of the valid HTTP date formats.
	 */
	public static Date parse(String value) {
		return parse(value,new UnknownHttpDateFormatException("Invalid HTTP date"));
	}

	/**
	 * Get a {@code java.util.Date} instance for a properly formated date. If
	 * the parsing fails, return a default date.
	 * 
	 * @param value
	 *            the date to be parsed
	 * @param defaultDate
	 *            the date to be returned if the parsing fails
	 * @return A {@code Date} instance for the specified {@code value} or the
	 *         {@code defaultDate} in case of failure.
	 * @throws UnknownHttpDateFormatException
	 *             if the specified {@code value} is not formated according any
	 *             of the valid HTTP date formats and no {@code defaultDate} is provided.
	 */
	public static Date parse(String value, Date defaultDate) {
		return parse(value,defaultDate,new UnknownHttpDateFormatException("Invalid HTTP date"));
	}

	/**
	 * Get a {@code java.util.Date} instance for a properly formated date. If
	 * the parsing fails, throw a default exception.
	 * 
	 * @param value
	 *            the date to be parsed.
	 * @param exception
	 *            the business specific exception to be thrown in case of
	 *            failure.
	 * @return A {@code Date} instance for the specified {@code value}.
	 * 
	 * @throws E
	 *             if the specified {@code value} is not formated according any
	 *             of the valid HTTP date formats.
	 */
	public static <E extends Exception> Date parse(String value, E exception) throws E {
		return parse(value,null,exception);
	}

}
