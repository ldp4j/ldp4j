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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.application.entity.spi.IdentifierGenerator;


final class IdentityFactory {

	@SuppressWarnings("unused")
	private UUID randomUUID;

	private IdentifierGenerator<?> identifierGenerator;

	private static final class DefaultIdentifierGenerator implements IdentifierGenerator<Long> {

		private AtomicLong counter;

		private DefaultIdentifierGenerator() {
			this.counter=new AtomicLong();
		}

		@Override
		public Long nextIdentifier() {
			return counter.incrementAndGet();
		}

	}

	private IdentityFactory(UUID randomUUID, IdentifierGenerator<?> identifierGenerator) {
		this.randomUUID = randomUUID;
		this.identifierGenerator = identifierGenerator;
	}

	private Object nextLocalId() {
		return this.identifierGenerator.nextIdentifier();
	}

	Identity createIdentity() {
		return LocalIdentity.create(nextLocalId());
	}

	<T> ManagedIdentity<T> createManagedIdentity(Key<T> key) {
		checkNotNull(key,"Key cannot be null");
		return ManagedIdentity.create(key);
	}

	<T,V> ManagedIdentity<T> createManagedIdentity(Class<T> owner, V nativeId) {
		checkNotNull(owner,"Key owner cannot be null");
		checkNotNull(nativeId,"Key native identifier cannot be null");
		return createManagedIdentity(Key.create(owner, nativeId));
	}

	ExternalIdentity createExternalIdentity(URI location) {
		checkNotNull(location,"Location cannot be null");
		return ExternalIdentity.create(location);
	}

	<T> RelativeIdentity<T> createRelativeIdentity(Key<T> parent, URI path) {
		checkNotNull(parent,"Parent key cannot be null");
		checkNotNull(path,"Path cannot be null");
		return RelativeIdentity.create(parent, path);
	}

	<T,V> RelativeIdentity<T> createRelativeIdentity(Class<T> owner, V nativeId, URI path) {
		checkNotNull(owner,"Key owner cannot be null");
		checkNotNull(nativeId,"Key native identifier cannot be null");
		return createRelativeIdentity(Key.create(owner, nativeId),path);
	}

	public static IdentityFactory create() {
		return create(new DefaultIdentifierGenerator());
	}

	public static IdentityFactory create(IdentifierGenerator<?> nameGenerator) {
		return new IdentityFactory(UUID.randomUUID(),nameGenerator);
	}

}