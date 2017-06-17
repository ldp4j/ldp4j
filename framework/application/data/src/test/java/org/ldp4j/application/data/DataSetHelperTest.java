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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.io.Serializable;
import java.net.URI;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.ldp4j.application.data.IndividualReference.*;

public class DataSetHelperTest extends DataTestingSupport {

	private static final String HAS_TITLE = "http://www.ldp4j.org/example#hasTitle";
	private static final String HAS_LINK = "http://www.ldp4j.org/example#hasLink";
	private static final String SHOULD_LINK_TO = "http://www.ldp4j.org/example#shouldLinkTo";

	private DataSet exampleDataSet() {
		DataSet dataSet=
			DataDSL.
				dataSet().
					individual(managed(MANAGED_INDIVIDUAL_ID)).
						hasProperty(HAS_TITLE).
							withValue("managed").
							withValue("custom").
						hasLink(HAS_LINK).
							referringTo(external(EXTERNAL)).
							referringTo(managed(RESOURCE_NAME,MANAGER_ID)).
						hasLink(HAS_LINK).
							toIndividual(external(EXTERNAL)).
								hasLink(HAS_LINK).
									referringTo(managed(RESOURCE_NAME,MANAGER_ID)).
								hasProperty(SHOULD_LINK_TO).
									withValue("managed").
					individual(relative(MANAGED_INDIVIDUAL_ID,RELATIVE_PATH)).
						hasProperty(HAS_TITLE).
							withValue("relative").
							withValue("custom").
						hasLink(HAS_LINK).
							referringTo(external(EXTERNAL)).
							referringTo(relative(MANAGED_INDIVIDUAL_ID,RELATIVE_PATH)).
						hasLink(HAS_LINK).
							toIndividual(external(EXTERNAL)).
								hasLink(HAS_LINK).
									referringTo(relative(MANAGED_INDIVIDUAL_ID,RELATIVE_PATH)).
								hasProperty(SHOULD_LINK_TO).
									withValue("relative").
					individual(newIndividual(SELF)).
						hasProperty(HAS_TITLE).
							withValue("self").
							withValue("custom").
						hasLink(HAS_LINK).
							referringTo(external(EXTERNAL)).
							referringTo(newIndividual(SELF)).
						hasLink(HAS_LINK).
							toIndividual(external(EXTERNAL)).
								hasLink(HAS_LINK).
									referringTo(newIndividual(SELF)).
								hasProperty(SHOULD_LINK_TO).
									withValue("self").
					individual(newIndividual(NEW)).
						hasProperty(HAS_TITLE).
							withValue("new").
							withValue("custom").
						hasLink(HAS_LINK).
							referringTo(external(EXTERNAL)).
							referringTo(newIndividual(NEW)).
						hasLink(HAS_LINK).
							toIndividual(external(EXTERNAL)).
								hasLink(HAS_LINK).
									referringTo(newIndividual(NEW)).
								hasProperty(SHOULD_LINK_TO).
									withValue("new").
					individual(anonymous(BLANK_NODE)).
						hasProperty(HAS_TITLE).
							withValue("bnode").
							withValue("custom").
						hasLink(HAS_LINK).
							referringTo(external(EXTERNAL)).
							referringTo(anonymous(BLANK_NODE)).
						hasLink(HAS_LINK).
							toIndividual(external(EXTERNAL)).
								hasLink(HAS_LINK).
									referringTo(anonymous(BLANK_NODE)).
								hasProperty(SHOULD_LINK_TO).
									withValue("bnode").
					individual(external(EXTERNAL)).
						hasProperty(HAS_TITLE).
							withValue("external").
							withValue("custom").
						hasLink(HAS_LINK).
							referringTo(external(EXTERNAL)).
						hasLink(HAS_LINK).
							toIndividual(external(EXTERNAL)).
								hasProperty(SHOULD_LINK_TO).
									withValue("external").
					build();
		return dataSet;
	}

	private void contains(DataSet dataSet, DataSet original) {
		for(Individual<?,?> individual:dataSet) {
			Individual<?, ?> originalInd = original.individualOfId(individual.id());
			assertThat(originalInd,notNullValue());
			for(Property property:individual) {
				assertThat(originalInd.hasProperty(property.predicate()),equalTo(true));
				final Property originalProp = originalInd.property(property.predicate());
				for(Value value:property) {
					value.accept(
						new ValueVisitor() {
							@Override
							public void visitLiteral(Literal<?> value) {
								assertThat(originalProp.hasLiteralValue(value),equalTo(true));
							}
							@Override
							public void visitIndividual(Individual<?, ?> value) {
								assertThat(originalProp.hasIdentifiedIndividual(value.id()),equalTo(true));
							}

					});
				}
			}
		}
	}

