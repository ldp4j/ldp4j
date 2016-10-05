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

import static java.util.Objects.requireNonNull;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

public final class CharacterEncodings {

	private static final String WILDCARD = "*";

	private CharacterEncodings() {
	}

	public static CharacterEncoding wildcard() {
		return new ImmutableCharacterEncoding(null);
	}

	public static CharacterEncoding of(Charset charset) {
		requireNonNull(charset,"Charset cannot be null");
		return new ImmutableCharacterEncoding(charset);
	}

	public static CharacterEncoding fromString(final String name) {
		requireNonNull(name,"Character encoding name cannot be null");
		try {
			if(WILDCARD.equals(name)) {
				return wildcard();
			} else {
				return of(Charset.forName(name));
			}
		} catch (final UnsupportedCharsetException ex) {
			throw new IllegalArgumentException("Unsupported character encoding '"+ex.getCharsetName()+"'",ex);
		} catch (final IllegalCharsetNameException ex) {
			throw new IllegalArgumentException("Invalid character encoding name '"+ex.getCharsetName()+"'",ex);
		}
	}

	public static boolean includes(CharacterEncoding ce1, CharacterEncoding ce2) {
		if(ce1==null || ce2==null) {
			return false;
		}
		if(ce1.isWildcard()) {
			return true;
		}
		if(ce2.isWildcard()) {
			return false;
		}
		return ce1.charset().equals(ce2.charset());
	}

}
