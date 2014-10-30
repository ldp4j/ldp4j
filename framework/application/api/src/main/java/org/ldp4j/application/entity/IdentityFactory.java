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

import static com.google.common.base.Preconditions.*;

import java.net.URI;


final class IdentityFactory {

	private IdentityFactory() {
	}

	public static <T> ManagedIdentity<T> createManagedIdentity(Key<T> key) {
		checkNotNull(key,"Key cannot be null");
		return ManagedIdentity.create(key);
	}

	public static <T,V> ManagedIdentity<T> createManagedIdentity(Class<T> owner, V nativeId) {
		checkNotNull(owner,"Key owner cannot be null");
		checkNotNull(nativeId,"Key native identifier cannot be null");
		return createManagedIdentity(Key.create(owner, nativeId));
	}

	public static ExternalIdentity createExternalIdentity(URI location) {
		checkNotNull(location,"Location cannot be null");
		return ExternalIdentity.create(location);
	}

	public static <T> RelativeIdentity<T> createRelativeIdentity(Key<T> parent, URI path) {
		checkNotNull(parent,"Parent key cannot be null");
		checkNotNull(path,"Path cannot be null");
		return RelativeIdentity.create(parent, path);
	}

	public static <T,V> RelativeIdentity<T> createRelativeIdentity(Class<T> owner, V nativeId, URI path) {
		checkNotNull(owner,"Key owner cannot be null");
		checkNotNull(nativeId,"Key native identifier cannot be null");
		return createRelativeIdentity(Key.create(owner, nativeId),path);
	}

}
