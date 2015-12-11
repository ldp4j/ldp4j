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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-kernel-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.engine;

import java.util.List;

import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.engine.context.Change;
import org.ldp4j.application.kernel.resource.ResourceId;
import org.ldp4j.application.sdk.ChangeFactory;

import com.google.common.collect.Lists;

final class ChangeUtil {

	private ChangeUtil() {
	}

	private static Change<ManagedIndividualId> translateIdentifier(Change<ResourceId> change) {
		ResourceId resourceId = change.targetResource();
		ManagedIndividualId individualId=
			ManagedIndividualId.
				createId(
					resourceId.name(),
					resourceId.templateId());
		Change<ManagedIndividualId> newChange=null;
		switch(change.action()) {
			case CREATED:
				newChange=
					ChangeFactory.
						createCreation(
							individualId,
							change.resourceLocation(),
							change.lastModified().get(),
							change.entityTag().get());
				break;
			case MODIFIED:
				newChange=
					ChangeFactory.
						createModification(
							individualId,
							change.resourceLocation(),
							change.lastModified().get(),
							change.entityTag().get());
				break;
			case DELETED:
				newChange=
					ChangeFactory.
						createDeletion(
							individualId,
							change.resourceLocation());
				break;
			default:
				throw new IllegalStateException("Unsupported change action '"+change.action()+"'");
		}
		return newChange;
	}

	static <T> List<Change<ManagedIndividualId>> translateIdentifiers(Iterable<Change<ResourceId>> result) {
		List<Change<ManagedIndividualId>> changes = Lists.newArrayList();
		for(Change<ResourceId> change:result) {
			Change<ManagedIndividualId> newChange = translateIdentifier(change);
			changes.add(newChange);
		}
		return changes;
	}

}
