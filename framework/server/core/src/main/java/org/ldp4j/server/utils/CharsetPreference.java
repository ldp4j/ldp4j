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

import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.math.DoubleMath;

public final class CharsetPreference implements Comparable<CharsetPreference> {

	private static final int    MAX_WEIGHT        = 1000;
	private static final double MAX_WEIGHT_DOUBLE = 1000D;

	private static final char[] CTRL = {
		 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
		10,11,12,13,14,15,16,17,18,19,
		20,21,22,23,24,25,26,27,28,29,
		30,31,
		127
	};

	private static final char[] SEPARATORS = {
		'(' ,  ')' ,  '<' , '>' , '@' , ',' , ';' ,
		':' , '\\' , '\"' , '/' , '[' , ']' , '?' ,
		'=' , '{'  , '}'  , ' ' , '\t'
	};

	private static final CharMatcher TOKEN_MATCHER=
		CharMatcher.
			ASCII.
				and(CharMatcher.noneOf(new String(CTRL))).
				and(CharMatcher.noneOf(new String(SEPARATORS)));

	private static final Pattern QUALITY_PATTERN = Pattern.compile("(q|Q)=((1\\.0{0,3})|(0\\.\\d{0,3}))");
	private static final DecimalFormat FORMAT;

	static {
		final DecimalFormat format = new DecimalFormat("0",new DecimalFormatSymbols(Locale.ENGLISH));
		format.setMaximumFractionDigits(3);
		FORMAT=format;
	}

	private final String charset;
	private final int weight;

	public CharsetPreference(final String charset, final int weight) {
		this.charset = charset;
		this.weight = weight;
	}

	public String charset() {
		return this.charset;
	}

	public double weight() {
		return this.weight/MAX_WEIGHT_DOUBLE;
	}

	public boolean isWildcard() {
		return "*".equals(this.charset);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.charset,this.weight);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof CharsetPreference) {
			final CharsetPreference that=(CharsetPreference)obj;
			result=
				Objects.equals(this.charset, that.charset) &&
				Objects.equals(this.weight, that.weight);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			this.charset+
			(this.weight==MAX_WEIGHT?
				"":
				" ; q="+FORMAT.format((float)this.weight/(float)MAX_WEIGHT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(final CharsetPreference o) {
		return this.weight-o.weight;
	}

	/**
	 * Create a charset preference that matches the following grammar:
	 *
	 * <pre>
	 *        {@code
	   CHAR           = <any US-ASCII character (octets 0 - 127)>
	   DIGIT          = <any US-ASCII digit "0".."9">
	   CTL            = <any US-ASCII control character
	                    (octets 0 - 31) and DEL (127)>
	   SP             = <US-ASCII SP, space (32)>
	   HT             = <US-ASCII HT, horizontal-tab (9)>
	   <">            = <US-ASCII double-quote mark (34)>
	   token          = 1*<any CHAR except CTLs or separators>
	   separators     = "(" | ")" | "<" | ">" | "@"
	                  | "," | ";" | ":" | "\" | <">
	                  | "/" | "[" | "]" | "?" | "="
	                  | "{" | "}" | SP | HT
	   charset        = token
	   qvalue         = ( "0" [ "." 0*3DIGIT ] )
	                  | ( "1" [ "." 0*3("0") ] )
	   preference     = ( charset | "*" )[ ";" "q" "=" qvalue ]}
	 * </pre>
	 *
	 * @param str
	 *            a charset specifier
	 * @return the matching chartset preference, or {@code null} if no one could
	 *         be found
	 */
	public static CharsetPreference valueOf(final String str) {
		final String[] parts = str.split(";");
		if(parts.length<=2) {
			final String charsetName =parts[0].trim();
			if("*".equals(charsetName) || TOKEN_MATCHER.matchesAllOf(charsetName)) {
				int weight=MAX_WEIGHT;
				if(parts.length==2) {
					final String weightValue=parts[1].trim();
					final Matcher matcher = QUALITY_PATTERN.matcher(weightValue);
					if(!matcher.matches()) {
						return null;
					}
					if(weightValue.charAt(2)=='0') {
						weight=Integer.parseInt(Strings.padEnd(weightValue.substring(4),3,'0'));
					}
				}
				return new CharsetPreference(charsetName,weight);
			}
		}
		return null;
	}

	public static CharsetPreference wildcard() {
		return new CharsetPreference("*",MAX_WEIGHT);
	}

	public static CharsetPreference wildcard(final double weight) {
		return new CharsetPreference("*",round(weight));
	}

	public static CharsetPreference create(final Charset charset) {
		Objects.requireNonNull(charset,"Charset cannot be null");
		return new CharsetPreference(charset.name(),MAX_WEIGHT);
	}

	public static CharsetPreference create(final Charset charset, final double weight) {
		Objects.requireNonNull(charset,"Charset cannot be null");
		return new CharsetPreference(charset.name(),round(weight));
	}

	static int round(final double weight) {
		Preconditions.checkArgument(weight>=0D,"Weight must be greater than or equal to 0 (%s)",weight);
		Preconditions.checkArgument(weight<=1D,"Weight must be lower than or equal to 1 (%s)",weight);
		return DoubleMath.roundToInt(weight*MAX_WEIGHT_DOUBLE,RoundingMode.DOWN);
	}

}