	private void assertIsProperlyRenamed(DataSet dataSet, Individual<?, ?> original, Individual<?, ?> individual, String title) {
		assertThat(individual,notNullValue());
		assertThat(dataSet.individualOfId(original.id()),nullValue());
		assertThat(hasTitle(individual, title),equalTo(true));
		assertThat(hasLink(individual, EXTERNAL),equalTo(true));
		assertThat(hasLink(individual, individual.id()),equalTo(true));
		assertThat(hasLink(dataSet.individualOfId(EXTERNAL), individual.id()),equalTo(true));
		assertThat(hasLink(dataSet.individualOfId(EXTERNAL), original.id()),equalTo(false));
		assertThat(hasLink(dataSet.individualOfId(EXTERNAL), original.id()),equalTo(false));
	}

	private boolean hasTitle(Individual<?,?> individual, String title) {
		return individual.property(URI.create(HAS_TITLE)).hasLiteralValue(Literals.of(title));
	}

	private boolean hasLink(Individual<?,?> individual, Serializable id) {
		return individual.property(URI.create(HAS_LINK)).hasIdentifiedIndividual(id);
	}

	@Test
	public void testDataSetBuilding() {
		DataSet dataSet = DataSets.createDataSet(null);
		DataSetHelper helper = DataSetUtils.newHelper(dataSet);
		helper.
			managedIndividual(RESOURCE_NAME, MANAGER_ID).
				property(HAS_TITLE).
					withLiteral("managed").
					withLiteral("custom").
				property(HAS_LINK).
					withIndividual(EXTERNAL).
					withIndividual(RESOURCE_NAME, MANAGER_ID);
		helper.
			relativeIndividual(RESOURCE_NAME, MANAGER_ID,RELATIVE_PATH).
				property(HAS_TITLE).
					withLiteral("relative").
					withLiteral("custom").
				property(HAS_LINK).
					withIndividual(EXTERNAL).
					withIndividual(RESOURCE_NAME, MANAGER_ID,RELATIVE_PATH);
		helper.
			localIndividual(BLANK_NODE).
				property(HAS_TITLE).
					withLiteral("bnode").
					withLiteral("custom").
				property(HAS_LINK).
					withIndividual(EXTERNAL).
					withIndividual(BLANK_NODE);
		helper.
			externalIndividual(SELF).
				property(HAS_TITLE).
					withLiteral("self").
					withLiteral("custom").
				property(HAS_LINK).
					withIndividual(EXTERNAL).
					withIndividual(SELF);
		helper.
			externalIndividual(NEW).
				property(HAS_TITLE).
					withLiteral("new").
					withLiteral("custom").
				property(HAS_LINK).
					withIndividual(EXTERNAL).
					withIndividual(NEW);
		helper.
			externalIndividual(EXTERNAL).
				property(HAS_TITLE).
					withLiteral("external").
					withLiteral("custom").
				property(HAS_LINK).
					withIndividual(RESOURCE_NAME, MANAGER_ID).
					withIndividual(RESOURCE_NAME, MANAGER_ID,RELATIVE_PATH.toString()).
					withIndividual(SELF.toString()).
					withIndividual(NEW).
					withIndividual(BLANK_NODE).
					withIndividual(EXTERNAL).
				property(SHOULD_LINK_TO).
					withLiteral("managed").
					withLiteral("relative").
					withLiteral("self").
					withLiteral("new").
					withLiteral("bnode").
					withLiteral("external");
		DataSet original = exampleDataSet();
		contains(dataSet, original);
		contains(original, dataSet);
	}

	@Test
	public void testSelf() throws Exception {
		DataSet dataSet = exampleDataSet();
		DataSetHelper newHelper = DataSetUtils.newHelper(dataSet);
		Individual<?,?> self = newHelper.self();
		assertThat(self.property(URI.create(HAS_TITLE)).hasLiteralValue(Literals.of("self")),equalTo(true));
	}

	@Test
	public void testManagesSelf() throws Exception {
		DataSet dataSet = exampleDataSet();
		Individual<?,?> original = dataSet.individualOfId(SELF);
		Individual<?,?> individual=
			DataSetUtils.
				newHelper(dataSet).
					manage(managedIndividualId("other","managerId"));
		assertIsProperlyRenamed(dataSet, original, individual, "self");
	}

	@Test(expected=DataSetModificationException.class)
	public void testCannotManagedUsingAnExistingManagedIndividual() throws Exception {
		DataSet dataSet = exampleDataSet();
		DataSetUtils.
			newHelper(dataSet).
				manage(MANAGED_INDIVIDUAL_ID);
	}

