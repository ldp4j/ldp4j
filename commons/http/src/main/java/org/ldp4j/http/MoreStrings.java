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

final class MoreStrings {

	private MoreStrings() {
	}

	/**
	 * Check that the given {@code CharSequence} is neither {@code null} nor of
	 * length 0.
	 * <p>
	 * Note: this method returns {@code true} for a {@code CharSequence} that
	 * purely consists of whitespace.
	 * <p>
	 *
	 * <pre class="code">
	 * MoreStrings.hasLength(null) = false
	 * MoreStrings.hasLength("") = false
	 * MoreStrings.hasLength(" ") = true
	 * MoreStrings.hasLength("Hello") = true
	 * </pre>
	 *
	 * @param cs
	 *            the {@code CharSequence} to check (may be {@code null})
	 * @return {@code true} if the {@code CharSequence} is not {@code null} and
	 *         has length; {@code false} otherwise
	 */
	static boolean hasLength(final CharSequence cs) {
		return (cs != null && cs.length() > 0);
	}

	/**
	 * Check that the given {@code String} is neither {@code null} nor of length
	 * 0.
	 * <p>
	 * Note: this method returns {@code true} for a {@code String} that purely
	 * consists of whitespace.
	 *
	 * @param str
	 *            the {@code String} to check (may be {@code null})
	 * @return {@code true} if the {@code String} is not {@code null} and has
	 *         length
	 * @see #hasLength(CharSequence)
	 */
	static boolean hasLength(final String str) {
		return hasLength((CharSequence) str);
	}

	/**
	 * <p>
	 * Compares two CharSequences, returning {@code true} if they represent
	 * equal sequences of characters, ignoring case.
	 * </p>
	 *
	 * <p>
	 * {@code null}s are handled without exceptions. Two {@code null}
	 * references are considered equal. Comparison is case insensitive.
	 * </p>
	 *
	 * <pre>
	 * equalsIgnoreCase(null, null)   = true
	 * equalsIgnoreCase(null, "abc")  = false
	 * equalsIgnoreCase("abc", null)  = false
	 * equalsIgnoreCase("abc", "abc") = true
	 * equalsIgnoreCase("abc", "ABC") = true
	 * </pre>
	 *
	 * @param str1
	 *            the first CharSequence, may be {@code null}
	 * @param str2
	 *            the second CharSequence, may be {@code null}
	 * @return {@code true} if the CharSequence are equal, case insensitive,
	 *         or both {@code null}; {@code false} otherwise
	 */
	static boolean equalsIgnoreCase(final CharSequence str1, final CharSequence str2) {
		if(str1==str2) {
			return true;
		} else if(str1==null||str2==null) {
			return false;
		} else if(str1.length()!=str2.length()) {
			return false;
		} else {
			return regionMatches(str1,true,0,str2,0,str1.length());
		}
	}

	/**
	 * Green implementation of regionMatches.
	 *
	 * @param target
	 *            the {@code CharSequence} to be processed
	 * @param ignoreCase
	 *            whether or not to be case insensitive
	 * @param offset
	 *            the index to start on the {@code cs} CharSequence
	 * @param other
	 *            the {@code CharSequence} to be looked for
	 * @param start
	 *            the index to start on the {@code substring} CharSequence
	 * @param length
	 *            character length of the region
	 * @return {@code true} if the region matched; {@code false} otherwise
	 * @throws NullPointerException
	 *             if the target or other {@code CharSequence} are {@code null}
	 */
	static boolean regionMatches(
			final CharSequence target,
			final boolean ignoreCase,
			final int offset,
			final CharSequence other,
			final int start,
			final int length) {
		boolean result;
		if (target instanceof String && other instanceof String) {
			result=delegateCheck((String)target,ignoreCase,offset,start,length,(String) other);
		} else {
			result=carryOutCheck(target, ignoreCase, offset, other, start, length);
		}
		return result;
	}

	private static boolean delegateCheck(
			final String target,
			final boolean ignoreCase,
			final int offset,
			final int start,
			final int length,
			final String other) {
		return target.regionMatches(ignoreCase,offset,other,start,length);
	}

	private static boolean carryOutCheck(
			final CharSequence target,
			final boolean ignoreCase,
			final int offset,
			final CharSequence other,
			final int start,
			final int length) {
		int index1=offset;
		int index2=start;
		int tmpLen=length;
		while(tmpLen-->0) {
			final char c1=target.charAt(index1++);
			final char c2=other.charAt(index2++);
			if(c1==c2) {
				continue;
			}
			/**
			 * If characters don't match but case may be ignored...
			 */
			if(ignoreCase) {
				/**
				 * ... try converting both characters to uppercase. If the
				 * results match, then the comparison scan should continue.
				 */
				final char u1=Character.toUpperCase(c1);
				final char u2=Character.toUpperCase(c2);
				if(u1==u2) {
					continue;
				}
				/**
				 * Unfortunately, conversion to uppercase does not work properly
				 * in certain alphabets (e.g., Georgian) that have strange rules
				 * about case conversion, and does not take into consideration
				 * alternative variants in certain UNICODE blocks (e.g., Greek
				 * and Coptic, Latin Extended-A). So we need to make one last
				 * check before exiting.
				 */
				if(Character.toLowerCase(u1)==Character.toLowerCase(u2)) {
					continue;
				}
			}
			return false;
		}
		return true;
	}

}