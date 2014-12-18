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
package org.ldp4j.reflect.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Lists;


/**
 * A helper class to simplify the handling on Java language modifiers.
 *
 * @author Miguel Esteban Guti&eacute;rrez
 * @see java.lang.reflect.Modifier
 */
public final class Modifiers {

	private interface Checker {

		boolean check(int modifier);

	}

	private static class ImmutableChecker implements Checker {

		private int modifier;

		private ImmutableChecker(int modifier) {
			this.modifier = modifier;
		}

		@Override
		public boolean check(int modifier) {
			return (this.modifier & modifier) != 0;
		}

		private static ImmutableChecker of(int pattern) {
			return new ImmutableChecker(pattern);
		}

		private static int any(int modifier, int... others) {
			int result=modifier;
			for(int other:others) {
				result&=other;
			}
			return result;
		}

		private static int not(int modifier) {
			return ~modifier;
		}

	}


	public enum Modifier {
		PUBLIC(ImmutableChecker.of(java.lang.reflect.Modifier.PUBLIC)),
		PRIVATE(ImmutableChecker.of(java.lang.reflect.Modifier.PRIVATE)),
		PROTECTED(ImmutableChecker.of(java.lang.reflect.Modifier.PROTECTED)),
		DEFAULT(
			ImmutableChecker.of(
				ImmutableChecker.not(
					ImmutableChecker.any(
						java.lang.reflect.Modifier.PUBLIC,
						java.lang.reflect.Modifier.PRIVATE,
						java.lang.reflect.Modifier.PROTECTED)
					)
				)
			),
		STATIC(ImmutableChecker.of(java.lang.reflect.Modifier.STATIC)),
		INSTANCE(ImmutableChecker.of(ImmutableChecker.not(java.lang.reflect.Modifier.STATIC))),
		ABSTRACT(ImmutableChecker.of(java.lang.reflect.Modifier.ABSTRACT)),
		IMPLEMENTED(ImmutableChecker.of(ImmutableChecker.not(java.lang.reflect.Modifier.ABSTRACT))),
		FINAL(ImmutableChecker.of(java.lang.reflect.Modifier.FINAL)),
		NATIVE(ImmutableChecker.of(java.lang.reflect.Modifier.NATIVE)),
		STRICT(ImmutableChecker.of(java.lang.reflect.Modifier.STRICT)),
		SYNCHRONIZED(ImmutableChecker.of(java.lang.reflect.Modifier.SYNCHRONIZED)),
		VOLATILE(ImmutableChecker.of(java.lang.reflect.Modifier.VOLATILE)),
		TRANSIENT(ImmutableChecker.of(java.lang.reflect.Modifier.TRANSIENT)),
		INTERFACE(ImmutableChecker.of(java.lang.reflect.Modifier.INTERFACE)),
		;

		private Checker checker;

		private Modifier(Checker checker) {
			this.checker = checker;
		}

		protected final boolean isDefined(int modifiers) {
			return this.checker.check(modifiers);
		}

	}

	private final EnumSet<Modifier> modifiers;

	private Modifiers(EnumSet<Modifier> modifiers) {
		this.modifiers = modifiers;
	}

	public Set<Modifier> values() {
		return EnumSet.copyOf(this.modifiers);
	}

	public boolean is(Modifier modifier) {
		checkNotNull(modifier,"Modifier cannot be null");
		return this.modifiers.contains(modifier);
	}

	public boolean is(Modifier... modifiers) {
		checkNotNull(modifiers,"Modifiers cannot be null");
		return is(Lists.newArrayList(modifiers));
	}

	public boolean is(Iterable<Modifier> modifiers) {
		checkNotNull(modifiers,"Modifiers cannot be null");
		return is(modifiers.iterator());
	}

	public boolean is(Iterator<Modifier> modifiers) {
		checkNotNull(modifiers,"Modifiers cannot be null");
		while(modifiers.hasNext()) {
			if(!this.modifiers.contains(modifiers.next())) {
				return false;
			}
		}
		return true;
	}

	public boolean isAnyOf(Modifier... modifiers) {
		checkNotNull(modifiers,"Modifiers cannot be null");
		return isAnyOf(Lists.newArrayList(modifiers));
	}

	public boolean isAnyOf(Iterable<Modifier> modifiers) {
		checkNotNull(modifiers,"Modifiers cannot be null");
		return isAnyOf(modifiers.iterator());
	}

	public boolean isAnyOf(Iterator<Modifier> modifiers) {
		checkNotNull(modifiers,"Modifiers cannot be null");
		while(modifiers.hasNext()) {
			if(this.modifiers.contains(modifiers.next())) {
				return true;
			}
		}
		return false;
	}

	public boolean isNot(Modifier modifier) {
		return !is(modifier);
	}

	public boolean isNoneOf(Modifier... modifiers) {
		checkNotNull(modifiers,"Modifiers cannot be null");
		return isNoneOf(Lists.newArrayList(modifiers));
	}

	public boolean isNoneOf(Iterable<Modifier> modifiers) {
		checkNotNull(modifiers,"Modifiers cannot be null");
		return isNoneOf(modifiers.iterator());
	}

	public boolean isNoneOf(Iterator<Modifier> modifiers) {
		return !is(modifiers);
	}

	public static Modifiers of(Class<?> clazz) {
		checkNotNull(clazz,"Class cannot be null");
		return new Modifiers(unroll(clazz.getModifiers()));
	}

	public static Modifiers of(Member member) {
		checkNotNull(member,"Member cannot be null");
		return new Modifiers(unroll(member.getModifiers()));
	}

	private static EnumSet<Modifier> unroll(int modifiers) {
		EnumSet<Modifier> result=EnumSet.noneOf(Modifier.class);
		for(Modifier modifier:Modifier.values()) {
			if(modifier.isDefined(modifiers)) {
				result.add(modifier);
			}
		}
		return result;
	}

}