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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.2
 *   Bundle      : ldp4j-application-examples-0.2.2.jar
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

/**
 * Utility class for managing the collections of names that can be used for
 * identifying the resources upon creation at runtime.
 */
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

	/**
	 * Return the name of the owner of the name provider.
	 *
	 * @return the name of the name provider.
	 */
	public Name<String> owner() {
		return this.owner;
	}

	/**
	 * Return the pending names for the specified attachment.
	 *
	 * @param attachmentId
	 *            the name of the attachment.
	 * @return the pending names for the specified attachment.
	 */
	public List<Name<String>> pendingAttachmentNames(String attachmentId) {
		List<Name<String>> result = new ArrayList<Name<String>>();
		NameSource source = this.attachmentNameSources.get(attachmentId);
		if(source!=null) {
			result.addAll(source.pendingNames);
		}
		return result;
	}

	/**
	 * Return the pending resource names.
	 *
	 * @return the pending resource names.
	 */
	public List<Name<String>> pendingResourceNames() {
		return new ArrayList<Name<String>>(this.resourceNamesSource.pendingNames);
	}

	/**
	 * Return the pending member names.
	 *
	 * @return the pending member names.
	 */
	public List<Name<String>> pendingMemberNames() {
		return new ArrayList<Name<String>>(this.memberNamesSource.pendingNames);
	}

	/**
	 * Add a resource name to the list of available resource names.
	 *
	 * @param nextName
	 *            the resource name.
	 */
	public void addResourceName(Name<String> nextName) {
		this.resourceNamesSource.addName(nextName);
	}

	/**
	 * Add a member name to the list of available member names.
	 *
	 * @param nextName
	 *            the member name.
	 */
	public void addMemberName(Name<String> nextName) {
		this.memberNamesSource.addName(nextName);
	}

	/**
	 * Add a name to the list of available names for a given attachment.
	 *
	 * @param attachmentId
	 *            the name of the attachment.
	 * @param nextName
	 *            the member name.
	 */
	public void addAttachmentName(String attachmentId, Name<String> nextName) {
		nameSource(attachmentId).addName(nextName);
	}

	/**
	 * Return the next available resource name. The name will be removed from
	 * the list of available resource names.
	 *
	 * @return the next available resource name.
	 */
	public Name<String> nextResourceName() {
		return this.resourceNamesSource.nextName();
	}

	/**
	 * Return the next available member name. The name will be removed from
	 * the list of available member names.
	 *
	 * @return the next available member name.
	 */
	public Name<String> nextMemberName() {
		return this.memberNamesSource.nextName();
	}

	/**
	 * Return the next available name for a given attachment. The name will be
	 * removed from the list of available names for the specified attachment.
	 *
	 * @param attachmentId
	 *            the name of the attachment.
	 * @return the next available name for the specified attachment.
	 */
	public Name<String> nextAttachmentName(String attachmentId) {
		NameSource result = this.attachmentNameSources.get(attachmentId);
		if(result==null) {
			result=new NameSource("attachment <<"+attachmentId+">>");
		}
		return result.nextName();
	}

	/**
	 * Create a new name provider.
	 *
	 * @param resource
	 *            the name of the owner of the name provider.
	 * @return a name provider.
	 */
	public static NameProvider create(Name<String> resource) {
		Objects.requireNonNull(resource,"Owner name cannot be null");
		return new NameProvider(resource);
	}

}