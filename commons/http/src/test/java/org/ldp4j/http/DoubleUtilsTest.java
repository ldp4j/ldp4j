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

public class DoubleUtilsTest {

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(DoubleUtils.class),equalTo(true));
	}

	@Test
	public void rejectsValuesWithMoreDigitsThanSpecified() throws Exception {
		assertThat(DoubleUtils.hasPrecision(0.1234,3),equalTo(false));
	}

	@Test
	public void acceptsValuesWithLessDigitsThanSpecified() throws Exception {
		assertThat(DoubleUtils.hasPrecision(0.12345678,9),equalTo(true));
	}

	@Test
	public void acceptsValuesWithTheSameDigitsThanSpecified() throws Exception {
		assertThat(DoubleUtils.hasPrecision(0.12345,5),equalTo(true));
	}

	@Test
	public void doesNotRoundValuesWithLessDigitsThanSpecified() throws Exception {
		assertThat(DoubleUtils.limitPrecision(0.12345678,9),equalTo(0.12345678));
	}

	@Test
	public void roundsValuesWithMoreDigitsThanSpecified$roundDown() throws Exception {
		assertThat(DoubleUtils.limitPrecision(0.12345678,3),equalTo(0.123));
	}

	@Test
	public void roundsValuesWithMoreDigitsThanSpecified$roundUp() throws Exception {
		assertThat(DoubleUtils.limitPrecision(0.12345678,5),equalTo(0.12346));
	}

}
