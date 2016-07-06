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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-testing:0.2.1
 *   Bundle      : ldp4j-commons-testing-0.2.1.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons.testing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Common testing utilities.
 */
public final class Utils {

	private Utils() {
	}

	/**
	 * Returns the default string representation of an object as implemented in
	 * the {@code Object} class.
	 *
	 * @param obj
	 *            the object
	 * @return the string representation of the specified object
	 */
	public static String defaultToString(Object obj) {
		return
			obj.getClass().getName()+"@"+Integer.toHexString(obj.hashCode());
	}

	/**
	 * Determine whether a class is a proper utility class (final class +
	 * private no-arg constructor).
	 *
	 * @param clazz
	 *            the library class to evaluate
	 * @return {@code true} if the specified class is a propery utility class.
	 */
	public static <T> boolean isUtilityClass(Class<? extends T> clazz) {
		if(!isPotentialLibraryClass(clazz)) {
			return false;
		}
		Constructor<?>[] constructor = clazz.getDeclaredConstructors();
		if(constructor.length!=1) {
			return false;
		}
		Constructor<?> defaultConstructor = constructor[0];
		if(isValidDefaultConstructor(defaultConstructor)) {
			invoke(defaultConstructor);
			return true;
		}
		return false;
	}

	private static boolean isValidDefaultConstructor(Constructor<?> defaultConstructor) {
		return
			Modifier.isPrivate(defaultConstructor.getModifiers()) &&
			defaultConstructor.getParameterTypes().length==0;
	}

	private static void invoke(Constructor<?> defaultConstructor) {
		try {
			defaultConstructor.setAccessible(true);
			defaultConstructor.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Should be able to instantiate library",e);
		}
	}

	private static <T> boolean isPotentialLibraryClass(Class<? extends T> clazz) {
		return
			Modifier.isFinal(clazz.getModifiers()) &&
			(Modifier.isStatic(clazz.getModifiers()) || clazz.getEnclosingClass()==null) ;
	}

}
