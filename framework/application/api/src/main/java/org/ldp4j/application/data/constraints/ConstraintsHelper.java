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
package org.ldp4j.application.data.constraints;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NewIndividual;
import org.ldp4j.application.data.RelativeIndividual;
import org.ldp4j.application.data.RelativeIndividualId;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

final class ConstraintsHelper {

	private ConstraintsHelper() {
	}

	private static Individual<?, ?> createIndividual(DataSet dataSet, Serializable id) throws AssertionError {
		Individual<?,?> newIndividual=null;
		if(id instanceof ManagedIndividualId) {
			newIndividual=dataSet.individual((ManagedIndividualId)id,ManagedIndividual.class);
		} else if(id instanceof URI) {
			newIndividual=createIndividualOfUri((URI)id, dataSet);
		} else if(id instanceof Name<?>) {
			@SuppressWarnings("rawtypes")
			Name name = (Name)id;
			newIndividual=dataSet.individual(name,LocalIndividual.class);
		} else if(id instanceof RelativeIndividualId) {
			newIndividual=dataSet.individual((RelativeIndividualId)id,RelativeIndividual.class);
		} else {
			throw new AssertionError("Unsupported individual identifier type '"+id.getClass().getName()+"'");
		}
		return newIndividual;
	}

	private static Individual<?, ?> createIndividualOfUri(URI uri, DataSet dataSet) {
		Individual<?,?> tmp=null;
		if(uri.isAbsolute()) {
			tmp=dataSet.individual(uri,ExternalIndividual.class);
		} else {
			tmp=dataSet.individual(uri,NewIndividual.class);
		}
		return tmp;
	}

	private static void populate(DataSet dataSet, Collection<Serializable> individuals, ImmutableCollection.Builder<Individual<?,?>> builder) {
		for(Serializable node:individuals) {
			Individual<?,?> individual=dataSet.individualOfId(node);
			if(individual==null) {
				individual=createIndividual(dataSet, node);
			}
			builder.add(individual);
		}
	}

	static Set<Individual<?, ?>> getOrCreateIndividuals(DataSet dataSet, Set<Serializable> individuals) {
		ImmutableSet.Builder<Individual<?, ?>> builder=ImmutableSet.builder();
		populate(dataSet, individuals, builder);
		return builder.build();
	}

	static List<Individual<?, ?>> getOrCreateIndividuals(DataSet dataSet, List<Serializable> individuals) {
		ImmutableList.Builder<Individual<?, ?>> builder=ImmutableList.builder();
		populate(dataSet, individuals, builder);
		return builder.build();
	}

}
