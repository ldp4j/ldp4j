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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;

public class WeightedTest {

	@Test
	public void weightsCannotBeNegative() throws Exception {
		try {
			Weighted.newInstance().weight(-1.0D);
			fail("Should fail if weight is negative");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Weight cannot be negative (-1.0)"));
		}
	}

	@Test
	public void weightsCannotBeGreaterThanOne() throws Exception {
		try {
			Weighted.newInstance().weight(1.001D);
			fail("Should fail if weight is greater than one");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Weight cannot be greater than 1 (1.001)"));
		}
	}

	@Test
	public void weightsCannotHaveMoreThanThreeDecimals() throws Exception {
		try {
			Weighted.newInstance().weight(0.1234D);
			fail("Should fail if weight has more than three decimals");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Weight cannot have more than 3 decimals (0.1234)"));
		}
	}

	@Test
	public void changingTheWeightDoesNotModifyTheInstance() throws Exception {
		final Weighted<Object> original = Weighted.newInstance();
		final Weighted<?> updated = original.weight(0.123D);
		assertThat(original.weight(),equalTo(1.0D));
		assertThat(updated.weight(),equalTo(0.123D));
	}

	@Test
	public void changingTheWeightedDoesNotModifyTheInstance() throws Exception {
		final Weighted<Object> original = Weighted.newInstance();
		final Weighted<String> updated = original.content("string");
		assertThat(original.get(),nullValue());
		assertThat(updated.get(),equalTo("string"));
	}

	@Test
	public void hasCustomStringRepresentation() {
		final Weighted<String> original=Weighted.newInstance().weight(0.123D).content("text/turtle");
		assertThat(original.toString(),not(equalTo(Utils.defaultToString(original))));
	}

}
