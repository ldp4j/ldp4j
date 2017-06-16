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
package org.ldp4j.application.sdk;

import java.util.Date;

import org.junit.Test;
import org.ldp4j.application.engine.context.HttpRequest;
import org.ldp4j.application.engine.context.HttpRequest.HttpMethod;
import org.ldp4j.application.engine.context.HttpRequest.ProtocolVersion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class HttpRequestBuilderTest {


	private static final HttpMethod DEFAULT_METHOD = HttpMethod.GET;
	private static final String DEFAULT_PATH = "/absolute/path";
	private static final String DEFAULT_HOST = "www.example.org";

	private void assertDateIsInRange(Date before, Date after, Date date) {
		assertThat(date,notNullValue());
		assertThat(date.getTime(),greaterThanOrEqualTo(before.getTime()));
		assertThat(date.getTime(),not(greaterThan(after.getTime())));
	}

	private HttpRequestBuilder sut() {
		return
			HttpRequestBuilder.
				newInstance().
					withMethod(DEFAULT_METHOD).
					withHost(DEFAULT_HOST).
					withAbsolutePath(DEFAULT_PATH);
	}

	private void verifyDefaultValues(HttpRequest build) {
		assertThat(build.body(),nullValue());
		assertThat(build.headers(),hasSize(0));
		assertThat(build.protocolVersion(),equalTo(HttpRequest.ProtocolVersion.HTTP_1_1));
		assertThat(build.method(),equalTo(DEFAULT_METHOD));
		assertThat(build.host(),equalTo(DEFAULT_HOST));
		assertThat(build.absolutePath(),equalTo(DEFAULT_PATH));
		String value = build.toString();
		assertThat(value,notNullValue());
	}

	@Test(expected=NullPointerException.class)
	public void testRequireMethod() {
		sut().withMethod(null).build();
	}

	@Test(expected=NullPointerException.class)
	public void testRequireHost() {
		sut().withHost(null).build();
	}

	@Test(expected=NullPointerException.class)
	public void testRequireAbsolutePath() {
		sut().withAbsolutePath(null).build();
	}

	@Test
	public void testDefaultBuild() {
		Date before=new Date();
		HttpRequest build=sut().build();
		Date after=new Date();
		assertDateIsInRange(before, after, build.serverDate());
		assertThat(build.clientDate(),nullValue());
		verifyDefaultValues(build);
	}

	@Test
	public void testWithServerDate$nonNull() {
		Date date = new Date(System.currentTimeMillis()-3600000);
		HttpRequest build =sut().withServerDate(date).build();
		assertThat(build.serverDate(),equalTo(date));
		assertThat(build.clientDate(),nullValue());
		verifyDefaultValues(build);
	}

	@Test
	public void testWithServerDate$null() {
		Date before=new Date();
		HttpRequest build=sut().withServerDate(null).build();
		Date after=new Date();
		assertDateIsInRange(before, after, build.serverDate());
		assertThat(build.clientDate(),nullValue());
		verifyDefaultValues(build);
	}

	@Test
	public void testWithClientDate$nonNull() {
		Date date = new Date(System.currentTimeMillis()-3600000);
		Date before=new Date();
		HttpRequest build=sut().withClientDate(date).build();
		Date after=new Date();
		assertThat(build.clientDate(),equalTo(date));
		assertDateIsInRange(before, after, build.serverDate());
		verifyDefaultValues(build);
	}

	@Test
	public void testWithClientDate$null() {
		Date before=new Date();
		HttpRequest build=sut().withClientDate(null).build();
		Date after=new Date();
		assertDateIsInRange(before, after, build.serverDate());
		assertThat(build.clientDate(),nullValue());
		verifyDefaultValues(build);
	}

	@Test
	public void testWithProtocolVersion$null(){
		HttpRequest build=sut().withProtocolVersion(null).build();
		assertThat(build.protocolVersion(),equalTo(HttpRequest.ProtocolVersion.HTTP_1_1));
	}

	@Test
	public void testWithProtocolVersion$notNull(){
		ProtocolVersion otherValue = HttpRequest.ProtocolVersion.HTTP_1_0;
		HttpRequest build=sut().withProtocolVersion(otherValue).build();
		assertThat(build.protocolVersion(),equalTo(otherValue));
	}

	@Test(expected=NullPointerException.class)
	public void testHeaderNameCannotBeNull(){
		sut().withHeader(null,"value");
	}

	@Test(expected=NullPointerException.class)
	public void testHeaderRawValueCannotBeNull(){
		sut().withHeader("name",null);
	}

	@Test
	public void testWithHeader(){
		String name = "name";
		String rawValue = "value";
		HttpRequest build = sut().withHeader(name,rawValue).build();
		assertThat(build.headers(),hasSize(1));
		assertThat(build.headers().get(0).name(),equalTo("name"));
		assertThat(build.headers().get(0).rawValue(),equalTo("value"));
		assertThat(build.headers().get(0).elements(),hasSize(0));
	}

	@Test
	public void testHeadersAreNotDuplicated(){
		String name = "name";
		String rawValue = "value";
		HttpRequest build =
				sut().
					withHeader(name,rawValue).
					withHeader(name,rawValue).
					build();
		assertThat(build.headers(),hasSize(1));
		assertThat(build.headers().get(0).name(),equalTo("name"));
		assertThat(build.headers().get(0).rawValue(),equalTo("value"));
		assertThat(build.headers().get(0).elements(),hasSize(0));
	}

	@Test
	public void testWithEntity(){
		HttpRequest build = sut().withBody("entity").build();
		assertThat(build.body(),equalTo("entity"));
	}

}
