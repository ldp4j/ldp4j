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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.google.common.collect.Lists;

public class AlternativeProviderTest {

	private static final Language US_ENGLISH = Languages.of(Locale.US);
	private static final Language ENGLISH = Languages.of(Locale.ENGLISH);
	private static final CharacterEncoding UTF_16BE = CharacterEncodings.of(StandardCharsets.UTF_16BE);
	private static final CharacterEncoding UTF_16LE = CharacterEncodings.of(StandardCharsets.UTF_16LE);
	private static final CharacterEncoding UTF_16 = CharacterEncodings.of(StandardCharsets.UTF_16);
	private static final CharacterEncoding US_ASCII = CharacterEncodings.of(StandardCharsets.US_ASCII);
	private static final CharacterEncoding ISO_8859_1 = CharacterEncodings.of(StandardCharsets.ISO_8859_1);
	private static final CharacterEncoding UTF_8 = CharacterEncodings.of(StandardCharsets.UTF_8);

	private static final MediaType LD_JSON = MediaTypes.of("application","ld","json");
	private static final MediaType RDF_XML = MediaTypes.of("application","rdf","xml");
	private static final MediaType TURTLE = MediaTypes.of("text","turtle");

	private static final MediaType TURTLE_UTF8 =
			MediaTypes.
				from(TURTLE).
					withCharset(StandardCharsets.UTF_8).
					build();

	@Test
	public void removeIsNotSupported() throws Exception {
		AlternativeProvider sut=
			new AlternativeProvider(
				Lists.<MediaType>newArrayList(),
				Lists.<CharacterEncoding>newArrayList(),
				Lists.<Language>newArrayList());
		try {
			sut.remove();
			fail("Should not support remove");
		} catch (final UnsupportedOperationException e) {
			// Nothing to test
		}
	}

	@Test
	public void cannotIterateOverEmptyComponents() throws Exception {
		AlternativeProvider sut=
			new AlternativeProvider(
				Lists.<MediaType>newArrayList(),
				Lists.<CharacterEncoding>newArrayList(),
				Lists.<Language>newArrayList());
		assertThat(sut.hasNext(),equalTo(false));
	}

	@Test
	public void failsToIterateOverEmptyComponents() throws Exception {
		AlternativeProvider sut=
			new AlternativeProvider(
				Lists.<MediaType>newArrayList(),
				Lists.<CharacterEncoding>newArrayList(),
				Lists.<Language>newArrayList());
		try {
			sut.next();
			fail("Should fail to iterate over the end");
		} catch (NoSuchElementException e) {
			assertThat(e.getMessage(),equalTo("No more alternatives available"));
		}
	}

	@Test
	public void canIterateOverMediaTypes() throws Exception {
		AlternativeProvider sut=
			new AlternativeProvider(
				Lists.<MediaType>newArrayList(
					TURTLE,
					RDF_XML,
					LD_JSON
				),
				Lists.<CharacterEncoding>newArrayList(),
				Lists.<Language>newArrayList());
		double w1 = verifyAlternative(sut.next(), TURTLE);
		double w2 = verifyAlternative(sut.next(), RDF_XML);
		double w3 = verifyAlternative(sut.next(), LD_JSON);
		assertThat(w1,greaterThan(w2));
		assertThat(w2,greaterThan(w3));
		assertThat(sut.hasNext(),equalTo(false));
	}

	@Test
	public void canIterateOverLanguages() throws Exception {
		AlternativeProvider sut=
			new AlternativeProvider(
				Lists.<MediaType>newArrayList(),
				Lists.<CharacterEncoding>newArrayList(),
				Lists.<Language>newArrayList(
					ENGLISH,
					US_ENGLISH));
		double w1 = verifyAlternative(sut.next(), ENGLISH);
		double w2 = verifyAlternative(sut.next(), US_ENGLISH);
		assertThat(w1,greaterThan(w2));
		assertThat(sut.hasNext(),equalTo(false));
	}

	@Test
	public void canIterateOverCharacterEncodings() throws Exception {
		AlternativeProvider sut=
			new AlternativeProvider(
				Lists.<MediaType>newArrayList(),
				Lists.<CharacterEncoding>newArrayList(
					UTF_8,
					ISO_8859_1,
					US_ASCII,
					UTF_16,
					UTF_16LE,
					UTF_16BE
				),
				Lists.<Language>newArrayList());
		double w1 = verifyAlternative(sut.next(), UTF_8);
		double w2 = verifyAlternative(sut.next(), ISO_8859_1);
		double w3 = verifyAlternative(sut.next(), US_ASCII);
		double w4 = verifyAlternative(sut.next(), UTF_16);
		double w5 = verifyAlternative(sut.next(), UTF_16LE);
		double w6 = verifyAlternative(sut.next(), UTF_16BE);
		assertThat(w1,greaterThan(w2));
		assertThat(w2,greaterThan(w3));
		assertThat(w3,greaterThan(w4));
		assertThat(w4,greaterThan(w5));
		assertThat(w5,greaterThan(w6));
		assertThat(sut.hasNext(),equalTo(false));
	}

