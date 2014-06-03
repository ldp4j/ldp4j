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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.testing.stubs;

import static org.ldp4j.server.sdk.IndividualFormattedContent.individual;
import static org.ldp4j.server.sdk.IndividualFormattedContent.Placeholders.literal;
import static org.ldp4j.server.sdk.IndividualFormattedContent.Placeholders.qualifiedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;
import org.ldp4j.server.sdk.IndividualFormattedContent.Individual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ResourceManager {

	private static final Logger LOGGER=LoggerFactory.getLogger(ResourceManager.class);
	
	private final ConcurrentMap<String,String> cache=new ConcurrentHashMap<String, String>();

	private final IdGenerator keyGenerator;

	ResourceManager() {
		keyGenerator=new IdGenerator();
	}
	
	Set<String> getResources() {
		return cache.keySet();
	}

	boolean hasResource(String resourceId) {
		return cache.containsKey(resourceId);
	}

	String createResource(IContent content, Format format) throws IOException {
		String body=content.serialize(String.class);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Creating resource for content '%s'...",body));
		}
		String generateKey = keyGenerator.generateKey(body);
		if(cache.putIfAbsent(generateKey, body)!=null) {
			throw new IOException("Repeated identifier generated...");
		}
		return generateKey;
	}

	String retrieveResource(String resourceId) {
		return cache.get(resourceId);
	}

	boolean updateResource(String resourceId, String body) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Updating resource for content '%s'...",body));
		}
		return cache.replace(resourceId, body)!=null;
	}

	boolean deleteResource(String resourceId) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Deleting resource '%s'...",resourceId));
		}
		
		return cache.remove(resourceId)!=null;
	}

	List<Individual> getSummary(final Collection<String> resources, final Format format) {
		List<Individual> individuals=new ArrayList<Individual>();
		for(String res:cache.keySet()) {
			if(resources.contains(res)) {
				individuals.add(
					individual(qualifiedName(res).withNamespace("http://example.org/data#")).
						withPropertyValue(
								qualifiedName("version").withNamespace("http://example.org/vocabulary#"),
								literal("1.0")).
					build());
			}
		}
		return individuals;
	}

}