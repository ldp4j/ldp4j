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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import java.net.URI;
import java.net.URISyntaxException;

import org.ldp4j.commons.cbd.Precondition;
import org.ldp4j.commons.cbd.assertions.Assertions;

final class LinkUtil {

	private static final String LINK_HEADER_PREFIX = "<";

	private LinkUtil() {
	}
	
	private static String getSuffix(String relation) {
		return String.format(">; rel=\"%s\"",relation);
	}
	
	public static URI parseLinkRelation(String link, String relation) throws InvalidLinkException {
		Precondition.acceptArgument("link", link, Assertions.notNull());
		Precondition.acceptArgument("relation", relation, Assertions.notNull());
		String suffix = getSuffix(relation);
		String tLink = link.trim();
		if(!(tLink.startsWith(LINK_HEADER_PREFIX) && tLink.endsWith(suffix))) {
			throw new InvalidLinkException("Invalid link header format for relation '"+relation+"'",tLink);
		}
		try {
			String base=tLink.substring(LINK_HEADER_PREFIX.length(),tLink.length()-suffix.length());
			return new URI(base);
		} catch (URISyntaxException e) {
			throw new InvalidLinkException("Could not process link for relation '"+relation+"'",e,tLink);
		}
	}

	public static URI parseSelf(String link) throws InvalidLinkException {
		Precondition.acceptArgument("link", link, Assertions.notNull());
		return parseLinkRelation(link,"self");
	}

	public static String create(String link, String relation) {
		Precondition.acceptArgument("link", link, Assertions.notNull());
		Precondition.acceptArgument("relation", relation, Assertions.notNull());
		return String.format("<%s>; rel='%s'",link,relation);
	}
	
}
