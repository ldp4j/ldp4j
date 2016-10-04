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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-api:0.2.2
 *   Bundle      : rmf-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.URIRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class RDFOperations {

	private static final Logger LOGGER=LoggerFactory.getLogger(RDFOperations.class);

	private static final String TRIPLES_PARAM = "Triples cannot be null";
	private static final String OLD_NODE_PARAM = "Old node cannot be null";
	private static final String NEW_NODE_PARAM = "New node cannot be null";

	private static final String NL = System.lineSeparator();

	private abstract static class AbstractReplacer implements ITripleTransformation {

		@Override
		public final Triple transform(Triple t) {
			return
				new Triple(
					update(t.getSubject(),Resource.class),
					update(t.getPredicate(),URIRef.class),
					update(t.getObject(),Node.class)
				);
		}

		protected abstract <T extends Node> T update(T original, Class<T> clazz);

	}

	private static class NodeReplacer<O extends Node, N extends Node> extends AbstractReplacer{

		private final N newNode;
		private final O replacedNode;

		public NodeReplacer(O replacedNode, N newNode) {
			this.replacedNode = replacedNode;
			this.newNode = newNode;
		}

		protected final O getReplacedNode() {
			return replacedNode;
		}

		@Override
		protected <T extends Node> T update(T original, Class<T> clazz) {
			T result=original;
			if(clazz.isInstance(newNode) &&
				clazz.isInstance(getReplacedNode()) &&
				original.equals(getReplacedNode())) {
				result=clazz.cast(newNode);
			}
			return result;
		}
	}

	private static class MultiNodeReplacer extends AbstractReplacer{

		private final Map<Node,Node> replacements;

		public MultiNodeReplacer(Map<Node,Node> replacements) {
			this.replacements=replacements;
		}

		@Override
		protected <T extends Node> T update(T original, Class<T> clazz) {
			T result=original;
			Node value = replacements.get(original);
			if(value!=null && clazz.isInstance(value)) {
				result=clazz.cast(value);
			}
			return result;
		}
	}

	private RDFOperations() {
	}

	private static ITripleTransformation newNodeReplacer(Node replacedNode, Node newNode) {
		return new NodeReplacer<Node, Node>(replacedNode, newNode);
	}

	private static ITripleTransformation newNodeReplacer(Map<Node,Node> replacements) {
		return new MultiNodeReplacer(replacements);
	}

	private static ITripleTransformation newNodeReplacer(Map<Node,Node> replacements, boolean resolvable) {
		Map<Node,Node> targetReplacements=replacements;
		if(resolvable) {
			targetReplacements=compact(replacements);
		}
		return newNodeReplacer(targetReplacements);
	}
	private static void trace(String format, Object... args) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(String.format(format,args));
		}
	}

	private static Map<Node, Node> compact(Map<Node, Node> replacements) {
		Set<Node> nodes=replacements.keySet();
		Collection<Node> values=replacements.values();
		Set<Node> rewrites=new HashSet<Node>(nodes);
		rewrites.retainAll(values);
		trace("- Replacements...: "+replacements);
		trace("- Rewriting nodes: "+rewrites);
		Map<Node, Node> result=new HashMap<Node,Node>();
		for(Node node:nodes) {
			trace("- Compacting '%s'...",node);
			Node newNode=resolve(1,node, replacements, rewrites, result);
			trace("- Compacted '%s' to '%s' from '%s'...",node,newNode,replacements.get(node));
			result.put(node, newNode);
		}
		return result;
	}

	private static String indent(int i) {
		char[] value = new char[2*i];
		Arrays.fill(value, ' ');
		return new String(value);
	}

	private static Node resolve(int i, Node node, Map<Node, Node> original, Set<Node> rewrites, Map<Node, Node> compacted) {
		Node newNode=original.get(node);
		if(rewrites.contains(newNode)) {
			Node tmp=compacted.get(newNode);
			if(tmp==null) {
				String prefix=indent(i);
				trace("%s+ Resolving rewrite '%s' for node '%s'...",prefix,newNode,node);
				tmp=resolve(i+1,newNode,original,rewrites,compacted);
				trace("%s+ Rewrite '%s' resolved to '%s'",prefix,newNode,tmp);
				compacted.put(newNode, tmp);
			}
			newNode=tmp;
		}
		return newNode;
	}

	public static TripleSet toTripleSet(Triple... original) {
		TripleSet result=new TripleSet();
		result.add(original);
		return result;
	}

	public static <T extends Iterable<Triple>> TripleSet toTripleSet(T original) {
		if(original instanceof TripleSet) {
			return (TripleSet)original;
		}
		TripleSet result=new TripleSet();
		result.add(original);
		return result;
	}

	public static TripleSet union(Triple[] original, Triple... triples) {
		TripleSet result=toTripleSet(original);
		result.add(triples);
		return result;
	}

	public static <T extends Iterable<Triple>> TripleSet union(Triple[] original, T triples) {
		TripleSet result=toTripleSet(original);
		result.add(triples);
		return result;
	}

	public static <T extends Iterable<Triple>> TripleSet union(T original, Triple... triples) {
		TripleSet result=toTripleSet(original);
		result.add(triples);
		return result;
	}

	public static <T extends Iterable<Triple>, S extends Iterable<Triple>> TripleSet union(T original, S triples) {
		TripleSet result = toTripleSet(original);
		result.add(triples);
		return result;
	}

	public static <T extends Iterable<Triple>> InmutableTripleSet transform(T triples, ITripleTransformation tripleTransformation) {
		Objects.requireNonNull(triples,TRIPLES_PARAM);
		Objects.requireNonNull(tripleTransformation,"Triple transformation cannot be null");
		TripleSet result=new TripleSet();
		for(Triple t:triples) {
			Triple newTriple = tripleTransformation.transform(t);
			if(newTriple!=null) {
				result.add(newTriple);
			}
		}
		return new InmutableTripleSet(result);
	}

	public static <T extends Iterable<Triple>> InmutableTripleSet replace(T triples, Node oldNode, Node newNode) {
		Objects.requireNonNull(oldNode,OLD_NODE_PARAM);
		Objects.requireNonNull(newNode,NEW_NODE_PARAM);
		return transform(triples, newNodeReplacer(oldNode,newNode));
	}

	public static <T extends Iterable<Triple>> InmutableTripleSet replace(T triples, Map<Node,Node> replacements) {
		Objects.requireNonNull(replacements,NEW_NODE_PARAM);
		return transform(triples, newNodeReplacer(replacements,true));
	}

	public static <T extends Iterable<Triple>> String toString(T triples) {
		Objects.requireNonNull(triples,TRIPLES_PARAM);
		StringBuilder builder = new StringBuilder();
		builder.append("Triples {");
		for(Triple t:triples) {
			builder.append(NL).append("\t").append(t);
		}
		builder.append(NL).append("}");
		return builder.toString();
	}

}