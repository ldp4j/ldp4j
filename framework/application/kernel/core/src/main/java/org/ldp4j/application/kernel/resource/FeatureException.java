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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.resource;

import org.ldp4j.application.session.ResourceSnapshot;

public class FeatureException extends Exception {

	private static final long serialVersionUID = -3427958905010583776L;

	private final String templateId;
	private final String handlerClassName;
	private final String featureClassName;

	private FeatureException(String templateId, String handlerClassName, String featureClassName, String message, Throwable cause) {
		super(message,cause);
		this.templateId = templateId;
		this.handlerClassName = handlerClassName;
		this.featureClassName = featureClassName;
	}

	public FeatureException(ResourceSnapshot resource, Class<?> feature, String message, Throwable cause) {
		this(resource.templateId(),resource.handlerClass().getName(),feature.getName(),message,cause);
	}

	public FeatureException(ResourceSnapshot resource, Class<?> feature, Throwable cause) {
		this(resource.templateId(),resource.handlerClass().getName(),feature.getName(),"Unexpected feature exception",cause);
	}
	public FeatureException(ResourceSnapshot resource, Class<?> feature, String message) {
		this(resource.templateId(),resource.handlerClass().getName(),feature.getName(),message,null);
	}

	public final String getTemplateId() {
		return this.templateId;
	}

	public final String getHandlerClassName() {
		return this.handlerClassName;
	}

	public final String getFeatureClassName() {
		return this.featureClassName;
	}

}