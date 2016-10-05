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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.ldp4j.rdf.bean.Cardinality;
import org.ldp4j.rdf.bean.InvalidDefinitionException;
import org.ldp4j.rdf.bean.Property;
import org.ldp4j.rdf.bean.PropertyEditor;
import org.ldp4j.rdf.bean.Range;
import org.ldp4j.rdf.bean.annotations.CardinalityConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PropertyFactory {

	private class PropertyDefinitionBuilder {

		private org.ldp4j.rdf.bean.annotations.Property property;
		private String defaultName;
		private Class<?> domain;
		private Range range;
		private Cardinality cardinality;
		private List<Annotation> cardinalityConstraints;

		private PropertyDefinitionBuilder() {
		}

		private Property assemble(PropertyEditor editor) {
			String name=StringUtils.nonEmptyOrDefault(property.name(),defaultName);
			String namespace=StringUtils.nonEmptyOrDefault(property.namespace(),defaultNamespace);
			return new PropertyDefinition(name, namespace, domain, range, cardinality, editor);
		}

		private PropertyDefinitionBuilder withDefaultName(String name) {
			this.defaultName=name;
			return this;
		}

		private void dumpDescriptor(PropertyDescriptor descriptor) {
			if(LOGGER.isTraceEnabled()){
				LOGGER.trace(
					String.format(
						"Added property '%s' {class=%s, type=%s, editor=%s}",
						descriptor.getName(),
						descriptor.getPropertyType().getCanonicalName(),
						descriptor.getGenericPropertyType(),
						descriptor.getPropertyEditor()));
			}
		}

		<T extends AnnotatedElement & Member> PropertyDefinitionBuilder withAnnotatedMember(T element) {
			this.domain=element.getDeclaringClass();
			this.property=element.getAnnotation(org.ldp4j.rdf.bean.annotations.Property.class);
			this.cardinalityConstraints=getCardinalityConstraints(element);
			return this;
		}

		private <T extends AnnotatedElement> List<Annotation> getCardinalityConstraints(T element) {
			Annotation[] annotations = element.getAnnotations();
			List<Annotation> constraints=new ArrayList<Annotation>();
			for(Annotation annotation:annotations) {
				if(annotation.annotationType().isAnnotationPresent(CardinalityConstraint.class)) {
					constraints.add(annotation);
				}
			}
			return constraints;
		}

		PropertyDefinitionBuilder withRange(Type type) {
			this.range=getPropertyRange(type);
			this.cardinality=CardinalityDefinition.fromConstraints(type,cardinalityConstraints);
			return this;
		}

		Property forDescriptor(PropertyDescriptor descriptor) {
			dumpDescriptor(descriptor);
			return
				withDefaultName(descriptor.getName()).
				withRange(descriptor.getGenericPropertyType()).
				assemble(descriptor.getPropertyEditor());
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(PropertyScanner.class);

	private final TypeManager typeManager;

	private final String defaultNamespace;

	PropertyFactory(String defaultNamespace, TypeManager typeManager) {
		this.defaultNamespace = defaultNamespace;
		this.typeManager = typeManager;
	}

	boolean isAnnotated(AnnotatedElement element) {
		return element.isAnnotationPresent(org.ldp4j.rdf.bean.annotations.Property.class);
	}

	<T extends AnnotatedElement & Member> Property assemble(T member, PropertyDescriptor descriptor) {
		return
			new PropertyDefinitionBuilder().
				withAnnotatedMember(member).
				forDescriptor(descriptor);
	}

	Property createDefinition(Method getter) {
		assert isAnnotated(getter) : "Method '"+getter+"' is not annotated with '"+Property.class+"'";
		PropertyDescriptor descriptor=PropertyDescriptor.newDescriptor(getter);
		verifyGetterModifiers(getter);
		verifyGetterCompliance(getter);
		verifyPropertyWritability(PropertyDescriptor.getPropertyWriter(descriptor));
		return assemble(getter,descriptor);
	}

	Property createDefinition(Field field) {
		assert isAnnotated(field) : "Field '"+field+"' is not annotated with '"+Property.class+"'";
		PropertyDescriptor descriptor=PropertyDescriptor.newDescriptor(field);
		verifyPropertyWritability(field);
		return assemble(field,descriptor);
	}

	private Range getPropertyRange(Type type) {
		return new RangeExtractor(typeManager).getRange(type);
	}

	private static void fail(String message, Object... args) {
		throw new InvalidDefinitionException(String.format(message,args));
	}

	private static void verifyGetterModifiers(Method getter) {
		int modifiers = getter.getModifiers();
		assertGetterIsNotPrivate(modifiers);
		assertGetterIsNotProtected(modifiers);
		assertGetterIsNotAbstract(modifiers);
		assertGetterIsNotStatic(modifiers);
	}

	private static void verifyGetterCompliance(Method getter) {
		String methodName = getter.getName();
		boolean booleanGetter = assertGetterNameIsValid(methodName);
		assertGetterHasNoParameters(getter, methodName);
		assertGetterHasProperReturnType(getter, methodName, booleanGetter);
	}

	private static void verifyPropertyWritability(Method setter) {
		assertSetterExists(setter);
		int modifiers = setter.getModifiers();
		assertSetterIsNotPrivate(modifiers);
		assertSetterIsNotProtected(modifiers);
		assertSetterIsNotAbstract(modifiers);
		assertSetterIsNotStatic(modifiers);
	}

	private static void verifyPropertyWritability(Field field) {
		int modifiers = field.getModifiers();
		if(Modifier.isFinal(modifiers)) {
			fail("Final fields cannot be annotated as '%s'",Property.class.getName());
		}
		if(Modifier.isStatic(modifiers)) {
			fail("Static fields cannot be annotated as '%s'",Property.class.getName());
		}
	}

	private static void assertGetterIsNotStatic(int modifiers) {
		if(Modifier.isStatic(modifiers)) {
			fail("Static methods cannot be annotated as '%s'",Property.class.getName());
		}
	}

	private static void assertGetterIsNotAbstract(int modifiers) {
		if(Modifier.isAbstract(modifiers)) {
			fail("Abstract methods cannot be annotated as '%s'",Property.class.getName());
		}
	}

	private static void assertGetterIsNotProtected(int modifiers) {
		if(Modifier.isProtected(modifiers)) {
			fail("Protected methods cannot be annotated as '%s'",Property.class.getName());
		}
	}

	private static void assertGetterIsNotPrivate(int modifiers) {
		if(Modifier.isPrivate(modifiers)) {
			fail("Private methods cannot be annotated as '%s'",Property.class.getName());
		}
	}

	private static void assertGetterHasProperReturnType(Method getter, String methodName, boolean booleanGetter) {
		Class<?> tmp = getter.getReturnType();
		if(tmp.equals(Void.TYPE) || tmp.equals(Void.class)) {
			fail("Only getter methods can be annotated as '%s': method '%s' does not return value",Property.class.getName(),methodName);
		}

		if(booleanGetter && !tmp.equals(Boolean.TYPE)) {
			fail("Only getter methods can be annotated as '%s': method '%s' should return a boolean value",Property.class.getName(),methodName);
		}
	}

	private static void assertGetterHasNoParameters(Method getter, String methodName) {
		if(getter.getParameterTypes().length!=0) {
			fail("Only getter methods can be annotated as '%s': method '%s' has parameters",Property.class.getName(),methodName);
		}
	}

	private static boolean assertGetterNameIsValid(String methodName) {
		boolean booleanGetter = isBooleanGetterName(methodName);
		if(!booleanGetter && !isNonBooleanGetterName(methodName)) {
			fail("Only methods following the getter naming convention can be annotated as '%s': found '%s'",Property.class.getName(),methodName);
		}
		return booleanGetter;
	}

	private static boolean isBooleanGetterName(String methodName) {
		return methodName.startsWith("is") && methodName.length()>2 && Character.isUpperCase(methodName.charAt(2));
	}

	private static boolean isNonBooleanGetterName(String methodName) {
		return methodName.startsWith("get") && methodName.length()>3 && Character.isUpperCase(methodName.charAt(3));
	}

	private static void assertSetterIsNotStatic(int modifiers) {
		if(Modifier.isStatic(modifiers)) {
			fail("Setter method cannot be static");
		}
	}

	private static void assertSetterIsNotAbstract(int modifiers) {
		if(Modifier.isAbstract(modifiers)) {
			fail("Setter method cannot be abstract");
		}
	}

	private static void assertSetterIsNotProtected(int modifiers) {
		if(Modifier.isProtected(modifiers)) {
			fail("Setter method cannot be protected");
		}
	}

	private static void assertSetterIsNotPrivate(int modifiers) {
		if(Modifier.isPrivate(modifiers)) {
			fail("Setter method cannot be private");
		}
	}

	private static void assertSetterExists(Method setter) {
		if(setter==null) {
			fail("Property cannot be written");
		}
	}

}