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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.ldp4j.application.persistence.domain.Application;
import org.ldp4j.application.persistence.domain.Endpoint;
import org.ldp4j.application.persistence.domain.Failure;
import org.ldp4j.application.persistence.domain.Resource;
import org.ldp4j.application.persistence.domain.Template;
import org.ldp4j.application.persistence.domain.TemplateAttachment;

import com.google.common.collect.Lists;

public final class ApplicationManager extends BaseManager {

	ApplicationManager(EntityManager manager, Date timestamp) {
		super(manager,timestamp);
	}

	private void deleteEndpoints(Application application, EntityManager ctxManager) {
		List<Endpoint> endpoints =
			ctxManager.
				createNamedQuery(Endpoint.FIND_ENDPOINTS_BY_APPLICATION,Endpoint.class).
					setParameter(Endpoint.APPLICATION, application).
					getResultList();
		for(Endpoint endpoint:endpoints) {
			ctxManager.remove(endpoint);
		}
	}

	// TODO: Add proper exception handling
	public Application createApplication(String path, String name, String description) {
		Application application=new Application();
		application.setPath(path);
		application.setName(name);
		application.setDescription(description);
		getManager().persist(application);
		return application;
	}

	// TODO: Add proper exception handling
	public void deleteApplication(Application application) {
		List<Resource> rootResources=Lists.newArrayList();
		List<TemplateAttachment> templateAttachments=Lists.newArrayList();
		for(Template template:application.getTemplates()) {
			for(Resource resource:template.getResources()) {
				if(resource.isRoot()) {
					rootResources.add(resource);
				}
			}
			templateAttachments.addAll(template.getTemplateAttachments());
		}
		EntityManager ctxManager = getManager();
		for(Resource root:rootResources) {
			ResourceManager.deleteResource(root, getManager(), getTimestamp());
		}
		deleteEndpoints(application, ctxManager);
		for(TemplateAttachment templateAttachment:templateAttachments) {
			ctxManager.remove(templateAttachment);
		}
		for(Template template:application.getTemplates()) {
			ctxManager.remove(template);
		}
		ctxManager.remove(application);
	}

	// TODO: Add proper exception handling
	public void deleteApplicationResources(Application application) {
		List<Resource> rootResources=Lists.newArrayList();
		EntityManager ctxManager = getManager();
		for(Template template:application.getTemplates()) {
			for(Resource resource:template.getResources()) {
				if(resource.isRoot()) {
					rootResources.add(resource);
				}
			}
		}
		for(Resource resource:rootResources) {
			ResourceManager.deleteResource(resource,ctxManager,getTimestamp());
		}
		deleteEndpoints(application, ctxManager);
	}

	// TODO: Add proper exception handling
	public int deleteApplicationFailures(Application application) {
		return
			getManager().
				createNamedQuery(Failure.DELETE_ALL_APPLICATION_FAILURES).
					setParameter(Failure.APPLICATION,application).
					executeUpdate();
	}

	// TODO: Add proper exception handling
	public int deleteApplicationFailures(Application application, long ammount, TimeUnit unit) {
		long millis = unit.toMillis(ammount);
		Date ocurredOn=new Date(getTimestamp().getTime()-millis);
		return
			getManager().
				createNamedQuery(Failure.DELETE_APPLICATION_FAILURES_BY_DATE).
					setParameter(Failure.APPLICATION,application).
					setParameter(Failure.OCCURRED_ON, ocurredOn).
					executeUpdate();
	}

	// TODO: Add proper exception handling
	public Application findApplication(String path) {
		return getManager().find(Application.class,path);
	}

	// TODO: Add proper exception handling
	public <T extends Resource> T findResourceByPath(Application application, String path, Class<? extends T> clazz) {
		List<? extends Resource> resources =
			getManager().
				createNamedQuery(Resource.FIND_APPLICATION_RESOURCE_BY_PATH,Resource.class).
					setParameter(Resource.TEMPLATE, application).
					setParameter(Resource.PATH, path).
					getResultList();
		if(resources.isEmpty()) {
			return null;
		}
		return clazz.cast(resources.get(0));
	}

	// TODO: Add proper exception handling
	public <T extends Template> T findTemplateByHandler(Application application,String handlerClassName, Class<? extends T> clazz) {
		List<? extends Template> templates =
			getManager().
				createNamedQuery(Template.FIND_APPLICATION_TEMPLATE_BY_HANDLER,Template.class).
					setParameter(Template.APPLICATION, application).
					setParameter(Template.HANDLER_CLASS_NAME,handlerClassName).
					getResultList();
		if(templates.isEmpty()) {
			return null;
		}
		return clazz.cast(templates.get(0));
	}

	// TODO: Add proper exception handling
	public <T extends Resource> T findResourceByBusinessKey(Template template, Serializable businessKey, Class<? extends T> clazz) {
		List<? extends Resource> resources =
			getManager().
				createNamedQuery(Resource.FIND_TEMPLATE_RESOURCE_BY_BUSINESS_KEY,Resource.class).
					setParameter(Resource.TEMPLATE, template).
					setParameter(Resource.BUSINESS_KEY, businessKey).
					getResultList();
		if(resources.isEmpty()) {
			return null;
		}
		return clazz.cast(resources.get(0));
	}

	public TemplateManager getTemplateManager(Application application) {
		return new TemplateManager(getManager(),getTimestamp(),application);
	}


}