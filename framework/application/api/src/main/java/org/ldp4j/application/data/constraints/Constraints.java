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
package org.ldp4j.application.data.constraints;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.ValueVisitor;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public final class Constraints implements Serializable {

	public enum NodeKind {
		NODE("Node"),
		BLANK_NODE_OR_IRI("BlankNodeOrIRI"),
		BLANK_NODE_OR_LITERAL("BlankNodeOrLiteral"),
		LITERAL_OR_IRI("LiteralOrIRI"),
		BLANK_NODE("BlankNode"),
		IRI("IRI"),
		LITERAL("Literal")
		;

		private final String localName;

		private NodeKind(String localName) {
			this.localName = localName;
		}

		public String localName() {
			return localName;
		}
	}

	public interface Describable {

		String label();
		String comment();
	}

	public static final class Cardinality implements Serializable {

		private static final long serialVersionUID = 7262473645776142538L;

		private int min;
		private int max;

		private Cardinality(int min, int max) {
			this.min = min;
			this.max = max;
		}

		public int min() {
			return this.min;
		}

		public int max() {
			return this.max;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						add("min", this.min).
						add("max", this.max).
						toString();
		}

		public static Cardinality mandatory() {
			return new Cardinality(1,1);
		}

		public static Cardinality atMost(int max) {
			return new Cardinality(0,max);
		}

		public static Cardinality atLeast(int min) {
			return new Cardinality(min,-1);
		}

		public static Cardinality create(int min, int max) {
			return new Cardinality(min,max);
		}

		public static Cardinality optional() {
			return new Cardinality(0,1);
		}

		public static Cardinality unbound() {
			return new Cardinality(0,-1);
		}

	}


	public abstract static class AbstractPropertyConstraint<T extends AbstractPropertyConstraint<T>> implements Serializable {

		private static final long serialVersionUID = 6473281395518369031L;

		private static final class ValueCollector implements ValueVisitor {

			private final Collection<Serializable> individuals;
			private final Collection<Literal<?>> literals;

			private ValueCollector(Collection<Serializable> individuals, Collection<Literal<?>> lLiterals) {
				this.individuals = individuals;
				this.literals = lLiterals;
			}

			@Override
			public void visitLiteral(Literal<?> value) {
				literals.add(value);
			}

			@Override
			public void visitIndividual(Individual<?, ?> value) {
				individuals.add(value.id());
			}

		}

		private final URI predicate;
		private String comment;
		private String label;
		private Set<Serializable> allowedIndividuals;
		private Set<Literal<?>> allowedLiterals;
		private List<Serializable> individuals;
		private List<Literal<?>> literals;
		private URI datatype;
		private NodeKind nodeKind;
		private Shape valueShape;
		private URI valueType;
		private Cardinality cardinality;

		protected AbstractPropertyConstraint(URI predicate) {
			this.predicate=predicate;
		}

		protected abstract T delegate();

		public T withLabel(String label) {
			checkNotNull(label,"Label cannot be null");
			this.label=label;
			return delegate();
		}

		public String label() {
			return this.label;
		}

		public T withComment(String comment) {
			checkNotNull(comment,"Comment cannot be null");
			this.comment=comment;
			return delegate();
		}

		public String comment() {
			return this.comment;
		}

		public URI predicate() {
			return this.predicate;
		}

		public T withCardinality(Cardinality cardinality) {
			checkNotNull(cardinality,"Cardinality cannot be null");
			this.cardinality=cardinality;
			return delegate();
		}

		public Cardinality cardinality() {
			Cardinality result = this.cardinality;
			if(result==null) {
				result=Cardinality.unbound();
			}
			return result;
		}

		public T withAllowedValues(Value... allowedValues) {
			checkNotNull(allowedValues,"Allowed values cannot be null");
			this.allowedLiterals=Sets.newLinkedHashSet();
			this.allowedIndividuals=Sets.newLinkedHashSet();
			ValueCollector valueCollector=new ValueCollector(this.allowedIndividuals,this.allowedLiterals);
			for(Value value:allowedValues) {
				value.accept(valueCollector);
			}
			return delegate();
		}

		public Set<Literal<?>> allowedLiterals() {
			Set<Literal<?>> result=this.allowedLiterals;
			if(result==null) {
				result=Sets.newLinkedHashSet();
			}
			return ImmutableSet.copyOf(result);
		}

		public Set<Individual<?,?>> allowedIndividuals(DataSet dataSet) {
			Set<Serializable> result=this.allowedIndividuals;
			if(result==null) {
				result=Sets.newLinkedHashSet();
			}
			return ConstraintsHelper.getOrCreateIndividuals(dataSet,result);
		}

		public T withDatatype(URI datatype) {
			checkNotNull(datatype,"Datatype cannot be null");
			this.datatype = datatype;
			return delegate();
		}

		public URI datatype() {
			return this.datatype;
		}

		public T withValue(Value... values) {
			checkNotNull(values,"Value cannot be null");
			this.literals=Lists.newArrayList();
			this.individuals=Lists.newArrayList();
			ValueCollector valueCollector=new ValueCollector(this.individuals,this.literals);
			for(Value value:values) {
				value.accept(valueCollector);
			}
			return delegate();
		}

		public List<Literal<?>> literals() {
			List<Literal<?>> result=this.literals;
			if(result==null) {
				result=Lists.newArrayList();
			}
			return ImmutableList.copyOf(result);
		}

		public List<Individual<?,?>> individuals(DataSet dataSet) {
			List<Serializable> result=this.individuals;
			if(result==null) {
				result=Lists.newArrayList();
			}
			return ConstraintsHelper.getOrCreateIndividuals(dataSet,result);
		}

		public T withNodeKind(NodeKind nodeKind) {
			checkNotNull(nodeKind,"Node kind cannot be null");
			this.nodeKind=nodeKind;
			return delegate();
		}

		public NodeKind nodeKind() {
			return this.nodeKind;
		}

		public T withValueShape(Shape valueShape) {
			this.valueShape = valueShape;
			return delegate();
		}

		public Shape valueShape() {
			return this.valueShape;
		}

		public T withValueType(URI valueType) {
			this.valueType = valueType;
			return delegate();
		}

		public URI valueType() {
			return this.valueType;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						omitNullValues().
						add("predicate", this.predicate).
						add("label", this.label).
						add("comment", this.comment).
						add("cardinality", this.cardinality()).
						add("allowedLiterals", this.allowedLiterals).
						add("allowedIndividuals", this.allowedIndividuals).
						add("datatype", this.datatype).
						add("literals", this.literals).
						add("literals", this.individuals).
						add("nodeKind", this.nodeKind).
						add("valueShape", this.valueShape).
						add("valueType", this.valueType).
						toString();
		}

	}

	public static final class PropertyConstraint extends AbstractPropertyConstraint<PropertyConstraint> implements Describable {

		private static final long serialVersionUID = -2646499801130951583L;

		private PropertyConstraint(URI predicate) {
			super(predicate);
		}

		@Override
		protected PropertyConstraint delegate() {
			return this;
		}

	}

	public static final class InversePropertyConstraint extends AbstractPropertyConstraint<InversePropertyConstraint> implements Describable {

		private static final long serialVersionUID = -6328974380403084873L;

		private InversePropertyConstraint(URI predicate) {
			super(predicate);
		}

		@Override
		protected InversePropertyConstraint delegate() {
			return this;
		}

	}

	public static final class Shape implements Describable, Serializable {

		private static final long serialVersionUID = 3966457418001884744L;

		private Map<URI,AbstractPropertyConstraint<?>> constraints; // NOSONAR
		private String label;
		private String comment;

		private Shape() {
			this.constraints=Maps.newLinkedHashMap();
		}

		public Shape withLabel(String label) {
			this.label=label;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String label() {
			return this.label;
		}

		public Shape withComment(String comment) {
			this.comment=comment;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String comment() {
			return this.comment;
		}

		public Shape withPropertyConstraint(PropertyConstraint constraint) {
			checkNotNull(constraint);
			URI predicate = constraint.predicate();
			checkArgument(!this.constraints.containsKey(predicate),"Shape already defines constraints for predicate '"+predicate+"'");
			this.constraints.put(predicate,constraint);
			return this;
		}

		public Shape withPropertyConstraint(InversePropertyConstraint constraint) {
			checkNotNull(constraint);
			URI predicate = constraint.predicate();
			checkArgument(!this.constraints.containsKey(predicate),"Shape already defines constraints for predicate '"+predicate+"'");
			this.constraints.put(predicate,constraint);
			return this;
		}

		public List<PropertyConstraint> propertyConstraints() {
			final Builder<PropertyConstraint> builder=ImmutableList.<PropertyConstraint>builder();
			filter(builder,PropertyConstraint.class);
			return builder.build();
		}

		public List<InversePropertyConstraint> inversePropertyConstraints() {
			final Builder<InversePropertyConstraint> builder=ImmutableList.<InversePropertyConstraint>builder();
			filter(builder,InversePropertyConstraint.class);
			return builder.build();
		}

		private <T extends AbstractPropertyConstraint<?>> void filter(Builder<T> builder, Class<? extends T> clazz) {
			for(AbstractPropertyConstraint<?> c:this.constraints.values()) {
				if(clazz.isInstance(c)) {
					builder.add(clazz.cast(c));
				}
			}
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						omitNullValues().
						add("label", this.label).
						add("comment", this.comment).
						add("constraints",this.constraints).
						toString();
		}

	}

	private static final long serialVersionUID = 4368698694568719975L;

	private Map<Serializable,Shape> nodeShapes; // NOSONAR
	private Map<URI,Shape> typeShapes; // NOSONAR

	private Constraints() {
		this.nodeShapes=Maps.newLinkedHashMap();
		this.typeShapes=Maps.newLinkedHashMap();
	}

	public List<Shape> shapes() {
		return
			ImmutableList.
				<Shape>builder().
					addAll(this.typeShapes.values()).
					addAll(this.nodeShapes.values()).
					build();
	}

	public Set<URI> types() {
		return ImmutableSet.copyOf(this.typeShapes.keySet());
	}

	public Set<Individual<?,?>> nodes(DataSet dataSet) {
		checkNotNull(dataSet,"Data set cannot be null");
		return
			ConstraintsHelper.
				getOrCreateIndividuals(
					dataSet,
					this.nodeShapes.keySet());
	}

	public Shape typeShape(URI type) {
		return this.typeShapes.get(type);
	}

	public Shape nodeShape(Individual<?,?> individual) {
		return this.nodeShapes.get(individual.id());
	}

	public Constraints withTypeShape(URI type, Shape shape) {
		checkNotNull(type,"Type URI cannot be null");
		checkNotNull(shape,"Shape cannot be null");
		checkArgument(!this.typeShapes.containsKey(type),"A shape is already defined for type '"+type+"'");
		this.typeShapes.put(type,shape);
		return this;
	}

	public Constraints withNodeShape(Individual<?,?> individual, Shape shape) {
		checkNotNull(individual,"Type URI cannot be null");
		checkNotNull(shape,"Shape cannot be null");
		checkArgument(!this.nodeShapes.containsKey(individual.id()),"A shape is already defined for individual '"+individual.id()+"'");
		this.nodeShapes.put(individual.id(),shape);
		return this;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("typeShapes", this.typeShapes).
					add("nodeShapes", this.nodeShapes).
					toString();
	}

	public static Shape shape() {
		return new Shape();
	}

	public static Constraints constraints() {
		return new Constraints();
	}

	public static PropertyConstraint propertyConstraint(URI predicate) {
		return new PropertyConstraint(predicate);
	}

	public static InversePropertyConstraint inversePropertyConstraint(URI predicate) {
		return new InversePropertyConstraint(predicate);
	}

}
