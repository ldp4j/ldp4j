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
import java.util.Comparator;

import org.junit.Test;

public class CharacterEncodingComparatorTest extends AbstractComparatorTest<CharacterEncoding> {

	private static final CharacterEncoding WILDCARD = CharacterEncodings.wildcard();
	private static final CharacterEncoding US_ASCII = CharacterEncodings.of(StandardCharsets.US_ASCII);
	private static final CharacterEncoding UTF_8 = CharacterEncodings.of(StandardCharsets.UTF_8);

	@Override
	protected Comparator<CharacterEncoding> sut() {
		return CharacterEncodingComparator.INSTANCE;
	}

	@Test
	public void wildcardIsGreaterThanAnything() throws Exception {
		assertIsGreaterThan(WILDCARD, UTF_8);
	}

	@Test
	public void regularCharacterEncodingsAreLowerThanWildcardEncodings() throws Exception {
		assertIsLowerThan(UTF_8,WILDCARD);
	}

	@Test
	public void regularCharacterEncodingsOfSameCharsetAreEqual() throws Exception {
		assertIsEqualTo(UTF_8, UTF_8);
	}

	@Test
	public void comparisonOfRegularCharacterEncodingsOfDifferentCharsetDependsOnCharsetOrdering() throws Exception {
		assertIsGreaterThan(UTF_8,US_ASCII);
		assertIsLowerThan(US_ASCII,UTF_8);
	}

}
