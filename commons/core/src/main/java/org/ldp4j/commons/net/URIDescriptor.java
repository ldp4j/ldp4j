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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons.net;

import java.net.URI;

final class URIDescriptor {

	private static final String NL=System.getProperty("line.separator");

	private final URI uri;
	private final String dir;
	private final String file;
	private final String query;
	private final String fragment;

	private URIDescriptor(
		URI uri,
		String dir,
		String file,
		String query,
		String fragment) {
		this.uri = uri;
		this.dir = dir;
		this.file = file;
		this.query = query;
		this.fragment = fragment;
	}

	public URI getUri() {
		return uri;
	}

	public String getDir() {
		return dir;
	}

	public String getFile() {
		return file;
	}

	public boolean isDir() {
		return this.file.isEmpty();
	}

	public String getQuery() {
		return query;
	}

	public String getFragment() {
		return fragment;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("URIDescriptor(").append(this.uri).append(") {").append(NL);
		if(!this.dir.isEmpty()) {
			builder.append("\t-").append("Directory").append(": ").append(this.dir).append(NL);
		}
		if(!this.file.isEmpty()) {
			builder.append("\t-").append("File").append(": ").append(this.file).append(NL);
		}
		if(this.query!=null) {
			builder.append("\t-").append("Query").append(": ").append(this.query).append(NL);
		}
		if(this.fragment!=null) {
			builder.append("\t-").append("Fragment").append(": ").append(this.fragment).append(NL);
		}
		builder.append("}");
		return builder.toString();
	}

	public static URIDescriptor create(URI target) {
		if(target==null) {
			throw new NullPointerException("URI cannot be null");
		}
		if(target.isOpaque()) {
			throw new IllegalArgumentException("URI must be hierarchical");
		}
		URI targetDir = target.resolve(".");
		String targetFile=target.getPath().substring(targetDir.getPath().length());
		String targetQuery = target.getQuery();
		String targetFragment=target.getFragment();
		return new URIDescriptor(target,targetDir.getPath(),targetFile,targetQuery,targetFragment);
	}

}