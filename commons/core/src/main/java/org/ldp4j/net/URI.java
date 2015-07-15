/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.net;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ldp4j.commons.net.URIUtils;

public final class URI {

	private static final class Objects {

		private Objects() {
		}

		static boolean defined(Object value) {
			return value!=null;
		}

		static int hashCode(Object obj) {
			return obj!=null?obj.hashCode():0;
		}

		static boolean equal(Object one, Object other) {
			return
				one==null?
					other==null:
					other==null?
						false:
						one.equals(other);
		}

	}

	public static final class Authority {

		private static final int DEFAULT_SCHEME_PORT = -1;
		private String userInfo;
		private String host;
		private int port;

		private Authority(String host) {
			setHost(host);
			setPort(DEFAULT_SCHEME_PORT);
		}

		private Authority(Authority authority) {
			this(authority.host);
			setPort(authority.port);
			setUserInfo(authority.userInfo);
		}

		private void setHost(String host) {
			if(host==null || host.trim().isEmpty()) {
				throw new IllegalArgumentException("Host cannot be empty");
			}
			this.host = host;
		}

		private void setPort(int port) {
			if(port>Short.MAX_VALUE) {
				throw new IllegalArgumentException("Port cannot be bigger than "+Short.MAX_VALUE);
			}
			int cPort=port;
			if(cPort<0) {
				cPort=DEFAULT_SCHEME_PORT;
			}
			this.port = cPort;
		}

		private void setUserInfo(String userInfo) {
			this.userInfo=userInfo;
		}

		public String getUserInfo() {
			return this.userInfo;
		}

		public String getHost() {
			return this.host;
		}

		public int getPort() {
			return this.port;
		}

		public boolean isDefaultSchemePort() {
			return this.port==DEFAULT_SCHEME_PORT;
		}

		public Authority withHost(String host) {
			Authority result = new Authority(this);
			result.setHost(host);
			return result;
		}

		public Authority withPort(int port) {
			Authority result = new Authority(this);
			result.setPort(port);
			return result;
		}

		public Authority withUserInfo(String userInfo) {
			if(userInfo==null || userInfo.trim().isEmpty()) {
				throw new IllegalArgumentException("User info cannot be null nor empty");
			}
			Authority result = new Authority(this);
			result.setUserInfo(userInfo);
			return result;
		}

		public Authority withoutUserInfo() {
			Authority result = new Authority(this);
			result.setUserInfo(null);
			return result;
		}

		@Override
		public String toString() {
			StringBuilder builder=new StringBuilder();
			if(this.userInfo!=null && !this.userInfo.isEmpty()) {
				builder.append(this.userInfo);
				builder.append("@");
			}
			builder.append(this.host);
			if(!isDefaultSchemePort()) {
				builder.append(":");
				builder.append(this.port);
			}
			return builder.toString();
		}

		public static Authority forHost(String host) {
			return new Authority(host);
		}

		public static Authority create(java.net.URI jdkURI) {
			Authority result=null;
			if(jdkURI.getAuthority()!=null) {
				result=new Authority(jdkURI.getHost());
				result.setUserInfo(jdkURI.getUserInfo());
				result.setPort(jdkURI.getPort());
			}
			return result;
		}

	}

	public static final class Path {

		private static final Path EMPTY_PATH = Path.create("");

		private String directory;
		private String fileName;
		private String fileExtension;

		private Path() {
		}

		private Path(Path path) {
			setDirectory(path.getDirectory());
			setFile(path.getFile());
		}

		private void setDirectory(String directory) {
			this.directory = directory;
		}

		private void setFile(String file) {
			String fileName=null;
			String fileExtension=null;
			if(file!=null && !file.isEmpty()) {
				int ext = file.lastIndexOf('.');
				if(ext>=0) {
					fileName=file.substring(0,ext);
					fileExtension=file.substring(ext+1);
				} else {
					fileName=file;
				}
			}
			setFileName(fileName);
			setFileExtension(fileExtension);
		}

		private void setFileExtension(String fileExtension) {
			this.fileExtension=fileExtension;
		}

		private void setFileName(String fileName) {
			this.fileName=fileName;
		}

		private String nullable(String string) {
			return string==null?"":string;
		}

		private String file() {
			StringBuilder builder=new StringBuilder();
			builder.append(nullable(this.fileName));
			if(this.fileExtension!=null) {
				builder.append(".");
				builder.append(this.fileExtension);
			}
			return builder.toString();
		}

