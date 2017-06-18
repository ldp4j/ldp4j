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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	DoubleUtilsTest.class,
	MoreStringsTest.class,
	MoreCollectionsTest.class,
	InvalidTokenExceptionTest.class,
	HttpUtilsTest.class,
	ParameterTest.class,
	HeaderPartIteratorTest.class,
	CaseInsensitiveMapTest.class,
	RFC6838MediaRangeValidatorTest.class,
	MediaRangeSyntaxTest.class,
	ImmutableMediaTypeTest.class,
	ImmutableLanguageTest.class,
	ImmutableCharacterEncodingTest.class,
	MediaTypesTest.class,
	MediaTypeComparatorTest.class,
	LanguagesTest.class,
	LanguageComparatorTest.class,
	CharacterEncodingsTest.class,
	CharacterEncodingComparatorTest.class,
	WeightedTest.class,
	WeightedComparatorTest.class,
	ContentNegotiationUtilsTest.class,
	ImmutableVariantTest.class,
	ImmutableQualityTest.class,
	QualitiesTest.class,
	ImmutableAlternativeTest.class,
	ImmutableAlternativesTest.class,
	AlternativeEvaluationTest.class,
	AlternativeProviderTest.class,
	AlternativeComparatorTest.class,
	VariantsTest.class,
	ImmutableNegotiationResultTest.class,
	ContentNegotiatorTest.class,
	ContentNegotiationTest.class,
})
public class HttpUnitTestSuite {
}
