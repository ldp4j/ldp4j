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
package org.ldp4j.persistence.testing.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.ldp4j.persistence.testing.Population;
import org.ldp4j.persistence.testing.Population.Replacement;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;

public final class PopulationBuilder {

	public class ReplacementBuilder {

		private Object oldValue;

		private ReplacementBuilder(Object oldValue) {
			this.oldValue = oldValue;
		}

		public PopulationBuilder withSerializationOfValue(Serializable newValue) {
			return withReplacement(oldValue, newValue, true);
		}

		public PopulationBuilder withValue(Serializable newValue) {
			return withReplacement(oldValue, newValue, false);
		}

	}

	private final class ReplacementImpl implements Replacement {

		private final Object newValue;
		private final Object oldValue;
		private boolean serialized;

		private ReplacementImpl(Object oldValue, Object newValue, boolean serialized) {
			this.newValue = newValue;
			this.oldValue = oldValue;
			this.serialized = serialized;
		}

		private byte[] serialize(Serializable value) {
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			ObjectOutputStream oos=null;
			byte[] byteArray=null;
			try {
				oos=new ObjectOutputStream(out);
				oos.writeObject(value);
				oos.flush();
				byteArray = out.toByteArray();
			} catch (IOException e) {
				throw new RuntimeException("Could not serialize '"+newValue+"'");
			} finally {
				IOUtils.closeQuietly(oos);
			}
			return byteArray;
		}

		@Override
		public Object getOldValue() {
			return this.oldValue;
		}

		@Override
		public Object getNewValue() {
			return this.serialized?serialize((Serializable)this.newValue):this.newValue;
		}

		@Override
		public String toString() {
			return
				Objects.
					toStringHelper(Replacement.class).
						omitNullValues().
						add("oldValue",this.oldValue).
						add("newValue", this.newValue).
						add("serialized", this.serialized).
						toString();
		}
	}

	private static final class PopulationImpl implements Population {

		private final ImmutableList<URL> datasets;
		private final URL dtd;
		private final ImmutableList<Replacement> replacements;

		private PopulationImpl(ImmutableList<URL> datasets, URL dtd, ImmutableList<Replacement> replacements) {
			this.dtd = dtd;
			this.datasets = datasets;
			this.replacements = replacements;
		}

		@Override
		public List<URL> getDatasets() {
			return this.datasets;
		}

		@Override
		public URL getDTD() {
			return this.dtd;
		}

		@Override
		public List<Replacement> getReplacements() {
			return this.replacements;
		}

	}

	private final Builder<URL> datasets;
	private final Map<Object,Object> replacements;
	private final Map<Object,Boolean> replacementFlags;
	private URL dtd;

	private PopulationBuilder() {
		this.replacements=Maps.newLinkedHashMap();
		this.replacementFlags=Maps.newLinkedHashMap();
		this.datasets=ImmutableList.<URL>builder();
	}

	public PopulationBuilder withSource(URI resource) throws MalformedURLException {
		return withDataset(resource.toURL());
	}

	public PopulationBuilder withDataset(URL location) {
		this.datasets.add(location);
		return this;
	}

	public PopulationBuilder withSource(File file) throws MalformedURLException {
		return withSource(file.toURI());
	}

	public PopulationBuilder withDTD(URI resource) throws MalformedURLException {
		return withDTD(resource.toURL());
	}

	public PopulationBuilder withDTD(URL location) {
		this.dtd = location;
		return this;
	}

	public PopulationBuilder withDTD(File file) throws MalformedURLException {
		return withDTD(file.toURI());
	}

	private PopulationBuilder withReplacement(Object oldValue, Object newValue, boolean serialize) {
		this.replacements.put(oldValue,newValue);
		this.replacementFlags.put(oldValue,serialize);
		return this;
	}

	public ReplacementBuilder replacingValue(Object oldValue) {
		return new ReplacementBuilder(oldValue);
	}

	public Population build() throws IOException {
		Builder<Replacement> reps = ImmutableList.<Replacement>builder();
		for(final Entry<Object,Object> entry:this.replacements.entrySet()) {
			Object oldValue = entry.getKey();
			reps.add(new ReplacementImpl(oldValue, entry.getValue(), this.replacementFlags.get(oldValue)));
		}
		return new PopulationImpl(this.datasets.build(),this.dtd,reps.build());
	}

	public static PopulationBuilder create() {
		return new PopulationBuilder();
	}
}
