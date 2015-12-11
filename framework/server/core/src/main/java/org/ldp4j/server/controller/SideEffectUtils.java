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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import java.net.URI;

import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetHelper;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.IndividualHelper;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.engine.context.Change;
import org.ldp4j.application.engine.context.Result;
import org.ldp4j.application.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SideEffectUtils {

	private static final Logger LOGGER=LoggerFactory.getLogger(SideEffectUtils.class);

	private SideEffectUtils() {
	}

	static DataSet toDataSet(URI base, Iterable<Change> changes) {
		Name<String> name = NamingScheme.getDefault().name("sideEffects");
		DataSet result=DataSets.createDataSet(name);
		DataSetHelper helper = DataSetUtils.newHelper(result);
		IndividualHelper sideEffects =
			helper.
				localIndividual(name).
					property(RDF.TYPE).
						withIndividual(ldp4jTerm("SideEffects"));

		int i=0;
		for(Change change:changes) {
			Name<Integer> changeName=NamingScheme.getDefault().name(i++);
			sideEffects.
				property(ldp4jTerm("hasChange")).
					withIndividual(changeName);
			IndividualHelper changeIndividual =
				helper.
					localIndividual(changeName).
						property(RDF.TYPE).
							withIndividual(ldp4jTerm("Change"));
			switch(change.action()) {
				case CREATED:
					populateCreationChange(change,changeIndividual);
					break;
				case MODIFIED:
					populateModificationChange(change,changeIndividual);
					break;
				case DELETED:
					populateDeletionChange(change,base,changeIndividual);
					break;
				default:
					LOGGER.error("Cannot transform change {}",change);
					break;
			}
		}
		return result;
	}

	private static URI ldp4jTerm(String term) {
		return URI.create("http://www.ldp4j.org/ns/side-effects#"+term);
	}

	private static void populateModificationChange(Change change, IndividualHelper individual) {
		individual.
			property(ldp4jTerm("action")).
				withLiteral("MODIFICATION").
			property(ldp4jTerm("targetResource")).
				withIndividual(
					change.targetResource().name(),
					change.targetResource().managerId()).
			property(ldp4jTerm("entityTag")).
				withLiteral(change.entityTag().get()).
			property(ldp4jTerm("lastModified")).
				withLiteral(change.lastModified().get());
	}

	private static void populateCreationChange(Change change, IndividualHelper individual) {
		individual.
			property(ldp4jTerm("action")).
				withLiteral("CREATED").
			property(ldp4jTerm("targetResource")).
				withIndividual(
					change.targetResource().name(),
					change.targetResource().managerId()).
			property(ldp4jTerm("entityTag")).
				withLiteral(change.entityTag().get()).
			property(ldp4jTerm("lastModified")).
				withLiteral(change.lastModified().get());
	}

	private static void populateDeletionChange(Change change, URI base, IndividualHelper individual) {
		individual.
			property(ldp4jTerm("action")).
				withLiteral("DELETED").
			property(ldp4jTerm("targetResource")).
				withIndividual(base.resolve(change.resourceLocation()));
	}

	static <T> String serialize(Result<T> result, OperationContext context, Variant variant) {
		LOGGER.trace("Result to process: \n {}",result);
		DataSet entity=toDataSet(context.base(),result);
		LOGGER.trace("Side-effect report to serialize: \n {}",entity);
		return
			context.serialize(
				entity,
				NamespacesHelper.resourceNamespaces(context.applicationNamespaces()),
				variant.getMediaType());
	}

}
