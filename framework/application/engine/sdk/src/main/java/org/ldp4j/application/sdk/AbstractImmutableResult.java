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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.ldp4j.application.engine.context.Change;
import org.ldp4j.application.engine.context.Change.Action;
import org.ldp4j.application.engine.context.Result;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Lists;

abstract class AbstractImmutableResult<T,ID> implements Result<T,ID> {

	private final T result;
	private final List<Change<ID>>[] changes;
	private final int size;

	/**
	 * TODO: We should enforce that there are not clashing changes (i.e., two
	 * changes for the same action)
	 */
	AbstractImmutableResult(T result, List<Change<ID>> changes) {
		this.result = result;
		this.changes=AbstractImmutableResult.<ID>createArray(Action.values().length);
		for(Action action:Action.values()) {
			this.changes[action.ordinal()]=Lists.newArrayList();
		}
		for(Change<ID> change:changes) {
			this.changes[change.action().ordinal()].add(change);
		}
		this.size=changes.size();
	}

	public final T get() {
		return this.result;
	}

	public final List<Change<ID>> sideEffects(Action action) {
		return this.changes[action.ordinal()];
	}

	public final int size() {
		return this.size;
	}

	public final boolean isEmpty() {
		return this.size==0;
	}

	@Override
	public final String toString() {
		ToStringHelper helper =
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
						add("result",this.result);
		for(Action action:Action.values()) {
			helper.add(action.name().toLowerCase(Locale.ENGLISH),sideEffects(action));
		}
		return helper.toString();
	}

	@Override
	public final Iterator<Change<ID>> iterator() {
		return new ArrayCollectionIterator<Change<ID>>(this.changes);
	}

	@SuppressWarnings("unchecked")
	static <ID> List<Change<ID>>[] createArray(int length) {
		return (List<Change<ID>>[])Array.newInstance(List.class,length);
	}

}
