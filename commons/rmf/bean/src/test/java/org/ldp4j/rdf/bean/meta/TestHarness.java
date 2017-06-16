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

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TestHarness {
	
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	public @interface CustomAnnotation {
		int value();
	}

	public static class GenericRoot<R extends TestHarness.Root> {
		
		private R root;

		public R getRoot() {
			return root;
		}
		
		public void setRoot(R Root) {
			root = Root;
		}
		
	}
	
	public static class GenericGreaterGrandParent<GGP extends TestHarness.GreaterGrandParent> extends TestHarness.GenericRoot<GGP> {

		@Override
		public GGP getRoot() {
			return super.getRoot();
		}
		
	}
	
	public static class GenericGrandParent<GP extends TestHarness.GrandParent> extends TestHarness.GenericGreaterGrandParent<GP> {

		@Override
		public void setRoot(GP root) {
			super.setRoot(root);
		}
		
	}

	public static class GenericParent<P extends TestHarness.Parent> extends TestHarness.GenericGrandParent<P> {

		private TestHarness.Parent unknown;
	
		@SuppressWarnings("unchecked")
		public <T extends TestHarness.Parent> T getUnknown() {
			return (T)unknown;
		}
		
		public <T extends TestHarness.Parent> void setUnknown(T unknown) {
			this.unknown = unknown;
		}

	}

	public static class GenericExample extends TestHarness.GenericParent<TestHarness.Example> {

		@Override
		public void setRoot(TestHarness.Example Root) {
			super.setRoot(Root);
		}

		@Override
		public TestHarness.Example getRoot() {
			return super.getRoot();
		}
		
	}

	@CustomAnnotation(4)
	public static class Root {

		private int primitive;
		private TestHarness.Root parent;

		public boolean isRootMethod() {
			return true;
		}
		
		public int getRootPrimitive() {
			return primitive;
		}

		public void setRootPrimitive(int primitive) {
			this.primitive=primitive;
		}
		
		@CustomAnnotation(5)
		public TestHarness.Root getParent() {
			return parent;
		}
		public void setParent(TestHarness.Root parent) {
			this.parent=parent;
		}
		
	}

	@CustomAnnotation(3)
	public static class GreaterGrandParent extends TestHarness.Root {
		private String string;

		@Override
		@CustomAnnotation(4)
		public TestHarness.GreaterGrandParent getParent() {
			return (TestHarness.GreaterGrandParent)super.getParent();
		}

		public void setParent(TestHarness.GreaterGrandParent parent) {
			super.setParent(parent);
		}

		@Override
		public int getRootPrimitive() {
			return super.getRootPrimitive()+1;
		}

		public String getGreaterGrandParentString() {
			return string;
		}

		public void setGreaterGrandParentString(String string) {
			this.string=string;
		}
		
	}

	@CustomAnnotation(2)
	public static class GrandParent extends TestHarness.GreaterGrandParent {
		@Override
		public TestHarness.GrandParent getParent() {
			return this;
		}
		@Override
		public boolean isRootMethod() {
			return false;
		}
		@Override
		public void setRootPrimitive(int primitive) {
			super.setRootPrimitive(primitive);
		}
	}

	@CustomAnnotation(1)
	public static class Parent extends TestHarness.GrandParent {
		@Override
		public TestHarness.Parent getParent() {
			return this;
		}
	}

	public static class Example extends TestHarness.Parent {
		@Override
		@CustomAnnotation(1)
		public TestHarness.Example getParent() {
			return this;
		}
		@Override
		public void setRootPrimitive(int primitive) {
			super.setRootPrimitive(primitive);
		}
	}

}