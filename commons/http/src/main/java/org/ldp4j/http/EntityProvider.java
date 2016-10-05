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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import java.util.List;

final class EntityProvider<T> {

	private final List<? extends T> delegate;
	private final int size;
	private int idx;

	private EntityProvider(List<? extends T> delegate) {
		this.delegate=delegate;
		this.idx=0;
		this.size=delegate.size();
	}

	void discard() {
		this.idx++;
	}

	T entity() {
		T result=null;
		if(this.idx<this.size) {
			result=this.delegate.get(this.idx);
		}
		return result;
	}

	boolean hasMoreEntities() {
		return this.idx<this.size;
	}

	boolean isExhausted() {
		return this.idx>this.size || (this.idx==this.size && this.size>0);
	}

	void reset() {
		this.idx=0;
	}

	int consumableEntities() {
		return this.delegate.isEmpty()?1:this.delegate.size();
	}

	static <T> EntityProvider<T> of(List<? extends T> entities) {
		return new EntityProvider<T>(entities);
	}

}