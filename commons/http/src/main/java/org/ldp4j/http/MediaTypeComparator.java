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
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

final class MediaTypeComparator implements Comparator<MediaType> {

	private static final class ParameterComparatorHelper {

		private final TreeSet<String> sortedParams;
		private final MediaType mediaType;

		private ParameterComparatorHelper(MediaType mediaType) {
			this.mediaType = mediaType;
			this.sortedParams = Sets.newTreeSet(mediaType.parameters().keySet());
		}

		boolean hasCharset() {
			return this.mediaType.charset()!=null;
		}

		String parameterNames() {
			return Joiner.on(":").join(this.sortedParams);
		}

		String parameterValuesExceptCharset() {
			final Map<String, String> parameters = this.mediaType.parameters();
			final StringBuilder builder=new StringBuilder();
			for(String parameter:this.sortedParams) {
				if(!MediaTypes.PARAM_CHARSET.equals(parameter)) {
					builder.append(parameters.get(parameter)).append(":");
				}
			}
			return builder.toString();
		}

	}

	static final MediaTypeComparator INSTANCE = new MediaTypeComparator();

	private MediaTypeComparator() {
	}

	@Override
	public int compare(final MediaType mediaType1, final MediaType mediaType2) {
		if(MediaTypes.isWildcardType(mediaType1) && !MediaTypes.isWildcardType(mediaType2)) { // */* < text/*
			return 1;
		} else if(MediaTypes.isWildcardType(mediaType2) && !MediaTypes.isWildcardType(mediaType1)) { // text/* > */*
			return -1;
		} else if(!mediaType1.type().equals(mediaType2.type())) { // text/plain == application/xml
			return 0;
		}
		return compareMediaTypesOfSameFamily(mediaType1, mediaType2);
	}

	private int compareMediaTypesOfSameFamily(final MediaType mediaType1, final MediaType mediaType2) { // NOSONAR
		if(MediaTypes.isWildcardSubType(mediaType1) && !MediaTypes.isWildcardSubType(mediaType2)) { // text/* < text/plain
			return 1;
		} else if(MediaTypes.isWildcardSubType(mediaType2) && !MediaTypes.isWildcardSubType(mediaType1)) { // text/plain > text/*
			return -1;
		} else if(!mediaType1.subType().equals(mediaType2.subType())) { // text/plain == text/turtle
			return 0;
		} else if(!Objects.equals(mediaType1.suffix(),mediaType2.suffix())) { // application/rdf+xml == application/rdf+thrift
			return 0;
		}
		return compareMediaTypesOfSameMediaRange(mediaType1, mediaType2);
	}

	private int compareMediaTypesOfSameMediaRange(final MediaType mediaType1, final MediaType mediaType2) {
		final int comparison=compareNumberOfParameters(mediaType1, mediaType2);
		if(comparison!=0) { // text/plain;format=flowed < text/plain
			return comparison;
		}
		return compareParametersStructurally(mediaType1,mediaType2);
	}

	private int compareNumberOfParameters(final MediaType mediaType1, final MediaType mediaType2) {
		return mediaType2.parameters().size()-mediaType1.parameters().size();
	}

	private int compareParametersStructurally(final MediaType mediaType1, final MediaType mediaType2) {
		final ParameterComparatorHelper h1=new ParameterComparatorHelper(mediaType1);
		final ParameterComparatorHelper h2=new ParameterComparatorHelper(mediaType2);
		final int paramNameComparison = h1.parameterNames().compareTo(h2.parameterNames());
		if(paramNameComparison!=0) {
			return paramNameComparison;
		}
		final int paramValueComparison=h1.parameterValuesExceptCharset().compareTo(h2.parameterValuesExceptCharset());
		if(paramValueComparison!=0) {
			return paramValueComparison;
		}
		if(!h1.hasCharset()) {
			return 0;
		}
		return mediaType1.charset().compareTo(mediaType2.charset());
	}

}