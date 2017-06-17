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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.util.ListBuilder;
import org.ldp4j.util.MapBuilder;

public final class Examples {

	private Examples() {
	}

	public static final class Custom {

		private static final List<? extends String> PATHS =
			ListBuilder.
				<String>builder().
					add("/a/b/c/d/f").
					add("/a/b/c/d/.").
					add("/a/b/c/d/..").
					add("/a/b/c/d/./f").
					add("/a/b/c/d/../f").
					add("/a/b/./c/d/./f").
					add("/a/../b/c/d/../f").
					add("a/b/c/d/f").
					add("a/b/c/d/.").
					add("a/b/c/d/..").
					add("a/b/c/d/./f").
					add("a/b/c/d/../f").
					add("a/b/./c/d/./f").
					add("a/../b/c/d/../f").
					add("../f").
					add("../d/").
					add("/../f").
					add("/../d/").
					build();

		private static final List<? extends String> URIS =
			ListBuilder.
				<String>builder().
					add("http://www.example.org").
					add("http://www.example.org:8080").
					add("http://user:password@www.example.org").
					add("http://user:password@www.example.org:8080").
					add("http://www.example.org/").
					add("http://www.example.org/.").
					add("http://www.example.org/..").
					add("http://www.example.org/file").
					add("http://www.example.org/file.").
					add("http://www.example.org/file.ext").
					add("http://www.example.org/.ext").
					add("http://www.example.org/directory/subdirectory/").
					add("http://www.example.org/directory/subdirectory/.").
					add("http://www.example.org/directory/subdirectory/..").
					add("http://www.example.org/directory/subdirectory/file").
					add("http://www.example.org/directory/subdirectory/file.").
					add("http://www.example.org/directory/subdirectory/file.ext").
					add("http://www.example.org/directory/subdirectory/.ext").
					add("//www.example.org").
					add("//www.example.org:8080").
					add("//user:password@www.example.org").
					add("//user:password@www.example.org:8080").
					add("//www.example.org/").
					add("//www.example.org/.").
					add("//www.example.org/..").
					add("//www.example.org/file").
					add("//www.example.org/file.").
					add("//www.example.org/file.ext").
					add("//www.example.org/.ext").
					add("//www.example.org/directory/subdirectory/").
					add("//www.example.org/directory/subdirectory/file").
					add("//www.example.org/directory/subdirectory/file.").
					add("//www.example.org/directory/subdirectory/file.ext").
					add("//www.example.org/directory/subdirectory/.ext").
					add("").
					add("/").
					add("/.").
					add("/..").
					add("/file").
					add("/file.").
					add("/file.ext").
					add("/.ext").
					add("/directory/subdirectory/").
					add("/directory/subdirectory/file").
					add("/directory/subdirectory/file.").
					add("/directory/subdirectory/file.ext").
					add("/directory/subdirectory/.ext").
					add(".").
					add("..").
					add("file").
					add("file.").
					add("file.ext").
					add(".ext").
					add("directory/subdirectory/").
					add("directory/subdirectory/file").
					add("directory/subdirectory/file.").
					add("directory/subdirectory/file.ext").
					add("directory/subdirectory/.ext").
					add("urn:opaque:path").
					build();

		private Custom() {
		}

		public static List<String> paths() {
			return Collections.unmodifiableList(PATHS);
		}

		public static List<String> uris() {
			return Collections.unmodifiableList(URIS);
		}

		public static final class Resolution {

			public static final class Variant {

				private final String alternativeScheme;
				private final String alternativeAuthority;

				private Variant(String alternativeScheme, String alternativeAuthority) {
					this.alternativeScheme = alternativeScheme;
					this.alternativeAuthority = alternativeAuthority;
				}

				protected String base() {
					return modify(Examples.Normative.Resolution.base());
				}

				private String modify(final String uri) {
					String result=uri;
					if(alternativeScheme!=null) {
						if(!alternativeScheme.isEmpty()) {
							result=result.replace("http", alternativeScheme);
						} else {
							result=result.replace("http"+":", "");
						}
					}
					if(alternativeAuthority!=null) {
						if(!alternativeAuthority.isEmpty()) {
							result=result.replace("//a", "//"+alternativeAuthority);
						} else {
							result=result.replace("//a", "");
						}
					}
					return result;
				}

				protected Map<String,String> examples() {
					Map<String, String> regular = new LinkedHashMap<String, String>();
					for(Entry<String,String> example:Examples.Normative.Resolution.regular().entrySet()) {
						regular.put(example.getKey(), modify(example.getValue()));
					}
					return regular;
				}

