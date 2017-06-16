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
import static com.google.common.base.Preconditions.checkNotNull;
import static org.ldp4j.http.HttpUtils.checkToken;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

final class ImmutableMediaType implements MediaType {

	private static class MediaRange {
		private String type;
		private String subType;
		private String suffix;
	}

	private static final char SLASH = '\\';

	private static final BitSet QDTEXT;
	private static final BitSet QUOTED_PAIR;

	private static final String TYPE_SEPARATOR  = "/";
	private static final String PARAM_SEPARATOR = ";";

	static {
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

	ImmutableMediaType(final MediaRangeSyntax syntax, final String type, final String subtype, String suffix, final Map<String, String> parameters) {
		checkNotNull(syntax,"Syntax cannot be null");
		this.type=syntax.checkType(type);
		this.subtype=syntax.checkSubType(subtype);
		this.suffix=syntax.checkSuffix(suffix);
		ensureValidMediaType(this.type,this.subtype,this.suffix);
		this.parameters=verifyParameters(parameters);
		this.charset=getCharset(this.parameters);
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
	public boolean isWildcard() {
		return MediaTypes.WILDCARD_TYPE.equals(this.type) || MediaTypes.WILDCARD_TYPE.equals(this.subtype);
	}

	@Override
	public String toHeader() {
		return MediaTypes.toHeader(this);
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
		return 13*Objects.hash(this.charset);
	}

	private int customParametersHashCode() {
		int hash=19;
		for(final Entry<String, String> parameter:this.parameters.entrySet()) {
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
			Objects.equals(this.suffix,that.suffix);
	}

	private boolean hasSameStandardParameters(final ImmutableMediaType that) {
		return Objects.equals(this.charset,that.charset);
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
		for(final Entry<String, String> parameter:this.parameters.entrySet()) {
			final String key=parameter.getKey();
			if(MediaTypes.isStandardParameter(key)) {
				continue;
			}

			if(!Objects.equals(parameter.getValue(),that.parameters.get(key))) {
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
	 * @param syntax the syntax of the media type
	 * @return the mime type
	 * @throws InvalidMediaTypeException
	 *             if the string cannot be parsed
	 */
	static ImmutableMediaType fromString(final String mediaType, final MediaRangeSyntax syntax) {
		if(mediaType==null) {
			throw new InvalidMediaTypeException("Media type cannot be null");
		}
		if(mediaType.isEmpty()) {
			throw new InvalidMediaTypeException(mediaType,"Media type cannot be empty");
		}
		final String[] parts = mediaType.split(PARAM_SEPARATOR);

		String fullType = parts[0];
		// java.net.HttpURLConnection returns a *; q=.2 Accept header
		if(MediaTypes.WILDCARD_TYPE.equals(fullType)) {
			fullType = "*/*";
		}
		final MediaRange mr=parseMediaRange(mediaType,HttpUtils.trimWhitespace(fullType));
		final Map<String,String> parameters=parseParameters(mediaType,parts);
		try {
			return new ImmutableMediaType(syntax,mr.type,mr.subType,mr.suffix,parameters);
		} catch (final IllegalArgumentException ex) {
			throw new InvalidMediaTypeException(mediaType,ex,"Could not create media type");
		}
	}

	static ImmutableMediaType copyOf(final MediaType other) {
		ImmutableMediaType result=null;
		if(other instanceof ImmutableMediaType) {
			result=(ImmutableMediaType)other;
		} else if(other!=null) {
			final String header = other.toHeader();
			try {
				result=fromString(header,MediaRangeSyntax.RFC6838);
			} catch (InvalidMediaTypeException e) { // NOSONAR
				result=fromString(header,MediaRangeSyntax.RFC7230);
			}
		}
		return result;
	}

	private static int caseInsensitiveHashCode(String str) {
		return str.toLowerCase(Locale.ENGLISH).hashCode();
	}

	private static MediaRange parseMediaRange(final String mediaType, final String fullType) {
		if(fullType.isEmpty()) {
			throw new InvalidMediaTypeException(mediaType,"No media range specified");
		}
		final MediaRange mr=new MediaRange();
		parseTypes(mr,mediaType,fullType);
		parseSuffix(mr,mediaType);
		return mr;
	}

	/**
	 * As per RFC 6838, Section 4.2 structuring syntaxes specifier syntaxes are
	 * defined after the last '+' symbol.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6838#section-4.2>[RFC 6838]
	 *      Media Type Specifications and Registration Procedures, Section
	 *      4.2</a>
	 */
	private static void parseSuffix(final MediaRange mr, final String mediaType) {
		final String subType=mr.subType;
		final int plusIdx=subType.lastIndexOf('+');
		if(plusIdx==0) {
			throw new InvalidMediaTypeException(mediaType,"missing subtype for structured media type ("+subType.substring(1)+")");
		} else if(plusIdx==subType.length()-1) {
			throw new InvalidMediaTypeException(mediaType,"missing suffix for structured media type ("+subType.substring(0,subType.length()-1)+")");
		} else if(plusIdx>0) {
			mr.subType=subType.substring(0,plusIdx);
			mr.suffix=subType.substring(plusIdx+1);
		} // Otherwise the subtype does not define a structuring syntax.
	}

	private static void parseTypes(final MediaRange mr, final String mediaType, final String fullType) {
		final int separatorIdx=fullType.indexOf('/');
		if(separatorIdx==-1) {
			throw new InvalidMediaTypeException(mediaType,"No media range subtype specified");
		} else if(separatorIdx==0) {
			throw new InvalidMediaTypeException(mediaType,"No media range type specified");
		} else if(separatorIdx==fullType.length()-1) {
			throw new InvalidMediaTypeException(mediaType,"No media range subtype specified");
		}
		final String[] types=fullType.split(TYPE_SEPARATOR);
		if(types.length!=2) {
			throw new InvalidMediaTypeException(mediaType,"Expected 2 types in media range but got "+types.length+"");
		}
		mr.type=types[0];
		mr.subType=types[1];
	}

	private static Map<String, String> parseParameters(final String mediaType, final String[] parts) {
		Map<String, String> parameters=Collections.emptyMap();
		if(parts.length>1) {
			parameters=new LinkedHashMap<>(parts.length-1);
			for(int i=1;i<parts.length;i++) {
				parseParameter(mediaType,parameters,parts[i]);
			}
		}
		return parameters;
	}

	private static void parseParameter(final String mediaType,Map<String,String> parameters,String rawParameter) {
		final String parameter=HttpUtils.trimWhitespace(rawParameter);
		final int eqIndex=parameter.indexOf('=');
		if(eqIndex==-1) {
			throw new InvalidMediaTypeException(mediaType,"Invalid parameter '"+rawParameter+"': no value defined");
		}
		final String name=parameter.substring(0,eqIndex);
		final String value=parameter.substring(eqIndex+1);
		final String previous=parameters.put(name, value);
		if(previous!=null && !areCompatible(name, previous, value)) {
			throw new InvalidMediaTypeException(mediaType,"Duplicated parameter '"+name+"': found '"+value+"' after '"+previous+"'");
		}
	}

	private static boolean areCompatible(final String attribute, final String first, final String second) {
		String actualFirst=first;
		String actualSecond=second;
		boolean caseSensitive=true;
		if(MediaTypes.PARAM_CHARSET.equalsIgnoreCase(attribute)) {
			actualFirst=HttpUtils.unquote(first);
			actualSecond=HttpUtils.unquote(second);
			caseSensitive=false;
		}
		if(caseSensitive) {
			return actualFirst.equals(actualSecond);
		} else {
			return actualFirst.equalsIgnoreCase(actualSecond);
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

	private static void ensureValidMediaType(final String type, final String subtype, String suffix) {
		if(MediaTypes.WILDCARD_TYPE.equals(type)) {
			checkArgument(MediaTypes.WILDCARD_TYPE.equals(subtype),"Wildcard type is legal only in wildcard media range ('*/*')");
			checkArgument(suffix==null,"Wildcard structured syntax media types are not allowed (i.e., '*/*+%s')",suffix);
		} else {
			checkArgument(!"*".equals(suffix),"Structured syntax suffix cannot be a wildcard");
		}
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

	private static void checkHasLength(final String argument, final String message, Object... args) {
		checkArgument(!Strings.isNullOrEmpty(argument),message,args);
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

	private static void checkParameter(final String parameter, String value) {
		checkHasLength(parameter, "Parameter name cannot be empty");
		checkToken(parameter,"Invalid parameter name '%s'",parameter);
		checkHasLength(value, "Value for parameter '%s' cannot be empty",parameter);
		if(MediaTypes.PARAM_CHARSET.equalsIgnoreCase(parameter)) {
			final String unquotedValue = HttpUtils.unquote(value);
			try {
				Charset.forName(unquotedValue);
			} catch (final UnsupportedCharsetException ex) {
				throw new IllegalArgumentException("Unsupported charset '"+ex.getCharsetName()+"'",ex);
			} catch (final IllegalCharsetNameException ex) {
				throw new IllegalArgumentException("Invalid charset name '"+ex.getCharsetName()+"'",ex);
			}
		} else if(!HttpUtils.isQuotedString(value)) {
			checkToken(value);
		} else {
			checkQuotedString(HttpUtils.unquote(value));
		}
	}

}