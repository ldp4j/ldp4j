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
package org.ldp4j.application.engine.template;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.ext.annotations.Attachment;
import org.ldp4j.application.ext.annotations.BasicContainer;
import org.ldp4j.application.ext.annotations.DirectContainer;
import org.ldp4j.application.ext.annotations.IndirectContainer;
import org.ldp4j.application.ext.annotations.MembershipRelation;
import org.ldp4j.application.ext.annotations.Resource;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

final class MutableTemplateLibrary implements TemplateLibrary {

	private static interface TemplateRegistry {

		void register(Class<? extends ResourceHandler> handlerClass, ResourceTemplate template);

	}

	private static interface TemplateResolver {

		ResourceTemplate resolve(Class<? extends ResourceHandler> targetClass);

	}

	private static abstract class Processor<A extends Annotation, R extends ResourceHandler,  M extends AbstractMutableTemplate<?>> {

		private final Class<? extends A> annotationClass;

		public Processor(Class<? extends A> annotationClass) {
			this.annotationClass = annotationClass;
		}


		public M preProcess(Annotation annotation, Class<? extends R> handler, TemplateResolver resolver) {
			if(!annotationClass.isInstance(annotation)) {
				throw new IllegalArgumentException("Invalid annotation");
			}
			return doProcess(annotationClass.cast(annotation),handler);
		}

		public void postProcess(Annotation annotation, M template, TemplateResolver resolver) {
			if(!annotationClass.isInstance(annotation)) {
				throw new IllegalArgumentException("Invalid annotation");
			}
			doPostprocess(annotationClass.cast(annotation),template,resolver);
		}

		protected M doProcess(A annotation, Class<? extends R> handler) {
			M template = createTemplate(annotation,handler);
			template.setName(nullable(name(annotation)));
			template.setDescription(nullable(description(annotation)));
			return template;
		}

		protected void doPostprocess(A annotation, M template, TemplateResolver resolver) {
			for(Attachment attachment:attachments(annotation)) {
				try {

					MutableAttachedTemplate attachedTemplate = template.attachTemplate(attachment.id(), resolver.resolve(attachment.handler()), attachment.path());
					String predicate = attachment.predicate();
					if(predicate!=null && predicate.length()>0) {
						try {
							attachedTemplate.setPredicate(new URI(predicate));
						} catch (URISyntaxException e) {
							throw new InvalidAttachmentDefinitionException(template.id(),attachment.id(),"Predicate value '"+predicate+"' is not valid",e);
						}
					}
				} catch (IllegalArgumentException e) {
					throw new InvalidAttachmentDefinitionException(template.id(),attachment.id(),"Invalid attachment definition",e);
				}
			}
		}

		protected abstract M createTemplate(A annotation, Class<? extends R> handler);

		protected abstract String name(A annotation);

		protected abstract String description(A annotation);

		protected abstract Attachment[] attachments(A annotation);

		protected final String nullable(String value) {
			String result=value;
			if(result!=null && result.length()==0) {
				result=null;
			}
			return result;
		}

	}

	private static abstract class ContainerProcessor<A extends Annotation, T extends MutableContainerTemplate> extends Processor<A,ContainerHandler,T> {

		public ContainerProcessor(Class<? extends A> annotationClass) {
			super(annotationClass);
		}

		@Override
		protected T doProcess(A annotation, Class<? extends ContainerHandler> handler) {
			T template = super.doProcess(annotation, handler);
			template.setMemberPath(nullable(memberPath(annotation)));
			return template;
		}

		@Override
		protected void doPostprocess(A annotation, T template, TemplateResolver resolver) {
			super.doPostprocess(annotation,template, resolver);
			Class<? extends ResourceHandler> handler = memberHandler(annotation);
			ResourceTemplate memberTemplate = resolver.resolve(handler);
			if(memberTemplate==null) {
				throw new TemplateCreationException(template.id(), "Could not resolve template for member handler '"+handler.getCanonicalName()+"' ");
			}
			template.setMemberTemplate(memberTemplate);
		}

		protected abstract String memberPath(A annotation);

		protected abstract Class<? extends ResourceHandler> memberHandler(A annotation);

	}

	private static abstract class MembershipAwareContainerProcessor<A extends Annotation, T extends MutableMembershipAwareContainerTemplate> extends ContainerProcessor<A,T> {

