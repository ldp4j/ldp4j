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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-examples-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;

public class InMemoryContainerHandler extends InMemoryResourceHandler implements ContainerHandler {

	public static class NameProvider {
		
		public class NameSource {
			
			private final Deque<Name<?>> pendingNames;
			private final List<Name<?>> consumedNames;
			private final String tag;
			
			private NameSource(String tag) {
				this.tag = tag;
				this.pendingNames=new LinkedList<Name<?>>();
				this.consumedNames=new ArrayList<Name<?>>();
			}
			
			public Name<?> nextName() {
				if(this.pendingNames.isEmpty()) {
					throw new IllegalStateException(String.format("No more '%s' names available for resource '%s'",tag,NameProvider.this.owner));
				}
				Name<?> result = this.pendingNames.pop();
				this.consumedNames.add(result);
				return result;
			}
			
			public void addName(Name<?> name) {
				this.pendingNames.addLast(name);
			}
			
			public boolean isServed(Name<?> name) {
				return this.consumedNames.contains(name);
			}
			
			public boolean isPending(Name<?> name) {
				return this.pendingNames.contains(name);
			}

		}
		

		private final Name<?> owner;
		private final Map<String,NameSource> attachmentNameSources;
		private final NameSource resourceNamesSource;
		private final NameSource memberNamesSource;

		private NameProvider(Name<?> owner) {
			this.owner = owner;
			this.attachmentNameSources=new LinkedHashMap<String, NameSource>();
			this.resourceNamesSource=new NameSource("resource");
			this.memberNamesSource=new NameSource("member");
		}
		
		public Name<?> owner() {
			return this.owner;
		}

		private NameSource nameSource(String attachmentId) {
			NameSource result = this.attachmentNameSources.get(attachmentId);
			if(result==null) {
				result=new NameSource("attachment <<"+attachmentId+">>");
				this.attachmentNameSources.put(attachmentId, result);
			}
			return result;
		}
		
		public List<Name<?>> pendingAttachmentNames(String attachmentId) {
			List<Name<?>> result = new ArrayList<Name<?>>();
			NameSource source = this.attachmentNameSources.get(attachmentId);
			if(source!=null) {
				result.addAll(source.pendingNames);
			}
			return result;
		}

		public List<Name<?>> pendingResourceNames(String attachmentId) {
			return new ArrayList<Name<?>>(this.resourceNamesSource.pendingNames);
		}

		public List<Name<?>> pendingMemberNames(String attachmentId) {
			return new ArrayList<Name<?>>(this.memberNamesSource.pendingNames);
		}
		
		public void addResourceName(Name<?> nextName) {
			this.resourceNamesSource.addName(nextName);
		}

		public void addMemberName(Name<?> nextName) {
			this.memberNamesSource.addName(nextName);
		}

		public void addAttachmentName(String attachmentId, Name<?> nextName) {
			nameSource(attachmentId).addName(nextName);
		}

		public Name<?> nextResourceName() {
			return this.resourceNamesSource.nextName();
		}

		public Name<?> nextMemberName() {
			return this.memberNamesSource.nextName();
		}
		
//		public static NameProvider create(Resource resource) {
//			return create(resource.id().name());
//		}
		
		public static NameProvider create(Name<?> resource) {
			return new NameProvider(resource);
		}

	}
	
	private final Map<Name<?>,NameProvider> nameProviders;

	protected InMemoryContainerHandler(String handlerName) {
		super(handlerName);
		this.nameProviders=new HashMap<Name<?>, NameProvider>();
	}
	
	public final void addNameProvider(Name<?> containerName, NameProvider provider) {
		this.nameProviders.put(containerName, provider);
	}
	
	public final NameProvider nameProvider(Name<?> containerName) {
		NameProvider result = this.nameProviders.get(containerName);
		if(result==null) {
			throw new IllegalStateException("Unknown container '"+containerName+"'");
		}
		return result;
	}

	@Override
	public ResourceSnapshot create(ContainerSnapshot container, DataSet representation, WriteSession session) {
		throw new UnsupportedOperationException("["+getHandlerName()+"] Method not implemented yet");
	}
	
}