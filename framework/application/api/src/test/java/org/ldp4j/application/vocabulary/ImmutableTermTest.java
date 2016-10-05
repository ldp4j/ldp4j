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

import org.junit.Test;

public class ImmutableTermTest {

	static class V extends AbstractImmutableVocabulary<ImmutableTerm> {
		private static final long serialVersionUID = 6320487110172330521L;
		V() {
			super(ImmutableTerm.class,"namespace","prefix");
		}
	}

	private ImmutableTerm term() {
		V v=new V();
		ImmutableTerm term = new ImmutableTerm(v,"term");
		return term;
	}

	@Test
	public void testEqual$same() throws Exception {
		V v=new V();
		ImmutableTerm term = new ImmutableTerm(v,"term");
		assertThat(term,equalTo(term));
	}

	@Test
	public void testEqual$notSame() throws Exception {
		V v=new V();
		ImmutableTerm term1 = new ImmutableTerm(v,"termA");
		V v2=new V();
		ImmutableTerm term2 = new ImmutableTerm(v2,"termA");
		assertThat(term1,not(equalTo(term2)));
	}
	@Test
	public void testHashCode$isCustom() throws Exception {
		ImmutableTerm term = term();
		assertThat(term.hashCode(),not(equalTo(System.identityHashCode(term))));
	}

	@Test
	public void testHashCode$notSame() throws Exception {
		V v=new V();
		ImmutableTerm term1 = new ImmutableTerm(v,"termA");
		V v2=new V();
		ImmutableTerm term2 = new ImmutableTerm(v2,"termA");
		assertThat(term1.hashCode(),not(equalTo(term2.hashCode())));
	}

	@Test
	public void testAs$notSupported() throws Exception {
		try {
			term().as(Double.class);
			fail("Should not cast to Double");
		} catch (UnsupportedOperationException e) {
			assertThat(e.getCause(),instanceOf(CannotAdaptClassesException.class));
		}
	}

	@Test
	public void testCompareTo$differentInstancesSameVocabulary() throws Exception {
		V v=new V();
		ImmutableTerm term1 = new ImmutableTerm(v,"termA");
		V v2=new V();
		ImmutableTerm term2 = new ImmutableTerm(v2,"termA");
		try {
			term1.compareTo(term2);
			fail("Should not be able to compare between different instances of the same vocabulary");
		} catch(ClassCastException e) {
		}
	}
	@Test
	public void testCompareTo$differentVocabularies() throws Exception {
		class VV extends V {
			private static final long serialVersionUID = 6590572386564141355L;
		}
		V v=new V();
		ImmutableTerm term1 = new ImmutableTerm(v,"termA");
		VV v2=new VV();
		ImmutableTerm term2 = new ImmutableTerm(v2,"termA");
		try {
			term1.compareTo(term2);
			fail("Should not be able to compare between different vocabularies");
		} catch(ClassCastException e) {
		}
	}

	@Test
	public void testCompareTo$sameVocabulary() throws Exception {
		V v=new V();
		ImmutableTerm term1 = new ImmutableTerm(v,"termA");
		ImmutableTerm term2 = new ImmutableTerm(v,"termB");
		assertThat(term1.compareTo(term2),not(greaterThanOrEqualTo(0)));
		assertThat(term2.compareTo(term1),greaterThanOrEqualTo(0));
		assertThat(term1.compareTo(term1),equalTo(0));
	}
	@Test
	public void testClone() throws Exception {
		try {
			term().clone();
			fail("Clone should not be supported");
		} catch (CloneNotSupportedException e) {
		}
	}

}
