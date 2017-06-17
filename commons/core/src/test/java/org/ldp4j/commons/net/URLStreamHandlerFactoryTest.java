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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons.net;


import java.net.URLStreamHandler;

import org.ldp4j.commons.net.URLStreamHandlerFactory;
import org.ldp4j.commons.net.URLStreamHandlerFactory.ClassInstantiationException;
import org.ldp4j.commons.net.URLStreamHandlerFactory.ClassInstantiator;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class URLStreamHandlerFactoryTest {

	private static final class FailingInstantiator implements
			ClassInstantiator {
		private Failure failure;

		@Override
		public <T> T instantiateAs(String clsName, Class<? extends T> clazz) throws ClassInstantiationException {
			switch(failure) {
			case CNFE:
				throw new ClassInstantiationException(clsName,new ClassNotFoundException(clsName));
			case IAE:
				throw new ClassInstantiationException(clsName,new IllegalAccessException(clsName));
			default: // IE
				throw new ClassInstantiationException(clsName,new InstantiationException(clsName));
			}
		}
		
		private static enum Failure {
			CNFE,
			IE,
			IAE,
			;
		}

		void setFailure(Failure failure) {
			this.failure = failure;
		}

		@Override
		public String getDescription() {
			return failure.name();
		}
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateHandlerForScheme$existing() throws Exception {
		URLStreamHandlerFactory sut=new URLStreamHandlerFactory(true, true);
		URLStreamHandler result = sut.createForScheme("net", "org.ldp4j.commons");
		assertThat(result,instanceOf(Handler.class));
	}

	@Test
	public void testCreateHandlerForScheme$notExisting() throws Exception {
		URLStreamHandlerFactory sut=new URLStreamHandlerFactory(true, true);
		URLStreamHandler result = sut.createForScheme("set", "org.ldp4j.commons");
		assertThat(result,nullValue());
	}

	@Test
	public void testCreateHandlerForScheme$failure() throws Exception {
		FailingInstantiator instantiator = new FailingInstantiator();
		URLStreamHandlerFactory sut=new URLStreamHandlerFactory(instantiator);
		for(FailingInstantiator.Failure failure:FailingInstantiator.Failure.values()) {
			instantiator.setFailure(failure);
			URLStreamHandler result = sut.createForScheme("set", "org.ldp4j.commons");
			assertThat(result,nullValue());
		}
	}
}
