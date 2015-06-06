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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.data.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ws.rs.core.MediaType;

import org.ldp4j.server.data.spi.IMediaTypeProvider;
import org.ldp4j.server.data.spi.RuntimeDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreRuntimeDelegate extends RuntimeDelegate {

	private static final Logger LOGGER=LoggerFactory.getLogger(CoreRuntimeDelegate.class);
	
	private final List<IMediaTypeProvider> providers=new CopyOnWriteArrayList<IMediaTypeProvider>();
	private final Set<MediaType> mediaTypes=new HashSet<MediaType>();
	
	public CoreRuntimeDelegate() {
		ServiceLoader<IMediaTypeProvider> loader=ServiceLoader.load(IMediaTypeProvider.class);
		for(IMediaTypeProvider provider:loader) {
			addMediaTypeProvider(provider);
		}
	}
	
	@Override
	public Set<MediaType> getSupportedMediaTypes() {
		return Collections.unmodifiableSet(mediaTypes);
	}

	@Override
	public IMediaTypeProvider getMediaTypeProvider(MediaType mediaType) {
		for(IMediaTypeProvider candidate:providers) {
			if(candidate.isSupported(mediaType)) {
				return candidate;
			}
		}
		return null;
	}
	
	public void registerMediaTypeProvider(IMediaTypeProvider provider) {
		if(provider!=null) {
			addMediaTypeProvider(provider);
		}
	}

	private void addMediaTypeProvider(IMediaTypeProvider provider) {
		providers.add(provider);
		mediaTypes.addAll(provider.getSupportedMediaTypes());
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(
				"Registered media type provider '"+
				provider.getClass().getCanonicalName()+
				"'. Extended media type support for: "+
				provider.getSupportedMediaTypes());
		}
	}

}
