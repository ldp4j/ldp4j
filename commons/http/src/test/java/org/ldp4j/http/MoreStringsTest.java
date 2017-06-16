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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;


public class MoreStringsTest {

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(MoreStrings.class),equalTo(true));
	}

	@Test
	public void nonEmptyStringHasLength() throws Exception {
		assertThat(MoreStrings.hasLength("length"),equalTo(true));
	}

	@Test
	public void emptyStringHasNotLength() throws Exception {
		assertThat(MoreStrings.hasLength(""),equalTo(false));
	}

	@Test
	public void nullStringHasNotLength() throws Exception {
		assertThat(MoreStrings.hasLength((String)null),equalTo(false));
	}

	@Test
	public void nullIsEqualToNull() throws Exception {
		final String str1 = null;
		final String str2 = null;
		assertThat(MoreStrings.equalsIgnoreCase(str1,str2),equalTo(true));
	}

	@Test
	public void nullIsNotEqualToAnyCharSequence() throws Exception {
		final String str1 = null;
		final String str2 = "string";
		assertThat(MoreStrings.equalsIgnoreCase(str1,str2),equalTo(false));
	}

	@Test
	public void charSequencesAreNotEqualToNull() throws Exception {
		final String str1 = "string";
		final String str2 = null;
		assertThat(MoreStrings.equalsIgnoreCase(str1,str2),equalTo(false));
	}

	@Test
	public void charSequencesAreAlwaysEqualToThemselves() throws Exception {
		final String str1 = "camelCase";
		final String str2 = str1;
		assertThat(MoreStrings.equalsIgnoreCase(str1,str2),equalTo(true));
	}

	@Test
	public void charSequencesOfDifferentLengthsAreNeverEqual() throws Exception {
		final String str1 = "string";
		final String str2 = "longerString";
		assertThat(MoreStrings.equalsIgnoreCase(str1,str2),equalTo(false));
	}

	@Test
	public void charSequencesOfSameLengthMayNotBeEqualDisregardingTheCase() throws Exception {
		final String str1 = "aString";
		final String str2 = "bString";
		assertThat(MoreStrings.equalsIgnoreCase(str1,str2),equalTo(false));
	}

	@Test
	public void charSequenceAndStringOfSameLengthMayNotBeEqualDisregardingTheCase() throws Exception {
		final CharSequence str1 = new StringBuilder("aString");
		final CharSequence str2 = "bString";
		assertThat(MoreStrings.equalsIgnoreCase(str1,str2),equalTo(false));
	}

	@Test
	public void stringAndCharSequenceOfSameLengthMayNotBeEqualDisregardingTheCase() throws Exception {
		final CharSequence str1 = "aString";
		final CharSequence str2 = new StringBuilder("bString");
		assertThat(MoreStrings.equalsIgnoreCase(str1,str2),equalTo(false));
	}

	@Test
	public void caseInsensitiveEqualCharSequencesAreEqual() throws Exception {
		final CharSequence str1 = new StringBuilder("camelCaseString");
		final CharSequence str2 = new StringBuilder("CAMELcASEsTRING");
		assertThat(MoreStrings.equalsIgnoreCase(str1,str2),equalTo(true));
	}

	@Test
	public void regionDoesNotMatchCaseSensitiveIfStringsAreNotEqual() throws Exception {
		final CharSequence str1 = new StringBuilder("camelCaseString");
		final CharSequence str2 = new StringBuilder("CAMELcASEsTRING");
		assertThat(MoreStrings.regionMatches(str1, false, 0, str2, 0, str1.length()),equalTo(false));
	}

	@Test
	public void regionMatchesCaseSensitiveIfStringsAreEqual() throws Exception {
		final String str1 = "camelCaseString";
		final String str2 = "camelCaseString";
		regionMatchesCase(str1, false, 0, str2, 0, str1.length());
	}

	@Test
	public void regionDoesNotMatchIfTargetOffsetIsNegative() throws Exception {
		final String str1 = "camelCaseString";
		final String str2 = "camelCaseString";
		regionMatchesCase(str1, true, -1, str2, 0, str1.length());
	}

	@Test
	public void regionDoesNotMatchIfTargetIsNotLongEnough() throws Exception {
		final String str1 = "camelCaseString";
		final String str2 = "camelCaseString";
		regionMatchesCase(str1, true, 0, str2, 0, str1.length()+1);
	}

	@Test
	public void regionDoesNotMatchIfOtherOffsetIsNegative() throws Exception {
		final String str1 = "camelCaseString";
		final String str2 = "camelCaseString";
		regionMatchesCase(str1, true, 0, str2, -1, str1.length());
	}

	@Test
	public void regionDoesNotMatchIfOtherIsNotLongEnough() throws Exception {
		final String str1 = "camelCaseString";
		final String str2 = "camelCase";
		regionMatchesCase(str1, true, 0, str2, 0, str1.length());
	}

	@Test
	public void regionMatchesIfGoingBackwards() throws Exception {
		final String str1 = "camelCaseString";
		final String str2 = "camelCaseString";
		regionMatchesCase(str1, true, str1.length(), str2, 0, -str1.length());
	}

	@Test
	public void equalsIgnoreCaseTakesIntoAccountCaseSensitivityInGreekAndCopticPlane() {
		final CharSequence str1 = new StringBuilder().appendCodePoint(0x03f4); // Greek capital theta symbol
		final CharSequence str2 = new StringBuilder().appendCodePoint(0x03b8); // Greek small theta symbol
		System.out.printf("Greek and Coptic Plane (capital symbol and small symbol): %s <--> %s%n",str1,str2);
		assertThat(MoreStrings.equalsIgnoreCase(str1, str2),equalTo(true));
	}

	@Test
	public void equalsIgnoreCaseTakesIntoAccountAlternativesInGreekAndCopticPlaneSymbolsAndLetters() {
		final CharSequence str1 = new StringBuilder().appendCodePoint(0x03f4); // Greek capital theta symbol
		final CharSequence str2 = new StringBuilder().appendCodePoint(0x03d1); // Greek theta symbol
		System.out.printf("Greek and Coptic Plane (capital symbol and small letter): %s <--> %s%n",str1,str2);
		assertThat(MoreStrings.equalsIgnoreCase(str1, str2),equalTo(true));
	}

	@Test
	public void equalsIgnoreCaseTakesIntoAccountAlternativesInGreekAndCopticPlaneLetterAndSymbols() {
		final CharSequence str1 = new StringBuilder().appendCodePoint(0x0398); // Greek capital letter theta
		final CharSequence str2 = new StringBuilder().appendCodePoint(0x03b8); // Greek small theta symbol
		System.out.printf("Greek and Coptic Plane (capital letter and small symbol): %s <--> %s%n",str1,str2);
		assertThat(MoreStrings.equalsIgnoreCase(str1, str2),equalTo(true));
	}


	@Test
	public void equalsIgnoreCaseTakesIntoAccountCaseSensitivityInLatinExtendedABlock() {
		final CharSequence str1 = new StringBuilder().appendCodePoint(0x0130); // Latin capital letter I with dot above
		final CharSequence str2 = new StringBuilder().appendCodePoint(0x0131); // Latin small letter dotless I
		System.out.printf("Latin Extended-A Block (capital letter and small letter): %s <--> %s%n",str1,str2);
		assertThat(MoreStrings.equalsIgnoreCase(str1, str2),equalTo(true));
	}

	private void regionMatchesCase(
			final String target,
			final boolean ignoreCase,
			final int toffset,
			final String other,
			final int ooffset,
			final int length) {
		final CharSequence cs1 = new StringBuilder(target);
		final CharSequence cs2 = new StringBuilder(other);
		assertThat(
			MoreStrings.regionMatches(cs1, ignoreCase, toffset, cs2, ooffset, length),
			equalTo(
				MoreStrings.regionMatches(target, ignoreCase, toffset, other, ooffset, length)));
	}

}