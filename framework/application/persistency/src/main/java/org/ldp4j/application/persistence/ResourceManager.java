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
package org.ldp4j.application.persistence;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.EntityManager;

import org.ldp4j.application.persistence.domain.Application;
import org.ldp4j.application.persistence.domain.BasicContainerTemplate;
import org.ldp4j.application.persistence.domain.Container;
import org.ldp4j.application.persistence.domain.DirectContainerTemplate;
import org.ldp4j.application.persistence.domain.Endpoint;
import org.ldp4j.application.persistence.domain.Failure;
import org.ldp4j.application.persistence.domain.IndirectContainerTemplate;
import org.ldp4j.application.persistence.domain.MembershipAwareContainer;
import org.ldp4j.application.persistence.domain.MembershipAwareContainerTemplate;
import org.ldp4j.application.persistence.domain.RDFSource;
import org.ldp4j.application.persistence.domain.RDFSourceTemplate;
import org.ldp4j.application.persistence.domain.Resource;
import org.ldp4j.application.persistence.domain.ResourceAttachment;
import org.ldp4j.application.persistence.domain.ResourceVisitor;
import org.ldp4j.application.persistence.domain.Slug;
import org.ldp4j.application.persistence.domain.Template;
import org.ldp4j.application.persistence.domain.TemplateAttachment;
import org.ldp4j.application.persistence.domain.TemplateVisitor;

public final class ResourceManager extends BaseManager {

	private abstract class CommonResourceBuilder<T extends CommonResourceBuilder<T>> {

		protected Serializable businessKey;
		protected Resource targetResource;
		private String entityTag;

		private Resource createMembershipAwareResource() {
			checkState(this.targetResource!=null,"No target resource specified for membership aware container");
			checkState(
				this.targetResource.getDefinedBy().getApplication().equals(getTemplate().getApplication()),
				"Membership aware container cannot have a target resource of a different application from that of the specified template");
			MembershipAwareContainer resource=new MembershipAwareContainer();
			resource.setTargetResource(this.targetResource);
			return resource;
		}

		protected abstract T getBuilder();

		protected abstract Template getTemplate();

		protected final Resource createEmptyResource() {
			checkState(this.businessKey!=null,"No business key defined");
			final AtomicReference<Resource> result=new AtomicReference<Resource>();
			getTemplate().accept(
				new TemplateVisitor() {
					@Override
					public void visitRDFResourceTemplate(RDFSourceTemplate template) {
						result.set(new RDFSource());
					}
					@Override
					public void visitBasicContainerTemplate(BasicContainerTemplate template) {
						result.set(new Container());
					}
					@Override
					public void visitIndirectContainerTemplate(IndirectContainerTemplate template) {
						result.set(createMembershipAwareResource());
					}
					@Override
					public void visitDirectContainerTemplate(DirectContainerTemplate template) {
						result.set(createMembershipAwareResource());
					}
				}
			);
			Resource resource = result.get();
			resource.setBusinessKey(this.businessKey);
			resource.setDefinedBy(getTemplate());
			return resource;
		}

		protected final Endpoint createEndpoint(String path) {
			checkNotNull(path,"Endpoint path cannot be null");
			checkState(this.entityTag!=null,"No entity tag defined");
			Date timestamp = getTimestamp();
			Endpoint endpoint=new Endpoint();
			endpoint.setApplication(getTemplate().getApplication());
			endpoint.setCreated(timestamp);
			endpoint.setModified(timestamp);
			endpoint.setEntityTag(this.entityTag);
			endpoint.setPath(path);
			return endpoint;
		}

		protected final void persistResource(Resource resource) {
			Endpoint endpoint=createEndpoint(getPath());
			Application application = endpoint.getApplication();
			resource.setEndpoint(endpoint);
			endpoint.setResource(resource);
			getTemplate().getResources().add(resource);
			application.getEndpoints().add(endpoint);
			getManager().persist(endpoint);
			getManager().persist(resource);
			persistResourceAttachments(resource);
		}

