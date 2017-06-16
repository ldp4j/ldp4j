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
package org.ldp4j.commons;


import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.IndentUtils;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class IndentUtilsTest {

	private static final String SINGLE_TAB = "\t";
	private static final String DOUBLE_TAB = "\t\t";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testIndentUtils$noLevel() throws Exception {
		IndentUtils sut=new IndentUtils();
		assertThat(sut.indent(),isEmptyString());
	}

	@Test
	public void testIndentUtils$positiveLevel() throws Exception {
		String tabs = "\t\t\t\t\t";
		for(int i=0;i<tabs.length();i++) {
			IndentUtils sut=new IndentUtils(i+1);
			assertThat(sut.indent(),equalTo(tabs.substring(0,i+1)));
		}
	}

	@Test
	public void testIndentUtils$negativeLevel() throws Exception {
		IndentUtils sut=new IndentUtils(-1);
		assertThat(sut.indent(),isEmptyString());
	}

	@Test
	public void testIncrease() throws Exception {
		IndentUtils sut=new IndentUtils();
		sut.increase();
		assertThat(sut.indent(),equalTo(SINGLE_TAB));
	}

	@Test
	public void testDecrease$greaterThanZero() throws Exception {
		IndentUtils sut=new IndentUtils();
		sut.increase();
		sut.increase();
		assertThat(sut.indent(),equalTo(DOUBLE_TAB));
		sut.decrease();
		assertThat(sut.indent(),equalTo(SINGLE_TAB));
	}

	@Test
	public void testDecrease$neverLessThanZero() throws Exception {
		IndentUtils sut=new IndentUtils();
		sut.increase();
		sut.increase();
		assertThat(sut.indent(),equalTo(DOUBLE_TAB));
		sut.decrease();
		sut.decrease();
		sut.decrease();
		assertThat(sut.indent(),isEmptyString());
	}

}
