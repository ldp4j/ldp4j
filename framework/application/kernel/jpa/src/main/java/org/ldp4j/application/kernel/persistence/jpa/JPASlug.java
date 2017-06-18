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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-application-kernel-jpa-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.jpa;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.kernel.resource.Slug;

import com.google.common.base.MoreObjects;

final class JPASlug implements Slug {

	private static final Pattern SLUG_PATH_PATTERN=Pattern.compile("(^.*)(_(\\d+)?$)");

	/**
	 * Persistent key required by JPA
	 */
	private long primaryKey;

	/**
	 * Surrogate key to guarantee DB portability
	 */
	private Key containerId;

	/**
	 * Not final to enable its usage in JPA
	 */
	private String preferredPath;

	/**
	 * Not final to enable its usage in JPA
	 */
	private long version;

	private JPASlug() {
		// JPA friendly
	}

	private JPASlug(ResourceId containerId, String preferredPath, long version) {
		this();
		this.containerId = Key.newInstance(containerId);
		this.preferredPath = preferredPath;
		this.version=version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceId containerId() {
		return this.containerId.resourceId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String preferredPath() {
		return this.preferredPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized long version() {
		return this.version;
	}

	synchronized void setVersion(long version) {
		this.version=version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String nextPath() {
		long id=this.version++;
		String result = this.preferredPath;
		if(id>0) {
			result+="_"+id;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("primaryKey",this.primaryKey).
					add("containerId",this.containerId).
					add("preferredPath",this.preferredPath).
					add("version",version()).
					toString();
	}

	static JPASlug create(String slugPath, Container container) {
		Matcher matcher=SLUG_PATH_PATTERN.matcher(slugPath);
		String path=slugPath;
		long version=0;
		if(matcher.matches()) {
			String strVersion = matcher.group(3);
			if(strVersion!=null) {
				String strPath = matcher.group(1);
				if(!strPath.isEmpty()) {
					path=strPath;
					version=Long.parseLong(strVersion);
				}
			}
		}
		return new JPASlug(container.id(),path,version);
	}

}