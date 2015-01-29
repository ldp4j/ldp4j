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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-reflection:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-reflection-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.reflect.util;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.ldp4j.reflect.Reflection;
import org.ldp4j.reflect.meta.MetaClass;

import com.google.common.collect.Sets;

public class MetaClassHierarchyIterator implements Iterator<MetaClass<?>> {

	private final LinkedList<MetaClass<?>> pendingClasses;
	private final LinkedList<MetaClass<?>> pendingInterfaces;
	private final Set<MetaClass<?>> visitedClasses;
	private MetaClass<?> next;

	private MetaClassHierarchyIterator(MetaClass<?> root) {
		this.visitedClasses = Sets.newIdentityHashSet();
		this.pendingClasses = new LinkedList<MetaClass<?>>();
		this.pendingInterfaces = new LinkedList<MetaClass<?>>();
		this.pendingClasses.add(root);
	}

	@Override
	public boolean hasNext() {
		if(this.next!=null) {
			return true;
		}
		while(!(this.pendingClasses.isEmpty() && this.pendingInterfaces.isEmpty())) {
			this.next=pendingClasses.poll();
			if(this.next==null) {
				this.next=this.pendingInterfaces.poll();
			}
			if(!this.visitedClasses.contains(this.next)) {
				this.visitedClasses.add(this.next);
				MetaClass<?> superClass=this.next.getSuperclass();
				if(superClass!=null) {
					this.pendingClasses.offer(superClass);
				}
				this.pendingInterfaces.addAll(0,this.next.getInterfaces());
				return true;
			}
		}
		return false;
	}

	@Override
	public MetaClass<?> next() {
		checkState(next!=null,"No more class declarations available");
		MetaClass<?> result=next;
		this.next=null;
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove meta classes ");
	}

	public static MetaClassHierarchyIterator of(Class<?> root) {
		checkNotNull(root);
		return of(Reflection.of(root));
	}

	public static MetaClassHierarchyIterator of(MetaClass<?> root) {
		checkNotNull(root);
		return new MetaClassHierarchyIterator(root);
	}

}