		protected abstract String getPath();

		protected final void persistResourceAttachments(Resource resource) {
			for(TemplateAttachment attachment:getTemplate().getTemplateAttachments()) {
				ResourceAttachment resAttachment=new ResourceAttachment();
				resAttachment.setTemplateAttachment(attachment);
				resAttachment.setOwnerResource(resource);
				resource.getResourceAttachments().add(resAttachment);
				getManager().persist(resAttachment);
			}
		}

		public T withBusinessKey(Serializable businessKey) {
			this.businessKey=checkNotNull(businessKey,"Business key cannot be null");
			return getBuilder();
		}

		public T withEntityTag(String entityTag) {
			this.entityTag=checkNotNull(entityTag,"Entity tag cannot be null");
			return getBuilder();
		}


		/**
		 * This method must be used for building membership aware containers
		 * @param targetResource
		 * @return
		 */
		public T withTargetResource(Resource targetResource) {
			this.targetResource=targetResource;
			return getBuilder();
		}
	}

	private class ResourceBuilder {

		final class RootResourceBuilder extends CommonResourceBuilder<RootResourceBuilder> {

			private final Template template;
			private String path;

			private RootResourceBuilder(Template template) {
				this.template = template;
			}

			/**
			 * This method is to build root resources
			 * @param path
			 * @return
			 */
			public RootResourceBuilder withEndpointPath(String path) {
				this.path = checkNotNull(path,"Path cannot be null");
				return this;
			}

			public Resource createResource() {
				Resource resource=createEmptyResource();
				persistResource(resource);
				return resource;
			}

			@Override
			protected String getPath() {
				return this.path;
			}

			@Override
			protected Template getTemplate() {
				return this.template;
			}

			@Override
			protected RootResourceBuilder getBuilder() {
				return this;
			}

		}

		final class MemberResourceBuilder extends CommonResourceBuilder<MemberResourceBuilder> {

			private Container container;

			private URI indirectId;
			private String slugPath;

			private Slug slug;

			private MemberResourceBuilder(Container container) {
				this.container = container;
			}

			private Slug getSlug() {
				if(this.slug!=null) {
					this.slug=processSlugPath(this.slugPath);
				}
				return this.slug;
			}

			private Slug processSlugPath(String slugPath) {
				if(slugPath==null) {
					return null;
				}

				Slug tmp=Slug.create(this.slugPath,this.container);

				Slug slug=getManager().find(Slug.class,tmp.id());
				if(slug==null) {
					return tmp;
				}

				synchronized(slug) {
					if(slug.getVersion()<tmp.getVersion()) {
						slug.setVersion(tmp.getVersion());
						return slug;
					}
				}

				return processSlugPath(slugPath+"_1");
			}

			private void persistSlug(Slug slug) {
				if(slug!=null && !getManager().contains(slug)) {
					getManager().persist(slug);
					this.container.getSlugs().add(slug);
				}
			}

			/**
			 * This method should be used to build members of indirect containers.
			 * In particular to populate the indirect id of the member once the
			 * parent container has been specified.
			 *
			 * @param indirectId
			 * @return
			 */
			public MemberResourceBuilder withIndirectId(URI indirectId) {
				this.indirectId = indirectId;
				return this;
			}

			/**
			 * This method should be used to build members of containers. In
			 * particular to populate the slug to use when creating the endpoint for
			 * the member once the parent container has been specified.
			 *
			 * @param indirectId
			 * @return
			 */
			public MemberResourceBuilder withSlug(String slug) {
				this.slugPath = slug;
				return this;
			}

			public Resource createResource() {
				checkState(!(this.container.getDefinedBy() instanceof IndirectContainerTemplate) || this.indirectId!=null,"No indirect ID specified for a member of an indirect container");
				Resource resource=createEmptyResource();
				resource.setIndirectId(this.indirectId);
				resource.setContainer(this.container);;
				this.container.getMembers().add(resource);
				persistResource(resource);
				persistSlug(getSlug());
				return resource;
			}

