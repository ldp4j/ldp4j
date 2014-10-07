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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ldp4j.application.ext.ResourceHandler;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.MutableClassToInstanceMap;

public final class TemplateManager {

	private static final class HandlerMapBuilder implements TemplateVisitor {

		private final Builder<HandlerId, ResourceHandler> builder;

		private HandlerMapBuilder(Builder<HandlerId, ResourceHandler> builder) {
			this.builder = builder;
		}

		@Override
		public void visitResourceTemplate(ResourceTemplate template) {
			Class<? extends ResourceHandler> handlerClass = template.handlerClass();
			try {
				ResourceHandler newInstance = handlerClass.newInstance();
				this.builder.put(HandlerId.createId(handlerClass), newInstance);
			} catch (InstantiationException e) {
				throw new IllegalStateException("Could not instantiate resource handler from template '"+template.id()+"' ("+handlerClass.getCanonicalName()+")");
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Could not instantiate resource handler from template '"+template.id()+"' ("+handlerClass.getCanonicalName()+")");
			}
		}

		@Override
		public void visitContainerTemplate(ContainerTemplate template) {
			visitResourceTemplate(template);
		}

		@Override
		public void visitBasicContainerTemplate(BasicContainerTemplate template) {
			visitResourceTemplate(template);
		}

		@Override
		public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
			visitResourceTemplate(template);
		}

		@Override
		public void visitDirectContainerTemplate(DirectContainerTemplate template) {
			visitResourceTemplate(template);
		}

