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
package org.ldp4j.rdf.bean.example;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.bean.JARBContext;
import org.ldp4j.rdf.bean.NamingPolicy;
import org.ldp4j.rdf.bean.example.services.Controller;
import org.ldp4j.rdf.bean.example.transactions.Transaction;
import org.ldp4j.rdf.bean.example.transactions.TransactionCapabilities;
import org.ldp4j.rdf.bean.example.transactions.TransactionControl;
import org.ldp4j.rdf.bean.example.transactions.TransactionOperation;
import org.ldp4j.rdf.util.RDFModelDSL;
import org.ldp4j.rdf.util.TripleSet;

public class ImplementationDriver {

	public static class Super<T> {
		
		private Set<T> values;

		public void setValues(Set<T> values) {
			this.values = values;
		}
		
		public Set<T> getValues() {
			return values;
		}
		
	}
	
	public static class Child extends Super<String> {

		@Override
		public Set<String> getValues() {
			Set<String> values = super.getValues();
			values.add("ME");
			return values;
		}

	}
	
	public static class Root {
		
	}

	public static class Leaf extends Root {
		
	}
	
	public static class Overridable<T extends Root> {
		private T root;

		public void setRoot(T root) {
			this.root = root;
		}
		
		public T getRoot() {
			return root;
		}
	}

	public static class Overriding extends Overridable<Leaf> {
		
		@Override
		public void setRoot(Leaf root) {
			super.setRoot(root);
		}
		
		@Override
		public Leaf getRoot() {
			return super.getRoot();
		}
		
	}

	public class CustomDeploymentPolicy implements NamingPolicy {

		private long ctr=0;
		
		public <T> Resource<?> createIdentity(T object) {
			return RDFModelDSL.uriRef(URI.create(String.format("urn:%03X:%08X", ctr++,System.identityHashCode(object))));
		}

		@Override
		public <T> Resource<?> enumeratedIdentity(T object) {
			if(object instanceof TransactionOperation) {
				TransactionOperation to=(TransactionOperation)object;
				return RDFModelDSL.uriRef("http://delicias.dia.fi.upm.es/alm-istack/transactions/transactions#"+to.name());
			}
			throw new IllegalStateException("Unknown enumerated object '"+object+"'");
		}

		@Override
		public <T> T resolveEnumerated(Resource<?> identity, Class<? extends T> clazz) {
			throw new UnsupportedOperationException("Method not implemented yet");
		}

	}
	
	@Test
	public void testAssumption$overridedMethodExecutionByReflection() throws Exception {
		Child child=new Child();
		child.setValues(new HashSet<String>());
		Method declaredMethod = Super.class.getDeclaredMethod("getValues");
		Object result = declaredMethod.invoke(child);
		assertThat(result,instanceOf(Set.class));
		assertThat((Set<?>)result,contains((Object)"ME"));
	}
	
	@Test
	public void testAssumption$overridedMethodIntrospection() throws Exception {
		ClassDescription<Overriding> cl=ClassDescription.newInstance(Overriding.class);
		System.err.println(
			cl.
				recursive(true).
				traverseClasses(false).
				traverseInterfaces(true).
				traverseSuperclass(true).
				ignoreDefaultMethods(true).
				ignoreDefaultSuperclass(true).
				excludePublicElementsFromDeclared(true).
				toString());
		introspect(Child.class,"getValues");
		introspect(Child.class,"setValues",Set.class);
		introspect(Overriding.class,"getRoot");
		introspect(Overriding.class,"setRoot",Root.class);
	}

	private void introspect(Class<?> clazz, String method, Class<?>... params) {
		System.err.printf("Looking for %s(%s):%n",method,Arrays.asList(params));
		Class<?> c=clazz;
		while(c!=null) {
			try {
				Method m = c.getDeclaredMethod(method, params);
				printMethod(m);
			} catch (Exception e) {
				System.err.println("Not found in '"+c.getCanonicalName()+"'...");
			}
			c=c.getSuperclass();
			if(c==Object.class) {
				c=null;
			}
		}
	}

	private void printMethod(Method method) {
		System.err.printf(
				"%s: %s %s %s(%s) {bridge=%s,synthetic=%s,varargs=%s}%n",
				method.getDeclaringClass().getCanonicalName(),
				Modifier.toString(method.getModifiers()),
				method.getGenericReturnType(),
				method.getName(),
				Arrays.asList(method.getGenericParameterTypes()),
				method.isBridge(),
				method.isSynthetic(),
				method.isVarArgs()
				);
	}
	
	@Test
	public void shouldWork() {
		Transaction transaction = getTransaction();
		JARBContext context = JARBContext.newInstance(new CustomDeploymentPolicy());
		TripleSet triples = context.deflate(transaction);
		Resource<?> identity=context.getIdentity(transaction);
		System.err.printf("- Main individual: %s%n- Serialization:%n%s%n",identity,triples);
		Transaction inflate = context.inflate(identity,triples,Transaction.class);
		System.err.printf("- Inflated: %s%n",inflate);
	}

	private Transaction getTransaction() {
		Controller controller=new Controller();
		TransactionControl control1=new TransactionControl();
		control1.setOperation(TransactionOperation.ENROLL);
		control1.setController(controller);
		TransactionControl control2=new TransactionControl();
		control2.setOperation(TransactionOperation.COMMIT);
		control2.setController(new Controller());
		HashSet<TransactionControl> controls = new HashSet<TransactionControl>();
		controls.add(control1);
		controls.add(control2);
		TransactionCapabilities capabilities = new TransactionCapabilities();
		capabilities.setControls(controls);
		Transaction t=new Transaction();
		t.setCapabilities(capabilities);
		return t;
	}
	
	@Test
	public void testAssumption$genericRequiredSafeTypeCheck() throws Exception {
		TransactionControl control2=new TransactionControl();
		control2.setOperation(TransactionOperation.COMMIT);
		control2.setController(new Controller());
//		Control<?> c=(Control<?>)control2;
//		try {
//			Method method = TransactionControl.class.getMethod("setOperation", Operation.class);
//			method.invoke(c,new Operation());
//			fail("Update should not be allowed");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		assertThat(control2.getOperation(),equalTo(TransactionOperation.COMMIT));
	}
	
}
