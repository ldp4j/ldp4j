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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InMemStore implements Store {

	private static final Logger LOGGER=LoggerFactory.getLogger(InMemStore.class);

	private final MutableDataSet dataSet;

	InMemStore(Name<?> name) {
		this.dataSet=new MutableDataSet(name);
	}

	@Override
	public void addLink(IndividualReference<?,?> subjectReference, URI property, IndividualReference<?,?> objectReference) {
		subjectReference.realize(this.dataSet).addValue(property, objectReference.realize(this.dataSet));
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Added object property triple: <{}, {}, {}>",subjectReference,property,objectReference);
		}
	}

	@Override
	public void addValue(IndividualReference<?,?> subjectReference, URI property, Object value) {
		subjectReference.realize(this.dataSet).addValue(property,Literals.newLiteral(value));
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Added datatype property triple: <{}, {}, {} ({})>",subjectReference,property,value,value.getClass().getCanonicalName());
		}
	}

	@Override
	public DataSet serialize() {
		return dataSet;
	}

}