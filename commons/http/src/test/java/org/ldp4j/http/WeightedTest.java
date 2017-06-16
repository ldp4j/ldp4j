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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.ldp4j.http.Weighted.Parser;

public class WeightedTest {

	@Test
	public void parsingDoesNotRequireQualityDefinition() {
		final String candidate = "value ; qa=1.000";
		final Weighted<String> sut = Weighted.fromString(candidate);
		assertThat(sut.entity(),equalTo(candidate));
		assertThat(sut.hasWeight(),equalTo(false));
		assertThat(sut.weight(),equalTo(1.0D));
	}

	@Test
	public void qualityParameterCanBeSeparatedWithSpaces() {
		final Weighted<String> sut = Weighted.fromString("value ; q=1.000");
		assertThat(sut.entity(),equalTo("value"));
		assertThat(sut.hasWeight(),equalTo(true));
		assertThat(sut.weight(),equalTo(1.0D));
	}

	@Test
	public void qualityParameterCanBeSeparatedWithTabs() {
		final Weighted<String> sut = Weighted.fromString("value\t;\tq=1.000");
		assertThat(sut.entity(),equalTo("value"));
		assertThat(sut.hasWeight(),equalTo(true));
		assertThat(sut.weight(),equalTo(1.0D));
	}

	@Test
	public void qualityParameterCanBeLowerCase() {
		final Weighted<String> sut = Weighted.fromString("value;q=1.000");
		assertThat(sut.entity(),equalTo("value"));
		assertThat(sut.hasWeight(),equalTo(true));
		assertThat(sut.weight(),equalTo(1.0D));
	}

	@Test
	public void qualityParameterCanBeUpperCase() {
		final Weighted<String> sut = Weighted.fromString("value;Q=1.000");
		assertThat(sut.entity(),equalTo("value"));
		assertThat(sut.hasWeight(),equalTo(true));
		assertThat(sut.weight(),equalTo(1.0D));
	}

	@Test
	public void qualityParameterValueCanHaveNoDecimals() {
		final Weighted<String> sut = Weighted.fromString("value;Q=1.");
		assertThat(sut.entity(),equalTo("value"));
		assertThat(sut.hasWeight(),equalTo(true));
		assertThat(sut.weight(),equalTo(1.0D));
	}

	@Test
	public void qualityParameterValueCanHaveOneDecimal() {
		final Weighted<String> sut = Weighted.fromString("value;q=0.1");
		assertThat(sut.entity(),equalTo("value"));
		assertThat(sut.hasWeight(),equalTo(true));
		assertThat(sut.weight(),equalTo(0.1D));
	}

	@Test
	public void qualityParameterValueCanHaveTwoDecimals() {
		final Weighted<String> sut = Weighted.fromString("value;q=0.01");
		assertThat(sut.entity(),equalTo("value"));
		assertThat(sut.hasWeight(),equalTo(true));
		assertThat(sut.weight(),equalTo(0.01D));
	}

	@Test
	public void qualityParameterValueCanHaveThreeDecimals() {
		final Weighted<String> sut = Weighted.fromString("value;q=0.001");
		assertThat(sut.entity(),equalTo("value"));
		assertThat(sut.hasWeight(),equalTo(true));
		assertThat(sut.weight(),equalTo(0.001D));
	}

	@Test
	public void qualityParameterCannotHaveWhitespacesBeforeEqual() {
		try {
			Weighted.fromString("value;q \t=1.000");
			fail("Should fail when whitespaces before equal");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Quality definition failure found: whitespace before equal"));
		}
	}

	@Test
	public void qualityParameterCannotHaveWhitespacesAfterEqual() {
		try {
			Weighted.fromString("value;q=\t 1.000");
			fail("Should fail when whitespaces after equal");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Quality definition failure found: whitespace after equal"));
		}
	}

	@Test
	public void qualityParameterValuesCannotHaveMoreThanThreeDigits() {
		try {
			Weighted.fromString("value;q=1.0000");
			fail("Should fail when quality parameter value has more than three digits");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Quality definition failure found: invalid weight value '1.0000'"));
		}
	}

	@Test
	public void qualityParameterValuesRequireDotSeparator() {
		try {
			Weighted.fromString("value;q=1");
			fail("Should fail when no dot separator is present");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Quality definition failure found: invalid weight value '1'"));
		}
	}

