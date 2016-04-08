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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.util;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.application.util.Index.EntityResolver;
import org.ldp4j.application.util.Index.FacetResolver;
import org.ldp4j.application.util.Index.Facet;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;


public class IndexTest {

	private static final String STRING_VALUE2 = "New string";
	private static final URI URI_VALUE1 = URI.create("");
	private static final String STRING_VALUE1 = "String";

	private static final class StringFacet implements FacetResolver<String, ExampleEntity> {
		@Override
		public String get(ExampleEntity entity) {
			return entity.getStringFacet();
		}
		@Override
		public String name() {
			return "String facet";
		}
	}

	private static final class URIFacet implements FacetResolver<URI, ExampleEntity> {
		@Override
		public URI get(ExampleEntity entity) {
			return entity.getUriFacet();
		}
		@Override
		public String name() {
			return "URI facet";
		}
	}

	private static final class ExampleEntity {

		private final long id;
		private String stringFacet;
		private URI uriFacet;

		private ExampleEntity(long id, String stringFacet, URI uriFacet) {
			this.id=id;
			setStringFacet(stringFacet);
			setUriFacet(uriFacet);
		}

		public long id() {
			return this.id;
		}

		public String getStringFacet() {
			return stringFacet;
		}

		private void setStringFacet(String stringFacet) {
			this.stringFacet = stringFacet;
		}

		public URI getUriFacet() {
			return uriFacet;
		}

		private void setUriFacet(URI uriFacet) {
			this.uriFacet = uriFacet;
		}

		@Override
		public int hashCode() {
			return
				Objects.hashCode(this.id,this.stringFacet,this.uriFacet);
		}

		@Override
		public boolean equals(Object obj) {
			boolean result=obj==this;
			if(!result && obj instanceof ExampleEntity) {
				ExampleEntity that=(ExampleEntity)obj;
				result=
					Objects.equal(this.id,that.id) &&
					Objects.equal(this.stringFacet,that.stringFacet) &&
					Objects.equal(this.uriFacet,that.uriFacet);
			}
			return result;
		}

		@Override
		public String toString() {
			return
				Objects.
					toStringHelper(getClass()).
						add("id", this.id).
						add("stringFacet",this.stringFacet).
						add("uriFacet",this.uriFacet).
						toString();
		}

	}

	private static final class ExampleEntityStore {

		private final Map<Long,ExampleEntity> entities;
		private final AtomicLong entityCounter=new AtomicLong();

		private ExampleEntityStore() {
			this.entities=Maps.newLinkedHashMap();
		}

		public ExampleEntity addEntity(String stringFacet, URI uriFacet) {
			ExampleEntity entity = new ExampleEntity(entityCounter.incrementAndGet(),stringFacet, uriFacet);
			this.entities.put(entity.id(),entity);
			return entity;
		}

		public void removeEntity(ExampleEntity entity) {
			this.entities.remove(entity.id());
		}

		public EntityResolver<ExampleEntity,Long> resolver() {
			return
				new EntityResolver<ExampleEntity, Long>() {
					@Override
					public Long id(ExampleEntity individual) {
						return individual.id();
					}
					@Override
					public ExampleEntity resolve(Long id) {
						return entities.get(id);
					}
				};
		}

	}

	private ExampleEntityStore store;
	private Index<ExampleEntity,Long> index;
	private Facet stringFacet;
	private Facet uriFacet;

	@Before
	public void setUp() {
		this.store=new ExampleEntityStore();
	}


	private void initialize() {
		this.index=Index.create(this.store.resolver());
		this.stringFacet=registerFacet(new StringFacet(),this.index);
		this.uriFacet=registerFacet(new URIFacet(),this.index);
	}

	private ExampleEntity storeEntity(String strinfFacet, URI uriFacet) {
		return this.store.addEntity(strinfFacet, uriFacet);
	}

	private void removeEntity(ExampleEntity entity) {
		this.store.removeEntity(entity);
	}

	private void indexEntity(ExampleEntity entity) {
		this.index.index(entity);
	}

	private List<ExampleEntity> findByString(String value) {
		return this.index.findByFacetValue(stringFacet(), value);
	}

	private List<ExampleEntity> findByURI(URI value) {
		return this.index.findByFacetValue(uriFacet(), value);
	}


	private <V> Facet registerFacet(FacetResolver<V,ExampleEntity> facet, Index<ExampleEntity, Long> index) {
		Facet facetId = index.registerFacet(facet);
		assertThat(facetId,notNullValue());
		return facetId;
	}

	private void tryFind(Facet facetId) {
		try {
			this.index.findByFacetValue(facetId,new Object());
			fail("Should not try to resolve already deregistered facets ("+facetId+")");
		} catch(NullPointerException e) {
			assertThat(e.getMessage(),equalTo("Unknown facet"));
		}
	}

	private Facet uriFacet() {
		return this.uriFacet;
	}


	private Facet stringFacet() {
		return this.stringFacet;
	}


	private void deregisterFacet(Facet facetId) {
		this.index.deregisterFacet(facetId);
	}


	public Index<ExampleEntity, Long> createIndex() throws Exception {
		Index<ExampleEntity, Long> index = Index.create(store.resolver());
		assertThat(index,notNullValue());
		return index;
	}

	@Test(expected=NullPointerException.class)
		public void testFindByFacetValue$null() throws Exception {
			initialize();
			findByString(null);
		}


