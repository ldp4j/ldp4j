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
import static org.ldp4j.http.HttpUtils.checkQuality;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

final class ImmutableAlternative implements Alternative {

	private static final DecimalFormatSymbols ENGLISH_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ENGLISH);

	private final double quality;
	private final ImmutableVariant variant;

	private ImmutableAlternative(final double quality, final ImmutableVariant variant) {
		this.quality = quality;
		this.variant = variant;
	}

	@Override
	public double quality() {
		return this.quality;
	}

	@Override
	public MediaType type() {
		return this.variant.type();
	}

	@Override
	public CharacterEncoding charset() {
		return this.variant.charset();
	}

	@Override
	public Language language() {
		return this.variant.language();
	}

	@Override
	public String toString() {
		final StringBuilder builder=new StringBuilder("{");
		builder.append(formatQuality(this.quality));
		appendField(builder,"type",this.variant.type());
		appendField(builder,"charset",this.variant.charset());
		appendField(builder,"language",this.variant.language());
		builder.append("}");
		return builder.toString();
	}

	private <T extends Negotiable> void appendField(final StringBuilder builder, final String field, final T value) {
		if(value!=null) {
			builder.append(" {").append(field).append(" ").append(value.toHeader()).append("}");
		}
	}

	static ImmutableAlternative create(double quality, ImmutableVariant variant) {
		requireNonNull(variant,"Variant cannot be null");
		requireNonNull(quality,"Quality cannot be null");
		checkQuality(quality, "Quality");
		return new ImmutableAlternative(quality,variant);
	}

	static ImmutableAlternative copyOf(Alternative predefined) {
		ImmutableAlternative result=null;
		if(predefined instanceof ImmutableAlternative) {
			result=(ImmutableAlternative)predefined;
		} else if(predefined!=null) {
			result=create(predefined.quality(),ImmutableVariant.copyOf(predefined));
		}
		return result;
	}

	/**
	 * Create an instance each time because JavaDocs says access should be
	 * synchronized in multithreaded environments, but not too sure this
	 * scenario really requires such synchronization
	 */
	private static String formatQuality(double quality) {
		return
			new DecimalFormat("0.000",ENGLISH_SYMBOLS).
				format(quality);
	}

}
