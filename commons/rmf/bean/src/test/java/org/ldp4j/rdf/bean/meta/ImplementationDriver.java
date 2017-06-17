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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.Test;
import org.ldp4j.rdf.bean.meta.MetaAnnotation.AnnotationDeclaration;
import org.ldp4j.rdf.bean.meta.MetaProperty.VariableScope;
import org.ldp4j.rdf.bean.meta.TestHarness.CustomAnnotation;
import org.ldp4j.rdf.bean.meta.TestHarness.Example;
import org.ldp4j.rdf.bean.meta.TestHarness.GenericExample;
import org.ldp4j.rdf.bean.meta.TestHarness.GenericGrandParent;
import org.ldp4j.rdf.bean.meta.TestHarness.GenericGreaterGrandParent;
import org.ldp4j.rdf.bean.meta.TestHarness.GenericParent;
import org.ldp4j.rdf.bean.meta.TestHarness.GenericRoot;
import org.ldp4j.rdf.bean.meta.TestHarness.GrandParent;
import org.ldp4j.rdf.bean.meta.TestHarness.GreaterGrandParent;
import org.ldp4j.rdf.bean.meta.TestHarness.Parent;
import org.ldp4j.rdf.bean.meta.TestHarness.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImplementationDriver {

	private static final Logger LOGGER=LoggerFactory.getLogger(ImplementationDriver.class);
	
	@Test
	public void testAssumption$newScanner$non_generic() {
		Class<?>[] classes={Root.class,GreaterGrandParent.class,GrandParent.class,Parent.class,Example.class};
		processClasses(classes);
	}

	private void processClasses(Class<?>[] classes) {
		for(Class<?> clazz:classes) {
			MetaType<?> metaClass=MetaType.forClass(clazz);
			LOGGER.trace("+ {}: ",clazz.getCanonicalName());
			for(String property:metaClass.properties()) {
				MetaProperty<?> metaProperty=metaClass.getProperty(property);
				LOGGER.trace("\t- {}",metaProperty);
			}
		}
	}
	
	@Test
	public void testAssumption$newScanner$generic() {
		Class<?>[] classes={GenericRoot.class,GenericGreaterGrandParent.class,GenericGrandParent.class,GenericParent.class,GenericExample.class};
		processClasses(classes);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testMetaClass$metaAnnotations() {
		GrandParent gp = new GrandParent();
		MetaType<GrandParent> metaClass=MetaType.forObject(gp);

		MetaAnnotation<CustomAnnotation> metaAnnotation = metaClass.getMetaAnnotation(TestHarness.CustomAnnotation.class);
		assertThat(metaAnnotation,notNullValue());

		Collection<AnnotationDeclaration<CustomAnnotation>> declarations = metaAnnotation.getDeclarations();
		assertThat(declarations,notNullValue());
		assertThat(metaAnnotation.get(),notNullValue());
		assertThat(metaAnnotation.getScope(),notNullValue());
		assertThat(declarations.size(),equalTo(3));

		AnnotationDeclaration[] array = declarations.toArray(new AnnotationDeclaration[0]);
		assertThat(metaAnnotation.get(),sameInstance(((AnnotationDeclaration<CustomAnnotation>)array[0]).get()));
		assertThat((Object)metaAnnotation.getScope(),sameInstance((Object)((AnnotationDeclaration<CustomAnnotation>)array[0]).getScope()));
		
		assertThat(((AnnotationDeclaration<CustomAnnotation>)array[0]).get().value(),equalTo(2));
		assertThat(((AnnotationDeclaration<CustomAnnotation>)array[1]).get().value(),equalTo(3));
		assertThat(((AnnotationDeclaration<CustomAnnotation>)array[2]).get().value(),equalTo(4));
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testMetaProperty$metaAnnotations() {
		GrandParent gp = new GrandParent();
		MetaType<GrandParent> metaClass=MetaType.forObject(gp);
		MetaProperty metaProperty=metaClass.getProperty("parent");

		MetaAnnotation<CustomAnnotation> metaAnnotation = metaProperty.getMetaAnnotation(TestHarness.CustomAnnotation.class);
		assertThat(metaAnnotation,notNullValue());
		assertThat(metaAnnotation.get(),notNullValue());
		assertThat(metaAnnotation.getScope(),notNullValue());
		assertThat(metaAnnotation.isOverriden(),equalTo(true));

		Collection<AnnotationDeclaration<CustomAnnotation>> declarations = metaAnnotation.getDeclarations();
		assertThat(declarations,notNullValue());
		assertThat(declarations.size(),equalTo(2));

		AnnotationDeclaration[] array = declarations.toArray(new AnnotationDeclaration[0]);
		assertThat(metaAnnotation.get(),sameInstance(((AnnotationDeclaration<CustomAnnotation>)array[0]).get()));
		assertThat((Object)metaAnnotation.getScope(),sameInstance((Object)((AnnotationDeclaration<CustomAnnotation>)array[0]).getScope()));

		assertThat(((AnnotationDeclaration<CustomAnnotation>)array[0]).get().value(),equalTo(4));
		assertThat(((AnnotationDeclaration<CustomAnnotation>)array[1]).get().value(),equalTo(5));
	}

	@Test
	public void testMetaProperty$genericClass$write() {
		GenericParent<Parent> gp = new GenericParent<Parent>();

		MetaType<GenericParent<Parent>> metaClass=MetaType.forObject(gp);
		MetaProperty<?> metaProperty = verifyProperty(metaClass,"root",VariableScope.TYPE);
		
		Parent root = new Parent();
		GrandParent wrongRoot = new GrandParent();
		gp.setRoot(root);
		assertThat((Object)metaProperty.getRawType(),equalTo((Object)Parent.class));
		assertThat(metaProperty.getValue(gp),sameInstance((Object)root));
		try {
			metaProperty.setValue(gp,wrongRoot);
			fail("Property should not be updated");
		} catch (IllegalArgumentException e) {
			LOGGER.trace("error: "+e.getMessage());
			assertThat(metaProperty.getValue(gp),sameInstance((Object)root));
		}
	}

	@Test
	public void testMetaProperty$genericMethod$write() {
		GenericParent<Parent> gp = new GenericParent<Parent>();

		MetaType<GenericParent<Parent>> metaClass=MetaType.forObject(gp);
		MetaProperty<?> metaProperty = verifyProperty(metaClass, "unknown",VariableScope.METHOD);

		Parent root = new Parent();
		GrandParent wrongRoot = new GrandParent();
		gp.setUnknown(root);
		assertThat((Object)metaProperty.getRawType(),equalTo((Object)Parent.class));
		assertThat(metaProperty.getValue(gp),sameInstance((Object)root));
		try {
			metaProperty.setValue(gp,wrongRoot);
			fail("Property should not be updated");
		} catch (IllegalArgumentException e) {
			LOGGER.trace("error: "+e.getMessage());
			assertThat(metaProperty.getValue(gp),sameInstance((Object)root));
		}
	}

	private MetaProperty<?> verifyProperty(MetaType<?> metaClass, String property, VariableScope scope) {
		MetaProperty<?> metaProperty = metaClass.getProperty(property);
		LOGGER.trace("Property:\n"+metaProperty);
		assertThat(metaProperty.isTypeVariable(),equalTo(true));
		assertThat(metaProperty.getTypeVariableScope(),equalTo(scope));
		return metaProperty;
	}

}
