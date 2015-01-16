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
package org.ldp4j.reflect.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import org.ldp4j.reflect.Reflection;
import org.ldp4j.reflect.Types;
import org.ldp4j.reflect.meta.MetaClass;
import org.ldp4j.reflect.meta.MetaConstructor;
import org.ldp4j.reflect.meta.MetaField;
import org.ldp4j.reflect.meta.MetaGenericDeclaration;
import org.ldp4j.reflect.meta.MetaMethod;
import org.ldp4j.reflect.meta.MetaTypeVariable;
import org.ldp4j.reflect.model.Modifiers;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

final class ImmutableMetaClass<T> extends ImmutableMetaAnnotatedElement<Class<T>> implements MetaClass<T> {

	private static abstract class Factory<R,S> {

		abstract S create(R data);

	}

	private interface Extractor<T,R,E extends Extractor<T,R,E>> {

		Extractor<T,R,E> allowInherit();

		R extract(Class<T> rawType) throws Exception;

	}

	private final class MetaFieldFactory extends Factory<Field, MetaField<T>> {
		@Override
		public MetaField<T> create(Field data) {
			return ImmutableMetaField.of(ImmutableMetaClass.this,data);
		}
	}

	private final class MetaConstructorFactory extends Factory<Constructor<T>,MetaConstructor<T>> {
		@Override
		public MetaConstructor<T> create(Constructor<T> data) {
			return ImmutableMetaConstructor.of(ImmutableMetaClass.this, data);
		}
	}

	private final class MetaMethodFactory extends Factory<Method, MetaMethod<T,?>> {
		@Override
		public MetaMethod<T,?> create(Method method) {
			return
				ImmutableMetaMethod.of(
						ImmutableMetaClass.this,
						method,
						ImmutableMetaClass.of(
							method.getReturnType(),
							method.getGenericReturnType()));
		}
	}

	private static abstract class AbstractExtractor<T,R,E extends AbstractExtractor<T,R,E>> implements Extractor<T,R,E> {

		private boolean allowInherit;

		protected AbstractExtractor() {
			this.allowInherit=false;
		}

		@Override
		public final Extractor<T,R,E> allowInherit() {
			this.allowInherit=true;
			return this;
		}

		protected final boolean isInheritanceAllowed() {
			return this.allowInherit;
		}

	}

	private static abstract class DefaultExtractor<T,R,E extends DefaultExtractor<T,R,E>> extends AbstractExtractor<T,R,E> {

		@Override
		public R extract(Class<T> rawType) throws Exception {
			return
				isInheritanceAllowed()?
					getInherited(rawType):
					getDeclared(rawType);
		}

		protected abstract R getInherited(Class<T> rawType) throws Exception;

		protected abstract R getDeclared(Class<T> rawType) throws Exception;

	}

	private static abstract class OptionalExtractor<T,R,E extends OptionalExtractor<T,R,E>> extends DefaultExtractor<T,R,E> {

		private final Class<? extends Throwable> exceptionType;

		protected OptionalExtractor(Class<? extends Throwable> exceptionType) {
			this.exceptionType = exceptionType;
		}

		@Override
		public R extract(Class<T> rawType) throws Exception {
			try {
				return super.extract(rawType);
			} catch (Exception exception) {
				if(!this.exceptionType.isInstance(exception)) {
					throw exception;
				}
				return null;
			}
		}

	}

	private static final class FieldExtractor<T> extends OptionalExtractor<T,Field,FieldExtractor<T>> {

		private final String fieldName;

		private FieldExtractor(String fieldName) {
			super(NoSuchFieldException.class);
			this.fieldName = fieldName;
		}

		@Override
		protected Field getInherited(Class<T> rawType) throws Exception {
			return rawType.getField(this.fieldName);
		}
		@Override
		protected Field getDeclared(Class<T> rawType) throws Exception {
			return rawType.getDeclaredField(this.fieldName);
		}

	}

	private static final class FieldArrayExtractor<T> extends DefaultExtractor<T,Field[],FieldArrayExtractor<T>> {
		@Override
		protected Field[] getInherited(Class<T> rawType) {
			return rawType.getFields();
		}
		@Override
		protected Field[] getDeclared(Class<T> rawType) {
			return rawType.getDeclaredFields();
		}
	}

	private static class ConstructorExtractor<T> extends OptionalExtractor<T, Constructor<T>,ConstructorExtractor<T>> {
		private final Class<?>[] parameterClasses;
		private ConstructorExtractor(Class<?>[] parameterClasses) {
			super(NoSuchMethodException.class);
			this.parameterClasses = parameterClasses;
		}
		@Override
		protected Constructor<T> getInherited(Class<T> rawType) throws Exception {
			return rawType.getConstructor(this.parameterClasses);
		}
		@Override
		protected Constructor<T> getDeclared(Class<T> rawType) throws Exception {
			return rawType.getDeclaredConstructor(this.parameterClasses);
		}
	}

