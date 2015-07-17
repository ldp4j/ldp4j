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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-kernel-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.template;

import org.ldp4j.application.ext.ResourceHandler;

final class ImmutableTemplateLibrary implements TemplateLibrary {

	private static final class SafeTemplateVisitor implements TemplateVisitor {

		private final TemplateVisitor visitor;

		private SafeTemplateVisitor(TemplateVisitor visitor) {
			this.visitor = visitor;
		}

		@Override
		public void visitResourceTemplate(ResourceTemplate template) {
			ImmutableTemplateFactory.newImmutable(template).accept(this.visitor);
		}

		@Override
		public void visitContainerTemplate(ContainerTemplate template) {
			ImmutableTemplateFactory.newImmutable(template).accept(this.visitor);
		}

		@Override
		public void visitBasicContainerTemplate(BasicContainerTemplate template) {
			ImmutableTemplateFactory.newImmutable(template).accept(this.visitor);
		}

		@Override
		public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
			ImmutableTemplateFactory.newImmutable(template).accept(this.visitor);
		}

		@Override
		public void visitDirectContainerTemplate(DirectContainerTemplate template) {
			ImmutableTemplateFactory.newImmutable(template).accept(this.visitor);
		}

		@Override
		public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
			ImmutableTemplateFactory.newImmutable(template).accept(this.visitor);
		}
	}

	private final TemplateLibrary delegate;

	ImmutableTemplateLibrary(TemplateLibrary delegate) {
		this.delegate = delegate;
	}

	private TemplateLibrary delegate() {
		return this.delegate;
	}

	// TODO: Returning a mutable version...
	@Override
	public ResourceTemplate findByHandler(Class<? extends ResourceHandler> handlerClass) {
		return delegate().findByHandler(handlerClass);
	}

	// TODO: Returning a mutable version...
	@Override
	public ResourceTemplate findById(String templateId) {
		return delegate().findById(templateId);
	}

	@Override
	public boolean contains(ResourceTemplate template) {
		return delegate().contains(template);
	}

	@Override
	public void accept(final TemplateVisitor visitor) {
		delegate().accept(new SafeTemplateVisitor(visitor));
	}
}