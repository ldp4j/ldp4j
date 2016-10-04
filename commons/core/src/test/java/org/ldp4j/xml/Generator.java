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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class Generator {

	private static final int MAX_STEPS = 9;
	private final boolean[] logStep;

	Generator() {
		this.logStep=new boolean[MAX_STEPS];
		doNotLogAnyStep();
	}

	public Generator logAllSteps() {
		Arrays.fill(this.logStep, true);
		return this;
	}

	public Generator doNotLogAnyStep() {
		Arrays.fill(this.logStep, false);
		return this;
	}

	public Generator logStep(final int step) {
		return updateConfig(step, true);
	}

	public Generator doNotLogStep(final int step) {
		return updateConfig(step,false);
	}

	private Generator updateConfig(final int step, final boolean value) {
		if(step>=0 && step<MAX_STEPS) {
			this.logStep[step]=value;
		}
		return this;
	}

	public String generate(final String methodName, final String data) {
		final String[] steps=new String[MAX_STEPS];
		generate(methodName, data, steps);
		for(int i=0;i<MAX_STEPS;i++) {
			if(this.logStep[i]) {
				System.out.printf("Step %d: %s%n",i,steps[i]);
			}
		}
		return steps[MAX_STEPS-1];
	}

	private void generate(final String methodName, final String data, final String[] steps) {
		steps[0] = data;
		steps[1] = steps[0].replace("#x", "codePoint == 0x");
		steps[2] = steps[1].replace("[codePoint ==", "(codePoint >=");
		steps[3] = steps[2].replace("-codePoint ==", " && codePoint <=");
		steps[4] = normalize(steps[3]);
		steps[5] = steps[4].replace("]", ")");
		steps[6] = steps[5].replace("|", "||\n\t");
		steps[7] = String.format("return\n\t%s;",steps[6]);
		steps[8] =
			String.
				format(
					"public static boolean is%s(final int codePoint) {\n\t%s\n}",
					methodName,
					steps[7].replace("\t", "\t\t"));
	}

	private String normalize(final String string) {
		final List<String> normalized=Lists.newArrayList();
		int max=0;
		for(final String part:Splitter.on('|').split(string)) {
			String normal=part.trim();
			if(normal.startsWith("\"")) {
				normal="codePoint == '"+normal.substring(1,2)+"'";
			} else if(normal.startsWith("[")) {
				final String def=normal.substring(1,normal.length()-1);
				final String[] split=def.split("-");
				normal="(codePoint >= '"+split[0]+"' && codePoint <= '"+split[1]+"')";
			} else if(!normal.contains("codePoint")) {
				normal="is"+normal+"(codePoint)";
			}
			if(normal.contains("==")) {
				normal="("+normal+")";
			}
			normalized.add(normal);
			max=Math.max(max, normal.length());
		}
		final List<String> padded=Lists.newArrayList();
		for(final String normal:normalized) {
			padded.add(Strings.padEnd(normal, max+1, ' '));
		}
		return Joiner.on('|').join(padded).trim();
	}

	public static void main(final String... args) throws IOException {
		final String data = Files.toString(new File(args[0]), Charsets.UTF_8);
		final String methodName = args[1];
		System.out.println(new Generator().generate(methodName, data));
	}

}
