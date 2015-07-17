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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.commands;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.server.Endpoint;
import org.ldp4j.server.EndpointFactory;
import org.ldp4j.server.EndpointRegistrationException;
import org.ldp4j.server.EndpointRegistry;
import org.ldp4j.server.MutableConfiguration;
import org.ldp4j.server.ResourceIndex;
import org.ldp4j.server.UnsupportedMediaTypeException;
import org.ldp4j.server.blueprint.ComponentRegistry;
import org.ldp4j.server.commands.xml.CreateEndpoint;
import org.ldp4j.server.commands.xml.DeleteEndpoint;
import org.ldp4j.server.commands.xml.EndpointConfiguration.Capabilities;
import org.ldp4j.server.commands.xml.EntityType;
import org.ldp4j.server.commands.xml.MembershipConfigurationType;
import org.ldp4j.server.commands.xml.MembershipRelationType;
import org.ldp4j.server.commands.xml.ModifyEndpointConfiguration;
import org.ldp4j.server.commands.xml.ResourceStateType;
import org.ldp4j.server.commands.xml.UpdateResourceState;
import org.ldp4j.server.data.DataTransformator;
import org.ldp4j.server.resources.MembershipRelation;
import org.ldp4j.server.resources.Resource;
import org.ldp4j.server.resources.impl.ResourceBuilder;
import org.ldp4j.server.resources.impl.ResourceBuilder.ContainerBuilder;
import org.ldp4j.server.xml.converters.FormatConverter;

abstract class CommandProcessor<T> {

	static final Class<CreateEndpoint> CREATE_ENDPOINT_CLAZZ = CreateEndpoint.class;
	static final Class<DeleteEndpoint> DELETE_ENDPOINT_CLAZZ = DeleteEndpoint.class;
	static final Class<ModifyEndpointConfiguration> MODIFY_ENDPOINT_CONFIGURATION_CLAZZ = ModifyEndpointConfiguration.class;
	static final Class<UpdateResourceState> UPDATE_RESOURCE_STATE_CLAZZ = UpdateResourceState.class;

	private static class DeleteEndpointCommandProcessor extends CommandProcessor<DeleteEndpoint> {

		private DeleteEndpointCommandProcessor(DeleteEndpoint command) {
			super(command);
		}

		@Override
		void execute(ComponentRegistry registry) {
			URI path = URI.create(getCommand().getPath());
			deregisterEndpoint(path, registry.getComponent(EndpointRegistry.class));
			unpublishResource(path, registry.getComponent(ResourceIndex.class));
		}

		private void unpublishResource(URI path, ResourceIndex resourceIndex) {
			ResourceId location =
				toResourceId(resourceIndex.resolveLocation(path));
			if(location!=null) {
				resourceIndex.unpublish(location);
			}
		}

		private void deregisterEndpoint(URI path,
				EndpointRegistry endpointRegistry) {
			Endpoint endpoint =
				endpointRegistry.findEndpoint(path);
			if(endpoint!=null) {
				endpointRegistry.deregisterEndpoint(path);
			}
		}

	}

	private static class UpdateResourceStateCommandProcessor extends CommandProcessor<UpdateResourceState> {

		private UpdateResourceStateCommandProcessor(UpdateResourceState command) {
			super(command);
		}

	}

	private static class ModifyEndpointConfigurationCommandProcessor extends CommandProcessor<ModifyEndpointConfiguration> {

		private ModifyEndpointConfigurationCommandProcessor(ModifyEndpointConfiguration command) {
			super(command);
		}

	}

	private static class UnsupportedCommandProcessor extends CommandProcessor<CommandDescription> {

		private UnsupportedCommandProcessor(CommandDescription command) {
			super(command);
		}

	}

	private static class CreateEndpointCommandProcessor extends CommandProcessor<CreateEndpoint> {

		private CreateEndpointCommandProcessor(CreateEndpoint command) {
			super(command);
		}

		@Override
		public void execute(ComponentRegistry registry) throws CommandExecutionException {
			Endpoint endpoint = createEndpoint(registry);
			try {
				registry.
					getComponent(EndpointRegistry.class).
						registerEndpoint(endpoint);
			} catch (EndpointRegistrationException e) {
				unpublishResource(registry, endpoint.path());
				throw new CommandExecutionException("Endpoint creation failed", e);
			}
		}

		private void unpublishResource(ComponentRegistry registry, URI resourcePath) {
			ResourceIndex resourceIndex=registry.getComponent(ResourceIndex.class);
			ResourceId location =
				toResourceId(resourceIndex.resolveLocation(resourcePath));
			if(location!=null) {
				resourceIndex.unpublish(location);
			}
		}

		private Endpoint createEndpoint(ComponentRegistry registry) throws CommandExecutionException {
			Resource resource = createResource(registry, getCommand());
			Capabilities capabilities = getCommand().getCapabilities();
			if(capabilities==null) {
				capabilities=new Capabilities();
			}
			return
				EndpointFactory.
					newEndpoint(
						URI.create(getCommand().getPath()),
						createEndpointConfiguration(resource, capabilities),
						registry);
		}

