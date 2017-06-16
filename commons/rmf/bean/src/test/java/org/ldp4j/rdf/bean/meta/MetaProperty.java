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
package org.ldp4j.rdf.bean.meta;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.ldp4j.rdf.bean.util.TypeUtils;

public abstract class MetaProperty<T extends AnnotatedMember<?>> extends MetaAnnotatedObject<T> {

	public static enum VariableScope {
		TYPE,
		METHOD
		;

		@SuppressWarnings("unchecked")
		static MetaProperty.VariableScope fromType(Type genericType) {
			MetaProperty.VariableScope result=null;
			if(genericType instanceof TypeVariable<?>) {
				TypeVariable<? extends GenericDeclaration> tv=(TypeVariable<? extends GenericDeclaration>)genericType;
				if(tv.getGenericDeclaration() instanceof Type) {
					result=TYPE;
				} else {
					result=METHOD;
				}
			}
			return result;
		}
	}

	private abstract static class AbstractMetaProperty<T extends AnnotatedMember<?>> extends MetaProperty<T> {

		private final String name;
		private final Class<?> type;
		private final Type genericType;
		private final Class<?> rawType;
		private final VariableScope scope;
		private final AnnotatedClass<?> context;

		@SafeVarargs
		private AbstractMetaProperty(AnnotatedClass<?> context, String name, Class<?> type, Type genericType, Class<?> rawType, T... elements) {
			this(context,name,type,genericType,rawType,Arrays.asList(elements));
		}

		private AbstractMetaProperty(AnnotatedClass<?> context, String name, Class<?> type, Type genericType, Class<?> rawType, List<T> elements) {
			super(elements);
			this.context=context;
			this.name = name;
			this.type = type;
			this.genericType = genericType;
			this.rawType = rawType;
			this.scope=VariableScope.fromType(genericType);
		}

		@Override
		public final String getName() {
			return name;
		}

		@Override
		public final VariableScope getTypeVariableScope() {
			return scope;
		}

		@Override
		public final boolean isTypeVariable() {
			return genericType instanceof TypeVariable<?>;
		}

		@Override
		public final Class<?> getRawType() {
			return rawType;
		}

		@Override
		public final Type getGenericType() {
			return genericType;
		}

		@Override
		public final Class<?> getType() {
			return type;
		}

		@Override
		public final AnnotatedClass<?> getContextType() {
			return context;
		}

		@Override
		public void setValue(Object object, Object value) {
			if(!isWritable()) {
				throw new IllegalStateException("Property is not writable");
			}
			if(!getRawType().isInstance(value)) {
				throw new IllegalArgumentException("Value is not of the expected type ("+value.getClass().getCanonicalName()+" is not instance of "+getRawType().getCanonicalName()+")");
			}
			doSetValue(object,value);
		}

		protected abstract void doSetValue(Object object, Object value);

	}

	private static final class FieldMetaProperty extends AbstractMetaProperty<AnnotatedField> {

		private final AnnotatedField field;

		private FieldMetaProperty(AnnotatedClass<?> context, AnnotatedField field) {
			super(context,field.getName(),field.getType(),field.getGenericType(),field.getRawType(),field);
			this.field = field;
		}

