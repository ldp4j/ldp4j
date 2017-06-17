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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ldp4j.commons.net.URIUtils;
import org.ldp4j.util.ListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class URIOpsImpComparator {

	private static final Logger LOGGER=LoggerFactory.getLogger(URIOpsImpComparator.class);

	public abstract static class RoundtripResolutionTester<T> {

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

		public void roundtrip(String rawBase, String rawTarget) {
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
			LOGGER.info("{} Tests:",getClass().getSimpleName());
			LOGGER.info(" - Total: {}",total);
			LOGGER.info(String.format("   + OK...: %d (%.3f%%)",passes,percent(passes,total)));
			int invalidErrors = failures.size()-errors;
			if(!failures.isEmpty()) {
				LOGGER.info(String.format("   + ERROR: %d (%.3f%%)",failures.size(),percent(failures.size(),total)));
				LOGGER.info(String.format("     * INVALID: %d (%.3f%%) [%.3f%%]",invalidErrors,percent(invalidErrors,failures.size()),percent(invalidErrors,total)));
				LOGGER.info(String.format("     * FAILURE: %d (%.3f%%) [%.3f%%]",errors,percent(errors,failures.size()),percent(errors,total)));
			}
		}

		@SafeVarargs
		private final void registerFailure(T base, T target, RuntimeException e, Stage stage, T... prev) {
			errors++;
			showFail(base,target,e,stage,prev);
			failTest(base,target,stage.description+" failed: "+e.getMessage());
		}

		@SafeVarargs
		private final void showFail(T base, T target, RuntimeException e, Stage stage, T... prev) {
			String message="";
			switch(stage) {
			case RELATIVIZATION:
				message=String.format("[%s] <%s> : <%s> --[REL]--> ERROR",getClass().getSimpleName(),base,target);
				break;
			case RESOLUTION:
				message=String.format("[%s] <%s> : <%s> --[REL]--> <%s> --[RES] --> ERROR",getClass().getSimpleName(),base,target,prev[0]);
				break;
			case PRE_VERIFICATION:
				message=String.format("[%s] <%s> : <%s> --[REL]--> <%s> --[RES] --> <%s> ?? ERROR",getClass().getSimpleName(),base,target,prev[0],prev[1]);
				break;
			default:
				break;
			}
			LOGGER.trace(message+" ({})",e.getMessage());
		}

		@SafeVarargs
		private final void showTest(T base, T target, T... parts) {
			LOGGER.debug(String.format("[%s] <%s> : <%s> --[REL]--> <%s> --[RES]--> %s != %s",getClass().getSimpleName(),base,target,parts[0],parts[1],parts[2]));
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

	private static class CustomTester extends RoundtripResolutionTester<URI> {

		@Override
		protected URI expected(URI base, URI target) {
			URI expected = target.normalize();
			if(!(target.isHierarchical() && target.getPath().isRoot())) {
				expected=base.resolve(target);
			}
			return expected;
		}

		@Override
		protected URI relativize(URI base, URI target) {
			return base.relativize(target);
		}

		@Override
		protected URI resolve(URI base, URI target) {
			return base.resolve(target);
		}

		@Override
		protected URI wrap(String value) {
			return URI.create(value);
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
					add("/").
					add("").
					build();
		crossTest(pathScenarios);
	}

	private static void crossTest(List<String> pathScenarios) {
		URIUtilsTester uTester=new URIUtilsTester();
		NativeTester nTester=new NativeTester();
		PathTester pTester=new PathTester();
		CustomTester cTester=new CustomTester();
		for(String rawBase:pathScenarios) {
			for(String rawTarget:pathScenarios) {
				nTester.roundtrip(rawBase, rawTarget);
				uTester.roundtrip(rawBase, rawTarget);
				pTester.roundtrip(rawBase, rawTarget);
				cTester.roundtrip(rawBase, rawTarget);
			}
		}
		nTester.showStatistics();
		uTester.showStatistics();
		pTester.showStatistics();
		cTester.showStatistics();
	}

}
