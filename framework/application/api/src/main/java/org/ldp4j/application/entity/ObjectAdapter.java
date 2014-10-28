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

public final class ObjectAdapter<S> {

	private final Class<? extends S> targetClazz;
	private final Object object;
	private final boolean compatible;

	ObjectAdapter(Class<? extends S> targetClazz, Object object) {
		this.targetClazz=targetClazz;
		this.object=object;
		this.compatible=isCompatible(targetClazz, object);
	}

	private boolean isCompatible(Class<? extends S> targetClazz, Object object) {
		return
			targetClazz!=null?
				targetClazz.isInstance(object):
				false;
	}

	private S safeCast() {
		return this.targetClazz.cast(this.object);
	}

	public S orNull() {
		return or(null);
	}

	public S or(S defaultValue) {
		S result=defaultValue;
		if(this.compatible) {
			result=safeCast();
		}
		return result;
	}

	public <E extends Exception> S orFail(E exception) throws E {
		if(!this.compatible) {
			throw exception;
		}
		return safeCast();
	}

	public S now() throws ClassCastException {
		return orFail(new ClassCastException("Object is not of type '"+this.targetClazz.getName()+"'"));
	}

}