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
package org.ldp4j.application.data;


public final class ManagedIndividualId {
	
	private final String managerId;
	private final Name<?> name;

	private ManagedIndividualId(Name<?> name, String managerId) {
		this.name = name;
		this.managerId = managerId;
	}
	
	public Name<?> name() {
		return this.name;
	}

	public String managerId() {
		return this.managerId;
	}

	@Override
	public int hashCode() {
		return this.name().hashCode()+this.managerId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj!=null && obj.getClass()==this.getClass()) {
			ManagedIndividualId that=(ManagedIndividualId)obj;
			result=
				equal(this.name(),that.name()) &&
				equal(this.managerId(),that.managerId());
		}
		return result;
	}
	
	@Override
	public String toString() {
		return 
			String.format("%s [name=%s, managerId=%s]",getClass().getCanonicalName(),name(),managerId());
	}
	
	public static ManagedIndividualId createId(Name<?> name, String managerId) {
		checkNotNull(name,"Resource name cannot be null");
		checkNotNull(managerId,"Manager identifier cannot be null");
		return new ManagedIndividualId(name,managerId);
	}

	private static <T> boolean equal(T o1, T o2) {
		if(o1!=null && o2!=null) {
			return o1.equals(o2);
		}
		return o1==null && o2==null;
	}

	private static void checkNotNull(Object obj, String errorMessage) {
		if(obj==null) {
			throw new NullPointerException(errorMessage);
		}
	}

}