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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.2
 *   Bundle      : ldp4j-application-examples-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import java.net.URI;

import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.session.ReadSession;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.spi.ResourceSnapshotResolver;
import org.ldp4j.application.spi.RuntimeDelegate;
import org.ldp4j.application.spi.ShutdownListener;

final class MockedRuntimeDelegate extends RuntimeDelegate {

	private WriteSession session;
	private boolean failure;
	private ResourceSnapshotResolver resolver;

	void setSession(WriteSession session) {
		this.session = session;
	}

	void setFailure(boolean failure) {
		this.failure = failure;
	}

	void setResolver(ResourceSnapshotResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public boolean isOffline() {
		return false;
	}

	@Override
	public WriteSession createSession() throws ApplicationContextException {
		if(this.failure) {
			throw new ApplicationContextException("failure");
		}
		return this.session;
	}

	@Override
	public ResourceSnapshotResolver createResourceResolver(URI canonicalBase, ReadSession session) {
		return resolver;
	}

	@Override
	public void registerShutdownListener(ShutdownListener listener) {
	}

}