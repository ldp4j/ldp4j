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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.2.2
 *   Bundle      : ldp4j-application-kernel-jpa-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.encoding;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.io.Serializable;

import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;

import com.google.common.io.BaseEncoding;

@RunWith(JMockit.class)
public class AbstractEncoderTest {

	private final class TestProxy extends AbstractEncoder {
		@Override
		protected Serializable prepare(Name<?> name) {
			return name;
		}

		@Override
		protected <T extends Serializable> Name<T> assemble(Serializable subject) throws IOException {
			throw new IOException("Assemble failure");
		}
	}

	@Test
	public void testEncode$nullName() throws Exception {
		TestProxy sut = new TestProxy();
		String result = sut.encode(null);
		assertThat(result,nullValue());
	}

	@Test
	public void testEncode$exception(@Mocked SerializationUtils mock) throws Exception {
		final Name<String> name = NamingScheme.getDefault().name("test");

		new StrictExpectations() {{
			SerializationUtils.serialize(name);
			result=new IOException("Serialization failure");
		}};

		TestProxy sut = new TestProxy();
		try {
			sut.encode(name);
		} catch (AssertionError e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
		}
	}

	@Test
	public void testDecode$null() throws Exception {
		TestProxy sut = new TestProxy();
		Name<?> result = sut.decode(null);
		assertThat(result,nullValue());
	}

	@Test
	public void testDecode$exception(@Mocked final BaseEncoding encoding) throws Exception {
		final String data = "test";

		new StrictExpectations() {{
			BaseEncoding.base64();result=encoding;
			encoding.decode(data);result=new IOException("Serialization failure");
		}};

		TestProxy sut = new TestProxy();
		try {
			sut.decode(data);
		} catch (AssertionError e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
		}
	}

}