			@Override
			protected String getPath() {
				return this.container.nextMemberPath(getSlug());
			}

			@Override
			protected MemberResourceBuilder getBuilder() {
				return this;
			}

			@Override
			protected Template getTemplate() {
				return this.container.getDefinedBy().getMemberTemplate();
			}

		}

		final class AttachedResourceBuilder extends CommonResourceBuilder<AttachedResourceBuilder> {

			private final Resource ownerResource;
			private ResourceAttachment resourceAttachment;

			private AttachedResourceBuilder(Resource ownerResource) {
				this.ownerResource = ownerResource;
			}

			public Resource createResource() {
				checkState(this.resourceAttachment!=null,"No attachment specified");
				Resource resource=createEmptyResource();
				persistResource(resource);
				resource.setAttachement(this.resourceAttachment);
				this.resourceAttachment.setAttachedResource(resource);
				return resource;
			}

			protected String getPath() {
				return this.resourceAttachment.nextAttachmentPath();
			}

			public AttachedResourceBuilder withAttachmentName(String name) {
				checkNotNull(name,"Attachment name cannot be null");
				for(ResourceAttachment resAttachment : ownerResource.getResourceAttachments()) {
					if(resAttachment.getTemplateAttachment().getName().equals(name)) {
						this.resourceAttachment=resAttachment;
						if(this.resourceAttachment.getTemplateAttachment().getAttachedTemplate() instanceof MembershipAwareContainerTemplate) {
							this.targetResource=this.ownerResource;
						} else {
							this.targetResource=null;
						}
						return this;
					}
				}
				throw new IllegalArgumentException("Unknown attachment '"+name+"'");

			}

			@Override
			protected Template getTemplate() {
				return this.resourceAttachment.getTemplateAttachment().getAttachedTemplate();
			}

			@Override
			protected AttachedResourceBuilder getBuilder() {
				return this;
			}

		}

		private ResourceBuilder() {
		}

		public RootResourceBuilder withTemplate(Template template) {
			checkNotNull(template,"Template cannot be null");
			return new RootResourceBuilder(template);
		}

		/**
		 * This method should be used to build members of containers.
		 * @param container
		 * @return
		 */
		public MemberResourceBuilder memberOf(Container container) {
			checkNotNull(container,"Container cannot be null");
			return new MemberResourceBuilder(container);
		}

		/**
		 * This method should be used for creating
		 * @param ownerResource
		 * @param path
		 * @return
		 */
		public AttachedResourceBuilder attachedTo(Resource ownerResource) {
			checkNotNull(ownerResource,"Owner resource cannot be null");
			return new AttachedResourceBuilder(ownerResource);
		}

	}

	private static final class DeletionContext {

		private final Deque<ResourceAttachment> attachments;
		private final Deque<Resource> pendingResources;
		private final Deque<Resource> completedResources;
		private final EntityManager ctxManager;
		private final Date timestamp;

		private DeletionContext(Resource resource, EntityManager ctxManager, Date timestamp) {
			this.ctxManager = ctxManager;
			this.timestamp = timestamp;
			this.attachments=new LinkedList<ResourceAttachment>();
			this.pendingResources=new LinkedList<Resource>();
			this.completedResources=new LinkedList<Resource>();
			this.pendingResources.add(resource);
		}

		public void addAttachment(ResourceAttachment attachment) {
			this.attachments.add(attachment);
			Resource attachedResource = attachment.getAttachedResource();
			if(attachedResource!=null) {
				this.pendingResources.add(attachedResource);
			}
		}

		public void addMembers(List<Resource> members) {
			this.pendingResources.addAll(members);
		}

		public boolean hasNextResource() {
			return !this.pendingResources.isEmpty();
		}

		public Resource nextResource() {
			Resource nextResource = this.pendingResources.pop();
			if(nextResource!=null) {
				this.completedResources.add(nextResource);
			}
			return nextResource;
		}

