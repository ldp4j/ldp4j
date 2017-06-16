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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-core:0.2.2
 *   Bundle      : rmf-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.impl;

import org.ldp4j.rdf.spi.Configuration;

public final class UnmarshallOptions {

	public static final String TRIPLE_ORDERING="org.ldp4j.rdf.unmarshall.triple_ordering";

	public enum Ordering {
		SORT_TRIPLES,
		KEEP_TRIPLE_ORDER,
		;
	}
	
	public static final String UNMARSHALL_STYLE="org.ldp4j.rdf.unmarshall.style";
	
	public enum UnmarshallStyle {
		REPOSITORY_BASED,
		PARSER_BASED,
		;
	}

	static Ordering ordering(Configuration configuration) {
		return configuration.getOption(TRIPLE_ORDERING, Ordering.class, Ordering.SORT_TRIPLES);
	}

	static UnmarshallStyle style(Configuration configuration) {
		return configuration.getOption(UNMARSHALL_STYLE, UnmarshallStyle.class, UnmarshallStyle.REPOSITORY_BASED);
	}

}