	@SuppressWarnings("unchecked")
	private static final class ConstructorArrayExtractor<T> extends DefaultExtractor<T,Constructor<T>[],ConstructorArrayExtractor<T>> {
		@Override
		protected Constructor<T>[] getInherited(Class<T> rawType) throws Exception {
			return (Constructor<T>[]) rawType.getConstructors();
		}
		@Override
		protected Constructor<T>[] getDeclared(Class<T> rawType) {
			return (Constructor<T>[]) rawType.getDeclaredConstructors();
		}
	}

	private static class MethodExtractor<T> extends OptionalExtractor<T, Method, MethodExtractor<T>> {
		private final String methodName;
		private Class<?>[] parameterClasses;
		private MethodExtractor(String methodName, Class<?>[] parameterClasses) {
			super(NoSuchMethodException.class);
			this.methodName = methodName;
			this.parameterClasses = parameterClasses;
		}
		@Override
		protected Method getInherited(Class<T> rawType) throws Exception {
			return rawType.getMethod(this.methodName,this.parameterClasses);
		}
		@Override
		protected Method getDeclared(Class<T> rawType) throws Exception {
			return rawType.getDeclaredMethod(this.methodName,this.parameterClasses);
		}
	}

	private static final class MethodArrayExtractor<T> extends DefaultExtractor<T,Method[],MethodArrayExtractor<T>> {
		@Override
		protected Method[] getInherited(Class<T> rawType) throws Exception {
			return rawType.getMethods();
		}
		@Override
		protected Method[] getDeclared(Class<T> rawType) {
			return rawType.getDeclaredMethods();
		}
	}

	private final MetaGenericDeclaration<Class<T>> genericDeclaration;
	private final Type type;

