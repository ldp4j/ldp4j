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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.xml;

import org.ldp4j.util.CharSequences;


final class Validators {

	private static final class CompositeValidator implements Validator<CharSequence> {

		private final Validator<Integer> rest;
		private final Validator<Integer> first;

		private CompositeValidator(final Validator<Integer> first, final Validator<Integer> rest) {
			this.first = first;
			this.rest = rest;
		}

		@Override
		public boolean isValid(final CharSequence s) {
			if(CharSequences.isNullOrEmpty(s)) {
				return false;
			}
			final int firstCodePoint = Character.codePointAt(s, 0);
			if (!this.first.isValid(firstCodePoint)) {
				return false;
			}
			int i = Character.charCount(firstCodePoint);
			while(i < s.length()) {
				final int codePoint = Character.codePointAt(s, i);
				if(!this.rest.isValid(codePoint)) {
					return false;
				}
				i += Character.charCount(codePoint);
			}
			return true;
		}
	}

	private Validators() {
	}

	static Validator<CharSequence> qName() {
		return new Validator<CharSequence>() {
			@Override
			public final boolean isValid(final CharSequence item) {
				return QualifiedName.parse(item)!=null;
			}
		};
	}

	static Validator<CharSequence> name() {
		return new CompositeValidator(nameStartChar(), nameChar());
	}

	static Validator<CharSequence> ncName() {
		return new CompositeValidator(ncNameStartChar(), ncNameChar());
	}

	private static Validator<Integer> nameStartChar() {
		return new Validator<Integer>() {
			@Override
			public boolean isValid(final Integer codePoint) {
				return Characters.isNameStartChar(codePoint);
			}

		};
	}

	private static Validator<Integer> nameChar() {
		return new Validator<Integer>() {
			@Override
			public boolean isValid(final Integer codePoint) {
				return Characters.isNameChar(codePoint);
			}

		};
	}

	private static Validator<Integer> ncNameStartChar() {
		return new Validator<Integer>() {
			@Override
			public boolean isValid(final Integer codePoint) {
				return Characters.isNCNameStartChar(codePoint);
			}

		};
	}

	private static Validator<Integer> ncNameChar() {
		return new Validator<Integer>() {
			@Override
			public boolean isValid(final Integer codePoint) {
				return Characters.isNCNameChar(codePoint);
			}

		};
	}

}