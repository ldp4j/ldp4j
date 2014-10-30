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


import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

public final class Key<T> {

	public class KeyAdapter<S> {
	
		private final Class<S> clazz;
		private final boolean compatible;
	
		private KeyAdapter(Class<S> clazz) {
			this.clazz = clazz;
			this.compatible=isCompatible(clazz, owner);
		}
	
		private boolean isCompatible(Class<S> targetClazz, Class<T> sourceClazz) {
			return
				targetClazz!=null?
					targetClazz==sourceClazz:
					false;
		}
	
		@SuppressWarnings("unchecked")
		private Key<S> safeCast() {
			return (Key<S>)Key.this;
		}
	
		public Key<S> orNull() {
			return or(null);
		}
	
		public Key<S> or(Key<S> defaultValue) {
			Key<S> result=defaultValue;
			if(this.compatible) {
				result=safeCast();
			}
			return result;
		}
	
		public <E extends Exception> Key<S> orFail(E exception) throws E {
			if(!this.compatible) {
				throw exception;
			}
			return safeCast();
		}
	
		public Key<S> now() throws ClassCastException {
			return orFail(new ClassCastException("Key is not managed by '"+this.clazz.getName()+"'"));
		}
	
	}

	private final Class<T> owner;
	private final Object nativeId;

	private Key(Class<T> owner, Object nativeId) {
		this.owner = owner;
		this.nativeId = nativeId;
	}

	public Class<T> owner() {
		return this.owner;
	}

	public Object nativeId() {
		return this.nativeId;
	}

	public <S> KeyAdapter<S> keyAs(Class<S> clazz) {
		return new KeyAdapter<S>(clazz);
	}

	public <V> ObjectAdapter<V> nativeIdAs(Class<? extends V> clazz) {
		return new ObjectAdapter<V>(clazz, this.nativeId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.owner,this.nativeId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Key) {
			Key<?> that = (Key<?>) obj;
			result=
				Objects.equal(this.nativeId, that.nativeId) &&
				this.owner==that.owner;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					add("owner",this.owner.getCanonicalName()).
					add("nativeId",this.nativeId).
					toString();
	}

	public static <T> Key<T> create(Class<T> owner, Object nativeId) {
		checkNotNull(owner,"Key owner cannot be null");
		checkNotNull(nativeId,"Key native identifier cannot be null");
		return new Key<T>(owner,nativeId);
	}

}