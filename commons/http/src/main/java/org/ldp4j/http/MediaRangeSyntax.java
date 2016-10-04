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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Locale;

import com.google.common.base.Strings;

public enum MediaRangeSyntax {
	RFC6838(new RFC6838MediaRangeValidator()),
	RFC7230(new RFC7230MediaRangeValidator()),
	;

	private final MediaRangeValidator validator;

	MediaRangeSyntax(MediaRangeValidator validator) {
		this.validator = validator;
	}

	String checkType(final String type) {
		validateLength(type,"Type");
		this.validator.checkType(type);
		return type.toLowerCase(Locale.ENGLISH);
	}

	String checkSubType(final String subType) {
		validateLength(subType,"Subtype");
		this.validator.checkSubType(subType);
		return subType.toLowerCase(Locale.ENGLISH);
	}

	String checkSuffix(final String suffix) {
		if(!Strings.isNullOrEmpty(suffix)) {
			int idx = suffix.indexOf('+');
			checkArgument(idx<0,"Invalid character '+' in suffix '%s' at %s",suffix,idx);
			this.validator.checkSuffix(suffix);
		}
		return suffix==null?suffix:suffix.toLowerCase(Locale.ENGLISH);
	}

	private void validateLength(final String value, final String name) {
		checkArgument(value!=null,"%s cannot be null",name);
		checkArgument(!value.isEmpty(),"%s cannot be empty",name);
	}


}