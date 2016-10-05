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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;


final class PropertyCollection implements Iterable<Property> {

	private final Map<URI,MutableProperty> properties;
	private final ReentrantReadWriteLock lock;
	private final MutableDataSet dataSet;
	private final Individual<?, ?> individual;
	
	PropertyCollection(Individual<?,?> individual, MutableDataSet context) {
		this.individual = individual;
		this.dataSet = context;
		this.properties=new LinkedHashMap<URI, MutableProperty>();
		this.lock=new ReentrantReadWriteLock();
	}

	@Override
	public Iterator<Property> iterator() {
		return properties().iterator();
	}

	int size() {
		this.lock.readLock().lock();
		try {
			return this.properties.size();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	boolean isEmpty() {
		this.lock.readLock().lock();
		try {
			return this.properties.isEmpty();
		} finally {
			this.lock.readLock().unlock();
		}
	}

	Collection<Property> properties() {
		this.lock.readLock().lock();
		try {
			List<Property> result = new ArrayList<Property>();
			for(MutableProperty property:this.properties.values()) {
				result.add(new ImmutableProperty(property));
			}
			return result;
		} finally {
			this.lock.readLock().unlock();
		}
	}

	boolean hasProperty(URI propertyId) {
		this.lock.readLock().lock();
		try {
			return this.properties.containsKey(propertyId);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	Property property(URI propertyId) {
		this.lock.readLock().lock();
		try {
			MutableProperty result=this.properties.get(propertyId);
			if(result!=null) {
				return new ImmutableProperty(result);
			}
			return result;
		} finally {
			this.lock.readLock().unlock();
		}
	}

	void addValue(URI propertyId, Value value) {
		checkNotNull(propertyId,"Property identifier cannot be null");
		checkNotNull(value,"Property value cannot be null");
		this.lock.writeLock().lock();
		try {
			MutableProperty property=this.properties.get(propertyId);
			if(property==null) {
				property = new MutableProperty(this.individual,this.dataSet,propertyId);
				this.properties.put(propertyId,property);
			}
			property.addValue(value);
		} finally {
			this.lock.writeLock().unlock();
		}
	}
	
	void removeValue(URI propertyId, Value value) {
		checkNotNull(propertyId,"Property identifier cannot be null");
		checkNotNull(value,"Property value cannot be null");
		this.lock.writeLock().lock();
		try {
			MutableProperty property=this.properties.get(propertyId);
			if(property!=null) {
				property.removeValue(value);
				if(!property.hasValues()) {
					this.properties.remove(propertyId);
				}
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	Set<URI> propertyIds() {
		this.lock.readLock().lock();
		try {
			return new LinkedHashSet<URI>(this.properties.keySet());
		} finally {
			this.lock.readLock().unlock();
		}
	}

}