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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-core:0.2.2
 *   Bundle      : rmf-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.rdf.Format;
import org.ldp4j.rdf.spi.Marshaller;
import org.ldp4j.rdf.spi.RuntimeInstance;
import org.ldp4j.rdf.spi.Transformer;
import org.ldp4j.rdf.spi.Unmarshaller;
import org.ldp4j.rdf.spi.annotations.Transformable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RuntimeInstanceImpl extends RuntimeInstance {

	private static final Logger LOGGER=LoggerFactory.getLogger(RuntimeInstance.class);
	

	private final Map<Class<?>, Transformer<?>> transformers;

	public RuntimeInstanceImpl() {
		transformers=new HashMap<Class<?>, Transformer<?>>();
	}

	private static void trace(String format, Object... args) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(String.format(format,args));
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Transformer<T> instantiateTransformerImplementation(Class<?> transformerImplementationClass) {
		Class<? extends Transformer<T>> creator=(Class<? extends Transformer<T>>)transformerImplementationClass;
		try {
			return creator.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static boolean isValidTransformer(Class<?> transformableClass, Class<?> transformerImplementationClass) {
		boolean result=false;
		if(Transformer.class.isAssignableFrom(transformerImplementationClass)) {
			TypeVariable<?> targetVariable = Transformer.class.getTypeParameters()[0];
			Class<?> seed=transformerImplementationClass;
			while(!result && seed!=null) {
				TypeVariable<?>[] typeParameters = seed.getTypeParameters();
				for(int i=0;i<typeParameters.length && !result ; i++) {
					result=checkVariable(typeParameters[i], targetVariable, transformableClass);
				}
				seed=seed.getSuperclass();
			}
		}
		return result;
	}

	/**
	 * @param checkedVariable
	 * @param targetVariable
	 * @param targetClass
	 * @return
	 */
	private static boolean checkVariable(TypeVariable<?> checkedVariable, TypeVariable<?> targetVariable, Class<?> targetClass) {
		boolean assignable=false;
		if(targetVariable.getGenericDeclaration().equals(checkedVariable.getGenericDeclaration())) {
			Type type = checkedVariable.getBounds()[0];
			if(type instanceof Class<?>) {
				Class<?> expectation=(Class<?>)type;
				assignable = expectation.isAssignableFrom(targetClass);
			}
		}
		return assignable;
	}

	@SuppressWarnings("unchecked")
	private static <T> Transformer<T> cast(Transformer<?> transformer, Class<? extends T> transformable) {
		if(!isValidTransformer(transformable, transformer.getClass())) {
			throw new IllegalArgumentException(transformer.getClass() + " does not implement support "+ transformable.getName() + " elements");
		}
		return (Transformer<T>)transformer;
	}

	private <T> Transformer<T> getRegisteredTransformer(Class<? extends T> transformable) {
		Transformer<T> result=null;
		for(Entry<Class<?>,Transformer<?>> entry:transformers.entrySet()) {
			Class<?> registeredClass = entry.getKey();
			if(registeredClass.isAssignableFrom(transformable)) {
				trace("Found compatible transformer registered for type '%s' (%s).",transformable.getName(),registeredClass.getName());
				try {
					result=cast(entry.getValue(),transformable);
					trace("Using registered transformer '%s' via type '%s'.",result.getClass().getName(),registeredClass.getName());
					break;
				} catch (IllegalArgumentException e) {
					trace("Invalid transformer registration for type '%s'. Transformer '%s' does not support source class '%s'.",registeredClass.getName(),entry.getValue().getClass().getName(),transformable.getName());
					throw new IllegalStateException("Invalid transformer registration",e);
				}
			}
		}
		return result;
	}

	private <T> Transformer<T> getTransformerFromAnnotation(Class<? extends T> transformable) {
		Transformer<T> result=null;
		for (Class<?> clz=transformable; clz!=null && result==null; clz=clz.getSuperclass()) {
			Transformable annotation = clz.getAnnotation(Transformable.class);
			if(annotation!=null) {
				result=tryAnnotation(annotation.transformer(), transformable, clz);
			}
		}
		return result;
	}

	/**
	 * @param annotation
	 * @param transformable
	 * @param clz
	 * @return
	 */
	private <T> Transformer<T> tryAnnotation(Class<? extends Transformer<?>> transformerImplementationClass, Class<? extends T> transformable, Class<?> clz) {
		Transformer<T> transformer=null;
		trace("Type '%s' is transformable via type '%s'.",transformable.getName(),clz.getName());
		try {
			if(!isValidTransformer(transformable, transformerImplementationClass)) {
				throw new IllegalArgumentException("Not a valid transformer defined");
			}
			transformer=instantiateTransformerImplementation(transformerImplementationClass);
			trace("Using declared transformer '"+transformer.getClass().getName()+"' via type '"+clz.getName()+"'.");
		} catch (IllegalArgumentException e) {
			trace("Incompatible transformer declaration for type '"+clz.getClass().getName()+"'. Transformer class '"+transformerImplementationClass.getName()+"' does not support source class '"+transformable.getName()+"'.");
			throw new IllegalStateException("Incompatible transformer declaration",e);
		}
		return transformer;
	}

	@Override
	public <T> void registerTransformer(Class<? extends T> type, Transformer<T> transformer) {
		trace("Registered transformer '"+transformer.getClass().getName()+"' for type '"+type.getName()+"'.");
		transformers.put(type, transformer);
	}

	@Override
	public <T> Transformer<T> findTransformer(Class<? extends T> transformable) {
		trace("Requested transformer for source class '"+transformable.getName()+"'...");

		Transformer<?> tmp = transformers.get(transformable);
		if(tmp!=null) {
			trace("Using transformer '"+tmp.getClass().getName()+"' registered for type '"+transformable.getName()+"'.");
			return cast(tmp,transformable);
		}
		
		trace("No transformer registered for type '"+transformable.getName()+"'. Checking if type is transformable...");
		Transformer<T> result=getTransformerFromAnnotation(transformable);
		if(result!=null) {
			registerTransformer(transformable, result);
			return result;
		}

		trace("Type '"+transformable.getName()+"' is not transformable. Looking for compatible registered transformer...");
		result=getRegisteredTransformer(transformable);
		if(result!=null) {
			registerTransformer(transformable, result);
			return result;
		}

		trace("No compatible transformer registered for type '"+transformable.getName()+"'.");
		result=new NullTransformer<T>();
		trace("Using default transformer '"+result.getClass().getName()+"'.");
		return result;
	}

	@Override
	public <T> Marshaller<T> newMarshaller(Format format, T output) {
		Marshaller<T> marshaller=null;
		if(Writer.class.isInstance(output)) {
			@SuppressWarnings("unchecked")
			Marshaller<T> tmp = (Marshaller<T>)new WriterMarshaller();
			marshaller=tmp;
		} else if(OutputStream.class.isInstance(output)) {
			@SuppressWarnings("unchecked")
			Marshaller<T> tmp = (Marshaller<T>)new OutputStreamMarshaller();
			marshaller=tmp;
		} else {
			marshaller=new UnavailableMarshaller<T>(format, output);
		}
		return marshaller;
	}

	@Override
	public <T> Unmarshaller<T> newUnmarshaller(Format format, T source) {
		Unmarshaller<T> unmarshaller=null;
		if(CharSequence.class.isInstance(source)) {
			@SuppressWarnings("unchecked")
			Unmarshaller<T> tmp = (Unmarshaller<T>) new CharSequenceUnmarshaller();
			unmarshaller=tmp;
		} else if(InputStream.class.isInstance(source)) {
			@SuppressWarnings("unchecked")
			Unmarshaller<T> tmp = (Unmarshaller<T>) new InputStreamUnmarshaller();
			unmarshaller=tmp;
		} else if(Reader.class.isInstance(source)) {
			@SuppressWarnings("unchecked")
			Unmarshaller<T> tmp = (Unmarshaller<T>) new ReaderUnmarshaller();
			unmarshaller=tmp;
		} else {
			unmarshaller=new UnavailableUnmarshaller<T>(format, source);
		}
		return unmarshaller;
	}

}