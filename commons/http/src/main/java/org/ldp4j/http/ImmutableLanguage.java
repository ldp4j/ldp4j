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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import java.util.Locale;
import java.util.Objects;

import com.google.common.base.MoreObjects;

final class ImmutableLanguage implements Language {

	private final Locale locale;

	ImmutableLanguage(Locale locale) {
		this.locale=locale;
	}

	@Override
	public boolean isWildcard() {
		return this.locale==null;
	}

	@Override
	public String primaryTag() {
		return this.locale==null?"*":this.locale.getLanguage();
	}

	@Override
	public String subTag() {
		return this.locale==null?null:this.locale.getCountry();
	}

	@Override
	public Locale locale() {
		return this.locale;
	}

	@Override
	public String toHeader() {
		return languageTag().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.locale);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Language) {
			Language that=(Language)obj;
			result=Objects.equals(this.locale,that.locale());
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("locale",languageTag()).
					toString();
	}

	private String languageTag() {
		return this.locale==null?
			"*":
			this.locale.getLanguage()+
			(this.locale.getCountry().isEmpty()?
				"":
				"-"+this.locale.getCountry());
	}

	static ImmutableLanguage copyOf(Language language) {
		ImmutableLanguage result=null;
		if(language instanceof ImmutableLanguage) {
			result=(ImmutableLanguage)language;
		} else if(language!=null) {
			result=new ImmutableLanguage(language.locale());
		}
		return result;
	}

}