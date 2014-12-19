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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-reflection:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-reflection-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.reflect;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;
import org.ldp4j.reflect.harness.Childclass;
import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.spi.MetaModelFactory;
import org.ldp4j.reflect.spi.ModelFactory;
import org.ldp4j.reflect.spi.RuntimeDelegate;

public class ReflectionTest {

	@Mocked RuntimeDelegate delegate;
	@Mocked MetaModelFactory metaModelFactory;
	@Mocked ModelFactory modelFactory;
	@Mocked MetaClass<Childclass> metaClass;

	@Test
	public void testOf$type() throws Exception {
		new Expectations() {{
			RuntimeDelegate.getInstance(); result = delegate;
			delegate.getMetaModelFactory(); result = metaModelFactory;
			RuntimeDelegate.getInstance(); result = delegate;
			delegate.getModelFactory(); result = modelFactory;
			metaModelFactory.newMetaClass(Childclass.class,Childclass.class); result = metaClass;
			metaClass.get(); result = Childclass.class;
		}};
		MetaClass<Childclass> mc = Reflection.of(Childclass.class);
		assertThat(mc,notNullValue());
		assertThat(mc.get(),equalTo(Childclass.class));
	}

}
