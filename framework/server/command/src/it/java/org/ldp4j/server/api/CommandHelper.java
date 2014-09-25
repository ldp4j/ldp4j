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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.api;

import java.util.Date;

import org.ldp4j.application.domain.LDP;
import org.ldp4j.server.commands.xml.CreateEndpoint;
import org.ldp4j.server.commands.xml.EndpointConfiguration.Capabilities;
import org.ldp4j.server.commands.xml.EntityType;
import org.ldp4j.server.commands.xml.FormatType;
import org.ldp4j.server.commands.xml.MembershipConfigurationType;
import org.ldp4j.server.commands.xml.MembershipRelationType;
import org.ldp4j.server.commands.xml.ResourceStateType;

public final class CommandHelper {
	
	private CommandHelper() {
	}
	
	public static CommandHelper.CreateEndpointCommandBuilder newCreateEndpointCommand(String path, String templateId) {
		return new CreateEndpointCommandBuilder(path,templateId);
	}
	
	public static final class CreateEndpointCommandBuilder {
		
		private final String path;
		private final String templateId;
		

		private FormatType format;
		private String content;
		private String etag;
		private Date lastModified;
		private Boolean modifiable;
		private Boolean deletable;
		private Boolean patchable;

		private String membershipIndirection;
		private String membershipTarget;
		private String membershipPredicate=LDP.MEMBER.qualifiedEntityName();
		private MembershipRelationType membershipRelation=MembershipRelationType.HAS_MEMBER;

		private boolean container=false;

		private CreateEndpointCommandBuilder(String path, String templateId) {
			this.path = path;
			this.templateId = templateId;
		}
		
		public CommandHelper.CreateEndpointCommandBuilder modifiable(Boolean modifiable) {
			this.modifiable = modifiable;
			return this;
		}

		public CommandHelper.CreateEndpointCommandBuilder deletable(Boolean deletable) {
			this.deletable = deletable;
			return this;
		}
		
		public CommandHelper.CreateEndpointCommandBuilder patchable(Boolean patchable) {
			this.patchable = patchable;
			return this;
		}

		public CommandHelper.CreateEndpointCommandBuilder withEntityTag(String etag) {
			this.etag = etag;
			return this;
		}

		public CommandHelper.CreateEndpointCommandBuilder withFormat(FormatType format) {
			this.format = format;
			return this;
		}
		
		public CommandHelper.CreateEndpointCommandBuilder withLastModified(Date date) {
			lastModified = date;
			return this;
		}
		
		public CommandHelper.CreateEndpointCommandBuilder withContent(String content) {
			this.content = content;
			return this;
		}
		
		public CommandHelper.CreateEndpointCommandBuilder withMembershipPredicate(String predicate) {
			if(predicate!=null) {
				container=true;
				this.membershipPredicate=predicate;
			}
			return this;
		}
		
		public CommandHelper.CreateEndpointCommandBuilder withWithMembershipRelation(MembershipRelationType relation) {
			if(relation!=null) {
				container=true;
				this.membershipRelation=relation;
			}
			return this;
		}

		public CommandHelper.CreateEndpointCommandBuilder withMembershipTarget(String target) {
			if(target!=null) {
				container=true;
				this.membershipTarget=target;
			}
			return this;
		}
		
		public CommandHelper.CreateEndpointCommandBuilder withInsertedContentRelation(String predicate) {
			if(predicate!=null) {
				container=true;
				this.membershipIndirection=predicate;
			}
			return this;
		}

		public CreateEndpoint build() {
			boolean hasEntity=false;
			
			EntityType entity = new EntityType();
			if(format!=null) {
				entity.withFormat(format);
			}
			if(content!=null) {
				hasEntity=true;
				entity.withValue(content);
			}

			ResourceStateType resourceState = new ResourceStateType();
			if(hasEntity) {
				resourceState.withEntity(entity);
			}
			if(etag!=null) {
				resourceState.withEtag(etag);
			}
			if(lastModified!=null) {
				resourceState.withLastModified(lastModified);
			}
			
			Capabilities capabilities=
				new Capabilities().
					withModifiable(modifiable).
					withDeletable(deletable).
					withPatchable(patchable);

			MembershipConfigurationType config=null;
			if(container) {
				config=
					new MembershipConfigurationType().
						withMembershipRelation(membershipRelation).
						withMembershipPredicate(membershipPredicate).
						withMembershipTarget(membershipTarget).
						withMembershipIndirection(membershipIndirection);
			}

			CreateEndpoint command=
					new CreateEndpoint().
						withPath(path).
						withTemplateId(templateId).
						withCapabilities(capabilities).
						withResourceState(resourceState).
						withMembershipConfiguration(config);

			return command;
		}
		
	}
	
}