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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.vocabulary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class AbstractImmutableVocabularyTest {

	static class V extends AbstractImmutableVocabulary<ImmutableTerm> {
		private static final long serialVersionUID = 6320487110172330521L;
		V() {
			super(ImmutableTerm.class,"namespace","prefix");
		}
	}


	@Test
	public void testValueOf() throws Exception {
		Class<?> enumClass=
			Thread.
				currentThread().
					getContextClassLoader().
						loadClass(AbstractImmutableVocabulary.class.getName()+"$Status");
		exerciseEnumerationGeneratedCode(enumClass);
	}

	private void exerciseEnumerationGeneratedCode(Class<?> enumClass) {
		Object[] enumValues=(Object[])Deencapsulation.invoke(enumClass, "values");
		for(Object enumValue:enumValues) {
			Object valueOf = Deencapsulation.invoke(enumClass, "valueOf",enumValue.toString());
			assertThat(valueOf,sameInstance(enumValue));
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDuplicatedTerms() throws Exception {
		V v=new V();
		new ImmutableTerm(v,"entity");
		new ImmutableTerm(v,"entity");
	}

	@Test(expected=IllegalStateException.class)
	public void testBadRegistration$afterInitialized() throws Exception {
		V v=new V();
		new ImmutableTerm(v,"entity");
		v.initialize();
		new ImmutableTerm(v,"entity");
	}

	@Test(expected=IllegalStateException.class)
	public void testInitialize$cannotReinit() throws Exception {
		V v=new V();
		new ImmutableTerm(v,"entity");
		v.initialize();
		v.initialize();
	}

	@Test
	public void testBadRegistration$invalidOrdinal$lowerBound() throws Exception {
		V v = new V();
		new MockUp<ImmutableTerm>() {
			@Mock
			public int ordinal() {
				return -1;
			}
		};
		try {
			new ImmutableTerm(v, "entity");
			fail("Should not allow register a term with invalid ordinal (lower)");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),containsString("Vocabulary 'namespace' ("));
			assertThat(e.getMessage(),containsString(") initialization failure: Invalid ordinal '-1' for reserved name 'ENTITY'"));
		}
	}

	@Test
	public void testBadRegistration$invalidOrdinal$upperBound() throws Exception {
		V v = new V();
		new MockUp<ImmutableTerm>() {
			@Mock
			public int ordinal() {
				return 3;
			}
		};
		try {
			new ImmutableTerm(v, "entity");
			fail("Should not allow register a term with invalid ordinal (upper)");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),containsString("Vocabulary 'namespace' ("));
			assertThat(e.getMessage(),containsString(") initialization failure: Invalid ordinal '3' for reserved name 'ENTITY'"));
		}
	}

	@Test
	public void testBadRegistration$incomplete() throws Exception {
		V v = new V();
		new MockUp<AbstractImmutableVocabulary<ImmutableTerm>>() {
			@Mock
			public void registerTerm(ImmutableTerm term) {
			}
		};
		new ImmutableTerm(v, "one");
		try {
			v.initialize();
			fail("Shot not initialize vocabulary without completing term registration");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),containsString("Vocabulary 'namespace' ("));
			assertThat(e.getMessage(),containsString(") initialization failure: not all reserved names have been registered"));
		}
	}

}
