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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;
import org.ldp4j.xml.QualifiedName;

public class QualifiedNameTest {

	@Test
	public void testParseURI() throws Exception {
		final QualifiedName parse = QualifiedName.parse("http://www.ldp4j.org");
		assertThat(parse,nullValue());
	}

	@Test
	public void testParse$cornerCase$startsWithColon() throws Exception {
		final QualifiedName parse = QualifiedName.parse(":type");
		assertThat(parse,nullValue());
	}

	@Test
	public void testParse$cornerCase$endsWithColon() throws Exception {
		final QualifiedName parse = QualifiedName.parse("rdfs:");
		assertThat(parse,nullValue());
	}

	@Test
	public void testParse$cornerCase$hasMultipleColons() throws Exception {
		final QualifiedName parse = QualifiedName.parse("urn:type:sha1");
		assertThat(parse,nullValue());
	}

	@Test
	public void testParse$prefixedValue() throws Exception {
		final QualifiedName parse = QualifiedName.parse("rdfs:type");
		assertThat(parse,notNullValue());
		assertThat(parse.isPrefixed(),equalTo(true));
		assertThat(parse.prefix(),equalTo("rdfs"));
		assertThat(parse.localPart(),equalTo("type"));
	}

	@Test
	public void testParse$onlyLocalPart() throws Exception {
		final QualifiedName parse = QualifiedName.parse("aBigEnoughLocalPart");
		assertThat(parse,notNullValue());
		assertThat(parse.isPrefixed(),equalTo(false));
		assertThat(parse.prefix(),nullValue());
		assertThat(parse.localPart(),equalTo("aBigEnoughLocalPart"));
	}

}
