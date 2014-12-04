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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ldp4j.application.util.TypeVisitor.TypeFunction;

import static com.google.common.base.Preconditions.*;

/**
 * Utilities for working with {@link Type}.
 *
 * @author Ben Yu
 * @author Miguel Esteban Gutiérrez
 */
public final class Types {

	private static enum GenericArrayTypeCreation {
		JAVA6 {
			@Override
			GenericArrayType newArrayType(Type componentType) {
				return new GenericArrayTypeImpl(componentType);
			}
			@Override
			Type usedInGenericType(Type type) {
				checkArgument(type!=null, "Object 'type' cannot be null");
				Type result=type;
				if (type instanceof Class<?>) {
					Class<?> cls = (Class<?>) type;
					if(cls.isArray()) {
						result=new GenericArrayTypeImpl(cls.getComponentType());
					}
				}
				return result;
			}
		},
		JAVA7 {
			@Override
			Type newArrayType(Type componentType) {
				Type result=null;
				if (componentType instanceof Class<?>) {
					result=getArrayClass((Class<?>)componentType);
				} else {
					result=new GenericArrayTypeImpl(componentType);
				}
				return result;
			}
			@Override
			Type usedInGenericType(Type type) {
				checkArgument(type!=null,"Object 'type' cannot be null");
				return type;
			}
		};

		abstract Type newArrayType(Type componentType);

		abstract Type usedInGenericType(Type type);

		final List<Type> usedInGenericType(Type[] types) {
			List<Type> result = new ArrayList<Type>();
			for (Type type : types) {
				result.add(usedInGenericType(type));
			}
			return Collections.unmodifiableList(result);
		}

		static final GenericArrayTypeCreation JVM_BEHAVIOR=detectJvmBehavior();

		private static GenericArrayTypeCreation detectJvmBehavior() {
			GenericArrayTypeCreation result=JAVA6;
			TypeCapture<int[]> array = new TypeCapture<int[]>(){};
			if(array.capture() instanceof Class<?>) {
				result=JAVA7;
			}
			return result;
		}

	}

	private interface CustomType {

	}

	private static final class GenericArrayTypeImpl implements GenericArrayType, CustomType {

		private final Type componentType;

		GenericArrayTypeImpl(Type componentType) {
			this.componentType = GenericArrayTypeCreation.JVM_BEHAVIOR.usedInGenericType(componentType);
		}

		@Override
		public Type getGenericComponentType() {
			return componentType;
		}

		@Override
		public String toString() {
			return Types.toString(componentType) + "[]";
		}

		@Override
		public int hashCode() {
			return componentType.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			boolean result=false;
			if (obj instanceof GenericArrayType) {
				GenericArrayType that = (GenericArrayType) obj;
				result=equal(getGenericComponentType(), that.getGenericComponentType());
			}
			return result;
		}

	}

	private static final class ParameterizedTypeImpl implements ParameterizedType, CustomType {

		private final Type ownerType;
		private final List<Type> argumentsList;
		private final Class<?> rawType;

		ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type[] typeArguments) {
			checkArgument(
				rawType != null,
				"Object 'rawType' cannot be null");
			checkArgument(
				rawType.getTypeParameters() != null,
				"Class '%s' cannot is not parameterized",Types.toString(rawType));
			checkArgument(
				typeArguments != null,
				"Object 'typeArguments' cannot be null");
			checkArgument(
				typeArguments.length == rawType.getTypeParameters().length,
				"Invalid type arguments provided. Expected %d but got %d",rawType.getTypeParameters(), typeArguments.length);
			disallowPrimitiveType(typeArguments, "type parameter");
			this.ownerType = ownerType;
			this.rawType = rawType;
			this.argumentsList = GenericArrayTypeCreation.JVM_BEHAVIOR.usedInGenericType(typeArguments);
		}

		@Override
		public Type[] getActualTypeArguments() {
			return toArray(argumentsList);
		}

		@Override
		public Type getRawType() {
			return rawType;
		}

