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
package org.ldp4j.server.data;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.server.utils.URIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

final class ResolutionContext {

	static final Logger LOGGER=LoggerFactory.getLogger(ResolutionContext.class);

	private static final class Resolution {

		private final URI path;
		private final URIDescriptor descriptor;
		private final URI realPath;

		private Resolution(URI path, URIDescriptor descriptor, URI realPath) {
			this.path = path;
			this.descriptor = descriptor;
			this.realPath = realPath;
		}

		URI path() {
			return path;
		}

		@Override
		public String toString() {
			return String.format("<%s> [<%s>] : %s",this.path,this.realPath,this.descriptor);
		}

		static Resolution create(URI path, URIDescriptor descriptor, URI realPath) {
			return new Resolution(path, descriptor, realPath);
		}

	}

	final class ResolutionResolver {

		private Resolution currentResolution;
		private List<URI> parents;
		private Iterator<URI> iterator;

		private ResolutionResolver(Resolution currentResolution) {
			this.currentResolution=currentResolution;
			this.parents=URIHelper.getParents(currentResolution.path());
			this.iterator=parents.iterator();
		}

		ManagedIndividualId resolve(URI path) {
			acceptResolution(path);
			ManagedIndividualId result=null;
			if(hasAlreadyFailed(path)) {
				checkResolutionFailed(this);
			} else {
				result=getCachedResolution(path);
				if(result==null) {
					result=resolveNewPath(this,path);
				}
			}
			if(result!=null) {
				resolveResolution();
			}
			return result;
		}

		private boolean hasNext() {
			return this.iterator.hasNext();
		}

		private URI path() {
			return this.currentResolution.path();
		}

		private void acceptResolution(URI path) {
			URI next = this.iterator.next();
			if(!next.equals(path)) {
				throw new IllegalStateException("Invalid resolution: expected <"+next+"> but requested <"+path+">");
			}
		}

	}

	private final Iterator<Resolution> resolutionIterator;

	private final Map<URI,ManagedIndividualId> resolvedURIs;
	private final Map<URI,URI> resolvedResolutions;
	private final List<URI> failedURIs;
	private final List<URI> failedResolutions;

	private ResourceResolver delegate;
	private ResolutionResolver currentResolver;

	private List<Resolution> resolutions;

	private ResolutionContext(List<Resolution> resolutions) {
		this.resolutions=ImmutableList.copyOf(resolutions);
		this.resolutionIterator=resolutions.iterator();
		this.delegate=new NullResourceResolver();
		this.resolvedURIs=Maps.newLinkedHashMap();
		this.resolvedResolutions=Maps.newLinkedHashMap();
		this.failedURIs=Lists.newArrayList();
		this.failedResolutions=Lists.newArrayList();
	}

	ResolutionContext withDelegate(ResourceResolver resolver) {
		ResolutionContext resolutionContext = new ResolutionContext(this.resolutions);
		resolutionContext.delegate=resolver;
		return resolutionContext;
	}

	ResolutionResolver currentResolver() {
		if(this.currentResolver==null||!this.currentResolver.hasNext()) {
			if(!this.resolutionIterator.hasNext()) {
				LOGGER.error("No more resolutions available");
				throw new IllegalStateException("No more resolutions available");
			}
			this.currentResolver=new ResolutionResolver(this.resolutionIterator.next());
			LOGGER.trace("Current resolution: {}",this.currentResolver.currentResolution);
		}
		return this.currentResolver;
	}

	private ManagedIndividualId resolveNewPath(ResolutionResolver currentResolver, URI path) {
		ManagedIndividualId resolved=this.delegate.resolveLocation(path);
		if(resolved!=null) {
			LOGGER.trace(" - Resolution <{}> resolved from path <{}>: {}",currentResolver.path(),path,resolved);
			this.resolvedURIs.put(path, resolved);
			this.resolvedResolutions.put(this.currentResolver.path(), path);
		} else {
			LOGGER.trace(" - Path <{}> could not be resolved",path);
			this.failedURIs.add(path);
			checkResolutionFailed(currentResolver);
		}
		return resolved;
	}

	private void checkResolutionFailed(ResolutionResolver resolutionResolver) {
		URI resolutionPath = resolutionResolver.path();
		if(!resolutionResolver.hasNext() && !this.failedResolutions.contains(resolutionPath)) {
			LOGGER.trace(" - Resolution <{}> could not be resolved",resolutionPath);
			this.failedResolutions.add(resolutionPath);
		}
	}

	private void resolveResolution() {
		this.currentResolver=null;
	}

	private boolean hasAlreadyFailed(URI path) {
		return this.failedURIs.contains(path);
	}

	private ManagedIndividualId getCachedResolution(URI path) {
		return this.resolvedURIs.get(path);
	}

	static ResolutionContextBuilder builder() {
		return new ResolutionContextBuilder();
	}

	static final class ResolutionContextBuilder {

		private List<Resolution> resolutions;

		private ResolutionContextBuilder() {
			this.resolutions=Lists.newArrayList();
		}

		ResolutionContextBuilder withResolution(URI path, URIDescriptor descriptor, URI realPath) {
			this.resolutions.add(Resolution.create(path, descriptor, realPath));
			return this;
		}

		ResolutionContext build() {
			return new ResolutionContext(resolutions);
		}

	}

}