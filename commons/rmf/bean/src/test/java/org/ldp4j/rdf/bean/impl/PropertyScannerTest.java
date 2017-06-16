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


import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.rdf.bean.annotations.Property;
import org.ldp4j.rdf.bean.example.transactions.Transaction;
import org.ldp4j.rdf.bean.impl.PropertyScanner;
import org.ldp4j.rdf.bean.impl.TransactionalTypeRegistry;
import org.ldp4j.rdf.bean.impl.TypeManager;
import org.ldp4j.rdf.bean.impl.TypeManagerImpl;

public class PropertyScannerTest {

	@SuppressWarnings("unused")
	private abstract static class AnnotatedClass<C extends Transaction> {

		@Property(name="privateFinalStaticStringLiteral")
		private final static String privateFinalStaticStringLiteral="";

		@Property(name="protectedFinalStaticStringLiteral")
		protected final static String protectedFinalStaticStringLiteral="";

		@Property(name="publicFinalStaticStringLiteral")
		public final static String publicFinalStaticStringLiteral="";

		@Property(name="privateStaticStringLiteral")
		private static String privateStaticStringLiteral="";

		@Property(name="protectedStaticStringLiteral")
		protected static String protectedStaticStringLiteral="";

		@Property(name="publicStaticStringLiteral")
		public static String publicStaticStringLiteral="";

		@Property(name="privateStringLiteral")
		private String privateStringLiteral="";

		@Property(name="protectedStringLiteral")
		protected String protectedStringLiteral="";

		@Property(name="publicStringLiteral")
		public String publicStringLiteral="";

		@Property(name="privateFinalStaticWrappedLiteral")
		private final static Integer privateFinalStaticWrappedLiteral=0;

		@Property(name="protectedFinalStaticWrappedLiteral")
		protected final static Integer protectedFinalStaticWrappedLiteral=0;

		@Property(name="publicFinalStaticWrappedLiteral")
		public final static Integer publicFinalStaticWrappedLiteral=0;

		@Property(name="privateStaticWrappedLiteral")
		private static Integer privateStaticWrappedLiteral=0;

		@Property(name="protectedStaticWrappedLiteral")
		protected static Integer protectedStaticWrappedLiteral=0;

		@Property(name="publicStaticWrappedLiteral")
		public static Integer publicStaticWrappedLiteral=0;

		@Property(name="privateFinalWrappedLiteral")
		private final Integer privateFinalWrappedLiteral=0;

		@Property(name="protectedFinalWrappedLiteral")
		protected final Integer protectedFinalWrappedLiteral=0;

		@Property(name="publicFinalWrappedLiteral")
		public final Integer publicFinalWrappedLiteral=0;

		@Property(name="privateWrappedLiteral")
		private Integer privateWrappedLiteral=0;

		@Property(name="protectedWrappedLiteral")
		protected Integer protectedWrappedLiteral=0;

		@Property(name="publicWrappedLiteral")
		public Integer publicWrappedLiteral=0;

		@Property(name="privateFinalStaticPrimitiveLiteral")
		private final static int privateFinalStaticPrimitiveLiteral=0;

		@Property(name="protectedFinalStaticPrimitiveLiteral")
		protected final static int protectedFinalStaticPrimitiveLiteral=0;

		@Property(name="publicFinalStaticPrimitiveLiteral")
		public final static int publicFinalStaticPrimitiveLiteral=0;

		@Property(name="privateStaticPrimitiveLiteral")
		private static int privateStaticPrimitiveLiteral=0;

		@Property(name="protectedStaticPrimitiveLiteral")
		protected static int protectedStaticPrimitiveLiteral=0;

		@Property(name="publicStaticPrimitiveLiteral")
		public static int publicStaticPrimitiveLiteral=0;

		@Property(name="privateFinalPrimitiveLiteral")
		private final int privateFinalPrimitiveLiteral=0;

		@Property(name="protectedFinalPrimitiveLiteral")
		protected final int protectedFinalPrimitiveLiteral=0;

		@Property(name="publicFinalPrimitiveLiteral")
		public final int publicFinalPrimitiveLiteral=0;

		@Property(name="privatePrimitiveLiteral")
		private int privatePrimitiveLiteral=0;

		@Property(name="protectedPrimitiveLiteral")
		protected int protectedPrimitiveLiteral=0;

		@Property(name="publicPrimitiveLiteral")
		public int publicPrimitiveLiteral=0;

		@Property(name="privateFinalGeneric")
		private final C privateFinalGeneric=null;

		@Property(name="protectedFinalGeneric")
		protected final C protectedFinalGeneric=null;

		@Property(name="publicFinalGeneric")
		public final C publicFinalGeneric=null;

		@Property(name="privateGeneric")
		private C privateGeneric;

		@Property(name="protectedGeneric")
		protected C protectedGeneric;

		@Property(name="publicGeneric")
		public C publicGeneric;

		@Property(name="external")
		public <F extends Transaction> F getExternalGeneric() {
			return null;
		}

		public <F extends Transaction> void setExternalGeneric(F value) {

		}

		@Property(name="readableOnly")
		public <F extends Transaction> F getReadableOnly() {
			return null;
		}

		@Property(name="internal")
		public C getInternalGeneric() {
			return null;
		}

		public void setInternalGeneric(C value) {

		}

		@Property(name="aggregation")
		public Set<C> getInternalGenericAggregation() {
			return null;
		}

		public void setInternalGenericAggregation(Set<C> value) {

		}

		@Property(name="abstract")
		public abstract String getAbstract();
		public abstract void setAbstract(String value);

	}

	private TypeManager manager;


	@Before
	public void setUp() throws Exception {
		manager = new TypeManagerImpl(new TransactionalTypeRegistry());
	}

	@Test
	public void scanFields() throws Exception {
		PropertyScanner propertyScanner = new PropertyScanner(AnnotatedClass.class,"http://www.example.org/vocab#");
		try {
			propertyScanner.getProperties(manager);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void scanProperties() throws Exception {
		PropertyScanner propertyScanner = new PropertyScanner(Transaction.class,"http://www.example.org/vocab#");
		propertyScanner.getProperties(manager);
	}

}
