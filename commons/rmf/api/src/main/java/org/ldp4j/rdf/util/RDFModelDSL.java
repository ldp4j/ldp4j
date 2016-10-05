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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.Datatype;
import org.ldp4j.rdf.Literal;
import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.RDFFactory;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.Triple;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.rdf.spi.RuntimeInstance;
import org.ldp4j.rdf.spi.Transformer;

import javax.xml.namespace.QName;

public final class RDFModelDSL {

	private static final String IDENTITY_PARAM = "Identity cannot be null";
	private static final String TYPE_PARAM     = "Type cannot be null";
	private static final String LANGUAGE_PARAM = "Language cannot be null";
	private static final String VALUE_PARAM    = "Value cannot be null";

	private static final RDFFactory FACTORY = new RDFFactory();

	private RDFModelDSL() {
	}

	public abstract static class AbstractBuilder<T> {

		protected abstract T build();

	}

	private abstract static class AbstractCompositeBuilder<P, T, B extends AbstractBuilder<T>> {

		protected abstract B updateBuilder(P part);

	}

	private abstract static class AbstractCompositeUpdater<T, B extends AbstractBuilder<T>, P> {

		protected abstract B updateComponent(P part);

	}

	private static class CompositeBuilder<P, T, B extends AbstractBuilder<T>> extends AbstractCompositeBuilder<P, T, B> {

		private final AbstractCompositeUpdater<T, B, P> updater;

		protected CompositeBuilder(AbstractCompositeUpdater<T, B, P> updater) {
			this.updater = updater;
		}

		@Override
		protected B updateBuilder(P part) {
			return updater.updateComponent(part);
		}

	}

	public static class ResourceBuilder extends CompositeBuilder<Resource<?>, Triple, TripleBuilder> {

		private RDFFactory factory;

		private ResourceBuilder(
				RDFFactory factory,
				AbstractCompositeUpdater<Triple, TripleBuilder, Resource<?>> updater) {
			super(updater);
			this.factory = factory;
		}

		public TripleBuilder blankNode() {
			return blankNode(factory.newBlankNode());
		}

		public TripleBuilder blankNode(BlankNode bn) {
			return updateBuilder(bn);
		}

		public TripleBuilder uriRef(String uriRef) {
			return uriRef(factory.newURIRef(URI.create(uriRef)));
		}

		public TripleBuilder uriRef(URIRef uriRef) {
			return updateBuilder(uriRef);
		}

	}

	public static final class URIRefBuilder extends CompositeBuilder<URIRef, Triple, TripleBuilder> {

		private RDFFactory factory;

		private URIRefBuilder(RDFFactory factory,
				AbstractCompositeUpdater<Triple, TripleBuilder, URIRef> updater) {
			super(updater);
			this.factory = factory;
		}

		public TripleBuilder uriRef(String uriRef) {
			return uriRef(factory.newURIRef(URI.create(uriRef)));
		}

		public TripleBuilder uriRef(URIRef uriRef) {
			return updateBuilder(uriRef);
		}

	}

	public static final class NodeBuilder extends CompositeBuilder<Node, Triple, TripleBuilder> {

		private RDFFactory factory;

		private NodeBuilder(RDFFactory factory,
				AbstractCompositeUpdater<Triple, TripleBuilder, Node> updater) {
			super(updater);
			this.factory = factory;
		}

		public TripleBuilder blankNode() {
			return blankNode(factory.newBlankNode());
		}

		public TripleBuilder blankNode(String identity) {
			return blankNode(factory.newBlankNode(identity));
		}

		private TripleBuilder blankNode(BlankNode bn) {
			return updateBuilder(bn);
		}

		public TripleBuilder uriRef(String uriRef) {
			return uriRef(factory.newURIRef(URI.create(uriRef)));
		}

		public TripleBuilder uriRef(URIRef uriRef) {
			return updateBuilder(uriRef);
		}

