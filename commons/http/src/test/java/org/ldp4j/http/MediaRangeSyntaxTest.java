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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import org.junit.Test;


public class MediaRangeSyntaxTest {

	@Test
	public void syntaxesAreDiscoverable() {
		for(MediaRangeSyntax sut:MediaRangeSyntax.values()) {
			assertThat(MediaRangeSyntax.valueOf(sut.name()),equalTo(sut));
		}
	}

	@Test
	public void doesNotAcceptNullType() throws Exception {
		for(MediaRangeSyntax sut:MediaRangeSyntax.values()) {
			try {
				sut.checkType(null);
				fail("Should fail for null type");
			} catch (IllegalArgumentException e) {
				assertThat(e.getMessage(),equalTo("Type cannot be null"));
			}
		}
	}

	@Test
	public void doesNotAcceptEmptyType() throws Exception {
		for(MediaRangeSyntax sut:MediaRangeSyntax.values()) {
			try {
				sut.checkType("");
				fail("Should fail for empty type");
			} catch (IllegalArgumentException e) {
				assertThat(e.getMessage(),equalTo("Type cannot be empty"));
			}
		}
	}

	@Test
	public void doesNotAcceptNullSubtype() throws Exception {
		for(MediaRangeSyntax sut:MediaRangeSyntax.values()) {
			try {
				sut.checkSubType(null);
				fail("Should fail for null subtype");
			} catch (IllegalArgumentException e) {
				assertThat(e.getMessage(),equalTo("Subtype cannot be null"));
			}
		}
	}

	@Test
	public void doesNotAcceptEmptySubtype() throws Exception {
		for(MediaRangeSyntax sut:MediaRangeSyntax.values()) {
			try {
				sut.checkSubType("");
				fail("Should fail for empty subtype");
			} catch (IllegalArgumentException e) {
				assertThat(e.getMessage(),equalTo("Subtype cannot be empty"));
			}
		}
	}

	@Test
	public void acceptsNullSuffix() throws Exception {
		for(MediaRangeSyntax sut:MediaRangeSyntax.values()) {
			assertThat(sut.checkSuffix(null),nullValue());
		}
	}

	@Test
	public void acceptsEmptySuffix() throws Exception {
		for(MediaRangeSyntax sut:MediaRangeSyntax.values()) {
			assertThat(sut.checkSuffix(""),equalTo(""));
		}
	}

	@Test
	public void suffixCannotHavePlus() throws Exception {
		for(MediaRangeSyntax sut:MediaRangeSyntax.values()) {
			try {
				assertThat(sut.checkSuffix("suffix+value"),equalTo(""));
				fail("Should fail if suffix contains '+'");
			} catch (IllegalArgumentException e) {
				assertThat(e.getMessage(),equalTo("Invalid character '+' in suffix 'suffix+value' at 6"));
			}
		}
	}

}
