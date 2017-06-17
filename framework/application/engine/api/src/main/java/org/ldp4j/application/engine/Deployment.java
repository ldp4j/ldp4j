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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-api:0.2.2
 *   Bundle      : ldp4j-application-engine-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

final class Deployment {

	private abstract static class TemporalDirectoryFactory {

		private final boolean compose;
		private final String subdirPrefix;

		private TemporalDirectoryFactory(boolean compose,String subdirPrefix) {
			this.compose = compose;
			this.subdirPrefix = subdirPrefix;
		}

		private String temporalDirectoryPath(String context) {
			String result=null;
			String tmpdir = System.getProperty(configurationProperty(context));
			if(tmpdir!=null) {
				if(this.compose) {
					result=compose(context, tmpdir);
				} else{
					result=tmpdir;
				}
			}
			return result;
		}

		private String compose(String context, String tmpdir) {
			return
				tmpdir+
				File.separator+
				this.subdirPrefix+
				File.separator+
				(!context.isEmpty()?
					context:
					Long.toHexString(System.currentTimeMillis()))+
				File.separator;
		}

		abstract String configurationProperty(String context);

		abstract String configurationDescription(String tmpdir);

		final File create(String context) {
			File result=null;
			String tmpdir = temporalDirectoryPath(context);
			if(tmpdir!=null) {
				try {
					result=new File(tmpdir);
					Files.createParentDirs(new File(result,"engine.tmp"));
				} catch (IOException e) {
					LOGGER.warn("Could not create {}. Full stacktrace follows",configurationDescription(tmpdir),e);
					result=null;
				}
			}
			return result;
		}

	}

	private static  final class ContextTemporalDirectoryFactory extends TemporalDirectoryFactory {

		private ContextTemporalDirectoryFactory() {
			super(false,null);
		}

		@Override
		String configurationProperty(String context) {
			return contextTemporalDirectoryProperty(context);
		}

		@Override
		String configurationDescription(String tmpdir) {
			return "user specified context temporal directory "+tmpdir;
		}

	}

	private static final class EngineTemporalDirectoryFactory extends TemporalDirectoryFactory {

		private EngineTemporalDirectoryFactory() {
			super(true,"ctx");
		}

		@Override
		String configurationProperty(String context) {
			return APPLICATION_ENGINE_TMPDIR;
		}

		@Override
		String configurationDescription(String tmpdir) {
			return "temporal directory "+tmpdir+" within user specified Application Engine temporal directory";
		}

	}

	private static final class VMTemporalDirectoryFactory extends TemporalDirectoryFactory {

		private VMTemporalDirectoryFactory() {
			super(true,"ldp4j"+File.separator+"ctx");
		}

		@Override
		String configurationProperty(String context) {
			return "java.io.tmpdir";
		}

		@Override
		String configurationDescription(String tmpdir) {
			return "temporal directory "+tmpdir+" within the VM temporal directory";
		}

	}

	private static ImmutableList<TemporalDirectoryFactory> FACTORIES=
			ImmutableList.
				<TemporalDirectoryFactory>builder().
					add(new ContextTemporalDirectoryFactory()).
					add(new EngineTemporalDirectoryFactory()).
					add(new VMTemporalDirectoryFactory()).
					build();

	/**
	 * Visible for testing
	 */
	static final String APPLICATION_ENGINE_TMPDIR = "org.ldp4j.application.engine.tmpdir";

	private static final Logger LOGGER=LoggerFactory.getLogger(Deployment.class);

	private String contextPath;
	private File temporalDirectory;

	private Deployment(String contextPath, File temporalDirectory) {
		this.contextPath=contextPath;
		this.temporalDirectory=temporalDirectory;
	}

	Deployment withContextPath(String contextPath) {
		checkNotNull(contextPath,"Context path cannot be null");
		checkArgument(contextPath.startsWith("/"),"Context path should start with '/' (%s)",contextPath);
		this.contextPath=contextPath;
		return this;
	}

	Deployment withTemporalDirectory(File temporalDirectory) {
		checkNotNull(temporalDirectory,"Temporal directory cannot be null");
		checkArgument(temporalDirectory.isDirectory(),"Path %s cannot be used as a temporal directory (it is not a directory)",temporalDirectory.getAbsolutePath());
		checkArgument(temporalDirectory.canWrite(),"Path %s cannot be used as a temporal directory (cannot be written)",temporalDirectory.getAbsolutePath());
		this.temporalDirectory=temporalDirectory;
		return this;
	}

	String contextPath() {
		return this.contextPath;
	}

	File temporalDirectory() {
		if(this.temporalDirectory==null) {
			String ctx=this.contextPath.substring(1);
			File result=null;
			for(TemporalDirectoryFactory factory:FACTORIES) {
				result=factory.create(ctx);
				if(result!=null) {
					break;
				}
			}
			if(result==null) {
				result=Files.createTempDir();
			}
			this.temporalDirectory=result;
		}
		return this.temporalDirectory;

	}

	static Deployment newInstance() {
		return new Deployment("/",null);
	}

	/**
	 * Visible for testing
	 */
	static String contextTemporalDirectoryProperty(String ctx) {
		return "org.ldp4j.application.engine."+ctx+".tmpdir";
	}

}
