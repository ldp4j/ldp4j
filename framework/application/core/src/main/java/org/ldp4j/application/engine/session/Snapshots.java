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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.session;

import org.ldp4j.application.engine.template.BasicContainerTemplate;
import org.ldp4j.application.engine.template.ContainerTemplate;
import org.ldp4j.application.engine.template.DirectContainerTemplate;
import org.ldp4j.application.engine.template.IndirectContainerTemplate;
import org.ldp4j.application.engine.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.engine.template.ResourceTemplate;
import org.ldp4j.application.engine.template.TemplateVisitor;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;

public final class Snapshots {

	private static final class Checker implements TemplateVisitor {

		private Class<?> clazz;

		private boolean result;

		private Checker(Class<?> clazz) {
			this.clazz=clazz;
		}

		private boolean getResult() {
			return this.result;
		}

		@Override
		public void visitResourceTemplate(ResourceTemplate template) {
			this.result=this.clazz.isAssignableFrom(ResourceSnapshot.class);
		}
		@Override
		public void visitContainerTemplate(ContainerTemplate template) {
			this.result=this.clazz.isAssignableFrom(ContainerSnapshot.class);
		}
		@Override
		public void visitBasicContainerTemplate(BasicContainerTemplate template) {
			visitContainerTemplate(template);
		}
		@Override
		public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
			visitContainerTemplate(template);
		}
		@Override
		public void visitDirectContainerTemplate(DirectContainerTemplate template) {
			visitContainerTemplate(template);
		}
		@Override
		public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
			visitContainerTemplate(template);
		}
	}

	private Snapshots() {
	}

	public static boolean areCompatible(Class<?> clazz, ResourceTemplate template) {
		Checker checker = new Checker(clazz);
		template.accept(checker);
		return checker.getResult();
	}

}

