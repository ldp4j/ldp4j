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

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

final class AlternativeEvaluation {

	private static final class AttributeQuality<T extends Negotiable> {

		private final Quality quality;
		private final Weighted<T> value;

		private AttributeQuality(final Quality quality, final Weighted<T> value) {
			this.quality = quality;
			this.value = value;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						omitNullValues().
						add("quality",this.quality).
						add("value",this.value).
						toString();
		}

		static <T extends Negotiable> AttributeQuality<T> create(double weight) {
			return new AttributeQuality<>(Qualities.definite(weight),null);
		}

		static <T extends Negotiable> AttributeQuality<T> create(Weighted<T> value) {
			final Quality quality =
				value.entity().isWildcard()?
					Qualities.speculative(value.weight()):
					Qualities.definite(value.weight());
			return new AttributeQuality<>(quality,value);
		}

		static <T extends Negotiable> AttributeQuality<T> create(double weight, Weighted<T> value) {
			return new AttributeQuality<>(Qualities.definite(weight),value);
		}

	}

	private interface Matcher<T> {
		boolean includes(T t1, T t2);
	}

	private static final class MediaTypeMatcher implements Matcher<MediaType> {
		private static final MediaTypeMatcher INSTANCE=new MediaTypeMatcher();
		@Override
		public boolean includes(MediaType t1, MediaType t2) {
			return MediaTypes.includes(t1,t2);
		}
	}

	private static final class CharacterEncodingMatcher implements Matcher<CharacterEncoding> {
		private static final CharacterEncodingMatcher INSTANCE=new CharacterEncodingMatcher();
		@Override
		public boolean includes(CharacterEncoding t1, CharacterEncoding t2) {
			return CharacterEncodings.includes(t1,t2);
		}
	}

	private static final class LanguageMatcher implements Matcher<Language> {
		private static final LanguageMatcher INSTANCE=new LanguageMatcher();
		@Override
		public boolean includes(Language t1, Language t2) {
			return Languages.includes(t1,t2);
		}
	}

	static final Comparator<AlternativeEvaluation> COMPARATOR =
		new Comparator<AlternativeEvaluation>() {
			@Override
			public int compare(AlternativeEvaluation o1, AlternativeEvaluation o2) {
				int comparison=o2.quality.compareTo(o1.quality);
				if(comparison!=0) {
					return comparison;
				}
				return AlternativeComparator.INSTANCE.compare(o2.alternative, o1.alternative);
			}
		};

	private final ImmutableAlternative alternative;
	private final boolean predefined;

	private AttributeQuality<MediaType> qt;
	private AttributeQuality<CharacterEncoding> qc;
	private AttributeQuality<Language> ql;
	private ImmutableQuality quality;

	private AlternativeEvaluation(boolean predefined, ImmutableAlternative alternative) {
		this.predefined=predefined;
		this.alternative=alternative;
	}

	ImmutableAlternative alternative() {
		return this.alternative;
	}

	ImmutableQuality quality() {
		return this.quality;
	}

	ImmutableVariant variant() {
		ImmutableVariant selection=null;
		if(this.predefined) {
			selection=ImmutableVariant.copyOf(this.alternative);
		} else {
			final MediaType mediaType = normalize(this.alternative.type(),ContentNegotiator.DEFAULT_MEDIA_TYPE);
			final CharacterEncoding charset= normalize(this.alternative.charset(),ContentNegotiator.DEFAULT_CHARACTER_ENCODING);
			final Language language= normalize(this.alternative.language(),ContentNegotiator.DEFAULT_LANGUAGE);
			selection=
				ImmutableVariant.
					newInstance().
						type(mediaType).
						charset(charset).
						language(language);
		}
		return selection;
	}

	void evaluate(
			List<Weighted<MediaType>> accepts,
			List<Weighted<CharacterEncoding>> acceptCharsets,
			List<Weighted<Language>> acceptLanguages) {
		final Quality qs = Qualities.definite(this.alternative.quality());
		this.qt = computeMediaTypeQuality(accepts);
		this.qc = computeCharsetQuality(acceptCharsets);
		this.ql = computeLanguageQuality(acceptLanguages);
		this.quality = combine(qs,this.qt.quality,this.qc.quality,this.ql.quality);
	}

	private AttributeQuality<MediaType> computeMediaTypeQuality(List<Weighted<MediaType>> acceptable) {
		return computeAttributeQuality(this.alternative.type(),acceptable,MediaTypeMatcher.INSTANCE,MediaTypeComparator.INSTANCE);
	}

	private AttributeQuality<CharacterEncoding> computeCharsetQuality(List<Weighted<CharacterEncoding>> acceptable) {
		return computeAttributeQuality(this.alternative.charset(),acceptable,CharacterEncodingMatcher.INSTANCE,CharacterEncodingComparator.INSTANCE);
	}

	private AttributeQuality<Language> computeLanguageQuality(List<Weighted<Language>> acceptable) {
		return computeAttributeQuality(this.alternative.language(),acceptable,LanguageMatcher.INSTANCE,LanguageComparator.INSTANCE);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("predefined",this.predefined).
					add("alternative",this.alternative).
					add("qt",this.qt).
					add("qc",this.qc).
					add("ql",this.ql).
					add("quality",this.quality).
					toString();
	}

	static AlternativeEvaluation predefined(Alternative alternative) {
		return new AlternativeEvaluation(true,ImmutableAlternative.copyOf(alternative));
	}

	static AlternativeEvaluation generated(ImmutableAlternative alternative) {
		return new AlternativeEvaluation(false,alternative);
	}

	private static <T extends Negotiable> T normalize(T original, T defaultValue) {
		T result=original;
		if(result==null) {
			result=defaultValue;
		}
		return result;
	}

	private static <T extends Negotiable> AttributeQuality<T> computeAttributeQuality(
			T attribute,
			List<Weighted<T>> acceptable,
			Matcher<T> matcher,
			Comparator<T> comparator) {
		if(attribute==null) {
			return AttributeQuality.create(1.0D,getPreferredValue(acceptable, comparator));
		}
		if(acceptable.isEmpty()) {
			return AttributeQuality.create(1.0D);
		}
		final SortedSet<Weighted<T>> compatible=Sets.newTreeSet(WeightedComparator.create(comparator));
		for(Weighted<T> accept:acceptable) {
			if(matcher.includes(accept.entity(),attribute)) {
				compatible.add(accept);
			}
		}
		if(compatible.isEmpty()) {
			return AttributeQuality.create(0.0D);
		}
		return AttributeQuality.create(Iterables.get(compatible,0));
	}

	private static <T extends Negotiable> Weighted<T> getPreferredValue(List<Weighted<T>> accepts, Comparator<T> comparator) {
		final SortedSet<Weighted<T>> sortedAccepts=Sets.newTreeSet(WeightedComparator.create(comparator));
		sortedAccepts.addAll(accepts);
		Weighted<T> first = Iterables.getFirst(sortedAccepts,null);
		if(first!=null && first.entity().isWildcard()) {
			first=null;
		}
		return first;
	}

	private static ImmutableQuality combine(Quality... qualities) {
		Quality.Type type=Quality.Type.DEFINITE;
		double weight=1.0D;
		for(Quality quality:qualities) {
			if(Quality.Type.DEFINITE.equals(type)) {
				type=quality.type();
			}
			weight*=quality.weight();
		}
		return ImmutableQuality.create(type,weight);
	}

}