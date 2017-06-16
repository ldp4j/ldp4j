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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.rdf.bean.Type;
import org.ldp4j.rdf.bean.TypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TypeRegistryImpl implements TypeRegistry {

	private static final Logger LOGGER=LoggerFactory.getLogger(TypeRegistryImpl.class);

	private static final AtomicLong COUNTER=new AtomicLong();

	private final long id;
	private final Map<Integer,Type> definitions;
	private final ReadWriteLock lock=new ReentrantReadWriteLock();

	TypeRegistryImpl() {
		this.id=COUNTER.incrementAndGet();
		this.definitions=new HashMap<Integer, Type>();
	}

	protected final long getId() {
		return id;
	}

	protected final void update(Map<Integer,Type> definitions) {
		lock.writeLock().lock();
		try {
			for(Entry<Integer,Type> entry:definitions.entrySet()) {
				Type result = this.definitions.put(entry.getKey(),entry.getValue());
				if(LOGGER.isTraceEnabled() && result==null) {
					LOGGER.trace(String.format("Added type '%s' to TypeRegistry(%d)",entry.getValue(),getId()));
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean register(Class<?> clazz, Type type) {
		lock.writeLock().lock();
		try {
			int key = System.identityHashCode(clazz);
			boolean result=!definitions.containsKey(key);
			if(result) {
				definitions.put(key,type);
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace(String.format("Registered type '%s' for class '%s' in TypeRegistry(%d)",type.getNamespace()+type.getName(),clazz,getId()));
				}
			}
			return result;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void deregister(Class<?> clazz) {
		lock.writeLock().lock();
		try {
			definitions.remove(System.identityHashCode(clazz));
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public Type lookup(Class<?> clazz) {
		lock.readLock().lock();
		try {
			return definitions.get(System.identityHashCode(clazz));
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		StringWriter result = new StringWriter();
		PrintWriter out=new PrintWriter(result);
		out.printf("TypeRegistryImpl(%d) {%n",getId());
		out.print("\tDefinitions {");
		for(Entry<?, ?> entry:definitions.entrySet()) {
			out.printf("%n\t\t%s",entry.getValue());
		}
		out.printf("%n\t}%n");
		out.printf("}%n");
		return result.toString();
	}

	protected final ReadWriteLock getLock() {
		return lock;
	}

	protected final Map<Integer,Type> getDefinitions() {
		return definitions;
	}

}