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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-engine-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.context;

final class EntityTagHelper {

	private static final String EMPTY = "";
	private static final String QUOTATION_MARK = "\"";
	private static final String WILDCARD_ETAG = "*";
	private static final String WEAK_PREFIX = "W/";

	private EntityTagHelper() {
	}
	
	static EntityTag fromString(String header) {
		if(header==null) {
			throw new IllegalArgumentException("Entity tag value cannot be null");
		}

		if(WILDCARD_ETAG.equals(header)) {
			return new EntityTag(WILDCARD_ETAG);
		}

		String tag = null;
		boolean weak = false;
		int i = header.indexOf(WEAK_PREFIX);
        if (i != -1) {
            weak = true;
            if (i + 2 < header.length()) {
                tag = header.substring(i + 2);
            } else {
                return new EntityTag(EMPTY, weak);
            }
        }  else {
            tag = header;
        }
        if (tag.length() > 0 && !tag.startsWith(QUOTATION_MARK) && !tag.endsWith(QUOTATION_MARK)) {
            return new EntityTag(tag, weak);
        }
        if (tag.length() < 2 || !tag.startsWith(QUOTATION_MARK) || !tag.endsWith(QUOTATION_MARK)) {
            throw new IllegalArgumentException("Misformatted entity tag : " + header);
        }
        tag = tag.length() == 2 ? EMPTY : tag.substring(1, tag.length() - 1); 
        return new EntityTag(tag, weak);
    }

	static String toString(EntityTag tag) {
		StringBuilder sb = new StringBuilder();
		if (tag.isWeak()) {
			sb.append(WEAK_PREFIX);
		}
		String tagValue = tag.getValue();
		if (!tagValue.startsWith(QUOTATION_MARK)) {
			sb.append(QUOTATION_MARK).append(tagValue).append(QUOTATION_MARK);
		} else {
			sb.append(tagValue);
		}
		return sb.toString();
	}

}
