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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.NoSuchElementException;

final class HeaderPartIterator implements Iterator<String> {

	private static final String ERROR_ITERATION_HAS_NOT_STARTED = "Iteration has not started";
	private static final String ERROR_OPERATION_NOT_SUPPORTED   = "Operation not supported";
	private static final String ERROR_NO_MORE_BLOCKS_AVAILABLE  = "No more parts are available in the header";

	private enum State {
		BEFORE_DELIMITER,
		DELIMITER,
		AFTER_DELIMITER;
	}

	private static final class Traversal {

		private enum Action {
			CONTINUE(false),
			ACCEPT(true),
			LEADING_DELIMITER("Leading delimiter found"),
			LEADING_WHITESPACE("Leading whitespace found"),
			TRAILING_WHITESPACE("Trailing whitespace found"),
			DANGLING_BLOCK("Dangling block definition found"),
			TOKEN_MISSING("No token found"),
			DELIMITER_MISSING("No delimiter found"),
			EMPTY_BLOCK("Empty block found"),
			WHITESPACE_BLOCK("Whitespace block found"),
			;

			private final String failure;
			private final boolean terminal;
			private final int progress;

			Action(final String failure) {
				this.failure=failure;
				this.terminal=true;
				this.progress=0;
			}

			Action(final boolean terminal) {
				this.failure=null;
				this.terminal=terminal;
				this.progress=terminal?0:1;
			}

			private boolean isFailure() {
				return this.failure!=null;
			}

			private boolean isTerminal() {
				return isFailure() || this.terminal;
			}

			private String failureMessage() {
				return this.failure;
			}

			private int progress() {
				return this.progress;
			}

		}

		private final int start;
		private final int length;

		private int currentOffset;
		private Action status;

		Traversal(final int start, final int limit) {
			this.start=start;
			this.length = limit;
			this.currentOffset=start;
			this.status=Action.CONTINUE;
		}

		void process(final Action status) {
			this.status=status;
			this.currentOffset+=status.progress();
		}

		boolean canContinue() {
			return !isTerminated() && this.currentOffset<this.length;
		}

		int currentOffset() {
			return this.currentOffset;
		}

		boolean isFirstTraversal() {
			return this.start==0;
		}

		boolean hasAdvanced() {
			return this.start<this.currentOffset;
		}

		boolean isTerminated() {
			return this.status.isTerminal();
		}

		boolean isAccepted() {
			return this.status.isTerminal() && !this.status.isFailure();
		}

		boolean hasFailure() {
			return this.status.isFailure();
		}

		String failure() {
			return this.status.failureMessage();
		}

	}

	private final int length;
	private final String header;

	private Traversal traversal;
	private int partStart;
	private int partEnd;
	private String part;

	private HeaderPartIterator(final String str) {
		this.header=str;
		this.length=str.length();
		this.partStart=0;
		this.partEnd=this.partStart;
		this.part=null;
		this.traversal=continueTraversal();
	}

	@Override
	public boolean hasNext() {
		return this.traversal.isAccepted();
	}

	@Override
	public String next() {
		if(!hasNext()) {
			throw new NoSuchElementException(ERROR_NO_MORE_BLOCKS_AVAILABLE);
		}
		this.partStart=this.traversal.currentOffset();
		this.partEnd=findTokenEnd();
		this.part=this.header.substring(this.partStart,this.partEnd);
		this.traversal=continueTraversal();
		return this.part;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(ERROR_OPERATION_NOT_SUPPORTED);
	}

	String header() {
		return this.header;
	}

	String part() {
		checkState(this.partStart<this.partEnd,ERROR_ITERATION_HAS_NOT_STARTED);
		return this.part;
	}

	int startsAt() {
		checkState(this.partStart<this.partEnd,ERROR_ITERATION_HAS_NOT_STARTED);
		return this.partStart;
	}

	int endsAt() {
		checkState(this.partStart<this.partEnd,ERROR_ITERATION_HAS_NOT_STARTED);
		return this.partEnd;
	}

	boolean hasFailure() {
		return this.traversal.hasFailure();
	}

	String failure() {
		return this.traversal.failure();
	}

	private int findTokenEnd() {
		int offset=this.partStart;
		while(offset<this.length) {
			final char lastChar=this.header.charAt(offset);
			if(HttpUtils.isWhitespace(lastChar) || HttpUtils.isParameterDelimiter(lastChar)) {
				break;
			}
			offset++;
		}
		return offset;
	}

	private Traversal continueTraversal() {
		final Traversal next=new Traversal(this.partEnd,this.length);
		State state = traverse(next);
		verifyTermination(next, state);
		return next;
	}

	private void verifyTermination(final Traversal next, State state) {
		if(!next.isTerminated()) {
			if(next.hasAdvanced()) {
				next.process(
					State.BEFORE_DELIMITER.equals(state)?
						// No delimiter has been found yet...
						Traversal.Action.TRAILING_WHITESPACE:
						// Delimiter found, we were awaiting for the next block...
						Traversal.Action.DANGLING_BLOCK);
			} else if(next.isFirstTraversal()) {
				next.process(Traversal.Action.TOKEN_MISSING);
			}
		}
	}

	private State traverse(final Traversal next) {
		State state=State.BEFORE_DELIMITER;
		while(next.canContinue()) {
			final char lastChar=this.header.charAt(next.currentOffset());
			if(State.BEFORE_DELIMITER.equals(state)) {
				state=beforeDelimiter(next,lastChar);
			} else if(State.DELIMITER.equals(state)) {
				state=delimiter(next,lastChar);
			} else { // AFTER_DELIMITER
				afterDelimiter(next,lastChar);
			}
		}
		return state;
	}

	private State beforeDelimiter(final Traversal traversal, final char lastChar) {
		State next=State.BEFORE_DELIMITER;
		Traversal.Action action=Traversal.Action.ACCEPT;
		if(HttpUtils.isParameterDelimiter(lastChar)) {
			if(traversal.isFirstTraversal()) {
				action=Traversal.Action.LEADING_DELIMITER;
			} else {
				next=State.DELIMITER;
				action=Traversal.Action.CONTINUE;
			}
		} else if(HttpUtils.isWhitespace(lastChar)) {
			if(traversal.isFirstTraversal()) {
				action=Traversal.Action.LEADING_WHITESPACE;
			} else {
				action=Traversal.Action.CONTINUE;
			}
		} else if(traversal.hasAdvanced()) {
			action=Traversal.Action.DELIMITER_MISSING;
		}
		traversal.process(action);
		return next;
	}

	private State delimiter(final Traversal traversal, final char lastChar) {
		State next=State.DELIMITER;
		Traversal.Action action=Traversal.Action.ACCEPT;
		if(HttpUtils.isParameterDelimiter(lastChar)) {
			action=Traversal.Action.EMPTY_BLOCK;
		} else if(HttpUtils.isWhitespace(lastChar)) {
			next=State.AFTER_DELIMITER;
			action=Traversal.Action.CONTINUE;
		}
		traversal.process(action);
		return next;
	}

	private void afterDelimiter(final Traversal traversal, final char lastChar) {
		Traversal.Action action=Traversal.Action.ACCEPT;
		if(HttpUtils.isParameterDelimiter(lastChar)) {
			action=Traversal.Action.WHITESPACE_BLOCK;
		} else if(HttpUtils.isWhitespace(lastChar)) {
			action=Traversal.Action.CONTINUE;
		}
		traversal.process(action);
	}

	static HeaderPartIterator create(final String header) {
		checkNotNull(header,"Header cannot be null");
		return new HeaderPartIterator(header);
	}

}