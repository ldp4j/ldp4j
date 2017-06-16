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
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;


public class RFC6838MediaRangeValidatorTest {

	private MediaRangeValidator sut=new RFC6838MediaRangeValidator();

	@Test
	public void failsForTypeWithInvalidFirstChar() throws Exception {
		try {
			sut.checkType(".invalidFirstChar");
			fail("Should fail type check if first char is not valid");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Invalid character '.' in type '.invalidFirstChar' at 0"));
		}
	}

	@Test
	public void failsForSubTypeWithInvalidFirstChar() throws Exception {
		try {
			sut.checkSubType(".invalidFirstChar");
			fail("Should fail subtype check if first char is not valid");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Invalid character '.' in subtype '.invalidFirstChar' at 0"));
		}
	}

	@Test
	public void failsIfTypeLengthIsGreaterThanExpected() throws Exception {
		try {
			sut.checkType(longName());
			fail("Should fail type check if length is greater than expected");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Type length cannot be greater than 127 ('"+longName()+"' has 128)"));
		}
	}

	@Test
	public void acceptsMinimalType() {
		sut.checkType("A");
	}

	@Test
	public void acceptsWildcardType() {
		sut.checkType("*");
	}

	@Test
	public void failsForWrongWildcardType() {
		try {
			sut.checkType("*bad");
			fail("Should fail type check if bad wildcard type is defined");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Invalid character '*' in type '*bad' at 0"));
		}
	}

	@Test
	public void failsForTypeWithInvalidOtherChar() throws Exception {
		try {
			sut.checkType("invalidÓtherChar");
			fail("Should fail type check if other char is not valid");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Invalid character 'Ó' in type 'invalidÓtherChar' at 7"));
		}
	}

	@Test
	public void failsForSubTypeWithInvalidOtherChar() throws Exception {
		try {
			sut.checkSubType("invalidÓtherChar");
			fail("Should fail subtype check if other char is not valid");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Invalid character 'Ó' in subtype 'invalidÓtherChar' at 7"));
		}
	}

	@Test
	public void failsIfSubtypeLengthIsGreaterThanExpected() throws Exception {
		try {
			sut.checkSubType(longName());
			fail("Should fail subtype check if length is greater than expected");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Subtype length cannot be greater than 127 ('"+longName()+"' has 128)"));
		}
	}

	@Test
	public void acceptsMinimalSubtype() {
		sut.checkSubType("A");
	}

	@Test
	public void acceptsWildcardSubType() {
		sut.checkSubType("*");
	}

	@Test
	public void failsForWrongWildcardSubType() {
		try {
			sut.checkSubType("*bad");
			fail("Should fail subtype check if bad wildcard subtype is defined");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Invalid character '*' in subtype '*bad' at 0"));
		}
	}

	@Test
	public void failsForSuffixWithInvalidOtherChar() throws Exception {
		try {
			sut.checkSuffix("invalidÓtherChar");
			fail("Should fail suffix check if other char is not valid");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Invalid character 'Ó' in suffix 'invalidÓtherChar' at 7"));
		}
	}

	@Test
	public void acceptsMinimalSuffix() {
		sut.checkSuffix("A");
	}

	private String longName() {
		char[] array=new char[128];
		Arrays.fill(array, 'a');
		return new String(array);
	}
}
