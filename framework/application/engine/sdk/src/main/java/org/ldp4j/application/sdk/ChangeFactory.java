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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-engine-sdk-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk;

import java.net.URI;
import java.util.Date;

import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.engine.context.Change;
import org.ldp4j.application.engine.context.Change.Action;
import org.ldp4j.application.engine.context.EntityTag;

import com.google.common.base.CaseFormat;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.*;

public final class ChangeFactory {

	private static final String ENTITY_TAG_CANNOT_BE_NULL         = "Entity tag cannot be null";
	private static final String LAST_MODIFIED_DATE_CANNOT_BE_NULL = "Last modified date cannot be null";
	private static final String RESOURCE_LOCATION_CANNOT_BE_NULL  = "Resource location cannot be null";
	private static final String TARGET_RESOURCE_CANNOT_BE_NULL    = "Target resource cannot be null";
 
	private static final class ImmutableGoneResourceChange implements Change {
		private final ManagedIndividualId targetResource;
		private final URI resourceLocation;

		private ImmutableGoneResourceChange(ManagedIndividualId targetResource,URI resourceLocation) {
			this.targetResource = targetResource;
			this.resourceLocation = resourceLocation;
		}

		@Override
		public Action action() {
			return Action.DELETED;
		}

		@Override
		public ManagedIndividualId targetResource() {
			return this.targetResource;
		}

		@Override
		public URI resourceLocation() {
			return this.resourceLocation;
		}

		@Override
		public Optional<Date> lastModified() {
			return Optional.absent();
		}

		@Override
		public Optional<EntityTag> entityTag() {
			return Optional.absent();
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(toTitle(Action.DELETED)).
						add("targetResource",this.targetResource).
						add("resourceLocation",this.resourceLocation).
						toString();
		}

	}

	private static final class ImmutableActiveResourceChange implements Change {

		private final EntityTag etag;
		private final ManagedIndividualId targetResource;
		private final Action action;
		private final URI resourceLocation;
		private final Date lastModified;

		private ImmutableActiveResourceChange(Action action, ManagedIndividualId targetResource,URI resourceLocation, Date lastModified, EntityTag etag) {
			this.action = action;
			this.targetResource = targetResource;
			this.resourceLocation = resourceLocation;
			this.lastModified = lastModified;
			this.etag = etag;
		}

		@Override
		public Action action() {
			return this.action;
		}

		@Override
		public ManagedIndividualId targetResource() {
			return this.targetResource;
		}

		@Override
		public URI resourceLocation() {
			return this.resourceLocation;
		}

		@Override
		public Optional<Date> lastModified() {
			return Optional.of(this.lastModified);
		}

		@Override
		public Optional<EntityTag> entityTag() {
			return Optional.of(this.etag);
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(toTitle(this.action)).
						add("targetResource",this.targetResource).
						add("resourceLocation",this.resourceLocation).
						add("lastModified",this.lastModified).
						add("entityTag",this.etag).
						toString();
		}

	}

	private ChangeFactory() {
	}

	private static String toTitle(Action action) {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,action.name());
	}

	public static Change createDeletion(final ManagedIndividualId targetResource, final URI resourceLocation) {
		checkNotNull(targetResource,TARGET_RESOURCE_CANNOT_BE_NULL);
		checkNotNull(resourceLocation,RESOURCE_LOCATION_CANNOT_BE_NULL);
		return new ImmutableGoneResourceChange(targetResource, resourceLocation);
	}

	public static Change createCreation(final ManagedIndividualId targetResource, final URI resourceLocation, final Date lastModified, final EntityTag etag) {
		checkNotNull(targetResource,TARGET_RESOURCE_CANNOT_BE_NULL);
		checkNotNull(resourceLocation,RESOURCE_LOCATION_CANNOT_BE_NULL);
		checkNotNull(lastModified,LAST_MODIFIED_DATE_CANNOT_BE_NULL);
		checkNotNull(etag,ENTITY_TAG_CANNOT_BE_NULL);
		return new ImmutableActiveResourceChange(Action.CREATED, targetResource, resourceLocation,lastModified, etag);
	}

	public static Change createModification(final ManagedIndividualId targetResource, final URI resourceLocation, final Date lastModified, final EntityTag etag) {
		checkNotNull(targetResource,TARGET_RESOURCE_CANNOT_BE_NULL);
		checkNotNull(resourceLocation,RESOURCE_LOCATION_CANNOT_BE_NULL);
		checkNotNull(lastModified,LAST_MODIFIED_DATE_CANNOT_BE_NULL);
		checkNotNull(etag,ENTITY_TAG_CANNOT_BE_NULL);
		return new ImmutableActiveResourceChange(Action.MODIFIED, targetResource, resourceLocation,lastModified, etag);
	}

}
