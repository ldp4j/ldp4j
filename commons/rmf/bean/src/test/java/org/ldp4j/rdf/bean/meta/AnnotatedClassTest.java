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


import java.util.Collection;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.ldp4j.rdf.bean.util.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ldp4j.rdf.bean.meta.AnnotatedClassHarness.*;
import static org.ldp4j.rdf.bean.meta.TestHarness.*;

public class AnnotatedClassTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(AnnotatedClassTest.class);

	@Test
	public void testAssumption$newScanner$non_generic() {
		Class<?>[] classes={Root.class,GreaterGrandParent.class,GrandParent.class,Parent.class,Example.class};
		processClasses(classes);
	}

	@Test
	public void testAssumption$newScanner$generic() {
		Class<?>[] classes={GenericRoot.class,GenericGreaterGrandParent.class,GenericGrandParent.class,GenericParent.class,GenericExample.class};
		processClasses(classes);
	}

	private void processClasses(Class<?>[] classes) {
		for(Class<?> clazz:classes) {
			processSimpleClass(clazz);
		}
	}

	private void processSimpleClass(Class<?> clazz) {
		AnnotatedClass<?> metaClass=AnnotatedClass.forClass(clazz);
		LOGGER.trace("+ {}: ",clazz.getCanonicalName());
		LOGGER.trace("\t- Regular....: {}",metaClass.toString());
		LOGGER.trace("\t- Description: {}",metaClass.describe());
	}
	private void processParameterizedClass(Class<?> clazz) {
		AnnotatedClass<?> metaClass=AnnotatedClass.forClass(clazz);
		LOGGER.trace("+ {}: ",clazz.getCanonicalName());
		LOGGER.trace("\t- Regular....: {}",metaClass.toString());
		LOGGER.trace("\t- Description: {}",metaClass.describe());
		AnnotatedMethod method=metaClass.getDeclaredMethod("get");
		LOGGER.trace("+ {}: ",method);
		LOGGER.trace("\t- Return type........: {}",method.getReturnType().getCanonicalName());
		LOGGER.trace("\t- Generic return type: {}",TypeUtils.toString(method.getGenericReturnType()));
		LOGGER.trace("\t- Raw return type....: {}",method.getRawReturnType().getCanonicalName());
		method=metaClass.getDeclaredMethod("array");
		LOGGER.trace("+ {}: ",method);
		LOGGER.trace("\t- Return type........: {}",method.getReturnType().getCanonicalName());
		LOGGER.trace("\t- Generic return type: {}",TypeUtils.toString(method.getGenericReturnType()));
		LOGGER.trace("\t- Raw return type....: {}",method.getRawReturnType().getCanonicalName());
	}

	@Test
	public void testMetaClass$simple() {
		processSimpleClass(BaseClass.class);
		processSimpleClass(Redef.class);
	}

	@Ignore("Case not valid in Java SE 7")
	@Test
	public void testMetaClass$parameterized() {
		processParameterizedClass(BaseParameterizedType.class);
		processParameterizedClass(MixinParameterizedClass.class);
	}
}

class AnnotatedClassHarness {

	public static class GenericClass<S extends Comparable<S>> {

	}

	public static class Mixin<R extends Number, S extends Comparable<S>> extends GenericClass<S> {

	}

	public static class Redef<S extends Comparable<S>> extends Mixin<Integer,S> {

	}

	public static class BaseClass extends Redef<String> {

	}

	public static class GenericParameterizedClass<S extends Collection<?>> {

		public <T> T get() {
			return null;
		}

		public <T> T[] array() {
			return null;
		}
	}

	public static class MixinParameterizedClass<R extends Number, S extends Collection<R>> extends GenericParameterizedClass<S> {

//		public <T extends Comparable<T>> T get() {
//			return null;
//		}

//		public <T extends Comparable<T>> T[] array() {
//			return null;
//		}
	}

	public static class BaseParameterizedType extends MixinParameterizedClass<Integer,Set<Integer>> {

//		public <T extends Number> T get() {
//			return null;
//		}
//
//		public <T extends Number> T[] array() {
//			return null;
//		}

	}

}
