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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

final class Weighted<T> {

	interface Parser<T> {

		T parse(String before, String after);

	}

	private static final class StringParser implements Parser<String> {
		@Override
		public String parse(String before, String after) {
			return before+(after==null?"":after);
		}
	}

	private static final class Match {

		private final int start;
		private final int end;
		private final String weight;

		Match(int start,int end,String weight) {
			this.start = start;
			this.end   = end;
			this.weight = weight;
		}

		String before(String candidate) {
			return candidate.substring(0,this.start);
		}

		String after(String candidate) {
			String result="";
			if(this.end<candidate.length()) {
				result=candidate.substring(this.end);
			}
			return result;
		}

		Double weight() {
			return Double.parseDouble(this.weight);
		}

		@Override
		public String toString() {
			return
				new StringBuilder().
					append("'").append(this.weight).append("'").
					append(" {").append(this.start).append(",").append(this.end).append("}").
					toString();
		}

	}

	private static final Pattern QUALITY_PATTERN = Pattern.compile("[ \t]*;[ \t]*[qQ]([ \t]*)=([ \t]*)([^ \t;]*)");

	private static final Pattern WEIGHT_PATTERN  = Pattern.compile("((?:1\\.0{0,3})|(?:0\\.\\d{0,3}))");

	private static final double DEFAULT_WEIGHT = 1.0D;

	private final Double weight;

	private final T value;

	private Weighted(final Double weight, final T content) {
		this.weight = weight;
		this.value = content;
	}

	Weighted<T> withWeight(final Double weight) {
		return new Weighted<T>(HttpUtils.checkQuality(weight, "Weight"),this.value);
	}

	<S> Weighted<S> withEntity(final S content) {
		return new Weighted<S>(this.weight,content);
	}

	T entity() {
		return this.value;
	}

	boolean hasWeight() {
		return this.weight!=null;
	}

	double weight() {
		return this.weight==null?DEFAULT_WEIGHT:this.weight;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("value",this.value).
					add("weight", this.weight).
					toString();
	}

	static <T> Weighted<T> newInstance() {
		return new Weighted<T>(null,null);
	}

	static Weighted<String> fromString(String candidate) {
		return fromString(candidate,new StringParser());
	}

	static <T> Weighted<T> fromString(final String candidate, final Parser<T> parser) {
		final Matcher matcher = QUALITY_PATTERN.matcher(candidate);
		final List<Match> weights=Lists.newArrayList();
		final List<String> errors=Lists.newArrayList();
		while(matcher.find()) {
			weights.
				add(
					new Match(
						matcher.start(),
						matcher.end(),
						validateDefinition(matcher,errors)));
		}
		checkArgument(errors.isEmpty(),"Quality definition failure%s found: %s",errors.size()==1?"":"s",Joiner.on(", ").join(errors));
		checkArgument(weights.size()<2,"Only one quality value can be specified (found %s: %s)",weights.size(),Joiner.on(", ").join(weights));
		if(weights.isEmpty()) {
			return Weighted.newInstance().withEntity(parser.parse(candidate,null));
		}
		final Match match=weights.get(0);
		final T value=
			parser.
				parse(
					match.before(candidate),
					match.after(candidate));
		return
			Weighted.
				newInstance().
					withWeight(match.weight()).
					withEntity(value);
	}

	private static String validateDefinition(final Matcher matcher, final List<String> errors) {
		verifyNoWhitespaceBeforeEqual(matcher, errors);
		verifyNoWhitespaceAfterEqual(matcher, errors);
		return verifyWeight(matcher, errors);
	}

	private static String verifyWeight(final Matcher matcher, final List<String> errors) {
		final String weight = matcher.group(3);
		final Matcher weightMatcher = WEIGHT_PATTERN.matcher(weight);
		if(!weightMatcher.matches()) {
			if(weight.isEmpty()) {
				errors.add("weight cannot be empty");
			} else {
				errors.add("invalid weight value '"+weight+"' from "+matcher.start(3)+" to "+matcher.end(3));
			}
		}
		return weight;
	}

	private static void verifyNoWhitespaceAfterEqual(final Matcher matcher, final List<String> errors) {
		if(!matcher.group(2).isEmpty()) {
			errors.add("whitespace after equal from "+matcher.start(2)+" to "+matcher.end(2));
		}
	}

	private static void verifyNoWhitespaceBeforeEqual(final Matcher matcher, final List<String> errors) {
		if(!matcher.group(1).isEmpty()) {
			errors.add("whitespace before equal from "+matcher.start(1)+" to "+matcher.end(1));
		}
	}

}