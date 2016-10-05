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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.rdf.bean.Cardinality;
import org.ldp4j.rdf.bean.InvalidDefinitionException;
import org.ldp4j.rdf.bean.annotations.AtLeast;
import org.ldp4j.rdf.bean.annotations.AtMost;
import org.ldp4j.rdf.bean.annotations.CardinalityConstraint;
import org.ldp4j.rdf.bean.annotations.Optional;
import org.ldp4j.rdf.bean.annotations.Range;
import org.ldp4j.rdf.bean.annotations.Unbound;

final class CardinalityDefinition implements Cardinality {

	private static final class CardinalityConstraintValidator {

		private static final class Acumulator {

			private Integer value;
			private boolean redefined;
			private boolean overriden;

			public void setValue(int value) {
				if(this.value==null) {
					this.value=value;
				} else {
					redefined=true;
					if(!this.overriden) {
						this.overriden=this.value!=value;
					}
				}
			}

			public int getValue() {
				return value;
			}

			public boolean isDefined() {
				return !redefined && value!=null;
			}

			@SuppressWarnings("unused")
			public boolean isOverriden() {
				return overriden;
			}

		}

		private final Acumulator min=new Acumulator();
		private final Acumulator max=new Acumulator();
		private final Map<String,String> violations=new HashMap<String,String>();
		private final List<Annotation> constraints;
		private boolean optional;
		private boolean unbound;
		private boolean simple;
		private boolean repetitions;

		CardinalityConstraintValidator(List<Annotation> constraints) {
			this.constraints = constraints;
			for(Annotation genericConstraint:constraints) {
				validateGenericConstraint(genericConstraint);
			}
			if(isUnbound() && hasMax() ) {
				addViolation("Cannot have max cardinality if unbound");
			}
			if(isOptional() && hasMin() ) {
				addViolation("Cannot have min cardinality if optional");
			}
		}

		private void validateGenericConstraint(Annotation genericConstraint) {
			if(genericConstraint instanceof AtLeast) {
				validateAtLeast((AtLeast)genericConstraint);
			} else if(genericConstraint instanceof AtMost) {
				validateAtMost((AtMost)genericConstraint);
			} else if(genericConstraint instanceof Range) {
				validatRange((Range)genericConstraint);
			} else if(genericConstraint instanceof Optional) {
				this.optional=true;
			} else if(genericConstraint instanceof Unbound) {
				this.unbound=true;
			} else {
				addViolation("assertion","Unsupported cardinality constraint '"+genericConstraint.getClass().getCanonicalName()+"'");
			}
		}

		private void addViolation(String violation) {
			addViolation("combination", violation);
		}

		private void addViolation(String source, String violation) {
			violations.put(source, violation);
		}

		private void addViolation(Annotation source, String violation) {
			addViolation(source.annotationType().getSimpleName(), violation);
		}

		private void setMin(int min) {
			this.min.setValue(min);
		}

		private void setMax(int max) {
			this.max.setValue(max);
		}

		private void validatRange(Range constraint) {
			if(constraint.min()<0) {
				addViolation(constraint,"Min cardinality cannot be lower than 0");
			}
			if(constraint.max()<1) {
				addViolation(constraint,"Max cardinality cannot be lower than 1");
			}
			if(constraint.min()>constraint.max()) {
				addViolation(constraint,"Max cardinality cannot be lower than min cardinality");
			}
			setMin(constraint.min());
			setMax(constraint.max());
		}

		private void validateAtMost(AtMost constraint) {
			if(constraint.max()<2) {
				addViolation(constraint,"Max cardinality cannot be lower than 2");
			}
			setMin(constraint.max());
		}

		private void validateAtLeast(AtLeast constraint) {
			if(constraint.min()<1) {
				addViolation(constraint,"Min cardinality cannot be lower than 1");
			}
			setMin(constraint.min());
		}

		public boolean isOptional() {
			return optional;
		}

		public boolean isUnbound() {
			return unbound;
		}

		public boolean hasMin() {
			return min.isDefined();
		}

		public boolean hasMax() {
			return max.isDefined();
		}

		public int min() {
			if(hasMin()) {
				return min.getValue();
			} else {
				if(isOptional()) {
					return 0;
				} else {
					return 1;
				}
			}
		}

		public int max() {
			if(hasMax()) {
				return max.getValue();
			} else {
				if(isSimple()) {
					return 1;
				} else {
					return -1;
				}
			}
		}

		public boolean isValid(java.lang.reflect.Type type) {
			boolean result = violations.isEmpty();
			if(result) {
				Class<?> clazz=null;
				if(type instanceof Class<?>) {
					clazz=(Class<?>)type;
				} else if(type instanceof ParameterizedType) {
					clazz=(Class<?>)((ParameterizedType)type).getRawType();
				} else if(type instanceof TypeVariable<?>) {
					// TODO: Check whether or not this always hold
					clazz=(Class<?>)((TypeVariable<?>)type).getBounds()[0];
				} else {
					addViolation("assertion","Unsupported range type '"+type+"'");
				}
				if(clazz!=null) {
					this.simple=!TypeSupport.isAggregation(clazz);
					this.repetitions=this.simple?false:TypeSupport.isRepeatable(clazz);
					for(Annotation constraint:constraints) {
						CardinalityConstraint cc=
							constraint.
								annotationType().
								getAnnotation(CardinalityConstraint.class);
						verifyApplicability(type,clazz,constraint,cc.appliesTo());
					}
				}
				result=violations.isEmpty();
			}
			return result;
		}

		private void verifyApplicability(java.lang.reflect.Type type, Class<?> clazz, Annotation constraint, Class<?>[] appliesTo) {
			for(int i=0;i<appliesTo.length;i++) {
				if(!appliesTo[i].isAssignableFrom(clazz)) {
					addViolation(constraint,"Constraint cannot be applied to type '"+type+"'");
					break;
				}
			}
		}

		public boolean isSimple() {
			return simple;
		}

		public boolean isRepeteable() {
			return repetitions;
		}

		public String getReport() {
			StringWriter result = new StringWriter();
			PrintWriter out=new PrintWriter(result);
			out.printf("Cardinality definition violations found (%d):",violations.size());
			for(Entry<String,String> violation:violations.entrySet()) {
				out.printf("%n\t- %s (%s)",violation.getValue(),violation.getKey());
			}
			return result.toString();
		}
	}

	private final boolean repetitions;
	private final int min;
	private final int max;

	private CardinalityDefinition(int min, int max, boolean repetitions) {
		this.min = min;
		this.max = max;
		this.repetitions = repetitions;
	}

	@Override
	public int min() {
		return min;
	}

	@Override
	public int max() {
		return max;
	}

	@Override
	public boolean isOptional() {
		return min==0;
	}

	@Override
	public boolean isUnbounded() {
		return max<0;
	}

	@Override
	public boolean allowsRepetitions() {
		return repetitions;
	}

	static Cardinality fromConstraints(java.lang.reflect.Type type, List<Annotation> constraints) {
		CardinalityConstraintValidator validator=new CardinalityConstraintValidator(constraints);
		if(!validator.isValid(type)) {
			throw new InvalidDefinitionException(validator.getReport());
		}
		return new CardinalityDefinition(validator.min(),validator.max(),validator.isRepeteable());
	}

	@Override
	public String toString() {
		return "CardinalityDefinition [repetitions=" + repetitions + ", min="
				+ min + ", max=" + (max<0?"UNBOUND":Integer.toString(max)) + "]";
	}

}