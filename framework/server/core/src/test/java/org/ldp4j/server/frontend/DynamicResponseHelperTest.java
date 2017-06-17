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
package org.ldp4j.server.frontend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.ldp4j.example.MyApplication;
import org.ldp4j.server.testing.TestingUtil;

public class DynamicResponseHelperTest {

	@Test
	public void validateUpdateExtraction() throws MalformedURLException, IOException {
		assertThat(createHelper().getUpdates(),contains("2015-09-21T10:13:01.344+02:00"));
	}

	DynamicResponseHelper createHelper() throws MalformedURLException {
		DynamicResponseHelper sut=
			new DynamicResponseHelper(
				new URL("http://localhost:57315/ldp4j-server-core/"),
				MyApplication.ROOT_DYNAMIC_RESOURCE_PATH,
				TestingUtil.loadResource("queries/dynamic_dataset.ttl"));
		return sut;
	}

	@Test
	public void validateResolutionExtraction() throws MalformedURLException, IOException {
		assertThat(createHelper().getResolution(),equalTo("http://www.ldp4j.org/fixture/ldp4j/api/rootDynamicResource/"));
	}

	@Test
	public void validateRoundtripExtraction() throws MalformedURLException, IOException {
		assertThat(createHelper().getRoundtrip(),equalTo("OK"));
	}


}