		public <T> TripleBuilder literal(T literal) {
			return literal(factory.newLiteral(literal));
		}

		public <T> TripleBuilder literal(Literal<T> literal) {
			return updateBuilder(literal);
		}
	}

	public static final class TripleBuilder extends AbstractBuilder<Triple> {

		private final RDFFactory factory;

		private Resource<?> subject;
		private URIRef predicate;
		private Node object;

		private TripleBuilder(RDFFactory factory) {
			this.factory = factory;
		}

		protected TripleBuilder(TripleBuilder builder) {
			this.subject = builder.subject;
			this.predicate = builder.predicate;
			this.object = builder.object;
			this.factory = builder.factory;
		}

		public ResourceBuilder withSubject() {
			AbstractCompositeUpdater<Triple, TripleBuilder, Resource<?>> updater = new AbstractCompositeUpdater<Triple, TripleBuilder, Resource<?>>() {
				@Override
				public TripleBuilder updateComponent(Resource<?> part) {
					TripleBuilder.this.subject = part;
					return TripleBuilder.this;
				}
			};
			return new ResourceBuilder(this.factory, updater);
		}

		public URIRefBuilder withPredicate() {
			AbstractCompositeUpdater<Triple, TripleBuilder, URIRef> updater = new AbstractCompositeUpdater<Triple, TripleBuilder, URIRef>() {
				@Override
				public TripleBuilder updateComponent(URIRef part) {
					TripleBuilder.this.predicate = part;
					return TripleBuilder.this;
				}
			};
			return new URIRefBuilder(this.factory, updater);
		}

		public NodeBuilder withObject() {
			AbstractCompositeUpdater<Triple, TripleBuilder, Node> updater = new AbstractCompositeUpdater<Triple, TripleBuilder, Node>() {
				@Override
				public TripleBuilder updateComponent(Node part) {
					TripleBuilder.this.object = part;
					return TripleBuilder.this;
				}
			};
			return new NodeBuilder(this.factory, updater);
		}

		@Override
		protected Triple build() {
			return factory.newTriple(subject, predicate, object);
		}

	}

	private static void processTripleSource(TripleSet result, Object source) {
		if(source instanceof Triple) {
			result.add((Triple) source);
		} else if(source instanceof Iterable<?>) {
			for(Object s:(Iterable<?>) source) {
				processTripleSource(result, s);
			}
		} else if(source instanceof AbstractBuilder<?>) {
			Object s=((AbstractBuilder<?>)source).build();
			processTripleSource(result, s);
		} else {
			Transformer<Object> transformer=
				RuntimeInstance.
					getInstance().
						findTransformer(source.getClass());
			for(Triple triple:transformer.transform(source)) {
				result.add(triple);
			}
		}
	}

	private static Resource<?> asResource(Object object) {
		Resource<?> result=null;
		if(object instanceof Resource) {
			result=(Resource<?>)object;
		} else {
			result=asURIRef(object,true);
		}
		return result;
	}

	private static URIRef asURIRef(Object object, boolean nullable) {
		if(!nullable) {
			Objects.requireNonNull(object, IDENTITY_PARAM);
		}
		URIRef result=coherceURIRef(object, nullable);
		if(result==null && !nullable) {
			throw new IllegalArgumentException("Cannot crete URIRef from source object of type '"+ object.getClass().getName() + "'");
		}
		return result;
	}

	private static URIRef coherceURIRef(Object object, boolean nullable) {
		URIRef result=null;
		if(object instanceof URIRef) {
			result=(URIRef)object;
		} else if(object instanceof QName) {
			result=uriRef((QName)object);
		} else if(object instanceof URI) {
			result=uriRef((URI)object);
		} else if(object instanceof URL) {
			result=uriRef((URL)object);
		} else if(object instanceof String) {
			result=coherceStringURIRef(nullable, (String)object);
		}
		return result;
	}

