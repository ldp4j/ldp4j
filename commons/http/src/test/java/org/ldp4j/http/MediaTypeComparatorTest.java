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

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class MediaTypeComparatorTest extends AbstractComparatorTest<MediaType> {

	private static final MediaType WILDCARD = MediaTypes.wildcard();
	private static final MediaType TEXT_FAMILY = MediaTypes.of("text","*");
	private static final MediaType TEXT_PLAIN = MediaTypes.of("text","plain");
	private static final MediaType TEXT_TURTLE = MediaTypes.of("text","turtle");
	private static final MediaType APPLICATION_XML = MediaTypes.of("application","xml");

	private static final MediaType RDF_XML = MediaTypes.of("application","rdf","xml");
	private static final MediaType RDF_THRIFT = MediaTypes.of("application","rdf","thrift");

	private static final MediaType TEXT_PLAIN_WITH_FORMAT_FLOW = MediaTypes.from(TEXT_PLAIN).withParam("format","flowed").build();
	private static final MediaType TEXT_PLAIN_WITH_FORMAT_STRICT = MediaTypes.from(TEXT_PLAIN).withParam("format","strict").build();

	private static final MediaType MULTIPLE_PARAMS_NO_CHARSET = MediaTypes.from(TEXT_PLAIN).withParam("p1","v1").withParam("p2", "v2").build();
	private static final MediaType MULTIPLE_PARAMS_NO_CHARSET_ALTERNATIVE_PARAM = MediaTypes.from(TEXT_PLAIN).withParam("p3","v1").withParam("p2", "v2").build();
	private static final MediaType MULTIPLE_PARAMS_NO_CHARSET_ALTERNATIVE_VALUE = MediaTypes.from(TEXT_PLAIN).withParam("p1","v1").withParam("p2", "v3").build();
	private static final MediaType MULTIPLE_PARAMS_NO_CHARSET_ALTERNATIVE_ORDERING = MediaTypes.from(TEXT_PLAIN).withParam("p2","v2").withParam("p1","v1").build();

	private static final MediaType TEXT_PLAIN_UTF_8 = MediaTypes.from(TEXT_PLAIN).withCharset(StandardCharsets.UTF_8).build();
	private static final MediaType TEXT_PLAIN_US_ASCII = MediaTypes.from(TEXT_PLAIN).withCharset(StandardCharsets.US_ASCII).build();

	@Override
	protected MediaTypeComparator sut() {
		return MediaTypeComparator.INSTANCE;
	}

	@Test
	public void wildcardIsEqualToWildcard() throws Exception {
		assertIsEqualTo(WILDCARD, WILDCARD);
	}

	@Test
	public void wildcardTypeIsGreaterThanNonWildcardType() throws Exception {
		assertIsGreaterThan(WILDCARD, TEXT_FAMILY);
	}

	@Test
	public void nonWildcardTypeIsLowerThanWildcardType() throws Exception {
		assertIsLowerThan(TEXT_FAMILY, WILDCARD);
	}

	@Test
	public void nonWildcardTypeOfDifferentFamiliesAreEqual() throws Exception {
		assertIsEqualTo(TEXT_PLAIN, APPLICATION_XML);
	}

	@Test
	public void wildcardSubTypeIsGreaterThanNonWildcardSubTypeOfSameFamily() throws Exception {
		assertIsGreaterThan(TEXT_FAMILY, TEXT_PLAIN);
	}

	@Test
	public void nonWildcardTypeIsLowerThanWildcardSubTypeOfTheSameFamily() throws Exception {
		assertIsLowerThan(TEXT_PLAIN, TEXT_FAMILY);
	}

	@Test
	public void nonStructuredMediaTypesOfTheSameFamilyButDifferentSubtypeAreEqual() throws Exception {
		assertIsEqualTo(TEXT_PLAIN, TEXT_TURTLE);
	}

	@Test
	public void structuredMediaTypesOfTheSameFamilyAndSubTypeButDifferentSyntaxAreEqual() throws Exception {
		assertIsEqualTo(RDF_XML, RDF_THRIFT);
	}

	@Test
	public void mediaTypesWithParametersIsLowerThanMediaTypeOfTheSameMediaRangeWithLessParameters() throws Exception {
		assertIsLowerThan(TEXT_PLAIN_WITH_FORMAT_FLOW,TEXT_PLAIN);
	}

	@Test
	public void mediaTypesWithoutParametersGreaterThanMediaTypeOfTheSameMediaRangeWithParameters() throws Exception {
		assertIsGreaterThan(TEXT_PLAIN,TEXT_PLAIN_WITH_FORMAT_FLOW);
	}

	@Test
	public void mediaTypesOrderingDependsOnParameterValues() throws Exception {
		assertIsGreaterThan(TEXT_PLAIN_WITH_FORMAT_STRICT,TEXT_PLAIN_WITH_FORMAT_FLOW);
		assertIsLowerThan(TEXT_PLAIN_WITH_FORMAT_FLOW,TEXT_PLAIN_WITH_FORMAT_STRICT);
	}

	@Test
	public void mediaTypesOfSameMediaRangeAndWithSameParametersAreEqualDespiteTheParameterOrder() {
		assertIsEqualTo(MULTIPLE_PARAMS_NO_CHARSET, MULTIPLE_PARAMS_NO_CHARSET_ALTERNATIVE_ORDERING);
		assertIsEqualTo(MULTIPLE_PARAMS_NO_CHARSET_ALTERNATIVE_ORDERING, MULTIPLE_PARAMS_NO_CHARSET);
	}

	@Test
	public void mediaTypesOfSameMediaRangeAndWithDifferentNamesAreDifferent() {
		assertIsLowerThan(MULTIPLE_PARAMS_NO_CHARSET, MULTIPLE_PARAMS_NO_CHARSET_ALTERNATIVE_PARAM);
		assertIsGreaterThan(MULTIPLE_PARAMS_NO_CHARSET_ALTERNATIVE_PARAM, MULTIPLE_PARAMS_NO_CHARSET);
	}

	@Test
	public void mediaTypesOfSameMediaRangeAndWithSameParameterNamesButDifferentValuesAreDifferent() {
		assertIsLowerThan(MULTIPLE_PARAMS_NO_CHARSET, MULTIPLE_PARAMS_NO_CHARSET_ALTERNATIVE_VALUE);
		assertIsGreaterThan(MULTIPLE_PARAMS_NO_CHARSET_ALTERNATIVE_VALUE, MULTIPLE_PARAMS_NO_CHARSET);
	}

	@Test
	public void mediaTypesOfSameMediaRangeAndSameCharsetDespiteTheNameUsedInTheParameterAreEqual() {
		assertIsEqualTo(TEXT_PLAIN_US_ASCII,MediaTypes.fromString("text/plain;charset=\"US-ASCII\""));
	}

	@Test
	public void mediaTypesOfSameMediaRangeAndWithDifferentCharsetDifferent() {
		assertIsLowerThan(TEXT_PLAIN_US_ASCII,TEXT_PLAIN_UTF_8);
		assertIsGreaterThan(TEXT_PLAIN_UTF_8,TEXT_PLAIN_US_ASCII);
	}

}
