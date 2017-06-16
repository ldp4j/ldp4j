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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean;


import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.rdf.bean.Constraints.ConstraintDefinitionContext;
import org.ldp4j.rdf.bean.ConstraintsTest.Harness.AtLeastUsage;
import org.ldp4j.rdf.bean.ConstraintsTest.Harness.AtMostUsage;
import org.ldp4j.rdf.bean.ConstraintsTest.Harness.Composite;
import org.ldp4j.rdf.bean.ConstraintsTest.Harness.OptionalUsage;
import org.ldp4j.rdf.bean.ConstraintsTest.Harness.RangeUsage;
import org.ldp4j.rdf.bean.ConstraintsTest.Harness.UnboundUsage;
import org.ldp4j.rdf.bean.annotations.AtLeast;
import org.ldp4j.rdf.bean.annotations.AtMost;
import org.ldp4j.rdf.bean.annotations.Optional;
import org.ldp4j.rdf.bean.annotations.Range;
import org.ldp4j.rdf.bean.annotations.Unbound;

public class ConstraintsTest {

	static final class Harness {
		static final class AtMostUsage {
			@AtMost(max=3)
			List<String> valid$list() {
				return null;
			}
			@AtMost(max=3)
			Set<String> valid$set() {
				return null;
			}
			@AtMost(max=3)
			int invalid$type$primitive() {
				return 0;
			}
			@AtMost(max=3)
			Object invalid$type$object() {
				return 0;
			}
			@AtMost(max=1)
			Set<String> invalid$max() {
				return null;
			}
		}
		static final class AtLeastUsage {
			@AtLeast(min=3)
			List<String> valid$list() {
				return null;
			}
			@AtLeast(min=3)
			Set<String> valid$set() {
				return null;
			}
			@AtLeast(min=3)
			int invalid$type$primitive() {
				return 0;
			}
			@AtLeast(min=3)
			Object invalid$type$object() {
				return 0;
			}
			@AtLeast(min=0)
			Set<String> invalid$max() {
				return null;
			}
		}
		static final class RangeUsage {
			@Range(min=1,max=3)
			List<String> valid$list() {
				return null;
			}
			@Range(min=1,max=3)
			Set<String> valid$set() {
				return null;
			}
			@Range(min=1,max=3)
			int invalid$type$primitive() {
				return 0;
			}
			@Range(min=1,max=3)
			Object invalid$type$object() {
				return 0;
			}
			@Range(min=-1,max=3)
			Set<String> invalid$minLowerThanZero() {
				return null;
			}
			@Range(min=0,max=0)
			Set<String> invalid$maxLowerThanOne() {
				return null;
			}
			@Range(min=3,max=1)
			Set<String> invalid$maxLowerThanMin() {
				return null;
			}
			@Range(min=-1,max=-2)
			Set<String> invalid$all() {
				return null;
			}
		}
		static final class OptionalUsage {
			@Optional
			List<String> valid$list() {
				return null;
			}
			@Optional
			Set<String> valid$set() {
				return null;
			}
			@Optional
			int valid$simple$primitive() {
				return 0;
			}
			@Optional
			Object valid$simple$object() {
				return 0;
			}
		}
		static final class UnboundUsage {
			@Unbound
			List<String> valid$list() {
				return null;
			}
			@Unbound
			Set<String> valid$set() {
				return null;
			}
			@Unbound
			int invalid$type$primitive() {
				return 0;
			}
			@Unbound
			Object invalid$type$object() {
				return 0;
			}
		}

		static final class Composite {

			@AtMost(max=3)
			@AtLeast(min=3)
			@Unbound
			@Range(min=1,max=3)
			List<String> valid$list() {
				return null;
			}

			@AtMost(max=3)
			@AtLeast(min=3)
			@Unbound
			@Range(min=1,max=3)
			Set<String> valid$set() {
				return null;
			}

			@AtMost(max=3)
			@AtLeast(min=3)
			@Unbound
			@Range(min=1,max=3)
			String invalid$simple$object() {
				return null;
			}

			@AtMost(max=3)
			@AtLeast(min=3)
			@Unbound
			@Range(min=1,max=3)
			int invalid$simple$primitive() {
				return 0;
			}

		}
	}
	
	@Before
	public void setUp() throws Exception {
	}
	
	private static final class Context implements ConstraintDefinitionContext<Method> {

		private final Class<?> type;
		private final Method member;

		private Context(Method method) {
			this.member=method;
			this.type=method.getReturnType();
		}
		
		@Override
		public Class<?> getPropertyType() {
			return type;
		}

		@Override
		public Method getAnnotatedMember() {
			return member;
		}
		
	}
	
	@Test
	public void testSingleConstraintValidation$AtMost() throws Exception {
		validateHarnessTestCases(AtMostUsage.class);
	}

	@Test
	public void testSingleConstraintValidation$AtLeast() throws Exception {
		validateHarnessTestCases(AtLeastUsage.class);
	}

	@Test
	public void testSingleConstraintValidation$Range() throws Exception {
		validateHarnessTestCases(RangeUsage.class);
	}

	@Test
	public void testSingleConstraintValidation$Optional() throws Exception {
		validateHarnessTestCases(OptionalUsage.class);
	}
	
	@Test
	public void testSingleConstraintValidation$Unbound() throws Exception {
		validateHarnessTestCases(UnboundUsage.class);
	}

	@Test
	public void testCompositeConstraintValidation() throws Exception {
		validateHarnessTestCases(Composite.class);
	}

	/**
	 * @param clazz
	 */
	private void validateHarnessTestCases(Class<?> clazz) {
		for(Method m:clazz.getDeclaredMethods()) {
			if(m.getName().equals("$jacocoInit")) {
				continue;
			}
			String testCase = clazz.getCanonicalName()+"::"+m.getName();
			Context c=new Context(m);
			try {
				List<Cardinality> constraints = Constraints.getCardinalityConstraints(c);
				if(!m.getName().startsWith("valid")) {
					fail("Test case " + testCase + " should fail");
				}
				System.err.println(testCase+": "+constraints);
			} catch (Exception e) {
				if(!m.getName().startsWith("invalid")) {
					fail("Test case "+testCase+" should not fail: "+e.getMessage());
				} else {
					System.err.println(testCase+": "+e.getMessage());
				}
			}
		}
	}

}
