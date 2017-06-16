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
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.BitSet;

import com.google.common.base.Strings;

final class HttpUtils {

	private static final char   PARAM_DELIMITER = ';';
	private static final char   DQUOTE          = '\"';

	private static final BitSet TOKEN;

	static {
		// ASCII Control codes
		final BitSet ctl = new BitSet(128);
		ctl.set(0x00,0x20);
		ctl.set(0x7F);

		// SP, DQUOTE and "(),/:;<=>?@[\]{}"
		final BitSet delimiters = new BitSet(128);
		delimiters.set(' ');
		delimiters.set('\"');
		delimiters.set('(');
		delimiters.set(')');
		delimiters.set(',');
		delimiters.set('/');
		delimiters.set(':');
		delimiters.set(';');
		delimiters.set('<');
		delimiters.set('=');
		delimiters.set('>');
		delimiters.set('?');
		delimiters.set('@');
		delimiters.set('[');
		delimiters.set('\\');
		delimiters.set(']');
		delimiters.set('{');
		delimiters.set('}');

		TOKEN = new BitSet(0x80);
		TOKEN.set(0,0x80);
		TOKEN.andNot(ctl);
		TOKEN.andNot(delimiters);
	}

	private HttpUtils() {
	}

	static String trimWhitespace(final String token) {
		checkNotNull(token,"Token cannot be null");
		int startOffset=0;
		int endOffset=token.length();
		while(startOffset<endOffset) {
			final char lastChar=token.charAt(startOffset);
			if(!HttpUtils.isWhitespace(lastChar)) {
				break;
			}
			startOffset++;
		}
		while(endOffset>startOffset) {
			final char lastChar=token.charAt(endOffset-1);
			if(!HttpUtils.isWhitespace(lastChar)) {
				break;
			}
			endOffset--;
		}
		return token.substring(startOffset,endOffset);
	}

	static boolean isQuotedString(final String str) {
		boolean result=false;
		final int length=str.length();
		if(length>1) {
			result=
				str.charAt(0)       ==DQUOTE &&
				str.charAt(length-1)==DQUOTE;
		}
		return result;
	}

	static String unquote(final String str) {
		return
			isQuotedString(str) ?
				str.substring(1, str.length() - 1) :
				str;
	}

	static boolean isWhitespace(final char ch) {
		return ch==' ' || ch=='\t';
	}

	static boolean isParameterDelimiter(final char ch) {
		return ch==PARAM_DELIMITER;
	}

	/**
	 * Checks the given type name for illegal characters, as defined in RFC
	 * 7230, section 3.2.6.
	 *
	 * @param token
	 *            the string to validate
	 * @throws IllegalArgumentException
	 *             in case of illegal characters
	 * @see <a href="http://tools.ietf.org/html/rfc7230#section-3.2.6">Hypertext
	 *      Transfer Protocol (HTTP/1.1): Message Syntax and Routing, Section
	 *      3.2.6</a>
	 */
	static void checkToken(final String token, final String message, Object... args) {
		checkNotNull(message,"Message cannot be null");
		try {
			validateLength(token);
			validateCharacters(token);
		} catch (IllegalArgumentException e) {
			throw new InvalidTokenException(String.format(message,args),token,e);
		}
	}

	static void checkToken(final String token) {
		checkToken(token,"Invalid token");
	}

	static Double checkQuality(final Double quality, final String parameter) {
		if(quality==null) {
			return null;
		}
		checkQualityRange(quality, parameter);
		checkArgument(DoubleUtils.hasPrecision(quality,3),"%s cannot have more than 3 decimals (%s)",parameter,quality);
		return quality;
	}

	static void checkQualityRange(final Double quality, final String parameter) {
		checkArgument(quality>=0.0D,"%s cannot be negative (%s)",parameter,quality);
		checkArgument(quality<=1.0D,"%s cannot be greater than 1 (%s)",parameter,quality);
	}

	private static void validateCharacters(final String token) {
		for(int i=0;i<token.length();i++) {
			final char ch=token.charAt(i);
			checkArgument(TOKEN.get(ch),"Invalid character '%s' in token '%s' at %s",ch,token,i);
		}
	}

	private static void validateLength(final String token) {
		checkArgument(!Strings.isNullOrEmpty(token),"Token cannot be empty");
	}

}