		private String normalizePath(String[] segments, String file) {
			LinkedList<String> buffer=new LinkedList<String>();
			for(String segment:segments) {
				if(segment.equals(".")) {
					// do nothing
				} else if(segment.equals("..")) {
					if(!buffer.isEmpty()) {
						if(buffer.peekLast().equals(segment)) {
							buffer.addLast(segment);
						} else if(buffer.peekLast().equals("")){
							buffer.addLast(segment);
						} else {
							buffer.removeLast();
						}
					} else {
						buffer.addLast(segment);
					}
				} else {
					buffer.addLast(segment);
				}
			}
			return assemblePath(buffer, file);
		}

		private String assemblePath(List<String> segments, String file) {
			StringBuilder builder=new StringBuilder();
			for(Iterator<String> it=segments.iterator();it.hasNext();) {
				String segment=it.next();
				builder.append(segment);
				if(it.hasNext() || !isDotSegment(segment) || file!=null) {
					builder.append("/");
				}
			}
			if(file!=null) {
				builder.append(file);
			}
			return builder.toString();
		}

		private boolean isDotSegment(String segment) {
			return segment.equals(".") || segment.equals("..");
		}

		private String[] segments() {
			if(this.directory==null) {
				return new String[0];
			}
			return this.directory.split("/");
		}

		public boolean isEmpty() {
			return this.directory==null && this.fileName==null && this.fileExtension==null;
		}

		public boolean isRoot() {
			return this.directory!=null && this.directory.startsWith("/");
		}

		public boolean isOutOfScope() {
			// Files are always on scope
			if(this.directory==null) {
				return false;
			}

			// First, normalize
			Path normalize=normalize();

			// If now we are a file, we are in scope
			if(normalize.directory==null) {
				return false;
			}

			// If we find a segment which is '..' we are out of scope
			String[] segments=normalize.segments();
			boolean result=false;
			for(int i=0;i<segments.length && !result;i++) {
				result=isDotSegment(segments[i]);
			}
			return result;
		}

		public boolean isDirectory() {
			return this.directory!=null && this.fileName==null && this.fileExtension==null;
		}

		public boolean isFile() {
			return !isDirectory();
		}

		public String getDirectory() {
			return this.directory;
		}

		public String getFile() {
			String file=file();
			return file.isEmpty()?null:file;
		}

		public String getFileName() {
			return this.fileName;
		}

		public String getFileExtension() {
			return this.fileExtension;
		}

		public Path withDirectory(String directory) {
			Path result = new Path(this);
			result.setDirectory(directory);
			return result;
		}

		public Path withFile(String file) {
			Path result = new Path(this);
			result.setFile(file);
			return result;
		}

		public Path unroot() {
			if(!isRoot()) {
				return this;
			}
			return Path.create(toString().substring(1));
		}

		public Path normalize() {
			String[] segments=
				this.directory==null?
					new String[0]:
					this.directory.equals("/")?
						new String[] {""}:
						this.directory.split("/");
			return Path.create(normalizePath(segments, getFile()));
		}

		/**
		 * Computes relative relative path to reference "target" from "base". Uses ".." if needed, in
		 * contrast to {@link URI#relativize(URI)}.
		 */
		public Path relativize(Path path) {
			if(path==null) {
				throw new NullPointerException("Path cannot be null");
			}

			Path base=this;

			// By default, we return the normalized form of the input path
			Path defaultRelative = path.normalize();

			// Either root or not
			if(base.isRoot()!=path.isRoot()) {
				return defaultRelative;
			}

			// If the path is not root, return its normalized base
			if(!path.isRoot()) {
				return defaultRelative;
			}

			// Beyound this point we need the normalized form
			Path nBase = base.normalize();

			// If we are out of the scope...
			if(base.isOutOfScope()) {
				// ... we must return the input path because it will define the
				// scope
				Path relative=defaultRelative;
				if(nBase.equals(relative)) {
					// ... unless the path is the same as ourselfs, in which
					// case it is safe to return the empty path
					relative=EMPTY_PATH;
				}
				return relative;
			}

			// Can only compare for equality iff normalized.
			Path unBase=nBase.unroot();
			Path unPath=defaultRelative.unroot();

			// If the base and the target are the same, return the empty path
			if(unBase.equals(unPath)) {
				return EMPTY_PATH;
			}

			// Look for index of last common segment
			String[] nBaseSegments=unBase.segments();
			String[] nTargetSegments=unPath.segments();
			int comparableSegments=
				Math.min(
					nBaseSegments.length,
					nTargetSegments.length);
			int commonSegments=0;
			for(int i=0;i<comparableSegments;i++) {
				if(!nBaseSegments[i].equals(nTargetSegments[i])) {
					break;
				}
				commonSegments++;
			}

			// For each different segment of the base path, add '..' to the
			// segments of the relative path
			LinkedList<String> buffer=new LinkedList<String>();
			for(int i=commonSegments;i<nBaseSegments.length;i++) {
				buffer.add("..");
			}

			// Add each different segment of the input path to the segments of
			// the relative path
			for(int i=commonSegments;i<nTargetSegments.length;i++) {
				buffer.add(nTargetSegments[i]);
			}

			// If there are no segments in the resolved path, and we are trying
			// to resolve a directory coming from a path, we have to make
			// explicit that we want the directory
			if(buffer.isEmpty() && path.isDirectory() && base.isFile()) {
				buffer.add(".");
			}
			return Path.create(assemblePath(buffer,path.getFile()));
		}

