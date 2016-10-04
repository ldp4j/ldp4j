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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-api:0.2.2
 *   Bundle      : ldp4j-application-kernel-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.constraints;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.kernel.resource.ResourceId;


public class ConstraintReportIdTest {

	@Test
	public void testHasCustomString() {
		ConstraintReportId id = constraintReportId("name","failureId");
		assertThat(id.toString(),not(equalTo(ConstraintReportId.class.getName()+"@"+System.identityHashCode(id))));
	}

	@Test
	public void testReturnsResourceId() {
		assertThat(constraintReportId("name","failureId").resourceId(),equalTo(resourceId("name")));
	}

	@Test
	public void testReturnsFailureId() {
		assertThat(constraintReportId("name","failureId").failureId(),equalTo("failureId"));
	}

	@Test
	public void testEqualOnlyToOtherIds() {
		ConstraintReportId id = constraintReportId("name", "failureId");
		assertThat((Object)id,not(equalTo((Object)"another type")));
	}

	@Test
	public void testEqualToSelf() {
		ConstraintReportId id = constraintReportId("name", "failureId");
		assertThat(id,equalTo(id));
	}

	@Test
	public void testHashCodeAsSelf() {
		ConstraintReportId id = constraintReportId("name", "failureId");
		assertThat(id.hashCode(),equalTo(id.hashCode()));
	}

	@Test
	public void testEqual() {
		ConstraintReportId id1 = constraintReportId("name", "failureId");
		ConstraintReportId id2 = constraintReportId("name", "failureId");
		assertThat(id1,equalTo(id2));
	}

	@Test
	public void testHashCode() {
		ConstraintReportId id1 = constraintReportId("name", "failureId");
		ConstraintReportId id2 = constraintReportId("name", "failureId");
		assertThat(id1.hashCode(),equalTo(id2.hashCode()));
	}

	@Test
	public void testEqualWithDifferentReportIds() {
		ConstraintReportId id1 = constraintReportId("name1", "failureId");
		ConstraintReportId id2 = constraintReportId("name2", "failureId");
		assertThat(id1,not(equalTo(id2)));
	}

	@Test
	public void testHashCodeWithDifferentReportIds() {
		ConstraintReportId id1 = constraintReportId("name1", "failureId");
		ConstraintReportId id2 = constraintReportId("name2", "failureId");
		assertThat(id1.hashCode(),not(equalTo(id2.hashCode())));
	}

	@Test
	public void testEqualWithDifferentFailureIds() {
		ConstraintReportId id1 = constraintReportId("name", "failureId1");
		ConstraintReportId id2 = constraintReportId("name", "failureId2");
		assertThat(id1,not(equalTo(id2)));
	}

	@Test
	public void testHashCodeWithDifferentFailureIds() {
		ConstraintReportId id1 = constraintReportId("name", "failureId1");
		ConstraintReportId id2 = constraintReportId("name", "failureId2");
		assertThat(id1.hashCode(),not(equalTo(id2.hashCode())));
	}

	@Test
	public void testEqualWithDifferentIds() {
		ConstraintReportId id1 = constraintReportId("name1", "failureId1");
		ConstraintReportId id2 = constraintReportId("name2", "failureId2");
		assertThat(id1,not(equalTo(id2)));
	}

	@Test
	public void testHashCodeWithDifferentIds() {
		ConstraintReportId id1 = constraintReportId("name1", "failureId1");
		ConstraintReportId id2 = constraintReportId("name2", "failureId2");
		assertThat(id1.hashCode(),not(equalTo(id2.hashCode())));
	}

	private ConstraintReportId constraintReportId(String name, String failureId) {
		return ConstraintReportId.create(resourceId(name), failureId);
	}

	private ResourceId resourceId(String name) {
		return ResourceId.createId(NamingScheme.getDefault().name(name), "template");
	}

}