	private static URIRef coherceStringURIRef(boolean nullable, String str) {
		URIRef tmp=null;
		try {
			tmp=uriRef(new URI(str));
		} catch (URISyntaxException e) {
			if(!nullable) {
				throw new IllegalArgumentException("Cannot crete URIRef from string '"+str+"': invalid URI",e);
			}
		}
		return tmp;
	}

	private static URIRef asURIRef(Object object) {
		return asURIRef(object,false);
	}

	private static <T> Node asNode(T value) {
		Node result=null;
		if(value instanceof Node) {
			result=(Node)value;
		} else {
			result=asResource(value);
		}
		return
			result!=null?
				result:
				FACTORY.newLiteral(value);
	}

	public static TripleBuilder triple() {
		return new TripleBuilder(FACTORY);
	}

	public static TripleSet tripleSet(Object... sources) {
		TripleSet result=new TripleSet();
		for(Object source:sources) {
			if(source!=null) {
				processTripleSource(result, source);
			}
		}
		return result;
	}

	public static Triple triple(Object subject, Object predicate, Object object) {
		Objects.requireNonNull(subject, "Subject cannot be null");
		Objects.requireNonNull(predicate, "Predicate cannot be null");
		Objects.requireNonNull(object, "Object cannot be null");
		return
			FACTORY.
				newTriple(
					asResource(subject),
					asURIRef(predicate),
					asNode(object));
	}

	public static BlankNode blankNode() {
		return FACTORY.newBlankNode();
	}

	public static BlankNode blankNode(String identity) {
		Objects.requireNonNull(identity, IDENTITY_PARAM);
		return FACTORY.newBlankNode(identity);
	}

	public static URIRef uriRef(String identity) {
		Objects.requireNonNull(identity, IDENTITY_PARAM);
		try {
			return uriRef(new URI(identity));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Cannot create URIRef from '"+identity+"'",e);
		}
	}

	public static URIRef uriRef(URI identity) {
		Objects.requireNonNull(identity, IDENTITY_PARAM);
		return FACTORY.newURIRef(identity);
	}

	public static URIRef uriRef(URL identity) {
		Objects.requireNonNull(identity, IDENTITY_PARAM);
		return uriRef(identity.toString());
	}

	public static URIRef uriRef(QName identity) {
		Objects.requireNonNull(identity, IDENTITY_PARAM);
		return uriRef(identity.getNamespaceURI() + identity.getLocalPart());
	}

	public static <T> Literal<T> literal(T value) {
		Objects.requireNonNull(value, VALUE_PARAM);
		return FACTORY.newLiteral(value);
	}

	public static Literal<String> literal(String value, String language) {
		Objects.requireNonNull(value, VALUE_PARAM);
		Objects.requireNonNull(language, LANGUAGE_PARAM);
		return FACTORY.newLiteral(value,language);
	}

	public static <T> Literal<T> typedLiteral(T value,String type) {
		Objects.requireNonNull(value, VALUE_PARAM);
		Objects.requireNonNull(type, TYPE_PARAM);
		return typedLiteral(value,URI.create(type));
	}

	public static <T> Literal<T> typedLiteral(T value,URI type) {
		Objects.requireNonNull(value, VALUE_PARAM);
		Objects.requireNonNull(type, TYPE_PARAM);
		return FACTORY.newLiteral(value, Datatype.fromURI(type));
	}

	public static <T> Literal<T> typedLiteral(T value, URL type) {
		Objects.requireNonNull(value, VALUE_PARAM);
		Objects.requireNonNull(type, TYPE_PARAM);
		return typedLiteral(value,type.toString());
	}

	public static <T> Literal<T> typedLiteral(T value, QName type) {
		Objects.requireNonNull(value, VALUE_PARAM);
		Objects.requireNonNull(type, TYPE_PARAM);
		return typedLiteral(value,type.getNamespaceURI() + type.getLocalPart());
	}

}