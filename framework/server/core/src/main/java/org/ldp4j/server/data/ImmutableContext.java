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
package org.ldp4j.server.data;

import java.net.URI;

import org.ldp4j.rdf.Namespaces;
import org.ldp4j.server.data.spi.Context;


final class ImmutableContext implements Context {

	private URI base;
	private Namespaces namespaces;

	private ImmutableContext(URI base,Namespaces namespaces) {
		this.base = base;
		this.namespaces = namespaces;
	}

	private ImmutableContext(ImmutableContext context) {
		this(context.base,context.namespaces);
	}

	@Override
	public URI getBase() {
		return this.base;
	}

	@Override
	public Namespaces getNamespaces() {
		return new Namespaces(this.namespaces);
	}

	public ImmutableContext setBase(URI base) {
		ImmutableContext copy=new ImmutableContext(this);
		copy.base=base;
		return copy;
	}

	public ImmutableContext setNamespaces(Namespaces namespaces) {
		ImmutableContext copy=new ImmutableContext(this);
		copy.namespaces=new Namespaces(namespaces);
		return copy;
	}

	public static ImmutableContext newInstance(URI base) {
		return new ImmutableContext(base,new Namespaces());
	}

}
