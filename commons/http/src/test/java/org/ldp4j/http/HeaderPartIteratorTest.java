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
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import mockit.Deencapsulation;

import org.junit.Test;

public class HeaderPartIteratorTest {

	public HeaderPartIterator createSut(final String data) {
		return HeaderPartIterator.create(data);
	}

	@Test
	public void testStateValues() throws ClassNotFoundException {
		final Class<?> clazz=Class.forName("org.ldp4j.http.HeaderPartIterator$State", false, Thread.currentThread().getContextClassLoader());
		assertThat(Enum.class.isAssignableFrom(clazz),equalTo(true));
		@SuppressWarnings("unchecked")
		final Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) clazz;
		verifyEnum(enumClass);
	}

	@Test
	public void testTraversalActionValues() throws ClassNotFoundException {
		final Class<?> clazz=Class.forName("org.ldp4j.http.HeaderPartIterator$Traversal$Action", false, Thread.currentThread().getContextClassLoader());
		assertThat(Enum.class.isAssignableFrom(clazz),equalTo(true));
		@SuppressWarnings("unchecked")
		final Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) clazz;
		verifyEnum(enumClass);
	}

	@Test
	public void tokenIsNotAvailableBeforeStartingIteration() throws Exception {
		final HeaderPartIterator sut = createSut("");
		try {
			sut.part();
			fail("Should not retrieve token before starting iteration");
		} catch(final IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Iteration has not started"));
		}
	}

	@Test
	public void startsAtIsNotAvailableBeforeStartingIteration() throws Exception {
		final HeaderPartIterator sut = createSut("");
		try {
			sut.startsAt();
			fail("Should not retrieve start position before starting iteration");
		} catch(final IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Iteration has not started"));
		}
	}

	@Test
	public void endsAtIsNotAvailableBeforeStartingIteration() throws Exception {
		final HeaderPartIterator sut = createSut("");
		try {
			sut.endsAt();
			fail("Should not retrieve end position before starting iteration");
		} catch(final IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Iteration has not started"));
		}
	}

	public void doesNotIterateEmptyStrings() throws Exception {
		final HeaderPartIterator sut = createSut("");
		assertHasFailed(sut,"No token found");
	}

	@Test
	public void iteratesSimpleStrings() throws Exception {
		final HeaderPartIterator sut = createSut("block1Block2Block3");
		assertHasNextToken(sut,"block1Block2Block3");
		assertIsCompleted(sut);
	}

	@Test
	public void spacesAreNotDelimiters() throws Exception {
		final HeaderPartIterator sut = createSut("block1 Block2");
		assertHasNextToken(sut,"block1");
		assertHasFailed(sut,"No delimiter found");
	}

	@Test
	public void tabsAreNotDelimiters() throws Exception {
		final HeaderPartIterator sut = createSut("block1\tBlock2");
		assertHasNextToken(sut,"block1");
		assertHasFailed(sut,"No delimiter found");
	}

	@Test
	public void testHasNext$noWhitespaces() throws Exception {
		final HeaderPartIterator sut=createSut("block1;block2;block3");
		assertHasNextToken(sut,"block1");
		assertHasNextToken(sut,"block2");
		assertHasNextToken(sut,"block3");
		assertIsCompleted(sut);
	}

	@Test
	public void testHasNext$nextParameterRightAfterDelimiter() throws Exception {
		final HeaderPartIterator sut = createSut("block1 \t;block2");
		assertHasNextToken(sut,"block1");
		assertHasNextToken(sut,"block2");
		assertIsCompleted(sut);
	}


	@Test
	public void testHasNext$whitespaces() throws Exception {
		final HeaderPartIterator sut = createSut("block1 \t;\t block2\t ; \tblock3");
		assertHasNextToken(sut,"block1");
		assertHasNextToken(sut,"block2");
		assertHasNextToken(sut,"block3");
		assertIsCompleted(sut);
	}

	@Test
	public void testHasNext$leadingDelimiter() throws Exception {
		final HeaderPartIterator sut=createSut(";block1");
		assertHasFailed(sut,"Leading delimiter found");
	}

	@Test
	public void testHasNext$leadingWhitespace$space() throws Exception {
		final HeaderPartIterator sut = createSut(" block1");
		assertHasFailed(sut,"Leading whitespace found");
	}

	@Test
	public void testHasNext$leadingWhitespace$tab() throws Exception {
		final HeaderPartIterator sut = createSut("\tblock1");
		assertHasFailed(sut,"Leading whitespace found");
	}

	@Test
	public void testHasNext$trailingWhitespace() throws Exception {
		final HeaderPartIterator sut = createSut("block1 \t;\t block2\t ; \tblock3 \t");
		assertHasNextToken(sut,"block1");
		assertHasNextToken(sut,"block2");
		assertHasNextToken(sut,"block3");
		assertHasFailed(sut,"Trailing whitespace found");
	}


	@Test
	public void testHasNext$danglingBlock$noWhitespace() throws Exception {
		final HeaderPartIterator sut = createSut("block1 \t;\t block2\t ; \tblock3 \t;");
		assertHasNextToken(sut,"block1");
		assertHasNextToken(sut,"block2");
		assertHasNextToken(sut,"block3");
		assertHasFailed(sut,"Dangling block definition found");
	}

	@Test
	public void testHasNext$danglingBlock$withWhitespace() throws Exception {
		final HeaderPartIterator sut = createSut("block1 \t;\t block2\t ; \tblock3 \t; ");
		assertHasNextToken(sut,"block1");
		assertHasNextToken(sut,"block2");
		assertHasNextToken(sut,"block3");
		assertHasFailed(sut,"Dangling block definition found");
	}

	@Test
	public void testHasNext$emptyBlockFound() throws Exception {
		final HeaderPartIterator sut = createSut("block1 \t;\t block2\t ; \tblock3 \t;;");
		assertHasNextToken(sut,"block1");
		assertHasNextToken(sut,"block2");
		assertHasNextToken(sut,"block3");
		assertHasFailed(sut,"Empty block found");
	}

	@Test
	public void testHasNext$whitespaceBlockFound() throws Exception {
		final HeaderPartIterator sut = createSut("block1 \t;\t block2\t ; \tblock3 \t; \t;");
		assertThat(sut.next(),equalTo("block1"));
		assertThat(sut.next(),equalTo("block2"));
		assertThat(sut.next(),equalTo("block3"));
		assertThat(sut.failure(),equalTo("Whitespace block found"));
	}

	@Test
	public void failsIfAttemptsToGoBeyondEnd() throws Exception {
		final Iterator<String> sut = createSut("block1");
		assertThat(sut.next(),equalTo("block1"));
		try {
			sut.next();
			fail("Should fail when going beyond end");
		} catch (final NoSuchElementException e) {
			// Nothing to test
		}
	}

	@Test
	public void removeIsNotSupported() throws Exception {
		final Iterator<String> sut = createSut("block1");
		try {
			sut.remove();
			fail("Should not support remove");
		} catch (final UnsupportedOperationException e) {
			// Nothing to test
		}
	}

	private void assertHasNextToken(final HeaderPartIterator sut, final String token) {
		assertThat(sut.next(),equalTo(token));
		assertThat(sut.part(),equalTo(token));
		assertThat(sut.header().substring(sut.startsAt(),sut.endsAt()),equalTo(token));
		if(sut.hasNext()) {
			assertThat(sut.failure(),equalTo(null));
			assertThat(sut.hasFailure(),equalTo(false));
		}
	}

	private void assertIsCompleted(final HeaderPartIterator sut) {
		assertThat(sut.hasNext(),equalTo(false));
		assertThat(sut.hasFailure(),equalTo(false));
		assertThat(sut.failure(),equalTo(null));
		assertThat(sut.endsAt(),equalTo(sut.header().length()));
	}

	private void assertHasFailed(final HeaderPartIterator sut, final String failure) {
		assertThat(sut.hasNext(),equalTo(false));
		assertThat(sut.hasFailure(),equalTo(true));
		assertThat(sut.failure(),equalTo(failure));
	}

	private void verifyEnum(final Class<? extends Enum<?>> enumClass) {
		final Object invoke = Deencapsulation.invoke(enumClass, "values");
		assertThat(invoke.getClass().isArray(),equalTo(true));
		final Object[] values=(Object[])invoke;
		for(final Object expected:values) {
			canBeFound(enumClass, expected);
		}
	}

	private void canBeFound(final Class<? extends Enum<?>> enumClass, final Object expected) {
		final Enum<?> cast = enumClass.cast(expected);
		final Object actual = Deencapsulation.invoke(enumClass, "valueOf",cast.name());
		assertThat(actual,equalTo(expected));
	}

}