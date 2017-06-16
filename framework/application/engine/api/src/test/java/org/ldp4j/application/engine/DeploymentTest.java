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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.io.Files;


@RunWith(JMockit.class)
public class DeploymentTest {

	private static final String CONTEXT = "/context";

	private static String dirName(String tmp, String... contexts) {
		StringBuilder builder=new StringBuilder();
		String base=tmp;
		if(base.endsWith(File.separator)) {
			base=tmp.substring(0,tmp.length()-1);
		}
		for(String ctx:contexts) {
			builder.append(File.separator).append(ctx);
		}
		return base+builder.toString();
	}

	@Test
	public void testContextPath$defaultValue() throws Exception {
		assertThat(Deployment.newInstance().contextPath(),equalTo("/"));
	}

	@Test
	public void testContextPath$customValue() throws Exception {
		assertThat(Deployment.newInstance().withContextPath(CONTEXT).contextPath(),equalTo(CONTEXT));
	}

	@Test(expected=NullPointerException.class)
	public void testWithContextPath$nullValue() throws Exception {
		Deployment.newInstance().withContextPath(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWithContextPath$invalidArgument() throws Exception {
		Deployment.newInstance().withContextPath("no slash");
	}

	@Test(expected=NullPointerException.class)
	public void testWithTemporalDirectory$nullValue() {
		Deployment.newInstance().withTemporalDirectory(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWithTemporalDirectory$failIfNoDir() {
		File directory=new File("pom.xml");
		Deployment.newInstance().withTemporalDirectory(directory);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWithTemporalDirectory$failIfNoWritable() {
		new MockUp<File>() {
			@Mock
			boolean canWrite() {
				return false;
			}
		};
		File directory=new File(System.getProperty("java.io.tmpdir"));
		Deployment.newInstance().withTemporalDirectory(directory);
	}

	@Test
	public void testWithTemporalDirectory$maintainsOverride() {
		File directory=new File(System.getProperty("java.io.tmpdir"));
		String ctxConfigProperty = Deployment.contextTemporalDirectoryProperty("context");
		System.setProperty(ctxConfigProperty,"ctx_dir");
		System.setProperty(Deployment.APPLICATION_ENGINE_TMPDIR,"engine_dir");
		try {
			assertThat(Deployment.newInstance().withTemporalDirectory(directory).temporalDirectory(),equalTo(directory));
		} finally {
			System.clearProperty(ctxConfigProperty);
			System.clearProperty(Deployment.APPLICATION_ENGINE_TMPDIR);
		}
	}

	@Test
	public void testTemporalDirectory$ctxConfiguration$happyPath() throws Exception {
		String configProperty = Deployment.contextTemporalDirectoryProperty("context");
		System.setProperty(configProperty,"directory");
		try {
			final AtomicBoolean valid=new AtomicBoolean(false);
			final AtomicInteger tries=new AtomicInteger(0);
			final AtomicBoolean fallback=new AtomicBoolean(false);
			new MockUp<Files>() {
				@Mock
				void createParentDirs(File file) throws IOException {
					tries.incrementAndGet();
					valid.set(file.getParentFile().getPath().equals("directory"));
				}
				@Mock
				File createTempDir() {
					fallback.set(true);
					return null;
				}
			};
			assertThat(Deployment.newInstance().withContextPath(CONTEXT).temporalDirectory(),equalTo(new File("directory")));
			assertThat(valid.get(),equalTo(true));
			assertThat(tries.get(),equalTo(1));
			assertThat(fallback.get(),equalTo(false));
		} finally {
			System.clearProperty(configProperty);
		}
	}

	@Test
	public void testTemporalDirectory$ctxConfiguration$fail() throws Exception {
		String configProperty = Deployment.contextTemporalDirectoryProperty("context");
		System.setProperty(configProperty,"directory");
		try {
			final File file=new File("test");
			final AtomicBoolean valid=new AtomicBoolean(true);
			final AtomicInteger tries=new AtomicInteger(0);
			new MockUp<Files>() {
				@Mock
				void createParentDirs(File file) throws IOException {
					tries.incrementAndGet();
					String path = file.getParentFile().getPath();
					valid.set(
						valid.get() &&
						(path.equals("directory") ||
						 path.startsWith(dirName(System.getProperty("java.io.tmpdir"),"ldp4j","ctx","context"))));
					throw new IOException("failure");
				}
				@Mock
				File createTempDir() {
					return file;
				}
			};
			assertThat(Deployment.newInstance().withContextPath(CONTEXT).temporalDirectory(),sameInstance(file));
			assertThat(valid.get(),equalTo(true));
			assertThat(tries.get(),equalTo(2));
		} finally {
			System.clearProperty(configProperty);
		}
	}

	@Test
	public void testTemporalDirectory$ctxConfiguration$failNoContext() throws Exception {
		String configProperty = Deployment.contextTemporalDirectoryProperty("");
		System.setProperty(configProperty,"directory");
		try {
			final File file=new File("test");
			final AtomicBoolean valid=new AtomicBoolean(true);
			final AtomicInteger tries=new AtomicInteger(0);
			new MockUp<Files>() {
				@Mock
				void createParentDirs(File file) throws IOException {
					tries.incrementAndGet();
					String path = file.getParentFile().getPath();
					valid.set(
						valid.get() &&
						(path.equals("directory") ||
						 path.startsWith(dirName(System.getProperty("java.io.tmpdir"),"ldp4j","ctx"))));
					throw new IOException("failure");
				}
				@Mock
				File createTempDir() {
					return file;
				}
			};
			assertThat(Deployment.newInstance().temporalDirectory(),sameInstance(file));
			assertThat(valid.get(),equalTo(true));
			assertThat(tries.get(),equalTo(2));
		} finally {
			System.clearProperty(configProperty);
		}
	}

	@Test
	public void testTemporalDirectory$engineConfiguration$happyPath() throws Exception {
		String configProperty = Deployment.APPLICATION_ENGINE_TMPDIR;
		System.setProperty(configProperty,"directory");
		try {
			final AtomicBoolean valid=new AtomicBoolean(false);
			final AtomicInteger tries=new AtomicInteger(0);
			final AtomicBoolean fallback=new AtomicBoolean(false);
			final AtomicReference<File> result=new AtomicReference<File>(null);
			new MockUp<Files>() {
				@Mock
				void createParentDirs(File file) throws IOException {
					tries.incrementAndGet();
					File parentFile = file.getParentFile();
					String path = parentFile.getPath();
					valid.set(path.startsWith(dirName("directory","ctx","context")));
					result.set(parentFile);
				}
				@Mock
				File createTempDir() {
					fallback.set(true);
					return null;
				}
			};
			assertThat(Deployment.newInstance().withContextPath(CONTEXT).temporalDirectory(),equalTo(result.get()));
			assertThat(valid.get(),equalTo(true));
			assertThat(tries.get(),equalTo(1));
			assertThat(fallback.get(),equalTo(false));
		} finally {
			System.clearProperty(configProperty);
		}
	}

	@Test
	public void testTemporalDirectory$engineConfiguration$fail() throws Exception {
		String configProperty = Deployment.APPLICATION_ENGINE_TMPDIR;
		System.setProperty(configProperty,"directory");
		try {
			final File file=new File("test");
			final AtomicBoolean valid=new AtomicBoolean(true);
			final AtomicInteger tries=new AtomicInteger(0);
			new MockUp<Files>() {
				@Mock
				void createParentDirs(File file) throws IOException {
					tries.incrementAndGet();
					String path = file.getParentFile().getPath();
					valid.set(
						valid.get() &&
						(path.startsWith(dirName("directory","ctx","context")) ||
						 path.startsWith(dirName(System.getProperty("java.io.tmpdir"),"ldp4j","ctx","context"))));
					throw new IOException("failure");
				}
				@Mock
				File createTempDir() {
					return file;
				}
			};
			assertThat(Deployment.newInstance().withContextPath(CONTEXT).temporalDirectory(),sameInstance(file));
			assertThat(valid.get(),equalTo(true));
			assertThat(tries.get(),equalTo(2));
		} finally {
			System.clearProperty(configProperty);
		}
	}

	@Test
	public void testTemporalDirectory$engineConfiguration$failNoContext() throws Exception {
		String configProperty = Deployment.APPLICATION_ENGINE_TMPDIR;
		System.setProperty(configProperty,"directory");
		try {
			final File file=new File("test");
			final AtomicBoolean valid=new AtomicBoolean(true);
			final AtomicInteger tries=new AtomicInteger(0);
			new MockUp<Files>() {
				@Mock
				void createParentDirs(File file) throws IOException {
					tries.incrementAndGet();
					String path = file.getParentFile().getPath();
					valid.set(
						valid.get() &&
						(path.startsWith(dirName("directory","ctx")) ||
						 path.startsWith(dirName(System.getProperty("java.io.tmpdir"),"ldp4j","ctx"))));
					throw new IOException("failure");
				}
				@Mock
				File createTempDir() {
					return file;
				}
			};
			assertThat(Deployment.newInstance().temporalDirectory(),sameInstance(file));
			assertThat(valid.get(),equalTo(true));
			assertThat(tries.get(),equalTo(2));
		} finally {
			System.clearProperty(configProperty);
		}
	}

	@Test
	public void testTemporalDirectory$vmConfig$happyPath() throws Exception {
		final AtomicBoolean valid=new AtomicBoolean(false);
		final AtomicInteger tries=new AtomicInteger(0);
		final AtomicBoolean fallback=new AtomicBoolean(false);
		final AtomicReference<File> result=new AtomicReference<File>(null);
		new MockUp<Files>() {
			@Mock
			void createParentDirs(File file) throws IOException {
				tries.incrementAndGet();
				File parentFile = file.getParentFile();
				String path = parentFile.getPath();
				valid.set(path.startsWith(dirName(System.getProperty("java.io.tmpdir"),"ldp4j","ctx","context")));
				result.set(parentFile);
			}
			@Mock
			File createTempDir() {
				fallback.set(true);
				return null;
			}
		};
		assertThat(Deployment.newInstance().withContextPath(CONTEXT).temporalDirectory(),equalTo(result.get()));
		assertThat(valid.get(),equalTo(true));
		assertThat(tries.get(),equalTo(1));
		assertThat(fallback.get(),equalTo(false));
	}

	@Test
	public void testTemporalDirectory$vmConfig$fail() throws Exception {
		final File file=new File("test");
		final AtomicBoolean valid=new AtomicBoolean(false);
		final AtomicInteger tries=new AtomicInteger(0);
		new MockUp<Files>() {
			@Mock
			void createParentDirs(File file) throws IOException {
				tries.incrementAndGet();
				String path = file.getParentFile().getPath();
				valid.set(path.startsWith(dirName(System.getProperty("java.io.tmpdir"),"ldp4j","ctx","context")));
				throw new IOException("failure");
			}
			@Mock
			File createTempDir() {
				return file;
			}
		};
		assertThat(Deployment.newInstance().withContextPath(CONTEXT).temporalDirectory(),sameInstance(file));
		assertThat(valid.get(),equalTo(true));
		assertThat(tries.get(),equalTo(1));
	}

	@Test
	public void testTemporalDirectory$vmConfig$failNoContext() throws Exception {
		final File file=new File("test");
		final AtomicBoolean valid=new AtomicBoolean(false);
		final AtomicInteger tries=new AtomicInteger(0);
		new MockUp<Files>() {
			@Mock
			void createParentDirs(File file) throws IOException {
				tries.incrementAndGet();
				String path = file.getParentFile().getPath();
				valid.set(path.startsWith(dirName(System.getProperty("java.io.tmpdir"),"ldp4j","ctx")));
				throw new IOException("failure");
			}
			@Mock
			File createTempDir() {
				return file;
			}
		};
		assertThat(Deployment.newInstance().temporalDirectory(),sameInstance(file));
		assertThat(valid.get(),equalTo(true));
		assertThat(tries.get(),equalTo(1));
	}

}
