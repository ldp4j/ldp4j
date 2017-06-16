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
package org.ldp4j.application.engine;

import org.ldp4j.application.engine.lifecycle.ApplicationEngineState;

public class ApplicationEngineLifecycleException extends ApplicationEngineException {

	private static final long serialVersionUID = 1815238454226331703L;

	private final ApplicationEngineState originalState;
	private final ApplicationEngineState pretendedState;

	public ApplicationEngineLifecycleException(ApplicationEngineState originalState, ApplicationEngineState pretendedState) {
		this(originalState,pretendedState,defaultErrorMessage(originalState, pretendedState));
	}

	public ApplicationEngineLifecycleException(ApplicationEngineState originalState, ApplicationEngineState pretendedState,String message) {
		this(originalState,pretendedState,message,null);
	}

	public ApplicationEngineLifecycleException(ApplicationEngineState originalState, ApplicationEngineState pretendedState, Throwable cause) {
		this(originalState,pretendedState,defaultErrorMessage(originalState, pretendedState, cause));
	}

	public ApplicationEngineLifecycleException(ApplicationEngineState originalState, ApplicationEngineState pretendedState, String message, Throwable cause) {
		super(message, cause);
		this.originalState = originalState;
		this.pretendedState = pretendedState;
	}

	public ApplicationEngineState originalState() {
		return originalState;
	}

	public ApplicationEngineState pretendedState() {
		return pretendedState;
	}

	private static String defaultErrorMessage(ApplicationEngineState originalState, ApplicationEngineState pretendedState) {
		return String.format("Application engine exception will trying to go from '%s' to '%s'",originalState,pretendedState);
	}

	private static String defaultErrorMessage(ApplicationEngineState originalState, ApplicationEngineState pretendedState, Throwable cause) {
		return String.format(defaultErrorMessage(originalState, pretendedState).concat(" (%s)"),cause.getMessage());
	}

}
