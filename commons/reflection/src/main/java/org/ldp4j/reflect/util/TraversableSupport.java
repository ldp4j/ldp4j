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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-reflection:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-reflection-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.reflect.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.EnumMap;

import org.ldp4j.reflect.util.TypeHierarchyTraversal.Relation;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

final class TraversableSupport implements Traversable {

	private static int DISCOVERY  = 0;
	private static int TRAVERSAL  = 1;
	private static int VISITATION = 2;
	private static int COMPLETION = 3;

	private final EnumMap<Relation,Integer> childTraversalCompletion;
	private final int[] timestamps;
	private int lastTimestamp;

	TraversableSupport() {
		this.childTraversalCompletion=Maps.newEnumMap(Relation.class);
		this.lastTimestamp=-1;
		this.timestamps = new int[4];
		Arrays.fill(timestamps,-1);
	}

	private void checkCanProgress(int timestamp) {
		checkArgument(timestamp>=this.lastTimestamp,"Cannot set past timestamps");
		checkState(isDiscovered(),"Cannot progress before being discovered");
	}

	private void setEventTimestamp(int timestamp, int event) {
		this.timestamps[event]=timestamp;
		this.lastTimestamp=timestamp;
	}

	private boolean hasEventHappened(int event) {
		return this.timestamps[event]>=0;
	}

	private void setVisitation(int timestamp) {
		setEventTimestamp(timestamp, TraversableSupport.VISITATION);
		refreshCompletion(timestamp);
	}

	private void setChildTraversalCompletion(Relation entity, int timestamp) {
		this.childTraversalCompletion.put(entity,timestamp);
		this.lastTimestamp=timestamp;
		if(areChildrenTraversed(Relation.IMPLEMENTS) && areChildrenTraversed(Relation.EXTENDS)) {
			setEventTimestamp(timestamp, TraversableSupport.TRAVERSAL);
			refreshCompletion(timestamp);
		}
	}

	private void refreshCompletion(int timestamp) {
		if(isVisited() && isTraversed()) {
			setEventTimestamp(timestamp, TraversableSupport.COMPLETION);
		}
	}

	private int normalize(Integer timestamp) {
		return timestamp!=null?timestamp.intValue():-1;
	}

	void discoveredAt(int timestamp) {
		checkState(!isDiscovered(),"Element has already been discovered");
		setEventTimestamp(timestamp, TraversableSupport.DISCOVERY);
	}

	void visitedAt(int timestamp) {
		checkCanProgress(timestamp);
		checkState(!isVisited(),"Element has already been visited");
		setVisitation(timestamp);
	}

	void childrenTraversedAt(Relation entity, int timestamp) {
		checkNotNull(entity);
		checkCanProgress(timestamp);
		checkState(!areChildrenTraversed(entity),"%s children have already been traversed",entity);
		setChildTraversalCompletion(entity,timestamp);
	}

	@Override
	public boolean isDiscovered() {
		return hasEventHappened(TraversableSupport.DISCOVERY);
	}

	@Override
	public boolean isVisited() {
		return hasEventHappened(TraversableSupport.VISITATION);
	}

	@Override
	public boolean areChildrenTraversed(Relation entity) {
		return this.childTraversalCompletion.containsKey(entity);
	}

	@Override
	public boolean isTraversed() {
		return hasEventHappened(TraversableSupport.TRAVERSAL);
	}

	@Override
	public boolean isCompleted() {
		return hasEventHappened(TraversableSupport.COMPLETION);
	}

	@Override
	public int discoveryTimestamp() {
		return this.timestamps[TraversableSupport.DISCOVERY];
	}

	@Override
	public int traversalTimestamp() {
		return this.timestamps[TraversableSupport.TRAVERSAL];
	}

	@Override
	public int childrenTraversalTimestamp(Relation entity) {
		return normalize(this.childTraversalCompletion.get(entity));
	}

	@Override
	public int completionTimestamp() {
		return this.timestamps[TraversableSupport.DISCOVERY];
	}

	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
					add("timestamps",Arrays.toString(this.timestamps)).
					add("childTraversalCompletion",this.childTraversalCompletion).
					toString();
	}

}