		@Override
		public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
			visitResourceTemplate(template);
		}
	}

	private final TemplateLibrary library;
	private final ImmutableMap<HandlerId, ResourceHandler> handlers;

	private TemplateManager(TemplateLibrary library, ImmutableMap<HandlerId, ResourceHandler> handlers) {
		this.library = library;
		this.handlers = handlers;
	}
	
	private <T extends ResourceTemplate> T castTemplate(Class<? extends T> templateClass, ResourceTemplate template) throws InvalidTemplateClassException {
		if(template!=null && !templateClass.isInstance(template)) {
			throw new InvalidTemplateClassException(templateClass,template);
		}
		return templateClass.cast(template);
	}
	
	public ResourceTemplate getTemplate(String templateId) {
		checkNotNull(templateId,"Template identifier cannot be null");
		return this.library.findById(templateId);

	}
	public ResourceTemplate getTemplate(Class<? extends ResourceHandler> handlerClass) {
		checkNotNull(handlerClass,"Handler class cannot be null");
		return this.library.findByHandler(handlerClass);
	}

	public <T extends ResourceTemplate> T getTemplate(Class<? extends T> templateClass, String templateId) throws InvalidTemplateClassException {
		checkNotNull(templateClass,"Template handler class cannot be null");
		checkNotNull(templateId,"Template identifier cannot be null");
		return castTemplate(templateClass, library.findById(templateId));
	}

	public <T extends ResourceTemplate> T getTemplate(Class<? extends T> templateClass, Class<? extends ResourceHandler> handlerClass) throws InvalidTemplateClassException {
		checkNotNull(templateClass,"Template handler class cannot be null");
		checkNotNull(handlerClass,"Handler class cannot be null");
		return castTemplate(templateClass, library.findByHandler(handlerClass));
	}

	public <T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass, ResourceTemplate template) {
		checkNotNull(handlerClass,"Handler class cannot be null");
		checkNotNull(template,"Template cannot be null");
		checkArgument(this.library.contains(template),"Unknown template '"+template+"'");
		checkArgument(handlerClass.isAssignableFrom(template.handlerClass()));
		return handlerClass.cast(this.handlers.get(HandlerId.createId(template.handlerClass())));
	}
	
	private static ImmutableMap<HandlerId, ResourceHandler> createHandlerMap(TemplateLibrary library) {
		Builder<HandlerId, ResourceHandler> builder = ImmutableMap.<HandlerId, ResourceHandler>builder();
		library.accept(new HandlerMapBuilder(builder));
		return builder.build();
	}

	@Deprecated
	public static TemplateManager newInstance(Collection<Class<?>> handlerClasses) throws TemplateLibraryLoadingException {
		checkNotNull(handlerClasses,"Handler class collection cannot be null");
		checkArgument(!handlerClasses.isEmpty(),"No handler classes specified");
		TemplateLibrary library = new TemplateLibraryLoader().createLibrary(handlerClasses);
		ImmutableMap<HandlerId, ResourceHandler> handlers = createHandlerMap(library);
		return new TemplateManager(library,handlers);
	}

	@Deprecated
	public static TemplateManager newInstance(Class<?>... handlerClasses) throws TemplateLibraryLoadingException {
		return newInstance(Arrays.asList(handlerClasses));
	}
	
	public static TemplateManagerBuilder builder() {
		return new TemplateManagerBuilder();
	}
	
	public static final class TemplateManagerBuilder {
		
		private static final class HandlerMapBuilder implements TemplateVisitor {

			private final Builder<HandlerId, ResourceHandler> builder;
			private final ClassToInstanceMap<ResourceHandler> handlers;

			private HandlerMapBuilder(Builder<HandlerId, ResourceHandler> builder, ClassToInstanceMap<ResourceHandler> handlers) {
				this.builder = builder;
				this.handlers = handlers;
			}

			@Override
			public void visitResourceTemplate(ResourceTemplate template) {
				Class<? extends ResourceHandler> handlerClass = template.handlerClass();
				ResourceHandler handler=this.handlers.getInstance(handlerClass);
				if(handler==null) {
					try {
						handler=handlerClass.newInstance();
					} catch (InstantiationException e) {
						throw new ResourceHandlerInstantiationException("Could not instantiate resource handler from template '"+template.id()+"' ("+handlerClass.getCanonicalName()+")",handlerClass);
					} catch (IllegalAccessException e) {
						throw new ResourceHandlerInstantiationException("Could not instantiate resource handler from template '"+template.id()+"' ("+handlerClass.getCanonicalName()+")",handlerClass);
					}
				}
				this.builder.put(HandlerId.createId(handlerClass), handler);
			}

			@Override
			public void visitContainerTemplate(ContainerTemplate template) {
				visitResourceTemplate(template);
			}

			@Override
			public void visitBasicContainerTemplate(BasicContainerTemplate template) {
				visitResourceTemplate(template);
			}

			@Override
			public void visitMembershipAwareContainerTemplate(MembershipAwareContainerTemplate template) {
				visitResourceTemplate(template);
			}

			@Override
			public void visitDirectContainerTemplate(DirectContainerTemplate template) {
				visitResourceTemplate(template);
			}

			@Override
			public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
				visitResourceTemplate(template);
			}
		}

		private final List<Class<?>> handlerClasses;
		private final ClassToInstanceMap<ResourceHandler> handlers;
		
		
		private TemplateManagerBuilder() {
			this.handlerClasses=Lists.newArrayList();
			this.handlers=MutableClassToInstanceMap.<ResourceHandler>create();
		}
		
		public TemplateManagerBuilder withHandlerClasses(Class<?>... classes) {
			checkNotNull(classes,"Handler class collection cannot be null");
			return withHandlerClasses(Arrays.asList(classes));
		}

		public TemplateManagerBuilder withHandlerClasses(Collection<Class<?>> classes) {
			checkNotNull(classes,"Handler class collection cannot be null");
			for(Class<?> clazz:classes) {
				if(!this.handlerClasses.contains(clazz)) {
					this.handlerClasses.add(clazz);
				}
			}
			return this;
		}

		public TemplateManagerBuilder withHandlers(ResourceHandler... handlers) {
			checkNotNull(handlers,"Handler collection cannot be null");
			return withHandlers(Arrays.asList(handlers));
		}

		public TemplateManagerBuilder withHandlers(Collection<ResourceHandler> handlers) {
			checkNotNull(handlers,"Handler collection cannot be null");
			for(ResourceHandler handler:handlers) {
				Class<? extends ResourceHandler> handlerClass = handler.getClass();
				if(!this.handlerClasses.contains(handlerClass)) {
					this.handlerClasses.add(handlerClass);
					this.handlers.put(handlerClass, handler);
				}
			}
			return this;
		}
		
		public TemplateManager build() throws InvalidTemplateManagerConfigurationException {
			try {
				TemplateLibrary library = new TemplateLibraryLoader().createLibrary(this.handlerClasses);
				Builder<HandlerId, ResourceHandler> builder = ImmutableMap.<HandlerId, ResourceHandler>builder();
				library.accept(new HandlerMapBuilder(builder,this.handlers));
				return new TemplateManager(library, builder.build());
			} catch (ResourceHandlerInstantiationException e) {
				throw new InvalidTemplateManagerConfigurationException(e);
			} catch (TemplateLibraryLoadingException e) {
				throw new InvalidTemplateManagerConfigurationException(e);
			}
		}
		
	}

}
