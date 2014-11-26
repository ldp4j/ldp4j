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
package org.ldp4j.application.entity.core;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.application.entity.Identity;
import org.ldp4j.application.entity.Key;
import org.ldp4j.application.entity.ManagedIdentity;
import org.ldp4j.application.entity.RelativeIdentity;
import org.ldp4j.application.entity.spi.IdentifierGenerator;
import org.ldp4j.application.entity.spi.IdentityFactory;


public final class DefaultIdentityFactory implements IdentityFactory {

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

	private DefaultIdentityFactory(UUID randomUUID, IdentifierGenerator<?> identifierGenerator) {
		this.randomUUID = randomUUID;
		this.identifierGenerator = identifierGenerator;
	}

	public DefaultIdentityFactory() {
		this(UUID.randomUUID(),new DefaultIdentifierGenerator());
	}

	private Object nextLocalId() {
		return this.identifierGenerator.nextIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity createIdentity() {
		return BaseLocalIdentity.create(nextLocalId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ManagedIdentity<T> createManagedIdentity(Key<T> key) {
		return BaseManagedIdentity.create(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseExternalIdentity createExternalIdentity(URI location) {
		return BaseExternalIdentity.create(location);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> RelativeIdentity<T> createRelativeIdentity(Key<T> parent, URI path) {
		return BaseRelativeIdentity.create(parent, path);
	}

	/**
	 * TODO: Migrate to new configuration API
	 */
	@Deprecated
	public static DefaultIdentityFactory create(IdentifierGenerator<?> nameGenerator) {
		return new DefaultIdentityFactory(UUID.randomUUID(),nameGenerator);
	}

	@Override
	public String getVersion() {
		return "default";
	}

}