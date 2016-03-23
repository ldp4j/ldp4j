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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-http-0.3.0-SNAPSHOT.jar
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

final class Weighted<T> implements Acceptable {

	interface Parser<T> {

		T parse(String data);

	}

	private static final Pattern QUALITY_PATTERN = Pattern.compile("[ \t]*;[ \t]*[qQ]=((1\\.0{0,3})|(0\\.\\d{0,3}))");

	private static final double DEFAULT_WEIGHT = 1.0D;

	private final Double weight;

	private final T value;

	private Weighted(final Double weight, final T content) {
		this.weight = weight;
		this.value = content;
	}

	Weighted<T> weight(final Double weight) {
		return new Weighted<T>(round(weight),this.value);
	}

	<S> Weighted<S> content(final S content) {
		return new Weighted<S>(this.weight,content);
	}

	T get() {
		return this.value;
	}

	@Override
	public boolean hasWeight() {
		return this.weight!=null;
	}

	@Override
	public double weight() {
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
		final Matcher matcher = QUALITY_PATTERN.matcher(candidate);
		final List<String> weights=Lists.newArrayList();
		while(matcher.find()) {
			final String group = matcher.group(1);
			weights.add(group);
		}
		checkArgument(weights.size()<2,"Only one quality value can be specified (found %s: %s)",weights.size(),Joiner.on(", ").join(weights));
		final String trimmed = matcher.replaceAll("");
		final Double weight=
			weights.isEmpty()?
				null:
				Double.parseDouble(weights.get(0));
		return
			Weighted.
				newInstance().
					weight(weight).
					content(trimmed);
	}

	static <T> Weighted<T> fromString(final String candidate, final Parser<T> parser) {
		final Weighted<String> base=fromString(candidate);
		return base.content(parser.parse(base.get()));
	}

	static Double round(final Double weight) {
		if(weight==null) {
			return null;
		}
		checkArgument(weight>=0.0D,"Weight cannot be negative (%s)",weight);
		checkArgument(weight<=1.0D,"Weight cannot be greater than 1 (%s)",weight);
		checkArgument(hasPrecision(weight,3),"Weight cannot have more than 3 decimals (%s)",weight);
		return weight;
	}

	private static boolean hasPrecision(final double weight,final int decimals) {
		final double scaled=weight*Math.pow(10D, decimals);
		return Double.doubleToRawLongBits(scaled-(long)scaled)==0;
	}

}