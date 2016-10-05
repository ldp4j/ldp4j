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

import java.util.Comparator;

final class LanguageComparator implements Comparator<Language> {

	static final LanguageComparator INSTANCE=new LanguageComparator();

	private LanguageComparator() {
	}

	@Override
	public int compare(final Language o1, final Language o2) {
		if(o1.isWildcard()) {
			if(!o2.isWildcard()) {
				return 1;
			} else {
				return 0;
			}
		} else if(o2.isWildcard()) {
			return -1;
		}
		return compareTags(o1, o2);
	}

	private int compareTags(final Language o1, final Language o2) {
		int comparison=o1.primaryTag().compareTo(o2.primaryTag());
		if(comparison!=0) {
			return comparison;
		}
		return compareSubTags(o1, o2);
	}

	private int compareSubTags(final Language o1, final Language o2) {
		if(!o1.subTag().isEmpty() && o2.subTag().isEmpty()) {
			return -1;
		}
		if(!o2.subTag().isEmpty() && o1.subTag().isEmpty()) {
			return 1;
		}
		return o1.subTag().compareTo(o2.subTag());
	}

}