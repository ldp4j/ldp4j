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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static com.google.common.base.Preconditions.checkArgument;

import org.ldp4j.http.Weighted.Parser;

import com.google.common.base.Strings;

final class ContentNegotiationUtils {

	private static final class MediaTypeParser implements Parser<MediaType> {

		private static final String WITNESS = "header";

		@Override
		public MediaType parse(String before, String after) {
			validateSubstringAfterQualityDefinition(after);
			return MediaTypes.fromString(before);
		}

		private void validateSubstringAfterQualityDefinition(String data) {
			if(data!=null) {
				final HeaderPartIterator it=HeaderPartIterator.create(WITNESS+data);
				// Consume the witness
				it.next();
				// If there is not valid next, we have a failure before the optional extension parameter...
				checkArgument(!it.hasFailure(),"Invalid content before extension parameter: %s (%s)",it.failure(),data);
				// ... but if there is no next at all, then no optional extension parameter is available
				if(it.hasNext()) {
					validateExtensionParameter(data, it.next());
					// Anything left to parse is invalid...
					checkArgument(!it.hasFailure() && !it.hasNext(),"Invalid content after extension parameter [%s] (%s)",it.header().substring(it.endsAt()),data);
				}
			}
		}

		private void validateExtensionParameter(String data, String extensionParameter) {
			try {
				Parameter.fromString(extensionParameter);
			} catch(IllegalArgumentException e) {
				throw new IllegalArgumentException("Invalid extension parameter ["+extensionParameter+"] ("+data+")",e);
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

	private static final class CharsetParser implements Parser<CharacterEncoding> {

		@Override
		public CharacterEncoding parse(final String before, final String after) {
			checkArgument(Strings.isNullOrEmpty(after),"Content after quality definition is not allowed (%s)",after);
			return CharacterEncodings.fromString(before);
		}

	}

	private ContentNegotiationUtils() {
	}

	static Weighted<MediaType> accept(final String header) {
		return Weighted.fromString(header, new MediaTypeParser());
	}

	static Weighted<CharacterEncoding> acceptCharset(final String header) {
		return Weighted.fromString(header, new CharsetParser());
	}

	static Weighted<Language> acceptLanguage(final String header) {
		return Weighted.fromString(header,new LanguageParser());
	}

}
