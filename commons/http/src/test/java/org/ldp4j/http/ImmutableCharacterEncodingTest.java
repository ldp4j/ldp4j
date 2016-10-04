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

import java.nio.charset.StandardCharsets;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class ImmutableCharacterEncodingTest {

	@Test
	public void nullCharsetsAreUsedForWildcards() throws Exception {
		assertThat(wildcard().isWildcard(),equalTo(true));
	}

	@Test
	public void nonNullCharsetsAreUsedForRegularCharacteEncodings() throws Exception {
		assertThat(regular().isWildcard(),equalTo(false));
	}

	@Test
	public void wildcardCharacterEncodingHasWildcardName() throws Exception {
		assertThat(wildcard().name(),equalTo("*"));
	}

	@Test
	public void regularCharacterEncodingHasNameOfTheCharset() throws Exception {
		assertThat(regular().name(),equalTo(StandardCharsets.UTF_8.name()));
	}

	@Test
	public void wildcardCharacterEncodingHasNullCharset() throws Exception {
		assertThat(wildcard().charset(),nullValue());
	}

	@Test
	public void regularCharacterEncodingExposesItCharset() throws Exception {
		assertThat(regular().charset(),equalTo(StandardCharsets.UTF_8));
	}

	@Test
	public void wildcardCharacterEncodingHeaderRepresentationIsValid() throws Exception {
		ImmutableCharacterEncoding sut = wildcard();
		assertThat(sut.toHeader(),equalTo("*"));
	}

	@Test
	public void regularCharacterEncodingHeaderRepresentationIsValid() throws Exception {
		ImmutableCharacterEncoding sut = regular();
		assertThat(sut.toHeader(),equalTo("utf-8"));
	}

	@Test
	public void wildcardCharacterEncodingHasCustomToString() throws Exception {
		ImmutableCharacterEncoding sut = wildcard();
		assertThat(sut.toString(),equalTo("ImmutableCharacterEncoding{charset=*}"));
	}

	@Test
	public void regularCharacterEncodingHasCustomToString() throws Exception {
		ImmutableCharacterEncoding sut = regular();
		assertThat(sut.toString(),equalTo("ImmutableCharacterEncoding{charset=UTF-8}"));
	}

	@Test
	public void characterEncodingsAreNotEqualToNonCharacterEncodingInstances() {
		assertThat((Object)regular(),not(equalTo((Object)"String")));
	}

	@Test
	public void wildcardCharacterEncodingsAreEqualToThemselves() {
		assertThat(wildcard(),equalTo(wildcard()));
	}

	@Test
	public void wildcardCharacterEncodingsHaveSameHashCode() {
		assertThat(wildcard().hashCode(),equalTo(wildcard().hashCode()));
	}

	@Test
	public void regularCharacterEncodingsAreEqualToThemselves() {
		assertThat(regular(),equalTo(regular()));
	}

	@Test
	public void equalRegularCharacterEncodingsHaveSameHashCode() {
		assertThat(regular().hashCode(),equalTo(regular().hashCode()));
	}

	@Test
	public void regularCharacterEncodingsAreNotEqualToWildcards() {
		assertThat(regular(),not(equalTo(wildcard())));
	}

	@Test
	public void regularCharacterEncodingsAreNotEqualToCharacterEncodingsWithDifferentCharset() {
		assertThat(regular(),not(equalTo(alternative())));
	}

	@Test
	public void differentRegularCharacterEncodingsHaveDifferentHashCode() {
		assertThat(regular().hashCode(),not(equalTo(alternative().hashCode())));
	}

	@Test
	public void wildcardCharacterEncodingsAreNotEqualToRegular() {
		assertThat(wildcard(),not(equalTo(regular())));
	}

	@Test
	public void doesNotCloneNull() throws Exception {
		ImmutableLanguage original = null;
		ImmutableLanguage copy = ImmutableLanguage.copyOf(original);
		assertThat(copy,sameInstance(original));
	}

	@Test
	public void copyDoesNotCloneImmutableInstances() throws Exception {
		ImmutableCharacterEncoding original = regular();
		ImmutableCharacterEncoding copy = ImmutableCharacterEncoding.copyOf(original);
		assertThat(copy,sameInstance(original));
	}

	@Test
	public void copyClonesAllCharacterEncodingComponents(@Mocked final CharacterEncoding original) throws Exception {
		new Expectations() {{
			original.charset();result=regular().charset();
		}};
		ImmutableCharacterEncoding copy = ImmutableCharacterEncoding.copyOf(original);
		assertThat(copy,not(sameInstance(original)));
		assertThat(copy.charset(),equalTo(regular().charset()));
	}

	private ImmutableCharacterEncoding wildcard() {
		return new ImmutableCharacterEncoding(null);
	}

	private ImmutableCharacterEncoding regular() {
		return new ImmutableCharacterEncoding(StandardCharsets.UTF_8);
	}

	private ImmutableCharacterEncoding alternative() {
		return new ImmutableCharacterEncoding(StandardCharsets.US_ASCII);
	}

}
