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
package org.ldp4j.server.sdk;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.ldp4j.server.IContent;

public class StringContent implements IContent {

	private final Charset charset=Charset.defaultCharset();
	private final String source;

	public StringContent(String source) {
		this.source = source;		
	}
	
	@Override
	public <S> S serialize(Class<S> clazz) throws IOException {
		Object result=null;
		if(clazz.isInstance(source)) {
			result=source;
		} else if(clazz.isAssignableFrom(InputStream.class)) {
			result=IOUtils.toBufferedInputStream(new ByteArrayInputStream(source.getBytes(charset)));
		} else if(clazz.isAssignableFrom(Reader.class)) {
			result=new StringReader(source);
		} else if(clazz.isAssignableFrom(CharSequence.class)) {
			result=CharSequence.class.cast(source);
		} else if(clazz.getClass().isArray() && Byte.TYPE.isAssignableFrom(clazz.getComponentType())) {
			result=(byte[])source.getBytes(charset);
		} else {
			throw new IOException(String.format("Could not serialize String content to '%s'",clazz.getCanonicalName()));
		}
		return clazz.cast(result);
	}

}