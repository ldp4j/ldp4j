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

final class AlternativeComparator implements Comparator<Alternative> {

	static final AlternativeComparator INSTANCE=new AlternativeComparator();

	private AlternativeComparator() {
	}

	@Override
	public int compare(Alternative o1, Alternative o2) {
		int comparison=Double.compare(o1.quality(),o2.quality());
		if(comparison==0) {
			comparison=compareAttribute(o1.type(),o2.type(),MediaTypeComparator.INSTANCE);
			if(comparison==0) {
				comparison=compareAttribute(o1.charset(),o2.charset(),CharacterEncodingComparator.INSTANCE);
				if(comparison==0) {
					comparison=compareAttribute(o1.language(),o2.language(),LanguageComparator.INSTANCE);
				}
			}
		}
		return comparison;
	}

	private <T> int compareAttribute(T t1, T t2, Comparator<T> comparator) {
		if(t1==null && t2!=null) {
			return -1;
		}
		if(t1!=null && t2==null) {
			return 1;
		}
		if(t1==null) { // t2 must be also equal to null
			return 0;
		}
		return comparator.compare(t1,t2);
	}
}