	private ImmutableMetaClass(Class<T> rawType, Type type) {
		super(rawType);
		this.type = type;
		this.genericDeclaration=ImmutableMetaGenericDeclaration.of(rawType,type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type getType() {
		return this.type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type getResolvedType() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return get().getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSimpleName() {
		return get().getSimpleName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCanonicalName() {
		return get().getCanonicalName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaTypeVariable<Class<T>>> getTypeParameters() {
		return this.genericDeclaration.getTypeParameters();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Modifiers getModifiers() {
		return Modifiers.of(get());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaClass<?> getSuperclass() {
		MetaClass<?> result=null;
		Class<? super T> superclass = get().getSuperclass();
		if(superclass!=null) {
			result=ImmutableMetaClass.of(superclass,get().getGenericSuperclass());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaClass<?>> getInterfaces() {
		return toClassDeclarationList(get().getInterfaces(), get().getGenericInterfaces());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Package getPackage() {
		return get().getPackage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaClass<?> getEnclosingClass() {
		return ImmutableMetaClass.of(get().getEnclosingClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMemberClass() {
		return get().isMemberClass();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaClass<?> getDeclaringClass() {
		return ImmutableMetaClass.of(get().getDeclaringClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLocalClass() {
		return get().isLocalClass();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaConstructor<?> getEnclosingConstructor() {
		@SuppressWarnings("unchecked") // Guarded by construction
		Constructor<Object> enclosingConstructor = (Constructor<Object>) get().getEnclosingConstructor();
		ImmutableMetaClass<Object> declaringClass = (ImmutableMetaClass<Object>)ImmutableMetaClass.of(enclosingConstructor.getDeclaringClass());
		return ImmutableMetaConstructor.of(declaringClass,enclosingConstructor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaMethod<?,?> getEnclosingMethod() {
		Method enclosingMethod = get().getEnclosingMethod();
		ImmutableMetaClass<?> declaringClass = ImmutableMetaClass.of(enclosingMethod.getDeclaringClass());
		ImmutableMetaClass<?> returnType = ImmutableMetaClass.of(enclosingMethod.getReturnType(),enclosingMethod.getGenericReturnType());
		return ImmutableMetaMethod.of(declaringClass,enclosingMethod,returnType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAnonymousClass() {
		return get().isAnonymousClass();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInterface() {
		return get().isInterface();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPrimitive() {
		return get().isPrimitive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAnnotation() {
		return get().isAnnotation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isArray() {
		return get().isArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaClass<?> getComponentType() {
		return ImmutableMetaClass.of(get().getComponentType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnum() {
		return get().isEnum();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> getEnumConstants() {
		return ImmutableList.copyOf(get().getEnumConstants());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSynthetic() {
		return get().isSynthetic();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAssignableFrom(Class<?> clazz) {
		return get().isAssignableFrom(clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAssignableFrom(MetaClass<?> clazz) {
		return get().isAssignableFrom(clazz.get());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <U> MetaClass<? extends U> asSubclass(Class<U> clazz) {
		return ImmutableMetaClass.of(get().asSubclass(clazz));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInstance(Object object) {
		return get().isInstance(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T cast(Object object) {
		return get().cast(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaClass<?>> getClasses() {
		Class<?>[] classes = get().getClasses();
		return toClassDeclarationList(classes,classes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaField<T> getField(final String name) {
		return optional(true,new FieldExtractor<T>(name),new MetaFieldFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaField<T>> getFields() {
		return toList(true,new FieldArrayExtractor<T>(),new MetaFieldFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaConstructor<T> getConstructor(Class<?>... parameterClasses) {
		return optional(true,new ConstructorExtractor<T>(parameterClasses),new MetaConstructorFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaConstructor<T>> getConstructors() {
		return toList(true,new ConstructorArrayExtractor<T>(),new MetaConstructorFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaMethod<T, ?> getMethod(String name, Class<?>... parameterClasses) {
		return optional(true,new MethodExtractor<T>(name,parameterClasses),new MetaMethodFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaMethod<T, ?>> getMethods() {
		return toList(true,new MethodArrayExtractor<T>(),new MetaMethodFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaField<T> getDeclaredField(String name) {
		return optional(false,new FieldExtractor<T>(name),new MetaFieldFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaField<T>> getDeclaredFields() {
		return toList(false,new FieldArrayExtractor<T>(),new MetaFieldFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaConstructor<T> getDeclaredConstructor(Class<?>... parameterClasses) {
		return optional(false,new ConstructorExtractor<T>(parameterClasses),new MetaConstructorFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaConstructor<T>> getDeclaredConstructors() {
		return toList(false,new ConstructorArrayExtractor<T>(),new MetaConstructorFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaMethod<T, ?> getDeclaredMethod(String name,Class<?>... parameterClasses) {
		return optional(false,new MethodExtractor<T>(name,parameterClasses),new MetaMethodFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MetaMethod<T, ?>> getDeclaredMethods() {
		return toList(false,new MethodArrayExtractor<T>(),new MetaMethodFactory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ Objects.hashCode(this.type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj) && obj instanceof ImmutableMetaClass<?>) {
			ImmutableMetaClass<?> that=(ImmutableMetaClass<?>)obj;
			return Objects.equal(this.type,that.type);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Types.toString(this.type);
	}

	private <R,S> ImmutableList<S> toList(boolean inherit,Extractor<T,R[],?> extractor,Factory<R,S> factory) {
		if(inherit) {
			extractor=extractor.allowInherit();
		}
		return toList(secure(get(),extractor), factory);
	}

	private <R,S> S optional(boolean inherit,Extractor<T,R,?> extractor,Factory<R,S> factory) {
		if(inherit) {
			extractor=extractor.allowInherit();
		}
		R element=secure(get(),extractor);
		if(element==null) {
			return null;
		}
		return factory.create(element);
	}

	static <T> ImmutableMetaClass<T> of(Class<T> rawType, Type type) {
		return new ImmutableMetaClass<T>(rawType, type);
	}

	static <T> ImmutableMetaClass<T> of(Class<T> type) {
		return of(type, type);
	}

	static ImmutableMetaClass<?> of(Type type) {
		return of(Reflection.erasureOf(type), type);
	}

	private static List<MetaClass<?>> toClassDeclarationList(Class<?>[] rawTypes, Type[] types) {
		Builder<MetaClass<?>> builder=ImmutableList.<MetaClass<?>>builder();
		for(int i=0;i<rawTypes.length;i++) {
			builder.add(ImmutableMetaClass.of(rawTypes[i],types[i]));
		}
		return builder.build();
	}

	private static <S, R> ImmutableList<S> toList(R[] sourceElements,Factory<R, S> factory) {
		Builder<S> builder = ImmutableList.<S>builder();
		for(R element:sourceElements) {
			builder.add(factory.create(element));
		}
		return builder.build();
	}

	private static <T,R> R secure(final Class<T> rawType, final Extractor<T,R,?> action) {
		return
			AccessController.doPrivileged(
				new PrivilegedAction<R>() {
					@Override
					public R run() {
						try {
							return action.extract(rawType);
						} catch (SecurityException e) {
							throw new IllegalStateException("Should not fail when running in priviledged mode");
						} catch (Exception e) {
							throw new IllegalStateException("Unexpected action failure",e);
						}
					}
				}
			);
	}

}
