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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-api:0.2.2
 *   Bundle      : rmf-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf;

import java.util.regex.Pattern;



public final class LanguageLiteral extends Literal<String> {

	private final String language;
	
	/**
	 * Language tag is RFC3066-conformant if it matches this regex:
	 * [a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*
	 */
	private static final Pattern MATCHER = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*");

	LanguageLiteral(String value, String language) {
		super(value);
		if(language==null) {
			throw new IllegalArgumentException("Object 'language' cannot be null");
		}
		if(!isRecognizedLanguage(language)) {
			throw new IllegalArgumentException("Invalid language lang '"+language+"'");
		}
		this.language = language;
	}

	private static boolean isRecognizedLanguage(String languageTag) {
		return MATCHER.matcher(languageTag).matches();
	}

	public String getLanguage() {
		return language;
	}

	@Override
	public String toString() {
		return super.toString().concat(String.format("@%s",language));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + language.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof LanguageLiteral)) {
			return false;
		}
		LanguageLiteral other = (LanguageLiteral) obj;
		return language.equals(other.language);
	}

	@Override
	public <T> T accept(NodeVisitor<T> visitor, T defaultValue) {
		return visitor.visitLanguageLiteral(this, defaultValue);
	}
	
}