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
package org.ldp4j.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public abstract class TypeVisitor<T> {

	public static abstract class TypeProcessor extends TypeVisitor<Void> {

		public TypeProcessor() {
			this(true);
		}

		public TypeProcessor(boolean failByDefault) {
			super(failByDefault);
		}

		public final void process(Type type) {
			process(type, new IllegalStateException("Unexpected exception"));
		}

		public final <E extends Exception> void process(Type type, E exception) throws E {
			visit(type,exception);
		}

		@Override
		protected final <T, E extends Exception> Void visitClass(Class<T> t, E exception) throws E {
			doVisitClass(t, exception);
			return null;
		}

		@Override
		protected final <E extends Exception> Void visitGenericArrayType(GenericArrayType t, E exception) throws E {
			doVisitGenericArrayType(t, exception);
			return null;
		}

		@Override
		protected final <E extends Exception> Void visitParameterizedType(ParameterizedType t, E exception) throws E {
			doVisitParameterizedType(t, exception);
			return null;
		}

		@Override
		protected final <D extends GenericDeclaration, E extends Exception> Void visitTypeVariable(TypeVariable<D> t, E exception) throws E {
			doVisitTypeVariable(t, exception);
			return null;
		}

		@Override
		protected final <E extends Exception> Void visitWildcardType(WildcardType t, E exception) throws E {
			doVisitWildcardType(t, exception);
			return null;
		}
		protected <E extends Exception> void doVisitClass(Class<?> t, E exception) throws E {
			if(failByDefault()) {
				throw exception;
			}
		}

		protected <E extends Exception> void doVisitGenericArrayType(GenericArrayType t, E exception) throws E {
			if(failByDefault()) {
				throw exception;
			}
		}

		protected <E extends Exception> void doVisitParameterizedType(ParameterizedType t, E exception) throws E {
			if(failByDefault()) {
				throw exception;
			}
		}

		protected <E extends Exception> void doVisitTypeVariable(TypeVariable<?> t, E exception) throws E {
			if(failByDefault()) {
				throw exception;
			}
		}

		protected <E extends Exception> void doVisitWildcardType(WildcardType t, E exception) throws E {
			if(failByDefault()) {
				throw exception;
			}
		}

	}

	public static abstract class TypeFunction<T> extends TypeVisitor<T> {

		private final T defaultResult;

		public TypeFunction() {
			this(null);
		}

		public TypeFunction(T defaultResult) {
			this(defaultResult,defaultResult==null);
		}

		public TypeFunction(T defaultResult, boolean failFirst) {
			super(!failFirst);
			this.defaultResult = defaultResult;
		}

		public final T apply(Type type) {
			return apply(type, new IllegalStateException("Unexpected exception"));
		}

		public final <E extends Exception> T apply(Type type, E exception) throws E {
			return visit(type,exception);
		}

		public final T getDefaultResult() {
			return defaultResult;
		}

		@Override
		protected <S, E extends Exception> T visitClass(Class<S> t, E exception) throws E {
			T result=null;
			if(!failByDefault()) {
				result = getDefaultResult();
			} else {
				result=super.visitClass(t, exception);
			}
			return result;
		}

		@Override
		protected <E extends Exception> T visitGenericArrayType(GenericArrayType t, E exception) throws E {
			T result=null;
			if(!failByDefault()) {
				result = getDefaultResult();
			} else {
				result=super.visitGenericArrayType(t, exception);
			}
			return result;
		}

		@Override
		protected <E extends Exception> T visitParameterizedType(ParameterizedType t, E exception) throws E {
			T result=null;
			if(!failByDefault()) {
				result = getDefaultResult();
			} else {
				result=super.visitParameterizedType(t, exception);
			}
			return result;
		}

		@Override
		protected <D extends GenericDeclaration, E extends Exception> T visitTypeVariable(TypeVariable<D> t, E exception) throws E {
			T result=null;
			if(!failByDefault()) {
				result = getDefaultResult();
			} else {
				result=super.visitTypeVariable(t, exception);
			}
			return result;
		}

		@Override
		protected <E extends Exception> T visitWildcardType(WildcardType t, E exception) throws E {
			T result=null;
			if(!failByDefault()) {
				result = getDefaultResult();
			} else {
				result=super.visitWildcardType(t, exception);
			}
			return result;
		}

	}

	private final boolean failByDefault;

	protected TypeVisitor(boolean failByDefault) {
		this.failByDefault = failByDefault;
	}

	protected boolean failByDefault() {
		return failByDefault;
	}

	protected final <E extends Exception> T visit(Type type, E exception) throws E {
		if(type==null) {
			return null;
		}

		T result=null;
		if (type instanceof TypeVariable) {
			result=visitTypeVariable((TypeVariable<?>) type,exception);
		} else if (type instanceof WildcardType) {
			result=visitWildcardType((WildcardType) type,exception);
		} else if (type instanceof ParameterizedType) {
			result=visitParameterizedType((ParameterizedType) type,exception);
		} else if (type instanceof Class) {
			result=visitClass((Class<?>) type,exception);
		} else if (type instanceof GenericArrayType) {
			result=visitGenericArrayType((GenericArrayType) type,exception);
		} else {
			throw exception;
		}

		return result;
	}

	protected <S,E extends Exception> T visitClass(Class<S> t, E exception) throws E {
		if(failByDefault()) {
			throw exception;
		}
		return null;
	}

	protected <E extends Exception> T visitGenericArrayType(GenericArrayType t, E exception) throws E {
		if(failByDefault()) {
			throw exception;
		}
		return null;
	}

	protected <E extends Exception> T visitParameterizedType(ParameterizedType t, E exception) throws E {
		if(failByDefault()) {
			throw exception;
		}
		return null;
	}

	protected <D extends GenericDeclaration, E extends Exception> T visitTypeVariable(TypeVariable<D> t, E exception) throws E {
		if(failByDefault()) {
			throw exception;
		}
		return null;
	}

	protected <E extends Exception> T visitWildcardType(WildcardType t, E exception) throws E {
		if(failByDefault()) {
			throw exception;
		}
		return null;
	}
}