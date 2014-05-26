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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.core;

public final class DeletionResult {

	private final boolean enacted;
	private final String message;

	private DeletionResult(boolean enacted, String message) {
		this.enacted = enacted;
		this.message = message;
	}
	

	public boolean isEnacted() {
		return enacted;
	}
	
	public String getMessage() {
		return message;
	}
	
	
	public static final class DeletionResultBuilder {
		
		private boolean enacted=true;
		private String message=null;
		
		private DeletionResultBuilder() {
		}
		
		public DeletionResultBuilder enacted(boolean enacted) {
			this.enacted=enacted;
			return this;
		}
		
		public DeletionResultBuilder withMessage(String message) {
			this.message=message;
			return this;
		}
		
		public DeletionResult build() {
			return new DeletionResult(enacted,message);
		}
	}
	
	public static DeletionResultBuilder newBuilder() {
		return new DeletionResultBuilder();
	}

}