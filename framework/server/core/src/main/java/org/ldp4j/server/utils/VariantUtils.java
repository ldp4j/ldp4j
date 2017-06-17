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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import org.ldp4j.server.data.DataTransformator;

public final class VariantUtils {

	private static final String[] TEMPLATES={
		"Media type: %1$s; encoding=%2$s; language=%3$s",
		"Media type: %1$s; language=%3$s",
	};

	private VariantUtils() {
	}

	public static List<Variant> defaultVariants() {
		return
			VariantUtils.
				createVariants(
					DataTransformator.supportedMediaTypes());
	}

	/**
	 * Get a list of acceptable variants. Current implementation only leverages
	 * media type for the specification of variants.
	 *
	 * @param mediaTypes
	 *            The list of acceptable media types.
	 * @return A list of acceptable variants.
	 */
	public static List<Variant> createVariants(MediaType... mediaTypes) {
		return
			Variant.VariantListBuilder.
				newInstance().
					mediaTypes(mediaTypes).
					encodings().
					languages().
					add().
					build();
	}

	public static List<Variant> createVariants(Collection<? extends MediaType> mediaTypes) {
		return createVariants(mediaTypes.toArray(new MediaType[mediaTypes.size()]));
	}

	public static String toString(Variant v) {
		return
			String.format(
				getTemplate(v),
				getMediaType(v),
				getEncoding(v),
				getLanguage(v));
	}

	public static String toString(List<Variant> variants) {
		StringBuilder builder=new StringBuilder();
		for(Iterator<Variant> it=variants.iterator();it.hasNext();) {
			String formatedVariants = toString(it.next());
			builder.append(formatedVariants);
			if(it.hasNext()) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}

	private static String getTemplate(Variant v) {
		return
			v.getEncoding()!=null?
				TEMPLATES[0]:
				TEMPLATES[1];
	}

	private static String getLanguage(Variant v) {
		String language="*";
		Locale locale = v.getLanguage();
		if(locale!=null) {
			language=locale.toString();
		}
		return language;
	}

	private static String getEncoding(Variant v) {
		String encoding = v.getEncoding();
		if(encoding==null) {
			encoding="*";
		}
		return encoding;
	}

	private static MediaType getMediaType(Variant v) {
		MediaType mediaType = v.getMediaType();
		if(mediaType==null) {
			mediaType=new MediaType(MediaType.MEDIA_TYPE_WILDCARD,MediaType.MEDIA_TYPE_WILDCARD);
		}
		return mediaType;
	}

}
