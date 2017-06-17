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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import java.net.URI;

import javax.ws.rs.core.Link;

import org.ldp4j.application.engine.context.CreationPreferences.InteractionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InteractionModelUtils {

	private static final Logger LOGGER=LoggerFactory.getLogger(InteractionModelUtils.class);

	private static final String INTERACTION_MODEL_LINK_REL = "type";

	private InteractionModelUtils() {
	}

	static String asLink(InteractionModel interactionModel) {
		return Link.fromUri(interactionModel.asURI()).rel(INTERACTION_MODEL_LINK_REL).build().toString();
	}

	static InteractionModel fromLink(String strLink) {
		InteractionModel result=null;
		try {
			Link link = Link.valueOf(strLink);
			if(link.getRel().equals(INTERACTION_MODEL_LINK_REL)) {
				result=toInteractionModel(link.getUri());
			}
		} catch (IllegalArgumentException e) {
			LOGGER.trace("Could not parse link '"+strLink+"'",e);
		}
		return result;
	}

	private static InteractionModel toInteractionModel(URI uri) {
		for(InteractionModel interactionModel:InteractionModel.values()) {
			if(uri.equals(interactionModel.asURI())) {
				return interactionModel;
			}
		}
		LOGGER.warn("Did not understand interaction model '{}'",uri);
		return null;
	}

}