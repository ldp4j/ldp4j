/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.data.impl;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.ldp4j.server.data.spi.MediaTypeProvider;
import org.ldp4j.server.data.spi.RuntimeDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CoreRuntimeDelegate extends RuntimeDelegate {

	private static final Logger LOGGER=LoggerFactory.getLogger(CoreRuntimeDelegate.class);

	private final List<MediaTypeProvider> providers;
	private final Set<MediaType> mediaTypes;

	public CoreRuntimeDelegate() {
		this.mediaTypes=Sets.newLinkedHashSet();
		this.providers=Lists.newCopyOnWriteArrayList();
		populateMediaTypeProviders();
	}

	private void populateMediaTypeProviders() {
		ServiceLoader<MediaTypeProvider> loader=ServiceLoader.load(MediaTypeProvider.class);
		for(MediaTypeProvider provider:loader) {
			addMediaTypeProvider(provider);
		}
	}

	private void addMediaTypeProvider(MediaTypeProvider provider) {
		this.providers.add(provider);
		this.mediaTypes.addAll(provider.supportedMediaTypes());
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(
				"Registered media type provider '"+
				provider.getClass().getCanonicalName()+
				"'. Extended media type support for: "+
				provider.supportedMediaTypes());
		}
	}

	@Override
	public Set<MediaType> getSupportedMediaTypes() {
		return Collections.unmodifiableSet(this.mediaTypes);
	}

	@Override
	public MediaTypeProvider getMediaTypeProvider(MediaType mediaType) {
		for(MediaTypeProvider candidate:this.providers) {
			if(candidate.isSupported(mediaType)) {
				return candidate;
			}
		}
		return null;
	}

	@Override
	public void registerMediaTypeProvider(MediaTypeProvider provider) {
		if(provider!=null) {
			addMediaTypeProvider(provider);
		}
	}

}
