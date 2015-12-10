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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-api:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-engine-api-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.context;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public final class Result<T,ID> implements Iterable<Change<ID>> {

	private final T result;
	private final List<Change<ID>>[] changes;
	private final int size;

	private Result(T result,List<Change<ID>>[] changes) {
		this.result = result;
		this.changes=createArray(changes.length);
		int tmp=0;
		for(int i=0;i<changes.length;i++) {
			List<Change<ID>> values = changes[i];
			this.changes[i]=ImmutableList.<Change<ID>>copyOf(values);
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
					while(this.i<Result.this.changes.length) {
						this.current=Result.this.changes[this.i++].iterator();
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
			this.changes=createArray(Change.Action.values().length);
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

		public Builder<T,ID> withChanges(Collection<Change<ID>> changes) {
			for(Change<ID> change:changes) {
				addChange(change);
			}
			return this;
		}

		public Builder<T,ID> withChanges(@SuppressWarnings("unchecked") Change<ID>... changes) {
			return withChanges(Arrays.asList(changes));
		}

		public Builder<T,ID> withChange(Change<ID> change) {
			addChange(change);
			return this;
		}

		public Result<T,ID> build() {
			return new Result<T,ID>(this.result,this.changes);
		}

	}

}
