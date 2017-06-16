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


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.net.ProtocolHandlerConfigurator;

public class ProtocolHandlerConfiguratorTest {

	@Before
	public void setUp() throws Exception {
		ProtocolHandlerConfigurator.reset();
	}

	@Test
	public void testAddProvider$valid() throws Exception {
		assertThat(ProtocolHandlerConfigurator.addProvider(Handler.class),equalTo(true));
		assertThat(ProtocolHandlerConfigurator.isSupported("net"),equalTo(true));
		assertThat(ProtocolHandlerConfigurator.getHandler("net"),instanceOf(Handler.class));
	}

	@Test
	public void testAddProvider$repeated() throws Exception {
		ProtocolHandlerConfigurator.addProvider(Handler.class);
		assertThat(ProtocolHandlerConfigurator.isSupported("net"),equalTo(true));
		assertThat(ProtocolHandlerConfigurator.addProvider(Handler.class),equalTo(false));
		assertThat(ProtocolHandlerConfigurator.isSupported("net"),equalTo(true));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddProvider$invalid() throws Exception {
		ProtocolHandlerConfigurator.addProvider(InvalidHandler.class);
	}

	@Test
	public void testGetHandler() throws Exception {
		ProtocolHandlerConfigurator.addProvider(Handler.class);
		assertThat(ProtocolHandlerConfigurator.getHandler("net"),instanceOf(Handler.class));
	}

}
