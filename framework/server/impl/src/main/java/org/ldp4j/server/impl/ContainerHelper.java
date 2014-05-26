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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;
import org.ldp4j.server.LinkedDataPlatformException;
import org.ldp4j.server.core.ILinkedDataPlatformContainer;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ContainerHelper {

	private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
	private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String LDP_NS = "http://www.w3.org/ns/ldp#";

	public static final class DescriptionOptions {
		
		private boolean includeSummary=false;
		private boolean includeMembers=false;
		private Format format;

		private DescriptionOptions() {
		}
		
		public Format getFormat() {
			return format;
		}
		
		public void setFormat(Format format) {
			this.format = format;
		}

		public boolean includeSummary() {
			return includeSummary;
		}

		public boolean includeMembers() {
			return includeMembers;
		}

		private void setIncludeSummary(boolean includeSummary) {
			this.includeSummary = includeSummary;
		}

		private void setIncludeMembers(boolean includeMembers) {
			this.includeMembers = includeMembers;
		}

		@Override
		public String toString() {
			return "DescriptionOptions [includeSummary=" + includeSummary
					+ ", includeMembers=" + includeMembers + ", format="
					+ format + "]";
		}

	}
	
	public static class DescriptionOptionsBuilder {
		
		private DescriptionOptions instance;

		private DescriptionOptionsBuilder() { 
			this.instance=new DescriptionOptions();
		}
		
		public DescriptionOptionsBuilder includeMembers() {
			this.instance.setIncludeMembers(true);
			return this;
		}

		public DescriptionOptionsBuilder excludeMembers() {
			this.instance.setIncludeMembers(false);
			this.instance.setIncludeSummary(false);
			return this;
		}

		public DescriptionOptionsBuilder includeSummary() {
			this.instance.setIncludeMembers(true);
			this.instance.setIncludeSummary(true);
			return this;
		}
		
		public DescriptionOptionsBuilder excludeSummary() {
			this.instance.setIncludeSummary(false);
			return this;
		}

		public DescriptionOptions build() {
			DescriptionOptions result = instance;
			this.instance=new DescriptionOptions();
			return result;
		}

		public DescriptionOptionsBuilder withFormat(Format format) {
			this.instance.setFormat(format);
			return this;
		}
		
	}
	
	private final DescriptionCreator creator;
	
	private final ILinkedDataPlatformContainer container;
	private final URI containerLocation;
	private final Map<String, URI> containerMembers;

	public ContainerHelper(ILinkedDataPlatformContainer container,
			URI containerLocation, Map<String, URI> containerMembers) {
		this.creator=new DescriptionCreator();
		this.container = container;
		this.containerLocation = containerLocation;
		this.containerMembers = containerMembers;
	}


	public static DescriptionOptionsBuilder newBuilder() {
		return new DescriptionOptionsBuilder();
	}
	
	/**
	 * Internal logger.
	 */
	private static final Logger LOGGER=LoggerFactory.getLogger(ContainerHelper.class);

	private class DescriptionCreator {

		private Collection<Statement> getContainerDefinition(Repository repository, URI container, Collection<URI> members) {
			ValueFactory factory=repository.getValueFactory();
			
			List<Statement> statements=new ArrayList<Statement>();
			org.openrdf.model.URI containerResource=factory.createURI(container.toString());
			org.openrdf.model.URI ldpContainer=factory.createURI(LDP_NS, "Container");
			org.openrdf.model.URI ldpMembershipPredicate=factory.createURI(LDP_NS, "membershipPredicate");
			org.openrdf.model.URI rdfsMember=factory.createURI(RDFS_NS, "member");
			org.openrdf.model.URI rdfType=factory.createURI(RDF_NS, "type");
			statements.add(factory.createStatement(containerResource, rdfType, ldpContainer));
			statements.add(factory.createStatement(containerResource, ldpMembershipPredicate, rdfsMember));
		
			for(URI member:members) {
				statements.add(factory.createStatement(containerResource, rdfsMember, factory.createURI(member.toString())));
			}
			return statements;
		}

		private void mergeDescriptions(
				Repository repository, 
				URI base,
				Collection<Statement> description, 
				IContent summary,
				Format format, 
				Writer out) throws IOException {
			RDFFormat sesameFormat = null;
			RDFHandler handler = null;
			switch (format) {
			case RDFXML:
				sesameFormat = RDFFormat.RDFXML;
				handler = new RDFXMLPrettyWriter(out);
				break;
			default: 
				// Is the case of Turtle:
				sesameFormat = RDFFormat.TURTLE;
				handler = new TurtleWriter(out);
				break;
			}
			RepositoryConnection connection = null;
			try {
				connection = repository.getConnection();
				connection.setNamespace("ldp",LDP_NS);
				connection.setNamespace("rdfs",RDFS_NS);
				connection.setNamespace("rdf",RDF_NS);
				connection.add(description);
				if(summary!=null) {
					connection.add(
						summary.serialize(InputStream.class),
						base.toString(), 
						sesameFormat);
				}
				connection.export(handler);
			} catch (RepositoryException e) {
				throw new IOException("Could not handle metadata", e);
			} catch (RDFParseException e) {
				throw new IOException("Could not parse container summary", e);
			} catch (RDFHandlerException e) {
				throw new IOException("Could not generate container representation", e);
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (RepositoryException e) {
						if(LOGGER.isWarnEnabled()) {
							LOGGER.warn("Could not close repository connection. Full stacktrace follows",e);
						}
					}
				}
			}
		}

		public String createContainerRepresentation(Repository repository, DescriptionOptions options) throws LinkedDataPlatformException, IOException {
			Collection<Statement> containerDefinition = 
				getContainerDefinition(
					repository,
					containerLocation,
					containerMembers.values());
			IContent containerSummary=null; 
			if(options.includeSummary()) {
				containerSummary = 
					container.
						getSummary(
							containerMembers.keySet(),
							options.getFormat());
			}
			StringWriter writer = new StringWriter();
			mergeDescriptions(repository, containerLocation, containerDefinition, containerSummary, options.getFormat(), writer);
			return writer.toString();
		}
		
	}
	
	private Repository createRepository() throws IOException {
		try {
			Repository myRepository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
			myRepository.initialize();
			return myRepository;
		} catch (RepositoryException e) {
			throw new IOException("Could not initialize Sesame repository",e);
		}
	}

	public String getRepresentation(DescriptionOptions options) throws LinkedDataPlatformException, IOException {
		Repository repository = createRepository();
		try {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(
					String.format(
						"Preparing container representation for container '%s' when holding resources %s using options %s",
						container.getContainerId(),
						containerMembers,
						options));
			}
			return creator.createContainerRepresentation(repository,options);
		} finally {
			try {
				repository.shutDown();
			} catch (RepositoryException e) {
				if(LOGGER.isWarnEnabled()) {
					LOGGER.warn("Could not shutdown internal repository. Full stacktrace follows",e);
				}
			}
		}
	}

}