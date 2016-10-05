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
package org.ldp4j.rdf.bean;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ldp4j.rdf.bean.annotations.AtLeast;
import org.ldp4j.rdf.bean.annotations.AtMost;
import org.ldp4j.rdf.bean.annotations.CardinalityConstraint;
import org.ldp4j.rdf.bean.annotations.CardinalityConstraint.CardinalityAdapter;
import org.ldp4j.rdf.bean.annotations.Optional;
import org.ldp4j.rdf.bean.annotations.Range;
import org.ldp4j.rdf.bean.annotations.Unbound;

public final class Constraints {

	public interface ConstraintDefinitionContext<T extends Member & AnnotatedElement> {

		Class<?> getPropertyType();

		T getAnnotatedMember();

	}

	public static class ConstraintViolationException extends InvalidDefinitionException {

		private static final long serialVersionUID = -8229664315206414875L;

		private final String constraint;
		private final String member;

		private final List<String> violations; // NOSONAR

		public ConstraintViolationException(Annotation constraint, Member member, List<String> violations) {
			this(message(constraint,member,violations),constraint,member,violations);
		}

		public ConstraintViolationException(String message, Annotation constraint, Member member, List<String> violations) {
			super(message);
			this.constraint = constraint.toString();
			this.member=member.toString();
			this.violations=new ArrayList<String>(violations);
		}

		private static String message(Annotation constraint, Member member, List<String> violations) {
			StringWriter result = new StringWriter();
			PrintWriter out=new PrintWriter(result);
			out.printf("Invalid cardinality constraint '%s' definition found in '%s'. %d violation(s) found:",constraint.annotationType().getCanonicalName(),member.toString(),violations.size());
			for(String violation:violations) {
				out.printf("%n\t- %s",violation);
			}
			return result.toString();
		}

		public String getConstraint() {
			return this.constraint;
		}

		public String getMember() {
			return this.member;
		}

		public List<String> getViolations() {
			return Collections.unmodifiableList(violations);
		}

	}

	private static class DefaultCardinality implements Cardinality {

		private final boolean allowsRepetitions;
		private final boolean simple;

		private DefaultCardinality(Class<?> clazz) {
			this.allowsRepetitions = List.class.isAssignableFrom(clazz);
			this.simple=!(List.class.isAssignableFrom(clazz) || Set.class.isAssignableFrom(clazz));
		}

		@Override
		public int min() {
			return 1;
		}

		@Override
		public int max() {
			return simple?1:Integer.MAX_VALUE;
		}

		@Override
		public final boolean isOptional() {
			return min()==0;
		}

		@Override
		public final boolean isUnbounded() {
			return !simple && max()==Integer.MAX_VALUE;
		}

		@Override
		public final boolean allowsRepetitions() {
			return !simple && allowsRepetitions;
		}

		@Override
		public String toString() {
			return "Cardinality [repetitions=" + allowsRepetitions()+ ", min="+ min() + ", max=" + (isUnbounded()?"UNBOUND":Integer.toString(max())) + "]";
		}

	}

	public static final class UnboundCardinalityConstraintAdapter implements CardinalityAdapter<Unbound> {

		@Override
		public Cardinality fromConstraint(Unbound constraint, ConstraintDefinitionContext<?> context) {
			CardinalityConstraintValidator.newInstance(context).validate(constraint);
			return new DefaultCardinality(context.getPropertyType()) {
				@Override
				public int max() {
					return Integer.MAX_VALUE;
				}
			};
		}

	}

	public static final class OptionalCardinalityConstraintAdapter implements CardinalityAdapter<Optional> {

		@Override
		public Cardinality fromConstraint(Optional constraint, ConstraintDefinitionContext<?> context) {
			CardinalityConstraintValidator.newInstance(context).validate(constraint);
			return new DefaultCardinality(context.getPropertyType()) {
				@Override
				public int min() {
					return 0;
				}
			};
		}

	}

	public static final class AtMostCardinalityConstraintAdapter implements CardinalityAdapter<AtMost> {

		@Override
		public Cardinality fromConstraint(final AtMost constraint, ConstraintDefinitionContext<?> context) {
			CardinalityConstraintValidator.newInstance(context).validate(constraint);
			return new DefaultCardinality(context.getPropertyType()) {
				@Override
				public int min() {
					return 0;
				}

				@Override
				public int max() {
					return constraint.max();
				}
			};
		}

	}

	public static final class AtLeastCardinalityConstraintAdapter implements CardinalityAdapter<AtLeast> {

		@Override
		public Cardinality fromConstraint(final AtLeast constraint, ConstraintDefinitionContext<?> context) {
			CardinalityConstraintValidator.newInstance(context).validate(constraint);
			return new DefaultCardinality(context.getPropertyType()) {
				@Override
				public int min() {
					return constraint.min();
				}

				@Override
				public int max() {
					return -1;
				}
			};
		}

	}

	public static final class RangeCardinalityConstraintAdapter implements CardinalityAdapter<Range> {
		@Override
		public Cardinality fromConstraint(final Range constraint, ConstraintDefinitionContext<?> context) {
			CardinalityConstraintValidator.newInstance(context).validate(constraint);
			return new DefaultCardinality(context.getPropertyType()) {
				@Override
				public int min() {
					return constraint.min();
				}

				@Override
				public int max() {
					return constraint.max();
				}
			};
		}
	}

