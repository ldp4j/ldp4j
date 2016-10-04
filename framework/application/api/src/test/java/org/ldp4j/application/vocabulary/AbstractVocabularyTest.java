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

import java.net.URI;

import javax.xml.namespace.QName;

import mockit.Deencapsulation;

import org.junit.Test;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class AbstractVocabularyTest<T extends AbstractImmutableVocabulary<?>> {

	protected abstract T vocabulary();

	protected abstract String namespace();

	protected abstract String preferredPrefix();

	protected abstract URI unknownURITerm();

	protected abstract QName unknownQNameTerm();

	@Test
	public void testGetId() {
		assertThat(vocabulary().getId(),notNullValue());
	}

	@Test
	public void testGetNamespace() {
		assertThat(vocabulary().getNamespace(),equalTo(namespace()));
	}

	@Test
	public void testGetPreferredPrefix() {
		assertThat(vocabulary().getPreferredPrefix(),equalTo(preferredPrefix()));
	}

	@Test
	public void testFromName() {
		Vocabulary vocabulary = vocabulary();
		for(Term term:vocabulary.terms()) {
			assertThat(
				vocabulary.fromName(term.name()),
				sameInstance(term));
		}
	}

	@Test
	public void testFromName$unknown() {
		Vocabulary vocabulary = vocabulary();
		assertThat(
			vocabulary.fromName("$$"),
			nullValue());
	}

	@Test(expected=NullPointerException.class)
	public void testFromName$null() {
		Vocabulary vocabulary = vocabulary();
		vocabulary.fromName(null);
	}

	@Test
	public void testFromOrdinal() {
		Vocabulary vocabulary = vocabulary();
		for(Term term:vocabulary.terms()) {
			assertThat(
				"fromOrdinal("+term.name()+".ordinal()) failed",
				vocabulary.fromOrdinal(term.ordinal()),
				sameInstance(term));
		}
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testFromOrdinal$outOfBounds$upper() {
		Vocabulary vocabulary = vocabulary();
		vocabulary.fromOrdinal(vocabulary.size()+1);
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testFromOrdinal$outOfBounds$lower() {
		Vocabulary vocabulary = vocabulary();
		vocabulary.fromOrdinal(-1);
	}

	@Test
	public void testFromValue() {
		Vocabulary vocabulary = vocabulary();
		for(Term term:vocabulary.terms()) {
			assertThat(
				vocabulary.fromValue(term.as(URI.class)),
				sameInstance(term));
		}
	}

	@Test(expected=NullPointerException.class)
	public void testFromValue$null() {
		Vocabulary vocabulary = vocabulary();
		vocabulary.fromValue(null);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testFromValue$incompatible() {
		Vocabulary vocabulary = vocabulary();
		vocabulary.fromValue("1");
	}

	@Test
	public void testFromValue$compatible$unknown$uri() {
		Vocabulary vocabulary = vocabulary();
		assertThat(vocabulary.fromValue(unknownURITerm()),nullValue());
	}

	@Test
	public void testFromValue$compatible$unknown$qname() {
		Vocabulary vocabulary = vocabulary();
		assertThat(vocabulary.fromValue(unknownQNameTerm()),nullValue());
	}

	@Test
	public void testSize() {
		Vocabulary vocabulary = vocabulary();
		assertThat(vocabulary.size(),equalTo(vocabulary.terms().length));
	}

	@Test
	public void testIterator() {
		Vocabulary vocabulary = vocabulary();
		assertThat(Lists.newArrayList(vocabulary),contains((Term[])vocabulary.terms()));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testIterator$removeNotSupported() {
		Vocabulary vocabulary = vocabulary();
		vocabulary.iterator().remove();
	}

	@Test
	public void testValues() {
		Vocabulary vocabulary = vocabulary();
		assertThat(Lists.newArrayList((Term[])Deencapsulation.invoke(vocabulary.getClass(),"values")),contains((Term[])vocabulary.terms()));
	}

	@Test
	public void testValueOf$fromString() {
		Vocabulary vocabulary = vocabulary();
		for(Term term:vocabulary.terms()) {
			assertThat(
				(Term)Deencapsulation.invoke(vocabulary.getClass(),"valueOf",term.name()),
				sameInstance(term));
		}
	}

	@Test
	public void testValueOf$fromURI() {
		Vocabulary vocabulary = vocabulary();
		for(Term term:vocabulary.terms()) {
			assertThat(
				(Term)Deencapsulation.invoke(vocabulary.getClass(),"valueOf",term.as(URI.class)),
				sameInstance(term));
		}
	}

	@Test
	public void testValueOf$fromQName() {
		Vocabulary vocabulary = vocabulary();
		for(Term term:vocabulary.terms()) {
			assertThat(
				(Term)Deencapsulation.invoke(vocabulary.getClass(),"valueOf",term.as(QName.class)),
				sameInstance(term));
		}
	}

}