		@Override
		public Object getValue(Object object) {
			try {
				return field.getValue(object);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public boolean isWritable() {
			return field.isAccessible();
		}

		@Override
		protected void doSetValue(Object object, Object value) {
			try {
				field.setValue(object,value);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public AnnotatedClass<?> getOwnerType() {
			return field.getDeclaringClass();
		}

		@Override
		public AnnotatedField get() {
			return field;
		}

	}

	private static final class MethodMetaProperty extends AbstractMetaProperty<AnnotatedMethod>  {

		private final List<AnnotatedMethod> methods;
		private final AnnotatedMethod actualGetter;
		private final AnnotatedMethod actualSetter;

		private MethodMetaProperty(AnnotatedClass<?> context, final List<AnnotatedMethod> methods, String name, Class<?> rawType, AnnotatedMethod actualGetter, AnnotatedMethod actualSetter) {
			super(context,name,actualGetter.getReturnType(),actualGetter.getGenericReturnType(),rawType,methods);
			this.methods = methods;
			this.actualGetter = actualGetter;
			this.actualSetter = actualSetter;
		}

		@Override
		public Object getValue(Object object) {
			try {
				return actualGetter.invoke(object);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public boolean isWritable() {
			return actualSetter!=null;
		}

		@Override
		protected void doSetValue(Object object, Object value) {
			try {
				actualSetter.invoke(object, value);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException(e);
			}
		}

		@SuppressWarnings("unused")
		public List<AnnotatedMethod> getMethods() {
			return methods;
		}

		@Override
		public AnnotatedClass<?> getOwnerType() {
			return actualGetter.getDeclaringClass();
		}

		@Override
		public AnnotatedMethod get() {
			return actualGetter;
		}

	}

	private static final class RenamedMetaProperty<T extends AnnotatedMember<?>> extends MetaProperty<T> {
		private final MetaProperty<T> delegate;
		private final String newName;

		private RenamedMetaProperty(MetaProperty<T> delegate, String newName) {
			super(delegate);
			this.delegate = delegate;
			this.newName = newName;
		}

		@Override
		public String getName() {
			return newName;
		}

		@Override
		public VariableScope getTypeVariableScope() {
			return getDelegate().getTypeVariableScope();
		}

		@Override
		public boolean isTypeVariable() {
			return getDelegate().isTypeVariable();
		}

		@Override
		public Class<?> getRawType() {
			return getDelegate().getRawType();
		}

		@Override
		public Type getGenericType() {
			return getDelegate().getGenericType();
		}

		@Override
		public Class<?> getType() {
			return getDelegate().getType();
		}

		@Override
		public AnnotatedClass<?> getContextType() {
			return getDelegate().getContextType();
		}

		@Override
		public AnnotatedClass<?> getOwnerType() {
			return getDelegate().getOwnerType();
		}

		@Override
		public Object getValue(Object object) {
			return getDelegate().getValue(object);
		}

		@Override
		public boolean isWritable() {
			return getDelegate().isWritable();
		}

		@Override
		public void setValue(Object object, Object value) {
			getDelegate().setValue(object, value);
		}

		@Override
		public T get() {
			return delegate.get();
		}

		public MetaProperty<T> getDelegate() {
			return delegate;
		}

	}

	private MetaProperty(List<T> elements) {
		super(elements.get(0),elements);
	}

	private MetaProperty(MetaProperty<T> provider) {
		super(provider);
	}

	public abstract String getName();

	public abstract VariableScope getTypeVariableScope();

	public abstract boolean isTypeVariable();

	public abstract Class<?> getRawType();

	public abstract Type getGenericType();

	public abstract Class<?> getType();

	public abstract AnnotatedClass<?> getContextType();

	public abstract AnnotatedClass<?> getOwnerType();

	public abstract Object getValue(Object object);

	public abstract boolean isWritable();

	public abstract void setValue(Object object, Object value);

	@Override
	public String toString() {
		List<String> segments=new ArrayList<String>();
		segments.add("name="+getName());
		segments.add("type="+getType().getCanonicalName());
		if(isTypeVariable()) {
			TypeVariable<?> typeVariable = (TypeVariable<?>)getGenericType();
			segments.add("genericType="+typeVariable.getName());
			segments.add("variableScope="+getTypeVariableScope()+" {"+TypeUtils.toString(typeVariable)+"}");
		} else {
			segments.add("genericType="+TypeUtils.toString(getGenericType()));
		}
		segments.add("ownerType="+getOwnerType());
		segments.add("contextType="+getContextType());
		segments.add("writable="+isWritable());
		StringBuilder out=new StringBuilder();
		out.append("MetaProperty [");
		for(Iterator<String> it=segments.iterator();it.hasNext();) {
			out.append(it.next());
			if(it.hasNext()) {
				out.append(", ");
			}
		}
		out.append("]");
		return out.toString();
	}

	static MetaProperty<AnnotatedField> forField(AnnotatedClass<?> context, AnnotatedField field) {
		return new FieldMetaProperty(context,field);
	}

	static MetaProperty<AnnotatedMethod> forMethods(AnnotatedClass<?> context, List<AnnotatedMethod> methods) {
		AnnotatedMethod actualGetter=methods.get(0);
		String name=AnnotatedBeanUtils.getPropertyName(actualGetter);
		Class<?> rawType=actualGetter.getRawReturnType();
		AnnotatedMethod actualSetter=AnnotatedBeanUtils.findSetter(context, name, rawType);
		return new MethodMetaProperty(context,methods,name,rawType,actualGetter,actualSetter);
	}

	static <T extends AnnotatedMember<?>> MetaProperty<T> rename(final String newName, final MetaProperty<T> property) {
		return new RenamedMetaProperty<T>(property, newName);
	}

}