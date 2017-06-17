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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.ldp4j.server.data.spi.MediaTypeProvider;

import com.google.common.collect.ImmutableSet;

abstract class AbstractMediaTypeProvider implements MediaTypeProvider {

	private final Set<MediaType> supportedMediaTypes;

	AbstractMediaTypeProvider(MediaType... supported) {
		this.supportedMediaTypes=ImmutableSet.copyOf(supported);
	}

	@Override
	public final Set<MediaType> supportedMediaTypes() {
		return this.supportedMediaTypes;
	}

	@Override
	public final boolean isSupported(MediaType mediaType) {
		checkNotNull(mediaType,"Media type cannot be null");
		boolean supported=false;
		for(Iterator<MediaType> it=supportedMediaTypes.iterator();it.hasNext() && !supported;) {
			supported=it.next().isCompatible(mediaType);
		}
		return supported;
	}

}