		public MembershipAwareContainerProcessor(Class<? extends A> annotationClass) {
			super(annotationClass);
		}

		@Override
		protected T doProcess(A annotation, Class<? extends ContainerHandler> handler) {
			T template = super.doProcess(annotation, handler);
			template.setMembershipRelation(membershipRelation(annotation));
			try {
				template.setMembershipPredicate(new URI(membershipPredicate(annotation)));
			} catch (URISyntaxException e) {
				throw new TemplateCreationException(template.id(),"Membership predicate value '"+membershipPredicate(annotation)+"' is not valid",e);
			}
			return template;
		}

		protected abstract String membershipPredicate(A annotation);

		protected abstract MembershipRelation membershipRelation(A annotation);

	}

	private static class ResourceProcessor extends Processor<Resource,ResourceHandler,MutableResourceTemplate> {

		public ResourceProcessor() {
			super(Resource.class);
		}

		@Override
		protected MutableResourceTemplate createTemplate(Resource annotation, Class<? extends ResourceHandler> handler) {
			return new MutableResourceTemplate(annotation.id(), handler);
		}

		@Override
		protected String name(Resource annotation) {
			return annotation.name();
		}

		@Override
		protected String description(Resource annotation) {
			return annotation.description();
		}

		@Override
		protected Attachment[] attachments(Resource annotation) {
			return annotation.attachments();
		}

	}

	private static class BasicContainerProcessor extends ContainerProcessor<BasicContainer,MutableBasicContainerTemplate> {

		public BasicContainerProcessor() {
			super(BasicContainer.class);
		}

		@Override
		protected MutableBasicContainerTemplate createTemplate(BasicContainer annotation, Class<? extends ContainerHandler> handler) {
			return new MutableBasicContainerTemplate(annotation.id(), handler);
		}

		@Override
		protected String name(BasicContainer annotation) {
			return annotation.name();
		}

		@Override
		protected String description(BasicContainer annotation) {
			return annotation.description();
		}

		@Override
		protected Attachment[] attachments(BasicContainer annotation) {
			return annotation.attachments();
		}

		@Override
		protected String memberPath(BasicContainer annotation) {
			return annotation.memberPath();
		}

		@Override
		protected Class<? extends ResourceHandler> memberHandler(BasicContainer annotation) {
			return annotation.memberHandler();
		}

	}

	private static class DirectContainerProcessor extends MembershipAwareContainerProcessor<DirectContainer,MutableDirectContainerTemplate> {

		public DirectContainerProcessor() {
			super(DirectContainer.class);
		}

		@Override
		protected MutableDirectContainerTemplate createTemplate(DirectContainer annotation, Class<? extends ContainerHandler> handler) {
			return new MutableDirectContainerTemplate(annotation.id(), handler);
		}

		@Override
		protected String name(DirectContainer annotation) {
			return annotation.name();
		}

		@Override
		protected String description(DirectContainer annotation) {
			return annotation.description();
		}

		@Override
		protected Attachment[] attachments(DirectContainer annotation) {
			return annotation.attachments();
		}

		@Override
		protected String memberPath(DirectContainer annotation) {
			return annotation.memberPath();
		}

		@Override
		protected Class<? extends ResourceHandler> memberHandler(DirectContainer annotation) {
			return annotation.memberHandler();
		}

		@Override
		protected String membershipPredicate(DirectContainer annotation) {
			return annotation.membershipPredicate();
		}

		@Override
		protected MembershipRelation membershipRelation(DirectContainer annotation) {
			return annotation.membershipRelation();
		}

	}

	private static class IndirectContainerProcessor extends MembershipAwareContainerProcessor<IndirectContainer,MutableIndirectContainerTemplate> {

		public IndirectContainerProcessor() {
			super(IndirectContainer.class);
		}

		@Override
		protected MutableIndirectContainerTemplate createTemplate(IndirectContainer annotation, Class<? extends ContainerHandler> handler) {
			try {
				URI insertedContentRelation = new URI(annotation.insertedContentRelation());
				if(insertedContentRelation.normalize().equals(URI.create(""))) {
					throw new TemplateCreationException(annotation.id(),"The inserted content relation cannot be the null URI");
				}
				return new MutableIndirectContainerTemplate(annotation.id(), handler,insertedContentRelation);
			} catch (URISyntaxException e) {
				throw new TemplateCreationException(annotation.id(),"Inserted content relation value '"+annotation.insertedContentRelation()+"' is not valid",e);
			}
		}

