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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.bean.JARBContext;
import org.ldp4j.rdf.bean.NamingPolicy;
import org.ldp4j.rdf.util.TripleSet;

final class JARBContextImpl extends JARBContext {

	private interface Memoizer {

		void memoize(Map<Object, Resource<?>> newDeployments);

		<T> Resource<?> getMemoizedIdentity(T object);

	}

	private static class MemoizedDeployments {

		private final Map<Object,Resource<?>> deployments;
		private final NamingPolicy policy;
		private final JARBContextImpl.Memoizer memoizer;
		private final ReadWriteLock lock=new ReentrantReadWriteLock();

		private MemoizedDeployments(NamingPolicy policy) {
			this.policy = policy;
			this.deployments=new HashMap<Object,Resource<?>>();
			this.memoizer=new Memoizer() {
				@Override
				public void memoize(Map<Object, Resource<?>> newDeployments) {
					lock.writeLock().lock();
					try {
						deployments.putAll(newDeployments);
					} finally {
						lock.writeLock().unlock();
					}
				}

				@Override
				public <T> Resource<?> getMemoizedIdentity(T object) {
					return getIdentity(object);
				}
			};
		}

		private JARBContextImpl.TemporalDeploymentPolicy newTemporalDeploymentPolicy() {
			return new TemporalDeploymentPolicy(memoizer,policy);
		}

		private <T> Resource<?> getIdentity(T object) {
			lock.readLock().lock();
			try {
				return deployments.get(object);
			} finally {
				lock.readLock().unlock();
			}
		}

	}

	private static class TemporalDeploymentPolicy implements NamingPolicy {

		private final Map<Object,Resource<?>> newDeployments;
		private final JARBContextImpl.Memoizer memoizer;
		private final NamingPolicy policy;

		private TemporalDeploymentPolicy(JARBContextImpl.Memoizer memoizer, NamingPolicy policy) {
			this.memoizer = memoizer;
			this.policy = policy;
			newDeployments=new HashMap<Object,Resource<?>>();
		}

		@Override
		public <T> Resource<?> createIdentity(T object) {
			Resource<?> identity=memoizer.getMemoizedIdentity(object);
			if(identity==null) {
				identity = policy.createIdentity(object);
				newDeployments.put(object, identity);
			}
			return identity;
		}

		private void makePermanent() {
			memoizer.memoize(newDeployments);
		}

		@Override
		public <T> Resource<?> enumeratedIdentity(T object) {
			Resource<?> identity=memoizer.getMemoizedIdentity(object);
			if(identity==null) {
				identity = policy.enumeratedIdentity(object);
				newDeployments.put(object, identity);
			}
			return identity;
		}

		@Override
		public <T> T resolveEnumerated(Resource<?> identity, Class<? extends T> clazz) {
			throw new UnsupportedOperationException("Method not implemented yet");
		}

	}

	private final JARBContextImpl.MemoizedDeployments deployments;
	private final TransactionalTypeRegistry registry;

	JARBContextImpl(NamingPolicy policy) {
		this.deployments = new MemoizedDeployments(policy);
		this.registry=new TransactionalTypeRegistry();
	}

	private <T> TypeProcessor<T> getTypeProcessor(Class<? extends T> clazz) {
		return new TypeProcessorImpl<T>(clazz,registry);
	}

	@Override
	public <T> Resource<?> getIdentity(T object) {
		return deployments.getIdentity(object);
	}

	@Override
	public <T> TripleSet deflate(T object) {
		Objects.requireNonNull(object, "Object cannot be null");
		JARBContextImpl.TemporalDeploymentPolicy temporalPolicy = deployments.newTemporalDeploymentPolicy();
		TripleSet result = getTypeProcessor(object.getClass()).deflate(object,temporalPolicy);
		temporalPolicy.makePermanent();
		return result;
	}

	@Override
	public <T> T inflate(Resource<?> identity, TripleSet triples, Class<? extends T> clazz) {
		Objects.requireNonNull(identity, "Identity cannot be null");
		Objects.requireNonNull(triples, "Triples cannot be null");
		Objects.requireNonNull(clazz, "Class cannot be null");
		return getTypeProcessor(clazz).inflate(identity,triples);
	}

}