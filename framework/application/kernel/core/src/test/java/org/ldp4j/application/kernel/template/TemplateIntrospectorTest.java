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
import static org.hamcrest.Matchers.*;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.ldp4j.example.PersonContainerHandler;

public class TemplateIntrospectorTest {


	@Test
	public void testInstrospection() {
		MutableTemplateLibrary library=new MutableTemplateLibrary();
		library.registerHandler(PersonContainerHandler.class);
		library.accept(
			new TemplateVisitor() {

				@Override
				public void visitResourceTemplate(ResourceTemplate template) {
					TemplateIntrospector introspector = TemplateIntrospector.newInstance(template);
					assertThat(introspector.template(),sameInstance(template));
					assertThat(introspector.isResource(),equalTo(true));
					assertThat(introspector.isContainer(),equalTo(false));
					assertThat(introspector.isBasicContainer(),equalTo(false));
					assertThat(introspector.isMembershipAwareContainer(),equalTo(false));
					assertThat(introspector.isDirectContainer(),equalTo(false));
					assertThat(introspector.isIndirectContainer(),equalTo(false));
				}

				@Override
				public void visitBasicContainerTemplate(BasicContainerTemplate template) {
					TemplateIntrospector introspector = TemplateIntrospector.newInstance(template);
					assertThat(introspector.template(),sameInstance((ResourceTemplate)template));
					assertThat(introspector.isResource(),equalTo(false));
					assertThat(introspector.isContainer(),equalTo(true));
					assertThat(introspector.isBasicContainer(),equalTo(true));
					assertThat(introspector.isMembershipAwareContainer(),equalTo(false));
					assertThat(introspector.isDirectContainer(),equalTo(false));
					assertThat(introspector.isIndirectContainer(),equalTo(false));
				}

				@Override
				public void visitDirectContainerTemplate(DirectContainerTemplate template) {
					TemplateIntrospector introspector = TemplateIntrospector.newInstance(template);
					assertThat(introspector.template(),sameInstance((ResourceTemplate)template));
					assertThat(introspector.isResource(),equalTo(false));
					assertThat(introspector.isContainer(),equalTo(true));
					assertThat(introspector.isBasicContainer(),equalTo(false));
					assertThat(introspector.isMembershipAwareContainer(),equalTo(true));
					assertThat(introspector.isDirectContainer(),equalTo(true));
					assertThat(introspector.isIndirectContainer(),equalTo(false));
					assertThat(introspector.getMembershipPredicate(),equalTo(template.membershipPredicate()));
					assertThat(introspector.getMembershipRelation(),equalTo(template.membershipRelation()));
				}

				@Override
				public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
					TemplateIntrospector introspector = TemplateIntrospector.newInstance(template);
					assertThat(introspector.template(),sameInstance((ResourceTemplate)template));
					assertThat(introspector.isResource(),equalTo(false));
					assertThat(introspector.isContainer(),equalTo(true));
					assertThat(introspector.isBasicContainer(),equalTo(false));
					assertThat(introspector.isMembershipAwareContainer(),equalTo(true));
					assertThat(introspector.isDirectContainer(),equalTo(false));
					assertThat(introspector.isIndirectContainer(),equalTo(true));
					assertThat(introspector.getMembershipPredicate(),equalTo(template.membershipPredicate()));
					assertThat(introspector.getMembershipRelation(),equalTo(template.membershipRelation()));
					assertThat(introspector.getInsertedContentRelation(),equalTo(template.insertedContentRelation()));
				}

				@Override
				public void visitContainerTemplate(ContainerTemplate template) {
					fail("Unexpected template type");
				}

				@Override
				public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
					fail("Unexpected template type");
				}
			}
		);

	}
}
