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

final class Characters {

	private Characters() {
	}

	static boolean isColon(final int codePoint) {
		return codePoint == ':';
	}

	/**
	 * Determines if a character is a Name start character.
	 *
	 * @param codePoint
	 *            The code point of the character to be tested. For UTF-16
	 *            characters the code point corresponds to the value of the char
	 *            that represents the character.
	 * @return {@code true} if {@code codePoint} is a Name start character,
	 *         otherwise {@code false}
	 * @see <b>Name</b> definition in the <a
	 *      href="https://www.w3.org/TR/REC-xml/#d0e804">Extensible Markup
	 *      Language (XML) 1.0 (Fifth Edition)</a> specification.
	 */
	static boolean isNameStartChar(final int codePoint) {
		return
			(codePoint == ':')                             ||
			(codePoint >= 'A' && codePoint <= 'Z')         ||
			(codePoint == '_')                             ||
			(codePoint >= 'a' && codePoint <= 'z')         ||
			(codePoint >= 0xC0 && codePoint <= 0xD6)       ||
			(codePoint >= 0xD8 && codePoint <= 0xF6)       ||
			(codePoint >= 0xF8 && codePoint <= 0x2FF)      ||
			(codePoint >= 0x370 && codePoint <= 0x37D)     ||
			(codePoint >= 0x37F && codePoint <= 0x1FFF)    ||
			(codePoint >= 0x200C && codePoint <= 0x200D)   ||
			(codePoint >= 0x2070 && codePoint <= 0x218F)   ||
			(codePoint >= 0x2C00 && codePoint <= 0x2FEF)   ||
			(codePoint >= 0x3001 && codePoint <= 0xD7FF)   ||
			(codePoint >= 0xF900 && codePoint <= 0xFDCF)   ||
			(codePoint >= 0xFDF0 && codePoint <= 0xFFFD)   ||
			(codePoint >= 0x10000 && codePoint <= 0xEFFFF);
	}

	/**
	 * Determines if a character is a Name character.
	 *
	 * @param codePoint
	 *        The code point of the character to be tested. For UTF-8 and UTF-16
	 *        characters the code point corresponds to the value of the char
	 *        that represents the character.
	 * @return {@code true} if {@code codePoint} is a Name character,
	 *         otherwise {@code false}
	 * @see <b>Name</b> definition in the <a
	 *      href="https://www.w3.org/TR/REC-xml/#d0e804">Extensible Markup
	 *      Language (XML) 1.0 (Fifth Edition)</a> specification.
	 */
	static boolean isNameChar(final int codePoint) {
		return
			isNameStartChar(codePoint)                   ||
			(codePoint == '-')                           ||
			(codePoint == '.')                           ||
			(codePoint >= '0' && codePoint <= '9')       ||
			(codePoint == 0xB7)                          ||
			(codePoint >= 0x0300 && codePoint <= 0x036F) ||
			(codePoint >= 0x203F && codePoint <= 0x2040);
	}

	/**
	 * Determines if a character is an NCName (Non-Colonised Name) start
	 * character.
	 *
	 * @param codePoint
	 *        The code point of the character to be tested. For UTF-8 and UTF-16
	 *        characters the code point corresponds to the value of the char
	 *        that represents the character.
	 * @return {@code true} if {@code codePoint} is a NCName start character,
	 *         otherwise {@code false}.
	 */
	static boolean isNCNameStartChar(final int codePoint) {
		return !isColon(codePoint) && isNameStartChar(codePoint);
	}

	/**
	 * Determines if a character is an NCName (Non-Colonised Name) character.
	 *
	 * @param codePoint
	 *        The code point of the character to be tested. For UTF-8 and UTF-16
	 *        characters the code point corresponds to the value of the char
	 *        that represents the character.
	 * @return {@code true} if {@code codePoint} is a NCName character,
	 *         otherwise {@code false}.
	 */
	static boolean isNCNameChar(final int codePoint) {
		return !isColon(codePoint) && isNameChar(codePoint);
	}

}