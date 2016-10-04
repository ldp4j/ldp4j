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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-api:0.2.2
 *   Bundle      : ldp4j-application-engine-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.context;

import java.util.Set;

import org.ldp4j.application.engine.context.CreationPreferences.InteractionModel;

import com.google.common.collect.ImmutableSet;

public class UnsupportedInteractionModelException extends OperationPrecondititionException {

	private static final long serialVersionUID = -5693006810913606248L;

	private final InteractionModel requiredInteractionModel;
	private final Set<InteractionModel> supportedInteractionModels;

	public UnsupportedInteractionModelException(InteractionModel requiredInteractionModel, Set<InteractionModel> supportedInteractionModels) {
		super("Unsupported interaction model '"+requiredInteractionModel+"'. The resource only supports: "+supportedInteractionModels);
		this.requiredInteractionModel = requiredInteractionModel;
		this.supportedInteractionModels = ImmutableSet.copyOf(supportedInteractionModels);
	}

	public InteractionModel getRequiredInteractionModel() {
		return requiredInteractionModel;
	}

	public Set<InteractionModel> getSupportedInteractionModels() {
		return supportedInteractionModels;
	}

}
