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

import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.Term;

import com.google.common.base.Objects;

public class CreationPreferences {

	public enum InteractionModel {
		RESOURCE(LDP.RESOURCE),
		BASIC_CONTAINER(LDP.BASIC_CONTAINER),
		DIRECT_CONTAINER(LDP.DIRECT_CONTAINER),
		INDIRECT_CONTAINER(LDP.INDIRECT_CONTAINER);

		private final Term term;

		private InteractionModel(Term term) {
			this.term = term;
		}
		
		public URI asURI() {
			return this.term.as(URI.class);
		}
		
	}
	
	private InteractionModel interactionModel;
	private String path;

	private CreationPreferences() {
	}

	public InteractionModel getInteractionModel() {
		return interactionModel;
	}

	public String getPath() {
		return this.path;
	}

	private void setPath(String path) {
		this.path = path;
	}

	private void setInteractionModel(InteractionModel interactionModel) {
		this.interactionModel = interactionModel;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.interactionModel,this.path);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj!=null && obj.getClass()==this.getClass()) {
			CreationPreferences that=(CreationPreferences)obj;
			result=
				Objects.equal(this.interactionModel,that.interactionModel) &&
				Objects.equal(this.path,that.path);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CreationPreferences [");
		builder.append("interactionModel=").append(this.interactionModel).append(", ");
		builder.append("path=").append(this.path);
		builder.append("]");
		return builder.toString();
	}

	public static CreationPreferences defaultPreferences() {
		return new CreationPreferences();
	}

	public static CreationPreferencesBuilder builder() {
		return new CreationPreferencesBuilder();
	}
	
	public static final class CreationPreferencesBuilder {
		
		private CreationPreferences creationPreferences;

		private CreationPreferencesBuilder() {
			this.creationPreferences=new CreationPreferences();
		}
		
		public CreationPreferencesBuilder withInteractionModel(InteractionModel interactionModel) {
			this.creationPreferences.setInteractionModel(interactionModel);
			return this;
		}
		
		public CreationPreferencesBuilder withPath(String path) {
			this.creationPreferences.setPath(path);
			return this;
		}
		
		public CreationPreferences build() {
			return this.creationPreferences;
		}
	}
	
}