	@Test(expected=NullPointerException.class)
	public void testRemove$null() throws Exception {
		initialize();
		unindex(null);
	}


	@Test(expected=NullPointerException.class)
	public void testRegisterFacet$null() throws Exception {
		createIndex().registerFacet(null);
	}

	@Test(expected=NullPointerException.class)
	public void testDeregisterFacet$null() throws Exception {
		createIndex().deregisterFacet(null);
	}

	@Test
	public void testDeregisterFacet() throws Exception {
		initialize();
		deregisterFacet(stringFacet());
		tryFind(stringFacet());
		deregisterFacet(uriFacet());
		tryFind(uriFacet());
	}


	@Test(expected=NullPointerException.class)
	public void testIndex$null() throws Exception {
		initialize();
		index.index(null);
	}

	@Test
	public void testIndexUsage$fromScratch() throws Exception {
		initialize();
		ExampleEntity entity = storeEntity(STRING_VALUE1,URI_VALUE1);
		indexEntity(entity);
		List<ExampleEntity> search = findByString(STRING_VALUE1);
		assertThat(search,notNullValue());
		assertThat(search,hasItem(entity));
	}

	@Test
	public void testIndexUsage$indexationsMayBeStale() throws Exception {
		initialize();
		ExampleEntity entity = storeEntity(STRING_VALUE1,URI_VALUE1);
		indexEntity(entity);
		entity.setStringFacet(STRING_VALUE2);
		List<ExampleEntity> newSearch=findByString(STRING_VALUE1);
		assertThat(newSearch,notNullValue());
		assertThat(newSearch,hasItem(entity));
		List<ExampleEntity> reindexSearch=findByString(STRING_VALUE2);
		assertThat(reindexSearch,notNullValue());
		assertThat(reindexSearch,hasSize(0));
	}

	@Test
	public void testIndexUsage$reindexationRefreshesIndex() throws Exception {
		initialize();
		ExampleEntity entity = storeEntity(STRING_VALUE1,URI_VALUE1);
		indexEntity(entity);
		entity.setStringFacet(STRING_VALUE2);
		indexEntity(entity);
		List<ExampleEntity> search=findByString(STRING_VALUE1);
		assertThat(search,notNullValue());
		assertThat(search,hasSize(0));
		List<ExampleEntity> reindexSearch=findByString(STRING_VALUE2);
		assertThat(reindexSearch,notNullValue());
		assertThat(reindexSearch,hasItem(entity));
	}

	@Test
	public void testIndexUsage$indexAllowsMultipleEntitiesPerFacetValue() throws Exception {
		initialize();
		ExampleEntity entity1 = storeEntity(STRING_VALUE1,URI_VALUE1);
		ExampleEntity entity2 = storeEntity(STRING_VALUE1,URI_VALUE1);
		indexEntity(entity1);
		indexEntity(entity2);
		List<ExampleEntity> search=findByString(STRING_VALUE1);
		assertThat(search,hasItem(entity1));
		assertThat(search,hasItem(entity2));
	}

	@Test
	public void testIndexUsage$removingMissingEntitiesDoesNotAffectIndexedEntities() throws Exception {
		initialize();
		ExampleEntity entity1=storeEntity(STRING_VALUE1,URI_VALUE1);
		ExampleEntity entity2=storeEntity(STRING_VALUE1,URI_VALUE1);
		indexEntity(entity1);
		assertThat(findByString(STRING_VALUE1),hasItem(entity1));
		assertThat(findByURI(URI_VALUE1),hasItem(entity1));
		unindex(entity2);
		assertThat(findByString(STRING_VALUE1),hasItem(entity1));
		assertThat(findByURI(URI_VALUE1),hasItem(entity1));
	}

	@Test
	public void testIndexUsage$removingExistingEntitiesDoesNotAffectSimilarlyIndexedEntities() throws Exception {
		initialize();
		ExampleEntity entity1=storeEntity(STRING_VALUE1,URI_VALUE1);
		ExampleEntity entity2=storeEntity(STRING_VALUE1,URI_VALUE1);
		indexEntity(entity1);
		indexEntity(entity2);
		assertThat(findByString(STRING_VALUE1),hasItems(entity1,entity2));
		assertThat(findByURI(URI_VALUE1),hasItems(entity1,entity2));
		unindex(entity1);
		assertThat(findByString(STRING_VALUE1),hasItems(entity2));
		assertThat(findByURI(URI_VALUE1),hasItems(entity2));
	}

	@Test
	public void testIndexUsage$noNullEntitesAreReturned() throws Exception {
		initialize();
		ExampleEntity entity1=storeEntity(STRING_VALUE1,URI_VALUE1);
		ExampleEntity entity2=storeEntity(STRING_VALUE1,URI_VALUE1);
		indexEntity(entity1);
		indexEntity(entity2);
		assertThat(findByString(STRING_VALUE1),hasItems(entity1,entity2));
		assertThat(findByURI(URI_VALUE1),hasItems(entity1,entity2));
		removeEntity(entity1);
		assertThat(findByString(STRING_VALUE1),hasItems(entity2));
		assertThat(findByURI(URI_VALUE1),hasItems(entity2));
	}

	private void unindex(ExampleEntity entity) {
		this.index.remove(entity);
	}

	@Test(expected=NullPointerException.class)
	public void testCreate$null() throws Exception {
		Index.create(null);
	}

}
