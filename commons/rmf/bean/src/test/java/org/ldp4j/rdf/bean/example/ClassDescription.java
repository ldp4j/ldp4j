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
package org.ldp4j.rdf.bean.example;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ClassDescription<T> {

	private final Class<T> type;
	private final ClassWrapper classWrapper;

	private static interface IPrinter<E> {

		public String toString(E element);

	}

	private ClassDescription(Class<T> type) {
		this.type = type;
		this.classWrapper = new ClassWrapper(type);
	}

	public abstract static class AbstractPrinter<T> implements IPrinter<T> {

		private static final String FIELD_SEPARATOR = ", ";
		private static final String BLOCK_OPENING = "[";
		private static final String BLOCK_CLOSING = "]";
		private static final String COMPOSITE_OPENING = "{";
		private static final String COMPOSITE_CLOSING = "}";
		private static final String INDEXATION = "\t";
		private static final String NEW_LINE = System.getProperty("line.separator");

		private static class Entry {

			private String field;
			private Object value;

			public Entry withField(String field) {
				this.field=field;
				return this;
			}

			public Entry withValue(Object value) {
				this.value=value;
				return this;
			}

		}

		private List<Entry> fields=new ArrayList<Entry>();
		private String title="Type";
		private boolean excludePublicElementsFromDeclared=false;

		public AbstractPrinter<T> excludePublicElementsFromDeclared(boolean excludePublicElementsFromDeclared) {
			this.excludePublicElementsFromDeclared = excludePublicElementsFromDeclared;
			return this;
		}

		public boolean excludePublicElementsFromDeclared() {
			return excludePublicElementsFromDeclared;
		}

		@Override
		public final String toString(T type) {
			String result=null;
			if(type!=null) {
				processType(type);
				StringBuilder builder=new StringBuilder();
				if(!fields.isEmpty()) {
					builder.append(title).append(" ").append(BLOCK_OPENING);
					boolean first=true;
					for(Entry entry:fields) {
						if(!first) {
							builder.append(FIELD_SEPARATOR);
						} else {
							first=false;
						}
						builder.append(NEW_LINE).append(INDEXATION).append(entry.field).append("=").append(entry.value);
					}
					builder.append(NEW_LINE).append(BLOCK_CLOSING);
				}
				result=builder.toString();
			}
			return result;
		}

		private static final Class<?>[][] ELEMENT_WRAPPER_MAP={
			{Constructor.class, ConstructorWrapper.class},
			{Annotation.class, AnnotationWrapper.class},
			{Method.class, MethodWrapper.class},
			{Field.class, FieldWrapper.class},
			{Type.class, TypeWrapper.class},
			{Member.class,MemberWrapper.class},
			{AnnotatedElement.class,AnnotatedElementWrapper.class},
			{GenericDeclaration.class,GenericDeclarationWrapper.class}
		};

		private <E> Constructor<Wrapper<E>> getWrapperConstructor(Class<E> clazz) {
			for(Class<?>[] mapping:ELEMENT_WRAPPER_MAP) {
				if(mapping[0].equals(clazz)) {
					return getConstructor(mapping[1],mapping[0]);
				}
			}
			for(Class<?>[] mapping:ELEMENT_WRAPPER_MAP) {
				if(mapping[0].isAssignableFrom(clazz)) {
					return getConstructor(mapping[1],mapping[0]);
				}
			}
			throw new IllegalArgumentException("Don't know how to wrap element of type '"+clazz.getName()+"'");
		}

		private <E> Constructor<Wrapper<E>> getConstructor(Class<?> wrapperClass, Class<?> sourceClass) {
			try {
				@SuppressWarnings("unchecked")
				Constructor<Wrapper<E>> result = (Constructor<Wrapper<E>>)wrapperClass.getConstructor(sourceClass);
				return result;
			} catch (SecurityException e) {
				throw new IllegalStateException("Could not retrieve constructor for wrapper class",e);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("Could not retrieve constructor for wrapper class",e);
			} catch (ClassCastException e) {
				throw new IllegalStateException("Could not retrieve constructor for wrapper class",e);
			}
		}

		@SuppressWarnings("unchecked")
		private <S> IPrinter<S> getPrinter(S value) {
			IPrinter<S> result=null;
			if(Annotation.class.isInstance(value)) {
				result=(IPrinter<S>)new AnnotationPrinter();
			} else if(Constructor.class.isInstance(value)) {
				result=(IPrinter<S>)new ConstructorPrinter();
			} else if(Method.class.isInstance(value)) {
				result=(IPrinter<S>)new MethodPrinter();
			} else if(Field.class.isInstance(value)) {
				result=(IPrinter<S>)new FieldPrinter();
			}
			return result;
		}

		private String toRawString(Object value) {
			String valueString=null;
			IPrinter<Object> printer=getPrinter(value);
			if(printer==null) {
				valueString=value.toString();
			} else {
				valueString=printer.toString(value);
			}
			return valueString;
		}

		protected final AbstractPrinter<T> addTitle(String title)  {
			this.title=title;
			return this;
		}

		protected final AbstractPrinter<T> addField(String field, Object value) {
			if(value==null) {
				return this;
			}

			String valueString=value.toString();
			if(valueString.isEmpty()) {
				return this;
			}

			this.fields.add(new Entry().withField(field).withValue(value));
			return this;
		}

		protected final AbstractPrinter<T> addBlockField(String field, Object value)  {
			if(value==null) {
				return this;
			}
			String valueString = toRawString(value);
			if(valueString.isEmpty()) {
				return this;
			}

			this.fields.add(new Entry().withField(field).withValue(COMPOSITE_OPENING+NEW_LINE+INDEXATION+INDEXATION+valueString.replace(NEW_LINE,NEW_LINE+INDEXATION+INDEXATION)+NEW_LINE+INDEXATION+COMPOSITE_CLOSING));
			return this;
		}

		protected final AbstractPrinter<T> addMultiField(String field, Object[] values)  {
			if(values==null || values.length==0) {
				return this;
			}

			return addMultiField(field,Arrays.asList(values));
		}

		protected final AbstractPrinter<T> addMultiField(String field, Collection<?> values)  {
			if(values==null || values.isEmpty()) {
				return this;
			}

			StringBuilder builder=new StringBuilder();
			builder.append(COMPOSITE_OPENING);
			boolean first=true;
			boolean nonEmpty=false;
			for(Object value:values) {
				if(value==null) {
					continue;
				}
				String valueString = toRawString(value);
				if(valueString.isEmpty()) {
					continue;
				}
				if(!first) {
					builder.append(FIELD_SEPARATOR);
				} else {
					first=false;
				}
				builder.append(NEW_LINE).append(INDEXATION).append(valueString.replace(NEW_LINE,NEW_LINE+INDEXATION));
				nonEmpty=true;
			}
			builder.append(NEW_LINE).append(COMPOSITE_CLOSING);
			if(nonEmpty) {
				this.fields.add(new Entry().withField(field).withValue(builder.toString().replace(NEW_LINE,NEW_LINE+INDEXATION)));
			}
			return this;
		}

		protected final <E, S extends E> Wrapper<E> wrapElement(S element) {
			if(element==null) {
				return null;
			}
			@SuppressWarnings("unchecked")
			Class<E> clazz = (Class<E>)element.getClass();
			return wrapElementAs(element, clazz);
		}

		protected final List<Wrapper<?>> wrapElements(List<?> elements) {
			List<Wrapper<?>> result=new ArrayList<Wrapper<?>>();
			if(elements!=null) {
				for(Object element:elements) {
					result.add(wrapElement(element));
				}
			}
			return result;
		}

		protected final List<Wrapper<?>> wrapElementArray(Object[] elements) {
			List<Wrapper<?>> result=null;
			if(elements!=null) {
				result=wrapElements(Arrays.asList(elements));
			} else {
				result=Collections.emptyList();
			}
			return result;
		}

		protected final <E, S extends E> Wrapper<E> wrapElementAs(S element, Class<E> clazz) {
			if(element==null) {
				return null;
			}
			try {
				Wrapper<E> result = getWrapperConstructor(clazz).newInstance(element);
				result.excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
				return result;
			} catch (Exception e) {
				throw new IllegalArgumentException("Could not instantiate wrapper ",e);
			}
		}

		protected final <E, S extends E, W extends Wrapper<E>> W wrapElementWith(S element, Class<W> wrapperClass) {
			if(element==null) {
				return null;
			}
			try {
				Constructor<W> result = wrapperClass.getConstructor(element.getClass());
				W newInstance = result.newInstance(element);
				newInstance.excludePublicElementsFromDeclared(excludePublicElementsFromDeclared);
				return newInstance;
			} catch (SecurityException e) {
				throw new IllegalStateException("Could not retrieve constructor for wrapper class",e);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("Could not retrieve constructor for wrapper class",e);
			} catch (ClassCastException e) {
				throw new IllegalStateException("Could not retrieve constructor for wrapper class",e);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Could not instantiate wrapper ",e);
			} catch (InstantiationException e) {
				throw new IllegalArgumentException("Could not instantiate wrapper ",e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Could not instantiate wrapper ",e);
			} catch (InvocationTargetException e) {
				throw new IllegalArgumentException("Could not instantiate wrapper ",e);
			}
		}

		protected final <E, S extends E, W extends Wrapper<E>> W wrapElementAsWith(S element, Class<E> clazz, Class<W> wrapperClass) {
			if(element==null) {
				return null;
			}
			try {
				Constructor<W> result = wrapperClass.getConstructor(clazz);
				return result.newInstance(element);
			} catch (SecurityException e) {
				throw new IllegalStateException("Could not retrieve constructor for wrapper class",e);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("Could not retrieve constructor for wrapper class",e);
			} catch (ClassCastException e) {
				throw new IllegalStateException("Could not retrieve constructor for wrapper class",e);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Could not instantiate wrapper ",e);
			} catch (InstantiationException e) {
				throw new IllegalArgumentException("Could not instantiate wrapper ",e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Could not instantiate wrapper ",e);
			} catch (InvocationTargetException e) {
				throw new IllegalArgumentException("Could not instantiate wrapper ",e);
			}
		}

		protected abstract void processType(T type);

	}

	private static class DefaultTypePrinter extends AbstractPrinter<Type> {

		@Override
		protected void processType(Type type) {
			super.addTitle("Type");
			super.addField("class", type.toString());
		}

	}

	private static class WildcardTypePrinter extends AbstractPrinter<WildcardType> {

		@Override
		protected void processType(WildcardType type) {
			super.addTitle("WildcardType");
			super.addMultiField("lower bounds", wrapElementArray(type.getLowerBounds()));
			super.addMultiField("upper bounds", wrapElementArray(type.getUpperBounds()));
		}

	}

	private static class GenericArrayTypePrinter extends AbstractPrinter<GenericArrayType> {

		@Override
		protected void processType(GenericArrayType type) {
			super.addTitle("GenericArrayType");
			super.addBlockField("generic component type",wrapElement(type.getGenericComponentType()));
		}

	}

	private static class ParameterizedTypePrinter extends AbstractPrinter<ParameterizedType> {

		@Override
		protected void processType(ParameterizedType type) {
			super.addTitle("ParameterizedType");
			super.addField("owner type", type.getOwnerType());
			super.addField("raw type", type.getRawType());
			super.addMultiField("actual type parameters",wrapElementArray(type.getActualTypeArguments()));
		}

	}

	private static class TypeVariablePrinter extends AbstractPrinter<TypeVariable<?>> {

		@Override
		protected void processType(TypeVariable<?> type) {
			super.addTitle("TypeVariable");
			super.addField("generic declaration", type.getGenericDeclaration());
			super.addField("name", type.getName());
			super.addMultiField("bounds",wrapElementArray(type.getBounds()));
		}

	}

	public static class AnnotatedElementPrinter extends AbstractPrinter<AnnotatedElement> {

		@Override
		protected void processType(AnnotatedElement type) {
			String suffix="";
			String prefix="";
			if(excludePublicElementsFromDeclared()) {
				suffix=" (excluding public)";
				prefix="public ";
			}
			Annotation[] annotations=type.getAnnotations();

			super.addTitle("AnnotatedElement");
			super.addMultiField(prefix+"annotations", wrapElementArray(annotations));
			super.addMultiField("declared annotations"+suffix, wrapElements(publicElementFilter(annotations,type.getDeclaredAnnotations())));
		}

		private <T> List<T> publicElementFilter(T[] publicElements, T[] declaredElements) {
			List<T> nonPublicClasses=new ArrayList<T>(Arrays.asList(declaredElements));
			if(excludePublicElementsFromDeclared()) {
				nonPublicClasses.removeAll(Arrays.asList(publicElements));
			}
			return nonPublicClasses;
		}

	}

	private static class MemberPrinter extends AbstractPrinter<Member> {

		@Override
		protected void processType(Member type) {
			super.addTitle("Member");
			super.addField("name", type.getName());
			super.addField("declaringClass", type.getDeclaringClass());
			super.addField("modifiers", Modifier.toString(type.getModifiers()));
			super.addField("synthetic", type.isSynthetic());
		}

	}

	private static class GenericDeclarationPrinter extends AbstractPrinter<GenericDeclaration> {

		@Override
		protected void processType(GenericDeclaration type) {
			super.addTitle("GenericDeclaration");
			super.addMultiField("typeParameters", wrapElementArray(type.getTypeParameters()));
		}

	}

	private static final class AnnotationPrinter extends AbstractPrinter<Annotation> {

		@Override
		protected void processType(Annotation type) {
			super.addTitle("Annotation");
			super.addField("annotationType", type.annotationType());
		}

	}

	private static class FieldPrinter extends AbstractPrinter<Field> {

		@Override
		protected void processType(Field type) {
			super.addTitle("Field");
			super.addBlockField("member", wrapElementAs(type,Member.class));
			super.addBlockField("annotated element", wrapElementAs(type,AnnotatedElement.class));
			super.addField("accessible", type.isAccessible());
			super.addField("type", type.getType());
			super.addField("generic type", type.getGenericType());
			super.addField("enum constant", type.isEnumConstant());
		}

	}

	private static class MethodPrinter extends AbstractPrinter<Method> {

		@Override
		protected void processType(Method type) {
			super.addTitle("Method");
			super.addBlockField("member", wrapElementAs(type,Member.class));
			super.addBlockField("generic declaration", wrapElementAs(type,GenericDeclaration.class));
			super.addBlockField("annotated element", wrapElementAs(type,AnnotatedElement.class));
			super.addField("accessible", type.isAccessible());
			super.addField("return type",type.getReturnType());
			super.addField("generic return type",type.getGenericReturnType());
			super.addMultiField("parameter types",type.getParameterTypes());
			super.addMultiField("generic parameter types",type.getGenericParameterTypes());
			Annotation[][] parameterAnnotations = type.getParameterAnnotations();
			List<String> annots=new ArrayList<String>();
			for(Annotation[] pa:parameterAnnotations) {
				annots.add(Arrays.toString(pa));
			}
			super.addMultiField("parameter annotations",annots);
			super.addField("bridge", type.isBridge());
			super.addField("var args",type.isVarArgs());
			super.addMultiField("exception types",type.getExceptionTypes());
			super.addMultiField("generic exception types",type.getGenericExceptionTypes());
		}

	}

	private static class ConstructorPrinter extends AbstractPrinter<Constructor<?>> {

		@Override
		protected void processType(Constructor<?> type) {
			super.addTitle("Constructor");
			super.addBlockField("member", wrapElementAs(type,Member.class));
			super.addBlockField("generic declaration", wrapElementAs(type,GenericDeclaration.class));
			super.addBlockField("annotated element", wrapElementAs(type,AnnotatedElement.class));
			super.addField("accessible", type.isAccessible());
			super.addMultiField("parameter types",type.getParameterTypes());
			super.addMultiField("generic parameter types",type.getGenericParameterTypes());
			Annotation[][] parameterAnnotations = type.getParameterAnnotations();
			List<String> annots=new ArrayList<String>();
			for(Annotation[] pa:parameterAnnotations) {
				annots.add(Arrays.toString(pa));
			}
			super.addMultiField("parameter annotations",annots);
			super.addField("var args",type.isVarArgs());
			super.addMultiField("exception types",type.getExceptionTypes());
			super.addMultiField("generic exception types",type.getGenericExceptionTypes());
		}

	}

	private static class ClassPrinter extends AbstractPrinter<Class<?>> {

		private boolean ignoreDefaultSuperclass;
		private boolean ignoreDefaultMethods;
		private boolean recursive;
		private boolean traverseInterfaces;
		private boolean traverseClasses;
		private boolean traverseSuperclass;

		public ClassPrinter() {
		}

		public ClassPrinter excludePublicElementsFromDeclared(boolean excludePublicElementsFromDeclared) {
			super.excludePublicElementsFromDeclared(excludePublicElementsFromDeclared);
			return this;
		}

		public ClassPrinter ignoreDefaultSuperclass(boolean ignoreDefaultSuperclass) {
			this.ignoreDefaultSuperclass = ignoreDefaultSuperclass;
			return this;
		}

		public ClassPrinter ignoreDefaultMethods(boolean ignoreDefaultMethods) {
			this.ignoreDefaultMethods = ignoreDefaultMethods;
			return this;
		}

		public ClassPrinter traverseInterfaces(boolean traverseInterfaces) {
			this.traverseInterfaces = traverseInterfaces;
			return this;
		}

		public ClassPrinter traverseClasses(boolean traverseClasses) {
			this.traverseClasses = traverseClasses;
			return this;
		}

		public ClassPrinter traverseSuperclass(boolean traverseSuperclass) {
			this.traverseSuperclass = traverseSuperclass;
			return this;
		}
		public ClassPrinter recursive(boolean recursive) {
			this.recursive = recursive;
			return this;
		}

		@Override
		protected void processType(Class<?> type) {
			super.addTitle("Class");
			super.addField("name", type.getName());
			super.addField("package", type.getPackage());
			super.addField("simple name", type.getSimpleName());
			super.addField("canonical name", type.getCanonicalName());
			super.addField("interface", type.isInterface());
			super.addField("annotation", type.isAnnotation());
			super.addField("primitive", type.isPrimitive());
			super.addField("enum", type.isEnum());
			super.addMultiField("enum constants", type.getEnumConstants());
			super.addField("array", type.isArray());
			super.addField("component type", type.getComponentType());
			super.addField("anonymous class", type.isAnonymousClass());
			super.addField("local class", type.isLocalClass());
			super.addField("member class", type.isMemberClass());
			super.addField("declaring class", type.getDeclaringClass());
			super.addField("modifiers", Modifier.toString(type.getModifiers()));
			super.addField("synthetic", type.isSynthetic());
			super.addField("enclosing method", type.getEnclosingMethod());
			super.addField("enclosing constructor", type.getEnclosingConstructor());
			super.addField("enclosing class", type.getEnclosingClass());
			super.addBlockField("generic declaration", wrapElementAs(type,GenericDeclaration.class));
			super.addBlockField("annotated element", wrapElementAs(type,AnnotatedElement.class));

			Class<?> superclass=type.getSuperclass();
			if(superclass!=null) {
				if(!superclass.equals(Object.class) || !ignoreDefaultSuperclass) {
					if(traverseSuperclass) {
						super.addBlockField("superclass", wrap(type.getSuperclass()));
					} else {
						super.addField("superclass", type.getSuperclass());
					}
				}
			}
			super.addBlockField("generic superclass", wrapElement(type.getGenericSuperclass()));

			if(traverseInterfaces) {
				super.addMultiField("interfaces", wrap(type.getInterfaces()));
			} else {
				super.addMultiField("interfaces", type.getInterfaces());
			}
			super.addMultiField("generic interfaces", wrapElementArray(type.getGenericInterfaces()));

			String suffix="";
			String prefix="";
			if(excludePublicElementsFromDeclared()) {
				suffix=" (excluding public)";
				prefix="public ";
			}

			Class<?>[] classes = type.getClasses();
			List<Class<?>> declaredClasses = publicElementFilter(classes,type.getDeclaredClasses(),excludePublicElementsFromDeclared());
			if(traverseClasses) {
				super.addMultiField(prefix+"classes", wrap(classes));
				super.addMultiField("declared classes"+suffix, wrap(declaredClasses.toArray(new Class[]{})));
			} else {
				super.addMultiField(prefix+"classes", classes);
				super.addMultiField("declared classes"+suffix, declaredClasses);
			}
			Field[] fields = type.getFields();
			super.addMultiField(prefix+"fields", wrapElementArray(fields));
			super.addMultiField("declared fields"+suffix, wrapElements(publicElementFilter(fields,type.getDeclaredFields(),excludePublicElementsFromDeclared())));

			Method[] methods = type.getMethods();
			String subprefix="";
			if(ignoreDefaultMethods) {
				subprefix="non-default ";
			}

			super.addMultiField(prefix+subprefix+"methods", defaultMethodFilter(Arrays.asList(methods),ignoreDefaultMethods));
			super.addMultiField(prefix+subprefix+"declared methods"+suffix, defaultMethodFilter(publicElementFilter(methods,type.getDeclaredMethods(),excludePublicElementsFromDeclared()),ignoreDefaultMethods));

			Constructor<?>[] constructors=type.getConstructors();
			super.addMultiField(prefix+"constructors", wrapElementArray(constructors));
			super.addMultiField("declared constructors"+suffix, wrapElements(publicElementFilter(constructors,type.getDeclaredConstructors(),excludePublicElementsFromDeclared())));
		}


		private List<ClassWrapper> wrap(Class<?>[] classes) {
			List<ClassWrapper> wrappedClasses = new ArrayList<ClassWrapper>();
			for(Class<?> w:classes) {
				wrappedClasses.add(wrap(w));
			}
			return wrappedClasses;
		}

		private ClassWrapper wrap(Class<?> clazz) {
			ClassWrapper e =
				new ClassWrapper(clazz).
					recursive(recursive).
					excludePublicElementsFromDeclared(excludePublicElementsFromDeclared()).
					ignoreDefaultMethods(ignoreDefaultMethods).
					ignoreDefaultSuperclass(ignoreDefaultSuperclass).
					traverseClasses(traverseClasses && recursive).
					traverseInterfaces(traverseInterfaces && recursive).
					traverseSuperclass(traverseSuperclass && recursive);
			return e;
		}

		private <T> List<T> publicElementFilter(T[] publicElements, T[] declaredElements, boolean filter) {
			List<T> nonPublicClasses=new ArrayList<T>(Arrays.asList(declaredElements));
			if(filter) {
				nonPublicClasses.removeAll(Arrays.asList(publicElements));
			}
			return nonPublicClasses;
		}

		private List<Wrapper<?>> defaultMethodFilter(List<Method> methods, boolean filter) {
			List<Wrapper<?>> result=new ArrayList<Wrapper<?>>();
			for(Method method:methods) {
				MethodWrapper w=new MethodWrapper(method);
				w.excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
				if(!w.isFromKernel() || !filter) {
					result.add(w);
				}
			}
			return result;
		}

	}

	private static interface IWrapper<E> {

		public E getElement();
	}

	private abstract static class Wrapper<E> implements IWrapper<E> {

		private final E element;

		public Wrapper(E element) {
			this.element = element;
		}

		public final E getElement() {
			return element;
		}

		private boolean excludePublicElementsFromDeclared=false;

		public Wrapper<E> excludePublicElementsFromDeclared(boolean excludePublicElementsFromDeclared) {
			this.excludePublicElementsFromDeclared = excludePublicElementsFromDeclared;
			return this;
		}

		public boolean excludePublicElementsFromDeclared() {
			return excludePublicElementsFromDeclared;
		}

		@Override
		public final String toString() {
			if(getElement()==null) {
				return null;
			}
			return createPrinter().toString(getElement());
		}

		protected abstract IPrinter<E> createPrinter();

	}

	private static class TypeWrapper<T extends Type> extends Wrapper<T> {

		protected IPrinter<T> createPrinter() {
			T element=getElement();
			IPrinter<T> result;
			if(ParameterizedType.class.isInstance(element)) {
				@SuppressWarnings("unchecked")
				IPrinter<T> raw = (IPrinter<T>) new ParameterizedTypePrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
				result=raw;
			} else if(TypeVariable.class.isInstance(element)) {
				@SuppressWarnings("unchecked")
				IPrinter<T> raw = (IPrinter<T>) new TypeVariablePrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
				result=raw;
			} else if(WildcardType.class.isInstance(element)){
				@SuppressWarnings("unchecked")
				IPrinter<T> raw = (IPrinter<T>) new WildcardTypePrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
				result=raw;
			} else if(GenericArrayType.class.isInstance(element)){
				@SuppressWarnings("unchecked")
				IPrinter<T> raw = (IPrinter<T>) new GenericArrayTypePrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
				result=raw;
			} else {
				@SuppressWarnings("unchecked")
				IPrinter<T> raw = (IPrinter<T>) new DefaultTypePrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
				result=raw;
			}
			return result;
		}

		public TypeWrapper(T type) {
			super(type);
		}

	}

	private static class AnnotationWrapper extends Wrapper<Annotation> {

		public AnnotationWrapper(Annotation annotation) {
			super(annotation);
		}

		@Override
		protected final IPrinter<Annotation> createPrinter() {
			return new AnnotationPrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
		}

	}

	private static class FieldWrapper extends Wrapper<Field> {

		public FieldWrapper(Field field) {
			super(field);
		}

		@Override
		protected final IPrinter<Field> createPrinter() {
			return new FieldPrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
		}

	}

	private static class MethodWrapper extends Wrapper<Method>{

		public MethodWrapper(Method method) {
			super(method);
		}

		public boolean isFromKernel() {
			return getElement().getDeclaringClass().equals(Object.class);
		}

		@Override
		protected IPrinter<Method> createPrinter() {
			return new MethodPrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
		}

	}

	private static class ConstructorWrapper extends Wrapper<Constructor<?>> {

		public ConstructorWrapper(Constructor<?> constructor) {
			super(constructor);
		}

		@Override
		protected IPrinter<Constructor<?>> createPrinter() {
			return new ConstructorPrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
		}

	}

	private static class ClassWrapper extends TypeWrapper<Class<?>> {

		private boolean recursive=false;
		private boolean traverseInterfaces=false;
		private boolean ignoreDefaultMethods=false;
		private boolean ignoreDefaultSuperclass=false;
		private boolean traverseSuperclass=false;
		private boolean traverseClasses=false;

		public ClassWrapper(Class<?> clazz) {
			super(clazz);
		}

		public ClassWrapper recursive(boolean recursive) {
			this.recursive = recursive;
			return this;
		}

		public ClassWrapper ignoreDefaultSuperclass(boolean ignoreDefaultSuperclass) {
			this.ignoreDefaultSuperclass = ignoreDefaultSuperclass;
			return this;
		}

		public ClassWrapper ignoreDefaultMethods(boolean ignoreDefaultMethods) {
			this.ignoreDefaultMethods = ignoreDefaultMethods;
			return this;
		}

		public ClassWrapper traverseInterfaces(boolean traverseInterfaces) {
			this.traverseInterfaces = traverseInterfaces;
			return this;
		}

		public ClassWrapper traverseSuperclass(boolean traverseSuperclass) {
			this.traverseSuperclass = traverseSuperclass;
			return this;
		}

		public ClassWrapper traverseClasses(boolean traverseClasses) {
			this.traverseClasses = traverseClasses;
			return this;
		}

		public ClassWrapper excludePublicElementsFromDeclared(boolean excludePublicElementsFromDeclared) {
			super.excludePublicElementsFromDeclared(excludePublicElementsFromDeclared);
			return this;
		}

		@Override
		protected IPrinter<Class<?>> createPrinter() {
			return
				new ClassPrinter().
					excludePublicElementsFromDeclared(excludePublicElementsFromDeclared()).
					ignoreDefaultMethods(ignoreDefaultMethods).
					ignoreDefaultSuperclass(ignoreDefaultSuperclass).
					recursive(recursive).
					traverseInterfaces(traverseInterfaces).
					traverseClasses(traverseClasses).
					traverseSuperclass(traverseSuperclass);
		}

	}

	private static class GenericDeclarationWrapper extends Wrapper<GenericDeclaration> {

		public GenericDeclarationWrapper(GenericDeclaration element) {
			super(element);
		}

		@Override
		protected IPrinter<GenericDeclaration> createPrinter() {
			return new GenericDeclarationPrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
		}

	}

	private static class MemberWrapper extends Wrapper<Member> {

		public MemberWrapper(Member element) {
			super(element);
		}

		@Override
		protected IPrinter<Member> createPrinter() {
			return new MemberPrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
		}

	}

	private static class AnnotatedElementWrapper extends Wrapper<AnnotatedElement> {

		public AnnotatedElementWrapper(AnnotatedElement element) {
			super(element);
		}

		@Override
		protected IPrinter<AnnotatedElement> createPrinter() {
			return new AnnotatedElementPrinter().excludePublicElementsFromDeclared(excludePublicElementsFromDeclared());
		}

	}

	public ClassDescription<T> excludePublicElementsFromDeclared(boolean excludePublicElementsFromDeclared) {
		classWrapper.excludePublicElementsFromDeclared(excludePublicElementsFromDeclared);
		return this;
	}

	public ClassDescription<T> ignoreDefaultSuperclass(boolean ignoreDefaultSuperclass) {
		classWrapper.ignoreDefaultSuperclass(ignoreDefaultSuperclass);
		return this;
	}

	public ClassDescription<T> ignoreDefaultMethods(boolean ignoreDefaultMethods) {
		classWrapper.ignoreDefaultMethods(ignoreDefaultMethods);
		return this;
	}

	public ClassDescription<T> traverseInterfaces(boolean traverseInterfaces) {
		classWrapper.traverseInterfaces(traverseInterfaces);
		return this;
	}

	public ClassDescription<T> traverseSuperclass(boolean traverseSuperclass) {
		classWrapper.traverseSuperclass(traverseSuperclass);
		return this;
	}

	public ClassDescription<T> recursive(boolean recursive) {
		classWrapper.recursive(recursive);
		return this;
	}
	public ClassDescription<T> traverseClasses(boolean traverseClasses) {
		classWrapper.traverseClasses(traverseClasses);
		return this;
	}

	public Class<T> getType() {
		return type;
	}

	@Override
	public String toString() {
		return classWrapper.toString();
	}

	public static <T> ClassDescription<T> newInstance(Class<T> type) {
		return new ClassDescription<T>(type);
	}

}