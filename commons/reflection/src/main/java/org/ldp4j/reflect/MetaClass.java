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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.reflect.TypeVisitor.TypeFunction;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class MetaClass {

	public static class MetaClassVisitor {

		public boolean visit(MetaClass metaType) {
			return false;
		}

	}

	private final Class<?> rawType;
	private final Type type;
	private final MetaClass context;

	private MetaClass(Class<?> rawType, Type type, MetaClass context) {
		this.rawType = rawType;
		this.type = type!=null?type:rawType;
		this.context = context;
	}

	private Type resolve(Type type) {
		final Map<TypeVariable<?>, Type> resolutions = parameterArguments();
		TypeFunction<Type> resolver = new TypeFunction<Type>() {
			private Type[] resolveAll(Type[] types) {
				Type[] resolved=new Type[types.length];
				for(int i=0;i<resolved.length;i++) {
					resolved[i]=apply(types[i]);
				}
				return resolved;
			}
			@Override
			protected <S, E extends Exception> Type visitClass(Class<S> t, E exception) throws E {
				return t;
			}
			@Override
			protected <E extends Exception> Type visitGenericArrayType(GenericArrayType t, E exception) throws E {
				return Types.newArrayType(apply(t.getGenericComponentType()));
			}
			@Override
			protected <E extends Exception> Type visitParameterizedType(ParameterizedType t, E exception) throws E {
				Type[] resolved = resolveAll(t.getActualTypeArguments());
				if(t.getOwnerType()!=null) {
					return Types.newParameterizedType(t.getOwnerType(),(Class<?>)t.getRawType(),resolved);
				} else {
					return Types.newParameterizedType((Class<?>)t.getRawType(),resolved);
				}
			}
			@Override
			protected <D extends GenericDeclaration, E extends Exception> Type visitTypeVariable(TypeVariable<D> t, E exception) throws E {
				Type resolved = resolutions.get(t);
				if(resolved==null) {
					return t;
				}
				return resolved;
			}
			@Override
			protected <E extends Exception> Type visitWildcardType(WildcardType t, E exception) throws E {
				return Types.newWildcard(resolveAll(t.getLowerBounds()),resolveAll(t.getUpperBounds()));
			}
		};
		return resolver.apply(type);
	}

	public MetaClass context() {
		return this.context;
	}

	public Class<?> rawType() {
		return this.rawType;
	}

	public Type type() {
		return this.type;
	}

	public TypeVariable<?>[] typeParameters() {
		return this.rawType.getTypeParameters();
	}

	public Type[] typeArguments() {
		Type[] arguments=null;
		if(this.type instanceof ParameterizedType) {
			arguments=((ParameterizedType)this.type).getActualTypeArguments();
		} else {
			arguments=typeParameters();
		}
		return arguments;
	}

	public MetaClass superClass() {
		Class<?> superclass = this.rawType.getSuperclass();
		if(superclass==null) {
			return null;
		}
		return new MetaClass(superclass,this.rawType.getGenericSuperclass(),this);
	}

	public List<MetaClass> interfaces() {
		List<MetaClass> interfaces=Lists.newArrayList();
		Class<?>[] is=this.rawType.getInterfaces();
		Type[] gis=this.rawType.getGenericInterfaces();
		for(int i=0;i<is.length;i++) {
			interfaces.add(new MetaClass(is[i],gis[i],this));
		}
		return interfaces;
	}

	public List<MetaClass> hierarchy() {
		final List<MetaClass> result = Lists.newArrayList();
		accept(
			new MetaClassVisitor(){
				@Override
				public boolean visit(MetaClass metaType) {
					result.add(metaType);
					return true;
				}
			}
		);
		return result;
	}

	public Map<TypeVariable<?>,Type> parameterArguments() {
		final Map<TypeVariable<?>,Type> resolutions=Maps.newLinkedHashMap();
		accept(
			new MetaClassVisitor(){
				@Override
				public boolean visit(MetaClass metaType) {
					TypeVariable<?>[] parameters = metaType.typeParameters();
					Type[] arguments = metaType.typeArguments();
					for(int i=0;i<parameters.length;i++) {
						resolutions.put(parameters[i],arguments[i]);
					}
					return true;
				}
			}
		);
		return resolutions;
	}

	public MetaClass resolve(final Class<?> clazz) {
		Preconditions.checkArgument(clazz.getTypeParameters().length>0,"Class '"+clazz.getName()+"' is not parameterized");
		final AtomicReference<Type> type=new AtomicReference<Type>();
		accept(
			new MetaClassVisitor(){
				@Override
				public boolean visit(MetaClass metaClass) {
					boolean found=metaClass.rawType==clazz;
					if(found) {
						type.set(metaClass.type);
					}
					return !found;
				}
			}
		);
		return create(clazz,resolve(type.get()));
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.rawType,this.type,this.context);
	}

	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof MetaClass) {
			MetaClass that=(MetaClass)obj;
			result=
				Objects.equal(this.rawType,that.rawType) &&
				Objects.equal(this.type,that.type) &&
				Objects.equal(this.context,that.context);
		}
		return result;
	}

	public void accept(MetaClassVisitor visitor) {
		Queue<MetaClass> types=new LinkedList<MetaClass>();
		types.add(this);
		while(!types.isEmpty()) {
			MetaClass next=types.poll();
			if(!visitor.visit(next)) {
				break;
			}
			MetaClass superClass = next.superClass();
			if(superClass!=null) {
				types.offer(superClass);
			}
			types.addAll(next.interfaces());
		}
	}

	@Override
	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
						add("context",this.context).
						add("rawType",this.rawType.getName()).
						add("type",this.type).
						toString();
	}

	public static MetaClass create(Class<?> rawType) {
		Preconditions.checkNotNull(rawType,"Raw type cannot be null");
		return new MetaClass(rawType,rawType,null);
	}

	public static MetaClass create(Type type) {
		TypeFunction<Class<?>> resolver=
			new TypeFunction<Class<?>>() {
				@Override
				protected <S, E extends Exception> Class<?> visitClass(Class<S> t, E exception) throws E {
					return t;
				}
				@Override
				protected <E extends Exception> Class<?> visitGenericArrayType(GenericArrayType t, E exception) throws E {
					return Types.getArrayClass(apply(t.getGenericComponentType()));
				}
				@Override
				protected <E extends Exception> Class<?> visitParameterizedType(ParameterizedType t, E exception) throws E {
					return (Class<?>)t.getRawType();
				}
				@Override
				protected <D extends GenericDeclaration, E extends Exception> Class<?> visitTypeVariable(TypeVariable<D> t, E exception) throws E {
					return apply(t.getBounds()[0]);
				}
				@Override
				protected <E extends Exception> Class<?> visitWildcardType(WildcardType t, E exception) throws E {
					Type[] lowerBounds=t.getLowerBounds();
					Type[] upperBounds=t.getUpperBounds();
					if(lowerBounds.length==0) {
						return apply(upperBounds[0]);
					}
					return apply(lowerBounds[0]);
				}
			};
		return new MetaClass(resolver.apply(type),type,null);
	}

	public static MetaClass create(Class<?> rawType, Type type) {
		return new MetaClass(rawType,type,null);
	}

}