		@Override
		public Type getOwnerType() {
			return ownerType;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if(this.ownerType!=null) {
				builder.append(Types.toString(this.ownerType));
				builder.append(".");
				// Find simple name of nested type by removing the
				// shared prefix with owner.
//				if(this.ownerType instanceof ParameterizedType) {
//					ParameterizedType pt = (ParameterizedType)ownerType;
//					Class<?> ptRawType = (Class<?>)pt.getRawType();
//					System.err.println("PT: "+ptRawType.getName()+ " -> "+this.rawType.getName());
//					builder.append(this.rawType.getName().replace(ptRawType.getName()+"$",""));
//				} else {
//					System.err.println("NON_PT: "+ownerType.toString()+ " -> "+this.rawType.getName());
//					builder.append(this.rawType.getName());
//				}
				if(this.ownerType==this.rawType.getDeclaringClass()) {
					builder.append(
						Types.toString(this.rawType).
							replace(Types.toString(this.ownerType)+"$",""));
				} else {
					builder.append(Types.toString(this.rawType));
				}
			} else {
				builder.append(Types.toString(this.rawType));
			}

			if(!this.argumentsList.isEmpty()) {
				builder.append('<');
				for (int i = 0; i < argumentsList.size(); i++) {
					if (i > 0) {
						builder.append(", ");
					}
					builder.append(Types.toString(argumentsList.get(i)));
				}
				builder.append('>');
			}

			return builder.toString();
		}