		public Path resolve(Path path) {
			if(path==null) {
				throw new NullPointerException("Target path cannot be null");
			}
			if(path.isEmpty()) {
				return this.normalize();
			}
			if(path.isRoot()) {
				return path.normalize();
			}
			Path base=normalize();
			Path relative=path.normalize();
			List<String> baseSegments=new ArrayList<String>(Arrays.asList(base.segments()));
			baseSegments.addAll(Arrays.asList(relative.segments()));
			String[] segments = baseSegments.toArray(new String[baseSegments.size()]);
			String resolved=normalizePath(segments,path.getFile());
			return Path.create(resolved);
		}

		@Override
		public int hashCode() {
			return
				13*Objects.hashCode(this.directory)+
				17*Objects.hashCode(this.fileName)+
				19*Objects.hashCode(this.fileExtension);
		}

		@Override
		public boolean equals(Object obj) {
			boolean result = false;
			if(obj instanceof Path) {
				Path that=(Path)obj;
				result=
					Objects.equal(this.directory,that.directory) &&
					Objects.equal(this.fileName,that.fileName) &&
					Objects.equal(this.fileExtension,that.fileExtension);
			}
			return result;
		}

		@Override
		public String toString() {
			StringBuilder builder=new StringBuilder();
			if(this.directory!=null) {
				builder.append(this.directory);
			}
			builder.append(file());
			return builder.toString();
		}
		public static Path create(java.net.URI jdkURI) {
			return create(jdkURI.getPath());
		}

		public static Path create(String path) {
			Path result=null;
			if(path!=null) {
				String directory=null;
				String file=null;
				int lastSegmentSeparator = path.lastIndexOf('/');
				if(lastSegmentSeparator<0) {
					if(path.equals(".")) {
						directory=".";
					} else if(path.equals("..")) {
						directory="..";
					} else {
						file=path;
					}
				} else if(lastSegmentSeparator==path.length()-1) {
					directory=path;
				} else {
					directory=path.substring(0,lastSegmentSeparator+1);
					String segment=path.substring(lastSegmentSeparator+1);
					if(segment.equals(".")) {
						directory+=".";
					} else if(segment.equals("..")) {
						directory+="..";
					} else {
						file=segment;
					}
				}
				result=new Path();
				result.setDirectory(directory);
				result.setFile(file);
			}
			return result;
		}

	}

	private final java.net.URI delegate;

	private URI(java.net.URI delegate) {
		this.delegate = delegate;
	}

	private java.net.URI createDelegate(
			String aScheme,
			String aSchemeSpecificPart,
			String aFragment) {
		StringBuilder builder=new StringBuilder();
		if(aSchemeSpecificPart!=null && !aSchemeSpecificPart.isEmpty()) {
			if(aScheme!=null) {
				builder.append(aScheme).append(":");
			}
			builder.append(aSchemeSpecificPart);
		}
		if(aFragment!=null) {
			builder.append("#").append(aFragment);
		}
		return java.net.URI.create(builder.toString());
	}

