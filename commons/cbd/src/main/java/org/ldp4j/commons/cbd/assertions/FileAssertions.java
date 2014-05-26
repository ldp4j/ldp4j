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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-cbd:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-cbd-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons.cbd.assertions;

import java.io.File;

final class FileAssertions {
	
	private FileAssertions() {
	}

	public static AbstractFilter<File> exists() {
		return new ChainedFilter<File>(ObjectAssertions.<File>notNull()) {
			@Override
			protected IExpectation<File> createChainedExpectation(final File value) {
				return new Expectation<File>(value.exists(), String.format("the path '%s' does not exist",value.getAbsolutePath()),value);
			}
		};
	}

	public static AbstractFilter<File> isFile() {
		return new ChainedFilter<File>(ObjectAssertions.<File>notNull()) {
			@Override
			protected IExpectation<File> createChainedExpectation(final File value) {
				return new Expectation<File>(value.isFile(),String.format("the path '%s' is not a file",value.getAbsolutePath()),value);
			}
		};
	}

	public static AbstractFilter<File> isDirectory() {
		return new ChainedFilter<File>(ObjectAssertions.<File>notNull()) {
			@Override
			protected IExpectation<File> createChainedExpectation(final File value) {
				return new Expectation<File>(value.isDirectory(), String.format("the path '%s' is not a directory",value.getAbsolutePath()),value);
			}
		};
	}
	
}