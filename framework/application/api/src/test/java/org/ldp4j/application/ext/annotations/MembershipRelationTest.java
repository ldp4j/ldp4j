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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.ext.annotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.net.URI;
import java.util.Arrays;

import org.junit.Test;
import org.ldp4j.application.vocabulary.LDP;

public class MembershipRelationTest {

	@Test
	public void testValues() {
		assertThat(Arrays.asList(MembershipRelation.values()),contains(MembershipRelation.HAS_MEMBER,MembershipRelation.IS_MEMBER_OF));
	}

	@Test
	public void testValueOf() {
		for(MembershipRelation value:MembershipRelation.values()) {
			assertThat(MembershipRelation.valueOf(value.toString()),equalTo(value));
		}
	}

	@Test
	public void testTerm$hasMember() throws Exception {
		assertThat(MembershipRelation.HAS_MEMBER.term(),equalTo(LDP.HAS_MEMBER_RELATION));
	}

	@Test
	public void testTerm$isMemberOf() throws Exception {
		assertThat(MembershipRelation.IS_MEMBER_OF.term(),equalTo(LDP.IS_MEMBER_OF_RELATION));
	}

	@Test
	public void testToURI$hasMember() throws Exception {
		assertThat(MembershipRelation.HAS_MEMBER.toURI(),equalTo(LDP.HAS_MEMBER_RELATION.as(URI.class)));
	}

	@Test
	public void testToURI$isMemberOf() throws Exception {
		assertThat(MembershipRelation.IS_MEMBER_OF.toURI(),equalTo(LDP.IS_MEMBER_OF_RELATION.as(URI.class)));
	}

}
