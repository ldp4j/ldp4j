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

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class ImmutableCharacterEncodingTest {

	@Test
	public void nullCharsetsAreUsedForWildcards() throws Exception {
		assertThat(new ImmutableCharacterEncoding(null).isWildcard(),equalTo(true));
	}

	@Test
	public void nonNullCharsetsAreNotWildcards() throws Exception {
		assertThat(new ImmutableCharacterEncoding(StandardCharsets.UTF_8).isWildcard(),equalTo(false));
	}

	@Test
	public void wildcardCharacterEncodingHasWildcardName() throws Exception {
		assertThat(new ImmutableCharacterEncoding(null).name(),equalTo("*"));
	}

	@Test
	public void regularCharacterEncodingHasNameOfTheCharset() throws Exception {
		assertThat(new ImmutableCharacterEncoding(StandardCharsets.UTF_8).name(),equalTo(StandardCharsets.UTF_8.name()));
	}

	@Test
	public void wildcardCharacterEncodingHasNullCharset() throws Exception {
		assertThat(new ImmutableCharacterEncoding(null).charset(),nullValue());
	}

	@Test
	public void regularCharacterEncodingExposesItCharset() throws Exception {
		assertThat(new ImmutableCharacterEncoding(StandardCharsets.UTF_8).charset(),equalTo(StandardCharsets.UTF_8));
	}

}
