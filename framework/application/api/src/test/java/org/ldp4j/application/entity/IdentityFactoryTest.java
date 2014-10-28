/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.net.URI;
import java.util.Deque;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.entity.IdentifierUtil.Classifier;
import org.ldp4j.application.entity.IdentifierUtil.IdentifierIntrospector;
import org.ldp4j.application.entity.spi.NameGenerator;

import com.google.common.collect.Queues;

public class IdentityFactoryTest {

	private static final class ControlledNameGenerator implements NameGenerator<String> {

		private Deque<String> nativeIds;

		private ControlledNameGenerator() {
			this.nativeIds=Queues.newArrayDeque();
		}

		private void addNextNativeId(String nativeId) {
			this.nativeIds.push(nativeId);
		}

		private String getNextNativeId() {
			return this.nativeIds.getFirst();
		}

		@Override
		public Name<String> nextName() {
			return  NamingScheme.getDefault().name(getNextNativeId());
		}

	}

	private DataSource dataSource;
	private ControlledNameGenerator nameGenerator;

	@Before
	public void setUp() {
		nameGenerator = new ControlledNameGenerator();
		dataSource = DataSource.create(nameGenerator);
	}

	@SuppressWarnings("rawtypes")
	private void assertInvariant(IdentifierIntrospector introspector, Object owner, Class<?> valueClass) {
		assertThat(introspector.owner(),equalTo(owner));
		assertThat((Class)introspector.valueClass(),sameInstance((Class)valueClass));
	}

	@Test
	public void testLocalCreation() {
		String nativeId = "$example%+%value^";
		nameGenerator.addNextNativeId(nativeId);
		LocalIdentity<?> local = IdentityFactory.createLocalIdentity(dataSource);
		URI identifier = local.identifier();
		System.out.println(String.format("%s --> %s",nativeId,identifier));
		assertThat(identifier,notNullValue());
		IdentifierIntrospector introspector = IdentifierUtil.introspect(identifier);
		assertThat(introspector.subject(),is(identifier));
		assertThat(introspector.isValid(),is(true));
		assertThat(introspector.classifier(),is(Classifier.LOCAL));
		assertInvariant(introspector, dataSource.identifier(), String.class);
		assertThat(introspector.value(String.class),equalTo(nativeId));
	}

	@Test
	public void testManagedCreation() {
		int nativeId = 23;
		ManagedIdentity<DataSet> managed = IdentityFactory.createManagedIdentity(DataSet.class,nativeId);
		URI identifier = managed.identifier();
		assertThat(identifier,notNullValue());
		IdentifierIntrospector introspector = IdentifierUtil.introspect(identifier);
		System.out.println(String.format("%s --> %s",nativeId,identifier));
		assertThat(introspector.subject(),is(identifier));
		assertThat(introspector.isValid(),is(true));
		assertThat(introspector.classifier(),is(Classifier.MANAGED));
		assertInvariant(introspector, DataSet.class, Integer.class);
		assertThat(introspector.value(Integer.class),equalTo(nativeId));
	}

	@Test
	public void testExternalCreation() {
		URI location = URI.create("http://localhost:8080/ldp4j/resource/1/");
		ExternalIdentity external = IdentityFactory.createExternalIdentity(location);
		URI identifier = external.identifier();
		System.out.println(String.format("%s --> %s",location,identifier));
		assertThat(identifier,notNullValue());
		IdentifierIntrospector introspector = IdentifierUtil.introspect(identifier);
		assertThat(introspector.subject(),is(identifier));
		assertThat(introspector.isValid(),is(true));
		assertThat(introspector.classifier(),is(Classifier.EXTERNAL));
		assertInvariant(introspector, location, URI.class);
		assertThat(introspector.value(URI.class),equalTo(location));
	}

	@Test
	public void testRelativeCreation() {
		Key<DataSource> key=Key.create(DataSource.class, 23);
		URI path = URI.create("http://localhost:8080/ldp4j/resource/1/");
		RelativeIdentity<DataSource> relative = IdentityFactory.createRelativeIdentity(key,path);
		URI identifier = relative.identifier();
		System.out.println(String.format("{%s,%s} --> %s",key,path,identifier));
		assertThat(identifier,notNullValue());
		IdentifierIntrospector introspector = IdentifierUtil.introspect(identifier);
		assertThat(introspector.subject(),is(identifier));
		assertThat(introspector.isValid(),is(true));
		assertThat(introspector.classifier(),is(Classifier.RELATIVE));
		assertInvariant(introspector, key, URI.class);
		assertThat(introspector.value(URI.class),equalTo(path));
	}

}
