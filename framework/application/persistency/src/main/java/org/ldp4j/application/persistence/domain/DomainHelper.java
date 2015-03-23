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

import java.util.List;

import com.google.common.collect.Lists;

final class DomainHelper {

	private DomainHelper() {
	}

	static String identifyEntity(Object entity) {
		if (entity == null) {
			return null;
		}
		return formatEntityIdentifier(entity);
	}

	static List<String> identifyEntities(List<?> entity) {
		if(entity == null || entity.isEmpty()) {
			return null;
		}
		List<String> ids = Lists.newArrayList();
		for (Object ent : entity) {
			if (ent != null) {
				ids.add(formatEntityIdentifier(ent));
			}
		}
		return ids;
	}

	static Object keyFrom(Object entity) {
		if (entity == null) {
			return null;
		}
		return keyFromEntity(entity);
	}

	static List<Object> keysFrom(List<?> entities) {
		if (entities == null || entities.isEmpty()) {
			return null;
		}
		List<Object> keys = Lists.newArrayList();
		fillInKeys(keys, entities, entities.get(0).getClass());
		return keys;
	}

	private static String formatEntityIdentifier(Object entity) {
		return String.format("%s(%s)", simpleName(entity.getClass()),keyFromEntity(entity));
	}

	private static Object keyFromEntity(Object entity) {
		if (Template.class.isInstance(entity)) {
			return ((Template) entity).getId();
		} else if (Resource.class.isInstance(entity)) {
			return ((Resource) entity).getId();
		} else if (Endpoint.class.isInstance(entity)) {
			return ((Endpoint) entity).getId();
		} else if (Failure.class.isInstance(entity)) {
			return ((Failure) entity).getId();
		} else if (Application.class.isInstance(entity)) {
			return ((Application) entity).getPath();
		} else if (TemplateAttachment.class.isInstance(entity)) {
			return ((TemplateAttachment) entity).getId();
		} else if (ResourceAttachment.class.isInstance(entity)) {
			return ((ResourceAttachment) entity).getId();
		} else if (Slug.class.isInstance(entity)) {
			return createSlugId((Slug) entity);
		} else {
			throw new UnsupportedOperationException(
					"Cannot get key from entity type '"
							+ entity.getClass().getCanonicalName() + "'");
		}
	}

	private static SlugId createSlugId(Slug slug) {
		SlugId id=new SlugId();
		id.setPath(slug.getPath());
		id.setContainer(slug.getContainer().getId());
		return id;
	}

	@SuppressWarnings("unchecked")
	private static void fillInKeys(List<Object> keys, List<?> entities, Class<?> clazz) {
		if (Template.class.isAssignableFrom(clazz)) {
			fillInTemplateKeys(keys, (List<Template>) entities);
		} else if (Resource.class.isAssignableFrom(clazz)) {
			fillInResourceKeys(keys, (List<Resource>) entities);
		} else if (Endpoint.class.isAssignableFrom(clazz)) {
			fillInEndpointKeys(keys, (List<Endpoint>) entities);
		} else if (Failure.class.isAssignableFrom(clazz)) {
			fillInFailureKeys(keys, (List<Failure>) entities);
		} else if (Application.class.isAssignableFrom(clazz)) {
			fillInApplicationKeys(keys, (List<Application>) entities);
		} else if (TemplateAttachment.class.isAssignableFrom(clazz)) {
			fillInTemplateAttachmentKeys(keys,(List<TemplateAttachment>) entities);
		} else if (ResourceAttachment.class.isAssignableFrom(clazz)) {
			fillInResourceAttachmentKeys(keys,(List<ResourceAttachment>) entities);
		} else if (Slug.class.isAssignableFrom(clazz)) {
			fillInSlugKeys(keys,(List<Slug>) entities);
		} else {
			throw new UnsupportedOperationException(
					"Cannot fill in keys from entity type '"
							+ clazz.getCanonicalName() + "'");
		}
	}

	private static void fillInSlugKeys(List<Object> keys, List<Slug> entities) {
		for (Slug entity : entities) {
			keys.add(createSlugId(entity));
		}
	}

	private static void fillInResourceAttachmentKeys(List<Object> keys, List<ResourceAttachment> entities) {
		for (ResourceAttachment entity : entities) {
			keys.add(entity.getId());
		}
	}

	private static void fillInTemplateAttachmentKeys(List<Object> keys, List<TemplateAttachment> entities) {
		for (TemplateAttachment entity : entities) {
			keys.add(entity.getId());
		}
	}

	private static void fillInEndpointKeys(List<Object> keys, List<Endpoint> entities) {
		for (Endpoint entity : entities) {
			keys.add(entity.getId());
		}
	}

	private static void fillInResourceKeys(List<Object> keys, List<Resource> entities) {
		for (Resource entity : entities) {
			keys.add(entity.getId());
		}
	}

	private static void fillInTemplateKeys(List<Object> keys, List<Template> entities) {
		for (Template entity : entities) {
			keys.add(entity.getId());
		}
	}

	  private static void fillInApplicationKeys(List<Object> keys, List<Application> entities) {
		for (Application entity : entities) {
			keys.add(entity.getPath());
		}
	}

	private static void fillInFailureKeys(List<Object> keys, List<Failure> entities) {
		for (Failure entity : entities) {
			keys.add(entity.getId());
		}
	}

	/**
	 * {@link Class#getSimpleName()} is not GWT compatible yet, so we provide
	 * our own implementation.
	 */
	private static String simpleName(Class<?> clazz) {
		String name = clazz.getName();

		// the nth anonymous class has a class name ending in "Outer$n"
		// and local inner classes have names ending in "Outer.$1Inner"
		name = name.replaceAll("\\$[0-9]+", "\\$");

		// we want the name of the inner class all by its lonesome
		int start = name.lastIndexOf('$');

		// if this isn't an inner class, just find the start of the
		// top level class name.
		if (start == -1) {
			start = name.lastIndexOf('.');
		}
		return name.substring(start + 1);
	}

}
