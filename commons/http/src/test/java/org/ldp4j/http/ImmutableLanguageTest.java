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
import static org.hamcrest.Matchers.nullValue;

import java.util.Locale;

import org.junit.Test;


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
