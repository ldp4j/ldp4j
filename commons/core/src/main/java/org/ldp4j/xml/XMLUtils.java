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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.xml;




/**
 * This class contains various methods for checking QNames, NCNames etc. The
 * implementation is based on the <a href="http://www.w3.org/TR/xml-names/">W3C
 * namespaces in XML specification</a>.
 *
 */
public final class XMLUtils {

	/** &amp;lt; shortcut */
	private static final String LT = "&lt;";

	/** &amp;gt; shortcut */
	private static final String GT = "&gt;";

	/** &amp;quot; shortcut */
	private static final String QUOT = "&quot;";

	/** &amp;amp; shortcut */
	private static final String AMP = "&amp;";

	/** &amp;apos; shortcut */
	private static final String APOS = "&apos;";

	private XMLUtils() {
	}

	/**
	 * Determines if a character sequence is a Name. An Name is a string which
	 * starts with a Name start character and is followed by zero or more Name
	 * characters.
	 *
	 * @param s
	 *            The character sequence to be tested.
	 * @return {@code true} if {@code s} is a Name, otherwise {@code false}.
	 */
	public static boolean isName(final CharSequence s) {
		return Validators.name().isValid(s);
	}

	/**
	 * Determines if a character sequence is an NCName (Non-Colonised Name). An
	 * NCName is a string which starts with an NCName start character and is
	 * followed by zero or more NCName characters.
	 *
	 * @param s
	 *            The character sequence to be tested.
	 * @return {@code true} if {@code s} is an NCName, otherwise {@code false}.
	 */
	public static boolean isNCName(final CharSequence s) {
		return Validators.ncName().isValid(s);
	}

	/**
	 * Determines if a character sequence is a QName. A QName is either an
	 * NCName (LocalName), or an NCName followed by a colon followed by another
	 * NCName (where the first NCName is referred to as the 'Prefix Name' and
	 * the second NCName is referred to as the 'Local Name' - i.e.
	 * PrefixName:LocalName).
	 *
	 * @param s
	 *            The character sequence to be tested.
	 * @return {@code true} if {@code s} is a QName, otherwise {@code false}.
	 */
	public static boolean isQName(final CharSequence s) {
		return Validators.qName().isValid(s);
	}

	/**
	 * Escapes a character sequence so that it is valid XML.
	 *
	 * @param s
	 *            The character sequence.
	 * @return The escaped version of the character sequence.
	 */
	public static String escapeXML(final CharSequence s) {
		final StringBuilder sb = new StringBuilder(s.length() * 2);
		final CodePointIterator iterator = new CodePointIterator(s);
		while (iterator.hasNext()) {
			final int codePoint = iterator.next();
			if (codePoint == '<') {
				sb.append(LT);
			} else if (codePoint == '>') {
				sb.append(GT);
			} else if (codePoint == '\"') {
				sb.append(QUOT);
			} else if (codePoint == '&') {
				sb.append(AMP);
			} else if (codePoint == '\'') {
				sb.append(APOS);
			} else {
				sb.appendCodePoint(codePoint);
			}
		}
		return sb.toString();
	}

}