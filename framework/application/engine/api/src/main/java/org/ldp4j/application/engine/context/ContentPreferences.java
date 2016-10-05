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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-api:0.2.2
 *   Bundle      : ldp4j-application-engine-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.context;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.Term;

import com.google.common.base.Objects;
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

		public static Preference fromString(String value) {
			for(Preference candidate:values()) {
				if(candidate.term.qualifiedEntityName().equals(value)) {
					return candidate;
				}
			}
			return null;
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
	
	public boolean isMinimalInclusionRequired() {
		return this.include.contains(Preference.MINIMAL_CONTAINER);
	}
	
	public boolean mayInclude(Preference preference) {
		Preference tmp = normalize(preference);
		return this.include.contains(tmp) || (!isOmissiontRequired(tmp) && !isMinimalInclusionRequired());
	}

	private boolean isOmissiontRequired(Preference preference) {
		return this.omit.contains(preference);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.include,this.omit);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj!=null && obj.getClass()==this.getClass()) {
			ContentPreferences that=(ContentPreferences)obj;
			result=
				Objects.equal(this.include,that.include) &&
				Objects.equal(this.omit,that.omit);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContentPreferences [");
		builder.append("include=").append(this.include).append(", ");
		builder.append("omit=").append(this.omit);
		builder.append("]");
		return builder.toString();
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
	
	private static Preference normalize(Preference preference) {
		Preference result=preference;
		if(Preference.EMPTY_CONTAINER.equals(result)) {
			result=Preference.MINIMAL_CONTAINER;
		}
		return result;
	}

	public static final class ContentPreferencesBuilder {
		
		private ContentPreferences contentPreferences;

		private ContentPreferencesBuilder() {
			this.contentPreferences=new ContentPreferences();
		}
		
		public ContentPreferencesBuilder withInclude(Preference preference) {
			this.contentPreferences.include(normalize(preference));
			return this;
		}
		
		public ContentPreferencesBuilder withOmit(Preference preference) {
			this.contentPreferences.omit(normalize(preference));
			return this;
		}
		
		public ContentPreferences build() {
			return this.contentPreferences;
		}
	}

}