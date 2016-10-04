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
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

public class ImmutableAlternativesTest {

	private static final ImmutableQuality Q2 = ImmutableQuality.create(Quality.Type.DEFINITE,0.3D);
	private static final ImmutableQuality Q1 = ImmutableQuality.create(Quality.Type.DEFINITE,0.1D);
	private static final ImmutableAlternative ALT2 = ImmutableAlternative.create(0.4D,ImmutableVariant.newInstance());
	private static final ImmutableAlternative ALT1 = ImmutableAlternative.create(0.2D,ImmutableVariant.newInstance());

	@Test
	public void emptyAlternativesIsEmpty() throws Exception {
		assertThat(empty().isEmpty(),equalTo(true));
	}

	@Test
	public void populatedAlternativesIsNotEmpty() throws Exception {
		assertThat(populated().isEmpty(),equalTo(false));
	}

	@Test
	public void hasSizeReflectsActualSizeOfPopulatedAlternatives() throws Exception {
		assertThat(empty().size(),equalTo(0));
		assertThat(populated().size(),equalTo(2));
	}

	@Test
	public void retrievesQualityAccordingToRegistrationIndex() throws Exception {
		assertThat(populated().quality(0),equalTo(Q1));
		assertThat(populated().quality(1),equalTo(Q2));
	}

	@Test
	public void retrievesAlternativeAccordingToRegistrationIndex() throws Exception {
		assertThat(populated().alternative(0),sameInstance(ALT1));
		assertThat(populated().alternative(1),sameInstance(ALT2));
	}

	@Test
	public void iteratesAlternativesAccordingRegistrationOrder() throws Exception {
		Iterator<Alternative> iterator = populated().iterator();
		assertThat(iterator.next(),sameInstance((Alternative)ALT1));
		assertThat(iterator.next(),sameInstance((Alternative)ALT2));
		assertThat(iterator.hasNext(),equalTo(false));
	}

	@Test
	public void cannotIteratesEmptyAlternatives() throws Exception {
		Iterator<Alternative> iterator = empty().iterator();
		assertThat(iterator.hasNext(),equalTo(false));
	}

	@Test
	public void cannotIterateBeyondTheEndOfTheRegisteredAlternatives() throws Exception {
		try {
			empty().iterator().next();
			fail("Iterator should not iterate over the end of the registered alternatives");
		} catch (NoSuchElementException e) {
			assertThat(e.getMessage(),equalTo("No more alternatives are available"));
		}
	}

	@Test
	public void iteratorDoesNotSupportRemoval() throws Exception {
		try {
			empty().iterator().remove();
			fail("Iterator should not support removal");
		} catch (UnsupportedOperationException e) {
			assertThat(e.getMessage(),equalTo("Cannot remove alternatives"));
		}
	}

	@Test
	public void emptyAlternativesHasCustomToStringRepresentation() throws Exception {
		assertThat(empty().toString(),equalTo("ImmutableAlternatives{entries=[]}"));
	}

	@Test
	public void populatedAlternativesHasCustomToStringRepresentation() throws Exception {
		assertThat(populated().toString(),equalTo("ImmutableAlternatives{entries=[Entry{quality=0.1 [DEFINITE], alternative={0.200}}, Entry{quality=0.3 [DEFINITE], alternative={0.400}}]}"));
	}

	private ImmutableAlternatives empty() {
		return ImmutableAlternatives.builder().build();
	}

	private ImmutableAlternatives populated() {
		return
			ImmutableAlternatives.
				builder().
					add(Q1,ALT1).
					add(Q2,ALT2).
					build();
	}

}
