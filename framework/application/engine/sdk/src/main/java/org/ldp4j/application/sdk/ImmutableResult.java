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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-engine-sdk-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ldp4j.application.engine.context.Change;

import com.google.common.collect.Lists;

public final class ImmutableResult<T> extends AbstractImmutableResult<T> {

	public static final class Builder<T> {

		private List<Change> changes;
		private T result;

		private Builder() {
			this.changes=Lists.newArrayList();
		}

		public Builder<T> withValue(T result) {
			this.result=result;
			return this;
		}

		public <C extends Change> Builder<T> withChanges(Collection<C> changes) {
			this.changes.addAll(changes);
			return this;
		}

		public <C extends Change> Builder<T> withChanges(@SuppressWarnings("unchecked") C... changes) {
			return withChanges(Arrays.asList(changes));
		}

		public <C extends Change> Builder<T> withChange(C change) {
			this.changes.add(change);
			return this;
		}

		public ImmutableResult<T> build() {
			return new ImmutableResult<T>(this.result,this.changes);
		}

	}

	private ImmutableResult(T result,List<Change> changes) {
		super(result,changes);
	}

	public static <T> Builder<T> builder() {
		return new Builder<T>();
	}

}
