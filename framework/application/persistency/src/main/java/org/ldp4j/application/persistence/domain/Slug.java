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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-persistency:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-persistency-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.persistence.domain;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.google.common.base.MoreObjects;

@Entity
@IdClass(SlugId.class)
public class Slug {

	private String path;
	private Container container;

	private AtomicLong version;

	public Slug() {
		this.version=new AtomicLong();
	}

	@Id
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Id
	@ManyToOne
	@JoinColumn(name="resource_id")
	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public long getVersion() {
		return version.get();
	}

	public void setVersion(long version) {
		this.version.set(version);
	}

	public SlugId id() {
		return SlugId.create(this.path, this.container);
	}

	public String nextSlugPath() {
		long id=this.version.getAndIncrement();
		String result = this.path;
		if(id>0) {
			result+="_"+id;
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("container",DomainHelper.identifyEntity(this.container)).
					add("path",this.path).
					add("version",this.version.get()).
					toString();
	}

	private static final Pattern SLUG_PATH_PATTERN=Pattern.compile("(^.*)(_(\\d+)?$)");

	public static Slug create(String slugPath, Container container) {
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
		Slug slug=new Slug();
		slug.setPath(path);
		slug.setContainer(container);
		slug.setVersion(version);
		return slug;
	}

}
