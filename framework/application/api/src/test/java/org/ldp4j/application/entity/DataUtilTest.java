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
package org.ldp4j.application.entity;

import java.net.URI;

import org.junit.Test;
import org.ldp4j.application.domain.RDFS;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DataUtilTest {

	@Test
	public void testPrettyPrint() {
		DefaultIdentityFactory idFactory=DefaultIdentityFactory.create();
		DataSource dataSource=CompositeDataSource.create();

		System.out.println("Initial dataset: ");
		System.out.println(DataUtil.prettyPrint(dataSource));

		URI ind1 = URI.create("http://www.ldp4j.org/ind1/");
		Entity ent1 = dataSource.newEntity(idFactory.createExternalIdentity(ind1));
		ent1.addProperty(RDFS.LABEL.as(URI.class), Literal.create("Individual 1 label"));
		ent1.addProperty(RDFS.COMMENT.as(URI.class), Literal.create("Comment 1"));

		Entity ent2 = dataSource.newEntity(idFactory.createManagedIdentity(Key.create(CompositeDataSource.class,123)));
		ent2.addProperty(RDFS.LABEL.as(URI.class), Literal.create("Individual 2 label"));
		ent2.addProperty(RDFS.COMMENT.as(URI.class), Literal.create("Comment 2"));

		Entity ent3 = dataSource.newEntity(idFactory.createIdentity());
		ent3.addProperty(RDFS.LABEL.as(URI.class), Literal.create("Individual 3 label"));
		ent3.addProperty(RDFS.COMMENT.as(URI.class), Literal.create("Comment 3"));

		URI linkedTo = URI.create("http://www.example.org/vocab#linkedTo");
		ent2.addProperty(linkedTo, ent3);
		ent1.addProperty(linkedTo, ent2);

		System.out.println("After population: ");
		System.out.println(DataUtil.prettyPrint(dataSource));

		Property property = ent2.getProperty(linkedTo);

		Value value = property.iterator().next();
		assertThat(value,is(instanceOf(Entity.class)));
		Entity sEnt3 = (Entity)value;
		assertThat(sEnt3.identity(),equalTo(ent3.identity()));
		assertThat(sEnt3.id(),equalTo(ent3.id()));



		System.out.println("After updating: "+DataUtil.prettyPrint(ent3.identity()));
		ent3.addProperty(linkedTo, ent1);
		sEnt3.addProperty(linkedTo, ent2);
		System.out.println(DataUtil.prettyPrint(dataSource));

		System.out.println("After removing: "+DataUtil.prettyPrint(ent2.identity()));
		dataSource.remove(ent2);
		System.out.println(DataUtil.prettyPrint(dataSource));

		System.out.println("After merging: "+DataUtil.prettyPrint(ent2.identity()));
		dataSource.merge(ent2);
		System.out.println(DataUtil.prettyPrint(dataSource));
	}

}
