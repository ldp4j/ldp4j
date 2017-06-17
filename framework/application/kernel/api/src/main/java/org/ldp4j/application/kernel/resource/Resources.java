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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-api:0.2.2
 *   Bundle      : ldp4j-application-kernel-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.resource;

import org.ldp4j.application.kernel.template.ContainerTemplate;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.SimpleTemplateVisitor;

public final class Resources {

	private static final class Checker extends SimpleTemplateVisitor {

		private Class<? extends Resource> clazz;

		private boolean result;

		private Checker(Class<? extends Resource> clazz) {
			this.clazz=clazz;
		}

		private boolean getResult() {
			return this.result;
		}

		@Override
		public void visitResourceTemplate(ResourceTemplate template) {
			this.result=this.clazz.isAssignableFrom(Resource.class);
		}

		@Override
		public void visitContainerTemplate(ContainerTemplate template) {
			this.result=this.clazz.isAssignableFrom(Container.class);
		}

	}

	private Resources() {
	}

	public static boolean areCompatible(Class<? extends Resource> clazz, ResourceTemplate template) {
		Checker checker = new Checker(clazz);
		template.accept(checker);
		return checker.getResult();
	}

}

