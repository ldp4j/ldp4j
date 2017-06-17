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

import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.ext.annotations.MembershipRelation;
import org.ldp4j.application.kernel.template.AttachedTemplate;
import org.ldp4j.application.kernel.template.BasicContainerTemplate;
import org.ldp4j.application.kernel.template.ContainerTemplate;
import org.ldp4j.application.kernel.template.DirectContainerTemplate;
import org.ldp4j.application.kernel.template.IndirectContainerTemplate;
import org.ldp4j.application.kernel.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateVisitor;

import com.google.common.base.Optional;

final class ImmutableTemplateFactory {

	private static class ResourceTemplateWrapper<T extends ResourceTemplate> implements ResourceTemplate {

		private final T template;

		private ResourceTemplateWrapper(T template) {
			this.template = template;
		}

		protected T template() {
			return this.template;
		}

		@Override
		public Iterator<AttachedTemplate> iterator() {
			return this.template.iterator();
		}

		@Override
		public String id() {
			return this.template.id();
		}

		@Override
		public Class<? extends ResourceHandler> handlerClass() {
			return this.template.handlerClass();
		}

		@Override
		public Optional<String> name() {
			return this.template.name();
		}

		@Override
		public Optional<String> description() {
			return this.template.description();
		}

		@Override
		public Set<String> attachmentIds() {
			return this.template.attachmentIds();
		}

		@Override
		public AttachedTemplate attachedTemplate(String attachmentId) {
			return this.template.attachedTemplate(attachmentId);
		}

		@Override
		public Set<AttachedTemplate> attachedTemplates() {
			return this.template.attachedTemplates();
		}

		@Override
		public void accept(TemplateVisitor visitor) {
			visitor.visitResourceTemplate(this);
		}

	}

	private static class ContainerTemplateWrapper<T extends ContainerTemplate> extends ResourceTemplateWrapper<T> implements ContainerTemplate {

		private ContainerTemplateWrapper(T template) {
			super(template);
		}

		@Override
		public Class<? extends ContainerHandler> handlerClass() {
			return template().handlerClass();
		}

		@Override
		public ResourceTemplate memberTemplate() {
			return ImmutableTemplateFactory.newImmutable(template().memberTemplate());
		}

		@Override
		public Optional<String> memberPath() {
			return template().memberPath();
		}

		@Override
		public void accept(TemplateVisitor visitor) {
			visitor.visitContainerTemplate(this);
		}

	}

	private static final class BasicContainerTemplateWrapper extends ContainerTemplateWrapper<BasicContainerTemplate> implements BasicContainerTemplate {

		private BasicContainerTemplateWrapper(BasicContainerTemplate template) {
			super(template);
		}

		@Override
		public void accept(TemplateVisitor visitor) {
			visitor.visitBasicContainerTemplate(this);
		}

	}

	private static class MembershipAwareContainerTemplateWrapper<T extends MembershipAwareContainerTemplate> extends ContainerTemplateWrapper<T> implements MembershipAwareContainerTemplate {

		private MembershipAwareContainerTemplateWrapper(T template) {
			super(template);
		}

		@Override
		public final URI membershipPredicate() {
			return template().membershipPredicate();
		}

		@Override
		public final MembershipRelation membershipRelation() {
			return template().membershipRelation();
		}

		@Override
		public void accept(TemplateVisitor visitor) {
			visitor.visitMembershipAwareContainerTemplate(this);
		}

	}

	private static final class DirectContainerTemplateWrapper extends MembershipAwareContainerTemplateWrapper<DirectContainerTemplate> implements DirectContainerTemplate {

		private DirectContainerTemplateWrapper(DirectContainerTemplate template) {
			super(template);
		}

		@Override
		public void accept(TemplateVisitor visitor) {
			visitor.visitDirectContainerTemplate(this);
		}

	}

	private static final class IndirectContainerTemplateWrapper extends MembershipAwareContainerTemplateWrapper<IndirectContainerTemplate> implements IndirectContainerTemplate {

		private IndirectContainerTemplateWrapper(IndirectContainerTemplate template) {
			super(template);
		}

		@Override
		public URI insertedContentRelation() {
			return template().insertedContentRelation();
		}

		@Override
		public void accept(TemplateVisitor visitor) {
			visitor.visitIndirectContainerTemplate(this);
		}

	}

	private static final class TemplateCreator implements TemplateVisitor {

		private ResourceTemplate immutableTemplate;

		@Override
		public void visitResourceTemplate(final ResourceTemplate template) {
			this.immutableTemplate=new ResourceTemplateWrapper<ResourceTemplate>(template);
		}

		@Override
		public void visitContainerTemplate(ContainerTemplate template) {
			this.immutableTemplate=new ContainerTemplateWrapper<ContainerTemplate>(template);
		}

		@Override
		public void visitBasicContainerTemplate(BasicContainerTemplate template) {
			this.immutableTemplate=new BasicContainerTemplateWrapper(template);
		}

		@Override
		public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
			this.immutableTemplate=new MembershipAwareContainerTemplateWrapper<MembershipAwareContainerTemplate>(template);
		}

		@Override
		public void visitDirectContainerTemplate(DirectContainerTemplate template) {
			this.immutableTemplate=new DirectContainerTemplateWrapper(template);
		}

		@Override
		public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
			this.immutableTemplate=new IndirectContainerTemplateWrapper(template);
		}

		private ResourceTemplate getCreatedTemplate() {
			return this.immutableTemplate;
		}
	}

	private ImmutableTemplateFactory() {
	}

	static ResourceTemplate newImmutable(ResourceTemplate template) {
		if(template==null) {
			return null;
		}
		TemplateCreator creator = new TemplateCreator();
		template.accept(creator);
		return creator.getCreatedTemplate();
	}

}
