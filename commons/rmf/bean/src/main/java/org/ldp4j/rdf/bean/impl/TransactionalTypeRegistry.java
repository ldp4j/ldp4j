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

import org.ldp4j.rdf.bean.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TransactionalTypeRegistry extends TypeRegistryImpl {

	private static final Logger LOGGER=LoggerFactory.getLogger(TransactionalTypeRegistry.class);

	private TransactionalTypeRegistry original;

	private final Map<Integer,Type> newDefinitions;

	TransactionalTypeRegistry() {
		super();
		this.newDefinitions=new HashMap<Integer, Type>();
	}

	@Override
	public boolean register(Class<?> clazz, Type type) {
		getLock().writeLock().lock();
		try {
			boolean result=super.register(clazz,type);
			if(result) {
				newDefinitions.put(System.identityHashCode(clazz), type);
			}
			return result;
		} finally {
			getLock().writeLock().unlock();
		}
	}

	TransactionalTypeRegistry setSavepoint() {
		getLock().readLock().lock();
		try {
			TransactionalTypeRegistry copy=new TransactionalTypeRegistry();
			copy.update(getDefinitions());
			copy.update(newDefinitions);
			copy.original=this;
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace(String.format("Created savepoint for TypeRegistry(%d)",getId()));
			}
			return copy;
		} finally {
			getLock().readLock().unlock();
		}
	}

	void rollback() {
		getLock().writeLock().lock();
		try {
			if(original!=null) {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace(String.format("Rolled back savepoint for TypeRegistry(%d)",original.getId()));
				}
				this.original=null;
				this.newDefinitions.clear();
			}
		} finally {
			getLock().writeLock().unlock();
		}
	}

	void commit() {
		getLock().writeLock().lock();
		try {
			if(!newDefinitions.isEmpty() && original!=null) {
				original.update(newDefinitions);
				original.commit();
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace(String.format("Commited savepoint for TypeRegistry(%d)",original.getId()));
				}
				this.original=null;
				this.newDefinitions.clear();
			}
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		StringWriter result = new StringWriter();
		PrintWriter out=new PrintWriter(result);
		out.printf("TransactionalTypeRegistryImpl(%d) {%n",getId());
		out.print("\tDefinitions {");
		for(Entry<?, ?> entry:this.newDefinitions.entrySet()) {
			out.printf("%n\t\t%s",entry.getValue());
		}
		out.printf("%n\t}%n");
		out.printf("}%n");
		return result.toString();
	}

}