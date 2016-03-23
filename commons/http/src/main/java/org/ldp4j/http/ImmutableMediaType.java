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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

final class ImmutableMediaType implements MediaType {

	private static class MediaRange {
		private String type;
		private String subType;
		private String suffix;
	}

	private static final char SLASH = '\\';

	private static final BitSet TOKEN;
	private static final BitSet QDTEXT;
	private static final BitSet QUOTED_PAIR;

	private static final Pattern WEIGHT_PATTERN=Pattern.compile("((?:1\\.0{0,3})|(?:0\\.\\d{0,3}))");

	private static final String TYPE_SEPARATOR  = "/";
	private static final String PARAM_SEPARATOR = ";";

	static {
		// ASCII Control codes
		final BitSet ctl = new BitSet(128);
		ctl.set(0x00,0x20);
		ctl.set(0x7F);

		// SP, DQUOTE and "(),/:;<=>?@[\]{}"
		final BitSet delimiters = new BitSet(128);
		delimiters.set(' ');
		delimiters.set('\"');
		delimiters.set('(');
		delimiters.set(')');
		delimiters.set(',');
		delimiters.set('/');
		delimiters.set(':');
		delimiters.set(';');
		delimiters.set('<');
		delimiters.set('=');
		delimiters.set('>');
		delimiters.set('?');
		delimiters.set('@');
		delimiters.set('[');
		delimiters.set(SLASH);
		delimiters.set(']');
		delimiters.set('{');
		delimiters.set('}');

		TOKEN = new BitSet(0x80);
		TOKEN.set(0,0x80);
		TOKEN.andNot(ctl);
		TOKEN.andNot(delimiters);

		QDTEXT=new BitSet(0xFF);
		QDTEXT.set('\t');
		QDTEXT.set(0x20,0x7F); // All printable ASCII chars...
		QDTEXT.clear(0x5C);    // ... except '\'
		QDTEXT.set(0x80,0xFF); // Other non-ASCII chars in
		QDTEXT.set(0xFF);      // a byte

		QUOTED_PAIR=new BitSet(0xFF);
		QUOTED_PAIR.set('\t');
		QUOTED_PAIR.set(0x20,0x7F); // All printable ASCII chars
		QUOTED_PAIR.set(0x80,0xFF); // Other non-ASCII chars in
		QUOTED_PAIR.set(0xFF);      // a byte
	}

	private final String type;
	private final String subtype;
	private final String suffix;

	private final Map<String, String> parameters;
	private final Charset charset;
	private final double weight;
	private final boolean hasWeight;

	ImmutableMediaType(final String type, final String subtype, String suffix, final Map<String, String> parameters) {
		this.type=verifyType(type,"media range type cannot be empty");
		this.subtype=verifyType(subtype,"media range subtype cannot be empty");
		this.suffix=verifySuffix(suffix,"invalid media range suffix");
		ensureValidMediaType(this.type,this.subtype);
		this.parameters=verifyParameters(parameters);
		this.charset=getCharset(this.parameters);
		this.weight=getWeight(this.parameters);
		this.hasWeight=hasWeight(this.parameters);
	}

	@Override
	public String type() {
		return this.type;
	}

	@Override
	public String subType() {
		return this.subtype;
	}

	@Override
	public Charset charset() {
		return this.charset;
	}

	@Override
	public String suffix() {
		return this.suffix;
	}

	@Override
	public Map<String,String> parameters() {
		return this.parameters;
	}

	@Override
	public boolean hasWeight() {
		return this.hasWeight;
	}

	@Override
	public double weight() {
		return this.weight;
	}

	@Override
	public int hashCode() {
		return
			mediaRangeHashCode() ^
			standardParametersHashCode() ^
			customParametersHashCode();
	}

	@Override
	public boolean equals(final Object other) {
		if(this==other) {
			return true;
		}
		if(!(other instanceof ImmutableMediaType)) {
			return false;
		}
		final ImmutableMediaType that = (ImmutableMediaType) other;
		return
			hasSameMediaRange(that) &&
			hasSameStandardParameters(that) &&
			hasSameCustomParameters(that);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("type",this.type).
					add("subtype",this.subtype).
					add("suffix",this.suffix).
					add("parameters",this.parameters).
					toString();
	}

