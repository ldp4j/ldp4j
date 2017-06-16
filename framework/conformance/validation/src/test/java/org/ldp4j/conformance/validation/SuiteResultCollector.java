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
 *   Artifact    : org.ldp4j.framework:ldp4j-conformance-validation:0.2.2
 *   Bundle      : ldp4j-conformance-validation-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.conformance.validation;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.google.common.collect.Lists;

public final class SuiteResultCollector extends TestListenerAdapter {

	static final class SuiteResults {

		enum Result {
			PASSED {
				@Override
				List<String> tests(SuiteResults result) {
					return result.passed;
				}
			},
			FAILED{
				@Override
				List<String> tests(SuiteResults result) {
					return result.failed;
				}
			},
			SKIPPED{
				@Override
				List<String> tests(SuiteResults result) {
					return result.skipped;
				}
			}
			;

			abstract List<String> tests(SuiteResults result);

		}

		private final List<String> passed=Lists.newArrayList();
		private final List<String> failed=Lists.newArrayList();
		private final List<String> skipped=Lists.newArrayList();

		int numberOfTests(Result... results) {
			int n=0;
			for(Result result:results) {
				n+=result.tests(this).size();
			}
			return n;
		}

		boolean hasTestResult(String test, Result result) {
			return result.tests(this).contains(test);
		}

		@Override
		public String toString() {
			StringBuilder builder=new StringBuilder();
			builder.append("SuiteResult {").append(System.lineSeparator());
			builder.append(" - Passed tests : ").append(passed).append(System.lineSeparator());
			builder.append(" - Failed tests : ").append(failed).append(System.lineSeparator());
			builder.append(" - Skipped tests: ").append(skipped).append(System.lineSeparator());
			builder.append("}").append(System.lineSeparator());
			return builder.toString();
		}

	}

	private static final AtomicReference<SuiteResults> LAST_TEST_RESULT=new AtomicReference<SuiteResults>();
	private SuiteResults status;

	@Override
	public void onStart(ITestContext testContext) {
		this.status = new SuiteResults();
		LAST_TEST_RESULT.set(status);
	}

	@Override
	public void onFinish(ITestContext testContext) {
		// Nothing to do
	}

	@Override
	public void onTestFailure(ITestResult tr) {
		this.status.failed.add(tr.getName());
	}

	@Override
	public void onTestSkipped(ITestResult tr) {
		this.status.skipped.add(tr.getName());
	}

	@Override
	public void onTestSuccess(ITestResult tr) {
		this.status.passed.add(tr.getName());
	}

	static SuiteResults lastResults() {
		return LAST_TEST_RESULT.get();
	}

}