	static List<Cardinality> getCardinalityConstraints(ConstraintDefinitionContext<?> context) {
		List<Cardinality> constraints=new ArrayList<Cardinality>();
		for(Annotation annotation:context.getAnnotatedMember().getDeclaredAnnotations()) {
			if(isCardinalityConstraint(annotation)) {
				CardinalityAdapter<Annotation> adapter = getCardinalityAdapter(annotation);
				constraints.add(adapter.fromConstraint(annotation, context));
			}
		}

		return constraints;
	}

	static Cardinality combineCardinalityConstraints(List<Cardinality> cardinalities, ConstraintDefinitionContext<?> context) {
		if(cardinalities.isEmpty()) {
			return new DefaultCardinality(context.getPropertyType());
		} else if(cardinalities.size()==1) {
			return cardinalities.get(0);
		} else {
			Cardinality result=cardinalities.get(0);
			for(Cardinality refinement:cardinalities.subList(1, cardinalities.size()-1)) {
				result=combineCardinalities(result,refinement);
			}
			return result;
		}
	}

	private static Cardinality combineCardinalities(Cardinality original, Cardinality refinement) {
		if(refinement.min()<original.min()) {
			throw new InvalidDefinitionException("Invalid cardinality constraint refinement: refined min cardinality ("+refinement.min()+") is lower than original min cardinality ("+original.min()+")");
		}
		if(refinement.max()>original.max()) {
			throw new InvalidDefinitionException("Invalid cardinality constraint refinement: refined max cardinality ("+refinement.max()+") is greater than original max cardinality ("+original.max()+")");
		}
		return refinement;
	}

	private static boolean isCardinalityConstraint(Annotation annotation) {
		return annotation.annotationType().isAnnotationPresent(CardinalityConstraint.class);
	}

	@SuppressWarnings("unchecked")
	private static <A extends Annotation> CardinalityAdapter<A> getCardinalityAdapter(Annotation annotation) {
		CardinalityConstraint meta = annotation.annotationType().getAnnotation(CardinalityConstraint.class);
		try {
			return (CardinalityAdapter<A>)meta.adaptedBy().newInstance();
		} catch (InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

 	private static final class CardinalityConstraintValidator {

		private final ConstraintDefinitionContext<?> context;

		private CardinalityConstraintValidator(ConstraintDefinitionContext<?> context) {
			this.context = context;
		}

		void validate(Annotation constraint) {
			CardinalityConstraint meta = constraint.annotationType().getAnnotation(CardinalityConstraint.class);
			if(meta==null) {
				throw new IllegalStateException("Annotation '"+constraint.annotationType()+"' is not a cardinality constraint");
			}

			List<String> violations=new ArrayList<String>();

			verifyConstraintApplicability(context.getPropertyType(), meta.appliesTo(), violations);
			evaluateConstraint(constraint, violations);

			if(!violations.isEmpty()) {
				throw new Constraints.ConstraintViolationException(constraint, context.getAnnotatedMember(), violations);
			}
		}

		private void verifyConstraintApplicability(Class<?> propertyType, Class<?>[] applicableTypes, List<String> violations) {
			if(!isApplicableTo(propertyType, applicableTypes)) {
				violations.add(String.format("Constraint cannot be applied to type '%s'",propertyType.getCanonicalName()));
			}
		}

		private void evaluateConstraint(Annotation constraint, List<String> violations) {
			if(constraint instanceof AtMost) {
				evaluateAtMost((AtMost)constraint,violations);
			} else if(constraint instanceof AtLeast) {
				evaluateAtLeast((AtLeast)constraint,violations);
			} else if(constraint instanceof Range) {
				evaluateRange((Range)constraint,violations);
			} else if(!(constraint instanceof Optional || constraint instanceof Unbound)) {
				throw new IllegalStateException("Unsupported cardinality constraint '"+constraint.annotationType()+"'");
			}
		}

		private void evaluateRange(Range constraint, List<String> violations) {
			if(constraint.min()<0) {
				violations.add(String.format("Min cardinality cannot be lower than 0 (%s)",constraint.min()));
			}
			if(constraint.max()<1) {
				violations.add(String.format("Max cardinality cannot be lower than 1 (%s)",constraint.max()));
			}
			if(constraint.min()>constraint.max()) {
				violations.add(String.format("Max cardinality cannot be lower than min cardinality (%s<%s)",constraint.max(),constraint.min()));
			}
		}

		private void evaluateAtMost(AtMost constraint, List<String> violations) {
			if(constraint.max()<2) {
				violations.add(String.format("Max cardinality cannot be lower than 2 (%s)",constraint.max()));
			}
		}

		private void evaluateAtLeast(AtLeast constraint, List<String> violations) {
			if(constraint.min()<1) {
				violations.add(String.format("Min cardinality cannot be lower than 1 (%s)",constraint.min()));
			}
		}

		private boolean isApplicableTo(Class<?> propertyType, Class<?>[] applicableTypes) {
			boolean canBeApplied=false;
			for(int i=0;i<applicableTypes.length && !canBeApplied;i++) {
				canBeApplied=applicableTypes[i]==Object.class;
				if(!canBeApplied) {
					canBeApplied=applicableTypes[i].isAssignableFrom(propertyType);
				}
			}
			return canBeApplied;
		}

		static CardinalityConstraintValidator newInstance(ConstraintDefinitionContext<?> context) {
			return new CardinalityConstraintValidator(context);
		}
	}

}
