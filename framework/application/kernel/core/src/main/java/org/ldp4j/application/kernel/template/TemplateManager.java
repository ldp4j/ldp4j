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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.template.BasicContainerTemplate;
import org.ldp4j.application.kernel.template.ContainerTemplate;
import org.ldp4j.application.kernel.template.DirectContainerTemplate;
import org.ldp4j.application.kernel.template.IndirectContainerTemplate;
import org.ldp4j.application.kernel.template.MembershipAwareContainerTemplate;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateLibrary;
import org.ldp4j.application.kernel.template.TemplateVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.MutableClassToInstanceMap;

final class TemplateManager {

	private static final Logger LOGGER=LoggerFactory.getLogger(TemplateManager.class);

	private final TemplateLibrary library;
	private final ImmutableMap<HandlerId, ResourceHandler> handlers;

	private TemplateManager(TemplateLibrary library, ImmutableMap<HandlerId, ResourceHandler> handlers) {
		this.library = library;
		this.handlers = handlers;
	}

	TemplateLibrary templateLibrary() {
		return this.library;
	}

	<T extends ResourceHandler> T getHandler(Class<? extends T> handlerClass, ResourceTemplate template) {
		checkNotNull(handlerClass,"Handler class cannot be null");
		checkNotNull(template,"Template cannot be null");
		checkArgument(this.library.contains(template),"Unknown template '"+template+"'");
		checkArgument(handlerClass.isAssignableFrom(template.handlerClass()));
		return handlerClass.cast(this.handlers.get(HandlerId.createId(template.handlerClass())));
	}

	static TemplateManagerBuilder builder() {
		return new TemplateManagerBuilder();
	}

	static final class TemplateManagerBuilder {

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
					} catch (Exception e) {
						String message=String.format("Could not instantiate resource handler from template '%s' (%s)",template.id(),handlerClass.getCanonicalName());
						LOGGER.warn(message);
						throw new ResourceHandlerInstantiationException(message,handlerClass,e);
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

		TemplateManagerBuilder withHandlerClasses(Class<?>... classes) {
			checkNotNull(classes,"Handler class collection cannot be null");
			return withHandlerClasses(Arrays.asList(classes));
		}

		TemplateManagerBuilder withHandlerClasses(Collection<Class<?>> classes) {
			checkNotNull(classes,"Handler class collection cannot be null");
			for(Class<?> clazz:classes) {
				if(!this.handlerClasses.contains(clazz)) {
					this.handlerClasses.add(clazz);
				}
			}
			return this;
		}

		TemplateManagerBuilder withHandlers(ResourceHandler... handlers) {
			checkNotNull(handlers,"Handler collection cannot be null");
			return withHandlers(Arrays.asList(handlers));
		}

		TemplateManagerBuilder withHandlers(Collection<ResourceHandler> handlers) {
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

		TemplateManager build() throws TemplateManagementServiceConfigurationException {
			try {
				MutableTemplateLibrary newLibrary=new MutableTemplateLibrary();
				for(Class<?> handlerClass:handlerClasses) {
					if(!newLibrary.isHandlerRegistered(handlerClass)) {
						newLibrary.registerHandler(handlerClass);
					}
				}
				Builder<HandlerId, ResourceHandler> builder = ImmutableMap.<HandlerId, ResourceHandler>builder();
				newLibrary.accept(new HandlerMapBuilder(builder,this.handlers));
				return new TemplateManager(new ImmutableTemplateLibrary(newLibrary), builder.build());
			} catch (TemplateCreationException e) {
				throw new TemplateManagementServiceConfigurationException(e);
			} catch (ResourceHandlerInstantiationException e) {
				throw new TemplateManagementServiceConfigurationException(e);
			}
		}

	}

}
