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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class Index<S,ID> {

	private static final Logger LOGGER=LoggerFactory.getLogger(Index.class);

	public interface FacetResolver<T,S> {

		String name();

		T get(S entity);

	}

	public interface EntityResolver<T,ID> {

		ID id(T entity);

		T resolve(ID id);

	}

	private final class Indexation {

		private final ID entityId;
		private final Facet facetId;
		private final Object value;

		private Indexation(final ID entityId, final Index.Facet facetId, final Object value) {
			this.entityId = entityId;
			this.facetId = facetId;
			this.value = value;
		}

		private void removeFromIndex() {
			Map<Object, List<ID>> facetIndex = Index.this.index.get(this.facetId);
			if(facetIndex!=null) {
				List<ID> entityIds = facetIndex.get(this.value);
				if(entityIds!=null) {
					entityIds.remove(this.entityId);
					if(entityIds.isEmpty()) {
						facetIndex.remove(this.value);
					}
				}
			}
		}

		private void insertIntoIndex() {
			Map<Object,List<ID>> facetIndex = getFacetIndex();
			List<ID> indexedInstances = getValueIndexedInstances(facetIndex);
			indexedInstances.add(this.entityId);
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(
					"Indexed entity identified by '{}' in facet '{}' with value '{}'",
					this.entityId,
					this.facetId.description(),
					this.value);
			}
		}

		private List<ID> getValueIndexedInstances(Map<Object,List<ID>> facetIndex) {
			List<ID> indexedInstances=facetIndex.get(this.value);
			if(indexedInstances==null) {
				indexedInstances=Lists.newArrayList();
				facetIndex.put(this.value,indexedInstances);
			}
			return indexedInstances;
		}

		private Map<Object,List<ID>> getFacetIndex() {
			Map<Object,List<ID>> facetIndex = Index.this.index.get(this.facetId);
			if(facetIndex==null) {
				facetIndex=Maps.newLinkedHashMap();
				Index.this.index.put(this.facetId, facetIndex);
			}
			return facetIndex;
		}

	}

	public static final class Facet {

		private final long indexId;
		private final long id;
		private final String name;

		private Facet(long indexId,long id,String name) {
			this.indexId = indexId;
			this.id = id;
			this.name = name;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.indexId,this.id,this.name);
		}

		@Override
		public boolean equals(Object other) {
			boolean result=false;
			if(Index.Facet.class.isInstance(other)) {
				Index.Facet that=(Index.Facet)other;
				result=this.id==that.id && this.indexId==that.indexId && this.name.equals(that.name);
			}
			return result;
		}

		private String description() {
			return String.format("%s {%d:%d}",this.name,this.indexId,this.id);
		}

		@Override
		public String toString() {
			return
				Objects.
					toStringHelper(getClass()).
						add("indexId",this.indexId).
						add("id",this.id).
						add("name",this.name).
						toString();
		}

	}

	private static final AtomicLong INDEX_COUNTER=new AtomicLong();

	private final long indexId;
	private final Map<Facet,Map<Object,List<ID>>> index;
	private final Map<Facet,FacetResolver<?,S>> facets;
	private final Map<ID,List<Indexation>> indexations;
	private final AtomicLong counter;
	private final EntityResolver<S,ID> resolver;

	private Index(Index.EntityResolver<S,ID> resolver) {
		this.indexId=INDEX_COUNTER.incrementAndGet();
		this.resolver = resolver;
		this.index=Maps.newLinkedHashMap();
		this.facets=Maps.newLinkedHashMap();
		this.indexations=Maps.newLinkedHashMap();
		this.counter=new AtomicLong();
	}

	private void removeFacetIndexations(final Facet facetId) {
		Map<Object,List<ID>> facetIndex = this.index.get(facetId);
		if(facetIndex==null) {
			return;
		}
		for(Entry<Object,List<ID>> entry:facetIndex.entrySet()) {
			for(ID entityId:entry.getValue()) {
				Indexation indexation = new Indexation(entityId,facetId, entry.getKey());
				removeIndexation(entityId, indexation);
			}
		}
	}

	private void removeIndexation(final Object individualId, final Indexation indexation) {
		List<Indexation> individualIndexations = this.indexations.get(individualId);
		if(individualIndexations!=null) {
			individualIndexations.remove(indexation);
			if(individualIndexations.isEmpty()) {
				this.indexations.remove(individualId);
			}
		}
	}

	private boolean removeEntityIndexations(ID entityId) {
		List<Indexation> currentIndexations=this.indexations.get(entityId);
		boolean removed = currentIndexations!=null;
		if(removed) {
			for(Indexation indexation:currentIndexations) {
				indexation.removeFromIndex();
			}
			this.indexations.remove(entityId);
		}
		return removed;
	}

	private List<Indexation> freshEntityIndexations(ID id) {
		List<Indexation> entityIndexations=this.indexations.get(id);
		if(entityIndexations==null) {
			entityIndexations=Lists.newArrayList();
			this.indexations.put(id,entityIndexations);
		} else {
			for(Indexation indexation:entityIndexations) {
				indexation.removeFromIndex();
			}
			entityIndexations.clear();
		}
		return entityIndexations;
	}

	public Facet registerFacet(final FacetResolver<?,S> facet) {
		checkNotNull(facet,"Facet resolver cannot be null");
		Facet facetId=new Facet(this.indexId,this.counter.incrementAndGet(),facet.name());
		this.facets.put(facetId, facet);
		this.index.put(facetId, Maps.<Object,List<ID>>newLinkedHashMap());
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Registered facet '{}' ({})",facetId.description(),facet.getClass().getName());
		}
		return facetId;
	}

	public void deregisterFacet(final Facet facetId) {
		checkNotNull(facetId,"Facet cannot be null");
		FacetResolver<?,?> facet=this.facets.get(facetId);
		if(facet!=null) {
			this.facets.remove(facetId);
			removeFacetIndexations(facetId);
			this.index.remove(facetId);
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Deregistered facet '{}' ({})",facetId.description(),facet.getClass().getName());
			}
		}
	}

	public void index(final S entity) {
		checkNotNull(entity,"Entity cannot be null");
		ID entityId = checkNotNull(this.resolver.id(entity),"Unknown entity");
		List<Indexation> currentIndexations = freshEntityIndexations(entityId);
		for(Entry<Facet,FacetResolver<?,S>> entry:this.facets.entrySet()) {
			Indexation indexation=new Indexation(entityId,entry.getKey(),entry.getValue().get(entity));
			indexation.insertIntoIndex();
			currentIndexations.add(indexation);
		}
	}

	public <T> List<S> findByFacetValue(final Facet facetId, final T value) {
		checkNotNull(facetId,"Facet cannot be null");
		checkNotNull(value,"Facet value cannot be null");
		Map<?,List<ID>> facetIndex = checkNotNull(this.index.get(facetId),"Unknown facet");
		Builder<S> builder = ImmutableList.<S>builder();
		List<ID> elements = facetIndex.get(value);
		List<ID> missingEntities=Lists.newArrayList();
		if(elements!=null) {
			for(ID id:elements) {
				S entity = this.resolver.resolve(id);
				if(entity!=null) {
					builder.add(entity);
				} else {
					missingEntities.add(id);
				}
			}
			if(!missingEntities.isEmpty()) {
				for(ID id:missingEntities) {
					removeEntityIndexations(id);
				}
			}
		}
		if(LOGGER.isDebugEnabled()) {
			if(elements==null) {
				LOGGER.debug("No entities indexed for value '{}' of facet '{}'",value,facetId.description());
			} else {
				LOGGER.debug(
					"{} entit{} found for value '{}' of facet '{}' index ('{}')",
					elements.size(),
					elements.size()==1?"y":"ies",
					value,
					facetId.description(),
					Joiner.on("', '").join(elements));
				if(!missingEntities.isEmpty()) {
					LOGGER.debug(
						"{} stale entit{} removed from index ('{}')",
						missingEntities.size(),
						missingEntities.size()==1?"y":"ies",
						Joiner.on("', '").join(missingEntities));
				}
			}
		}
		return builder.build();
	}

	public void remove(final S entity) {
		checkNotNull(entity,"Entity cannot be null");
		ID entityId=checkNotNull(this.resolver.id(entity),"Unknown entity");
		boolean removed=removeEntityIndexations(entityId);
		if(LOGGER.isDebugEnabled() && removed) {
			LOGGER.debug("Removed entity identified by '{}'",entityId);
		}
	}

	public static <S,ID> Index<S,ID> create(EntityResolver<S,ID> resolver) {
		checkNotNull(resolver,"Entity resolver cannot be null");
		return new Index<S,ID>(resolver);
	}

	public void clear() {
		this.index.clear();
		this.indexations.clear();
	}

}