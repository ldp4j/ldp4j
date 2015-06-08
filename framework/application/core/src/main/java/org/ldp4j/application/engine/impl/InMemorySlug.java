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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ldp4j.application.engine.resource.Container;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.engine.resource.Slug;

import com.google.common.base.Objects;

final class InMemorySlug implements Slug {

	private final ResourceId containerId;
	private final String preferredPath;
	private long version;

	private InMemorySlug(ResourceId containerId, String preferredPath, long version) {
		this.containerId = containerId;
		this.preferredPath = preferredPath;
		this.version=version;
	}

	@Override
	public ResourceId containerId() {
		return this.containerId;
	}

	@Override
	public String preferredPath() {
		return this.preferredPath;
	}

	@Override
	public synchronized long version() {
		return this.version;
	}

	synchronized void setVersion(long version) {
		this.version=version;
	}

	@Override
	public synchronized String nextPath() {
		long id=this.version++;
		String result = this.preferredPath;
		if(id>0) {
			result+="_"+id;
		}
		return result;
	}

	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
					add("containerId",this.containerId).
					add("preferredPath",this.preferredPath).
					add("version",version()).
					toString();
	}

	private static final Pattern SLUG_PATH_PATTERN=Pattern.compile("(^.*)(_(\\d+)?$)");

	static InMemorySlug create(String slugPath, Container container) {
		Matcher matcher = SLUG_PATH_PATTERN.matcher(slugPath);
		String path=slugPath;
		long version=0;
		if(matcher.matches()) {
			String strVersion = matcher.group(3);
			if(strVersion!=null) {
				String strPath = matcher.group(1);
				if(!strPath.equals("")) {
					path=strPath;
					version=Long.parseLong(strVersion);
				}
			}
		}
		return new InMemorySlug(container.id(),path,version);
	}
}