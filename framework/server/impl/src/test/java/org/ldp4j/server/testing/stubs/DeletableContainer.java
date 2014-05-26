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
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.core.DeletionException;
import org.ldp4j.server.core.DeletionResult;
import org.ldp4j.server.core.ILinkedDataPlatformContainer;
import org.ldp4j.server.sdk.IndividualFormattedContent;
import org.ldp4j.server.sdk.IndividualFormattedContent.Individual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeletableContainer implements ILinkedDataPlatformContainer {

	private static final Logger LOGGER=LoggerFactory.getLogger(DeletableContainer.class);
	
	public static final String CONTAINER_ID = "DeletableContainer";

	public final ConcurrentMap<String,String> cache=new ConcurrentHashMap<String, String>();
	
	@Override
	public String getContainerId() {
		return CONTAINER_ID;
	}

	@Override
	public String createResource(IContent content, Format format) throws LinkedDataPlatformException {
		try {
			String body=content.serialize(String.class);
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Creating resource for content '%s'...",body));
			}
			String resourceId = Integer.toHexString(body.hashCode());
			if(cache.putIfAbsent(resourceId, body)!=null) {
				throw new LinkedDataPlatformException("Tried to crete resource with same contents");
			}
			return resourceId;
		} catch (IOException e) {
			throw new LinkedDataPlatformException("Could not read content",e);
		}
	}
	
	boolean containsResource(String resourceId) {
		return cache.containsKey(resourceId);
	}

	String getResource(String resourceId) {
		return cache.get(resourceId);
	}

	Set<String> getResourceList() {
		return cache.keySet();
	}

	boolean updateResource(String resourceId, String body) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Creating resource for content '%s'...",body));
		}
		return cache.replace(resourceId, body)!=null;
	}
	
	DeletionResult deleteResource(String resourceId) throws DeletionException {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Deleting resource '%s'...",resourceId));
		}
		
		DeletionResult result;
		if(cache.remove(resourceId)!=null) {
			result=DeletionResult.newBuilder().enacted(true).withMessage(String.format("Resource '%s' succesfully deleted",resourceId)).build();
		} else {
			throw new DeletionException(String.format("Resource '%s' not found",resourceId));
		}
		return result;
	}

	@Override
	public IContent getSummary(final Collection<String> resources, final Format format) throws LinkedDataPlatformException {
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
		return new IndividualFormattedContent(format,individuals.toArray(new Individual[]{}));
	}

}