	private String buildHierarchicalSchemeSpecificPart(
			String aScheme,
			Authority aAuthority,
			Path aPath,
			String aQuery) {
		StringBuilder builder=new StringBuilder();
		if(aAuthority!=null) {
			builder.append("//").append(aAuthority.toString());
		}
		if(aPath!=null && !aPath.isEmpty()) {
			if(aScheme!=null && aAuthority==null && !aPath.isRoot()) {
				builder.append("/");
			}
			builder.append(aPath.toString());
		}
		if(aQuery!=null) {
			builder.append("?").append(aQuery);
		}
		return builder.toString();
	}

	public String getScheme() {
		return this.delegate.getScheme();
	}

	public String getSchemeSpecificPart() {
		return this.delegate.getSchemeSpecificPart();
	}

	// TODO: Analyze if it is worth caching this object
	public Authority getAuthority() {
		return Authority.create(this.delegate);
	}

	// TODO: Analyze if it is worth caching this object
	public Path getPath() {
		return Path.create(this.delegate);
	}

	public String getQuery() {
		return this.delegate.getQuery();
	}

	public String getFragment() {
		return this.delegate.getFragment();
	}

	public boolean isOpaque() {
		return this.delegate.isOpaque();
	}

	public boolean isAbsolute() {
		return this.delegate.isAbsolute();
	}

	public boolean isAuthoritative() {
		return getAuthority()!=null;
	}

	public boolean isHierarchical() {
		return !isOpaque();
	}

	public boolean isRelative() {
		return !isAbsolute() && !isOpaque();
	}

	public URI normalize() {
		return wrap(this.delegate.normalize());
	}

	public URI resolve(URI target) {
		if(target==null) {
			throw new NullPointerException("URI cannot be null");
		}
		URI base=this;
		if(base.isOpaque() || target.isOpaque()) {
			return target;
		}
		return relativeResolution(base,target);
	}

	public URI relativize(URI target) {
		if(target==null) {
			throw new NullPointerException("URI cannot be null");
		}

		URI base=this;
		if(base.isOpaque() || target.isOpaque()) {
			return target;
		}

		if(!Objects.equal(base.getScheme(),target.getScheme()) ||
			!Objects.equal(base.getAuthority(),target.getAuthority())) {
			return target;
		}

		Path basePath = base.normalize().getPath();
		Path targetPath = target.normalize().getPath();
		Path relativePath = basePath.relativize(targetPath);
		return target.withPath(relativePath).withScheme(null);
	}

	public URL toURL() throws MalformedURLException {
		return URIUtils.toURL(this.delegate);
	}

	public URI withScheme(String scheme) {
		String schemeSpecificPart=null;
		if(isHierarchical()) {
			schemeSpecificPart=
				buildHierarchicalSchemeSpecificPart(
					scheme,
					this.getAuthority(),
					this.getPath(),
					this.getQuery());
		} else {
			schemeSpecificPart=this.getSchemeSpecificPart();
		}
		return
			wrap(
				createDelegate(
					scheme,
					schemeSpecificPart,
					this.getFragment()));
	}

	public URI withSchemeSpecificPart(String schemeSpecificPart) {
		return
			wrap(
				createDelegate(
					this.getScheme(),
					schemeSpecificPart,
					this.getFragment()));
	}

	public URI withAuthority(Authority authority) {
		return
			withSchemeSpecificPart(
				buildHierarchicalSchemeSpecificPart(
					this.getScheme(),
					authority,
					this.getPath(),
					this.getQuery()
				)
			);
	}

	public URI withPath(Path path) {
		return
			withSchemeSpecificPart(
				buildHierarchicalSchemeSpecificPart(
					this.getScheme(),
					this.getAuthority(),
					path,
					this.getQuery()
				)
			);
	}
	public URI withQuery(String query) {
		return
			withSchemeSpecificPart(
				buildHierarchicalSchemeSpecificPart(
					this.getScheme(),
					this.getAuthority(),
					this.getPath(),
					query
				)
			);
	}

	public URI withFragment(String fragment) {
		return
			wrap(
				createDelegate(
					this.getScheme(),
					this.getSchemeSpecificPart(),
					fragment));
	}

	/**
	 * TODO: Verify weird equals/hashCode behaviour (java.net.URI bug)
	 */
	@Override
	public int hashCode() {
		return this.delegate.hashCode();
	}

