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
package org.ldp4j.commons;

public final class ExceptionUtils {

	private static final String NL=System.getProperty("line.separator");

	private ExceptionUtils() {
	}
	
	public static String toString(Throwable t) {
		StringBuilder builder=new StringBuilder();
		printStackTraceAsCause(builder, t, null);
		return builder.toString();
	}

	private static void printStackTraceAsCause(
			StringBuilder builder,
			Throwable cause, 
			StackTraceElement[] causedTrace) {
		// If no cause, finish
		if(cause==null) {
			return;
		}

		// Compute number of frames in common between this and caused
		StackTraceElement[] trace = cause.getStackTrace();
		int m=trace.length-1;
		String heading="";
		String trailer="";
		if(causedTrace!=null) {
			int n=causedTrace.length-1;
			while(m>=0&&n>=0&&trace[m].equals(causedTrace[n])) {
				m--;
				n--;
			}
			int framesInCommon=trace.length-1-m;
			heading="Caused by: ";
			trailer="\t... "+framesInCommon+" more"+NL;
		}
	
		builder.append(heading+display(cause)).append(NL);
		for(int i=0;i<=m;i++) {
			builder.append("\tat "+trace[i]).append(NL);
		}
		builder.append(trailer);
	
		printStackTraceAsCause(builder, cause.getCause(), trace);
	}
	private static String display(Throwable t) {
		String s=t.getClass().getName();
		String message=t.getLocalizedMessage();
		return 
			message!=null?
				s+": "+message:
				s;
	}
	
}
