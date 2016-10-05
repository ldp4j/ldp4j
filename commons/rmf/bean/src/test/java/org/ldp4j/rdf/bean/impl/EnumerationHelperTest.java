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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.impl;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.rdf.bean.InvalidDefinitionException;
import org.ldp4j.rdf.bean.impl.EnumerationHelper;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class EnumerationHelperTest {

	public enum Example {
		ONE,
		TWO;
	}

	public static class Invalid$Names {
		public static class Missing {
			public String name() {
				return null;
			}

			public static Missing valueOf(String name) {
				return null;
			}
		}
		public static class WrongReturn$NoParameterizedType {
			public static String names() {
				return null;
			}

			public static String name(WrongReturn$NoParameterizedType value) {
				return null;
			}

			public static WrongReturn$NoParameterizedType valueOf(String name) {
				return null;
			}
		}
		public static class WrongReturn$NoSet {
			public static List<String> names() {
				return null;
			}

			public static String name(WrongReturn$NoSet value) {
				return null;
			}

			public static WrongReturn$NoSet valueOf(String name) {
				return null;
			}
		}
		public static class WrongReturn$NoString {
			public static Set<Integer> names() {
				return null;
			}

			public static String name(WrongReturn$NoSet value) {
				return null;
			}

			public static WrongReturn$NoSet valueOf(String name) {
				return null;
			}
		}
	}

	public static class Invalid$Name {
		public static class Missing {
			public static Set<String> names() {
				return Collections.emptySet();
			}
			public static Missing valueOf(String name) {
				return null;
			}
		}
		public abstract static class WrongModifiers$abstract {
			public static Set<String> names() {
				return Collections.emptySet();
			}

			public abstract String name();

			public static WrongModifiers$abstract valueOf(String name) {
				return null;
			}
		}
		public static class WrongModifiers$static {
			public static Set<String> names() {
				return Collections.emptySet();
			}

			public static String name() {
				return null;
			}

			public static WrongModifiers$static valueOf(String name) {
				return null;
			}
		}
		public static class WrongModifiers$protected {
			public static Set<String> names() {
				return Collections.emptySet();
			}

			protected String name() {
				return null;
			}

			public static WrongModifiers$protected valueOf(String name) {
				return null;
			}
		}
		public static class WrongModifiers$private {
			public static Set<String> names() {
				return Collections.emptySet();
			}

			@SuppressWarnings("unused")
			private String name() {
				return null;
			}

			public static WrongModifiers$private valueOf(String name) {
				return null;
			}
		}
		public static class WrongModifiers$default {
			public static Set<String> names() {
				return Collections.emptySet();
			}

			String name() {
				return null;
			}

			public static WrongModifiers$private valueOf(String name) {
				return null;
			}
		}
		public static class WrongReturn{
			public static Set<String> names() {
				return Collections.emptySet();
			}
			public Integer name() {
				return null;
			}
			public static WrongReturn valueOf(String name) {
				return null;
			}
		}
	}

	public static class Invalid$FromName {
		public static class Missing {

			public static Set<String> names() {
				return Collections.emptySet();
			}

			public static String name(Missing value) {
				return null;
			}
		}
		public static class WrongParam {

			public static Set<String> names() {
				return Collections.emptySet();
			}

			public static String name(WrongParam value) {
				return null;
			}

			public static WrongParam valueOf(Integer name) {
				return null;
			}
		}
		public static class WrongReturn {

			public static Set<String> names() {
				return Collections.emptySet();
			}

			public static String name(WrongReturn value) {
				return null;
			}

			public static Integer valueOf(String name) {
				return null;
			}
		}
	}

	public static class ValidEnumeration {

		protected static final Set<String> NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("ONE","TWO")));
		protected static final ValidEnumeration ONE=new ValidEnumeration("ONE");
		protected static final ValidEnumeration TWO=new ValidEnumeration("TWO");
		protected static final Set<ValidEnumeration> VALUES = Collections.unmodifiableSet(new HashSet<ValidEnumeration>(Arrays.asList(ONE,TWO)));

		private final String name;

		private ValidEnumeration(String name) {
			this.name = name;
		}

		public static Set<String> names() {
			return NAMES;
		}

		public String name() {
			return name;
		}

		public static ValidEnumeration valueOf(String name) {
			if(ONE.name.equals(name)) {
				return ONE;
			}
			if(TWO.name.equals(name)) {
				return TWO;
			}
			throw new IllegalArgumentException("Unknown name '"+name+"'");
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ValidEnumeration)) {
				return false;
			}
			ValidEnumeration other = (ValidEnumeration) obj;
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testNewInstance$valid() throws Exception {
		EnumerationHelper<ValidEnumeration> sut = EnumerationHelper.newInstance(ValidEnumeration.class);
		assertThat(sut,notNullValue());
		assertThat((Object)sut.getEnumeratedClass(),is((Object)ValidEnumeration.class));
		assertThat(sut.names(),equalTo(ValidEnumeration.NAMES));
		assertThat(sut.name(ValidEnumeration.ONE),equalTo(ValidEnumeration.ONE.name));
		assertThat(sut.name(ValidEnumeration.TWO),equalTo(ValidEnumeration.TWO.name));
		assertThat(sut.valueOf(ValidEnumeration.ONE.name),equalTo(ValidEnumeration.ONE));
		assertThat(sut.valueOf(ValidEnumeration.TWO.name),equalTo(ValidEnumeration.TWO));
		assertThat(sut.values(),equalTo(ValidEnumeration.VALUES));
		try {
			sut.valueOf("other");
			fail("Invalid names should make the helper fail");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),containsString("'other'"));
		}
	}

	@Test
	public void testNewInstance$invalid$names() throws Exception {
		try {
			EnumerationHelper.newInstance(Invalid$Names.Missing.class);
			fail("Class should fail: "+Invalid$Names.Missing.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$Names.WrongReturn$NoParameterizedType.class);
			fail("Class should fail: "+Invalid$Names.WrongReturn$NoParameterizedType.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$Names.WrongReturn$NoSet.class);
			fail("Class should fail: "+Invalid$Names.WrongReturn$NoSet.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$Names.WrongReturn$NoString.class);
			fail("Class should fail: "+Invalid$Names.WrongReturn$NoString.class);
		} catch (InvalidDefinitionException e) {
		}
	}

	@Test
	public void testNewInstance$invalid$name() throws Exception {
		try {
			EnumerationHelper.newInstance(Invalid$Name.Missing.class);
			fail("Class should fail: "+Invalid$Name.Missing.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$Name.WrongModifiers$abstract.class);
			fail("Class should fail: "+Invalid$Name.WrongModifiers$abstract.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$Name.WrongModifiers$static.class);
			fail("Class should fail: "+Invalid$Name.WrongModifiers$static.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$Name.WrongModifiers$protected.class);
			fail("Class should fail: "+Invalid$Name.WrongModifiers$protected.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$Name.WrongModifiers$private.class);
			fail("Class should fail: "+Invalid$Name.WrongModifiers$private.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$Name.WrongModifiers$default.class);
			fail("Class should fail: "+Invalid$Name.WrongModifiers$default.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$Name.WrongReturn.class);
			fail("Class should fail: "+Invalid$Name.WrongReturn.class);
		} catch (InvalidDefinitionException e) {
		}
	}

	@Test
	public void testNewInstance$invalid$fromName() throws Exception {
		try {
			EnumerationHelper.newInstance(Invalid$FromName.Missing.class);
			fail("Class should fail: "+Invalid$Name.Missing.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$FromName.WrongParam.class);
			fail("Class should fail: "+Invalid$FromName.WrongParam.class);
		} catch (InvalidDefinitionException e) {
		}
		try {
			EnumerationHelper.newInstance(Invalid$FromName.WrongReturn.class);
			fail("Class should fail: "+Invalid$Name.WrongReturn.class);
		} catch (InvalidDefinitionException e) {
		}
	}
}