	/**
	 * TODO: Verify weird equals/hashCode behaviour (java.net.URI bug)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof URI) {
			URI that = (URI)obj;
			result=this.delegate.equals(that.delegate);
		}
		return result;
	}

	@Override
	public String toString() {
		return this.delegate.toString();
	}

	public String prettyPrint() {
		return URI.prettyPrint(this);
	}

	public java.net.URI unwrap() {
		return this.delegate;
	}

	private static URI relativeResolution(URI Base, URI R) {
		URIRef T=URIRef.empty();
		if(Objects.defined(R.getScheme())) {
			T.scheme=R.getScheme();
			T.authority=R.getAuthority();
			T.path=R.getPath().normalize();
			T.query=R.getQuery();
		} else {
			if(Objects.defined(R.getAuthority())) {
				T.authority=R.getAuthority();
				T.path=R.getPath().normalize();
				T.query=R.getQuery();
			} else {
				if(R.getPath().isEmpty()) {
					T.path=Base.getPath().normalize();
					if(Objects.defined(R.getQuery())) {
						T.query=R.getQuery();
					} else {
						T.query=Base.getQuery();
					}
				} else {
					if(R.getPath().isRoot()) {
						T.path=R.getPath().normalize();
					} else {
						Path base=Base.getPath();
						if(base.isEmpty() && !Objects.defined(Base.getAuthority())) {
							base=Path.create("/");
						}
						T.path=base.resolve(R.getPath());
					}
					T.query=R.getQuery();
				}
				T.authority=Base.getAuthority();
			}
			T.scheme=Base.getScheme();
		}
		T.fragment=R.getFragment();
		return T.toURI();
	}

	private static String prettyPrint(URI uri) {
		StringWriter writer = new StringWriter();
		PrintWriter out=new PrintWriter(writer);
		out.printf("URI(%s) {%n",uri);
		out.printf("\t- Scheme..............: %s%n",uri.getScheme());
		out.printf("\t- Scheme specific part: %s%n",uri.getSchemeSpecificPart());
		if(uri.isHierarchical()) {
			if(uri.isAuthoritative()) {
				Authority authority = uri.getAuthority();
				out.printf("\t  + Authority.........: %s%n",authority);
				out.printf("\t    * User info.......: %s%n",authority.getUserInfo());
				out.printf("\t    * Host............: %s%n",authority.getHost());
				String port=Integer.toString(authority.getPort());
				if(authority.isDefaultSchemePort()) {
					port="DEFAULT PROTOCOL PORT";
				}
				out.printf("\t    * Port............: %s%n",port);
			}
			Path path = uri.getPath();
			if(!path.isEmpty()) {
				out.printf("\t  + Path..............: %s%n",path);
				out.printf("\t    * Directory.......: %s%n",path.getDirectory());
				out.printf("\t    * File............: %s%n",path.getFile());
				out.printf("\t      - Name..........: %s%n",path.getFileName());
				out.printf("\t      - Extension.....: %s%n",path.getFileExtension());
				out.printf("\t    * Flags...........: %s%n",pathFlags(path));
			}
			out.printf("\t  + Query.............: %s%n",uri.getQuery());
		}
		out.printf("\t- Fragment............: %s%n",uri.getFragment());
		out.printf("\t- Flags...............: %s%n",uriFlags(uri));
		out.printf("}");
		out.flush();
		writer.flush();
		return writer.toString();
	}

	private static String uriFlags(URI uri) {
		StringBuilder builder=new StringBuilder();
		if(uri.isOpaque()) {
			builder.append("[Opaque]");
		}
		if(uri.isHierarchical()) {
			builder.append("[Hierarchical]");
			if(uri.isAuthoritative()) {
				builder.append("[Authoritative]");
			} else {
				builder.append("[Not-Authorit.]");
			}
			if(uri.isAbsolute()) {
				builder.append("[Absolute]");
			} else {
				builder.append("[Relative]");
			}
		}
		return builder.toString();
	}
	private static String pathFlags(Path path) {
		StringBuilder builder=new StringBuilder();
		if(path.isEmpty()) {
			builder.append("[Empty]");
		} else {
			if(path.isRoot()) {
				builder.append("[Root]");
			}
			if(path.isDirectory()) {
				builder.append("[Directory]");
			} else {
				builder.append("[File]");
			}
		}
		return builder.toString();
	}


	public static URI wrap(java.net.URI delegate) {
		return new URI(delegate);
	}

	public static URI create(String string) {
		return wrap(java.net.URI.create(string));
	}

}
