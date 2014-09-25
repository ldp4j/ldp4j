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
package org.ldp4j.server.xml.converters;

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

/**
 * JAXB 2.0 compliant converter to transforming between {@code String} and
 * {@code java.util.Date} instances.
 * 
 * @since 1.0.0
 * @version 1.0
 * @author Miguel Esteban Guti&eacute;rrez
 * @see <a
 *      href="https://jcp.org/aboutJava/communityprocess/mrel/jsr222/index2.html">JSR
 *      222: JavaTM Architecture for XML Binding (JAXB) 2.0</a>
 */
public final class DateTimeConverter {

	private DateTimeConverter() {
		// Just to avoid the instantiation of the class
	}

	/**
	 * Convert an {@code xs:date} string to a {@code java.util.Date} instance.
	 * 
	 * @param s
	 *            the string to parse
	 * @return The date instance.
	 */
	public static Date parseDate(String s) {
		return DatatypeConverter.parseDate(s).getTime();
	}

	/**
	 * Convert a {@code java.util.Date} instance to an {@code xs:date} string to .
	 * 
	 * @param s
	 *            the date to transform.
	 * @return The string representation for the date.
	 */
	public static String printDate(Date dt) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		return DatatypeConverter.printDate(c);
	}

	/**
	 * Convert an {@code xs:time} string to a {@code java.util.Date} instance.
	 * 
	 * @param s
	 *            the string to parse
	 * @return The date instance.
	 */
	public static Date parseTime(String s) {
		return DatatypeConverter.parseTime(s).getTime();
	}

	/**
	 * Convert a {@code java.util.Date} instance to an {@code xs:time} string to .
	 * 
	 * @param s
	 *            the date to transform.
	 * @return The string representation for the date.
	 */
	public static String printTime(Date dt) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		return DatatypeConverter.printTime(c);
	}

	/**
	 * Convert an {@code xs:dateTime} string to a {@code java.util.Date}
	 * instance.
	 * 
	 * @param s
	 *            the string to parse
	 * @return The date instance.
	 */
	public static Date parseDateTime(String s) {
		return DatatypeConverter.parseDateTime(s).getTime();
	}

	/**
	 * Convert a {@code java.util.Date} instance to an {@code xs:dateTime} string to .
	 * 
	 * @param s
	 *            the date to transform.
	 * @return The string representation for the date.
	 */
	public static String printDateTime(Date dt) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		return DatatypeConverter.printDateTime(c);
	}

}