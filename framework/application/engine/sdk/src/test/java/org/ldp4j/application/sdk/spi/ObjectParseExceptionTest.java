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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.2
 *   Bundle      : ldp4j-application-engine-sdk-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk.spi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class ObjectParseExceptionTest {

	private static final Throwable CAUSE = new RuntimeException();
	private static final String MESSAGE = "message";
	private static final Class<?> VALUE_CLASS = Object.class;
	private static final String VALUE = "value";

	@Test
	public void testExceptionWithMessage() throws Exception {
		ObjectParseException sut = new ObjectParseException(MESSAGE, VALUE_CLASS,VALUE);
		assertThat(sut.getRawValue(),equalTo(VALUE));
		assertThat((Object)sut.getValueClass(),sameInstance((Object)VALUE_CLASS));
		assertThat(sut.getCause(),nullValue());
		assertThat(sut.getMessage(),equalTo(MESSAGE));
	}

	@Test
	public void testExceptionWithCause() throws Exception {
		ObjectParseException sut = new ObjectParseException(CAUSE, VALUE_CLASS,VALUE);
		assertThat(sut.getRawValue(),equalTo(VALUE));
		assertThat((Object)sut.getValueClass(),sameInstance((Object)VALUE_CLASS));
		assertThat(sut.getCause(),sameInstance(CAUSE));
		assertThat(sut.getMessage(),notNullValue());
	}

	@Test
	public void testExceptionWithCauseAndMessage() throws Exception {
		ObjectParseException sut = new ObjectParseException(MESSAGE,CAUSE, VALUE_CLASS,VALUE);
		assertThat(sut.getRawValue(),equalTo(VALUE));
		assertThat((Object)sut.getValueClass(),sameInstance((Object)VALUE_CLASS));
		assertThat(sut.getCause(),sameInstance(CAUSE));
		assertThat(sut.getMessage(),equalTo(MESSAGE));
	}

}
