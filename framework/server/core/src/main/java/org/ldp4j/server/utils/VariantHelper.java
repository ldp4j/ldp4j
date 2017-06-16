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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

public final class VariantHelper {

	private final List<Variant> supportedVariants;

	private VariantHelper(Collection<? extends Variant> supportedVariants2) {
		this.supportedVariants = new ArrayList<Variant>(supportedVariants2);
	}

	public boolean isSupported(Variant variant) {
		boolean matched=false;
		for(Iterator<Variant> it=supportedVariants.iterator();it.hasNext() && !matched;) {
			matched=matches(it.next(),variant);
		}
		return matched;
	}

	public Variant select(Collection<? extends Variant> supportedVariants) {
		for(Variant variant:supportedVariants) {
			if(isSupported(variant)) {
				return variant;
			}
		}
		return null;
	}

	private static boolean isLanguageMatched(Locale supported, Locale required) {
		String language = supported.getLanguage();
		return
			"*".equals(language) ||
			language.equalsIgnoreCase(required.getLanguage());
	}

	private static boolean matches(Variant supported, Variant required) {
		return
			hasMatchingMediaType(supported, required) &&
			hasMatchingEncoding(supported, required) &&
			hasMatchingLanguage(supported, required);
	}

	private static boolean hasMatchingEncoding(Variant supported, Variant required) {
		String requiredEncoding = required.getEncoding();
		String supportedEncoding = supported.getEncoding();
		return
			requiredEncoding==null ||
			supportedEncoding==null ||
			supportedEncoding.equals(requiredEncoding);
	}

	private static boolean hasMatchingLanguage(Variant supported, Variant required) {
		Locale requiredLanguage = required.getLanguage();
		Locale supportedLanguage = supported.getLanguage();
		return
			requiredLanguage == null ||
			supportedLanguage ==null ||
			isLanguageMatched(supportedLanguage, requiredLanguage);
	}

	private static boolean hasMatchingMediaType(Variant supported, Variant required) {
		MediaType requiredMediaType = required.getMediaType();
		MediaType supportedMediaType = supported.getMediaType();
		return
			requiredMediaType == null ||
			supportedMediaType == null ||
			supportedMediaType.isCompatible(requiredMediaType);
	}

	public static VariantHelper forVariants(Collection<? extends Variant> supportedVariants) {
		return new VariantHelper(supportedVariants);
	}

	@SafeVarargs
	public static final <T extends Variant> VariantHelper forVariants(T... supportedVariants) {
		return forVariants(Arrays.asList(supportedVariants));
	}

}