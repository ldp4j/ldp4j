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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.config.util;

import org.junit.Test;
import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.ConfigurationException;
import org.ldp4j.application.config.core.DefaultImmutableConfiguration;
import org.ldp4j.application.config.core.DefaultMutableConfiguration;
import org.ldp4j.application.config.util.Configurator;

import com.google.common.base.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ConfiguratorTest {

	private static class MockConfig extends DefaultMutableConfiguration {

		/**
		 *
		 */
		private static final long serialVersionUID = 3283447365055677498L;

	}

	private static class MockConfigurable implements Configurable<MockConfig> {

		private MockConfig config=null;

		private final boolean configurable;

		private MockConfigurable(boolean configurable) {
			this.configurable=configurable;
		}

		@Override
		public void configure(MockConfig configuration) throws ConfigurationException {
			this.config=configuration;
		}

		@Override
		public Class<? extends MockConfig> configType() {
			return MockConfig.class;
		}

		@Override
		public boolean canConfigure() {
			return this.configurable;
		}

	}

	@Test
	public void testConfigure$configurable$validConfiguration$ready() throws Exception {
		MockConfigurable mock=new MockConfigurable(true);
		MockConfig config=new MockConfig();
		Configurator.configure(mock, config);
		assertThat(mock.config,sameInstance(config));
	}

	@Test(expected=ConfigurationException.class)
	public void testConfigure$configurable$invalidConfiguration$ready() throws Exception {
		MockConfigurable mock=new MockConfigurable(true);
		Configuration config=new DefaultImmutableConfiguration();
		Configurator.configure(mock, config);
	}

	@Test(expected=ConfigurationException.class)
	public void testConfigure$configurable$validConfiguration$notReady() throws Exception {
		MockConfigurable mock=new MockConfigurable(false);
		MockConfig config=new MockConfig();
		Configurator.configure(mock, config);
	}

	@Test(expected=ConfigurationException.class)
	public void testConfigure$configurable$invalidConfiguration$notReady() throws Exception {
		MockConfigurable mock=new MockConfigurable(false);
		Configuration config=new DefaultImmutableConfiguration();
		Configurator.configure(mock, config);
	}

	@Test
	public void testConfigure$notConfigurable() throws Exception {
		Configuration config=new DefaultImmutableConfiguration();
		Configurator.configure(new Object(), config);
	}

	// Configure with custom exception
	@Test
	public void testConfigureCustomException$configurable$validConfiguration$ready() {
		MockConfigurable mock=new MockConfigurable(true);
		MockConfig config=new MockConfig();
		Configurator.configure(mock, config, new RuntimeException());
		assertThat(mock.config,sameInstance(config));
	}

	@Test(expected=RuntimeException.class)
	public void testConfigureCustomException$configurable$invalidConfiguration$ready() {
		MockConfigurable mock=new MockConfigurable(true);
		Configuration config=new DefaultImmutableConfiguration();
		Configurator.configure(mock, config, new RuntimeException());
	}

	@Test(expected=RuntimeException.class)
	public void testConfigureCustomException$configurable$validConfiguration$notReady() {
		MockConfigurable mock=new MockConfigurable(false);
		MockConfig config=new MockConfig();
		Configurator.configure(mock, config, new RuntimeException());
	}

	@Test(expected=RuntimeException.class)
	public void testConfigureCustomException$configurable$invalidConfiguration$notReady() {
		MockConfigurable mock=new MockConfigurable(false);
		Configuration config=new DefaultImmutableConfiguration();
		Configurator.configure(mock, config, new RuntimeException());
	}

	@Test
	public void testConfigureCustomException$notConfigurable() {
		Configuration config=new DefaultImmutableConfiguration();
		Configurator.configure(new Object(), config, new RuntimeException());
	}

	// Try Configure

	@Test
	public void testTryConfigure$configurable$validConfiguration$ready() throws Exception {
		MockConfigurable mock=new MockConfigurable(true);
		MockConfig config=new MockConfig();
		Optional<Boolean> result = Configurator.tryConfigure(mock, config);
		assertThat(result,notNullValue());
		assertThat(mock.config,sameInstance(config));
		assertThat(result.isPresent(),equalTo(true));
		assertThat(result.get(),equalTo(true));
	}

	@Test
	public void testTryConfigure$configurable$invalidConfiguration$ready() throws Exception {
		MockConfigurable mock=new MockConfigurable(true);
		Configuration config=new DefaultImmutableConfiguration();
		Optional<Boolean> result = Configurator.tryConfigure(mock, config);
		assertThat(result,notNullValue());
		assertThat(result.isPresent(),equalTo(true));
		assertThat(result.get(),equalTo(false));
	}

	@Test
	public void testTryConfigure$configurable$validConfiguration$notReady() throws Exception {
		MockConfigurable mock=new MockConfigurable(false);
		MockConfig config=new MockConfig();
		Optional<Boolean> result = Configurator.tryConfigure(mock, config);
		assertThat(result,notNullValue());
		assertThat(result.isPresent(),equalTo(true));
		assertThat(result.get(),equalTo(false));
	}

	@Test
	public void testTryConfigure$configurable$invalidConfiguration$notReady() throws Exception {
		MockConfigurable mock=new MockConfigurable(false);
		Configuration config=new DefaultImmutableConfiguration();
		Optional<Boolean> result = Configurator.tryConfigure(mock, config);
		assertThat(result,notNullValue());
		assertThat(result.isPresent(),equalTo(true));
		assertThat(result.get(),equalTo(false));
	}

	@Test
	public void testTryConfigure$notConfigurable() throws Exception {
		Configuration config=new DefaultImmutableConfiguration();
		Optional<Boolean> result = Configurator.tryConfigure(new Object(), config);
		assertThat(result,notNullValue());
		assertThat(result.isPresent(),equalTo(false));
	}

}
