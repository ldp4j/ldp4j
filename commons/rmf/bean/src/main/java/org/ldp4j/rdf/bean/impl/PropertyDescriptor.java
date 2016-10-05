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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.ldp4j.rdf.bean.PropertyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class PropertyDescriptor {

	private static final Logger LOGGER=LoggerFactory.getLogger(PropertyDescriptor.class);

	private static final class MethodPropertyDescriptor extends PropertyDescriptor {

		private static final class MethodPropertyEditor implements PropertyEditor {

			private final Method getter;
			private final Method setter;

			public MethodPropertyEditor(Method getter, Method setter) {
				this.getter = getter;
				this.setter = setter;
			}

			@Override
			public Object getValue(Object subject) {
				try {
					return getter.invoke(subject);
				} catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				} catch (InvocationTargetException e) {
					throw new IllegalStateException(e);
				}
			}

			@Override
			public void setValue(Object subject, Object value) {
				try {
					setter.invoke(subject,value);
				} catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				} catch (InvocationTargetException e) {
					throw new IllegalStateException(e);
				}
			}

			@Override
			public String toString() {
				return "Methods {getter="+getter+", setter="+setter+"}";
			}

		}

		private final Method getter;
		private Method setter;
		private boolean tried;

		private MethodPropertyDescriptor(Method getter) {
			super(PropertyDescriptorUtils.getName(getter),getter.getReturnType(),getter.getGenericReturnType());
			this.getter=getter;
		}

		@Override
		PropertyEditor getPropertyEditor() {
			return new MethodPropertyEditor(getReadMethod(), getWriteMethod());
		}

		Method getReadMethod() {
			return getter;
		}

		Method getWriteMethod() {
			synchronized(getter) {
				if(!tried) {
					setter=findSetter();
					tried=true;
				}
			}
			return setter;
		}

		private Method findSetter() {
			String setterName = "set"+capitalize(getName());
			try {
				return getter.getDeclaringClass().getDeclaredMethod(setterName, getPropertyType());
			} catch (SecurityException e) {
				throw new IllegalStateException(e);
			} catch (NoSuchMethodException e) {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("No setter for property '"+getName()+"' was found ("+e.getMessage()+")",e);
				}
				return null;
			}
		}

		private String capitalize(String name) {
			if(name==null||name.length()==0) {
				return name;
			}
			if(name.length()>1&&Character.isUpperCase(name.charAt(1))&&Character.isUpperCase(name.charAt(0))) {
				return name;
			}
			char[] chars=name.toCharArray();
			chars[0]=Character.toUpperCase(chars[0]);
			return new String(chars);
		}
	}

	private static final class FieldPropertyDescriptor extends PropertyDescriptor {

		private static final class FieldPropertyEditor implements PropertyEditor {

			private final Field field;

			public FieldPropertyEditor(Field field) {
				this.field = field;
			}

			@Override
			public Object getValue(Object subject) {
				try {
					return field.get(subject);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}

			@Override
			public void setValue(Object subject, Object value) {
				try {
					field.set(subject,value);
				} catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}

			@Override
			public String toString() {
				return "Field {"+field+"}";
			}
		}

		private final Field field;

		private FieldPropertyDescriptor(Field field) {
			super(field.getName(),field.getType(),field.getGenericType());
			this.field = field;
		}

		@Override
		PropertyEditor getPropertyEditor() {
			return new FieldPropertyEditor(field);
		}

	}
	private final String name;
	private final Class<?> propertyType;
	private final Type genericPropertyType;

	PropertyDescriptor(String name, Class<?> propertyType, Type genericPropertyType) {
		this.name=name;
		this.propertyType = propertyType;
		this.genericPropertyType = genericPropertyType;
	}

	String getName() {
		return name;
	}

	Class<?> getPropertyType() {
		return propertyType;
	}

	Type getGenericPropertyType() {
		return genericPropertyType;
	}

	abstract PropertyEditor getPropertyEditor();

	static MethodPropertyDescriptor newDescriptor(Method method) {
		return new MethodPropertyDescriptor(method);
	}

	static FieldPropertyDescriptor newDescriptor(Field field) {
		return new FieldPropertyDescriptor(field);
	}

	static Method getPropertyWriter(PropertyDescriptor descriptor) {
		assert descriptor instanceof MethodPropertyDescriptor;
		return ((MethodPropertyDescriptor)descriptor).getWriteMethod();
	}

}

final class PropertyDescriptorUtils {

	private PropertyDescriptorUtils() {
	}

	static String getName(Method method) {
		String name=method.getName();
		if(name.startsWith("get")) {
			name=decapitalize(name.substring(3));
		} else if(name.startsWith("is")) {
			Class<?> resultType=method.getReturnType();
			if(resultType == Boolean.TYPE) {
				name=decapitalize(name.substring(2));
			}
		}
		return name;
	}

	static String decapitalize(String name) {
		if(name==null||name.length()==0) {
			return name;
		}
		if(name.length()>1&&Character.isUpperCase(name.charAt(1))&&Character.isUpperCase(name.charAt(0))) {
			return name;
		}
		char[] chars=name.toCharArray();
		chars[0]=Character.toLowerCase(chars[0]);
		return new String(chars);
	}

}