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
package org.ldp4j.application.template;

public final class TemplateIntrospector {

	private final ResourceTemplate template;

	private boolean container;
	private boolean basicContainer;
	private boolean membershipAware;
	private boolean directContainer;
	private boolean indirectContainer;

	private TemplateIntrospector(ResourceTemplate template) {
		this.template = template;
	}
	
	public ResourceTemplate template() {
		return template;
	}

	public boolean isResource() {
		return !container;
	}

	public boolean isContainer() {
		return container;
	}
	
	public boolean isBasicContainer() {
		return basicContainer;
	}
	
	public boolean isMembershipAwareContainer() {
		return membershipAware;
	}
	
	public boolean isDirectContainer() {
		return directContainer;
	}

	public boolean isIndirectContainer() {
		return indirectContainer;
	}
	
	public static TemplateIntrospector newInstance(ResourceTemplate template) {
		final TemplateIntrospector introspector = new TemplateIntrospector(template);
		template.accept(
			new TemplateVisitor() {
				@Override
				public void visitResourceTemplate(ResourceTemplate template) {
					introspector.container=false;
				}
				@Override
				public void visitContainerTemplate(ContainerTemplate template) {
					introspector.container=true;
				}
				@Override
				public void visitBasicContainerTemplate(BasicContainerTemplate template) {
					visitContainerTemplate(template);
					introspector.basicContainer=true;
				}
				@Override
				public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
					visitContainerTemplate(template);
					introspector.membershipAware=true;
				}
				@Override
				public void visitDirectContainerTemplate(DirectContainerTemplate template) {
					visitMembershipAwareContainerTemplate(template);
					introspector.directContainer=true;
				}
				@Override
				public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
					visitMembershipAwareContainerTemplate(template);
					introspector.indirectContainer=true;
				}
			}
		);
		return introspector;
	}
}
