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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;

import org.junit.Test;


public class CharsetPreferenceTest {

	@Test
	public void testValueOf$happyPath$nonWildcard() throws Exception {
		CharsetPreference sut = CharsetPreference.valueOf("unicode-1-1;q=0.8");
		assertThat(sut,notNullValue());
		assertThat(sut.charset(),equalTo("unicode-1-1"));
		assertThat(sut.weight(),equalTo(0.8D));
	}

	@Test
	public void testValueOf$happyPath$wildcard() throws Exception {
		CharsetPreference sut = CharsetPreference.valueOf("*;q=0.8");
		assertThat(sut,notNullValue());
		assertThat(sut.charset(),equalTo("*"));
		assertThat(sut.weight(),equalTo(0.8D));
	}

	@Test
	public void testValueOf$happyPath$noQuality() throws Exception {
		CharsetPreference sut = CharsetPreference.valueOf("unicode-1-1");
		assertThat(sut,notNullValue());
		assertThat(sut.charset(),equalTo("unicode-1-1"));
		assertThat(sut.weight(),equalTo(1D));
	}

	@Test
	public void testValueOf$happyPath$wildcardNoQuality() throws Exception {
		CharsetPreference sut = CharsetPreference.valueOf("*");
		assertThat(sut,notNullValue());
		assertThat(sut.charset(),equalTo("*"));
		assertThat(sut.weight(),equalTo(1D));
	}

	@Test
	public void testValueOf$unexpectedParameters() throws Exception {
		CharsetPreference sut = CharsetPreference.valueOf("unicode-1-1;q=0.8;param=2");
		assertThat(sut,nullValue());
	}

	@Test
	public void testValueOf$badCharset() throws Exception {
		CharsetPreference sut = CharsetPreference.valueOf("unicode\u000001\t1;q=0.8");
		assertThat(sut,nullValue());
	}

	@Test
	public void testValueOf$badQuality$greaterThanOne() throws Exception {
		CharsetPreference sut = CharsetPreference.valueOf("unicode-1-1;q=1.8");
		assertThat(sut,nullValue());
	}

	@Test
	public void testRound$happyPath() throws Exception {
		assertThat(CharsetPreference.round(0.5001D),equalTo(500));
	}

	@Test
	public void testRound$lowerThanZero() throws Exception {
		try {
			CharsetPreference.round(-1D);
			fail("Should not round weights lower than 0");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Weight must be greater than or equal to 0 (-1.0)"));
		}
	}

	@Test
	public void testRound$greaterThanOne() throws Exception {
		try {
			CharsetPreference.round(1.1D);
			fail("Should not round weights greater than 1");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Weight must be lower than or equal to 1 (1.1)"));
		}
	}

	@Test
	public void testCreateCharsetDouble() throws Exception {
		CharsetPreference sut=CharsetPreference.create(StandardCharsets.UTF_8,(double)1/3);
		assertThat(sut,notNullValue());
		assertThat(sut.charset(),equalTo(StandardCharsets.UTF_8.displayName()));
		assertThat(sut.weight(),equalTo(0.333D));
	}

	@Test
	public void testCreateCharset() throws Exception {
		CharsetPreference sut=CharsetPreference.create(StandardCharsets.UTF_8);
		assertThat(sut,notNullValue());
		assertThat(sut.charset(),equalTo(StandardCharsets.UTF_8.displayName()));
		assertThat(sut.weight(),equalTo(1D));
	}

	@Test
	public void testCreateWildcardDouble() throws Exception {
		CharsetPreference sut=CharsetPreference.wildcard((double)1/3);
		assertThat(sut,notNullValue());
		assertThat(sut.charset(),equalTo("*"));
		assertThat(sut.weight(),equalTo(0.333D));
	}

	@Test
	public void testCreateWildcard() throws Exception {
		CharsetPreference sut=CharsetPreference.wildcard();
		assertThat(sut,notNullValue());
		assertThat(sut.charset(),equalTo("*"));
		assertThat(sut.weight(),equalTo(1D));
	}

	@Test
	public void assertHasCustomString$withQuality() {
		String expected="unicode-1-1 ; q=0.8";
		CharsetPreference sut = CharsetPreference.valueOf(expected);
		assertThat(sut.toString(),equalTo(expected));
	}

	@Test
	public void assertHasCustomString$withOutQuality() {
		String expected="unicode-1-1 ; q=1.000";
		CharsetPreference sut = CharsetPreference.valueOf(expected);
		assertThat(sut.toString(),equalTo("unicode-1-1"));
	}

	@Test
	public void testEquals$differentType() {
		CharsetPreference one=CharsetPreference.wildcard();
		assertThat((Object)one,not(equalTo((Object)"string")));
	}

	@Test
	public void testEquals$equal() {
		CharsetPreference one=CharsetPreference.wildcard();
		CharsetPreference other=CharsetPreference.wildcard();
		assertThat(one,equalTo(other));
	}

	@Test
	public void testEquals$differentCharset() {
		CharsetPreference one=CharsetPreference.wildcard();
		CharsetPreference other=CharsetPreference.create(StandardCharsets.UTF_8);
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void testEquals$differentWeight() {
		CharsetPreference one=CharsetPreference.wildcard();
		CharsetPreference other=CharsetPreference.wildcard(0.00D);
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void testHashCode$equal() {
		CharsetPreference one=CharsetPreference.wildcard();
		CharsetPreference other=CharsetPreference.wildcard();
		assertThat(one.hashCode(),equalTo(other.hashCode()));
	}

	@Test
	public void testHashCode$differentCharset() {
		CharsetPreference one=CharsetPreference.wildcard();
		CharsetPreference other=CharsetPreference.create(StandardCharsets.UTF_8);
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void testHashCode$differentWeight() {
		CharsetPreference one=CharsetPreference.wildcard();
		CharsetPreference other=CharsetPreference.wildcard(0.00D);
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void testComparable$lower() {
		CharsetPreference other=CharsetPreference.wildcard();
		CharsetPreference one=CharsetPreference.wildcard(0.00D);
		assertThat(one.compareTo(other),not(greaterThanOrEqualTo(0)));
	}

	@Test
	public void testComparable$greater() {
		CharsetPreference one=CharsetPreference.wildcard();
		CharsetPreference other=CharsetPreference.wildcard(0.00D);
		assertThat(one.compareTo(other),greaterThanOrEqualTo(0));
	}

	@Test
	public void testComparable$equal() {
		CharsetPreference one=CharsetPreference.wildcard(0.333D);
		assertThat(one.compareTo(one),equalTo(0));
	}

}
