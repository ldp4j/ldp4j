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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SessionTerminationException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;

import com.google.common.base.MoreObjects;

final class ContextWriteSession implements WriteSession {

	private final WriteSession delegate;

	ContextWriteSession(WriteSession state) {
		this.delegate = state;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends ResourceSnapshot> S resolve(Class<? extends S> snapshotClass, Individual<?, ?> individual) {
		return this.delegate.resolve(snapshotClass,individual);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends ResourceSnapshot> S find(Class<? extends S> snapshotClass, Name<?> id, Class<? extends ResourceHandler> handlerClass) {
		return this.delegate.find(snapshotClass,id,handlerClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modify(ResourceSnapshot resource) {
		this.delegate.modify(resource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(ResourceSnapshot resource) {
		this.delegate.delete(resource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveChanges() throws WriteSessionException {
		this.delegate.saveChanges();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void discardChanges() throws WriteSessionException {
		this.delegate.discardChanges();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws SessionTerminationException {
		this.delegate.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("delegate",this.delegate).
					toString();
	}

}