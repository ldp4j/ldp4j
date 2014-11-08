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
package org.ldp4j.application.resource;

import java.net.URI;
import java.util.List;

import org.ldp4j.application.session.WriteSessionConfiguration;
import org.ldp4j.application.session.WriteSessionService;
import org.ldp4j.application.spi.Service;
import org.ldp4j.application.spi.ServiceBuilder;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateIntrospector;
import org.ldp4j.application.template.TemplateManagementService;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.IndividualVisitor;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.RelativeIndividual;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.ValueVisitor;
import org.ldp4j.application.ext.ResourceHandler;

import com.google.common.collect.Lists;

public class ResourceControllerService implements Service {

	private static final URI NEW_RESOURCE_SURROGATE_ID = URI.create("");

	private static final class ResourceControllerServiceBuilder extends ServiceBuilder<ResourceControllerService> {

		private ResourceControllerServiceBuilder() {
			super(ResourceControllerService.class);
		}

		public ResourceControllerService build() {
			return
				new ResourceControllerService(
					service(WriteSessionService.class),
					service(TemplateManagementService.class));
		}

	}

	private final WriteSessionService writeSessionService;
	private final TemplateManagementService templateManagementService;

	private ResourceControllerService(WriteSessionService writeSessionService, TemplateManagementService templateManagementService) {
		this.writeSessionService=writeSessionService;
		this.templateManagementService = templateManagementService;
	}

	private <T extends Resource> Adapter adapter(T resource, WriteSessionConfiguration configuration) {
		ResourceTemplate template=this.templateManagementService.findTemplateById(resource.id().templateId());
		Class<? extends ResourceHandler> handlerClass = template.handlerClass();
		ResourceHandler delegate=this.templateManagementService.getHandler(handlerClass);
		return AdapterFactory.newAdapter(resource,delegate,this.writeSessionService,configuration);
	}

	public DataSet getResource(Resource resource) {
		return adapter(resource, WriteSessionConfiguration.builder().build()).get();
	}

	public void updateResource(Resource resource, DataSet dataSet, WriteSessionConfiguration configuration) throws FeatureException {
		adapter(resource, configuration).update(dataSet);
	}

	public void deleteResource(Resource resource, WriteSessionConfiguration configuration) throws FeatureException {
		adapter(resource, configuration).delete();
	}

	public Resource createResource(Container container, DataSet dataSet, String desiredPath) throws FeatureException {
		WriteSessionConfiguration configuration =
			WriteSessionConfiguration.
				builder().
					withPath(desiredPath).
					withIndirectId(getIndirectId(container, dataSet)).
					build();
		return adapter(container,configuration).create(dataSet);
	}

	private URI getIndirectId(Container container, DataSet dataSet) {
		TemplateIntrospector introspector=
				TemplateIntrospector.
					newInstance(
						this.templateManagementService.
							findTemplateById(container.id().templateId()));
		if(!introspector.isIndirectContainer()) {
			return null;
		}
		Property property = getInsertedContentRelation(dataSet,introspector.getInsertedContentRelation());
		if(property==null) {
			// TODO: Check if this situation is a failure
			return null;
		}
		final List<URI> indirectIdentities= findIndirectIds(property);
		if(indirectIdentities.size()==1) {
			return indirectIdentities.get(0);
		}
		// TODO: We should fail here, either because no valid identifiers were
		// specified or because to many of them were specified
		return null;
	}

	private Property getInsertedContentRelation(DataSet dataSet, URI insertedContentRelation) {
		ExternalIndividual individual = dataSet.individual(NEW_RESOURCE_SURROGATE_ID, ExternalIndividual.class);
		return individual.property(insertedContentRelation);
	}

	private List<URI> findIndirectIds(Property property) {
		final List<URI> indirectIdentities=Lists.newArrayList();
		for(Value v:property) {
			v.accept(
				new ValueVisitor() {
					@Override
					public void visitLiteral(Literal<?> value) {
						// TODO: We should fail here
					}
					@Override
					public void visitIndividual(Individual<?, ?> value) {
						value.accept(
							new IndividualVisitor() {
								@Override
								public void visitManagedIndividual(ManagedIndividual individual) {
									// TODO: We should fail here
								}
								@Override
								public void visitLocalIndividual(LocalIndividual individual) {
									// TODO: We should fail here
								}
								@Override
								public void visitExternalIndividual(ExternalIndividual individual) {
									indirectIdentities.add(individual.id());
								}
								@Override
								public void visitRelativeIndividual(RelativeIndividual individual) {
									// TODO: We should fail here
								}
							}
						);
					}
				}
			);
		}
		return indirectIdentities;
	}

	public static ServiceBuilder<ResourceControllerService> serviceBuilder() {
		return new ResourceControllerServiceBuilder();
	}

	public static ResourceControllerService defaultService() {
		return serviceBuilder().build();
	}

}