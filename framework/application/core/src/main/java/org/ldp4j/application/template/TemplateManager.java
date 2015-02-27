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
import org.ldp4j.application.spi.PersistencyManager;
import org.ldp4j.application.spi.TemplateCreationException;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.MutableClassToInstanceMap;

final class TemplateManager {

	private final TemplateLibrary library;
	private final ImmutableMap<HandlerId, ResourceHandler> handlers;

	private TemplateManager(TemplateLibrary library, ImmutableMap<HandlerId, ResourceHandler> handlers) {
		this.library = library;
		this.handlers = handlers;
	}

	public <T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass, ResourceTemplate template) {
		checkNotNull(handlerClass,"Handler class cannot be null");
		checkNotNull(template,"Template cannot be null");
		checkArgument(this.library.contains(template),"Unknown template '"+template+"'");
		checkArgument(handlerClass.isAssignableFrom(template.handlerClass()));
		return handlerClass.cast(this.handlers.get(HandlerId.createId(template.handlerClass())));
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

		private PersistencyManager persistencyManager;


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

		public TemplateManager build() throws TemplateManagementServiceConfigurationException {
			try {
				for(Class<?> handlerClass:handlerClasses) {
					if(!this.persistencyManager.isHandlerRegistered(handlerClass)) {
						this.persistencyManager.registerHandler(handlerClass);
					}
				}
				TemplateLibrary library = this.persistencyManager.exportTemplates();
				Builder<HandlerId, ResourceHandler> builder = ImmutableMap.<HandlerId, ResourceHandler>builder();
				library.accept(new HandlerMapBuilder(builder,this.handlers));
				return new TemplateManager(library, builder.build());
			} catch (TemplateCreationException e) {
				throw new TemplateManagementServiceConfigurationException(e);
			} catch (ResourceHandlerInstantiationException e) {
				throw new TemplateManagementServiceConfigurationException(e);
			}
		}

		public TemplateManagerBuilder withPersistencyManager(PersistencyManager persistencyManager) {
			this.persistencyManager=persistencyManager;
			return this;
		}

	}

}
