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

import java.util.BitSet;

final class RFC6838MediaRangeValidator implements MediaRangeValidator {

	private static final long serialVersionUID = -6377052968246092604L;

	private static final BitSet RESTRICTED_NAME_CHARS;
	private static final BitSet RESTRICTED_NAME_FIRST;

	static {
		RESTRICTED_NAME_FIRST=new BitSet(0xFF);
		RESTRICTED_NAME_FIRST.set(0x41, 0x5B); // A-Z
		RESTRICTED_NAME_FIRST.set(0x61, 0x7B); // a-z
		RESTRICTED_NAME_FIRST.set(0x30, 0x3A); // 0-9

		RESTRICTED_NAME_CHARS=new BitSet(0xFF);
		RESTRICTED_NAME_CHARS.set(0x41, 0x5B); // A-Z
		RESTRICTED_NAME_CHARS.set(0x61, 0x7B); // a-z
		RESTRICTED_NAME_CHARS.set(0x30, 0x3A); // 0-9
		RESTRICTED_NAME_CHARS.set(0x30, 0x3A); // 0-9
		RESTRICTED_NAME_CHARS.set('!');
		RESTRICTED_NAME_CHARS.set('#');
		RESTRICTED_NAME_CHARS.set('$');
		RESTRICTED_NAME_CHARS.set('&');
		RESTRICTED_NAME_CHARS.set('-');
		RESTRICTED_NAME_CHARS.set('^');
		RESTRICTED_NAME_CHARS.set('_');
		RESTRICTED_NAME_CHARS.set('.');
		RESTRICTED_NAME_CHARS.set('+');
	}

	RFC6838MediaRangeValidator() {
	}

	/**
	 * Checks the given {@code type} for illegal characters, as defined in RFC
	 * 6838, section 4.2. NOTE: '*' is accepted as a valid type.
	 *
	 * @param type
	 *            the string to validate
	 * @throws IllegalArgumentException
	 *             in case of illegal characters
	 * @see <a href="https://tools.ietf.org/html/rfc6838#section-4.2">Media Type
	 *      Specifications and Registration Procedures, Section 4.2</a>
	 */
	@Override
	public void checkType(final String type) {
		validateLength(type,"Type");
		validateFirstChar(type,"type");
		validateOtherChars(type,"type");
	}

	/**
	 * Checks the given {@code subtype} for illegal characters, as defined in RFC
	 * 6838, section 4.2. NOTE: '*' is accepted as a valid subtype.
	 *
	 * @param subType
	 *            the string to validate
	 * @throws IllegalArgumentException
	 *             in case of illegal characters
	 * @see <a href="https://tools.ietf.org/html/rfc6838#section-4.2">Media Type
	 *      Specifications and Registration Procedures, Section 4.2</a>
	 */
	@Override
	public void checkSubType(final String subType) {
		validateLength(subType,"Subtype");
		validateFirstChar(subType,"subtype");
		validateOtherChars(subType,"subtype");
	}

	/**
	 * Checks the given {@code suffix} for illegal characters, as defined in RFC
	 * 6838, section 4.2.
	 *
	 * @param suffix
	 *            the string to validate
	 * @throws IllegalArgumentException
	 *             in case of illegal characters
	 * @see <a href="https://tools.ietf.org/html/rfc6838#section-4.2">Media Type
	 *      Specifications and Registration Procedures, Section 4.2</a>
	 */
	@Override
	public void checkSuffix(final String suffix) {
		validateOtherChars(suffix,"suffix");
	}

	private void validateLength(final String value, final String name) {
		checkArgument(value.length()<128,"%s length cannot be greater than 127 ('%s' has %s)",name,value,value.length());
	}

	private void validateFirstChar(final String value, String name) {
		final char ch = value.charAt(0);
		boolean check=false;
		if(ch=='*') {
			check=value.length()==1;
		} else {
			check=RESTRICTED_NAME_FIRST.get(ch);
		}
		checkArgument(check,"Invalid character '%s' in %s '%s' at 0",ch,name,value);
	}

	private void validateOtherChars(final String value, final String name) {
		for(int i=1;i<value.length();i++) {
			final char ch = value.charAt(i);
			checkArgument(RESTRICTED_NAME_CHARS.get(ch),"Invalid character '%s' in %s '%s' at %s",ch,name,value,i);
		}
	}

}