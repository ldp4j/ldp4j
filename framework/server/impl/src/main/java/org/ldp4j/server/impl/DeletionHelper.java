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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withParameters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import javassist.Modifier;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ldp4j.server.core.Deletable;
import org.ldp4j.server.core.Delete;
import org.ldp4j.server.core.DeletionException;
import org.ldp4j.server.core.DeletionResult;
import org.ldp4j.server.core.ILinkedDataPlatformResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;

final class DeletionHelper {

	/**
	 * Internal logger.
	 */
	private static final Logger LOGGER=LoggerFactory.getLogger(LinkedDataPlatformRegistry.class);

	private final class ReflectionBasedDeletable implements Deletable {

		private final Method method;

		private ReflectionBasedDeletable(Method method) {
			this.method = method;
		}

		@Override
		public DeletionResult delete(String id) throws DeletionException {
			try {
				Object rawResult = method.invoke(resourceHandler, id);
				if(rawResult==null) {
					throw new IllegalStateException("Deletion method returned a null result");
				}
				return (DeletionResult)rawResult;
			} catch (IllegalArgumentException e) {
				throw new DeletionException(String.format("Method '%s' should accept a single string as parameter",method),e);
			} catch (IllegalAccessException e) {
				throw new DeletionException(String.format("Method '%s' should be public",method),e);
			} catch (InvocationTargetException e) {
				throw invocationFailure(method,e);
			}
		}
	}

	private final class ResourceHandlerWrapper implements Deletable {

		private final boolean supported;
		private final Deletable delegate;

		public ResourceHandlerWrapper(Deletable delegate, boolean supported) {
			this.delegate = delegate;
			this.supported = supported;
		}
		
		@Override
		public DeletionResult delete(String id) throws DeletionException {
			if(!supported) {
				throw new DeletionException(String.format("Resource handler class '%s' does not support deletion",resourceHandler.getClass().getCanonicalName()));
			}
			return delegate.delete(id);
		}
		
		public boolean isDeletionSupported() {
			return supported;
		}
		
	}
	
	private final ILinkedDataPlatformResourceHandler resourceHandler;

	private ResourceHandlerWrapper wrapper=null;
	
	public DeletionHelper(ILinkedDataPlatformResourceHandler resourceHandler) {
		this.resourceHandler = resourceHandler;
	}

	private DeletionException invocationFailure(Method method, InvocationTargetException e) {
		Throwable cause = e.getCause();
		if(cause instanceof DeletionException) {
			return (DeletionException)cause;
		} else {
			return new DeletionException(String.format("Unexpected exception thrown when invoking method '%s'",method),cause);
		}
	}

	private Method getDeleteMethod(Class<?> resourceHandlerclass) {
		Method result = null;
		@SuppressWarnings("unchecked")
		Set<Method> deleters = 
			getAllMethods(
				resourceHandlerclass,
				Predicates.<Method>and(
					withModifier(Modifier.PUBLIC),
					withParameters(String.class),
					withAnnotation(Delete.class)));
		if(deleters.isEmpty()) {
			String error = String.format("No delete found in resource handler class '%s' (public method annotated with @Delete and a single String parameter)",resourceHandlerclass.getCanonicalName());
			LOGGER.warn(error);
		} else if (deleters.size()>1) {
			String error = String.format("More than one delete method defined in resource handler class '%s'",resourceHandlerclass.getCanonicalName());
			LOGGER.warn(error);
		} else {
			result=deleters.iterator().next();
			Class<?> returnType = result.getReturnType();
			if(!DeletionResult.class.isAssignableFrom(returnType)) {
				result=null;
				String error = String.format("Delete method does not return an instance of class  '%s' (found '%s')",DeletionResult.class.getCanonicalName(),returnType.getCanonicalName());
				LOGGER.warn(error);
			}
		}
		return result;
	}

	private synchronized ResourceHandlerWrapper getWrapper() {
		if(wrapper==null) {
			Class<? extends ILinkedDataPlatformResourceHandler> resourceHandlerClass = resourceHandler.getClass();
			if(Deletable.class.isInstance(resourceHandler)) {
				wrapper=new ResourceHandlerWrapper((Deletable)resourceHandler,true);
			} else {
				final Method method = getDeleteMethod(resourceHandlerClass);
				if(method!=null) {
					wrapper=new ResourceHandlerWrapper(new ReflectionBasedDeletable(method),true);
				} else {
					wrapper=new ResourceHandlerWrapper(null, false);
				}
			}
		}
		return wrapper;
	}

	public boolean isDeletionSupported() {
		return getWrapper().isDeletionSupported();
	}

	public Response delete(String resourceId) throws DeletionException {
		ResourceHandlerWrapper wrapper = getWrapper();
		if(!wrapper.isDeletionSupported()) {
			throw new DeletionException(String.format("Resource handler class '%s' does not support deletion",resourceHandler.getClass().getCanonicalName()));
		}
		DeletionResult result=wrapper.delete(resourceId);
		Response response=null;
		if(result.isEnacted()) {
			String message=result.getMessage();
			if(message==null) {
				response=
						Response.
							status(Status.NO_CONTENT).
							build();
			} else {
				response=
						Response.
							status(Status.OK).
							entity(message).
							build();
			}
		} else {
			response=
				Response.
					status(Status.ACCEPTED).
					entity(result.getMessage()).
					build();
		}
		return response;
	}

}