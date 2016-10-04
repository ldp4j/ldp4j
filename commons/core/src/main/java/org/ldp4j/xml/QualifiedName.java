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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.xml;

import static java.util.Objects.*;

public final class QualifiedName {

	private final String prefix;
	private final String localPart;

	private QualifiedName(final String prefix, final String localPart) {
		this.prefix = prefix;
		this.localPart = localPart;
	}

	public boolean isPrefixed() {
		return this.prefix!=null;
	}

	public String prefix() {
		return this.prefix;
	}

	public String localPart() {
		return this.localPart;
	}

	@Override
	public String toString() {
		final StringBuilder builder=new StringBuilder();
		if(isPrefixed()) {
			builder.append(this.prefix).append(":");
		}
		builder.append(this.localPart);
		return builder.toString();
	}

	public static QualifiedName localName(final String localPart) {
		return
			new QualifiedName(
				null,
				checkNCName(localPart, "Local part"));
	}

	public static QualifiedName prefixName(final String prefix, final String localPart) {
		return
			new QualifiedName(
				checkNCName(prefix, "Prefix"),
				checkNCName(localPart, "Local part"));
	}

	private static String checkNCName(final String argument, String parameter) {
		requireNonNull(argument, parameter+" cannot be null");
		if(!XMLUtils.isNCName(argument)) {
			throw new IllegalArgumentException(parameter+" must be an NCName ('"+argument+"' isn't)");
		}
		return argument;
	}

	public static QualifiedName parse(final CharSequence item) {
		QualifiedName result=null;
		final int colon = findValidPartition(item);
		if(colon>Integer.MIN_VALUE) {
			result=assemble(item, colon);
		}
		return result;
	}

	private static int findValidPartition(final CharSequence item) {
		final CodePointIterator iterator=new CodePointIterator(item);
		int colonIndex=-1;
		boolean inNCName=false;
		boolean invalid=false;
		while(!invalid && iterator.hasNext()) {
			final int codePoint=iterator.next();
			if(Characters.isColon(codePoint)) {
				invalid=colonIndex>-1 || !inNCName;
				inNCName=false;
				colonIndex=iterator.index();
			} else {
				inNCName =
					!inNCName?
						Characters.isNCNameStartChar(codePoint) :
						Characters.isNameChar(codePoint);
				invalid=!inNCName;
			}
		}
		if(invalid || !inNCName) {
			colonIndex=Integer.MIN_VALUE;
		}
		return colonIndex;
	}

	private static QualifiedName assemble(final CharSequence item, final int colon) {
		String prefix=null;
		String localPart=null;
		if(colon<0) {
			localPart=item.toString();
		} else {
			prefix=item.subSequence(0, colon).toString();
			localPart=item.subSequence(colon+1, item.length()).toString();
		}
		return new QualifiedName(prefix, localPart);
	}

}