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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-api:0.2.2
 *   Bundle      : ldp4j-application-engine-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.lifecycle;

import static com.google.common.base.Preconditions.*;

public abstract class ApplicationEngineLifecycleListener {

	public final void stateChanged(ApplicationEngineState newState) {
		checkNotNull(newState,"Application engine state cannot be null");
		switch(newState) {
		case AVAILABLE:
			onApplicationEngineAvailable();
			break;
		case SHUTDOWN:
			onApplicationEngineShutdown();
			break;
		case STARTED:
			onApplicationEngineStarted();
			break;
		case UNAVAILABLE:
			onApplicationEngineUnavailable();
			break;
		case UNDEFINED:
			onApplicationEngineUndefined();
			break;
		default:
			throw new IllegalArgumentException("Unknown application engine state '"+newState+"'");
		}
		onStateChange(newState);
	}

	protected void onApplicationEngineUndefined() {
		// To be overriden by implementations that just want to worry about the
		// event, but not the value
	}

	protected void onApplicationEngineUnavailable() {
		// To be overriden by implementations that just want to worry about the
		// event, but not the value
	}

	protected void onApplicationEngineStarted() {
		// To be overriden by implementations that just want to worry about the
		// event, but not the value
	}

	protected void onApplicationEngineShutdown() {
		// To be overriden by implementations that just want to worry about the
		// event, but not the value
	}

	protected void onApplicationEngineAvailable() {
		// To be overriden by implementations that just want to worry about the
		// event, but not the value
	}

	protected void onStateChange(ApplicationEngineState newState) {
		// To be overriden by implementations that just want to worry about the
		// event, but not the value
	}

}
