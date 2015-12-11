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
import org.ldp4j.rdf.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SideEffectUtils {


	private static final Logger LOGGER=LoggerFactory.getLogger(SideEffectUtils.class);

	private static final String PREFIX    = "se";
	private static final String NAMESPACE = "http://www.ldp4j.org/ns/side-effects#";

	private static final String SIDE_EFFECTS_TYPE = "SideEffects";
	private static final String CHANGE_TYPE       = "Change";

	private static final String LAST_MODIFIED   = "lastModified";
	private static final String ENTITY_TAG      = "entityTag";
	private static final String TARGET_RESOURCE = "targetResource";
	private static final String ACTION          = "action";
	private static final String HAS_CHANGE      = "hasChange";

	private SideEffectUtils() {
	}

	static DataSet toDataSet(URI base, Iterable<Change> changes) {
		Name<String> name = NamingScheme.getDefault().name(PREFIX);
		DataSet result=DataSets.createDataSet(name);
		DataSetHelper helper = DataSetUtils.newHelper(result);
		IndividualHelper sideEffects =
			helper.
				localIndividual(name).
					property(RDF.TYPE).
						withIndividual(sideEffects(SIDE_EFFECTS_TYPE));

		int i=0;
		for(Change change:changes) {
			Name<Integer> changeName=NamingScheme.getDefault().name(i++);
			sideEffects.
				property(sideEffects(HAS_CHANGE)).
					withIndividual(changeName);
			IndividualHelper changeIndividual =
				helper.
					localIndividual(changeName).
						property(RDF.TYPE).
							withIndividual(sideEffects(CHANGE_TYPE)).
						property(sideEffects(ACTION)).
							withLiteral(change.action().name());
			switch(change.action()) {
				case CREATED:
					populateActiveChange(change,changeIndividual);
					break;
				case MODIFIED:
					populateActiveChange(change,changeIndividual);
					break;
				case DELETED:
					populateGoneChange(change,base,changeIndividual);
					break;
				default:
					LOGGER.error("Cannot transform change {}",change);
					break;
			}
		}
		return result;
	}

	private static URI sideEffects(String term) {
		return URI.create(NAMESPACE+term);
	}

	private static void populateActiveChange(Change change, IndividualHelper individual) {
		individual.
			property(sideEffects(TARGET_RESOURCE)).
				withIndividual(
					change.targetResource().name(),
					change.targetResource().managerId()).
			property(sideEffects(ENTITY_TAG)).
				withLiteral(change.entityTag().get()).
			property(sideEffects(LAST_MODIFIED)).
				withLiteral(change.lastModified().get());
	}

	private static void populateGoneChange(Change change, URI base, IndividualHelper individual) {
		individual.
			property(sideEffects(TARGET_RESOURCE)).
				withIndividual(base.resolve(change.resourceLocation()));
	}

	static <T> String serialize(Result<T> result, OperationContext context, Variant variant) {
		LOGGER.trace("Result to process: \n {}",result);
		DataSet entity=toDataSet(context.base(),result);
		LOGGER.trace("Side-effect report to serialize: \n {}",entity);
		return
			context.serialize(
				entity,
				getNamespaces(context),
				variant.getMediaType());
	}

	private static Namespaces getNamespaces(OperationContext context) {
		return
			NamespacesHelper.
				resourceNamespaces(context.applicationNamespaces()).
					addPrefix(PREFIX,NAMESPACE);
	}

}
