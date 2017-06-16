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
package org.ldp4j.server.utils.spring;


import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.ldp4j.server.utils.spring.DumpableTable.Alignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

final class ConfigurationSummary {

	private static final String CLAZZ_NAME = "class org.springframework.osgi.io.OsgiBundleResource";

	public enum ResourceSource {
		CLASSPATH("classpath"),
		REMOTE("remote"),
		FILE_SYSTEM("file system"),
		STREAM("stream"),
		RAW("raw"),
		OSGI_BUNDLE("osgi bundle"),
		UNKNOWN("<unknown>"),
		;
		private final String tag;

		ResourceSource(String tag) {
			this.tag = tag;
		}

		public String getTag() {
			return this.tag;
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ConfigurationSummary.class);

	private List<Resource> resources;
	private Properties properties;
	private final String title;

	public ConfigurationSummary(String title) {
		this.title = title;
	}

	public void dump(PrintStream out) {
		dumpName(out);
		dumpResources(out);
		dumpTable(out);
	}

	private void dumpName(PrintStream out) {
		DumpableTable d=new DumpableTable(title);
		d.setColumnWidth(0, 111+45+3);
		d.dump(out);
	}

	private void dumpTable(PrintStream out) {
		DumpableTable d=new DumpableTable("CONFIGURATION PROPERTY","DEFINED VALUE");
		for(Entry<Object, Object> entry:properties.entrySet()) {
			d.addRow(entry.getKey().toString(), entry.getValue().toString());
		}
		d.setColumnWidth(0, 45);
		d.setColumnWidth(1, 111);
		d.sort(true);
		d.dump(out);
	}

	private void dumpResources(PrintStream out) {
		DumpableTable d=new DumpableTable("RESOURCE ID","SOURCE","FOUND","LOCATION");
		for(Resource resource:resources) {
			String id = getId(resource);
			String source = getSource(resource);
			String found = resource.exists()?"Yes":"No";
			String uri=null;
			try {
				uri=resource.getURI().toString();
			} catch(IOException e) {
				LOGGER.trace("Could not retrieve URI of resource {}",resource,e);
				uri="---";
			}
			d.addRow(id,source,found,uri);
		}
		d.setColumnWidth(0, 45);
		d.setColumnAlignment(1, Alignment.CENTER);
		d.setColumnWidth(1, 20);
		d.setColumnAlignment(2, Alignment.CENTER);
		d.setColumnWidth(2, 5);
		d.setColumnWidth(3, 80);
		d.dump(out);
	}

	private String getId(Resource resource) {
		ResourceSource source = getResourceSource(resource);
		String id=resource.toString();
		switch(source) {
			case CLASSPATH:
			case FILE_SYSTEM:
			case REMOTE:
				id=id.substring(id.indexOf("[")+1,id.indexOf("]"));
				break;
			case OSGI_BUNDLE:
				id=id.substring(id.indexOf("[")+1,id.indexOf("]"));
				id=id.split("\\|")[0];
				break;
			case RAW:
			case STREAM:
			case UNKNOWN:
				break;
			default:
				throw new AssertionError("Unsupported resource source '"+source+"'");
		}
		return id;
	}

	private String getSource(Resource resource) {
		ResourceSource source = getResourceSource(resource);
		String result=source.getTag();
		if(ResourceSource.OSGI_BUNDLE.equals(source)) {
			String id=resource.toString();
			id=id.substring(id.indexOf("[")+1,id.indexOf("]"));
			String[] parts = id.split("\\|");
			String bundle=parts[1];
			result=result.concat(" (").concat(bundle.split("=")[1]).concat(")");
		}
		return result;
	}

	private ResourceSource getResourceSource(Resource resource) {
		ResourceSource source=null;
		if(resource instanceof ClassPathResource) {
			source=ResourceSource.CLASSPATH;
		} else if(resource instanceof UrlResource) {
			source=ResourceSource.REMOTE;
		} else if(resource instanceof FileSystemResource) {
			source=ResourceSource.FILE_SYSTEM;
		} else if(resource instanceof InputStreamResource) {
			source=ResourceSource.STREAM;
		} else if(resource instanceof ByteArrayResource) {
			source=ResourceSource.RAW;
		} else {
			String type=resource.getClass().toString();
			if(CLAZZ_NAME.equals(type)) {
				source=ResourceSource.OSGI_BUNDLE;
			} else {
				source=ResourceSource.UNKNOWN;
			}
		}
		return source;
	}

	public void setResources(Resource[] resources) {
		this.resources=Arrays.asList(resources);
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}