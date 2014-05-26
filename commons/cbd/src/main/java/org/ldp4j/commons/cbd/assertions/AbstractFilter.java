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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-cbd:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-cbd-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons.cbd.assertions;

import org.ldp4j.commons.cbd.IFilter;

abstract class AbstractFilter<T> implements IFilter<T> {

	protected interface IExpectation<T> {
		
		boolean isMet();
		
		String getFailureDescription();
		
		T getValue();
		
	}
	
	protected abstract <F extends T> IExpectation<F> createExpectation(F value);

	@Override
	public <F extends T> F filter(F value) {
		IExpectation<F> expectation=createExpectation(value);
		if(!expectation.isMet()) {
			throw new AssertionError(expectation.getFailureDescription());
		}
		return expectation.getValue();
	}


	@Override
	public <F extends T> F filter(F value, String name, boolean isField) {
		IExpectation<F> expectation=createExpectation(value);
		if(!expectation.isMet()) {
			if(isField) {
				throw new IllegalStateException(String.format("Field '%s' is not valid: %s",name,expectation.getFailureDescription()));
			} else {
				throw new IllegalArgumentException(String.format("Argument '%s' is not valid: %s",name,expectation.getFailureDescription()));
			}
		}
		return expectation.getValue();
	}
	
}