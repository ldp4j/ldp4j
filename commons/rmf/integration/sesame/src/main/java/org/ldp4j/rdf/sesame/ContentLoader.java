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
 *   Artifact    : org.ldp4j.commons.rmf:integration-sesame:0.2.2
 *   Bundle      : integration-sesame-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.sesame;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.ldp4j.commons.net.URIUtils;
import org.ldp4j.rdf.io.Source;
import org.ldp4j.rdf.io.SourceVisitor;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

final class ContentLoader implements SourceVisitor<SesameUtilsException> {

	private static final String STORAGE_EXCEPTION = "Could not store data";
	private static final String PARSE_EXCEPTION = "Provided data does not meet specified format";
	private static final String READ_EXCEPTION = "Could not read source data";

	private final RepositoryConnection connection;
	private final String base;
	private final URI context;

	ContentLoader(RepositoryConnection connection, String base, URI context) {
		this.connection = connection;
		this.base = base;
		this.context = context;
	}

	public <S> void process(Source<S> source) throws SesameUtilsException {
		source.accept(this);
	}

	private void loadFrom(URL url) throws SesameUtilsException {
		try {
			if(context!=null) {
				connection.add(url, base, RDFFormat.TURTLE,context);
			} else {
				connection.add(url, base, RDFFormat.TURTLE);
			}
		} catch (IOException e) {
			throw new ContentProcessingException(READ_EXCEPTION, e);
		} catch (RDFParseException e) {
			throw new ContentProcessingException(PARSE_EXCEPTION, e);
		} catch (RepositoryException e) {
			throw new SesameRepositoryFailure(STORAGE_EXCEPTION, e);
		}
	}

	private void loadFrom(java.net.URI uri) throws SesameUtilsException {
		try {
			loadFrom(URIUtils.toURL(uri));
		} catch (MalformedURLException e) {
			throw new ContentProcessingException("Could not get a proper URL for accessing to the source data",e);
		}
	}

	@Override
	public void visitFile(Source<File> source) throws SesameUtilsException  {
		loadFrom(source.getData().toURI());
	}

	@Override
	public void visitURL(Source<URL> source) throws SesameUtilsException {
		loadFrom(source.getData());
	}

	@Override
	public void visitURI(Source<java.net.URI> source) throws SesameUtilsException {
		loadFrom(source.getData());
	}

	@Override
	public void visitString(Source<String> source) throws SesameUtilsException {
		StringReader reader=new StringReader(source.getData());
		try {
			if(context!=null) {
				connection.add(reader, base, RDFFormat.TURTLE,context);
			} else {
				connection.add(reader, base, RDFFormat.TURTLE);
			}
		} catch (IOException e) {
			throw new ContentProcessingException(READ_EXCEPTION, e);
		} catch (RDFParseException e) {
			throw new ContentProcessingException(PARSE_EXCEPTION, e);
		} catch (RepositoryException e) {
			throw new SesameRepositoryFailure(STORAGE_EXCEPTION, e);
		} finally {
			reader.close();
		}
	}
}