	@Test
	public void canIterateOverSimpleCombination() throws Exception {
		AlternativeProvider sut=
			new AlternativeProvider(
				Lists.<MediaType>newArrayList(
					TURTLE,
					RDF_XML,
					LD_JSON
				),
				Lists.<CharacterEncoding>newArrayList(
					UTF_8,
					ISO_8859_1,
					US_ASCII,
					UTF_16,
					UTF_16LE,
					UTF_16BE
				),
				Lists.<Language>newArrayList(
					ENGLISH,
					US_ENGLISH
				));
		double previous=Double.MAX_VALUE;
		for(int i=0;i<3;i++) {
			for(int j=0;j<6;j++) {
				for(int k=0;k<2;k++) {
					final Alternative alternative=sut.next();
					assertThat(alternative.type(),notNullValue());
					assertThat(alternative.charset(),notNullValue());
					assertThat(alternative.language(),notNullValue());
					assertThat(previous,greaterThanOrEqualTo(alternative.quality()));
					previous=alternative.quality();
				}
			}
		}
		assertThat(sut.hasNext(),equalTo(false));
	}

	@Test
	public void canIterateOverComplexCombination() throws Exception {
		AlternativeProvider sut=
			new AlternativeProvider(
				Lists.<MediaType>newArrayList(
					TURTLE_UTF8,
					RDF_XML,
					LD_JSON
				),
				Lists.<CharacterEncoding>newArrayList(
					ISO_8859_1,
					US_ASCII
				),
				Lists.<Language>newArrayList(
					ENGLISH,
					US_ENGLISH
				));
		double w0 =Double.MAX_VALUE;
		double w1 =verifyAlternative(sut.next(),TURTLE,UTF_8,ENGLISH);
		double w2 =verifyAlternative(sut.next(),TURTLE,UTF_8,US_ENGLISH);
		double w3 =verifyAlternative(sut.next(),RDF_XML,ISO_8859_1,ENGLISH);
		double w4 =verifyAlternative(sut.next(),RDF_XML,ISO_8859_1,US_ENGLISH);
		double w5 =verifyAlternative(sut.next(),RDF_XML,US_ASCII,ENGLISH);
		double w6 =verifyAlternative(sut.next(),RDF_XML,US_ASCII,US_ENGLISH);
		double w7 =verifyAlternative(sut.next(),LD_JSON,ISO_8859_1,ENGLISH);
		double w8 =verifyAlternative(sut.next(),LD_JSON,ISO_8859_1,US_ENGLISH);
		double w9 =verifyAlternative(sut.next(),LD_JSON,US_ASCII,ENGLISH);
		double w10=verifyAlternative(sut.next(),LD_JSON,US_ASCII,US_ENGLISH);
		assertThat(w0,greaterThanOrEqualTo(w1));
		assertThat(w1,greaterThanOrEqualTo(w2));
		assertThat(w2,greaterThanOrEqualTo(w3));
		assertThat(w3,greaterThanOrEqualTo(w4));
		assertThat(w4,greaterThanOrEqualTo(w5));
		assertThat(w5,greaterThanOrEqualTo(w6));
		assertThat(w6,greaterThanOrEqualTo(w7));
		assertThat(w7,greaterThanOrEqualTo(w8));
		assertThat(w8,greaterThanOrEqualTo(w9));
		assertThat(w9,greaterThanOrEqualTo(w10));
		assertThat(sut.hasNext(),equalTo(false));
	}

	private void checkMediaType(Alternative alternative, MediaType expected) {
		assertThat(alternative.type(),equalTo(expected));
	}

	private void checkCharacterEncoding(Alternative alternative, CharacterEncoding expected) {
		assertThat(alternative.charset(),equalTo(expected));
	}

	private void checkLanguage(Alternative alternative, Language expected) {
		assertThat(alternative.language(),equalTo(expected));
	}

	private double checkWeight(Alternative alternative) {
		double weight = alternative.quality();
		assertThat(weight,greaterThan(0D));
		assertThat(weight,not(greaterThan(1D)));
		return weight;
	}

	private double verifyAlternative(Alternative alternative, MediaType mediaType, CharacterEncoding characterEncoding, Language language) {
		checkMediaType(alternative, mediaType);
		checkCharacterEncoding(alternative, characterEncoding);
		checkLanguage(alternative, language);
		return checkWeight(alternative);
	}

	private double verifyAlternative(Alternative alternative, MediaType expected) {
		checkMediaType(alternative, expected);
		assertThat(alternative.charset(),nullValue());
		assertThat(alternative.language(),nullValue());
		return checkWeight(alternative);
	}

	private double verifyAlternative(Alternative alternative, CharacterEncoding expected) {
		assertThat(alternative.type(),nullValue());
		checkCharacterEncoding(alternative, expected);
		assertThat(alternative.language(),nullValue());
		return checkWeight(alternative);
	}

	private double verifyAlternative(Alternative alternative, Language expected) {
		assertThat(alternative.type(),nullValue());
		assertThat(alternative.charset(),nullValue());
		checkLanguage(alternative, expected);
		return checkWeight(alternative);
	}

}
