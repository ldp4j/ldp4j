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
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class ImmutableVariantTest {

	private static final CharacterEncoding UTF_8 = CharacterEncodings.of(StandardCharsets.UTF_8);
	private static final MediaType APPLICATION_XML = MediaTypes.of("application","xml");
	private static final Language ENGLISH = Languages.of(Locale.ENGLISH);

	@Test
	public void charsetLessMediaTypeOnlyFillsInVariantType() throws Exception {
		ImmutableVariant sut=
			ImmutableVariant.
				newInstance().
					type(APPLICATION_XML);
		assertThat(sut.type(),equalTo(APPLICATION_XML));
	}

	@Test
	public void mediaTypeWithCharsetFillsInVariantTypeAndCharset() throws Exception {
		ImmutableVariant sut=
			ImmutableVariant.
				newInstance().
					type(
						MediaTypes.
							from(APPLICATION_XML).
								withCharset(UTF_8.charset()).
								build());
		assertThat(sut.type(),equalTo(APPLICATION_XML));
		assertThat(sut.charset(),equalTo(UTF_8));
	}

	@Test
	public void nullMediaTypeClearsVariantType() throws Exception {
		ImmutableVariant sut=
			ImmutableVariant.
				newInstance().
					type(APPLICATION_XML).
					type(null);
		assertThat(sut.type(),nullValue());
	}

	@Test
	public void nullCharacterEncodingClearsVariantCharset() throws Exception {
		ImmutableVariant sut=
			ImmutableVariant.
				newInstance().
					charset(UTF_8).
					charset(null);
		assertThat(sut.charset(),nullValue());
	}

	@Test
	public void nullLanguageClearsVariantLanguage() throws Exception {
		ImmutableVariant sut=
			ImmutableVariant.
				newInstance().
					language(Languages.of(Locale.ENGLISH)).
					language(null);
		assertThat(sut.language(),nullValue());
	}

	@Test
	public void wildcardMediaTypesAreNotSupported() throws Exception {
		try {
			ImmutableVariant.newInstance().type(MediaTypes.wildcard());
			fail("Should not accept a wildcard media type");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Type cannot be a wildcard media type"));
		}
	}

	@Test
	public void wildcardCharacterEncodingsAreNotSupported() throws Exception {
		try {
			ImmutableVariant.newInstance().charset(CharacterEncodings.wildcard());
			fail("Should not accept a wildcard character encoding");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Charset cannot be a wildcard character encoding"));
		}
	}

	@Test
	public void wildcardLanguagesAreNotSupported() throws Exception {
		try {
			ImmutableVariant.newInstance().language(Languages.wildcard());
			fail("Should not accept a wildcard language");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),startsWith("Language cannot be a wildcard language"));
		}
	}

	@Test
	public void doesNotCloneNull() throws Exception {
		ImmutableVariant original = null;
		ImmutableVariant copy = ImmutableVariant.copyOf(original);
		assertThat(copy,sameInstance(original));
	}

	@Test
	public void copyDoesNotCloneImmutableInstances() throws Exception {
		ImmutableVariant original = ImmutableVariant.newInstance().type(APPLICATION_XML).charset(UTF_8).language(ENGLISH);
		ImmutableVariant copy = ImmutableVariant.copyOf(original);
		assertThat(copy,sameInstance(original));
	}

	@Test
	public void copyClonesAllVariantComponents(@Mocked final Variant original) throws Exception {
		new Expectations() {{
			original.type();result=APPLICATION_XML;
			original.charset();result=UTF_8;
			original.language();result=ENGLISH;
		}};
		ImmutableVariant copy = ImmutableVariant.copyOf(original);
		assertThat(copy,not(sameInstance(original)));
		assertThat(copy.type(),equalTo(APPLICATION_XML));
		assertThat(copy.charset(),equalTo(UTF_8));
		assertThat(copy.language(),equalTo(ENGLISH));
	}

}
