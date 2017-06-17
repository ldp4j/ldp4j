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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.ldp4j.application.data.IndividualReference.anonymous;
import static org.ldp4j.application.data.IndividualReference.external;
import static org.ldp4j.application.data.IndividualReference.managed;
import static org.ldp4j.application.data.IndividualReference.newIndividual;
import static org.ldp4j.application.data.IndividualReference.relative;

import org.junit.Test;
import org.ldp4j.application.data.IndividualReference.ExternalIndividualReference;
import org.ldp4j.application.data.IndividualReference.LocalIndividualReference;
import org.ldp4j.application.data.IndividualReference.ManagedIndividualReference;
import org.ldp4j.application.data.IndividualReference.NewIndividualReference;
import org.ldp4j.application.data.IndividualReference.RelativeIndividualReference;

public class IndividualReferenceTest extends DataTestingSupport {

	private static final String HAS_TITLE = "http://www.ldp4j.org/example#hasTitle";

	private static class FailingVisitor implements IndividualReferenceVisitor {
		@Override
		public void visitLocalIndividualReference(LocalIndividualReference reference) {
			fail("Unexpected reference");
		}
		@Override
		public void visitManagedIndividualReference(ManagedIndividualReference reference) {
			fail("Unexpected reference");
		}
		@Override
		public void visitRelativeIndividualReference(RelativeIndividualReference reference) {
			fail("Unexpected reference");
		}
		@Override
		public void visitExternalIndividualReference(ExternalIndividualReference reference) {
			fail("Unexpected reference");
		}
		@Override
		public void visitNewIndividualReference(NewIndividualReference reference) {
			fail("Unexpected reference");
		}
	}

	private DataSet exampleDataSet() {
		DataSet dataSet=
			DataDSL.
				dataSet().
					individual(managed(MANAGED_INDIVIDUAL_ID)).
						hasProperty(HAS_TITLE).
							withValue("managed").
					individual(relative(MANAGED_INDIVIDUAL_ID,RELATIVE_PATH)).
						hasProperty(HAS_TITLE).
							withValue("relative").
					individual(newIndividual(SELF)).
						hasProperty(HAS_TITLE).
							withValue("self").
					individual(newIndividual(NEW)).
						hasProperty(HAS_TITLE).
							withValue("new").
					individual(anonymous(BLANK_NODE)).
						hasProperty(HAS_TITLE).
							withValue("bnode").
					individual(external(EXTERNAL)).
						hasProperty(HAS_TITLE).
							withValue("external").
					build();
		return dataSet;
	}


	@Test
	public void testVisitor$LocalIndividualReference() {
		anonymous(BLANK_NODE).
			accept(
				new FailingVisitor() {
					@SuppressWarnings("rawtypes")
					@Override
					public void visitLocalIndividualReference(LocalIndividualReference reference) {
						assertThat(reference.ref(),equalTo((Name)BLANK_NODE));
					}
				}
			);
	}

	@Test
	public void testVisitor$ManagedIndividualReference() {
		managed(MANAGED_INDIVIDUAL_ID).
			accept(
				new FailingVisitor() {
					@Override
					public void visitManagedIndividualReference(ManagedIndividualReference reference) {
						assertThat(reference.ref(),equalTo(MANAGED_INDIVIDUAL_ID));
					}
				}
			);
	}

	@Test
	public void testVisitor$ManagedIndividualReference$indirectId() {
		managed(RELATIVE_PATH,MANAGED_INDIVIDUAL_ID).
			accept(
				new FailingVisitor() {
					@Override
					public void visitManagedIndividualReference(ManagedIndividualReference reference) {
						assertThat(reference.ref(),equalTo(ManagedIndividualId.createId(RELATIVE_PATH,MANAGED_INDIVIDUAL_ID)));
					}
				}
			);
	}

	@Test
	public void testVisitor$ManagedIndividualReference$indirectId$alternative() {
		managed(RESOURCE_NAME,MANAGER_ID,RELATIVE_PATH).
			accept(
				new FailingVisitor() {
					@Override
					public void visitManagedIndividualReference(ManagedIndividualReference reference) {
						assertThat(reference.ref(),equalTo(ManagedIndividualId.createId(RELATIVE_PATH,MANAGED_INDIVIDUAL_ID)));
					}
				}
			);
	}

