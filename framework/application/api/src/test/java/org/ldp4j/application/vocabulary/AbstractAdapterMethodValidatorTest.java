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

import java.lang.reflect.Method;
import java.net.URI;

import org.junit.Test;

public abstract class AbstractAdapterMethodValidatorTest {

	@SuppressWarnings("unused")
	private static class StaticPublic {
		static public URI adaptTo(String string) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class StaticPrivate {
		static private URI adaptTo(String string) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class StaticProtected {
		static protected URI adaptTo(String string) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class StaticPackagePrivate {
		static URI adaptTo(String string) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class Private {
		private URI adaptTo(String string) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class Protected {
		protected URI adaptTo(String string) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class PackagePrivate {
		URI adaptTo(String string) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class BadReturn {
		public String adaptTo(String string) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class BadParameter {
		public URI adaptTo(URI uri) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class NoReturn {
		public void adaptTo(String string) {
		}
	}

	@SuppressWarnings("unused")
	private static class NoParameter {
		public URI adaptTo() {
			return null;
		}
	}

	private Method getMethod(Class<?> target, Class<?>... classes) throws NoSuchMethodException {
		return target.getDeclaredMethod("adaptTo", classes);
	}

	protected abstract AdapterMethodValidator sut();

	@Test
	public void testIsValid() throws Exception {
		assertThat(sut().isValid(getMethod(StaticPublic.class,String.class)),equalTo(true));
	}

	@Test
	public void testIsValid$invalid$invalidName() throws Exception {
		assertThat(sut().isValid(getClass().getDeclaredMethod("sut")),equalTo(false));
	}

	@Test
	public void testIsValid$invalid$staticPrivate() throws Exception {
		assertThat(sut().isValid(getMethod(StaticPrivate.class,String.class)),equalTo(false));
	}

	@Test
	public void testIsValid$invalid$staticProtected() throws Exception {
		assertThat(sut().isValid(getMethod(StaticProtected.class,String.class)),equalTo(false));
	}

	@Test
	public void testIsValid$invalid$staticPackagePrivate() throws Exception {
		assertThat(sut().isValid(getMethod(StaticPackagePrivate.class,String.class)),equalTo(false));
	}

	@Test
	public void testIsValid$invalid$private() throws Exception {
		assertThat(sut().isValid(getMethod(Private.class,String.class)),equalTo(false));
	}

	@Test
	public void testIsValid$invalid$protected() throws Exception {
		assertThat(sut().isValid(getMethod(Protected.class,String.class)),equalTo(false));
	}

	@Test
	public void testIsValid$invalid$packagePrivate() throws Exception {
		assertThat(sut().isValid(getMethod(PackagePrivate.class,String.class)),equalTo(false));
	}

	@Test
	public void testIsValid$invalid$badReturn() throws Exception {
		assertThat(sut().isValid(getMethod(BadReturn.class,String.class)),equalTo(false));
	}

	@Test
	public void testIsValid$invalid$badParameter() throws Exception {
		assertThat(sut().isValid(getMethod(BadParameter.class,URI.class)),equalTo(false));
	}

	@Test
	public void testIsValid$invalid$noReturn() throws Exception {
		assertThat(sut().isValid(getMethod(NoReturn.class,String.class)),equalTo(false));
	}

	@Test
	public void testIsValid$invalid$noParameter() throws Exception {
		assertThat(sut().isValid(getMethod(NoParameter.class)),equalTo(false));
	}

	@Test
	public void testGetTargetClass() throws Exception {
		assertThat((Object)sut().getTargetClass(),sameInstance((Object)String.class));
	}

}
