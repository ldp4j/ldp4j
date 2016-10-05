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
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class ImmutableNegotiationResultTest {

	@Mocked
	private ImmutableAlternatives alternatives;

	@Mocked
	private ImmutableVariant variant;

	@Mocked
	private ImmutableVariant errorVariant;

	@Mocked
	private ImmutableQuality quality;

	@Test
	public void ifNoVariantIsPresentQualityMustNotBePresent() throws Exception {
		try {
			new ImmutableNegotiationResult(null,quality,errorVariant,alternatives);
			fail("No quality can be present if no variant is selected");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Variant and quality must be simultaneously defined or not"));
		}
	}

	@Test
	public void ifVariantIsPresentQualityMustBePresent() throws Exception {
		try {
			new ImmutableNegotiationResult(variant,null,errorVariant,alternatives);
			fail("Quality must be present if a variant is selected");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Variant and quality must be simultaneously defined or not"));
		}
	}

	@Test
	public void nullSelectionIsNotAcceptable() throws Exception {
		ImmutableNegotiationResult sut=new ImmutableNegotiationResult(null,null,errorVariant,alternatives);
		assertThat(sut.isAcceptable(),equalTo(false));
	}

	@Test
	public void nonNullSelectionsAreAcceptable() throws Exception {
		ImmutableNegotiationResult sut=new ImmutableNegotiationResult(variant,quality,errorVariant,alternatives);
		assertThat(sut.isAcceptable(),equalTo(true));
	}

	@Test
	public void acceptableVariantCanBeRetrieved() throws Exception {
		ImmutableNegotiationResult sut=new ImmutableNegotiationResult(variant,quality,errorVariant,alternatives);
		assertThat(sut.variant(),sameInstance(variant));
	}

	@Test
	public void qualityCanBeRetrieved() throws Exception {
		ImmutableNegotiationResult sut=new ImmutableNegotiationResult(variant,quality,errorVariant,alternatives);
		assertThat(sut.quality(),sameInstance(quality));
	}

	@Test
	public void alternativesCanBeRetrieved() throws Exception {
		ImmutableNegotiationResult sut=new ImmutableNegotiationResult(variant,quality,errorVariant,alternatives);
		assertThat(sut.alternatives(),sameInstance(alternatives));
	}

}
