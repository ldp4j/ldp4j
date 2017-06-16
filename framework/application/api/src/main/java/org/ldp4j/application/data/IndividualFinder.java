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

import java.io.Serializable;

final class IndividualFinder implements IndividualVisitor {

	private final DataSet dataSet;
	private Individual<?,?> found=null;

	IndividualFinder(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	@SuppressWarnings("unchecked")
	// TODO: fully generify when Vistor is generic
	<T extends Serializable> Individual<T,?> findOrCreate(Individual<T,?> individual) {
		individual.accept(this);
		return (Individual<T,?>)this.found;
	}

	@Override
	public void visitManagedIndividual(ManagedIndividual individual) {
		this.found=this.dataSet.individual(individual.id(),ManagedIndividual.class);
	}

	@Override
	public void visitLocalIndividual(LocalIndividual individual) {
		this.found=this.dataSet.individual(individual.id(),LocalIndividual.class);
	}

	@Override
	public void visitExternalIndividual(ExternalIndividual individual) {
		this.found=this.dataSet.individual(individual.id(),ExternalIndividual.class);
	}

	@Override
	public void visitRelativeIndividual(RelativeIndividual individual) {
		this.found=this.dataSet.individual(individual.id(),RelativeIndividual.class);
	}

	@Override
	public void visitNewIndividual(NewIndividual individual) {
		this.found=this.dataSet.individual(individual.id(),NewIndividual.class);
	}
}