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
package org.ldp4j.rdf.bean.example.transactions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ldp4j.rdf.bean.Category;
import org.ldp4j.rdf.bean.annotations.Type;
import org.ldp4j.rdf.bean.example.services.Operation;

@Type(category=Category.ENUMERATION)
public class TransactionOperation extends Operation {
		
	private static final String COMMIT_NAME = "Commit";
	private static final String ENROLL_NAME = "Enroll";
	private static final String ROLLBACK_NAME = "Rollback";
	private static final String NOTIFY_NAME = "Notify";

	public static final TransactionOperation ENROLL=new TransactionOperation(ENROLL_NAME);
	public static final TransactionOperation COMMIT=new TransactionOperation(COMMIT_NAME);
	public static final TransactionOperation ROLLBACK=new TransactionOperation(ROLLBACK_NAME);
	public static final TransactionOperation NOTIFY=new TransactionOperation(NOTIFY_NAME);

	private static final Set<String> NAMES = 
		Collections.
			unmodifiableSet(
				new HashSet<String>(
					Arrays.asList(
						ENROLL.name(),
						COMMIT.name(),
						ROLLBACK.name(),
						NOTIFY.name())
					)
				);
	
	private final String name;

	private TransactionOperation(String name) {
		this.name = name;
	}
	
	public static Set<String> names() {
		return NAMES;
	}
	
	public String name() {
		return name;
	}
	
	public static TransactionOperation valueOf(String name) {
		if(ENROLL.name.equals(name)) {
			return ENROLL;
		}
		if(COMMIT.name.equals(name)) {
			return COMMIT;
		}
		if(ROLLBACK.name.equals(name)) {
			return ROLLBACK;
		}
		if(NOTIFY.name.equals(name)) {
			return NOTIFY;
		}
		throw new IllegalArgumentException("Unknown name '"+name+"'");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TransactionOperation)) {
			return false;
		}
		TransactionOperation other = (TransactionOperation) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
