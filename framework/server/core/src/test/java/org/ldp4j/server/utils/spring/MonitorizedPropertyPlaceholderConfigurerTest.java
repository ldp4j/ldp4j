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
package org.ldp4j.server.utils.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.osgi.io.OsgiBundleResource;

public class MonitorizedPropertyPlaceholderConfigurerTest {

	private static final int BUNDLE_ID = 123;
	private static final String BUNDLE_SYMBOLIC_NAME = "SymbolicName";

	@Rule
	public TemporaryFolder folder=new TemporaryFolder();

	private Resource[] locations;
	private CustomInputStreamResource customResource;

	@Before
	public void setUp() throws Exception {
		Properties property=new Properties();
		property.setProperty("finalProperty", "other value");
		File tmpFile = folder.newFile("dynamicValues.cfg");
		OutputStream out=null;
		try {
			out=new FileOutputStream(tmpFile);
			property.store(out, "Test values");
		} finally {
			out.close();
		}

		File otherFile=folder.newFile("otherValues.cfg");
		property.setProperty("finalProperty", "my value");
		try {
			out=new FileOutputStream(otherFile);
			property.store(out, "Other test values");
		} finally {
			out.close();
		}
		this.customResource = CustomInputStreamResource.create(otherFile);
		this.locations = new Resource[] {
			new FileSystemResource(tmpFile),
			new ClassPathResource("resource.properties"),
			new ClassPathResource("nonExistingResource.properties"),
			this.customResource,
			new UrlResource(this.getClass().getResource("/otherResource.cfg")),
			new OsgiBundleResource(new CustomBundle(BUNDLE_ID, BUNDLE_SYMBOLIC_NAME),"/path")
		};
	}

	@After
	public void tearDown() throws Exception {
		IOUtils.closeQuietly(this.customResource);
	}

	@Test
	public void testMergeProperties$dontShow() throws Exception{
		MonitorizedPropertyPlaceholderConfigurer sut=new MonitorizedPropertyPlaceholderConfigurer();
		sut.setIgnoreResourceNotFound(true);
		sut.setLocations(this.locations);
		Properties mergeProperties = sut.mergeProperties();
		assertThat(mergeProperties.size(),is(4));
	}

	@Test
	public void testMergeProperties$show() throws Exception{
		MonitorizedPropertyPlaceholderConfigurer sut=new MonitorizedPropertyPlaceholderConfigurer();
		sut.setModuleName("module");
		sut.setIgnoreResourceNotFound(true);
		sut.setLocations(this.locations);
		System.setProperty(MonitorizedPropertyPlaceholderConfigurer.LDP4J_CONFIG_DUMP, Boolean.TRUE.toString());
		System.setProperty(MonitorizedPropertyPlaceholderConfigurer.LDP4J_CONFIG_MODULES, "modules, module");
		Properties mergeProperties = sut.mergeProperties();
		assertThat(mergeProperties.size(),is(4));
	}

}