	@Test
	public void qualityParameterValuesCannotBeGreaterThanOne() {
		try {
			Weighted.fromString("value;q=2.0");
			fail("Should fail when quality value is greater than 1");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Quality definition failure found: invalid weight value '2.0'"));
		}
	}

	@Test
	public void qualityParameterValuesCannotBeEmpty() {
		try {
			Weighted.fromString("value;q=");
			fail("Should fail when quality parameter value is empty");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Quality definition failure found: weight cannot be empty"));
		}
	}

	@Test
	public void parsersGetSubstringBeforeAndAfterQualityDefinition() {
		Weighted<String> sut = Weighted.fromString("value ; q=1.000;param=value", new Parser<String>(){
			@Override
			public String parse(String before, String after) {
				assertThat(before,equalTo("value"));
				assertThat(after,equalTo(";param=value"));
				return before;
			}});
		assertThat(sut.entity(),equalTo("value"));
		assertThat(sut.hasWeight(),equalTo(true));
		assertThat(sut.weight(),equalTo(1.0D));
	}

	@Test
	public void parsersGetSubstringBeforeAndEmptyStringIfQualityDefinitionIsTheTailOfTheHeader() {
		Weighted<String> sut = Weighted.fromString("value ; q=1.000", new Parser<String>(){
			@Override
			public String parse(String before, String after) {
				assertThat(before,equalTo("value"));
				assertThat(after,equalTo(""));
				return before;
			}});
		assertThat(sut.entity(),equalTo("value"));
		assertThat(sut.hasWeight(),equalTo(true));
		assertThat(sut.weight(),equalTo(1.0D));
	}

	@Test
	public void parsersGetInputDataAndNullStringIfNoQualityDefinitionWasFound() {
		final String candidate = "value ; qa=1.000;param=value";
		Weighted<String> sut = Weighted.fromString(candidate, new Parser<String>(){
			@Override
			public String parse(String before, String after) {
				assertThat(before,equalTo(candidate));
				assertThat(after,nullValue());
				return before;
			}});
		assertThat(sut.entity(),equalTo(candidate));
		assertThat(sut.hasWeight(),equalTo(false));
		assertThat(sut.weight(),equalTo(1.0D));
	}

	@Test
	public void multipleQualityParametersAreNotAllowed() {
		try {
			Weighted.fromString("value;q=1.000;q=0.000");
			fail("Should fail when multiple quality parameters are defined");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Only one quality value can be specified (found 2: "));
		}
	}

	@Test
	public void weightsCannotBeNegative() throws Exception {
		try {
			Weighted.newInstance().withWeight(-1.0D);
			fail("Should fail if weight is negative");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Weight cannot be negative (-1.0)"));
		}
	}

	@Test
	public void weightsCannotBeGreaterThanOne() throws Exception {
		try {
			Weighted.newInstance().withWeight(1.001D);
			fail("Should fail if weight is greater than one");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Weight cannot be greater than 1 (1.001)"));
		}
	}

	@Test
	public void weightsCannotHaveMoreThanThreeDecimals() throws Exception {
		try {
			Weighted.newInstance().withWeight(0.1234D);
			fail("Should fail if weight has more than three decimals");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Weight cannot have more than 3 decimals (0.1234)"));
		}
	}

	@Test
	public void changingTheWeightDoesNotModifyTheInstance() throws Exception {
		final Weighted<Object> original = Weighted.newInstance();
		final Weighted<?> updated = original.withWeight(0.123D);
		assertThat(original.weight(),equalTo(1.0D));
		assertThat(original.hasWeight(),equalTo(false));
		assertThat(updated.weight(),equalTo(0.123D));
		assertThat(updated.hasWeight(),equalTo(true));
	}

	@Test
	public void changingTheWeightedDoesNotModifyTheInstance() throws Exception {
		final Weighted<Object> original = Weighted.newInstance();
		final Weighted<String> updated = original.withEntity("string");
		assertThat(original.entity(),nullValue());
		assertThat(updated.entity(),equalTo("string"));
	}

	@Test
	public void hasCustomStringRepresentation() {
		final Weighted<String> original=Weighted.newInstance().withWeight(0.123D).withEntity("text/turtle");
		assertThat(original.toString(),not(equalTo(Utils.defaultToString(original))));
	}

}
