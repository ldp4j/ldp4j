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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;

/**
 * Utility class for handling Language instances.
 *
 * @see Language
 */
public final class Languages {

	private static final ImmutableLanguage WILDCARD_INSTANCE=new ImmutableLanguage(null);

	private static final class Requirements {

		private final String type;
		private final String composition;
		private final CharMatcher matcher;

		Requirements(final String type,final String composition,final CharMatcher matcher) {
			this.type = type;
			this.composition = composition;
			this.matcher = matcher;

		}

		private boolean hasValidLength(final String tag) {
			return tag.length()>0 && tag.length()<9;
		}

		String verify(final String tag) {
			checkArgument(
				this.matcher.matchesAllOf(tag),
				"Invalid language-range: %s must be %s ('%s')",
				this.type,
				this.composition,
				tag);
			checkArgument(
				hasValidLength(tag),
				"Invalid language-range: %s length must be greater than 1 and lower than 9 ('%s' --> %s)",
				this.type,
				tag,
				tag.length());
			return tag;
		}
	}

	private static final Requirements[] REQUIREMENTS={
		new Requirements("primary tag","alphabetical",CharMatcher.JAVA_LETTER),
		new Requirements("subtag","alphanumerical",CharMatcher.JAVA_LETTER_OR_DIGIT),
	};

	private static final String WILDCARD = "*";

	private Languages() {
	}

	/**
	 * Create a wildcard {@code Language}
	 *
	 * @return a wildcard {@code Language} instance
	 */
	public static Language wildcard() {
		return WILDCARD_INSTANCE;
	}

	/**
	 * Create a {@code Language} for a given {@code Locale}.
	 *
	 * @param locale
	 *            the {@code Locale} for the {@code Language}
	 * @return a {@code Language} instance with the specified {@code Locale}
	 * @throws NullPointerException
	 *             if {@code locale} is {@code null}
	 * @throws IllegalArgumentException
	 *             if the provided locale is a variant (i.e.,
	 *             {@link Locale#getVariant()} is not empty)
	 */
	public static Language of(final Locale locale) {
		requireNonNull(locale,"Locale cannot be null");
		checkArgument(locale.getVariant().isEmpty(),"Language does not allow locale variants (%s)",locale.getVariant());
		return new ImmutableLanguage(locale);
	}

	/**
	 * Create a {@code Language} instance from a valid {@code language-range}
	 * definition.
	 *
	 * @param language
	 *            the language-range for the language to be loaded.
	 * @return the {@code Language} instance for the specified language-range
	 * @throws NullPointerException
	 *             if {@code language} is null
	 * @throws IllegalArgumentException
	 *             if {@code language} is not a valid {@code language-range}
	 * @see <a href="http://tools.ietf.org/html/rfc4674#section-2.1">RFC 4674 -
	 *      Section 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.3.5">RFC 7231
	 *      - Section 5.3.5</a>
	 */
	public static Language fromString(final String language) {
		requireNonNull(language,"Language cannot be null");
		if(WILDCARD.equals(language)) {
			return wildcard();
		} else {
			return of(parseLocale(language));
		}
	}

	/**
	 * Implements Basic Filtering matching algorithm defined in
	 * <a href="https://tools.ietf.org/html/rfc4647#section-3.3.1">Section
	 * 3.3.1</a> of the <b>Matching of Language Tags</b> (BCP 47)
	 *
	 * @param language1
	 *            the including language
	 * @param language2
	 *            the language that will be checked for inclusion
	 * @return true, if the {@code language1} includes {@code language2}
	 *         attending to the aforementioned algorithm.
	 */
	public static boolean includes(final Language language1, final Language language2) {
		if(language1==null || language2==null) {
			return false;
		}
		if(language1.isWildcard()) {
			return true;
		}
		if(language2.isWildcard()) {
			return false;
		}
		return
			language1.primaryTag().equals(language2.primaryTag()) &&
			(language1.subTag().isEmpty() || language1.subTag().equals(language2.subTag()));
	}

	private static Locale parseLocale(final String locale) {
		final List<String> tags=getTags(locale);
		checkArgument(tags.size()<3,"Invalid language-range: to many tags (%s)",tags.size());
		return
			new Locale(
				verifyTag(tags,0),
				verifyTag(tags,1));
	}

	private static List<String> getTags(final String locale) {
		final List<String> tags=Lists.newArrayList();
		final int end = locale.length();
		int start=0;
		do {
			final int next=locale.indexOf('-',start);
			String tag=null;
			if(next<0) {
				tag=locale.substring(start,end);
				start=end;
			} else {
				tag=locale.substring(start,next);
				start=next+1;
			}
			tags.add(tag);
		} while(start<end);
		return tags;
	}

	private static String verifyTag(final List<String> parts, final int index) {
		if(parts.size()<=index) {
			return "";
		}
		final Requirements requirement=REQUIREMENTS[index];
		return
			requirement.
				verify(parts.get(index));
	}

}