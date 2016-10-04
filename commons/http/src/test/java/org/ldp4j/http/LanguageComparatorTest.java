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

import java.util.Comparator;
import java.util.Locale;

import org.junit.Test;

public class LanguageComparatorTest extends AbstractComparatorTest<Language> {

	private static final Language WILDCARD = Languages.wildcard();
	private static final Language GERMAN=Languages.of(Locale.GERMAN);
	private static final Language ENGLISH=Languages.of(Locale.ENGLISH);
	private static final Language US_ENGLISH=Languages.of(Locale.US);
	private static final Language UK_ENGLISH=Languages.of(Locale.UK);

	@Override
	protected Comparator<Language> sut() {
		return LanguageComparator.INSTANCE;
	}

	@Test
	public void wildcardIsEqualToWildcard() throws Exception {
		assertIsEqualTo(WILDCARD, WILDCARD);
	}

	@Test
	public void wildcardIsGreaterThanAnythingExceptWildcard() throws Exception {
		assertIsGreaterThan(WILDCARD, ENGLISH);
	}

	@Test
	public void regularLanguagesAreLowerThanWildcardLanguage() throws Exception {
		assertIsLowerThan(ENGLISH,WILDCARD);
	}

	@Test
	public void simpleLanguagesOfSameLocaleAreEqual() throws Exception {
		assertIsEqualTo(ENGLISH, ENGLISH);
	}

	@Test
	public void compositeLanguagesOfSameLocaleAreEqual() throws Exception {
		assertIsEqualTo(US_ENGLISH, US_ENGLISH);
	}

	@Test
	public void simpleLanguageIsGreaterThanCompositeLanguageIfTheyHaveTheSamePrimaryTag() throws Exception {
		assertIsGreaterThan(ENGLISH,US_ENGLISH);
	}

	@Test
	public void compositeLanguageIsLowerThanSimpleLanguageIfTheyHaveTheSamePrimaryTag() throws Exception {
		assertIsLowerThan(US_ENGLISH,ENGLISH);
	}

	@Test
	public void comparisonOfRegularLanguagesDependsPrimaryTagOrdering() throws Exception {
		assertIsGreaterThan(ENGLISH, GERMAN);
	}

	@Test
	public void comparisonOfRegularLanguagesDependsSubTagOrdering() throws Exception {
		assertIsGreaterThan(US_ENGLISH, UK_ENGLISH);
	}

}
