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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;


public class HttpUtilsTest {

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(HttpUtils.class),equalTo(true));
	}

	@Test
	public void cannotTrimNull() throws Exception {
		try {
			HttpUtils.trimWhitespace(null);
			fail("Should not trim null");
		} catch (NullPointerException e) {
			assertThat(e.getMessage(),equalTo("Token cannot be null"));
		}
	}

	@Test
	public void acceptsEmptyStrings() throws Exception {
		assertThat(HttpUtils.trimWhitespace(""),equalTo(""));
	}

	@Test
	public void doesNotTrimUntrimmableStrings() throws Exception {
		assertThat(HttpUtils.trimWhitespace("text"),equalTo("text"));
	}

	@Test
	public void trimsLeadingWhitespace() throws Exception {
		assertThat(HttpUtils.trimWhitespace(" \t \ttext"),equalTo("text"));
	}

	@Test
	public void trimsTrailingWhitespace() throws Exception {
		assertThat(HttpUtils.trimWhitespace("text \t \t"),equalTo("text"));
	}

	@Test
	public void trimsBothEndsWhitespace() throws Exception {
		assertThat(HttpUtils.trimWhitespace("\t \t text\t \t "),equalTo("text"));
	}

	@Test
	public void recognizesParameterDelimiter() {
		assertThat(HttpUtils.isParameterDelimiter(';'),equalTo(true));
	}

	@Test
	public void rejectsNonParameterDelimiter() {
		assertThat(HttpUtils.isParameterDelimiter(','),equalTo(false));
	}

	@Test
	public void nullIsNotValidToken() {
		try {
			HttpUtils.checkToken(null);
			fail("Null is not a valid token");
		} catch (IllegalArgumentException e) {
			assertThat(e.getCause().getMessage(),equalTo("Token cannot be empty"));
		}
	}

	@Test
	public void emptyStringIsNotValidToken() {
		try {
			HttpUtils.checkToken("");
			fail("Empty string is not a valid token");
		} catch (IllegalArgumentException e) {
			assertThat(e.getCause().getMessage(),equalTo("Token cannot be empty"));
		}
	}

	@Test
	public void nullQualityIsValid() {
		assertThat(HttpUtils.checkQuality(null,"Weight"),nullValue());
	}

}
