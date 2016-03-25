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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-http-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.ldp4j.http.Weighted.Parser;

import com.google.common.base.Strings;

final class ContentNegotiation {

	private static final class MediaTypeParser implements Parser<MediaType> {

		private static final String WITNESS = "header";

		@Override
		public MediaType parse(String before, String after) {
			String error = isValidExtensionParameter(after);
			checkArgument(error==null,error);
			return MediaTypes.fromString(before);
		}

		private String isValidExtensionParameter(String data) {
			if(data==null) {
				return null;
			}
			final HeaderPartIterator it=HeaderPartIterator.create(WITNESS+data);
			// Consume the witness
			it.next();
			// If there is not valid next, we have a failure before the optional extension parameter...
			if(it.hasFailure()) {
				return "Invalid content before extension parameter: "+it.failure()+ " ("+data+")";
			}
			// ... but if there is no next at all, then no optional extension parameter is available
			if(!it.hasNext()) {
				return null;
			}
			try {
				final Parameter parameter=Parameter.fromString(it.next());
				// Anything left to parse is invalid...
				if(it.hasFailure() || it.hasNext()) {
					return "Invalid content after extension parameter ["+it.header().substring(it.endsAt())+"] ("+data+")";
				}
				System.err.println("Discarding extension parameter "+parameter);
				return null;
			} catch(IllegalArgumentException e) {
				return e.getMessage()+" ("+data+")";
			}
		}

	}

	private static final class LanguageParser implements Parser<Language> {

		@Override
		public Language parse(final String before, final String after) {
			checkArgument(Strings.isNullOrEmpty(after),"Content after quality definition is not allowed (%s)",after);
			return Languages.fromString(before);
		}

	}

	private static final class CharsetParser implements Parser<Charset> {

		@Override
		public Charset parse(final String before, final String after) {
			checkArgument(Strings.isNullOrEmpty(after),"Content after quality definition is not allowed (%s)",after);
			if("*".equals(before)) {
				return null;
			}
			try {
				return Charset.forName(before);
			} catch (final IllegalCharsetNameException e) {
				throw new IllegalArgumentException("Invalid charset: illegal charset name ('"+before+"')",e);
			} catch (final UnsupportedCharsetException e) {
				throw new IllegalArgumentException("Invalid charset: not supported ('"+before+"')",e);
			}
		}

	}

	private ContentNegotiation() {
	}

	static Weighted<MediaType> accept(final String header) {
		return Weighted.fromString(header, new MediaTypeParser());
	}

	static Weighted<Charset> acceptCharset(final String header) {
		return Weighted.fromString(header, new CharsetParser());
	}

	static Weighted<Language> acceptLanguage(final String header) {
		return Weighted.fromString(header,new LanguageParser());
	}

}
