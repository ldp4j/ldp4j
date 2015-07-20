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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Introspection {

	private static final class IntrospectorUtil {

		private static final Logger LOGGER=LoggerFactory.getLogger(IntrospectorUtil.class);

		private IntrospectorUtil() {
		}

		private static final String getMethodName(String property) {
			return String.format("set%s%s",property.substring(0,1).toUpperCase(Locale.ENGLISH),property.substring(1));
		}

		static Method findSetter(Class<?> originalBeanClass, String propertyName, Class<?> propertyType) {
			String setter = getMethodName(propertyName);
			Class<?> beanClass=originalBeanClass;
			while(beanClass!=null) {
				Method method = findMethod(beanClass, propertyType, setter);
				if(method!=null) {
					return method;
				}
				beanClass=beanClass.getSuperclass();
			}
			String message=String.format("Class '%s' does not expose a setter method for property '%s'",originalBeanClass.getCanonicalName(),propertyName);
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace(message);
			}
			throw new IllegalArgumentException(message);
		}

		private static Method findMethod(final Class<?> clazz, final Class<?> type, final String methodName) {
			return java.security.AccessController.doPrivileged(
				new PrivilegedAction<Method>() {
					@Override
					public Method run() {
						Method method=null;
						try {
							method= clazz.getDeclaredMethod(methodName,type);
							method.setAccessible(true);
						} catch (SecurityException e) {
							throw new IllegalStateException(String.format("Method '%s' in class '%s' cannot be accessed",methodName, clazz.getName()),e);
						} catch (NoSuchMethodException e) {
							if(LOGGER.isTraceEnabled()) {
								LOGGER.trace(String.format("Method '%s' not found in class '%s'",methodName, clazz.getName()),e);
							}
						}
						return method;
					}
				}
			);
		}

	}

	public abstract static class AbstractPropertyEditor<T,V> {

		private static final Logger LOGGER=LoggerFactory.getLogger(AbstractPropertyEditor.class);

		private final Class<? extends T> beanClass;
		private final String propertyName;
		private final Class<? extends V> propertyType;
		private final Method setter;

		AbstractPropertyEditor(
				Class<? extends T> clazz,
				String propertyName,
				Class<? extends V> propertyType,
				Method setter) {
			this.beanClass = clazz;
			this.propertyName = propertyName;
			this.propertyType = propertyType;
			this.setter = setter;
		}

		final void update(T targetBean, V value) {
			try {
				setter.invoke(targetBean, value);
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("[{}] Property '{}' set to '{}'",targetBean, getPropertyName(), value);
				}
			} catch (IllegalArgumentException e) {
				throw handleFailure(e,"Method '%s' does not support value '%s'",setter,value);
			} catch (IllegalAccessException e) {
				throw handleFailure(e,"Method '%s' is not accessible",setter);
			} catch (InvocationTargetException e) {
				throw handleFailure(e,"Method '%s' failed",setter);
			}

		}

		public final Class<? extends T> getBeanClass() {
			return beanClass;
		}

		public final String getPropertyName() {
			return propertyName;
		}

		public final Class<? extends V> getPropertyType() {
			return propertyType;
		}

		private static Error handleFailure(Throwable t, String format, Object... args) {
			String message=String.format(format,args);
			if(LOGGER.isErrorEnabled()) {
				if(t!=null) {
					LOGGER.error(message.concat(". Full stacktrace follows"),t);
				} else {
					LOGGER.trace(message);
				}
			}
			AssertionError error = new AssertionError(message);
			error.initCause(t);
			return error;
		}

	}

	public static final class ClassIntrospector<T> {

		public static final class ClassPropertyEditor<T,V> extends AbstractPropertyEditor<T,V>  {

			private ClassPropertyEditor(Class<? extends T> clazz, String propertyName, Class<? extends V> propertyType, Method setter) {
				super(clazz,propertyName,propertyType,setter);
			}

			public void set(T targetBean, V value) {
				update(targetBean,value);
			}

		}

		private final Class<T> baseClass;

		private ClassIntrospector(Class<T> baseClass) {
			this.baseClass = baseClass;
		}

		public <V> ClassPropertyEditor<? super T,? super V> updateableProperty(String propertyName, Class<? extends V> propertyType) {
			Method setter = IntrospectorUtil.findSetter(baseClass, propertyName, propertyType);
			return new ClassPropertyEditor<T,V>(baseClass,propertyName,propertyType,setter);
		}

	}

	public static final class BeanIntrospector<T> {

		public static final class BeanPropertyEditor<T,V> extends AbstractPropertyEditor<T,V> {

			private final T targetBean;

			private BeanPropertyEditor(Class<? extends T> clazz, T bean, String propertyName, Class<? extends V> propertyType, Method setter) {
				super(clazz,propertyName,propertyType,setter);
				this.targetBean = bean;
			}

			public T getTargetBean() {
				return targetBean;
			}

			public void withValue(V value) {
				super.update(getTargetBean(), value);
			}

		}

		private final Class<T> baseClass;
		private final T bean;

		private BeanIntrospector(Class<T> baseClass, T bean) {
			this.baseClass = baseClass;
			this.bean = bean;
		}

		public <V> BeanPropertyEditor<T,V> updateProperty(String propertyName, Class<? extends V> propertyType) {
			Method method = IntrospectorUtil.findSetter(baseClass, propertyName,propertyType);
			return new BeanPropertyEditor<T,V>(baseClass,bean,propertyName,propertyType,method);
		}

	}

	private Introspection() {
	}

	public static <S> ClassIntrospector<S> create(Class<S> baseClass) {
		if(baseClass==null) {
			throw new IllegalArgumentException("Object 'baseClass' cannot be null");
		}
		return new ClassIntrospector<S>(baseClass);
	}

	public static <S> BeanIntrospector<S> create(S targetBean) {
		if(targetBean==null) {
			throw new IllegalArgumentException("Object 'targetBean' cannot be null");
		}
		@SuppressWarnings("unchecked")
		Class<S> baseClass=(Class<S>)targetBean.getClass();
		return new BeanIntrospector<S>(baseClass,targetBean);
	}

	public static <S> ClassIntrospector<S> create(TypeToken<S> baseType) {
		if(baseType==null) {
			throw new IllegalArgumentException("Object 'baseType' cannot be null");
		}
		@SuppressWarnings("unchecked")
		Class<S> baseClass = (Class<S>)baseType.getRawType();
		return new ClassIntrospector<S>(baseClass);
	}

	public abstract static class TypeToken<T> extends TypeCapture<T> {

		private static final class ClassCollector extends TypeVisitor {
			private final Set<Class<?>> rawTypes;

			private ClassCollector() {
				this.rawTypes = new HashSet<Class<?>>();
			}

			@Override
			void visitTypeVariable(TypeVariable<?> t) {
				visit(t.getBounds());
			}

			@Override
			void visitWildcardType(WildcardType t) {
				visit(t.getUpperBounds());
			}

			@Override
			void visitParameterizedType(ParameterizedType t) {
				rawTypes.add((Class<?>) t.getRawType());
			}

			@Override
			void visitClass(Class<?> t) {
				rawTypes.add(t);
			}

			@Override
			void visitGenericArrayType(GenericArrayType t) {
				rawTypes.add(Types.getArrayClass(getRawType(t.getGenericComponentType())));
			}

			public Set<Class<?>> getRawTypes() {
				return rawTypes;
			}

		}

		private static final String NL=System.getProperty("line.separator");

		private final Type runtimeType;

		/**
		 * Constructs a new type token of {@code T}.
		 *
		 * <p>
		 * Clients create an empty anonymous subclass. Doing so embeds the type
		 * parameter in the anonymous class's type hierarchy so we can
		 * reconstitute it at runtime despite erasure.
		 *
		 * <p>
		 * For example:
		 *
		 * <pre>
		 * {@code TypeToken<List<String>> t = new TypeToken<List<String>>() {};}}
		 * </pre>
		 */
		protected TypeToken() {
			this.runtimeType = capture();
			if(runtimeType instanceof TypeVariable) {
				throw new IllegalStateException(
						String.format(
							"Cannot construct a TypeToken for a type variable."+ NL+
							"You probably meant to call new TypeToken<%s>(getClass()) "+
							"that can resolve the type variable for you."+ NL +
							"If you do need to create a TypeToken of a type variable, " +
							"please use TypeToken.of() instead.",
							runtimeType));
			}
		}

		/**
		 * Returns the raw type of {@code T}. Formally speaking, if {@code T} is
		 * returned by {@link java.lang.reflect.Method#getGenericReturnType},
		 * the raw type is what's returned by
		 * {@link java.lang.reflect.Method#getReturnType} of the same method
		 * object. Specifically:
		 * <ul>
		 * <li>If {@code T} is a {@code Class} itself, {@code T} itself is
		 * returned.
		 * <li>If {@code T} is a {@link ParameterizedType}, the raw type of the
		 * parameterized type is returned.
		 * <li>If {@code T} is a {@link GenericArrayType}, the returned type is
		 * the corresponding array class. For example:
		 * {@code List<Integer>[] => List[]}.
		 * <li>If {@code T} is a type variable or a wildcard type, the raw type
		 * of the first upper bound is returned. For example:
		 * {@code <X extends Foo> => Foo}.
		 * </ul>
		 */
		@SuppressWarnings("unchecked")
		public final Class<? super T> getRawType() {
			// raw type is |T|
			return (Class<? super T>) getRawType(runtimeType);
		}

		private static Class<?> getRawType(Type type) {
			/**
			 * For wildcard or type variable, the first bound determines the
			 * runtime type.
			 */
			return getRawTypes(type).iterator().next();
		}

		private static Set<Class<?>> getRawTypes(Type type) {
			if(type==null) {
				throw new IllegalArgumentException("Object 'type' cannot be null");
			}
			ClassCollector collector = new ClassCollector();
			collector.visit(type);
			return collector.getRawTypes();
		}

	}

	private static final class Types {

		private Types() {
		}

		/**
		 * Returns the {@code Class} object of arrays with {@code componentType}
		 * .
		 */
		static Class<?> getArrayClass(Class<?> componentType) {
			/**
			 * TODO(user): This is not the most efficient way to handle generic
			 * arrays, but is there another way to extract the array class in a
			 * non-hacky way (i.e., using String value class names- "[L...")?
			 */
			return Array.newInstance(componentType, 0).getClass();
		}
	}

	private abstract static class TypeVisitor {

		private final Set<Type> visited = new HashSet<Type>();

		/**
		 * Visits the given types. Null types are ignored. This allows
		 * subclasses to call {@code visit(parameterizedType.getOwnerType())}
		 * safely without having to check nulls.
		 */
		public final void visit(Type... types) {
			for(Type type : types) {
				if (type == null || !visited.add(type)) {
					// No owner type, or type already visited
					continue;
				}
				if(!visitType(type)) {
					/**
					 * When the visitation failed, we don't want to ignore
					 * the second.
					 */
					visited.remove(type);
					throw new AssertionError("Unknown type: " + type);
				}
			}
		}

		protected boolean visitType(Type type) {
			boolean succeeded=true;
			if(type instanceof TypeVariable) {
				visitTypeVariable((TypeVariable<?>) type);
			} else if (type instanceof WildcardType) {
				visitWildcardType((WildcardType) type);
			} else if (type instanceof ParameterizedType) {
				visitParameterizedType((ParameterizedType) type);
			} else if (type instanceof Class) {
				visitClass((Class<?>) type);
			} else if (type instanceof GenericArrayType) {
				visitGenericArrayType((GenericArrayType) type);
			} else {
				succeeded=false;
			}
			return succeeded;
		}

		void visitClass(Class<?> t) {
		}

		void visitGenericArrayType(GenericArrayType t) {
		}

		void visitParameterizedType(ParameterizedType t) {
		}

		void visitTypeVariable(TypeVariable<?> t) {
		}

		void visitWildcardType(WildcardType t) {
		}
	}

	private abstract static class TypeCapture<T> { // NOSONAR
		/** Returns the captured type. */
		final Type capture() {
			Type superclass = getClass().getGenericSuperclass();
			if (!(superclass instanceof ParameterizedType)) {
				throw new IllegalStateException(String.format("%s isn't parameterized", superclass));
			}
			return ((ParameterizedType)superclass).getActualTypeArguments()[0];
		}
	}

}