		private Resource createResource(
				ComponentRegistry registry,
				CreateEndpoint cmd) throws CommandExecutionException {
			Resource resource =
				createResource(
					registry.getComponent(ResourceIndex.class),
					cmd.getPath(),
					cmd.getTemplateId(),
					cmd.getMembershipConfiguration(),
					cmd.getResourceState());
			return resource;
		}

		private MutableConfiguration<Resource> createEndpointConfiguration(
				Resource resource, Capabilities capabilities) {
			MutableConfiguration<Resource> config=new MutableConfiguration<Resource>();
			config.setResource(resource);
			config.setDeletable(capabilities.isDeletable());
			config.setModifiable(capabilities.isModifiable());
			config.setPatchable(capabilities.isPatchable());
			return config;
		}

		private Resource createResource(
				ResourceIndex resourceIndex,
				String path,
				String templateId,
				MembershipConfigurationType membershipConfiguration,
				ResourceStateType resourceState) throws CommandExecutionException {
			try {
				ResourceBuilder<?> builder=null;
				if(membershipConfiguration==null) {
					builder=ResourceBuilder.newResource(NamingScheme.getDefault().name(path),templateId);
				} else {
					ContainerBuilder tmp = ResourceBuilder.newContainer(NamingScheme.getDefault().name(path),templateId);
					builder=tmp;
					MembershipRelationType mr = membershipConfiguration.getMembershipRelation();
					if(mr!=null) {
						MembershipRelation membershipRelation=null;
						if(mr.equals(MembershipRelationType.HAS_MEMBER)) {
							membershipRelation=MembershipRelation.HAS_MEMBER;
						} else {
							membershipRelation=MembershipRelation.IS_MEMBER_OF;
						}
						tmp.withMembershipRelation(membershipRelation);
					}
					tmp.
						withMembershipTarget(membershipConfiguration.getMembershipTarget()).
						withMembershipPredicate(membershipConfiguration.getMembershipPredicate()).
						withInsertedContentRelation(membershipConfiguration.getMembershipIndirection());
				}
				if(resourceState!=null) {
					EntityType rawEntity = resourceState.getEntity();
					if(rawEntity!=null) {
						builder.withContent(unmarshallContent(path,rawEntity,resourceIndex));
					}
					String rawEtag = resourceState.getEtag();
					if(rawEtag!=null) {
						builder.withEntityTag(EntityTag.valueOf(rawEtag));
					}
					Date tmpLastModified = resourceState.getLastModified();
					if(tmpLastModified!=null) {
						builder.withLastModified(tmpLastModified);
					}
				}
				Resource newResource = builder.build();
				resourceIndex.publish(newResource.id(), URI.create(path));
				return newResource;
			} catch (IOException e) {
				throw new CommandExecutionException("Could not parse endpoint contents",e);
			}
		}

		/**
		 * @param rawEntity
		 * @param index
		 * @return
		 * @throws IOException
		 */
		private DataSet unmarshallContent(String path,EntityType rawEntity, ResourceIndex index) throws IOException {
			MediaType mediaType=
				FormatConverter.
					parseFormat(rawEntity.getFormat().value());
			try {
				return
					DataTransformator.
						create(URI.create("http://www.example.org/")).
						enableResolution(index).
						surrogateEndpoint(URI.create("http://www.example.org/").resolve(path)).
						mediaType(mediaType).
						unmarshall(rawEntity.getValue());
			} catch (org.ldp4j.server.data.UnsupportedMediaTypeException e) {
				throw new UnsupportedMediaTypeException("Could not create entity",mediaType);
			}
		}

	}

	private final T command;

	protected CommandProcessor(T command) {
		this.command = command;
	}

	void execute(ComponentRegistry registry) throws CommandExecutionException {
		throw new CommandExecutionException("Method not implemented yet");
	}

	protected final ResourceId toResourceId(ManagedIndividualId id) {
		return ResourceId.createId(id.name(),id.managerId());
	}

	protected final T getCommand() {
		return command;
	}

	static CommandProcessor<?> newCommand(CommandDescription command) {
		switch(command.getType()) {
		case CREATE_ENDPOINT:
			return new CreateEndpointCommandProcessor(command.as(CommandProcessor.CREATE_ENDPOINT_CLAZZ));
		case DELETE_ENDPOINT:
			return new DeleteEndpointCommandProcessor(command.as(CommandProcessor.DELETE_ENDPOINT_CLAZZ));
		case MODIFY_ENDPOINT_CONFIGURATION:
			return new ModifyEndpointConfigurationCommandProcessor(command.as(CommandProcessor.MODIFY_ENDPOINT_CONFIGURATION_CLAZZ));
		case UPDATE_RESOURCE_STATE:
			return new UpdateResourceStateCommandProcessor(command.as(CommandProcessor.UPDATE_RESOURCE_STATE_CLAZZ));
		default:
			return new UnsupportedCommandProcessor(command);
		}
	}

}