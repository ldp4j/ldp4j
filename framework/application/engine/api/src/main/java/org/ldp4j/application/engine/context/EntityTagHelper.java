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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-api:0.2.2
 *   Bundle      : ldp4j-application-engine-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.context;

import static com.google.common.base.Preconditions.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class EntityTagHelper {

	private static final String  ENTITY_TAG_VALUE_NOT_PROPERLY_QUOTED_S = "Entity tag value not properly quoted (%s)";
	private static final String  ENTITY_TAG_VALUE_CANNOT_BE_NULL = "Entity tag value cannot be null";
	private static final String  MISFORMATTED_ENTITY_TAG = "Misformatted entity tag : ";
	private static final String  ETAG_REGEX  ="(W/)?\"(([\\x20-\\x21]|[\\x23-\\x7E]|[\\x80-\\xFF]|\\[\\x00-\\x7f])*)\"";
	private static final Pattern ETAG_PATTERN=Pattern.compile(ETAG_REGEX);

	private static final String EMPTY          = "";
	private static final String QUOTATION_MARK = "\"";
	private static final String WILDCARD_ETAG  = "*";
	private static final String WEAK_PREFIX    = "W/";

	private EntityTagHelper() {
	}

	static EntityTag fromString0(String header) {
		checkNotNull(header,ENTITY_TAG_VALUE_CANNOT_BE_NULL);

		if(WILDCARD_ETAG.equals(header)) {
			return new EntityTag(WILDCARD_ETAG,false);
		}

		boolean weak = false;
		String tag=header;
		if(tag.startsWith(WEAK_PREFIX)) {
			weak=true;
			tag=tag.substring(2);
		}

		if(tag.isEmpty()) {
			return new EntityTag(EMPTY,weak);
		}

		EntityTag result = createUnquoted(header, tag, weak);
		if(result!=null) {
			return result;
		}

		return createQuoted(header, tag, weak);
	}

	private static EntityTag createQuoted(String header, String tag, boolean weak) {
		assertIsProperlyQuoted(header,tag);
		String unquotedTag=tag.length()==2?EMPTY:tag.substring(1,tag.length()-1);
		assertHasNoInnerQuotationMarks(header,unquotedTag);
		return new EntityTag(unquotedTag,weak);
	}

	private static void assertHasNoInnerQuotationMarks(String header, String tag) {
		if(tag.contains(QUOTATION_MARK)) {
			throw new IllegalArgumentException(MISFORMATTED_ENTITY_TAG+header);
		}
	}

	private static void assertIsProperlyQuoted(String header, String tag) {
		if (tag.length() < 2 || !tag.startsWith(QUOTATION_MARK) || !tag.endsWith(QUOTATION_MARK)) {
			throw new IllegalArgumentException(MISFORMATTED_ENTITY_TAG+header);
		}
	}

	private static EntityTag createUnquoted(String header, String tag, boolean weak) {
		EntityTag unquoted=null;
		if (tag.length() > 0 && !tag.startsWith(QUOTATION_MARK) && !tag.endsWith(QUOTATION_MARK)) {
			if(tag.contains(QUOTATION_MARK)) {
				throw new IllegalArgumentException(MISFORMATTED_ENTITY_TAG+header);
			} else {
				unquoted=new EntityTag(tag, weak);
			}
		}
		return unquoted;
	}

	static EntityTag fromString(String header) {
		checkNotNull(header,ENTITY_TAG_VALUE_CANNOT_BE_NULL);
		boolean weak=false;
		String tag=header;
		if(tag.startsWith(WEAK_PREFIX)) {
			weak=true;
			tag=tag.substring(2);
		}
		if(EMPTY.equals(tag)) {
			return new EntityTag(tag,weak);
		}
		return new EntityTag(normalizeValue(tag),weak);
	}

	static String normalizeValue(String tag) {
		String value = preprocessTag(tag);
		validateTag(value);
		return value;
	}

	private static void validateTag(String tag) {
		boolean mark=false;
		char[] chars=new char[tag.length()];
		tag.getChars(0, tag.length(), chars,0);
		for(int i=0;i<chars.length;i++) {
			char next=chars[i];
			if(mark) {
				if(next>0x7f) {
					fail(i,"Invalid quoted character 0x%02x ('%c'). Expected one of 0x00-x7f",next,next);
				}
				mark=false;
			} else if(next=='\\') {
				mark=true;
			} else if(next<0x20 || next==0x22 || next==0x7f) {
				fail(i,"Invalid character 0x%02x ('%c'). Expected one of 0x20-0x21, 0x23-x7e, 0x80-0xff",next,next);
			}
		}
		if(mark) {
			fail(chars.length,"expected the character to be quoted");
		}
	}

	private static String preprocessTag(String value) {
		String tag=value;
		boolean quotationStart=tag.startsWith(QUOTATION_MARK);
		boolean quotationEnd=tag.endsWith(QUOTATION_MARK);
		if(quotationStart!=quotationEnd) {
			if(quotationEnd && tag.charAt(tag.length()-2)!='\\') {
				throw new IllegalArgumentException(String.format(ENTITY_TAG_VALUE_NOT_PROPERLY_QUOTED_S,tag));
			}
		} else if(quotationStart) {
			if (value.length()==1) {
				throw new IllegalArgumentException(String.format(ENTITY_TAG_VALUE_NOT_PROPERLY_QUOTED_S,tag));
			} else {
				tag=tag.substring(1,tag.length()-1);
			}
		}
		return tag;
	}

	private static void fail(int i, String format, Object... args) {
		throw new IllegalArgumentException("Error at "+i+": "+String.format(format,args));
	}

	static EntityTag fromString1(String header) {
		EntityTag result=null;
		Matcher matcher = ETAG_PATTERN.matcher(header);
		if(matcher.matches()) {
			result=new EntityTag(matcher.group(2),matcher.group(1)!=null);
		}
		return result;
	}

	static String toString(EntityTag tag) {
		StringBuilder sb = new StringBuilder();
		if (tag.isWeak()) {
			sb.append(WEAK_PREFIX);
		}
		String tagValue = tag.getValue();
		if (!tagValue.startsWith(QUOTATION_MARK)) {
			sb.append(QUOTATION_MARK).append(tagValue).append(QUOTATION_MARK);
		} else {
			sb.append(tagValue);
		}
		return sb.toString();
	}

}