		@Override
		public int hashCode() {
			return
				(ownerType == null ? 0 : ownerType.hashCode()) ^
				argumentsList.hashCode() ^
				rawType.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			boolean result=false;
			if (other instanceof ParameterizedType) {
				ParameterizedType that=(ParameterizedType)other;
				result=
					equal(getOwnerType(), that.getOwnerType()) &&
					getRawType().equals(that.getRawType()) &&
					Arrays.equals(
						getActualTypeArguments(),
						that.getActualTypeArguments());
			}
			return result;
		}

	}

	private static final class TypeVariableImpl<D extends GenericDeclaration> implements TypeVariable<D>, CustomType {

		private final D genericDeclaration;
		private final String name;
		private final List<Type> bounds;

		TypeVariableImpl(D genericDeclaration, String name, Type[] bounds) {
			checkArgument(genericDeclaration != null, "Object 'genericDeclaration' cannot be null");
			checkArgument(name != null, "Object 'name' cannot be null");
			checkArgument(bounds != null, "Object 'bounds' cannot be null");
			disallowPrimitiveType(bounds, "bound for type variable");
			this.genericDeclaration = genericDeclaration;
			this.name = name;
			this.bounds = Collections.unmodifiableList(Arrays.asList(bounds));
		}

		@Override
		public D getGenericDeclaration() {
			return genericDeclaration;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Type[] getBounds() {
			return toArray(bounds);
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public int hashCode() {
			return genericDeclaration.hashCode() ^ name.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			boolean result = false;
			if(other instanceof TypeVariable<?>) {
				TypeVariable<?> that=(TypeVariable<?>)other;
				result=
					name.equals(that.getName()) &&
					genericDeclaration.equals(that.getGenericDeclaration());
			}
			return result;
		}
	}

	private static final class WildcardTypeImpl implements WildcardType, CustomType {

		private final List<Type> lowerBounds;
		private final List<Type> upperBounds;

		WildcardTypeImpl(Type[] lowerBounds, Type[] upperBounds) {
			checkArgument(lowerBounds!=null,"Object 'lowerBounds' cannot be null");
			checkArgument(upperBounds!=null,"Object 'upperBounds' cannot be null");
			disallowPrimitiveType(lowerBounds,"lower bound for wildcard");
			disallowPrimitiveType(upperBounds,"upper bound for wildcard");
			this.lowerBounds = GenericArrayTypeCreation.JVM_BEHAVIOR.usedInGenericType(lowerBounds);
			this.upperBounds = GenericArrayTypeCreation.JVM_BEHAVIOR.usedInGenericType(upperBounds);
		}

		@Override
		public Type[] getLowerBounds() {
			return toArray(lowerBounds);
		}

		@Override
		public Type[] getUpperBounds() {
			return toArray(upperBounds);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder("?");
			for(Type lowerBound:lowerBounds) {
				builder.append(" super ").append(Types.toString(lowerBound));
			}
			for (Type upperBound:filterUpperBounds(upperBounds)) {
				builder.append(" extends ").append(Types.toString(upperBound));
			}
			return builder.toString();
		}

		@Override
		public int hashCode() {
			return lowerBounds.hashCode() ^ upperBounds.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			boolean result = false;
			if(other instanceof WildcardType) {
				WildcardType that = (WildcardType) other;
				result=
					lowerBounds.equals(Arrays.asList(that.getLowerBounds())) &&
					upperBounds.equals(Arrays.asList(that.getUpperBounds()));
			}
			return result;
		}

	}

	private static final class TypeCloner extends TypeFunction<Type> {
		@Override
		protected <S, E extends Exception> Type visitClass(Class<S> t,E exception) throws E {
			return t;
		}

		@Override
		protected <E extends Exception> Type visitGenericArrayType(GenericArrayType t, E exception) throws E {
			return newArrayClassOrGenericArrayType(t.getGenericComponentType());
		}

		@Override
		protected <E extends Exception> Type visitParameterizedType(ParameterizedType t, E exception) throws E {
			if(t.getOwnerType()!=null) {
				return newParameterizedTypeWithOwner(t.getOwnerType(),(Class<?>)t.getRawType(), t.getActualTypeArguments());
			} else {
				return newParameterizedType((Class<?>)t.getRawType(), t.getActualTypeArguments());
			}
		}

		@Override
		protected <D extends GenericDeclaration, E extends Exception> Type visitTypeVariable(TypeVariable<D> t, E exception) throws E {
			return newTypeVariable(t.getGenericDeclaration(), t.getName(), t.getBounds());
		}

		@Override
		protected <E extends Exception> Type visitWildcardType(WildcardType t, E exception) throws E {
			return newWildcard(t.getLowerBounds(),t.getUpperBounds());
		}
	}

	private static final Type[] DEFAULT_BOUNDS = new Type[] { Object.class };

	private Types() {}

	private static Type clone(Type type) {
		if(type instanceof CustomType) {
			return type;
		}
		return new TypeCloner().apply(type);
	}

	private static boolean equal(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}

	/**
	 * Returns {@code ? extends X} if any of {@code bounds} is a subtype of
	 * {@code X[]}; or null otherwise.
	 */
	private static Type subtypeOfComponentType(Type[] bounds) {
		for (Type bound : bounds) {
			Type componentType = getComponentType(bound);
			if (componentType != null) {
				// Only the first bound can be a class or array.
				// Bounds after the first can only be interfaces.
				if(componentType instanceof Class<?>) {
					Class<?> componentClass = (Class<?>) componentType;
					if (componentClass.isPrimitive()) {
						return componentClass;
					}
				}
				return newSubtypeOfWildcard(componentType);
			}
		}
		return null;
	}

	private static Type[] toArray(Collection<Type> types) {
		return types.toArray(new Type[types.size()]);
	}

	private static Iterable<Type> filterUpperBounds(Iterable<Type> bounds) {
		List<Type> result=new ArrayList<Type>();
		for(Type bound:bounds) {
			if(!bound.equals(Object.class)) {
				result.add(bound);
			}
		}
		return result;
	}

	private static void disallowPrimitiveType(Type[] types, String usedAs) {
		for(Type type:types) {
			if(type instanceof Class<?>) {
				Class<?> cls=(Class<?>)type;
				checkArgument(!cls.isPrimitive(),"Primitive type '%s' used as %s", cls, usedAs);
			}
		}
	}

	/** Returns the array type of {@code componentType}. */
	static Type newArrayType(Type componentType) {
		if (componentType instanceof WildcardType) {
			WildcardType wildcard = (WildcardType) componentType;

			Type[] lowerBounds = wildcard.getLowerBounds();
			checkArgument(lowerBounds==null || lowerBounds.length <= 1,"Wildcard cannot have more than one lower bound.");
			if(lowerBounds!=null && lowerBounds.length == 1) {
				return newSupertypeOfWildcard(newArrayType(lowerBounds[0]));
			}

			Type[] upperBounds = wildcard.getUpperBounds();
			checkArgument(upperBounds!=null && upperBounds.length == 1, "Wildcard should have only one upper bound.");
			return newSubtypeOfWildcard(newArrayType(upperBounds[0]));
		}
		return GenericArrayTypeCreation.JVM_BEHAVIOR.newArrayType(componentType);
	}

	/**
	 * Creates an array class if {@code componentType} is a class, or else, a
	 * {@link GenericArrayType}. This is what Java7 does for generic array type
	 * parameters.
	 */
	static Type newArrayClassOrGenericArrayType(Type componentType) {
		return GenericArrayTypeCreation.JAVA7.newArrayType(componentType);
	}

	/**
	 * Returns a type where {@code rawType} is parameterized by
	 * {@code arguments}.
	 */
	static ParameterizedType newParameterizedType(
			Class<?> rawType,
			Type... arguments) {
		checkArgument(rawType != null, "Object 'rawType' cannot be null");
		checkArgument(arguments!= null, "Object 'arguments' cannot be null");
		return new ParameterizedTypeImpl(rawType.getDeclaringClass(), rawType, arguments);
	}

	/**
	 * Returns a type where {@code rawType} is parameterized by
	 * {@code arguments} and is owned by {@code ownerType}.
	 */
	static ParameterizedType newParameterizedTypeWithOwner(
			Type ownerType,
			Class<?> rawType,
			Type... arguments) {
		if(ownerType==null) {
			return newParameterizedType(rawType, arguments);
		}
		checkArgument(rawType!= null, "Object 'rawType' cannot be null");
		checkArgument(arguments!= null, "Object 'arguments' cannot be null");
		checkArgument(rawType.getEnclosingClass()!= null,"Owner type for unenclosed %s", rawType);
		return new ParameterizedTypeImpl(ownerType, rawType, arguments);
	}

	/**
	 * Returns a new {@link TypeVariable} that belongs to {@code declaration}
	 * with {@code name} and {@code bounds}.
	 */
	static <D extends GenericDeclaration> TypeVariable<D> newTypeVariable(
			D declaration,
			String name,
			Type... bounds) {
		checkArgument(declaration!=null, "Object 'declaration' cannot be null");
		checkArgument(name!=null && !name.isEmpty(), "Object 'name' cannot be null or empty");
		Type[] actualBounds = bounds;
		if(actualBounds==null || actualBounds.length==0) {
			actualBounds=DEFAULT_BOUNDS;
		}
		return new TypeVariableImpl<D>(declaration, name, actualBounds);
	}

	/** Returns a new {@link WildcardType} with {@code lowerBounds} and {@code upperBounds}. */
	static WildcardType newWildcard(Type[] lowerBounds, Type[] upperBounds) {
		return new WildcardTypeImpl(lowerBounds,upperBounds);
	}

	/** Returns a new {@link WildcardType} with {@code upperBound}. */
	static WildcardType newSubtypeOfWildcard(Type upperBound) {
		return newWildcard(new Type[0], new Type[] { upperBound });
	}

	/** Returns a new {@link WildcardType} with {@code lowerBound}. */
	static WildcardType newSupertypeOfWildcard(Type lowerBound) {
		return newWildcard(new Type[] { lowerBound }, DEFAULT_BOUNDS);
	}

	/**
	 * Returns human readable string representation of {@code type}.
	 * <ul>
	 * <li>For array type {@code Foo[]}, {@code "com.mypackage.Foo[]"} is
	 * returned.
	 * <li>For any class, {@code theClass.getName()} is returned.
	 * <li>For all other types, {@code type.toString()} is returned.
	 * </ul>
	 */
	public static String toString(Type type) {
		checkArgument(type!=null,"Object 'type' cannot be null");
		String result=null;
		if(type instanceof Class<?>) {
			Class<?> clazz=(Class<?>)type;
			result=clazz.getName();
			if(clazz.isArray()) {
				result=result.substring(2,result.length()-1)+"[]";
			}
		} else {
			result=clone(type).toString();
		}
		return result;
	}

	static Type getComponentType(Type type) {
		checkArgument(type!=null,"Object 'type' cannot be null");
		TypeFunction<Type> function=new TypeFunction<Type>() {
			@Override
			protected <S, E extends Exception> Type visitClass(Class<S> t, E exception) throws E {
				return t.getComponentType();
			}
			@Override
			protected <E extends Exception> Type visitGenericArrayType(GenericArrayType t, E exception) throws E {
				return t.getGenericComponentType();
			}
			@Override
			protected <E extends Exception> Type visitParameterizedType(ParameterizedType t, E exception) throws E {
				return null;
			}
			@Override
			protected <D extends GenericDeclaration, E extends Exception> Type visitTypeVariable(TypeVariable<D> t, E exception) throws E {
				return subtypeOfComponentType(t.getBounds());
			}
			@Override
			protected <E extends Exception> Type visitWildcardType(WildcardType t,E exception) throws E {
				return subtypeOfComponentType(t.getUpperBounds());
			}
		};
		return function.apply(type);
	}

	/** Returns the {@code Class} object of arrays with {@code componentType}. */
	static Class<?> getArrayClass(Class<?> componentType) {
		// TODO(user): This is not the most efficient way to handle generic
		// arrays, but is there another way to extract the array class in a
		// non-hacky way (i.e. using String value class names- "[L...")?
		return Array.newInstance(componentType, 0).getClass();
	}
}