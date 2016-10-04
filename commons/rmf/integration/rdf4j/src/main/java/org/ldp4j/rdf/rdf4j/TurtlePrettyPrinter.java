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
 *   Artifact    : org.ldp4j.commons.rmf:integration-rdf4j:0.2.1
 *   Bundle      : integration-rdf4j-0.2.1.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.rdf4j;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFWriter;
import org.eclipse.rdf4j.rio.helpers.RioSettingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TurtlePrettyPrinter extends AbstractRDFWriter implements RDFWriter {

	public static final RioSetting<Boolean> ENABLE_FOLDING = new RioSettingImpl<Boolean>("org.ldp4j.rdf.rdf4j.turtle.folding", "Enable blank node folding", Boolean.TRUE);

	private static final Logger LOGGER=LoggerFactory.getLogger(TurtlePrettyPrinter.class);

	private static final List<RioSetting<?>> SUPPORTED_SETTINGS;

	private static final AtomicLong COUNTER=new AtomicLong();
	private final long id=COUNTER.incrementAndGet();
	private final PrintWriter out;
	private GraphImpl graph;
	private String logPrefix;

	static {
		List<RioSetting<?>> tmp=new ArrayList<RioSetting<?>>();
		tmp.add(ENABLE_FOLDING);
		SUPPORTED_SETTINGS = Collections.unmodifiableList(tmp);
	}
	private final URI base;

	private WriterConfig writerConfig;

	public TurtlePrettyPrinter(Writer writer) {
		this(null,writer);
	}

	public TurtlePrettyPrinter(URI base, Writer writer) {
		Objects.requireNonNull(writer, "Writer cannot be null");
		this.base = base;
		this.out = new PrintWriter(writer);
		this.logPrefix = String.format("[%d] ",id);
		this.writerConfig=new WriterConfig();
	}

	private void trace(String format, Object... args) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(logPrefix.concat(String.format(format,args)));
		}
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		trace("Started RDF processing...");
		graph=new GraphImpl(base);
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		Resource subject = st.getSubject();
		URI predicate = st.getPredicate();
		Value object = st.getObject();
		trace("Added triple (%s,%s,%s).",subject,predicate,object);
		graph.add(subject, predicate, object);
	}

	@Override
	public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
		graph.addNamespace(prefix, uri);
		trace("Added prefix '%s' for namespace '%s'.",prefix,uri);
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		trace("Discarded comment '%s'.",comment);
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		GraphRenderer renderer=new GraphRenderer(graph,writerConfig.get(ENABLE_FOLDING));
		out.append(renderer.render());
		out.flush();
		trace("Completed RDF processing.");
	}

	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.TURTLE;
	}

	@Override
	public RDFWriter setWriterConfig(WriterConfig config) {
		this.writerConfig = config;
		return this;
	}

	@Override
	public WriterConfig getWriterConfig() {
		return this.writerConfig;
	}

	@Override
	public Collection<RioSetting<?>> getSupportedSettings() {
		return SUPPORTED_SETTINGS;
	}

}