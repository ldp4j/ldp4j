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
		return cs != null && cs.length() > 0;
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
	 * Tests if two {@link CharSequence charsequence} regions are equal.
	 * <p>
	 * A region of the {@code target} charsequence is compared to a region of
	 * the {@code other} charsequence. The result is {@code true} if these regions
	 * represent character sequences that are the same, ignoring case if and
	 * only if {@code ignoreCase} is true. The region of the {@code target}
	 * charsequence to be compared begins at index {@code offset}
	 * , whereas the region of the {@code other} charsequence begins at index {@code start}. Both
	 * regions have a length {@code length}.
	 * <p>
	 * The result is {@code false} if and only if at least one of the following
	 * is true:
	 * <ul>
	 * <li>{@code toffset} is negative.
	 * <li>{@code ooffset} is negative.
	 * <li>{@code toffset+length} is greater than the length of {@code target}
	 * argument.
	 * <li>{@code ooffset+length} is greater than the length of {@code other}
	 * argument.
	 * <li>{@code ignoreCase} is {@code false} and there is some nonnegative
	 * integer <i>k</i> less than {@code length} such that: <blockquote>
	 *
	 * <pre>
	 * target.charAt(toffset + k) != other.charAt(ooffset + k)
	 * </pre>
	 *
	 * </blockquote>
	 * <li>{@code ignoreCase} is {@code true} and there is some nonnegative
	 * integer <i>k</i> less than {@code length} such that: <blockquote>
	 *
	 * <pre>
	 * let:
	 *   char u1=Character.toUpperCase(target.charAt(toffset + k))
	 *   char u2=Character.toUpperCase(other.charAt(ooffset + k))
	 *
	 * u1! = u2 && Character.toLowerCase(u1) != Character.toLowerCase(u2)
	 * </pre>
	 * </ul>
	 *
	 * @param target
	 *            the first charsequence to compare.
	 * @param ignoreCase
	 *            if {@code true}, ignore case when comparing characters.
	 * @param toffset
	 *            the starting offset of the subregion of the {@code target}
	 *            argument.
	 * @param other
	 *            the second charsequence to compare.
	 * @param ooffset
	 *            the starting offset of the subregion in the {@code other}
	 *            argument.
	 * @param length
	 *            the number of characters to compare.
	 * @return {@code true} if the specified subregion of the {@code target}
	 *         argument matches the specified subregion of the {@code other}
	 *         argument; {@code false} otherwise. Whether the matching is exact
	 *         or case insensitive depends on the {@code ignoreCase} argument.
	 */
	static boolean regionMatches(
			final CharSequence target,
			final boolean ignoreCase,
			final int toffset,
			final CharSequence other,
			final int ooffset,
			final int length) {
		boolean result;
		if (target instanceof String && other instanceof String) {
			result=delegateCheck((String)target,ignoreCase,toffset,ooffset,length,(String) other);
		} else {
			result=carryOutCheck(target, ignoreCase, toffset, other, ooffset, length);
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
		if(outOfBounds(target,offset,length) || outOfBounds(other,start,length)) {
			return false;
		}
		while(tmpLen-->0) { // NOSONAR
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

	private static boolean outOfBounds(final CharSequence str, final int offset, final int length) {
		return offset < 0 || offset > (long) str.length() - length;
	}

}