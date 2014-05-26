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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client.impl.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class HttpUtil {
	
	private HttpUtil() {
	}
	
	private static SimpleDateFormat getCxfHttpDateFormat() {
		SimpleDateFormat dateFormat = 
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
		TimeZone tZone = TimeZone.getTimeZone("GMT");
		dateFormat.setTimeZone(tZone);
		return dateFormat;
	}

	private static DateFormat getISOHttpDateFormat() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		TimeZone tZone = TimeZone.getTimeZone("GMT");
		dateFormat.setTimeZone(tZone);
		return dateFormat;
	}

	public static Date getHttpDate(String value) {
		if (value == null) {
			return null;
		}
		Date result=null;
		try {
			result=getCxfHttpDateFormat().parse(value);
		} catch (ParseException ex) {
			try {
				result=getISOHttpDateFormat().parse(value);
			} catch (ParseException e) {
				// Any caveat?
			}
		}
		return result;
	}

	public static Locale getLocale(String value) {
		if (value == null) {
			return null;
		}

		String[] values = value.split("-");
		if (values.length == 0 || values.length > 2) {
			throw new IllegalArgumentException("Illegal locale value : "
					+ value);
		}
		if (values.length == 1) {
			return new Locale(values[0]);
		} else {
			return new Locale(values[0], values[1]);
		}

	}

}