	@Test(expected=DataSetModificationException.class)
	public void testCannotManageIfSelfDoesNotExist() throws Exception {
		DataSet dataSet = exampleDataSet();
		Individual<?,?> original = dataSet.individualOfId(SELF);
		dataSet.remove(original);
		DataSetUtils.
			newHelper(dataSet).
				manage(managedIndividualId("other","managerId"));
	}

	@Test
	public void testReplacesSelf() throws Exception {
		DataSet dataSet = exampleDataSet();
		Individual<?,?> original = dataSet.individualOfId(SELF);
		Individual<?,?> individual=
			DataSetUtils.
				newHelper(dataSet).
					replace(
						original.id(),
						managedIndividualId("other","managerId"), ManagedIndividual.class);
		assertIsProperlyRenamed(dataSet, original, individual, "self");
	}

	@Test
	public void testReplacesNew() throws Exception {
		DataSet dataSet = exampleDataSet();
		Individual<?,?> original = dataSet.individualOfId(NEW);
		Individual<?,?> individual=
			DataSetUtils.
				newHelper(dataSet).
					replace(
						original.id(),
						managedIndividualId("other","managerId"), ManagedIndividual.class);
		assertIsProperlyRenamed(dataSet, original, individual, "new");
	}

	@Test
	public void testReplacesManagedIndividual() throws Exception {
		DataSet dataSet = exampleDataSet();
		Individual<?,?> original = dataSet.individualOfId(MANAGED_INDIVIDUAL_ID);
		Individual<?,?> individual=
			DataSetUtils.
				newHelper(dataSet).
					replace(
						original.id(),
						managedIndividualId("other","managerId"), ManagedIndividual.class);
		assertIsProperlyRenamed(dataSet, original, individual, "managed");
	}

	@Test
	public void testReplacesRelativeIndividual() throws Exception {
		DataSet dataSet = exampleDataSet();
		Individual<?,?> original = dataSet.individualOfId(relativeIndividualId(RESOURCE_NAME.id(), MANAGER_ID, RELATIVE_PATH));
		Individual<?,?> individual=
			DataSetUtils.
				newHelper(dataSet).
					replace(
						original.id(),
						managedIndividualId("other","managerId"), ManagedIndividual.class);
		assertIsProperlyRenamed(dataSet, original, individual, "relative");
	}

	@Test
	public void testReplacesBlankNode() throws Exception {
		DataSet dataSet = exampleDataSet();
		Individual<?,?> original = dataSet.individualOfId(BLANK_NODE);
		Individual<?,?> individual=
			DataSetUtils.
				newHelper(dataSet).
					replace(
						original.id(),
						managedIndividualId("other","managerId"), ManagedIndividual.class);
		assertIsProperlyRenamed(dataSet, original, individual, "bnode");
	}

	@Test
	public void testDoesNotReplacesItself() throws Exception {
		DataSet dataSet = exampleDataSet();
		Individual<?,?> individual=
			DataSetUtils.
				newHelper(dataSet).
					replace(
						managedIndividualId("other","managerId"),
						managedIndividualId("other","managerId"), ManagedIndividual.class);
		assertThat(individual,nullValue());
	}

	@Test
	public void testDoesNotReplacesUnknown() throws Exception {
		DataSet dataSet = exampleDataSet();
		Individual<?,?> individual=
			DataSetUtils.
				newHelper(dataSet).
					replace(
						managedIndividualId("other","managerId"),
						managedIndividualId("another","managerId"), ManagedIndividual.class);
		assertThat(individual,nullValue());
	}

	@Test
	public void testCreatesManagedIndividualIfItDoesNotExist() throws Exception {
		DataSet dataSet = exampleDataSet();
		DataSetUtils.
			newHelper(dataSet).
				managedIndividual(name("other"),MANAGER_ID);
		Individual<?,?> individual =
			dataSet.
				individualOfId(managedIndividualId("other", MANAGER_ID));
		assertThat(individual,notNullValue());
	}

	@Test
	public void testCreatesRelativeIndividualIfItDoesNotExist() throws Exception {
		DataSet dataSet = exampleDataSet();
		DataSetUtils.
			newHelper(dataSet).
				relativeIndividual(name("other"),MANAGER_ID,RELATIVE_PATH);
		Individual<?,?> individual =
			dataSet.
				individualOfId(relativeIndividualId("other", MANAGER_ID, RELATIVE_PATH));
		assertThat(individual,notNullValue());
	}

	@Test
	public void testCreatesLocalIndividualIfItDoesNotExist() throws Exception {
		DataSet dataSet = exampleDataSet();
		DataSetUtils.
			newHelper(dataSet).
				localIndividual(name("other"));
		Individual<?,?> individual =
			dataSet.
				individualOfId(name("other"));
		assertThat(individual,notNullValue());
	}

}