		public void deleteResources() {
			for(ResourceAttachment attachment:this.attachments) {
				removeEntity(attachment);
			}
			while(!this.completedResources.isEmpty()) {
				Resource resource=this.completedResources.pop();
				Endpoint endpoint = resource.getEndpoint();
				endpoint.setDeleted(this.timestamp);
				endpoint.setResource(null);
				if(resource instanceof Container) {
					removeSlugs((Container)resource);
				}
				// Update template resource list
				resource.getDefinedBy().getResources().remove(resource);
				removeEntity(resource);
			}
		}

		private void removeSlugs(Container resource) {
			for(Slug slug:resource.getSlugs()) {
				removeEntity(slug);
			}
		}

		private  void removeEntity(Object entity) {
			this.ctxManager.remove(entity);
		}

	}

	ResourceManager(EntityManager entityManager, Date timestamp) {
		super(entityManager, timestamp);
	}

	// TODO: Add proper exception handling
	public Resource createRootResource(Template template, String endpoint, Serializable businessKey, String entityTag, Resource targetResource) {
		return
			new ResourceBuilder().
					withTemplate(template).
						withBusinessKey(businessKey).
						withEntityTag(entityTag).
						withEndpointPath(endpoint).
						withTargetResource(targetResource).
						createResource();
	}

	// TODO: Add proper exception handling
	public Resource createAttachedResource(Resource ownerResource, String attachmentName, Serializable businessKey, String entityTag) {
		return
			new ResourceBuilder().
				attachedTo(ownerResource).
					withBusinessKey(businessKey).
					withEntityTag(entityTag).
					withAttachmentName(attachmentName).
					withTargetResource(ownerResource).
					createResource();
	}

	// TODO: Add proper exception handling
	public Resource createMemberResource(Container ownerResource, Serializable businessKey, String entityTag, URI indirectId, String slug) {
		return
			new ResourceBuilder().
				memberOf(ownerResource).
					withBusinessKey(businessKey).
					withEntityTag(entityTag).
					withIndirectId(indirectId).
					withSlug(slug).
					withTargetResource(ownerResource).
					createResource();
	}

	// TODO: Add proper exception handling
	public void touchResource(Resource resource, String entityTag) {
		Endpoint endpoint = checkNotNull(resource.getEndpoint(),"Resource is deleted");
		endpoint.setModified(getTimestamp());
		endpoint.setEntityTag(entityTag);
	}

	// TODO: Add proper exception handling
	public void deleteResource(Resource resource) {
		ResourceManager.deleteResource(resource, getManager(), getTimestamp());
	}

	// TODO: Add proper exception handling
	public Failure createFailure(Resource resource, String request, String message) {
		Endpoint endpoint = resource.getEndpoint();
		Failure failure=new Failure();
		failure.setEndpoint(endpoint);
		failure.setRequest(request);
		failure.setMessage(message);
		failure.setOccurredOn(getTimestamp());
		endpoint.getFailures().add(failure);
		getManager().persist(failure);
		return failure;
	}

	static final void deleteResource(Resource resource, EntityManager ctxManager, Date timestamp) {
		final DeletionContext context=new DeletionContext(resource, ctxManager,timestamp);
		while(context.hasNextResource()) {
			Resource next=context.nextResource();
			next.accept(
				new ResourceVisitor() {
					@Override
					public void visitRDFSource(RDFSource resource) {
						for(ResourceAttachment attachment:resource.getResourceAttachments()) {
							context.addAttachment(attachment);
						}
					}
					@Override
					public void visitContainer(Container resource) {
						visitRDFSource(resource);
						context.addMembers(resource.getMembers());
					}
					@Override
					public void visitMembershipAwareContainer(MembershipAwareContainer resource) {
						visitContainer(resource);
					}

				}
			);
		}
		context.deleteResources();
		if(resource.isRoot()) {
			return ;
		}
		if(resource.isAttached()) {
			resource.getAttachement().setAttachedResource(null);
		} else {
			resource.getContainer().getMembers().remove(resource);
		}
	}

}
