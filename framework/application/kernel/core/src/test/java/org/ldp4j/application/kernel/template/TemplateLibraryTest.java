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
package org.ldp4j.application.kernel.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

public abstract class TemplateLibraryTest {

	protected static abstract class TemplateConsumer implements TemplateVisitor {

		protected abstract void processTemplate(ResourceTemplate template);

		@Override
		public final void visitResourceTemplate(ResourceTemplate template) {
			processTemplate(template);
		}

		@Override
		public final void visitContainerTemplate(ContainerTemplate template) {
			processTemplate(template);
		}

		@Override
		public final void visitBasicContainerTemplate(BasicContainerTemplate template) {
			processTemplate(template);
		}

		@Override
		public final void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
			processTemplate(template);
		}

		@Override
		public final void visitDirectContainerTemplate(DirectContainerTemplate template) {
			processTemplate(template);
		}

		@Override
		public final void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
			processTemplate(template);
		}

	}

	protected abstract TemplateLibrary getLibrary();

	@Test
	public void testTemplateHasCustomString() {
		TemplateLibrary library=getLibrary();
		library.accept(
			new TemplateConsumer() {

				private void assertThatHasCustomString(Object obj) {
					assertThat(obj.toString(),not(equalTo(obj.getClass().getName()+"@"+System.identityHashCode(obj))));
				}

				@Override
				protected void processTemplate(ResourceTemplate template) {
					assertThatHasCustomString(template);
					for(AttachedTemplate attachedTemplate:template.attachedTemplates()) {
						assertThatHasCustomString(attachedTemplate);
					}
				}

			}
		);
	}

}
