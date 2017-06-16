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

	private abstract static class Processor<A extends Annotation, R extends ResourceHandler,  M extends AbstractMutableTemplate<?>> {

		private final Class<? extends A> annotationClass;

		public Processor(Class<? extends A> annotationClass) {
			this.annotationClass = annotationClass;
		}


		M preProcess(Annotation annotation, Class<? extends R> handler) {
			checkArgument(annotationClass.isInstance(annotation),"Invalid annotation");
			return doProcess(annotationClass.cast(annotation),handler);
		}

		void postProcess(Annotation annotation, M template, TemplateResolver resolver) {
			checkArgument(annotationClass.isInstance(annotation),"Invalid annotation");
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
					updateAttachmentPredicate(template,attachment,attachedTemplate);
				} catch (IllegalArgumentException e) {
					throw new InvalidAttachmentDefinitionException(template.id(),attachment.id(),"Invalid attachment definition",e);
				}
			}
		}

		private void updateAttachmentPredicate(M template, Attachment attachment, MutableAttachedTemplate attachedTemplate) {
			String predicate = attachment.predicate();
			if(predicate!=null && predicate.length()>0) {
				try {
					attachedTemplate.setPredicate(createOptionalURI(template.id(),predicate,"The attachment predicate"));
				} catch (TemplateCreationException e) {
					throw new InvalidAttachmentDefinitionException(
									template.id(),
									attachment.id(),
									String.format("Attachment predicate value '%s' of attached template '%s' is not valid",predicate,attachedTemplate.id()),
									e);
				}
			}
		}

		protected abstract M createTemplate(A annotation, Class<? extends R> handler);

		protected abstract String name(A annotation);

		protected abstract String description(A annotation);

		protected abstract Attachment[] attachments(A annotation);

		protected final String nullable(String value) {
			String result=value.trim();
			if(result.isEmpty()) {
				result=null;
			}
			return result;
		}

		protected final URI createMandatoryURI(String id, String predicate, String uriType) {
			if(predicate.isEmpty()) {
				throw new TemplateCreationException(id,uriType+" cannot be empty");
			}
			return createOptionalURI(id, predicate, uriType);
		}

		private URI createOptionalURI(String id, String predicate, String uriType) {
			try {
				URI uri = new URI(predicate);
				if(uri.normalize().equals(URI.create(""))) {
					throw new TemplateCreationException(id,uriType+" cannot be the null URI");
				} else if(uri.isOpaque()) {
					/**
					 * TODO: Allow using opaque URIs whenever the RDF handling
					 * backend supports it (for the time being we are using
					 * Sesame and it requires using HTTP URIs)
					 */
					throw new TemplateCreationException(id,String.format("%s cannot be a opaque URI (%s)",uriType,predicate));
				} else if(!uri.isAbsolute()) {
					throw new TemplateCreationException(id,String.format("%s cannot be a hierarchical relative URI (%s)",uriType,predicate));
				}
				return uri;
			} catch (URISyntaxException e) {
				throw new TemplateCreationException(id,String.format("%s value '%s' is not valid",uriType,predicate),e);
			}
		}

	}

	private abstract static class ContainerProcessor<A extends Annotation, T extends MutableContainerTemplate> extends Processor<A,ContainerHandler,T> {

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

	private abstract static class MembershipAwareContainerProcessor<A extends Annotation, T extends MutableMembershipAwareContainerTemplate> extends ContainerProcessor<A,T> {

		public MembershipAwareContainerProcessor(Class<? extends A> annotationClass) {
			super(annotationClass);
		}

		@Override
		protected T doProcess(A annotation, Class<? extends ContainerHandler> handler) {
			T template = super.doProcess(annotation, handler);
			template.setMembershipRelation(membershipRelation(annotation));
			template.setMembershipPredicate(membershipPredicate(annotation, template));
			return template;
		}

		private URI membershipPredicate(A annotation, T template) {
			return createMandatoryURI(template.id(), membershipPredicate(annotation).trim(), "The membership predicate");
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
			String rawInsertedContentRelation = annotation.insertedContentRelation().trim();
			URI insertedContentRelation=createMandatoryURI(annotation.id(), rawInsertedContentRelation, "The inserted content relation");
			return new MutableIndirectContainerTemplate(annotation.id(),handler,insertedContentRelation);
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

	private static final class SupportedAnnotation {

		private static final SupportedAnnotation RESOURCE=new SupportedAnnotation(Resource.class,new ResourceProcessor(),false);
		private static final SupportedAnnotation BASIC_CONTAINER=new SupportedAnnotation(BasicContainer.class,new BasicContainerProcessor(),true);
		private static final SupportedAnnotation DIRECT_CONTAINER=new SupportedAnnotation(DirectContainer.class,new DirectContainerProcessor(),true);
		private static final SupportedAnnotation INDIRECT_CONTAINER=new SupportedAnnotation(IndirectContainer.class,new IndirectContainerProcessor(),true);

		private final Class<? extends Annotation> annotationClass;
		private final Processor<?,ResourceHandler,AbstractMutableTemplate<?>> processor;
		private final boolean container;

		@SuppressWarnings("unchecked")
		private <A extends Annotation, R extends ResourceHandler, M extends AbstractMutableTemplate<?>> SupportedAnnotation(Class<? extends A> annotationClass, Processor<A,R,M> processor, boolean container) {
			this.annotationClass = annotationClass;
			this.container = container;
			this.processor = (Processor<?, ResourceHandler, AbstractMutableTemplate<?>>) processor;
		}

		ResourceTemplate toTemplate(Annotation annotation, Class<? extends ResourceHandler> targetClass, TemplateRegistry templateRegistry, TemplateResolver templateResolver) {
			AbstractMutableTemplate<?> template = this.processor.preProcess(annotation,targetClass);
			templateRegistry.register(targetClass, template);
			this.processor.postProcess(annotation, template, templateResolver);
			return template;
		}

		boolean isContainer() {
			return this.container;
		}

		static SupportedAnnotation[] values() {
			return new SupportedAnnotation[]{
				SupportedAnnotation.RESOURCE,
				SupportedAnnotation.BASIC_CONTAINER,
				SupportedAnnotation.DIRECT_CONTAINER,
				SupportedAnnotation.INDIRECT_CONTAINER
			};
		}

		static SupportedAnnotation fromAnnotation(Annotation annotation) {
			for(SupportedAnnotation candidate:values()) {
				if(candidate.supports(annotation)) {
					return candidate;
				}
			}
			return null;
		}

		private boolean supports(Annotation annotation) {
			return this.annotationClass.isInstance(annotation);
		}

		static String toString(Collection<? extends SupportedAnnotation> values) {
			StringBuilder builder=new StringBuilder();
			for(Iterator<? extends SupportedAnnotation> it=values.iterator();it.hasNext();) {
				SupportedAnnotation candidate = it.next();
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

		static String toString(SupportedAnnotation... values) {
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
			ResourceTemplate template = retrieve(targetClass);
			if(template==null) {
				template=MutableTemplateLibrary.this.loadTemplates(targetClass,this);
			}
			return template;
		}

		@Override
		public void register(Class<? extends ResourceHandler> handlerClass, ResourceTemplate template) {
			ResourceTemplate previousTemplate = retrieve(handlerClass);
			if(previousTemplate!=null) {
				if(template==previousTemplate) {
					return;
				}
				throw new TemplateCreationException(template.id(), String.format("Cannot register two templates with the same handler (new: %s, registered: %s)",template,previousTemplate));
			}
			previousTemplate=this.templatesById.get(template.id());
			if(previousTemplate!=null) {
				throw new TemplateCreationException(template.id(), String.format("Cannot register two templates with the same identifier (new: %s, registered: %s)",template,previousTemplate));
			}
			this.templatesByHandler.put(HandlerId.createId(handlerClass), template);
			this.templatesById.put(template.id(), template);
		}

		private ResourceTemplate retrieve(Class<? extends ResourceHandler> handlerClass) {
			return this.templatesByHandler.get(HandlerId.createId(handlerClass));
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
		Map<SupportedAnnotation,Annotation> annotations=new LinkedHashMap<SupportedAnnotation,Annotation>();
		SupportedAnnotation found=null;
		for(Annotation annotation:targetClass.getDeclaredAnnotations()) {
			SupportedAnnotation type=SupportedAnnotation.fromAnnotation(annotation);
			if(type!=null) {
				found=type;
				annotations.put(type, annotation);
			}
		}
		if(annotations.size()==0) {
			throw new IllegalArgumentException(String.format("Class '%s' has not any of the required annotations (%s)",targetClass.getCanonicalName(),SupportedAnnotation.toString(SupportedAnnotation.values())));
		} else if(annotations.size()>1) {
			throw new IllegalArgumentException(String.format("Class '%s' is annotated with more than of the supported annotations (%s)",targetClass.getCanonicalName(),SupportedAnnotation.toString(annotations.keySet())));
		} else if(found.isContainer() && !ContainerHandler.class.isAssignableFrom(targetClass)) {
			throw new IllegalArgumentException(String.format("Not-container handler class '%s' is annotated as a container (%s)",targetClass.getCanonicalName(),SupportedAnnotation.toString(found)));
		}
		return found.toTemplate(annotations.get(found),targetClass,ctx,ctx);
	}

	boolean isHandlerRegistered(Class<?> handlerClass) {
		return findByHandler(toResourceHandlerClass(handlerClass))!=null;
	}

	ResourceTemplate registerHandler(Class<?> targetClass) {
		Class<? extends ResourceHandler> handlerClass=toResourceHandlerClass(targetClass);
		checkArgument(!this.context.isRegistered(handlerClass),"Handler '%s' is already registered",handlerClass.getCanonicalName());
		return loadTemplates(handlerClass,this.context);
	}

	@Override
	public ResourceTemplate findByHandler(Class<? extends ResourceHandler> handlerClass) {
		return this.context.retrieve(handlerClass);
	}

	@Override
	public ResourceTemplate findById(String templateId) {
		return this.context.resolve(templateId);
	}

	@Override
	public boolean contains(ResourceTemplate template) {
		checkNotNull(template,"Template cannot be null");
		return findByHandler(template.handlerClass())!=null;
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