		@Override
		protected String name(IndirectContainer annotation) {
			return annotation.name();
		}

		@Override
		protected String description(IndirectContainer annotation) {
			return annotation.description();
		}

		@Override
		protected Attachment[] attachments(IndirectContainer annotation) {
			return annotation.attachments();
		}

		@Override
		protected String memberPath(IndirectContainer annotation) {
			return annotation.memberPath();
		}

		@Override
		protected Class<? extends ResourceHandler> memberHandler(IndirectContainer annotation) {
			return annotation.memberHandler();
		}

		@Override
		protected String membershipPredicate(IndirectContainer annotation) {
			return annotation.membershipPredicate();
		}

		@Override
		protected MembershipRelation membershipRelation(IndirectContainer annotation) {
			return annotation.membershipRelation();
		}
	}

	private enum SupportedAnnotations {
		RESOURCE(Resource.class,new ResourceProcessor()),
		BASIC_CONTAINER(BasicContainer.class,new BasicContainerProcessor()),
		DIRECT_CONTAINER(DirectContainer.class,new DirectContainerProcessor()),
		INDIRECT_CONTAINER(IndirectContainer.class,new IndirectContainerProcessor())
		;

		private final Class<? extends Annotation> annotationClass;
		private final Processor<?,ResourceHandler,AbstractMutableTemplate<?>> processor;

		@SuppressWarnings("unchecked")
		private <A extends Annotation, R extends ResourceHandler, M extends AbstractMutableTemplate<?>> SupportedAnnotations(Class<? extends A> annotationClass, Processor<A,R,M> processor) {
			this.annotationClass = annotationClass;
			this.processor = (Processor<?, ResourceHandler, AbstractMutableTemplate<?>>) processor;
		}

		ResourceTemplate toTemplate(Annotation annotation, Class<? extends ResourceHandler> targetClass, TemplateRegistry templateRegistry, TemplateResolver templateResolver) {
			AbstractMutableTemplate<?> template = this.processor.preProcess(annotation,targetClass,templateResolver);
			templateRegistry.register(targetClass, template);
			this.processor.postProcess(annotation, template, templateResolver);
			return template;
		}

		static SupportedAnnotations fromAnnotation(Annotation annotation) {
			for(SupportedAnnotations candidate:values()) {
				if(candidate.supports(annotation)) {
					return candidate;
				}
			}
			return null;
		}

		private boolean supports(Annotation annotation) {
			return this.annotationClass.isInstance(annotation);
		}

		static String toString(Collection<? extends SupportedAnnotations> values) {
			StringBuilder builder=new StringBuilder();
			for(Iterator<? extends SupportedAnnotations> it=values.iterator();it.hasNext();) {
				SupportedAnnotations candidate = it.next();
				builder.append(candidate.annotationName());
				if(it.hasNext()) {
					builder.append(", ");
				}
			}
			return builder.toString();
		}

		private String annotationName() {
			return annotationClass.getCanonicalName();
		}

		static String toString(SupportedAnnotations[] values) {
			return toString(Arrays.asList(values));
		}

	}

	private final class TemplateLoaderContext implements TemplateResolver, TemplateRegistry {

		private final Map<HandlerId,ResourceTemplate> templatesByHandler;
		private final Map<String, ResourceTemplate> templatesById;

		private TemplateLoaderContext() {
			this.templatesByHandler=Maps.newLinkedHashMap();
			this.templatesById=Maps.newLinkedHashMap();
		}

		private boolean isRegistered(Class<? extends ResourceHandler> handlerClass) {
			return this.templatesByHandler.containsKey(HandlerId.createId(handlerClass));
		}

		@Override
		public ResourceTemplate resolve(Class<? extends ResourceHandler> targetClass) {
			ResourceTemplate template = this.templatesByHandler.get(HandlerId.createId(targetClass));
			if(template==null) {
				template=MutableTemplateLibrary.this.loadTemplates(targetClass,this);
			}
			return template;
		}

