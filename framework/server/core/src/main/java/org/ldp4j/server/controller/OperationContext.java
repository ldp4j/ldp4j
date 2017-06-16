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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.controller;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.engine.context.ContentPreferences;
import org.ldp4j.application.engine.context.CreationPreferences;
import org.ldp4j.application.engine.context.PublicContainer;
import org.ldp4j.application.engine.context.PublicResource;
import org.ldp4j.application.ext.Query;
import org.ldp4j.rdf.Namespaces;

public interface OperationContext {

	URI base();

	String path();

	DataSet dataSet();

	Variant expectedVariant();

	ContentPreferences contentPreferences();

	CreationPreferences creationPreferences();

	OperationContext checkContents();

	OperationContext checkPreconditions();

	OperationContext checkOperationSupport();

	PublicResource resource();

	PublicContainer container();

	URI resolve(PublicResource newResource);

	String serialize(DataSet entity, Namespaces namespaces, MediaType mediaType);

	boolean isResourceQueryable();

	Query getQuery();

	void startOperation();

	void completeOperation();

	Namespaces applicationNamespaces();

	List<Charset> supportedCharsets();

	boolean expectsCharset();

	String acceptedCharset();


}