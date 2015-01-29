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

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TypeHierarchyTraversal {

	public enum Relation {
		EXTENDS,
		IMPLEMENTS,
		;
		static Relation opposite(Relation relation) {
			checkNotNull(relation);
			return
				relation.equals(Relation.IMPLEMENTS) ?
					Relation.EXTENDS :
					Relation.IMPLEMENTS;
		}
	}

	public enum Entity {
		CLASS,
		INTERFACE,
		;
		static Entity opposite(Entity entity) {
			checkNotNull(entity);
			return
				entity.equals(Entity.INTERFACE) ?
					Entity.CLASS :
					Entity.INTERFACE;
		}
	}

	public enum SearchStrategy {
		IN_ORDER_DEPTH_FIRST,
		PRE_ORDER_DEPTH_FIRST,
		POST_ORDER_DEPTH_FIRST,
		BREADTH_FIRST
	}

	TypeHierarchyTraversal() {
		// Restrict instantiation
	}

}
