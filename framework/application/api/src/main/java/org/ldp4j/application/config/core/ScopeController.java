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
package org.ldp4j.application.config.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

final class ScopeController {

	private interface ContextProvider {
		Class<?>[] context();
	}

	private static final class LocalSecurityManager extends SecurityManager implements ContextProvider {

		@Override
		public Class<?>[] context() {
			return super.getClassContext();
		}

	}

	private static final class SecurityManagerIntrospector implements ContextProvider {

		private final SecurityManager manager;
		private final Method getClassContext;

		private SecurityManagerIntrospector(SecurityManager manager) {
			this.manager = manager;
			this.getClassContext=
				AccessController.doPrivileged(
					new PrivilegedAction<Method>() {
						@Override
						public Method run() {
							try {
								Method method = SecurityManager.class.getDeclaredMethod("getClassContext");
								method.setAccessible(true);
								return method;
							} catch (NoSuchMethodException e) {
								throw new IllegalStateException("Could not find required method",e);
							} catch (SecurityException e) {
								throw new IllegalStateException("Should have access to the method within a priviledged action",e);
							}
						}
					});
		}

		@Override
		public Class<?>[] context() {
			return
				AccessController.doPrivileged(
					new PrivilegedAction<Class<?>[]>() {
						@Override
						public Class<?>[] run() {
							try {
								return (Class<?>[])getClassContext.invoke(manager);
							} catch (SecurityException e) {
								throw new IllegalStateException("Should be able to invoke the method within a priviledged action",e);
							} catch (IllegalAccessException e) {
								throw new IllegalStateException("Should be able to invoke an accessible method",e);
							} catch (IllegalArgumentException e) {
								throw new IllegalStateException("Should be able to invoke the method using a valid instance",e);
							} catch (InvocationTargetException e) {
								throw new IllegalStateException("Unexpected method exception",e);
							}
						}
					});
		}

	}

	private static final int MIN_DEPTH=2;

	private static final ScopeController INSTANCE=init();

	private final ContextProvider provider;

	private ScopeController(ContextProvider provider) {
		this.provider = provider;
	}

	boolean invokedFrom(Class<?>... classes) {
		Class<?>[] classContext = this.provider.context();
		int target = findTargetPosition(classContext);
		if(classContext.length<target) return false;
		Class<?> invoker=classContext[target];
		for(Class<?> candidateClass:classes) {
			if(invoker==candidateClass) {
				return true;
			}
		}
		return false;
	}

	private int findTargetPosition(Class<?>[] classContext) {
		int me=0;
		while(classContext[++me]!=getClass());
		int target = me+MIN_DEPTH;
		return target;
	}

	static ScopeController getInstance() {
		return INSTANCE;
	}

	private static ScopeController init() {
		ContextProvider provider=null;
		SecurityManager jvmSecurityManager = System.getSecurityManager();
		if(jvmSecurityManager!=null) {
			provider=new SecurityManagerIntrospector(jvmSecurityManager);
		} else {
			provider=
				AccessController.
					doPrivileged(
						new PrivilegedAction<ContextProvider>(){
							@Override
							public ContextProvider run() {
								return new LocalSecurityManager();
							}
						}
					);
		}
		return new ScopeController(provider);
	}

}