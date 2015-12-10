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

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.ldp4j.application.engine.context.Change;
import org.ldp4j.application.engine.context.Result;
import org.ldp4j.application.engine.context.Change.Action;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public final class ImmutableResult<T,ID> implements Result<T,ID> {

	private final T result;
	private final List<Change<ID>>[] changes;
	private final int size;

	private ImmutableResult(T result,List<Change<ID>>[] changes) {
		this.result = result;
		this.changes=ImmutableResult.<ID>createArray(changes.length);
		int tmp=0;
		for(int i=0;i<changes.length;i++) {
			List<Change<ID>> values = changes[i];
			this.changes[i]=ImmutableList.copyOf(values);
			tmp+=values.size();
		}
		this.size=tmp;
	}

	public T get() {
		return this.result;
	}

	public List<Change<ID>> sideEffects(Change.Action action) {
		return this.changes[action.ordinal()];
	}

	public int size() {
		return this.size;
	}

	public boolean isEmpty() {
		return this.size==0;
	}

	@Override
	public String toString() {
		ToStringHelper helper =
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
						add("result",this.result);
		for(Change.Action action:Change.Action.values()) {
			helper.add(action.name().toLowerCase(Locale.ENGLISH),sideEffects(action));
		}
		return helper.toString();
	}

	@Override
	public Iterator<Change<ID>> iterator() {
		return new Iterator<Change<ID>>() {

			private Iterator<Change<ID>> current;
			private int i;

			@Override
			public boolean hasNext() {
				if(this.current==null || !this.current.hasNext()) {
					while(this.i<ImmutableResult.this.changes.length) {
						this.current=ImmutableResult.this.changes[this.i++].iterator();
						if(!this.current.hasNext()) {
							this.current=null;
						}
					}
					if(this.current==null) {
						return false;
					}
				}
				return this.current.hasNext();
			}

			@Override
			public Change<ID> next() {
				checkState(this.current!=null);
				return this.current.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Deletion is not supported");
			}

		};
	}

	@SuppressWarnings("unchecked")
	private static <ID> List<Change<ID>>[] createArray(int length) {
		return (List<Change<ID>>[])Array.newInstance(List.class,length);
	}

	public static <T,ID> Builder<T,ID> builder() {
		return new Builder<T,ID>();
	}

	public static final class Builder<T,ID>  {

		private List<Change<ID>>[] changes;
		private T result;

		private Builder() {
			this.changes=ImmutableResult.<ID>createArray(Change.Action.values().length);
			for(Change.Action action:Change.Action.values()) {
				this.changes[action.ordinal()]=Lists.newArrayList();
			}
		}

		private void addChange(Change<ID> change) {
			this.changes[change.action().ordinal()].add(change);
		}

		public Builder<T,ID> withValue(T result) {
			this.result=result;
			return this;
		}

		public <C extends Change<ID>> Builder<T,ID> withChanges(Collection<C> changes) {
			for(C change:changes) {
				addChange(change);
			}
			return this;
		}

		public <C extends Change<ID>> Builder<T,ID> withChanges(@SuppressWarnings("unchecked") C... changes) {
			return withChanges(Arrays.asList(changes));
		}

		public <C extends Change<ID>> Builder<T,ID> withChange(C change) {
			addChange(change);
			return this;
		}

		public ImmutableResult<T,ID> build() {
			return new ImmutableResult<T,ID>(this.result,this.changes);
		}

	}

}
