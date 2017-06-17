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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LiteralAdapterTest {

	@Test
	public void testVisitLiteral$valid() throws Exception {
		LiteralAdapter<Number> nAdapter = LiteralAdapter.newInstance(Number.class);
		nAdapter.visitLiteral(Literals.newLiteral(1));
		assertThat(nAdapter.adaptedValue(),equalTo((Number)1));
	}

	@Test
	public void testVisitTypedLiteral$valid() throws Exception {
		LiteralAdapter<Number> nAdapter = LiteralAdapter.newInstance(Number.class);
		nAdapter.visitTypedLiteral(Literals.newTypedLiteral(1,Datatypes.STRING));
		assertThat(nAdapter.adaptedValue(),equalTo((Number)1));
	}

	@Test
	public void testVisitLanguageLiteral$valid() throws Exception {
		LiteralAdapter<String> nAdapter = LiteralAdapter.newInstance(String.class);
		nAdapter.visitLanguageLiteral(Literals.newLanguageLiteral("1","en"));
		assertThat(nAdapter.adaptedValue(),equalTo("1"));
	}

	@Test
	public void testVisitLanguageLiteral$notValid() throws Exception {
		LiteralAdapter<Number> nAdapter = LiteralAdapter.newInstance(Number.class);
		nAdapter.visitLiteral(Literals.newLanguageLiteral("1","en"));
		assertThat(nAdapter.adaptedValue(),equalTo(null));
	}

}
