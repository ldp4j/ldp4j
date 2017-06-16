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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.2
 *   Bundle      : ldp4j-application-examples-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import static org.ldp4j.application.data.IndividualReferenceBuilder.newReference;

import java.util.Date;

import org.ldp4j.application.data.DataDSL;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.ext.annotations.Resource;

/**
 * An example resource handler whose resource representations include data that
 * cannot be resolved.
 */
@Resource(
	id=BadDataResourceHandler.ID
)
public class BadDataResourceHandler extends InMemoryResourceHandler {

	private static final String HAS_FATHER = "http://www.ldp4j.org/vocabulary/example#hasFather";

	private static final String AGE = "http://www.ldp4j.org/vocabulary/example#age";

	private static final String CREATION_DATE = "http://www.ldp4j.org/vocabulary/example#creationDate";

	private static final String KNOWS = "http://www.ldp4j.org/vocabulary/example#knows";

	/**
	 * A property that will be serialized for a blank node linked to an unknown
	 * managed individual.
	 */
	public static final String CREATED_ON = "http://www.ldp4j.org/vocabulary/example#createdOn";

	/**
	 * A property that will be serialized for a blank node that is linked from
	 * an unknown individual.
	 */
	public static final String HAS_WIFE = "http://www.ldp4j.org/vocabulary/example#hasWife";

	/**
	 * The identifier of the template defined by the handler.
	 */
	public static final String ID="BadDataResourceHandler";

	/**
	 * Create a new instance.
	 */
	public BadDataResourceHandler() {
		super(ID);
	}

	/**
	 * Get custom representation with bad data
	 *
	 * @return the representation
	 */
	public DataSet getRepresentation() {
		return
			DataDSL.
				dataSet().
					individual(newReference().toLocalIndividual().named("anonymous")).
						hasLink(KNOWS).
							referringTo(newReference().toManagedIndividual("unknownTemplate1").named("r1")).
					individual(newReference().toLocalIndividual().named("anonymous")).
						hasProperty(CREATED_ON).
							withValue(new Date()).
						hasLink(KNOWS).
							referringTo(newReference().toManagedIndividual("unknownTemplate2").named("r1")).
					individual(newReference().toManagedIndividual("unknownTemplate2").named("r1")).
						hasProperty(CREATION_DATE).
							withValue(new Date()).
						hasProperty(AGE).
							withValue(34).
						hasLink(HAS_FATHER).
							toIndividual(newReference().toLocalIndividual().named("Michel")).
								hasLink(HAS_WIFE).
									referringTo(newReference().toLocalIndividual().named("Consuelo")).
					build();
	}

	/**
	 * Get all the properties that will be filtered.
	 *
	 * @return the properties to be filtered
	 */
	public static final String[] filteredProperties() {
		return new String[]{
			HAS_FATHER,
			AGE,
			CREATION_DATE,
			KNOWS
		};
	}

}
