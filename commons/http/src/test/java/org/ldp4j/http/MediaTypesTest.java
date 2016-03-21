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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-http-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;

@RunWith(JMockit.class)
public class MediaTypesTest {

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(MediaTypes.class),equalTo(true));
	}

	@Test
	public void dependsOnImmutableMediaType() {
		final String mediaType = "text/turtle";
		new MockUp<ImmutableMediaType>() {
			@Mock
			public ImmutableMediaType fromString(Invocation context, String aValue) {
				assertThat(aValue,equalTo(mediaType));
				return context.proceed(aValue);
			}
		};
		MediaType result=MediaTypes.fromString(mediaType);
		assertThat(result.type(),equalTo("text"));
		assertThat(result.subType(),equalTo("turtle"));
	}


}
