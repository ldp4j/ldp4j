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
package org.ldp4j.net;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.ldp4j.commons.net.URIUtils;

public final class URI {

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

	public Authority getAuthority() {
		return Authority.create(this.delegate);
	}

	// TODO: Analyze if it is worth caching the path
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
		Objects.requireNonNull(target,"Target URI cannot be null");
		URI base=this;
		if(areOpaque(base, target)) {
			return target;
		}
		return relativeResolution(base,target);
	}

	public URI relativize(URI uri) {
		Objects.requireNonNull(uri,"Target URI cannot be null");
		URI base=this;
		URI target=uri.normalize();
		if(areRelativizable(base,target)) {
			Path relativePath=
				calculateRelativePath(
					base.normalize().getPath(),
					target.getPath());
			if(relativePath!=null) {
				target=
					uri.
						withAuthority(null).
						withScheme(null).
						withPath(relativePath);
			}
		}
		return target;
	}

	private boolean areRelativizable(URI base, URI target) {
		return
			!areOpaque(base, target) &&
			haveSameScheme(base, target) &&
			belongToSameAuthority(base, target);
	}

	private boolean areOpaque(URI base, URI target) {
		return base.isOpaque() || target.isOpaque();
	}

	private boolean belongToSameAuthority(URI base, URI target) {
		return Objects.equals(base.getAuthority(),target.getAuthority());
	}

	private boolean haveSameScheme(URI base, URI target) {
		return Objects.equals(base.getScheme(),target.getScheme());
	}

	private Path calculateRelativePath(Path basePath, Path targetPath) {
		Path relativePath = basePath.relativize(targetPath);
		if(relativePath.isEmpty() && !targetPath.isRoot()) {
			if(basePath.isRoot()) {
				relativePath=null;
			} else if(basePath.isFile()) {
				relativePath=Path.create(basePath.getFile());
			}
		}
		return relativePath;
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
	 * TODO: Verify weird equals/hashCode behavior (java.net.URI bug)
	 */
	@Override
	public int hashCode() {
		return this.delegate.hashCode();
	}

	/**
	 * TODO: Verify weird equals/hashCode behavior (java.net.URI bug)
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

	private static boolean defined(Object value) {
		return value!=null;
	}

	private static URI relativeResolution(URI base, URI relative) {
		URIRef ref=URIRef.empty();
		if(URI.defined(relative.getScheme())) {
			ref.scheme=relative.getScheme();
			ref.authority=relative.getAuthority();
			ref.path=relative.getPath().normalize();
			ref.query=relative.getQuery();
		} else {
			if(URI.defined(relative.getAuthority())) {
				ref.authority=relative.getAuthority();
				ref.path=relative.getPath().normalize();
				ref.query=relative.getQuery();
			} else {
				resolvePathOnlyTarget(base, relative, ref);
			}
			ref.scheme=base.getScheme();
		}
		ref.fragment=relative.getFragment();
		return ref.toURI();
	}

	private static void resolvePathOnlyTarget(URI base, URI relative, URIRef ref) {
		if(relative.getPath().isEmpty()) {
			ref.path=base.getPath().normalize();
			if(URI.defined(relative.getQuery())) {
				ref.query=relative.getQuery();
			} else {
				ref.query=base.getQuery();
			}
		} else {
			if(relative.getPath().isRoot()) {
				ref.path=relative.getPath().normalize();
			} else {
				Path pBase=base.getPath();
				if(pBase.isEmpty()) {
					pBase=Path.create("/");
				}
				ref.path=pBase.resolve(relative.getPath());
			}
			ref.query=relative.getQuery();
		}
		ref.authority=base.getAuthority();
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
		out.print("}");
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
