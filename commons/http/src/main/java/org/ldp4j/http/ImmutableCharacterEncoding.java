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

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;

import com.google.common.base.MoreObjects;

final class ImmutableCharacterEncoding implements CharacterEncoding {

	private final Charset charset;

	ImmutableCharacterEncoding(Charset charset) {
		this.charset = charset;
	}

	@Override
	public boolean isWildcard() {
		return this.charset==null;
	}

	@Override
	public String name() {
		return this.charset==null?"*":this.charset.name();
	}

	@Override
	public Charset charset() {
		return this.charset;
	}

	@Override
	public String toHeader() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.charset);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof CharacterEncoding) {
			CharacterEncoding that=(CharacterEncoding)obj;
			result=Objects.equals(this.charset,that.charset());
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("charset",name()).
					toString();
	}

	static ImmutableCharacterEncoding copyOf(CharacterEncoding characterEncoding) {
		ImmutableCharacterEncoding result=null;
		if(characterEncoding instanceof ImmutableCharacterEncoding) {
			result=(ImmutableCharacterEncoding)characterEncoding;
		} else if(characterEncoding!=null) {
			result=new ImmutableCharacterEncoding(characterEncoding.charset());
		}
		return result;
	}

}