				public static final Variant RELATIVE=new Variant("","");
				public static final Variant NO_AUTHORITY=new Variant("file","");

			}

			private Resolution() {
			}

			public static String base(Variant variant) {
				return variant.base();
			}

			public static Map<String,String> scenarios(Variant variant) {
				return variant.examples();
			}

		}

	}

	public static final class Normative {

		private Normative() {
		}

		public static final class Resolution {

			private static final String DEFAULT_BASE = "http://a/b/c/d;p?q";

			private static final Map<String,String> REGULAR =
				MapBuilder.
					<String,String>builder().
						add("g:h"           ,  "g:h").
						add("g"             ,  "http://a/b/c/g").
						add("./g"           ,  "http://a/b/c/g").
						add("g/"            ,  "http://a/b/c/g/").
						add("/g"            ,  "http://a/g").
						add("//g"           ,  "http://g").
						add("?y"            ,  "http://a/b/c/d;p?y").
						add("g?y"           ,  "http://a/b/c/g?y").
						add("#s"            ,  "http://a/b/c/d;p?q#s").
						add("g#s"           ,  "http://a/b/c/g#s").
						add("g?y#s"         ,  "http://a/b/c/g?y#s").
						add(";x"            ,  "http://a/b/c/;x").
						add("g;x"           ,  "http://a/b/c/g;x").
						add("g;x?y#s"       ,  "http://a/b/c/g;x?y#s").
						add(""              ,  DEFAULT_BASE).
						add("."             ,  "http://a/b/c/").
						add("./"            ,  "http://a/b/c/").
						add(".."            ,  "http://a/b/").
						add("../"           ,  "http://a/b/").
						add("../g"          ,  "http://a/b/g").
						add("../.."         ,  "http://a/").
						add("../../"        ,  "http://a/").
						add("../../g"       ,  "http://a/g").
						build();

			private static final Map<String, String> ABNORMAL_DOT_SEGMENTS_IN_NAMES =
				MapBuilder.
					<String,String>builder().
						add("/./g"   ,  "http://a/g"      ).
						add("/../g"  ,  "http://a/../g"      ).
						add("g."     ,  "http://a/b/c/g." ).
						add(".g"     ,  "http://a/b/c/.g" ).
						add("g.."    ,  "http://a/b/c/g..").
						add("..g"    ,  "http://a/b/c/..g").
						build();

			private static final Map<String,String> ABNORMAL_OUT_OF_SCOPE=
				MapBuilder.
					<String,String>builder().
						add("../../../g"     ,  "http://a/../g").
						add("../../../../g"  ,  "http://a/../../g").
						build();

			private static final Map<String,String> ABNORMAL_UNNECESSARY_DOT_SEGMENTS =
				MapBuilder.
					<String,String>builder().
						add("./../g"        ,  "http://a/b/g"        ).
						add("./g/."         ,  "http://a/b/c/g/"     ).
						add("g/./h"         ,  "http://a/b/c/g/h"    ).
						add("g/../h"        ,  "http://a/b/c/h"      ).
						add("g;x=1/./y"     ,  "http://a/b/c/g;x=1/y").
						add("g;x=1/../y"    ,  "http://a/b/c/y"      ).
						build();

			private static final Map<String,String> ABNORMAL_NOT_ISOLATED_PATH =
				MapBuilder.
					<String,String>builder().
						add("g?y/./x"  , "http://a/b/c/g?y/./x" ).
						add("g?y/../x" , "http://a/b/c/g?y/../x").
						add("g#s/./x"  , "http://a/b/c/g#s/./x" ).
						add("g#s/../x" , "http://a/b/c/g#s/../x").
						build();

			private Resolution() {
			}

			public static String base() {
				return DEFAULT_BASE;
			}

			public static Map<String,String> regular() {
				return Collections.unmodifiableMap(REGULAR);
			}

			public static Map<String,String> outOfScope() {
				return Collections.unmodifiableMap(ABNORMAL_OUT_OF_SCOPE);
			}

			public static Map<String, String> dotSegmentAsPartOfComponent() {
				return Collections.unmodifiableMap(ABNORMAL_DOT_SEGMENTS_IN_NAMES);
			}

			public static Map<String, String> unnecessaryDotSegments() {
				return Collections.unmodifiableMap(ABNORMAL_UNNECESSARY_DOT_SEGMENTS);
			}

			public static Map<String, String> notIsolatedPath() {
				return Collections.unmodifiableMap(ABNORMAL_NOT_ISOLATED_PATH);
			}

		}

		public static final class Relativization {
			private Relativization() {
			}
		}
	}
}