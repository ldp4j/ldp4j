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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.vocabulary;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class TermUtils {

	private static final class Context {

		private final StringBuilder builder=new StringBuilder();
		private final List<Character> buffer=new ArrayList<Character>();
		private int mark=0;

		private final void push(char character) {
			builder.append(Character.toUpperCase(character));
		}

		private final void startGroup(int position) {
			mark=position;
		}

		private final void save(char character) {
			buffer.add(character);
		}

		private final void pushGroup() {
			if(mark!=0) {
				builder.append("_");
			}
			int size = buffer.size();
			if(size>1) {
				for(int j=0;j<size-1;j++) {
					builder.append(buffer.get(j));
				}
				builder.append("_");
			}
			builder.append(buffer.get(size-1));
			buffer.clear();
		}

		private final String complete() {
			if(!buffer.isEmpty()) {
				if(mark>0) {
					builder.append("_");
				}
				for(int j=0;j<buffer.size();j++) {
					builder.append(buffer.get(j));
				}
				buffer.clear();
			}
			return builder.toString();
		}
	}

	private abstract static class State {

		private TermUtils.State accept(int position, char character, TermUtils.Context state) {
			if(Character.isUpperCase(character)) {
				return handleUpperCase(position,character,state);
			} else { // Must be Character.isLowerCase(character)
				return handleLowerCase(position,character,state);
			}
		}

		protected abstract TermUtils.State handleUpperCase(int position, char character, TermUtils.Context state);

		protected abstract TermUtils.State handleLowerCase(int position, char character, TermUtils.Context state);

	}

	private static final class Start extends TermUtils.State {
		@Override
		protected TermUtils.State handleUpperCase(int position, char character, TermUtils.Context state) {
			state.startGroup(position);
			state.save(character);
			return new UpperCase();
		}
		@Override
		protected TermUtils.State handleLowerCase(int position, char character, TermUtils.Context state) {
			state.push(character);
			return this;
		}
	}

	private static final class UpperCase extends TermUtils.State {
		@Override
		protected TermUtils.State handleUpperCase(int position, char character, TermUtils.Context state) {
			state.save(character);
			return this;
		}
		@Override
		protected TermUtils.State handleLowerCase(int position, char character, TermUtils.Context state) {
			state.pushGroup();
			state.push(character);
			return new Start();
		}
	}

	private TermUtils() {
	}

	static String toTermName(String string) {
		if(!isValidEntityName(string)) {
			throw new IllegalArgumentException("Object '"+string+"' is not a valid entity name");
		}
		TermUtils.State state=new Start();
		TermUtils.Context context=new Context();
		for(int i=0;i<string.length();i++) {
			state=state.accept(i,string.charAt(i),context);
		}
		return context.complete();

	}

	static boolean isValidTermName(String string) {
		if(string==null) {
			return false;
		}
		return Pattern.matches("^\\p{javaUpperCase}[_\\p{javaUpperCase}]*$", string);
	}

	static boolean isValidEntityName(String string) {
		if(string==null) {
			return false;
		}
		return Pattern.matches("^[\\p{javaLowerCase}\\p{javaUpperCase}]+$", string);
	}

}