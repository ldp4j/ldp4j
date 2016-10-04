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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-testing:0.2.2
 *   Bundle      : ldp4j-commons-testing-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons.testing;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class UtilsTest {

	private static class CustomObject {

	}

	private static final class CustomHashObject {

		private int hashCode;

		private CustomHashObject(int hashCode) {
			this.hashCode = hashCode;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + hashCode;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CustomHashObject other = (CustomHashObject) obj;
			if (hashCode != other.hashCode)
				return false;
			return true;
		}

	}

	private class NonStaticClass  {

	}

	@SuppressWarnings("unused")
	private static final class ToMuchConstructors  {

		private ToMuchConstructors() {
			this(0);
		}

		private ToMuchConstructors(int i) {
		}
	}

	private final class NotStaticInnerClass {

	}

	private static final class NoPrivateConstructor {

		@SuppressWarnings("unused")
		public NoPrivateConstructor() {
		}
	}

	private static final class PrivateConstructorWithArgs {

		private PrivateConstructorWithArgs(int arg) {
		}
	}

	@Test
	public void testIsUtilityClass() {
		assertThat(Utils.isUtilityClass(Utils.class),equalTo(true));
	}

	@Test
	public void testIsLibrary$notUtilityClass$notStaticClass() {
		assertThat(Utils.isUtilityClass(NonStaticClass.class),equalTo(false));
	}

	@Test
	public void testIsLibrary$notUtilityClass$multipleConstructors() {
		assertThat(Utils.isUtilityClass(NonStaticClass.class),equalTo(false));
	}

	@Test
	public void testIsLibrary$notUtilityClass$notFinalClass() {
		assertThat(Utils.isUtilityClass(CustomObject.class),equalTo(false));
	}

	@Test
	public void testIsLibrary$notUtilityClass$innerClass() {
		assertThat(Utils.isUtilityClass(NotStaticInnerClass.class),equalTo(false));
	}

	@Test
	public void testIsLibrary$notUtilityClass$methodClass() {
		final class NotStaticMethodClass {
		}
		assertThat(Utils.isUtilityClass(NotStaticMethodClass.class),equalTo(false));
	}

	@Test
	public void testIsLibrary$notUtilityClass$constructorClass() {
		final class InnerClass {
			InnerClass() {
				final class NotStaticMethodClass {
				}
				assertThat(Utils.isUtilityClass(NotStaticMethodClass.class),equalTo(false));
			}
		}
		new InnerClass();
	}

	@Test
	public void testIsLibrary$notUtilityClass$constructorWithArgs() {
		assertThat(Utils.isUtilityClass(CustomHashObject.class),equalTo(false));
	}

	@Test
	public void testIsLibrary$notUtilityClass$noPrivateConstructor() {
		assertThat(Utils.isUtilityClass(NoPrivateConstructor.class),equalTo(false));
	}

	@Test
	public void testIsLibrary$notUtilityClass$privateConstructorWithArgs() {
		assertThat(Utils.isUtilityClass(PrivateConstructorWithArgs.class),equalTo(false));
	}

	@Test(expected=IllegalStateException.class)
	public void testIsLibrary$fail() {
		Utils.isUtilityClass(UtilityClass.class);
	}

	@Test
	public void testDefaultToString$regularImplementation() {
		Object obj=new Object();
		assertThat(Utils.defaultToString(obj),equalTo(obj.toString()));
	}

	@Test
	public void testDefaultToString$customObjectNotOverridingHashNorEquals() {
		Object obj=new CustomObject();
		assertThat(Utils.defaultToString(obj),equalTo(obj.toString()));
	}

	@Test
	public void testDefaultToString$customObjectOverridingHashButNotEquals() {
		Object obj=new CustomHashObject(1234);
		assertThat(Utils.defaultToString(obj),equalTo(obj.toString()));
	}

}
