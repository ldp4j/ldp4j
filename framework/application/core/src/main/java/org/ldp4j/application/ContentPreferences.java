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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.ldp4j.application.domain.LDP;
import org.ldp4j.application.vocabulary.Term;

import com.google.common.collect.Sets;

public final class ContentPreferences {
	
	public enum Preference {
		CONTAINMENT_TRIPLES(LDP.PREFER_CONTAINMENT),
		MEMBERSHIP_TRIPLES(LDP.PREFER_MEMBERSHIP),
		MINIMAL_CONTAINER(LDP.PREFER_MINIMAL_CONTAINER),
		EMPTY_CONTAINER(LDP.PREFER_EMPTY_CONTAINER),
		;

		private final Term term;

		private Preference(Term term) {
			this.term = term;
		}
		
		public URI toURI() {
			return term.as(URI.class);
		}
	}
	
	private final Set<Preference> include;
	private final Set<Preference> omit;
	
	private ContentPreferences() {
		this.include=Sets.newTreeSet();
		this.omit=Sets.newTreeSet();
	}
	
	public Set<Preference> includes() {
		return Collections.unmodifiableSet(this.include);
	}
	
	public Set<Preference> omits() {
		return Collections.unmodifiableSet(this.omit);
	}
	
	public int size() {
		return this.include.size()+this.omit.size();
	}
	
	public boolean isEmpty() {
		return size()==0;
	}

	public void include(Preference preference) {
		if(preference!=null) {
			this.include.add(preference);
			this.omit.remove(preference);
		}
	}

	public void omit(Preference preference) {
		if(preference!=null) {
			this.include.remove(preference);
			this.omit.add(preference);
		}
	}
	
	public boolean isRequired(Preference preference) {
		return this.include.contains(preference) || !this.omit.contains(preference);
	}
	
	public static ContentPreferences defaultPreferences() {
		ContentPreferences tmp = new ContentPreferences();
		tmp.include(Preference.CONTAINMENT_TRIPLES);
		tmp.include(Preference.MEMBERSHIP_TRIPLES);
		return tmp;
	}

	public static ContentPreferencesBuilder builder() {
		return new ContentPreferencesBuilder();
	}
	
	public static final class ContentPreferencesBuilder {
		
		private ContentPreferences contentPreferences;

		private ContentPreferencesBuilder() {
			this.contentPreferences=new ContentPreferences();
		}
		
		public ContentPreferencesBuilder withInclude(Preference preference) {
			this.contentPreferences.include(preference);
			return this;
		}
		
		public ContentPreferencesBuilder withOmit(Preference preference) {
			this.contentPreferences.omit(preference);
			return this;
		}
		
		public ContentPreferences build() {
			return this.contentPreferences;
		}
	}

}