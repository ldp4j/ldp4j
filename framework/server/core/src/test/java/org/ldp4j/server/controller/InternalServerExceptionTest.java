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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Throwables;

@RunWith(JMockit.class)
public class InternalServerExceptionTest {

	@Mocked private OperationContext context;

	private Throwable cause=new RuntimeException("Failure");

	private InternalServerException sut;

	@Test
	public void testConstructor$withoutCauseAndMessage() throws Exception {
		sut=new InternalServerException(context,null);
		assertThat(sut.getDiagnosis().diagnostic(),equalTo("Unexpected application failure"));
	}

	@Test
	public void testConstructor$withoutCauseButMessage() throws Exception {
		sut=new InternalServerException(context,"Failure",null);
		assertThat(sut.getDiagnosis().diagnostic(),equalTo("Failure"));
	}

	@Test
	public void testConstructor$withoutMessage() throws Exception {
		sut=new InternalServerException(context, cause);
		assertThat(sut.getDiagnosis().diagnostic(),startsWith(Throwables.getStackTraceAsString(cause)));
	}

	@Test
	public void testConstructor$withNonEmptyMessage() throws Exception {
		sut=new InternalServerException(context, "Message",cause);
		String diagnostic = sut.getDiagnosis().diagnostic();
		assertThat(diagnostic,startsWith("Message"));
		assertThat(diagnostic,containsString(Throwables.getStackTraceAsString(cause)));
	}

	@Test
	public void testConstructor$withEmptyMessage() throws Exception {
		sut=new InternalServerException(context, "",cause);
		String diagnostic = sut.getDiagnosis().diagnostic();
		assertThat(diagnostic,startsWith(Throwables.getStackTraceAsString(cause)));
	}
}
