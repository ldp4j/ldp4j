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

import java.util.Locale;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class ImmutableLanguageTest {

	@Test
	public void wildcardsAreWildcards() throws Exception {
		assertThat(wildcard().isWildcard(),equalTo(true));
	}

	@Test
	public void wildcardsDoNotHaveLocale() throws Exception {
		assertThat(wildcard().locale(),nullValue());
	}

	@Test
	public void wildcardsHaveAsteriskPrimaryTag() throws Exception {
		assertThat(wildcard().primaryTag(),equalTo("*"));
	}

	@Test
	public void wildcardsDoNotHaveSubTag() throws Exception {
		assertThat(wildcard().subTag(),nullValue());
	}

	@Test
	public void simpleLanguagesAreNotWildcards() throws Exception {
		assertThat(simpleLanguage().isWildcard(),equalTo(false));
	}

	@Test
	public void simpleLanguagesHaveLocale() throws Exception {
		assertThat(simpleLanguage().locale(),equalTo(simpleLocale()));
	}

	@Test
	public void simpleLanguagesHasLocaleLanguageAsPrimaryTag() throws Exception {
		assertThat(simpleLanguage().primaryTag(),equalTo(simpleLocale().getLanguage()));
	}

	@Test
	public void simpleLanguagesHaveEmptySubTag() throws Exception {
		assertThat(simpleLanguage().subTag(),equalTo(""));
	}

	@Test
	public void compositeLanguagesAreNotWildcards() throws Exception {
		assertThat(compositeLanguage().isWildcard(),equalTo(false));
	}

	@Test
	public void compositeLanguagesHaveLocale() throws Exception {
		assertThat(compositeLanguage().locale(),equalTo(compositeLocale()));
	}

	@Test
	public void compositeLanguagesHasLocaleLanguageAsPrimaryTag() throws Exception {
		assertThat(compositeLanguage().primaryTag(),equalTo(compositeLocale().getLanguage()));
	}

	@Test
	public void compositeLanguagesHasLocaleCountryAsSubTag() throws Exception {
		assertThat(compositeLanguage().subTag(),equalTo(compositeLocale().getCountry()));
	}

	@Test
	public void wildcardHasCustomToString() throws Exception {
		ImmutableLanguage sut = wildcard();
		assertThat(sut.toString(),equalTo("ImmutableLanguage{locale=*}"));
	}

	@Test
	public void wildcardHeaderRepresentationIsValid() throws Exception {
		ImmutableLanguage sut = wildcard();
		assertThat(sut.toHeader(),equalTo("*"));
	}

	@Test
	public void simpleLanguageHasCustomToString() throws Exception {
		ImmutableLanguage sut = simpleLanguage();
		assertThat(sut.toString(),equalTo("ImmutableLanguage{locale=en}"));
	}

	@Test
	public void simpleLanguageHeaderRepresentationIsValid() throws Exception {
		ImmutableLanguage sut = simpleLanguage();
		assertThat(sut.toHeader(),equalTo(simpleLocale().getLanguage()));
	}

	@Test
	public void compositeLanguageHasCustomToString() throws Exception {
		ImmutableLanguage sut = compositeLanguage();
		assertThat(sut.toString(),equalTo("ImmutableLanguage{locale=en-US}"));
	}

	@Test
	public void compositeLanguageHeaderRepresentationIsValid() throws Exception {
		ImmutableLanguage sut = compositeLanguage();
		assertThat(sut.toHeader(),equalTo("en-us"));
	}

	@Test
	public void languagesAreNotEqualToNonLanguageInstances() {
		assertThat((Object)simpleLanguage(),not(equalTo((Object)"String")));
	}

	@Test
	public void wildcardLanguagesAreEqualToThemselves() {
		assertThat(wildcard(),equalTo(wildcard()));
	}

	@Test
	public void wildcardLanguagesHaveSameHashCode() {
		assertThat(wildcard().hashCode(),equalTo(wildcard().hashCode()));
	}

	@Test
	public void simpleLanguagesAreEqualToThemselves() {
		assertThat(simpleLanguage(),equalTo(simpleLanguage()));
	}

	@Test
	public void equalSimpleLanguageHaveSameHashCode() {
		assertThat(simpleLanguage().hashCode(),equalTo(simpleLanguage().hashCode()));
	}

	@Test
	public void compositeLanguagesAreEqualToThemselves() {
		assertThat(compositeLanguage(),equalTo(compositeLanguage()));
	}

	@Test
	public void equalCompositeLanguageHaveSameHashCode() {
		assertThat(compositeLanguage().hashCode(),equalTo(compositeLanguage().hashCode()));
	}

	@Test
	public void simpleLanguagesAreNotEqualToWildcards() {
		assertThat(simpleLanguage(),not(equalTo(wildcard())));
	}

	@Test
	public void compositeLanguagesAreNotEqualToWildcards() {
		assertThat(compositeLanguage(),not(equalTo(wildcard())));
	}

	@Test
	public void languagesAreNotEqualToLanguagesWithDifferentLocale() {
		assertThat(simpleLanguage(),not(equalTo(compositeLanguage())));
	}

	@Test
	public void differentLanguagesHaveDifferentHashCode() {
		assertThat(simpleLanguage().hashCode(),not(equalTo(compositeLanguage().hashCode())));
	}

	@Test
	public void wildcardLanguagesAreNotEqualToLangualesWithLocale() {
		assertThat(wildcard(),not(equalTo(simpleLanguage())));
	}

	@Test
	public void doesNotCloneNull() throws Exception {
		ImmutableLanguage original = null;
		ImmutableLanguage copy = ImmutableLanguage.copyOf(original);
		assertThat(copy,sameInstance(original));
	}

	@Test
	public void copyDoesNotCloneImmutableInstances() throws Exception {
		ImmutableLanguage original = simpleLanguage();
		ImmutableLanguage copy = ImmutableLanguage.copyOf(original);
		assertThat(copy,sameInstance(original));
	}

	@Test
	public void copyClonesAllLanguageComponents(@Mocked final Language original) throws Exception {
		new Expectations() {{
			original.locale();result=simpleLocale();
		}};
		ImmutableLanguage copy = ImmutableLanguage.copyOf(original);
		assertThat(copy,not(sameInstance(original)));
		assertThat(copy.locale(),equalTo(simpleLocale()));
	}

	private ImmutableLanguage wildcard() {
		return new ImmutableLanguage(null);
	}

	private ImmutableLanguage simpleLanguage() {
		return new ImmutableLanguage(simpleLocale());
	}

	private ImmutableLanguage compositeLanguage() {
		return new ImmutableLanguage(compositeLocale());
	}

	private Locale simpleLocale() {
		return Locale.ENGLISH;
	}

	private Locale compositeLocale() {
		return Locale.US;
	}

}
