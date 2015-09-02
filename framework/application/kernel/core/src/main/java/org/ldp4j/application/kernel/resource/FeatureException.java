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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.1.0
 *   Bundle      : ldp4j-application-kernel-core-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.resource;

public class FeatureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3427958905010583776L;

	private final String templateId;
	private final String handlerClassName;
	private final String featureClassName;

	public FeatureException(String templateId, String handlerClassName, String featureClassName, String message) {
		super(message);
		this.templateId = templateId;
		this.handlerClassName = handlerClassName;
		this.featureClassName = featureClassName;
	}

	public FeatureException(String templateId, String handlerClassName, String featureClassName, String message, Throwable t) {
		super(message,t);
		this.templateId = templateId;
		this.handlerClassName = handlerClassName;
		this.featureClassName = featureClassName;
	}


	public final String getTemplateId() {
		return templateId;
	}

	public final String getHandlerClassName() {
		return handlerClassName;
	}

	public final String getFeatureClassName() {
		return featureClassName;
	}

}