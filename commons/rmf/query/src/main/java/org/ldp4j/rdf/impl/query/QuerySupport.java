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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-query:0.2.2
 *   Bundle      : rmf-query-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.impl.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.ldp4j.rdf.io.Module;
import org.ldp4j.rdf.query.QueryTemplate;
import org.ldp4j.rdf.sesame.ContentProcessingException;
import org.ldp4j.rdf.sesame.SesameUtils;
import org.ldp4j.rdf.sesame.SesameUtilsException;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class QuerySupport {

	private static final String TEMPLATE_PARAM = "Template cannot be null";

	private static final String COULD_NOT_CLEAR_REPOSITORY = "Could not clear repository";

	private static final String NEXT_CONTEXT_TEMPLATE = "http://www.megatwork.org/query-template-support/%08X/graph-%04X/base-%04X/";

	private static final Logger LOGGER=LoggerFactory.getLogger(QuerySupport.class);

	private final Repository repository;

	private final QueryTemplate template;

	private final Map<String,URI> loadedGraphs;

	private final AtomicLong graphCounter;

	private QuerySupport(Repository tmpRepo, QueryTemplate template) {
		this.repository=tmpRepo;
		this.template = template;
		this.loadedGraphs=new HashMap<String,URI>();
		this.graphCounter=new AtomicLong();
	}

	private boolean isReady() {
		return template.getRequiredNamedGraphs().equals(loadedGraphs.keySet());
	}

	private void logDiscardedFailure(String message, Throwable t) {
		if(LOGGER.isWarnEnabled()) {
			LOGGER.warn(message,t);
		}
	}

	private <T> void loadGraph(String graphName, Module<T> module) throws InvalidContentsException {
		RepositoryConnection connection=getConnection();
		try {
			URI newCtx=null;
			if(graphName!=null) {
				newCtx=nextContext(connection,graphName,module.getBase());
			}
			SesameUtils.loadModule(connection,module,newCtx);
			if(graphName!=null) {
				URI previousCtx=loadedGraphs.put(graphName, newCtx);
				if(previousCtx!=null) {
					connection.clear(previousCtx);
				}
			}
		} catch (ContentProcessingException e) {
			throw new InvalidContentsException("The contents provided could not be loaded",e,graphName,module);
		} catch (RepositoryException e) {
			throw new QueryTemplateSupportFailure(e);
		} catch (SesameUtilsException e) {
			throw new QueryTemplateSupportFailure(e);
		} finally {
			close(connection);
		}
	}

	private URI nextContext(RepositoryConnection connection, String graph, String base) {
		return
			connection.
				getValueFactory().
					createURI(
						String.format(
							NEXT_CONTEXT_TEMPLATE,
							graphCounter.incrementAndGet(),
							graph.hashCode(),
							base.hashCode()));
	}

	<T> void addDefaultGraph(Module<T> module) throws InvalidContentsException  {
		if(!template.requiresNamedDefaultGraph()) {
			loadGraph(null,module);
		} else {
			loadGraph(template.getDefaultGraphName(),module);
		}
	}

	<T> void addNamedGraph(String graphName, Module<T> module) throws InvalidContentsException  {
		if(template.getRequiredNamedGraphs().contains(graphName)) {
			loadGraph(graphName, module);
		}
	}

	boolean clear() {
		boolean result = false;
		RepositoryConnection connection=null;
		try {
			connection=getConnection();
			connection.clear();
			loadedGraphs.clear();
			graphCounter.set(0);
			result=true;
		} catch (QueryTemplateSupportFailure e) {
			logDiscardedFailure(COULD_NOT_CLEAR_REPOSITORY,e);
		} catch (RepositoryException e) {
			logDiscardedFailure(COULD_NOT_CLEAR_REPOSITORY,e);
		} finally {
			close(connection);
		}
		return result;
	}

	boolean dispose() {
		boolean result = true;
		if(repository.isInitialized()) {
			try {
				repository.shutDown();
			} catch (RepositoryException e) {
				logDiscardedFailure("Could not shutdown repository", e);
				result=false;
			}
		}
		return result;
	}

	Map<String,String> getUsedNamedGraphs() {
		if(!isReady()) {
			throw new QueryTemplateSupportFailure("No all the required named graphs have been defined");
		}
		Map<String,String> result=new HashMap<String, String>();
		for(Entry<String,URI> entry:loadedGraphs.entrySet()) {
			result.put(entry.getKey(), entry.getValue().toString());
		}
		return result;
	}

	void close(RepositoryConnection connection) {
		if(connection==null) {
			return;
		}
		try {
			connection.close();
		} catch (RepositoryException e) {
			logDiscardedFailure("Could not close connection", e);
		}
	}

	RepositoryConnection getConnection() {
		if(!repository.isInitialized()) {
			throw new IllegalStateException("Template support has been disposed");
		}
		try {
			return repository.getConnection();
		} catch(RepositoryException e) {
			throw new QueryTemplateSupportFailure("Could not connect to the internal repository", e);
		}
	}

	public static QuerySupport newInstance(QueryTemplate template) {
		Objects.requireNonNull(template, TEMPLATE_PARAM);
		try {
			Repository tmpRepo=new SailRepository(new MemoryStore());
			tmpRepo.initialize();
			return new QuerySupport(tmpRepo,template);
		} catch (RepositoryException e) {
			throw new QueryTemplateSupportFailure("Could not initialize internal Sesame repository",e);
		}
	}

}