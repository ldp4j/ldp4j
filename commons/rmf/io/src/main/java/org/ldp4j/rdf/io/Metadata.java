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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-io:0.2.2
 *   Bundle      : rmf-io-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.io;

import java.nio.charset.Charset;

import org.ldp4j.rdf.Format;

public final class Metadata {

	public static class MetadataBuilder {

		private Charset charset;
		private Format format;

		public MetadataBuilder withCharset(Charset charset) {
			this.charset = charset;
			return this;
		}

		public MetadataBuilder withFormat(Format format) {
			this.format = format;
			return this;
		}

		public Metadata build() {
			return 
				new Metadata(
					format!=null?format:Format.TURTLE,
					charset!=null?charset:Charset.defaultCharset());
		}
	}

	static {
		DEFAULT=new Metadata(Format.TURTLE,Charset.defaultCharset());
	}

	public static final Metadata DEFAULT;
	private final Charset charset;
	private final Format format;

	private Metadata(Format format, Charset charset) {
		this.format = format;
		this.charset = charset;
	}

	public Format getFormat() {
		return format;
	}

	public Charset getCharset() {
		return charset;
	}

	public static MetadataBuilder builder() {
		return new MetadataBuilder();
	}

}