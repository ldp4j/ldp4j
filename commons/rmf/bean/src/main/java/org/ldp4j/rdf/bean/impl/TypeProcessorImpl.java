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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ldp4j.rdf.Literal;
import org.ldp4j.rdf.Node;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.URIRef;
import org.ldp4j.rdf.bean.Cardinality;
import org.ldp4j.rdf.bean.Category;
import org.ldp4j.rdf.bean.InvalidDefinitionException;
import org.ldp4j.rdf.bean.NamingPolicy;
import org.ldp4j.rdf.bean.Property;
import org.ldp4j.rdf.bean.Type;
import org.ldp4j.rdf.bean.impl.model.Graph;
import org.ldp4j.rdf.bean.impl.model.Individual;
import org.ldp4j.rdf.bean.impl.model.ModelFactory;
import org.ldp4j.rdf.util.RDFModelDSL;
import org.ldp4j.rdf.util.TripleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TypeProcessorImpl<T> implements TypeProcessor<T> {

	private static final class UnmarshallingSession {

		private final Map<Integer,Object> r2o=new HashMap<Integer, Object>();
		private final TripleSet triples;
		private final TypeManager manager;
		private final Graph graph;

		private UnmarshallingSession(TypeManager manager, TripleSet triples) {
			this.manager = manager;
			this.triples = triples;
			this.graph = ModelFactory.newGraph(getTriples());
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Created unmarshalling session for: \n"+graph);
			}
		}

		private <T> Unmarshaller<T> newUnmarshaller(Class<? extends T> clazz) {
			List<Type> types = manager.getTypes(clazz);
			return new Unmarshaller<T>(clazz,types,this);
		}

		private TripleSet getTriples() {
			return triples;
		}

		private <T> T resolve(Class<? extends T> clazz, Resource<?> resource) {
			Type lookup = manager.getRegistry().lookup(clazz);
			if(lookup.getCategory().equals(Category.ENUMERATION)) {
				throw new UnsupportedOperationException("Method not implemented yet");
			}
			Object object = r2o.get(System.identityHashCode(resource));
			if(clazz.isInstance(object)) {
				return clazz.cast(object);
			}
			return null;
		}

		private <T> void register(T object, Resource<?> resource) {
			r2o.put(System.identityHashCode(resource), object);
		}

		private Individual getIndividual(Resource<?> identity) {
			return graph.getIndividual(identity);
		}

	}

	private static class Unmarshaller<T> {

		private interface ValueProcessor<T> {

			List<T> getValues(Property property);

			<S> S processValue(T value, Class<? extends S> clazz);

		}

		private class ObjectProcessor implements ValueProcessor<Individual> {

			private final Individual individual;

			private ObjectProcessor(Individual individual) {
				this.individual = individual;
			}

			@Override
			public List<Individual> getValues(Property property) {
				List<Individual> links=new ArrayList<Individual>();
				for(Individual literal:individual.getPropertyObjects(predicate(property))) {
					links.add(literal);
				}
				return links;
			}

			@Override
			public <S> S processValue(Individual value, Class<? extends S> clazz) {
				return resolve(value,clazz,this.individual.getIdentity());
			}

			private <S> S resolve(Individual individual, Class<? extends S> type, Resource<?> source) {
				S bean = session.resolve(type, individual.getIdentity());
				if(bean==null) {
					log("Paused unmarshalling of resource '%s'.",source);
					Unmarshaller<S> marshaller=session.newUnmarshaller(type);
					bean=marshaller.unmarshall(individual.getIdentity());
					log("Resuming unmarshalling of resource '%s'...",source);
				}
				return bean;
			}

		}

		private static class LiteralProcessor implements ValueProcessor<Object> {

			private final Individual individual;

			private LiteralProcessor(Individual individual) {
				this.individual = individual;
			}

			@Override
			public List<Object> getValues(Property property) {
				List<Object> links=new ArrayList<Object>();
				for(Literal<?> literal:individual.getPropertyValues(predicate(property))) {
					links.add(literal.getValue());
				}
				return links;
			}

			@Override
			public <S> S processValue(Object value, Class<? extends S> clazz) {
				if(!clazz.isAssignableFrom(value.getClass())) {
					throw new IllegalStateException("Invalid type: expected an instance of '"+clazz.getCanonicalName()+"' but got an instance of '"+value.getClass().getCanonicalName()+"'");
				}
				return clazz.cast(value);
			}

		}

		private final Class<? extends T> clazz;
		private final UnmarshallingSession session;
		private List<Type> types;

		Unmarshaller(Class<? extends T> clazz, List<Type> types, UnmarshallingSession session) {
			this.clazz=clazz;
			this.types=types;
			this.session = session;
		}

		T unmarshall(Resource<?> identity) {
			T result=session.resolve(clazz, identity);
			if(result!=null) {
				Individual individual=session.getIndividual(identity);
				if(individual!=null) {
					result=createBean(identity, individual);
				}
			}
			return result;
		}

		private T createBean(Resource<?> identity, Individual individual) {
			try {
				log("Started unmarshalling of resource '%s' with %s...",identity,types);
				// TODO: Need to see what to do with enumerations...
				T bean=clazz.newInstance();
				session.register(bean,identity);
				log("Registered object '%s' for resource '%s'",bean,identity);
				for(Type type:types) {
					for(Property property:type.getProperties()) {
						populateProperty(property,createProcessor(individual, property),bean);
					}
				}
				log("Completed unmarshalling of resource '%s'.",identity);
				return bean;
			} catch (InstantiationException e) {
				throw new IllegalStateException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}

		private ValueProcessor<?> createProcessor(Individual individual, Property property) {
			ValueProcessor<?> processor=null;
			if(property.getRange().isLiteral()) {
				processor=new LiteralProcessor(individual);
			} else {
				processor=new ObjectProcessor(individual);
			}
			return processor;
		}

		/**
		 * Just for type safety
		 */
		private <S> Set<S> newSet(Class<? extends S> type) { // NOSONAR
			return new HashSet<S>();
		}

		/**
		 * Just for type safety
		 */
		private <S> List<S> newList(Class<? extends S> type) { // NOSONAR
			return new ArrayList<S>();
		}

		private <S> void populateProperty(Property property, ValueProcessor<S> handler, T target) {
			List<S> values=handler.getValues(property);
			int max=enforceCardinalityRestrictions(property,values);
			Class<?> range = property.getRange().getType();
			if(!values.isEmpty()) {
				if(max==1) {
					S value = values.get(0);
					Object bean = handler.processValue(value,range);
					property.setValue(target, bean);
					log("-> Property(%s)=%s",property.getName(),bean);
				} else {
					Collection<Object> collection=!property.getCardinality().allowsRepetitions()?newSet(range):newList(range);
					for(int i=0;i<max;i++) {
						S value = values.get(i);
						Object bean = handler.processValue(value,range);
						collection.add(bean);
					}
					property.setValue(target, collection);
					log("-> Property(%s)=%s",property.getName(),collection);
				}
			}
		}


		private static URIRef predicate(Property property) {
			return RDFModelDSL.uriRef(property.getNamespace()+property.getName());
		}

		private int enforceCardinalityRestrictions(Property property, List<?> values) {
			Cardinality cardinality=property.getCardinality();
			int min=cardinality.min();
			int max=cardinality.isUnbounded()?values.size():cardinality.max();
			if(min>values.size()) {
				throw new IllegalStateException("Not enough values defined for property '"+predicate(property)+"': expected "+min+" but got "+values.size());
			} else if(max<values.size()) {
				throw new IllegalStateException("Defined more values than required for property '"+predicate(property)+"': expected "+max+" but got "+values.size());
			}
			return max;
		}
	}

	private static final class MarshallingSession {

		private final NamingPolicy policy;

		private final Map<Integer,Resource<?>> o2r=new HashMap<Integer, Resource<?>>();
		private final TripleSet triples;
		private final TypeManager manager;

		private MarshallingSession(NamingPolicy policy, TypeManager manager) {
			this.policy=policy;
			this.manager=manager;
			this.triples=new TripleSet();
		}

		private <T> Marshaller<T> newMarshaller(T object) {
			List<Type> types = manager.getTypes(object.getClass());
			return new Marshaller<T>(object,types,this);
		}

		private <T> Resource<?> lookup(T object) {
			return o2r.get(System.identityHashCode(object));
		}

		private <T> Resource<?> deploy(T object) {
			Resource<?> resource=policy.createIdentity(object);
			o2r.put(System.identityHashCode(object), resource);
			return resource;
		}
		private <T> Resource<?> deployEnumerated(T object) {
			Resource<?> resource=policy.enumeratedIdentity(object);
			o2r.put(System.identityHashCode(object), resource);
			return resource;
		}
		private void addTriple(Resource<?> subject, URIRef predicate, Node object) {
			triples.add(subject, predicate, object);
		}

		private TripleSet getTriples() {
			return triples;
		}

	}

	private static class Marshaller<T> {

		private enum PropertyType {
			LITERAL("datatype"),
			OBJECT("object"),
			;
			private final String description;

			PropertyType(String description) {
				this.description = description;
			}

			@Override
			public String toString() {
				return description;
			}
		}

		private static final URIRef RDF_TYPE = RDFModelDSL.uriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		private final T source;
		private final List<Type> types;
		private final MarshallingSession session;

		Marshaller(T source, List<Type> types, MarshallingSession session) {
			this.source=source;
			this.types = types;
			this.session = session;
		}

		private void logTriple(Object subject, Object predicate, Object object) {
			log("-> Triple {%08X, %s, %s}",System.identityHashCode(subject),predicate,object);
		}

		private Collection<?> getValues(Property property) {
			Object value = property.getValue(source);
			Collection<?> values=null;
			if(value instanceof Collection<?>) {
				values=(Collection<?>)value;
			} else {
				List<Object> tmp=new ArrayList<Object>();
				if(value!=null) {
					tmp.add(value);
				}
				values=tmp;
			}
			return values;
		}

		private void marshallProperty(Resource<?> subject, URIRef predicate, Object value) {
			Node object=null;
			PropertyType propertyType=null;
			if(TypeSupport.isLiteral(value.getClass())) {
				propertyType=PropertyType.LITERAL;
				object=RDFModelDSL.literal(value);
			} else {
				propertyType=PropertyType.OBJECT;
				object=resolve(value);
			}
			log("Adding %s property '%s' to object '%s'...",propertyType,predicate,source);
			logTriple(source,predicate,object);
			session.addTriple(subject,predicate,object);
		}

		private Resource<?> resolve(Object value) {
			Resource<?> identity = session.lookup(value);
			if(identity==null) {
				log("Paused marshalling of object '%s'.",source);
				Marshaller<Object> marshaller=session.newMarshaller(value);
				marshaller.marshall();
				log("Resuming marshalling of object '%s'...",source);
				identity=session.lookup(value);
			}
			return identity;
		}

		void marshall() {
			Resource<?> subject=session.lookup(source);
			if(subject!=null) {
				return;
			}
			log("Started marshalling of object '%s' with %s...",source,types);
			Type mainType = types.get(0);
			if(mainType.getCategory().equals(Category.ENUMERATION)) {
				subject=session.deployEnumerated(source);
			} else {
				subject=session.deploy(source);
			}
			log("Identified object '%s' as resource '%s'",source,subject);
			for(Type type:types) {
				addType(subject, type);
				for(Property property:type.getProperties()) {
					Collection<?> values = getValues(property);
					for(Object value:values) {
						marshallProperty(subject,toURIRef(property),value);
					}
				}
			}
			log("Completed marshalling of object '%s'.",source);
		}

		private URIRef toURIRef(Property property) {
			return RDFModelDSL.uriRef(property.getNamespace()+property.getName());
		}

		private void addType(Resource<?> identity, Type type) {
			URIRef object = RDFModelDSL.uriRef(type.getNamespace()+type.getName());
			log("Adding type '%s' to object '%s'...",object,source);
			logTriple(source,RDF_TYPE,object);
			session.addTriple(identity, RDF_TYPE, object);
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(TypeProcessorImpl.class);

	private final Class<? extends T> clazz;

	private final TransactionalTypeRegistry registry;

	TypeProcessorImpl(Class<? extends T> clazz, TransactionalTypeRegistry registry) {
		this.clazz = clazz;
		this.registry = registry;
	}

	private static void log(String format, Object... args) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format(format,args));
		}
	}

	@Override
	public TripleSet deflate(T o, NamingPolicy policy) {
		TransactionalTypeRegistry savepoint=registry.setSavepoint();
		try {
			MarshallingSession session=new MarshallingSession(policy,new TypeManagerImpl(savepoint));
			Marshaller<T> newMarshaller = session.newMarshaller(o);
			newMarshaller.marshall();
			TripleSet result = session.getTriples();
			savepoint.commit();
			return result;
		} catch (InvalidDefinitionException e) {
			savepoint.rollback();
			throw e;
		}
	}

	@Override
	public T inflate(Resource<?> identity, TripleSet triples) {
		TransactionalTypeRegistry savepoint=registry.setSavepoint();
		try {
			UnmarshallingSession context=new UnmarshallingSession(new TypeManagerImpl(savepoint),triples);
			Unmarshaller<T> newUnmarshaller = context.newUnmarshaller(clazz);
			T result = newUnmarshaller.unmarshall(identity);
			savepoint.commit();
			return result;
		} catch (InvalidDefinitionException e) {
			savepoint.rollback();
			throw e;
		}
	}

}