	private int mediaRangeHashCode() {
		return
			17*
			caseInsensitiveHashCode(this.type)*
			caseInsensitiveHashCode(this.subtype)*
			(this.suffix==null?13:caseInsensitiveHashCode(this.suffix));
	}

	private int standardParametersHashCode() {
		return 13*Objects.hash(this.charset,this.weight);
	}

	private int customParametersHashCode() {
		int hash=19;
		for(final Entry<String, String> parameter : this.parameters.entrySet()) {
			final String key=parameter.getKey();
			if(MediaTypes.isStandardParameter(key)) {
				continue;
			}
			hash*=caseInsensitiveHashCode(key)^parameter.getValue().hashCode();
		}
		return hash;
	}

	private boolean hasSameMediaRange(final ImmutableMediaType that) {
		return
			this.type.equalsIgnoreCase(that.type) &&
			this.subtype.equalsIgnoreCase(that.subtype) &&
			Objects.equals(this.suffix, that.suffix);
	}

	private boolean hasSameStandardParameters(final ImmutableMediaType that) {
		return
			Objects.equals(this.charset, that.charset)  &&
			Objects.equals(this.weight, that.weight);
	}

	/**
	 * Determine if the parameters in this {@code MediaType} and the supplied
	 * {@code MediaType} are equal, ignoring parameters with special semantics
	 * (Charset and Q).
	 */
	private boolean hasSameCustomParameters(final ImmutableMediaType that) {
		if(this.parameters.size()!=that.parameters.size()) {
			return false;
		}
		for(final Entry<String, String> parameter : this.parameters.entrySet()) {
			final String key=parameter.getKey();
			if(MediaTypes.isStandardParameter(key)) {
				continue;
			}

			if(!Objects.equals(parameter.getValue(), that.parameters.get(key))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Parse the given String into a single {@code MediaType}.
	 *
	 * @param mediaType
	 *            the string to parse
	 * @return the mime type
	 * @throws InvalidMediaTypeException
	 *             if the string cannot be parsed
	 */
	static ImmutableMediaType fromString(final String mediaType) {
		if(mediaType==null) {
			throw new InvalidMediaTypeException("Media type cannot be null");
		}
		if(mediaType.isEmpty()) {
			throw new InvalidMediaTypeException(mediaType,"media type cannot be empty");
		}
		final String[] parts = mediaType.split(PARAM_SEPARATOR);

		String fullType = parts[0];
		// java.net.HttpURLConnection returns a *; q=.2 Accept header
		if(MediaTypes.WILDCARD_TYPE.equals(fullType)) {
			fullType = "*/*";
		}
		final MediaRange mr=parseMediaRange(mediaType,HttpUtils.trimWhitespace(fullType));
		final Map<String, String> parameters=parseParameters(mediaType, parts);
		try {
			return new ImmutableMediaType(mr.type,mr.subType,mr.suffix,parameters);
		} catch (final IllegalArgumentException ex) {
			throw new InvalidMediaTypeException(mediaType,ex,ex.getMessage());
		}
	}

	private static int caseInsensitiveHashCode(String str) {
		return str.toLowerCase().hashCode();
	}

	private static MediaRange parseMediaRange(final String mediaType, final String fullType) {
		if(fullType.isEmpty()) {
			throw new InvalidMediaTypeException(mediaType,"no media range specified");
		}
		final MediaRange mr=new MediaRange();
		parseTypes(mr, mediaType, fullType);
		parseSuffix(mr, mediaType);
		return mr;
	}

	private static void parseSuffix(final MediaRange mr, final String mediaType) {
		final int plusIdx = mr.subType.indexOf('+');
		if(plusIdx==0) {
			throw new InvalidMediaTypeException(mediaType, "missing subtype for structured media type ("+mr.subType.substring(1)+")");
		} else if(plusIdx>0) {
			final String[] parts=mr.subType.split("\\+");
			if(parts.length==2) {
				mr.subType=parts[0];
				mr.suffix=parts[1];
			} else if(parts.length==1) {
				throw new InvalidMediaTypeException(mediaType, "missing suffix for structured media type ("+parts[0]+")");
			} else {
				throw new InvalidMediaTypeException(mediaType, "only one suffix can be defined for a structured media type ("+Joiner.on(", ").join(Arrays.copyOfRange(parts,1,parts.length))+")");
			}
		}
	}

	private static void parseTypes(final MediaRange mr, final String mediaType, final String fullType) {
		final int separatorIdx=fullType.indexOf('/');
		if(separatorIdx==-1) {
			throw new InvalidMediaTypeException(mediaType,"no media range subtype specified");
		} else if(separatorIdx==0) {
			throw new InvalidMediaTypeException(mediaType,"no media range type specified");
		} else if(separatorIdx==fullType.length()-1) {
			throw new InvalidMediaTypeException(mediaType,"no media range subtype specified");
		}
		final String[] types=fullType.split(TYPE_SEPARATOR);
		if(types.length!=2) {
			throw new InvalidMediaTypeException(mediaType, "expected 2 types in media range ("+types.length+")");
		}
		mr.type=types[0];
		mr.subType=types[1];
	}

	/**
	 * TODO: Ensure that quality parameter is the last to appear...
	 */
	private static Map<String, String> parseParameters(final String mediaType, final String[] parts) {
		Map<String, String> parameters=Collections.emptyMap();
		if(parts.length>1) {
			parameters=new LinkedHashMap<>(parts.length-1);
			for(int i=1;i<parts.length;i++) {
				final String parameter=HttpUtils.trimWhitespace(parts[i]);
				final int eqIndex=parameter.indexOf('=');
				if(eqIndex==-1) {
					throw new InvalidMediaTypeException(mediaType, "invalid parameter '"+parts[i]+"'");
				}
				final String attribute=parameter.substring(0,eqIndex);
				final String value=parameter.substring(eqIndex+1);
				final String old=parameters.put(attribute, value);
				if(old!=null && !areCompatible(attribute, old, value)) {
					throw new InvalidMediaTypeException(mediaType, "duplicated parameter '"+attribute+"': found '"+value+"' after '"+old+"'");
				}
			}
		}
		return parameters;
	}

	private static boolean areCompatible(final String attribute, final String first, final String second) {
		String cSecond=second;
		String cFirst=first;
		boolean caseSensitive=true;
		if(MediaTypes.PARAM_CHARSET.equalsIgnoreCase(attribute)) {
			cFirst=HttpUtils.unquote(first);
			cSecond=HttpUtils.unquote(second);
			caseSensitive=false;
		}
		if(caseSensitive) {
			return cFirst.equals(cSecond);
		} else {
			return cFirst.equalsIgnoreCase(cSecond);
		}
	}

	private static Charset getCharset(final Map<String, String> parameters) {
		Charset result=null;
		final String charsetName = parameters.get(MediaTypes.PARAM_CHARSET);
		if(charsetName!=null) {
			result = Charset.forName(HttpUtils.unquote(charsetName));
		}
		return result;
	}

	private static double getWeight(final Map<String, String> parameters) {
		final String weightValue = parameters.get(MediaTypes.PARAM_QUALITY);
		double result=1.0D;
		if(weightValue!=null) {
			result = Double.parseDouble(weightValue);
		}
		return result;
	}

	private static boolean hasWeight(final Map<String, String> parameters) {
		return parameters.containsKey(MediaTypes.PARAM_QUALITY);
	}

	private static void ensureValidMediaType(final String type, final String subtype) {
		if(MediaTypes.WILDCARD_TYPE.equals(type) && !MediaTypes.WILDCARD_TYPE.equals(subtype)) {
			throw new IllegalArgumentException("wildcard type is legal only in wildcard media range ('*/*')");
		}
	}

	/**
	 * TODO: Align type verifications to match restrictions imposed on RFC 6838
	 * @see https://tools.ietf.org/html/rfc6838#section-4.2.8
	 */
	private static String verifyType(final String type, final String message) {
		checkHasLength(type,message);
		checkToken(type);
		return type.toLowerCase(Locale.ENGLISH);
	}

	/**
	 * TODO: Verify that suffix is valid
	 * @see https://tools.ietf.org/html/rfc6838#section-4.2.8
	 */
	private static String verifySuffix(final String suffix, final String message) {
		return suffix==null?null:suffix.toLowerCase();
	}

	private static Map<String, String> verifyParameters(final Map<String, String> parameters) {
		Map<String,String> tmp=Collections.emptyMap();
		if(!MoreCollections.isEmpty(parameters)) {
			final Map<String, String> map=new CaseInsensitiveMap<String>(parameters.size(),Locale.ENGLISH);
			for(final Entry<String, String> entry:parameters.entrySet()) {
				final String attribute = entry.getKey();
				final String value = entry.getValue();
				checkParameter(attribute, value);
				map.put(attribute, value);
			}
			tmp=Collections.unmodifiableMap(map);
		}
		return tmp;
	}

	private static void checkHasLength(final String argument, final String message) {
		checkArgument(!Strings.isNullOrEmpty(argument),message);
	}

	/**
	 * Checks the given token string for illegal characters, as defined in RFC
	 * 7230, section 3.2.6.
	 *
	 * @throws IllegalArgumentException
	 *             in case of illegal characters
	 * @see <a href="http://tools.ietf.org/html/rfc7230#section-3.2.6">Hypertext
	 *      Transfer Protocol (HTTP/1.1): Message Syntax and Routing, Section
	 *      3.2.6</a>
	 */
	private static void checkToken(final String token) {
		for(int i=0;i<token.length();i++) {
			final char ch=token.charAt(i);
			if(!TOKEN.get(ch)) {
				throw new IllegalArgumentException("invalid token character '"+ch+"' in token \""+token+"\" at "+i);
			}
		}
	}

	/**
	 * Checks the given quoted string for illegal characters, as defined in RFC
	 * 7230, section 3.2.6.
	 *
	 * @throws IllegalArgumentException
	 *             in case of illegal characters
	 * @see <a href="http://tools.ietf.org/html/rfc7230#section-3.2.6">Hypertext
	 *      Transfer Protocol (HTTP/1.1): Message Syntax and Routing, Section
	 *      3.2.6</a>
	 */
	private static void checkQuotedString(final String quotedString) {
		boolean quotedPair=false;
		for(int i=0;i<quotedString.length();i++) {
			final char ch=quotedString.charAt(i);
			if(quotedPair) {
				checkArgument(QUOTED_PAIR.get(ch),"Invalid quoted-pair character '%s' in quoted string '%s' at %d",ch,quotedString ,i);
				quotedPair=false;
			} else if(ch==SLASH) {
				quotedPair=true;
			} else {
				checkArgument(QDTEXT.get(ch),"Invalid character '%s' in quoted string '%s' at %d",ch,quotedString ,i);
			}

		}
		checkArgument(!quotedPair,"Missing quoted-pair character in quoted string '%s' at %d",quotedString,quotedString.length());
	}

	private static void checkParameter(final String attribute, String value) {
		checkHasLength(attribute, "parameter attribute must not be empty");
		checkHasLength(value, "parameter value must not be empty");
		checkToken(attribute);
		if(MediaTypes.PARAM_CHARSET.equalsIgnoreCase(attribute)) {
			String unquotedValue = HttpUtils.unquote(value);
			try {
				Charset.forName(unquotedValue);
			} catch (final UnsupportedCharsetException ex) {
				throw new IllegalArgumentException("Unsupported charset '"+ex.getCharsetName()+"'",ex);
			} catch (final IllegalCharsetNameException ex) {
				throw new IllegalArgumentException("Invalid charset name '"+ex.getCharsetName()+"'",ex);
			}
		} else if(MediaTypes.PARAM_QUALITY.equalsIgnoreCase(attribute)) {
			checkArgument(WEIGHT_PATTERN.matcher(value).matches(),"Invalid quality value '%s'",value);
		} else if(!HttpUtils.isQuotedString(value)) {
			checkToken(value);
		} else {
			checkQuotedString(HttpUtils.unquote(value));
		}
	}

}