	@Test
	public void testVisitor$RelativeIndividualReference() {
		relative(MANAGED_INDIVIDUAL_ID, RELATIVE_PATH).
			accept(
				new FailingVisitor() {
					@Override
					public void visitRelativeIndividualReference(RelativeIndividualReference reference) {
						assertThat(reference.ref(),equalTo(RelativeIndividualId.createId(MANAGED_INDIVIDUAL_ID, RELATIVE_PATH)));
					}
				}
			);
	}

	@Test
	public void testVisitor$ExternalIndividualReference() {
		external(EXTERNAL).
			accept(
				new FailingVisitor() {
					@Override
					public void visitExternalIndividualReference(ExternalIndividualReference reference) {
						assertThat(reference.ref(),equalTo(EXTERNAL));
					}
				}
			);
	}

	@Test
	public void testVisitor$NewIndividualReference$SELF() {
		newIndividual(SELF).
			accept(
				new FailingVisitor() {
					@Override
					public void visitNewIndividualReference(NewIndividualReference reference) {
						assertThat(reference.ref(),equalTo(SELF));
					}
				}
			);
	}

	@Test
	public void testVisitor$NewIndividualReference$NEW() {
		newIndividual(NEW).
			accept(
				new FailingVisitor() {
					@Override
					public void visitNewIndividualReference(NewIndividualReference reference) {
						assertThat(reference.ref(),equalTo(NEW));
					}
				}
			);
	}

	@Test
	public void testIsPresent$LocalIndividualReference$existingIndividual() {
		assertThat(anonymous(BLANK_NODE).isPresent(exampleDataSet()),equalTo(true));
	}

	@Test
	public void testIsPresent$ManagedIndividualReference$existingIndividual() {
		assertThat(managed(MANAGED_INDIVIDUAL_ID).isPresent(exampleDataSet()),equalTo(true));
	}

	@Test
	public void testIsPresent$RelativeIndividualReference$existingIndividual() {
		assertThat(relative(MANAGED_INDIVIDUAL_ID, RELATIVE_PATH).isPresent(exampleDataSet()),equalTo(true));
	}

	@Test
	public void testIsPresent$ExternalIndividualReference$existingIndividual() {
		assertThat(external(EXTERNAL).isPresent(exampleDataSet()),equalTo(true));
	}

	@Test
	public void testIsPresent$NewIndividualReference$SELF$existingIndividual() {
		assertThat(newIndividual(SELF).isPresent(exampleDataSet()),equalTo(true));
	}

	@Test
	public void testIsPresent$NewIndividualReference$NEW$existingIndividual() {
		assertThat(newIndividual(NEW).isPresent(exampleDataSet()),equalTo(true));
	}

	@Test
	public void testIsPresent$LocalIndividualReference$nonExistingIndividual() {
		assertThat(anonymous(BLANK_NODE).isPresent(emptyDataSet()),equalTo(false));
	}

	@Test
	public void testIsPresent$ManagedIndividualReference$nonExistingIndividual() {
		assertThat(managed(MANAGED_INDIVIDUAL_ID).isPresent(emptyDataSet()),equalTo(false));
	}

	@Test
	public void testIsPresent$RelativeIndividualReference$nonExistingIndividual() {
		assertThat(relative(MANAGED_INDIVIDUAL_ID, RELATIVE_PATH).isPresent(emptyDataSet()),equalTo(false));
	}

	@Test
	public void testIsPresent$ExternalIndividualReference$nonExistingIndividual() {
		assertThat(external(EXTERNAL).isPresent(emptyDataSet()),equalTo(false));
	}

	@Test
	public void testIsPresent$NewIndividualReference$SELF$nonExistingIndividual() {
		assertThat(newIndividual(SELF).isPresent(emptyDataSet()),equalTo(false));
	}

	@Test
	public void testIsPresent$NewIndividualReference$NEW$nonExistingIndividual() {
		assertThat(newIndividual(NEW).isPresent(emptyDataSet()),equalTo(false));
	}

	@Test
	public void testRoundtrip() {
		DataSet exampleDataSet = exampleDataSet();
		DataSet emptyDataSet=emptyDataSet();
		for(Individual<?,?> individual:exampleDataSet) {
			IndividualReference<?, ?> reference = IndividualReference.fromIndividual(individual);
			assertThat(reference.isPresent(exampleDataSet),equalTo(true));
			assertThat(reference.isPresent(emptyDataSet),equalTo(false));
			assertThat((Object)reference.resolve(exampleDataSet),sameInstance((Object)individual));
		}
	}

	private DataSet emptyDataSet() {
		return DataSets.createDataSet(null);
	}

}
