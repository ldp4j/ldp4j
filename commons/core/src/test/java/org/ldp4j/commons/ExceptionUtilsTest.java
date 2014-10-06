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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.ExceptionUtils;

public class ExceptionUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testToString$sameMethod() throws Exception {
		RuntimeException t = new RuntimeException(new IllegalStateException("Message"));
		StringWriter writer=new StringWriter();
		PrintWriter out=new PrintWriter(writer);
		t.printStackTrace(out);
		String dump=writer.toString();
		String result = ExceptionUtils.toString(t);
		assertThat(result,equalTo(dump));
	}

	@Test
	public void testToString$differentMethod() throws Exception {
		try {
			thrower();
		} catch (Exception e) {
			RuntimeException t = new RuntimeException(e);
			StringWriter writer=new StringWriter();
			PrintWriter out=new PrintWriter(writer);
			t.printStackTrace(out);
			String dump=writer.toString();
			String result = ExceptionUtils.toString(t);
			assertThat(result,equalTo(dump));
		}
	}
	
	private void thrower() throws Exception {
		try {
			throw new IllegalStateException("Message");
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
