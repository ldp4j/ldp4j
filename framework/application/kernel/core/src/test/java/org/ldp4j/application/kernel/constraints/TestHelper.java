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
package org.ldp4j.application.kernel.constraints;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.data.NewIndividual;
import org.ldp4j.application.data.RelativeIndividual;
import org.ldp4j.application.data.RelativeIndividualId;
import org.ldp4j.application.data.constraints.Constraints;
import org.ldp4j.application.data.constraints.Constraints.Cardinality;
import org.ldp4j.application.data.constraints.Constraints.InversePropertyConstraint;
import org.ldp4j.application.data.constraints.Constraints.NodeKind;
import org.ldp4j.application.data.constraints.Constraints.PropertyConstraint;
import org.ldp4j.application.data.constraints.Constraints.Shape;
import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.context.HttpRequest.HttpMethod;
import org.ldp4j.application.kernel.endpoint.Endpoint;
import org.ldp4j.application.kernel.impl.TestingModelFactory;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.sdk.HttpRequestBuilder;

public final class TestHelper {

	private TestHelper() {
	}

	public static Endpoint createEndpoint(String path, Resource resource, Date created, EntityTag entityTag) {
		return TestingModelFactory.createEndpoint(path, resource, created, entityTag);
	}

	public static Resource createResource(Name<?> name, String templateId) {
		return TestingModelFactory.createResource(name, templateId);
	}

	public static Container createContainer(Name<?> name, String templateId) {
		return TestingModelFactory.createContainer(name, templateId);
	}

	public static Name<String> name(String name) {
		return NamingScheme.getDefault().name(name);
	}

	public static HttpRequest httpRequest() {
		return
			HttpRequestBuilder.
				newInstance().
					withMethod(HttpMethod.POST).
					withHost("www.example.org").
					withAbsolutePath("service/resource/").
					withBody("body").
					withHeader("accept","text/turtle").
					withHeader("if-none-match",EntityTag.createWeak("asdjkkl").toString()).
					build();
	}

	public static Constraints constraints() {
		DataSet dataSet=DataSets.createDataSet(name("dataSet"));
		PropertyConstraint pc=
			Constraints.
				propertyConstraint(uri("property")).
					withLabel("property-constraint").
					withComment("A property constraint").
					withCardinality(Cardinality.mandatory()).
					withNodeKind(NodeKind.NODE).
					withDatatype(uri("datatype")).
					withValueType(uri("valueType")).
					withAllowedValues(
						Literals.duration(13, TimeUnit.SECONDS),
						managedIndividual(dataSet,"one","template1")).
					withValue(
						Literals.of(new Date()).date(),
						localIndividual(dataSet,"two"));
		InversePropertyConstraint ipc=
			Constraints.
				inversePropertyConstraint(uri("inverseProperty")).
					withLabel("inverse-property-constraint").
					withComment("An inverse property constraint").
					withCardinality(Cardinality.unbound()).
					withNodeKind(NodeKind.NODE).
					withDatatype(uri("anotherDatatype")).
					withValueType(uri("anotherValueType")).
					withAllowedValues(
						Literals.duration(31,TimeUnit.DAYS),
						relativeIndividual(dataSet,"three","template3","path")).
					withValue(
						Literals.of(new Date()).date(),
						externalIndividual(dataSet,"http://www.example.org/external/"),
						newIndividual(dataSet, "new/resource"));
		Shape nodeShape=
			Constraints.
				shape().
					withLabel("node-constraint").
					withComment("A constraint for an specific value").
					withPropertyConstraint(pc).
					withPropertyConstraint(ipc);
		Shape typeShape=
			Constraints.
				shape().
					withLabel("type-constraint").
					withComment("A constraint for a type").
					withPropertyConstraint(pc).
					withPropertyConstraint(ipc);
		return
			Constraints.
				constraints().
					withNodeShape(managedIndividual(dataSet, "individual", "template"), nodeShape).
					withTypeShape(uri("constrainedType"), typeShape);
	}

	private static ManagedIndividual managedIndividual(DataSet dataSet, String name,String managerId) {
		return dataSet.individual(managedIndividualId(name, managerId), ManagedIndividual.class);
	}

	@SuppressWarnings("rawtypes")
	private static LocalIndividual localIndividual(DataSet dataSet, String name) {
		return dataSet.individual((Name)name(name), LocalIndividual.class);
	}

	private static ExternalIndividual externalIndividual(DataSet dataSet, String path) {
		return dataSet.individual(URI.create(path), ExternalIndividual.class);
	}

	private static NewIndividual newIndividual(DataSet dataSet, String path) {
		return dataSet.individual(URI.create(path), NewIndividual.class);
	}

	private static RelativeIndividual relativeIndividual(DataSet dataSet, String name, String managerId, String path) {
		return dataSet.individual(RelativeIndividualId.createId(managedIndividualId(name,managerId), URI.create(path)), RelativeIndividual.class);
	}

	public static ManagedIndividualId managedIndividualId(String name,String managerId) {
		return ManagedIndividualId.createId(name(name),managerId);
	}

	public static ManagedIndividualId managedIndividualId(ResourceId resourceId) {
		return ManagedIndividualId.createId(resourceId.name(), resourceId.templateId());
	}


	private static URI uri(String string) {
		return URI.create("http://www.example.org/#"+string);
	}


}
