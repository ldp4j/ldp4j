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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

public final class MoreHttp {

	/**
	 * The status code to signal that the content could not be understood by the
	 * application.
	 *
	 * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.2">RFC 4918:
	 *      HTTP Extensions for Web Distributed Authoring and Versioning
	 *      (WebDAV)</a>
	 */
	public static final int UNPROCESSABLE_ENTITY_STATUS_CODE = 422;
	public static final String ACCEPT_POST_HEADER = "Accept-Post";
	public static final String ENTITY_TAG_HEADER     = "ETag";
	public static final String LAST_MODIFIED_HEADER  = "Last-Modified";
	public static final String LINK_HEADER           = "Link";
	public static final String ALLOW_HEADER          = "Allow";
	public static final String CONTENT_LENGTH_HEADER = "Content-Length";
	public static final int PRECONDITION_REQUIRED_STATUS_CODE = 428;

	private MoreHttp(){
	}

	public static String createLink(Object uriRef, Object rel) {
		return String.format("<%s>; rel=\"%s\"",uriRef,rel);
	}

}
