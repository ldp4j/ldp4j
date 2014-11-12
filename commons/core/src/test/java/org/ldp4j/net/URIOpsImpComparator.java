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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ldp4j.commons.net.URIUtils;
import org.ldp4j.net.URI.Path;
import org.ldp4j.util.ListBuilder;

public final class URIOpsImpComparator {

	public static abstract class RoundtripResolutionTester<T> {

		enum Stage {
			RELATIVIZATION("Relativization"),
			RESOLUTION("Resolution"),
			PRE_VERIFICATION("Expected result generation"),
			;
			private String description;

			private Stage(String description) {
				this.description = description;
			}
		}
		private Map<String,String> failures=new LinkedHashMap<String,String>();
		private int passes=0;
		private int total=0;
		private int errors=0;

		protected abstract T expected(T base, T target);
		protected abstract T relativize(T base, T target);
		protected abstract T resolve(T base, T target);
		protected abstract T wrap(String value);

		protected boolean verify(T resolved, T expected) {
			return resolved.equals(expected);
		}

		@SuppressWarnings("unchecked")
		public void test(String rawBase, String rawTarget) {
			T base=wrap(rawBase);
			T target=wrap(rawTarget);
			try {
				T relative=relativize(base,target);
				try {
					T resolved=resolve(base,relative);
					try {
						T expected=expected(base,target);
						if(verify(resolved, expected)) {
							passTest();
						} else {
							showTest(base,target,relative,resolved,expected);
							failTest(base,target,resolved);
						}
					} catch (RuntimeException e) {
						registerFailure(base,target,e,Stage.PRE_VERIFICATION,relative,resolved);
					}
				} catch (RuntimeException e) {
					registerFailure(base,target,e,Stage.RESOLUTION,relative);
				}
			} catch (RuntimeException e) {
				registerFailure(base,target,e,Stage.RELATIVIZATION);
			}
		}

		public void showStatistics() {
			System.out.printf("%s Tests:%n",getClass().getSimpleName());
			System.out.printf(" - Total: %d%n",total);
			System.out.printf("   + OK...: %d (%.3f%%) %n",passes,percent(passes,total));
			int invalidErrors = failures.size()-errors;
			if(!failures.isEmpty()) {
				System.out.printf("   + ERROR: %d (%.3f%%) %n",failures.size(),percent(failures.size(),total));
				System.out.printf("     * INVALID: %d (%.3f%%) [%.3f%%] %n",invalidErrors,percent(invalidErrors,failures.size()),percent(invalidErrors,total));
				System.out.printf("     * FAILURE: %d (%.3f%%) [%.3f%%] %n",errors,percent(errors,failures.size()),percent(errors,total));
			}
		}

		private void registerFailure(T base, T target, RuntimeException e, Stage stage, T... prev) {
			errors++;
			showFail(base,target,e,stage,prev);
			failTest(base,target,stage.description+" failed: "+e.getMessage());
		}

		private void showFail(T base, T target, RuntimeException e, Stage stage, T... prev) {
			switch(stage) {
			case RELATIVIZATION:
				System.out.printf("[%s] <%s> : <%s> --[REL]--> ERROR",getClass().getSimpleName(),base,target);
				break;
			case RESOLUTION:
				System.out.printf("[%s] <%s> : <%s> --[REL]--> <%s> --[RES] --> ERROR",getClass().getSimpleName(),base,target,prev[0]);
				break;
			case PRE_VERIFICATION:
				System.out.printf("[%s] <%s> : <%s> --[REL]--> <%s> --[RES] --> <%s> ?? ERROR",getClass().getSimpleName(),base,target,prev[0],prev[1]);
				break;
			default:
				break;
			}
			System.out.printf(" (%s)%n",e.getMessage());
		}

		private void showTest(T base, T target, T... parts) {
			System.out.printf("[%s] <%s> : <%s> --[REL]--> <%s> --[RES]--> %s != %s%n",getClass().getSimpleName(),base,target,parts[0],parts[1],parts[2]);
		}

		private void passTest() {
			total++;
			passes++;
		}

		private void failTest(T base, T target, Object result) {
			total++;
			failures.put(base.toString()+" : "+target.toString(), result.toString());
		}

		private static float percent(int passes, int total) {
			return ((float)passes)/((float)total)*(float)100;
		}

	}

	private static class NativeTester extends RoundtripResolutionTester<java.net.URI> {

		@Override
		protected java.net.URI expected(java.net.URI base, java.net.URI target) {
			java.net.URI expected = target.normalize();
			if(!target.getPath().startsWith("/")) {
				expected=base.resolve(target);
			}
			return expected;
		}

		@Override
		protected boolean verify(java.net.URI resolved, java.net.URI expected) {
			return super.verify(resolved.normalize(), expected);
		}

		@Override
		protected java.net.URI relativize(java.net.URI base, java.net.URI target) {
			return base.relativize(target);
		}

		@Override
		protected java.net.URI resolve(java.net.URI base, java.net.URI target) {
			return base.resolve(target);
		}

		@Override
		protected java.net.URI wrap(String value) {
			return java.net.URI.create(value);
		}

	}

	private static class PathTester extends RoundtripResolutionTester<Path> {

		@Override
		protected Path expected(Path base, Path target) {
			Path expected = target.normalize();
			if(!target.isRoot()) {
				expected=base.resolve(target);
			}
			return expected;
		}

		@Override
		protected Path relativize(Path base, Path target) {
			return base.relativize(target);
		}

		@Override
		protected Path resolve(Path base, Path target) {
			return base.resolve(target);
		}

		@Override
		protected Path wrap(String value) {
			return Path.create(value);
		}

	}

	private static class URIUtilsTester extends NativeTester {

		@Override
		protected java.net.URI relativize(java.net.URI base, java.net.URI target) {
			return URIUtils.relativize(base,target);
		}

		@Override
		protected java.net.URI resolve(java.net.URI base, java.net.URI target) {
			return URIUtils.resolve(base,target);
		}

	}


	public static void main(String[] args) {
		List<String> pathScenarios =
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
		crossTest(pathScenarios);
	}

	private static void crossTest(List<String> pathScenarios) {
		URIUtilsTester uTester=new URIUtilsTester();
		NativeTester nTester=new NativeTester();
		PathTester pTester=new PathTester();
		for(String rawBase:pathScenarios) {
			for(String rawTarget:pathScenarios) {
				nTester.test(rawBase, rawTarget);
				uTester.test(rawBase, rawTarget);
				pTester.test(rawBase, rawTarget);
			}
		}
		nTester.showStatistics();
		uTester.showStatistics();
		pTester.showStatistics();
	}

	@SuppressWarnings("unused")
	private static String decorate(String rawUri, boolean withQuery, boolean withFragment) {
		StringBuilder builder=new StringBuilder();
		builder.append(rawUri);
		if(withQuery) {
			builder.append("?query=value");
		}
		if(withFragment) {
			builder.append("#fragment");
		}
		return builder.toString();
	}

}
