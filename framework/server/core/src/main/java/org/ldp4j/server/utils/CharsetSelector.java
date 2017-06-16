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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import javax.ws.rs.core.MediaType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public final class CharsetSelector {

	protected final class Selection {

		private final SortedSet<CharsetPreference> preferences=Sets.newTreeSet();
		private final List<String> failed=Lists.newArrayList();
		private final String selection;
		private final double wildCardWeigth;

		private Selection() {
			this.wildCardWeigth=filterPreferences();
			String acceptableCharset=null;
			if(!this.preferences.isEmpty()) {
				acceptableCharset=Iterables.getLast(this.preferences).charset();
			} else if(this.failed.isEmpty()) {
				acceptableCharset=StandardCharsets.UTF_8.name();
			}
			this.selection=acceptableCharset;
		}

		private double filterPreferences() {
			ImmutableList<String> supportedCharsets = getCharsetNames();
			ImmutableList<String> charsetPreferences = getCharsetPreferences();

			double wildcardWeight=-1.0D;
			for(String candidate:charsetPreferences) {
				CharsetPreference preference = CharsetPreference.valueOf(candidate);
				if(preference!=null) {
					if(preference.isWildcard()) {
						wildcardWeight=Math.max(wildcardWeight, preference.weight());
					} else if(supportedCharsets.contains(preference.charset())) {
						this.preferences.add(preference);
					} else {
						this.failed.add(candidate);
					}
				} else {
					this.failed.add(candidate);
				}
			}
			return  wildcardWeight;
		}

		private ImmutableList<String> getCharsetPreferences() {
			Builder<String> builder=ImmutableList.<String>builder().addAll(CharsetSelector.this.acceptableCharsets);
			if(CharsetSelector.this.mediaType!=null) {
				String mediaTypePreference = CharsetSelector.this.mediaType.getParameters().get(MediaType.CHARSET_PARAMETER);
				if(mediaTypePreference!=null) {
					builder.add(mediaTypePreference);
				}
			}
			return builder.build();
		}

		private ImmutableList<String> getCharsetNames() {
			Builder<String> builder=ImmutableList.<String>builder();
			for(Charset supportedCharset:CharsetSelector.this.supportedCharsets) {
				builder.
					add(supportedCharset.name()).
					addAll(supportedCharset.aliases());
			}
			return builder.build();
		}

		boolean acceptsAny() {
			return this.wildCardWeigth>=0.0D;
		}

		boolean hasSelection() {
			return this.selection!=null;
		}

		List<String> unsupportedPreferences() {
			return Collections.unmodifiableList(failed);
		}

		String preferredCharset() {
			return this.selection;
		}

		public boolean hasPreferences() {
			return !getCharsetPreferences().isEmpty();
		}

	}

	private final MediaType mediaType;
	private final Iterable<String> acceptableCharsets;
	private final Iterable<Charset> supportedCharsets;
	private Selection selection;

	private CharsetSelector(MediaType mediaType, Iterable<String> acceptableCharsets, Iterable<Charset> supportedCharsets) {
		this.mediaType = mediaType;
		this.acceptableCharsets = acceptableCharsets;
		this.supportedCharsets = supportedCharsets;
	}

	private Selection currentSelect() {
		if(this.selection==null) {
			this.selection=new Selection();
		}
		return this.selection;
	}

	public CharsetSelector mediaType(MediaType mediaType) {
		return new CharsetSelector(mediaType,this.acceptableCharsets,this.supportedCharsets);
	}

	public CharsetSelector acceptableCharsets(Iterable<String> acceptableCharsets) {
		return new CharsetSelector(this.mediaType,acceptableCharsets,this.supportedCharsets);
	}

	public CharsetSelector supportedCharsets(Iterable<Charset> supportedCharsets) {
		return new CharsetSelector(this.mediaType,this.acceptableCharsets,supportedCharsets);
	}

	public String select() {
		return currentSelect().preferredCharset();
	}

	public boolean requiresCharset() {
		return currentSelect().hasPreferences() && !currentSelect().acceptsAny();
	}

	public static CharsetSelector newInstance() {
		return new CharsetSelector(null,Collections.<String>emptyList(),Collections.<Charset>emptyList());

	}

}
