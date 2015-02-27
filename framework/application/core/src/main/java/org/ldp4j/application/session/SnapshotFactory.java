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
package org.ldp4j.application.session;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.resource.Container;
import org.ldp4j.application.resource.Resource;
import org.ldp4j.application.resource.ResourceId;
import org.ldp4j.application.resource.ResourceVisitor;
import org.ldp4j.application.spi.PersistencyManager;
import org.ldp4j.application.template.ResourceTemplate;
import org.ldp4j.application.template.TemplateIntrospector;

final class SnapshotFactory {

	private final DelegatedWriteSession session;
	private final PersistencyManager persistencyManager;

	private SnapshotFactory(PersistencyManager persistencyManager, DelegatedWriteSession session) {
		this.persistencyManager = persistencyManager;
		this.session = session;
	}

	private ResourceTemplate getTemplate(ResourceId resourceId) {
		checkNotNull(resourceId,"Resource identifier cannot be null");
		ResourceTemplate template = this.persistencyManager.templateOfId(resourceId.templateId());
		checkArgument(template!=null,"Could not find template for resource '%s'",resourceId);
		return template;
	}

	private DelegatedResourceSnapshot instantiateTemplate(ResourceId resourceId, ResourceTemplate template) {
		DelegatedResourceSnapshot snapshot=null;
		TemplateIntrospector introspector=TemplateIntrospector.newInstance(template);
		if(!introspector.isContainer()) {
			snapshot=new DelegatedResourceSnapshot(resourceId);
		} else {
			snapshot=new DelegatedContainerSnapshot(resourceId);
		}
		return snapshot;
	}

	DelegatedResourceSnapshot newPersistent(Resource resource, final ResourceTemplate template) {
		final AtomicReference<DelegatedResourceSnapshot> result=new AtomicReference<DelegatedResourceSnapshot>();
		resource.accept(
			new ResourceVisitor() {
				@Override
				public void visitResource(Resource resource) {
					DelegatedResourceSnapshot snapshot=new DelegatedResourceSnapshot(resource.id());
					snapshot.setParentState(ParentState.parentOf(resource));
					snapshot.setPersistencyState(PersistencyState.newPersistent(resource,template,session));
					result.set(snapshot);
				}
				@Override
				public void visitContainer(Container resource) {
					DelegatedContainerSnapshot snapshot=new DelegatedContainerSnapshot(resource.id());
					snapshot.setParentState(ParentState.parentOf(resource));
					snapshot.setPersistencyState(PersistencyState.newPersistent(resource,template,session));
					result.set(snapshot);
				}
			}
		);
		return result.get();
	}

	DelegatedResourceSnapshot newTransient(ResourceId resourceId, DelegatedResourceSnapshot parent) {
		ResourceTemplate template = getTemplate(resourceId);
		DelegatedResourceSnapshot snapshot = instantiateTemplate(resourceId,template);
		snapshot.setPersistencyState(PersistencyState.newTransientState(resourceId, template));
		if(parent==null) {
			snapshot.setParentState(ParentState.orphan());
		} else {
			snapshot.setParentState(ParentState.childOf(parent));
		}
		return snapshot;
	}

	<T extends DelegatedResourceSnapshot> T newTransient(Class<? extends T> snapshotClass, ResourceId resourceId) {
		return newTransient(snapshotClass,resourceId,null);
	}

	<T extends DelegatedResourceSnapshot> T newTransient(Class<? extends T> snapshotClass, ResourceId resourceId, DelegatedResourceSnapshot parent) {
		checkNotNull(snapshotClass,"Snapshot class cannot be null");
		DelegatedResourceSnapshot tmp=newTransient(resourceId,parent);
		checkState(!snapshotClass.isInstance(tmp),"Cannot create snapshot of type '"+snapshotClass.getCanonicalName()+"' from template '"+resourceId.templateId()+"'");
		return snapshotClass.cast(tmp);
	}

	static SnapshotFactory newInstance(PersistencyManager persistencyManager, DelegatedWriteSession session) {
		return new SnapshotFactory(persistencyManager,session);
	}


}