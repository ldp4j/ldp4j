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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.ldp4j.rdf.bean.InvalidDefinitionException;
import org.ldp4j.rdf.bean.Property;
import org.ldp4j.rdf.bean.util.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PropertyScanner {

	private class Scanner {

		private List<Property> definitions=new ArrayList<Property>();
		private Map<String,String> violations=new HashMap<String,String>();
		private final PropertyFactory factory;

		private Scanner(TypeManager typeManager) {
			factory = new PropertyFactory(defaultNamespace,typeManager);
		}

		boolean isValid() {
			scanMethods();
			scanFields();
			return violations.isEmpty();
		}

		private void scanMethods() {
			LOGGER.trace("- Class: {}",clazz.getCanonicalName());
			for(Method method:clazz.getDeclaredMethods()) {
				if(LOGGER.isTraceEnabled()) {
					Class<?> declaringClass = method.getDeclaringClass();
					if(declaringClass!=Object.class) {
						LOGGER.trace("\t+ Method: {}",method.getName());
						LOGGER.trace("\t\t- Declaring class: {}",declaringClass.getCanonicalName());
						LOGGER.trace("\t\t- Local: {}",clazz==declaringClass);
						LOGGER.trace("\t\t- Return: {} ({})",method.getReturnType().getCanonicalName(),TypeUtils.toString(method.getGenericReturnType()));
					}
				}
				if(factory.isAnnotated(method)) {
					scanMethod(method);
				}
			}
		}

		private void scanMethod(Method method) {
			try {
				Property property = factory.createDefinition(method);
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Created property: "+property);
				}
				definitions.add(property);
			} catch(InvalidDefinitionException e) {
				addViolation(method, e);
			}
		}

		private void addViolation(Member member, Throwable e) {
			if(LOGGER.isDebugEnabled()) {
				String type="field";
				if(member instanceof Method) {
					type="method";
				}
				LOGGER.debug("Found invalid "+type+"'"+member.getName()+"': "+e.getMessage());
			}
			violations.put(member.getName(),e.getMessage());
		}

		private void scanFields() {
			for(Field field:clazz.getDeclaredFields()) {
				makeAccessible(field);
				if(factory.isAnnotated(field)) {
					scanField(field);
				}
			}
		}

		private void scanField(Field field) {
			try {
				Property property = factory.createDefinition(field);
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Created property: "+property);
				}
				definitions.add(property);
			} catch(InvalidDefinitionException e) {
				addViolation(field, e);
			}
		}

		private void makeAccessible(final Field field) {
			AccessController.doPrivileged(
				new PrivilegedAction<Void>() {
					@Override
					public Void run() {
						try {
							field.setAccessible(true);
							return null;
						} catch (SecurityException e) {
							throw new IllegalStateException(String.format("Field '%s' in class '%s' cannot be accessed",field.getName(), clazz.getName()),e);
						}
					}
				}
			);
		}

		public List<Property> getDefinitions() {
			return definitions;
		}

		public String getReport() {
			StringWriter result = new StringWriter();
			PrintWriter out=new PrintWriter(result);
			out.printf("Property definition violations found (%d):",violations.size());
			for(Entry<String,String> violation:violations.entrySet()) {
				out.printf("%n\t- %s (%s)",violation.getValue(),violation.getKey());
			}
			return result.toString();
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(PropertyScanner.class);

	private final Class<?> clazz;
	private final String defaultNamespace;

	PropertyScanner(Class<?> clazz, String defaultNamespace) {
		this.clazz = clazz;
		this.defaultNamespace = defaultNamespace;
	}

	List<Property> getProperties(TypeManager typeManager) {
		Scanner scanner = new Scanner(typeManager);
		if(!scanner.isValid()) {
			throw new InvalidDefinitionException(scanner.getReport());
		}
		return scanner.getDefinitions();
	}

}
