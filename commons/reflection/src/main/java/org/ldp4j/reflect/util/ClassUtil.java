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
package org.ldp4j.reflect.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.ldp4j.reflect.Types;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public final class ClassUtil {

	public static class TypeDeclaration<T> {

		private final Class<T> rawType;
		private final Type type;

		private TypeDeclaration(Class<T> rawType, Type type) {
			this.rawType = rawType;
			this.type = type;
		}

		public Class<T> getRawType() {
			return rawType;
		}

		public Type getType() {
			return type;
		}

		public TypeDeclaration<?> getSuperclass() {
			Class<?> superClass = this.rawType.getSuperclass();
			if(superClass==null) {
				return null;
			}
			return TypeDeclaration.create(superClass, this.rawType.getGenericSuperclass());
		}

		public List<TypeDeclaration<?>> getInterfaces() {
			Builder<TypeDeclaration<?>> builder = ImmutableList.<TypeDeclaration<?>>builder();
			Class<?>[] is = this.rawType.getInterfaces();
			Type[] gis = this.rawType.getGenericInterfaces();
			for(int i=0;i<is.length;i++) {
				builder.add(TypeDeclaration.create(is[i],gis[i]));
			}
			return builder.build();
		}

		private static <T> TypeDeclaration<T> create(Class<T> rawType, Type type) {
			return new TypeDeclaration<T>(rawType,type);
		}

		public static <T> TypeDeclaration<T> create(Class<T> clazz) {
			return create(clazz,clazz);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.rawType,this.type);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj==null) {
				return false;
			} else if(obj==this) {
				return true;
			} else if(obj instanceof ClassUtil.TypeDeclaration<?>) {
				ClassUtil.TypeDeclaration<?> that=(ClassUtil.TypeDeclaration<?>)obj;
				return
					Objects.equal(this.rawType,that.rawType) &&
					Objects.equal(this.type,that.type);
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return
				Objects.
					toStringHelper(getClass()).
						add("rawType",Types.toString(this.rawType)).
						add("type",Types.toString(this.type)).
						toString();
		}

	}

	private static class ClassHierarchyIterator implements Iterator<TypeDeclaration<?>> {

		private final Queue<TypeDeclaration<?>> pendingClasses;
		private final Queue<TypeDeclaration<?>> pendingInterfaces;
		private final Set<TypeDeclaration<?>> visitedClasses;
		private TypeDeclaration<?> next;

		private ClassHierarchyIterator(Class<?> target) {
			this.visitedClasses = Sets.newIdentityHashSet();
			this.pendingClasses = new LinkedList<TypeDeclaration<?>>();
			this.pendingInterfaces = new LinkedList<TypeDeclaration<?>>();
			this.pendingClasses.add(TypeDeclaration.create(target));
		}

		@Override
		public boolean hasNext() {
			while(!(this.pendingClasses.isEmpty() && this.pendingInterfaces.isEmpty())) {
				this.next=pendingClasses.poll();
				if(this.next==null) {
					this.next=this.pendingInterfaces.poll();
				}
				if(!this.visitedClasses.contains(this.next)) {
					this.visitedClasses.add(this.next);
					TypeDeclaration<?> superClass=this.next.getSuperclass();
					if(superClass!=null) {
						this.pendingClasses.offer(superClass);
					}
					this.pendingInterfaces.addAll(this.next.getInterfaces());
					return true;
				}
			}
			return false;
		}

		@Override
		public TypeDeclaration<?> next() {
			checkState(next!=null,"No more class declarations available");
			return this.next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove class declarations");
		}

	}


	private interface Signature {

		List<TypeVariable<Method>> typeParameters();

		Class<?> returnType();

		Type genericReturnType();

		List<Class<?>> formalParameterTypes();

		List<Type> genericFormalParameterTypes();

	}


	private enum ReturnType {
		VOID,
		PRIMITIVE,
		REFERENCE;

		public static ReturnType fromMethod(Method method) {
			Class<?> returnType = method.getReturnType();
			if(returnType==void.class) {
				return VOID;
			} if(returnType.isPrimitive()) {
				return PRIMITIVE;
			} else {
				return REFERENCE;
			}
		}
	}


	public static class MethodUtil {

		private MethodUtil() {
		}

		@SuppressWarnings("unused")
		private static class ErasedMethodSignature implements Signature {

			private Method m;

			private ErasedMethodSignature(Method m) {
				this.m = m;
			}

			@Override
			public Class<?> returnType() {
				return this.m.getReturnType();
			}

			@Override
			public Type genericReturnType() {
				return this.m.getReturnType();
			}

			@Override
			public List<Class<?>> formalParameterTypes() {
				return Lists.newArrayList(this.m.getParameterTypes());
			}

			@Override
			public List<Type> genericFormalParameterTypes() {
				return Lists.<Type>newArrayList(this.m.getParameterTypes());
			}

			@Override
			public List<TypeVariable<Method>> typeParameters() {
				return Lists.newArrayList();
			}

		}

		@SuppressWarnings("unused")
		private static class MethodSignature implements Signature {

			private Method m;

			private MethodSignature(Method m) {
				this.m = m;
			}

			@Override
			public Class<?> returnType() {
				return this.m.getReturnType();
			}

			@Override
			public Type genericReturnType() {
				return this.m.getGenericReturnType();
			}

			@Override
			public List<Class<?>> formalParameterTypes() {
				return Lists.newArrayList(this.m.getParameterTypes());
			}

			@Override
			public List<Type> genericFormalParameterTypes() {
				return Lists.newArrayList(this.m.getGenericParameterTypes());
			}

			@Override
			public List<TypeVariable<Method>> typeParameters() {
				return Lists.newArrayList(this.m.getTypeParameters());
			}

		}

		public static boolean overrides(Method m1, final Method m2) {
			checkNotNull(m1);
			checkNotNull(m2);
			checkArgument(MethodUtil.isInstanceMethod(m2));
			Class<?> A = m2.getDeclaringClass();
			Class<?> C = m1.getDeclaringClass();

			// An instance method m1, declared in class C, overrides another
			// instance method m2, declared in a class A if and only if all of
			// the following are true:
			// C is a subclass of A
			if(!A.isAssignableFrom(C)) {
				System.err.println("Not assignables");
				return false;
			}
			// The signature of m1 is a subsignature of the signature of m2
			if(!MethodUtil.isSubsignature(m1,m2)) {
				System.err.println("Not subsignature");
				return false;
			}

			// If a method declaration m1 with return type R1 overrides (or
			// hides) the declaration of another method m2 with return type R2,
			// then m1 must be return-type-substitutable for m2
			if(!MethodUtil.isReturnTypeSubstitutable(m1,m2)) {
				System.err.println("Not return-type-substitutable");
				return false;
			}

			// The access modifier of an overriding (or hiding) method must
			// provide at least as much access as the overridden (or hidden)
			// method as follows:
			if(MethodUtil.isPublic(m2) && !MethodUtil.isPublic(m1)) {
				System.err.println("Restricts access");
				return false;
			}
			if(MethodUtil.isProtected(m2) && (MethodUtil.isPublic(m1) || MethodUtil.isProtected(m1))) {
				System.err.println("Restricts access");
				return false;
			}
			if(MethodUtil.isPackagePrivate(m2) && MethodUtil.isPrivate(m1)) {
				System.err.println("Restricts access");
				return false;
			}
			return true;
		}

		public static boolean isSubsignature(Method m1, Method m2) {
			checkNotNull(m1);
			checkNotNull(m2);
			if(hasSameSignature(m2, m1)) {
				return true;
			}
			return hasSameErasedSignature(m2, m1);
		}

		public static boolean hasSameSignature(Method m, Method n) {
			checkNotNull(m);
			checkNotNull(n);
			// Must have the same name
			if(m.getName()!=n.getName()) {
				System.err.println("Different names");
				return false;
			}
			// Must have the same number of formal parameters
			if(m.getParameterTypes().length!=n.getParameterTypes().length) {
				System.err.println("Different formal parameter number");
				return false;
			}
			TypeVariable<?>[] a=m.getTypeParameters();
			TypeVariable<?>[] b=n.getTypeParameters();
			// Must have the same number of type parameters
			if(a.length!=b.length) {
				System.err.println("Different type parameter number");
				return false;
			}
			// After renaming each occurrence of Bi in N's type to Ai...
			// ... the bounds of corresponding type variables are the same...
			for(int i=0;i<a.length;i++) {
				Type[] aBounds = a[i].getBounds();
				Type[] bBounds = renameAll(b[i].getBounds(),b,a);
				for(int j=0;j<aBounds.length;j++) {
					if(!aBounds[i].equals(bBounds[i])) {
						System.err.println("Different renamed type parameters ("+Types.toString(aBounds[i])+"!="+Types.toString(bBounds[i])+")");
						return false;
					}
				}
			}
			// ... and the formal parameter types of M and N are the same.
			Type[] nTypes = n.getParameterTypes();
			Type[] mTypes = m.getParameterTypes();
			for(int i=0;i<mTypes.length;i++) {
				if(!mTypes[i].equals(nTypes[i])) {
					System.err.println("Different formal parameters ("+Types.toString(mTypes[i])+"!="+Types.toString(nTypes[i])+")");
					return false;
				}
			}
			return true;
		}

		public static boolean hasSameErasedSignature(Method m, Method n) {
			checkNotNull(m);
			checkNotNull(n);
			// Must have the same name
			if(m.getName()!=n.getName()) {
				return false;
			}
			Class<?>[] a=m.getParameterTypes();
			Type[] ga=m.getGenericParameterTypes();
			Class<?>[] b=n.getParameterTypes();
			Type[] gb=n.getGenericParameterTypes();
			// Must have the same number of formal parameters
			if(a.length!=b.length) {
				return false;
			}
			// Must have the same formal parameters
			for(int i=0;i<a.length;i++) {
				if(erasure(ga[i])!=erasure(gb[i])) {
					return false;
				}
			}
			return true;
		}

		public static boolean isReturnTypeSubstitutable(Method d1, Method d2) {
			checkNotNull(d1);
			checkNotNull(d2);
			// A method declaration d1 with return type R1 is
			// return-type-substitutable for another method d2 with return type
			// R2, if and only if the following conditions hold:
			Class<?> r1 = d1.getReturnType();
			Class<?> r2 = d2.getReturnType();
			// If R1 is void then R2 is void
			if(r1==void.class && r2==void.class) {
				return true;
			}
			// If R1 is a primitive type then R2 is identical to R1
			if(r1.isPrimitive() && r2==r1) {
				return true;
			}
			// If R1 is a reference type then R1 is either a subtype of R2 or R1
			// can be converted to a subtype of R2 by unchecked conversion
			if(r2.isAssignableFrom(r1)) {
				return true;
			}
			// If R1 is equal to the erasure of R2
			if(r1==erasure(d2.getGenericReturnType())) {
				return true;
			}
			return false;
		}

		private static Type rename(Type type, TypeVariable<?>[] from, TypeVariable<?>[] to) {
			Type result=null;
			if(type instanceof Class<?>) {
				result=type;
			} else if(type instanceof ParameterizedType) {
				ParameterizedType pt=(ParameterizedType)type;
				result=Types.newParameterizedType(pt.getOwnerType(), (Class<?>)pt.getRawType(), renameAll(pt.getActualTypeArguments(),from,to));
			} else if(type instanceof GenericArrayType) {
				GenericArrayType pt=(GenericArrayType)type;
				result=Types.newArrayType(rename(pt.getGenericComponentType(),from,to));
			} else if(type instanceof WildcardType) {
				WildcardType pt=(WildcardType)type;
				result=Types.newWildcard(renameAll(pt.getLowerBounds(), from, to), renameAll(pt.getUpperBounds(),from,to));
			} else if(type instanceof TypeVariable<?>) {
				for(int i=0;i<from.length;i++) {
					if(from[i]==type) {
						result=to[i];
						break;
					}
				}
			} else {
				throw new IllegalArgumentException("Unknown type '"+type.getClass().getCanonicalName()+"'");
			}
			System.err.printf("rename(type: %s, from: %s,to: %s)=%s%n",toString(type),toString(from),toString(to),toString(result));
			return result;
		}

		private static String toString(Type[] from) {
			StringBuilder builder=new StringBuilder();
			for(int i=0;i<from.length;i++) {
				if(i<from.length-1) {
					builder.append(", ");
				}
				builder.append(toString(from[i]));
			}
			return builder.toString();
		}

		private static String toString(Type type) {
			if(type instanceof TypeVariable<?>) {
				TypeVariable<?> tv=(TypeVariable<?>)type;
				return String.format("{%s} %s (%s)",tv.getGenericDeclaration(),tv.getName(),toString(tv.getBounds()));
			}
			return Types.toString(type);
		}

		private static Type[] renameAll(Type[] types, TypeVariable<?>[] from, TypeVariable<?>[] to) {
			Type[] result=new Type[types.length];
			for(int i=0;i<types.length;i++) {
				result[i]=rename(types[i],from,to);
			}
			return result;
		}

		public static Class<?> erasure(Type type) {
			if(type instanceof Class<?>) {
				return (Class<?>)type;
			} else if(type instanceof ParameterizedType) {
				ParameterizedType cType=(ParameterizedType)type;
				return erasure(cType.getRawType());
			} else if(type instanceof GenericArrayType) {
				GenericArrayType cType=(GenericArrayType)type;
				return erasure(cType.getGenericComponentType());
			} else if(type instanceof WildcardType) {
				WildcardType cType=(WildcardType)type;
				return erasure(cType.getLowerBounds()[0]);
			} else if(type instanceof TypeVariable<?>) {
				TypeVariable<?> cType=(TypeVariable<?>)type;
				return erasure(cType.getBounds()[0]);
			} else {
				throw new IllegalArgumentException("Unknown type '"+type.getClass().getCanonicalName()+"'");
			}
		}

		public static boolean isInstanceMethod(Method method) {
			return !Modifier.isStatic(method.getModifiers());
		}

		public static boolean isPublic(Method method) {
			return Modifier.isPublic(method.getModifiers());
		}

		public static boolean isProtected(Method method) {
			return Modifier.isProtected(method.getModifiers());
		}

		public static boolean isPrivate(Method method) {
			return Modifier.isPrivate(method.getModifiers());
		}

		public static boolean isPackagePrivate(Method method) {
			return !isPublic(method) && !isProtected(method) && !isPrivate(method);
		}

		public static boolean isSourceMethod(Method method) {
			return !method.isSynthetic() && !method.isBridge();
		}

	}

	public static class MethodDefinition {

		private final List<Method> stack;
		private final Method method;

		private MethodDefinition(Method method) {
			this.method=method;
			this.stack=Lists.newArrayList();
			this.stack.add(method);
		}

		public boolean addMethod(Method m2) {
			if(MethodUtil.overrides(this.method, m2)) {
				this.stack.add(m2);
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			StringBuilder builder=new StringBuilder();
			for(Method m:stack) {
				builder.append("- ").append(m.toGenericString()).append(String.format("%n"));
			}

			return builder.toString();
		}

		public static MethodDefinition create(Method method) {
			checkArgument(MethodUtil.isInstanceMethod(method));
			return new MethodDefinition(method);
		}


	}

	private static class MethodHash {

		private final String name;
		private final int parameters;
		private final ReturnType type;

		private MethodHash(String name, int parameters, ReturnType type) {
			this.name = name;
			this.parameters = parameters;
			this.type = type;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.name,this.parameters,this.type);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj==null) {
				return false;
			} else if(this==obj) {
				return true;
			} else if(obj instanceof MethodHash) {
				MethodHash that = (MethodHash)obj;
				return
					Objects.equal(this.name, that.name) &&
					Objects.equal(this.parameters, that.parameters) &&
					Objects.equal(this.type, that.type);
			} else {
				return false;
			}
 		}

		@Override
		public String toString() {
			return
				Objects.
					toStringHelper(getClass()).
						add("name", this.name).
						add("parameters", this.parameters).
						add("type", this.type).
						toString();
		}

		public static MethodHash create(Method method) {
			return new MethodHash(method.getName(),method.getParameterTypes().length, ReturnType.fromMethod(method));
		}

	}

	private ClassUtil() {
		// Prevent instantiation
	}

	public static Iterator<TypeDeclaration<?>> newClassHierarchyIterator(Class<?> target) {
		return new ClassHierarchyIterator(target);
	}

	public static List<MethodDefinition> getMethodDefinitions(Class<?> target) {
		Multimap<MethodHash,MethodDefinition> methods=LinkedHashMultimap.create();
		Iterator<TypeDeclaration<?>> it=ClassUtil.newClassHierarchyIterator(target);
		while(it.hasNext()) {
			Class<?> next = it.next().getRawType();
			for(Method method:next.getDeclaredMethods()) {
				if(!MethodUtil.isInstanceMethod(method) || !MethodUtil.isSourceMethod(method)) {
					System.out.println(" - Discard");
					continue;
				}
				System.out.println(method.toGenericString());
				System.out.println(method.toString());
				System.out.printf(" <- %s (%s)%n",Types.toString(method.getReturnType()),Types.toString(method.getGenericReturnType()));
				for(int i=0;i<method.getParameterTypes().length;i++) {
					System.out.printf(" -> %s (%s)%n",Types.toString(method.getParameterTypes()[i]),Types.toString(method.getGenericParameterTypes()[i]));
				}
				MethodHash hash=MethodHash.create(method);
				System.out.println(" - Hash: "+hash);
				if(!methods.containsKey(hash)) {
					MethodDefinition definition=MethodDefinition.create(method);
					methods.put(hash, definition);
					System.out.println(" - Create hash block");
				} else {
					System.out.println(" - Update hash block");
					boolean found=false;
					for(MethodDefinition definition:methods.get(hash)) {
						found=definition.addMethod(method);
						if(found) {
							System.out.println(" - Update definition");
							break;
						}
					}
					if(!found) {
						MethodDefinition definition=MethodDefinition.create(method);
						methods.put(hash, definition);
						System.out.println(" - Create definition");
					}
				}
			}
		}
		return Lists.newArrayList(methods.values());
	}

}