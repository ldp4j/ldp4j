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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client.impl;

import org.apache.commons.io.IOUtils;
import org.ldp4j.client.impl.spi.ISourceTypeAdapter;
import org.ldp4j.client.spi.ITypeAdapter;
import org.ldp4j.client.spi.SourceTransformationException;
import org.ldp4j.client.spi.UnsupportedTargetException;

final class StringSourceTypeAdapter implements ISourceTypeAdapter<String> {

	private static final class InputStream<T> implements ITypeAdapter<String, T> {

		private final Class<T> targetClazz;

		private InputStream(Class<T> targetClazz) {
			this.targetClazz = targetClazz;
		}

		@Override
		public T transform(String source) throws SourceTransformationException {
			return targetClazz.cast(IOUtils.toInputStream(source));
		}

	}

	private static final class StringAdapter<T> implements ITypeAdapter<String, T> {

		private final Class<T> targetClazz;

		private StringAdapter(Class<T> targetClazz) {
			this.targetClazz = targetClazz;
		}

		@Override
		public T transform(String source) throws SourceTransformationException {
			return targetClazz.cast(source);
		}

	}

	@Override
	public boolean supportsTarget(Class<?> targetClazz) {
		return
			targetClazz.isAssignableFrom(String.class) ||
			targetClazz.isAssignableFrom(InputStream.class);
	}

	@Override
	public <T> ITypeAdapter<String, T> createTypeAdapter(final Class<T> targetClazz) throws UnsupportedTargetException {
		if(targetClazz.isAssignableFrom(String.class)) {
			return new StringAdapter<T>(targetClazz);
		} else if(targetClazz.isAssignableFrom(InputStream.class)) {
			return new InputStream<T>(targetClazz);
		}
		throw new UnsupportedTargetException(String.format("Could not serialize content availabe as '%s' to '%s'",String.class.getCanonicalName(),targetClazz.getCanonicalName()));
	}
	
}