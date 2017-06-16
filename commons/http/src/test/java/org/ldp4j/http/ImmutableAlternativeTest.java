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
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class ImmutableAlternativeTest {

	private static final MediaType APPLICATION_XML = MediaTypes.of("application","xml");
	private static final Language ENGLISH = Languages.of(Locale.ENGLISH);
	private static final CharacterEncoding UTF_8 = CharacterEncodings.of(StandardCharsets.UTF_8);

	@Test
	public void qualityCannotBeInvalid(@Mocked final ImmutableVariant variant) throws Exception {
		try {
			ImmutableAlternative.create(-1.0D,variant);
			fail("Should not accept invalid quality");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Quality cannot be negative (-1.0)"));
		}
	}

	@Test
	public void variantCannotBeNull() throws Exception {
		try {
			ImmutableAlternative.create(0.0D,null);
			fail("Should not accept null variants");
		} catch (NullPointerException e) {
			assertThat(e.getMessage(),equalTo("Variant cannot be null"));
		}
	}


	@Test
	public void hasFullCustomRepresentation() throws Exception {
		ImmutableVariant variant =
			ImmutableVariant.
				newInstance().
					charset(UTF_8).
					language(ENGLISH).
					type(APPLICATION_XML);
		ImmutableAlternative sut = ImmutableAlternative.create(0.123D,variant);
		assertThat(sut.toString(),equalTo("{0.123 {type application/xml} {charset utf-8} {language en}}"));
	}

	@Test
	public void hasPartialCustomRepresentation() throws Exception {
		ImmutableVariant variant =
			ImmutableVariant.
				newInstance().
					charset(UTF_8).
					type(APPLICATION_XML);
		ImmutableAlternative sut = ImmutableAlternative.create(0.123D,variant);
		assertThat(sut.toString(),equalTo("{0.123 {type application/xml} {charset utf-8}}"));
	}

	@Test
	public void doesNotCopyNullAlternatives() throws Exception {
		assertThat(ImmutableAlternative.copyOf(null),nullValue());
	}

	@Test
	public void doesNotCopyImmutableAlternatives(@Mocked final ImmutableAlternative alternative) throws Exception {
		assertThat(ImmutableAlternative.copyOf(alternative),sameInstance(alternative));
	}

	@Test
	public void copiesValidNonImmutableAlternatives(@Mocked final Alternative original) throws Exception {
		new Expectations() {{
			original.quality();result=1.0D;
			original.type();result=APPLICATION_XML;
			original.charset();result=UTF_8;
			original.language();result=ENGLISH;
		}};
		final ImmutableAlternative copy = ImmutableAlternative.copyOf(original);
		assertThat(copy,not(sameInstance(original)));
		assertThat(copy.quality(),equalTo(original.quality()));
		assertThat(copy.type(),equalTo(original.type()));
		assertThat(copy.charset(),equalTo(original.charset()));
		assertThat(copy.language(),equalTo(original.language()));
	}

}