		@Override
		public void register(Class<? extends ResourceHandler> handlerClass, ResourceTemplate template) {
			ResourceTemplate previousTemplate = this.templatesByHandler.get(HandlerId.createId(handlerClass));
			if(previousTemplate!=null) {
				if(template==previousTemplate) {
					return;
				}
				throw new TemplateCreationException(template.id(), String.format("Cannot register two templates with the same handler (new: %s, registered: %s)",template,previousTemplate));
			}
			previousTemplate=this.templatesById.get(template.id());
			if(this.templatesById.containsKey(template.id()) ) {
				throw new TemplateCreationException(template.id(), String.format("Cannot register two templates with the same identifier (new: %s, registered: %s)",template,previousTemplate));
			}
			this.templatesByHandler.put(HandlerId.createId(handlerClass), template);
			this.templatesById.put(template.id(), template);
		}

		private ResourceTemplate resolve(String templateId) {
			return this.templatesById.get(templateId);
		}

		private Collection<ResourceTemplate> registeredTemplates() {
			return ImmutableSet.copyOf(this.templatesByHandler.values());
		}

	}

	private final TemplateLoaderContext context;

	MutableTemplateLibrary() {
		this.context=new TemplateLoaderContext();
	}

	private Class<? extends ResourceHandler> toResourceHandlerClass(Class<?> targetClass) {
		checkArgument(ResourceHandler.class.isAssignableFrom(targetClass),"Class '%s' does not implement '%s'",targetClass.getCanonicalName(),ResourceHandler.class.getCanonicalName());
		return targetClass.asSubclass(ResourceHandler.class);
	}

	private ResourceTemplate loadTemplates(Class<? extends ResourceHandler> targetClass, TemplateLoaderContext resolver) {
		ResourceTemplate template=createTemplate(targetClass,resolver);
		resolver.register(targetClass,template);
		return template;
	}

	private ResourceTemplate createTemplate(Class<? extends ResourceHandler> targetClass, final TemplateLoaderContext ctx) {
		Map<SupportedAnnotations,Annotation> annotations=new LinkedHashMap<SupportedAnnotations,Annotation>();
		SupportedAnnotations found=null;
		for(Annotation annotation:targetClass.getDeclaredAnnotations()) {
			SupportedAnnotations type=SupportedAnnotations.fromAnnotation(annotation);
			if(type!=null) {
				found=type;
				annotations.put(type, annotation);
			}
		}
		if(annotations.size()==0) {
			throw new IllegalArgumentException(String.format("Class '%s' has not any of the required annotations (%s)",targetClass.getCanonicalName(),SupportedAnnotations.toString(SupportedAnnotations.values())));
		} else if(annotations.size()>1) {
			throw new IllegalArgumentException(String.format("Class '%s' is annotated with more than of the supported annotations (%s)",targetClass.getCanonicalName(),SupportedAnnotations.toString(annotations.keySet())));
		}
		return found.toTemplate(annotations.get(found),targetClass,ctx,ctx);
	}

	public boolean isHandlerRegistered(Class<?> handlerClass) {
		return findByHandler(toResourceHandlerClass(handlerClass))!=null;
	}

	public ResourceTemplate registerHandler(Class<?> targetClass) {
		Class<? extends ResourceHandler> handlerClass=toResourceHandlerClass(targetClass);
		checkArgument(!this.context.isRegistered(handlerClass),"Handler '%s' is already registered",handlerClass.getCanonicalName());
		return loadTemplates(handlerClass,this.context);
	}

	@Override
	public ResourceTemplate findByHandler(Class<? extends ResourceHandler> handlerClass) {
		return this.context.resolve(handlerClass);
	}

	@Override
	public ResourceTemplate findById(String templateId) {
		return this.context.resolve(templateId);
	}

	@Override
	public boolean contains(ResourceTemplate template) {
		checkNotNull(template,"Template cannot be null");
		return this.context.resolve(template.handlerClass())!=null;
	}

	@Override
	public void accept(TemplateVisitor visitor) {
		checkNotNull(visitor,"Template visitor cannot be null");
		for(ResourceTemplate template:this.context.registeredTemplates()) {
			template.accept(visitor);
		}
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("templates",this.context.registeredTemplates()).
					toString();
	}

}
