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
package org.ldp4j.commons.net;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class URIUtils {

	private static final String TARGET_URI_CANNOT_BE_NULL = "Target URI cannot be null";

	private static final String BASE_URI_CANNOT_BE_NULL = "Base URI cannot be null";

	private static final String SLASH = "/";
	private static final String PARENT = "..";
	private static final String EMPTY = "";
	private static final Logger LOGGER=LoggerFactory.getLogger(URIUtils.class);

	private URIUtils() {
	}

	public static URL toURL(URI uri) throws MalformedURLException {
		Objects.requireNonNull(uri, "URI cannot be null");
		try {
			return uri.toURL();
		} catch(MalformedURLException e) {
			return tryFallback(uri, e);
		}
	}

	public static URI relativize(URI base, URI target) {
		Objects.requireNonNull(base,BASE_URI_CANNOT_BE_NULL);
		Objects.requireNonNull(target,TARGET_URI_CANNOT_BE_NULL);

		URI nTarget = target.normalize();
		if(areRelativizable(base,target)) {
			URI nBase=base.normalize();
			if(nBase.equals(nTarget)) {
				nTarget=URI.create(EMPTY);
			} else {
				URI walkthrough = absoluteRelativization(nBase,nTarget);
				if(!(walkthrough.getPath().startsWith(PARENT) && nTarget.getPath().isEmpty())) {
					nTarget=walkthrough;
				}
			}
		}
		return nTarget;
	}

	public static URI resolve(URI base, URI target) {
		Objects.requireNonNull(base,BASE_URI_CANNOT_BE_NULL);
		Objects.requireNonNull(target,TARGET_URI_CANNOT_BE_NULL);

		if(areOpaque(base, target)) {
			return target;
		}

		return relativeResolution(target, base).toURI();
	}

	private static boolean areRelativizable(URI base, URI target) {
		return
			!areOpaque(base, target) &&
			haveSameScheme(base, target) &&
			haveSameAuthority(base, target);
	}

	private static boolean haveSameAuthority(URI base, URI target) {
		return Objects.equals(base.getAuthority(),target.getAuthority());
	}

	private static boolean haveSameScheme(URI base, URI target) {
		return Objects.equals(base.getScheme(),target.getScheme());
	}

	private static boolean areOpaque(URI base, URI target) {
		return base.isOpaque() || target.isOpaque();
	}

	private static boolean defined(String value) {
		return value!=null && !value.isEmpty();
	}

	private static URL createFallback(URI uri) {
		URL fallback=null;
		String scheme = uri.getScheme();
		if(ProtocolHandlerConfigurator.isSupported(scheme)) {
			URLStreamHandler handler = ProtocolHandlerConfigurator.getHandler(scheme);
			try {
				fallback=new URL(scheme,null,0,uri.toString().replace(scheme+":",EMPTY),handler);
			} catch (MalformedURLException fatal) {
				if(LOGGER.isWarnEnabled()) {
					LOGGER.warn(String.format("Fallback solution for supported custom '%s' protocol failed. Full stack trace follows.",scheme),fatal);
				}
			}
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(
				String.format(
					fallback==null?
						"Support for custom '%s' protocol not configured":
						"Support for custom '%s' protocol was configured, but the handler was not instantiated automatically. Fallback solution provided.",
					scheme));
		}
		return fallback;
	}

	private static URL tryFallback(URI uri, MalformedURLException e) throws MalformedURLException {
		URL fallback = null;
		if(e.getMessage().startsWith("unknown protocol")) {
			fallback=createFallback(uri);
		}
		if(fallback!=null) {
			return fallback;
		} else {
			throw e;
		}
	}

	/**
	 * 5.2.  Relative Resolution
	 *
	 *    This section describes an algorithm for converting a URI reference
	 *    that might be relative to a given base URI into the parsed components
	 *    of the reference's target.  The components can then be recomposed, as
	 *    described in Section 5.3, to form the target URI.  This algorithm
	 *    provides definitive results that can be used to test the output of
	 *    other implementations.  Applications may implement relative reference
	 *    resolution by using some other algorithm, provided that the results
	 *    match what would be given by this one.
	 *
	 * 5.2.1.  Pre-parse the Base URI
	 *
	 *    The base URI (Base) is established according to the procedure of
	 *    Section 5.1 and parsed into the five main components described in
	 *    Section 3.  Note that only the scheme component is required to be
	 *    present in a base URI; the other components may be empty or
	 *    undefined.  A component is undefined if its associated delimiter does
	 *    not appear in the URI reference; the path component is never
	 *    undefined, though it may be empty.
	 *
	 *    Normalization of the base URI, as described in Sections 6.2.2 and
	 *    6.2.3, is optional.  A URI reference must be transformed to its
	 *    target URI before it can be normalized.
	 *
	 * 5.2.2.  Transform References
	 *
	 *    For each URI reference (R), the following pseudocode describes an
	 *    algorithm for transforming R into its target URI (T):
	 *       -- The URI reference is parsed into the five URI components
	 *       --
	 *       (R.scheme, R.authority, R.path, R.query, R.fragment) = parse(R);
	 *
	 *       -- A non-strict parser may ignore a scheme in the reference
	 *       -- if it is identical to the base URI's scheme.
	 *       --
	 *       if ((not strict) and (R.scheme == Base.scheme)) then
	 *          undefine(R.scheme);
	 *       endif;
	 *
	 *       if defined(R.scheme) then
	 *          T.scheme    = R.scheme;
	 *          T.authority = R.authority;
	 *          T.path      = remove_dot_segments(R.path);
	 *          T.query     = R.query;
	 *       else
	 *          if defined(R.authority) then
	 *             T.authority = R.authority;
	 *             T.path      = remove_dot_segments(R.path);
	 *             T.query     = R.query;
	 *          else
	 *             if (R.path == "") then
	 *                T.path = Base.path;
	 *                if defined(R.query) then
	 *                   T.query = R.query;
	 *                else
	 *                   T.query = Base.query;
	 *                endif;
	 *             else
	 *                if (R.path starts-with "/") then
	 *                   T.path = remove_dot_segments(R.path);
	 *                else
	 *                   T.path = merge(Base.path, R.path);
	 *                   T.path = remove_dot_segments(T.path);
	 *                endif;
	 *                T.query = R.query;
	 *             endif;
	 *             T.authority = Base.authority;
	 *          endif;
	 *          T.scheme = Base.scheme;
	 *       endif;
	 *
	 *       T.fragment = R.fragment;
	 */
	private static URIRef relativeResolution(URI target, URI base) {
		URIRef Base=URIRef.create(base); // NOSONAR
		URIRef R=URIRef.create(target); // NOSONAR
		URIRef T=URIRef.create(); // NOSONAR
		if(defined(R.scheme)) {
			T.scheme    = R.scheme;
			T.authority = R.authority;
			T.path      = removeDotSegments(R.path);
			T.query     = R.query;
		} else {
			if(defined(R.authority)) {
				T.authority = R.authority;
				T.path      = removeDotSegments(R.path);
				T.query     = R.query;
			} else {
				resolvePathOnlyTarget(Base, R, T);
			}
			T.scheme = Base.scheme;
		}
		T.fragment = R.fragment;
		return T;
	}

	private static void resolvePathOnlyTarget(URIRef Base, URIRef R, URIRef T) { // NOSONAR
		if(R.path.isEmpty()) {
			T.path=Base.path;
			if(defined(R.query)) {
				T.query=R.query;
			} else {
				T.query=Base.query;
			}
		} else {
			if(R.path.startsWith(SLASH)) {
				T.path=removeDotSegments(R.path);
			} else {
				T.path=merge(Base.path,R.path,defined(Base.authority));
				T.path=removeDotSegments(T.path);
			}
			T.query=R.query;
		}
		T.authority = Base.authority;
	}

	/**
	 * 5.2.3.  Merge Paths
	 *
	 *    The pseudocode above refers to a "merge" routine for merging a
	 *    relative-path reference with the path of the base URI.  This is
	 *    accomplished as follows:
	 *
	 *    o  If the base URI has a defined authority component and an empty
	 *       path, then return a string consisting of "/" concatenated with the
	 *       reference's path; otherwise,
	 *    o  return a string consisting of the reference's path component
	 *       appended to all but the last segment of the base URI's path (i.e.,
	 *       excluding any characters after the right-most "/" in the base URI
	 *       path, or excluding the entire base URI path if it does not contain
	 *       any "/" characters).
	 *
	 * @param hasAuthority
	 */
	private static String merge(String path, String relativePath, boolean hasAuthority) {
		String parent=path;
		if(hasAuthority && parent.isEmpty()) {
			parent=SLASH;
		}
		return parent.substring(0,parent.lastIndexOf('/')+1).concat(relativePath);
	}

	/**
	 * 5.2.4.  Remove Dot Segments
	 *
	 *    The pseudocode also refers to a "remove_dot_segments" routine for
	 *    interpreting and removing the special "." and ".." complete path
	 *    segments from a referenced path.  This is done after the path is
	 *    extracted from a reference, whether or not the path was relative, in
	 *    order to remove any invalid or extraneous dot-segments prior to
	 *    forming the target URI.  Although there are many ways to accomplish
	 *    this removal process, we describe a simple method using two string
	 *    buffers.
	 *
	 *    1.  The input buffer is initialized with the now-appended path
	 *        components and the output buffer is initialized to the empty
	 *        string.
	 *
	 *    2.  While the input buffer is not empty, loop as follows:
	 *
	 *        A.  If the input buffer begins with a prefix of "../" or "./",
	 *            then remove that prefix from the input buffer; otherwise,
	 *
	 *        B.  if the input buffer begins with a prefix of "/./" or "/.",
	 *            where "." is a complete path segment, then replace that
	 *            prefix with "/" in the input buffer; otherwise,
	 *
	 *        C.  if the input buffer begins with a prefix of "/../" or "/..",
	 *            where ".." is a complete path segment, then replace that
	 *            prefix with "/" in the input buffer and remove the last
	 *            segment and its preceding "/" (if any) from the output
	 *            buffer; otherwise,
	 *
	 *        D.  if the input buffer consists only of "." or "..", then remove
	 *            that from the input buffer; otherwise,
	 *
	 *        E.  move the first path segment in the input buffer to the end of
	 *            the output buffer, including the initial "/" character (if
	 *            any) and any subsequent characters up to, but not including,
	 *            the next "/" character or the end of the input buffer.
	 *
	 *    3.  Finally, the output buffer is returned as the result of
	 *        remove_dot_segments.
	 */
	private static String removeDotSegments(String path) {
		Deque<String> outputBuffer=new LinkedList<String>();
		String input=path==null?EMPTY:path;
		while(!input.isEmpty()) {
			input=processInput(outputBuffer, input);
		}
		return assembleInOrder(outputBuffer);
	}

	private static String processInput(Deque<String> outputBuffer, String input) {
		String next=null;
		if(input.startsWith("../")) {
			next=input.substring(3);
		} else if(input.startsWith("./") || input.startsWith("/./")) {
			next=input.substring(2);
		} else if("/.".equals(input)) {
			next=SLASH;
		} else if(input.startsWith("/../")) {
			next=discardSegment(outputBuffer, input, "/../");
		} else if("/..".equals(input)) {
			next=discardSegment(outputBuffer, input, "/..");
		} else if(PARENT.equals(input) || ".".equals(input)) {
			next=EMPTY;
		} else {
			next=discardSegment(outputBuffer, input);
		}
		return next;
	}

	private static String discardSegment(Deque<String> outputBuffer,String input) {
		int nextSlash=0;
		if(input.startsWith(SLASH)) {
			nextSlash=input.indexOf('/',1);
		} else {
			nextSlash=input.indexOf('/',0);
		}
		String nextSegment=input;
		String next=EMPTY;
		if(nextSlash>=0) {
			nextSegment=input.substring(0,nextSlash);
			next=input.substring(nextSlash);
		}
		addSegment(outputBuffer, nextSegment);
		return next;
	}

	private static String assembleInOrder(Deque<String> outputBuffer) {
		Deque<String> reverse=new LinkedList<String>();
		for(String item:outputBuffer) {
			reverse.push(item);
		}
		StringBuilder builder=new StringBuilder();
		for(String item:reverse) {
			builder.append(item);
		}
		return builder.toString();
	}

	private static void addSegment(Deque<String> outputBuffer, String nextSegment) {
		outputBuffer.push(nextSegment);
	}

	private static String discardSegment(Deque<String> outputBuffer, String input, String prefix) {
		if(!outputBuffer.isEmpty()) {
			outputBuffer.pop();
		}
		if(!outputBuffer.isEmpty() && SLASH.equals(outputBuffer.peek())) {
			outputBuffer.pop();
		}
		return SLASH+input.substring(prefix.length());
	}

	private static URI absoluteRelativization(URI base, URI target) {
		URI relative = null;
		URIDescriptor dBase=URIDescriptor.create(base);
		URIDescriptor dTarget=URIDescriptor.create(target);
		if(dBase.getDir().equals(dTarget.getDir())) {
			String rawURI=EMPTY;
			if(!dBase.getFile().equals(dTarget.getFile())) {
				rawURI=dTarget.getFile();
			}
			rawURI+=makeSuffix(dTarget.getQuery(),dTarget.getFragment());
			relative = URI.create(rawURI);
		} else {
			String[] baseDirPathSegments=tokenize(dBase.getDir());
			String[] targetDirPathSegments=tokenize(dTarget.getDir());
			int common = findCommonSegments(baseDirPathSegments, targetDirPathSegments);
			List<String> segments=
				getRelativeSegments(
					targetDirPathSegments,
					common,
					baseDirPathSegments.length-common);

			relative = recreateFromSegments(segments, dTarget);
		}
		return relative;
	}

	private static String[] tokenize(String path) {
		StringTokenizer tokenizer=new StringTokenizer(path,SLASH);
		List<String> segments=new ArrayList<String>();
		if(path.contains(SLASH)) {
			segments.add(EMPTY);
			while(tokenizer.hasMoreTokens()) {
				segments.add(tokenizer.nextToken());
			}
		}
		return segments.toArray(new String[segments.size()]);
	}

	private static String makeSuffix(String query, String fragment) {
		StringBuilder suffix=new StringBuilder();
		if(defined(query)) {
			suffix.append("?").append(query);
		}
		if(defined(fragment)) {
			suffix.append("#").append(fragment);
		}
		return suffix.toString();
	}

	private static int findCommonSegments(String[] basePath, String[] targetPath) {
		int common=0;
		while(common<basePath.length && common<targetPath.length) {
			if(basePath[common].equals(targetPath[common])) {
				common++;
			} else {
				break;
			}
		}
		return common;
	}

	private static List<String> getRelativeSegments(
			String[] targetPath,
			int commonSegments,
			int discardedSegments) {
		List<String> segments=new ArrayList<String>();
		for(int j=0;j<discardedSegments;j++) {
			segments.add(PARENT);
		}
		segments.
			addAll(
				Arrays.asList(
					Arrays.copyOfRange(
						targetPath,
						commonSegments,
						targetPath.length)));
		return segments;
	}

	private static URI recreateFromSegments(List<String> segments, URIDescriptor target) {
		StringBuilder builder=new StringBuilder();
		boolean ancestor=false;
		for(Iterator<String> it=segments.iterator();it.hasNext();) {
			String last = it.next();
			builder.append(last);
			if(it.hasNext()) {
				builder.append(SLASH);
			} else {
				ancestor=last.equals(PARENT);
			}
		}

		String file=
			new StringBuilder().
				append(target.getFile()).
				append(makeSuffix(target.getQuery(),target.getFragment())).
				toString();

		if(!ancestor || !file.isEmpty()) {
			builder.append(SLASH);
		}
		builder.append(file);

		return URI.create(builder.toString());
	}

}
