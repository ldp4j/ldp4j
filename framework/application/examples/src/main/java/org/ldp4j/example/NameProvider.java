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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-examples-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ApplicationRuntimeException;

public final class NameProvider {

	private final class NameSource {

		private final Deque<Name<String>> pendingNames;
		private final String tag;

		private NameSource(String tag) {
			this.tag = tag;
			this.pendingNames=new LinkedList<Name<String>>();
		}

		private Name<String> nextName() {
			if(this.pendingNames.isEmpty()) {
				throw new ApplicationRuntimeException(String.format("No more %s names available for resource '%s'",tag,NameProvider.this.owner));
			}
			return this.pendingNames.pop();
		}

		private void addName(Name<String> name) {
			this.pendingNames.addLast(name);
		}

	}


	private final Name<String> owner;
	private final Map<String,NameSource> attachmentNameSources;
	private final NameSource resourceNamesSource;
	private final NameSource memberNamesSource;

	private NameProvider(Name<String> owner) {
		this.owner = owner;
		this.attachmentNameSources=new LinkedHashMap<String, NameSource>();
		this.resourceNamesSource=new NameSource("resource");
		this.memberNamesSource=new NameSource("member");
	}

	private NameSource nameSource(String attachmentId) {
		NameSource result = this.attachmentNameSources.get(attachmentId);
		if(result==null) {
			result=new NameSource("attachment <<"+attachmentId+">>");
			this.attachmentNameSources.put(attachmentId, result);
		}
		return result;
	}

	public Name<String> owner() {
		return this.owner;
	}

	public List<Name<String>> pendingAttachmentNames(String attachmentId) {
		List<Name<String>> result = new ArrayList<Name<String>>();
		NameSource source = this.attachmentNameSources.get(attachmentId);
		if(source!=null) {
			result.addAll(source.pendingNames);
		}
		return result;
	}

	public List<Name<String>> pendingResourceNames() {
		return new ArrayList<Name<String>>(this.resourceNamesSource.pendingNames);
	}

	public List<Name<String>> pendingMemberNames() {
		return new ArrayList<Name<String>>(this.memberNamesSource.pendingNames);
	}

	public void addResourceName(Name<String> nextName) {
		this.resourceNamesSource.addName(nextName);
	}

	public void addMemberName(Name<String> nextName) {
		this.memberNamesSource.addName(nextName);
	}

	public void addAttachmentName(String attachmentId, Name<String> nextName) {
		nameSource(attachmentId).addName(nextName);
	}

	public Name<String> nextResourceName() {
		return this.resourceNamesSource.nextName();
	}

	public Name<String> nextMemberName() {
		return this.memberNamesSource.nextName();
	}

	public Name<String> nextAttachmentName(String attachmentId) {
		NameSource result = this.attachmentNameSources.get(attachmentId);
		if(result==null) {
			result=new NameSource("attachment <<"+attachmentId+">>");
		}
		return result.nextName();
	}

	public static NameProvider create(Name<String> resource) {
		Objects.requireNonNull(resource,"Owner name cannot be null");
		return new NameProvider(resource);
	}

}