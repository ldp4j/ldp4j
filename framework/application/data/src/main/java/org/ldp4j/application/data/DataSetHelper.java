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

import java.io.Serializable;
import java.net.URI;

public abstract class DataSetHelper {

	public static final URI SELF = URI.create("");

	public static interface TripleProcessor<T> extends TripleConsumer {

		T getResult();

	}

	public static interface TripleConsumer {

		void consume(Individual<?,?> subject, URI predicate, Literal<?> object);

		void consume(Individual<?,?> subject, URI predicate, Individual<?,?> object);

	}

	DataSetHelper() {
	}

	public abstract <T extends Serializable, S extends Individual<T, S>> S replace(Serializable from, T to, Class<? extends S> clazz);

	public abstract ManagedIndividual manage(ManagedIndividualId id) throws DataSetModificationException;

	public abstract <T extends Individual<URI,T>> T self();

	public abstract <T extends Individual<URI,T>> T relative(URI path);

	public abstract IndividualHelper externalIndividual(URI uri);

	public abstract IndividualHelper managedIndividual(Name<?> name, String managerId);

	public abstract IndividualHelper relativeIndividual(Name<?> name, String managerId, URI path);

	public abstract IndividualHelper localIndividual(Name<?> name);

}