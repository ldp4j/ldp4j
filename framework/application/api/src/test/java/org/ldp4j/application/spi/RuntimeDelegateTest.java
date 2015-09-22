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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.spi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.ReflectPermission;
import java.net.URI;
import java.security.Permission;
import java.util.List;
import java.util.Properties;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

@RunWith(JMockit.class)
public class RuntimeDelegateTest {

	@Rule
	public TemporaryFolder tmp=new TemporaryFolder(new File("target/"));

	@Mocked URI canonicalBase;
	@Mocked URI endpoint;
	@Mocked WriteSession session;
	@Mocked ResourceSnapshot snapshot;

	private static PrintStream err;

	private RuntimeDelegate sut() {
		RuntimeDelegate instance = RuntimeDelegate.getInstance();
		assertThat(instance,notNullValue());
		return instance;
	}

	@SuppressWarnings("rawtypes")
	private void verifyIsDefault(RuntimeDelegate sut) {
		assertThat((Class)sut.getClass().getEnclosingClass(),equalTo((Class)RuntimeDelegate.class));
		assertThat(sut.getClass().getSimpleName(),equalTo("DefaultRuntimeDelegate"));
	}

	private File spiFile() {
		File target = new File("target/test-classes/META-INF/services/"+RuntimeDelegate.class.getName());
		target.getParentFile().mkdirs();
		return target;
	}

	private boolean hasReflectionPermission(List<Permission> perms) {
		boolean found=false;
		for(Permission p:perms) {
			if(p instanceof ReflectPermission) {
				found=true;
			}
		}
		return found;
	}

	private void cleanFile(File target) {
		if(target.exists()) {
			if(!target.delete()) {
				target.deleteOnExit();
			}
		}
	}

	private File setUpExtension(String className) throws Exception {
		Properties properties=new Properties();
		properties.setProperty(RuntimeDelegate.APPLICATION_ENGINE_SPI_PROPERTY, className);
		return setUpExtension(properties);
	}

	private File setUpExtension(Properties properties) throws Exception {
		File newFile = tmp.newFile();
		try(FileOutputStream out = new FileOutputStream(newFile)) {
			properties.store(out,"");
		}
		return newFile;
	}

	@BeforeClass
	public static void setUpBefore() {
		err = System.err;
		PrintStream customErr = new PrintStream(new ByteArrayOutputStream());
		System.setErr(customErr);
	}

	@AfterClass
	public static void tearDownAfter() {
		System.setErr(err);
	}

	@Before
	public void setUp() {
		System.clearProperty(RuntimeDelegate.APPLICATION_ENGINE_SPI_PROPERTY);
		System.setProperty(RuntimeDelegate.APPLICATION_ENGINE_SPI_FINDER,"disable");
		RuntimeDelegate.setInstance(null);
	}

	@After
	public void tearDown() {
		cleanFile(spiFile());
		System.clearProperty(RuntimeDelegate.APPLICATION_ENGINE_SPI_PROPERTY);
	}

	@Test
	public void testGetInstance$spi$enabled() throws Exception {
		System.clearProperty(RuntimeDelegate.APPLICATION_ENGINE_SPI_FINDER);
		Files.copy(new File("src/test/resources/spi.delegates.cfg"),spiFile());
		assertThat(sut(),instanceOf(CustomRuntimeDelegate.class));
	}

	@Test
	public void testGetInstance$cacheWorks() throws Exception {
		RuntimeDelegate sut1 = sut();
		RuntimeDelegate sut2 = sut();
		assertThat(sut2,sameInstance(sut1));
	}

	@Test
	public void testGetInstance$spi$disabled() throws Exception {
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$extension$notReadable() throws Exception {
		final File file=setUpExtension("NotFound");
		new MockUp<RuntimeDelegate>() {
			@Mock
			File getConfigurationFile() {
				return file;
			}
		};
		new MockUp<File>() {
			@Mock
			public boolean canRead() {
				return false;
			}
		};
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$extension$ioException$notFound() throws Exception {
		final File file=new File("Not exists");
		new MockUp<RuntimeDelegate>() {
			@Mock
			File getConfigurationFile() {
				return file;
			}
		};
		new MockUp<File>() {
			@Mock
			public boolean canRead() {
				return true;
			}
		};
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$extension$ioException$readFailure() throws Exception {
		final File file=setUpExtension("IOERROR");
		new MockUp<RuntimeDelegate>() {
			@Mock
			File getConfigurationFile() {
				return file;
			}
		};
		new MockUp<File>() {
			@Mock
			public boolean canRead() {
				return true;
			}
		};
		new MockUp<FileInputStream>() {
			@Mock
			public int read() throws IOException {
				throw new IOException("FAILURE");
			}
			@Mock
			public int read(byte[] bytes) throws IOException {
				throw new IOException("FAILURE");
			}
		};
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$extension$ioException$closeFailure() throws Exception {
		final File file=setUpExtension("IOERROR");
		new MockUp<RuntimeDelegate>() {
			@Mock
			File getConfigurationFile() {
				return file;
			}
		};
		new MockUp<File>() {
			@Mock
			public boolean canRead() {
				return true;
			}
		};
		new MockUp<FileInputStream>() {
			@Mock
			public void close() throws IOException {
				throw new IOException("FAILURE");
			}
		};
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$extension$ioException$runtimeFailure() throws Exception {
		final File file=setUpExtension("IOERROR");
		new MockUp<RuntimeDelegate>() {
			@Mock
			File getConfigurationFile() {
				return file;
			}
		};
		new MockUp<File>() {
			@Mock
			public boolean canRead() {
				return true;
			}
		};
		new MockUp<FileInputStream>() {
			@Mock
			public int read() throws IOException {
				throw new RuntimeException("FAILURE");
			}
			@Mock
			public int read(byte[] bytes) throws IOException {
				throw new RuntimeException("FAILURE");
			}
		};
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$extension$empty() throws Exception {
		final File file=setUpExtension(new Properties());
		new MockUp<RuntimeDelegate>() {
			@Mock
			File getConfigurationFile() {
				return file;
			}
		};
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$extension$notFound() throws Exception {
		final File file=setUpExtension("NotFound");
		new MockUp<RuntimeDelegate>() {
			@Mock
			File getConfigurationFile() {
				return file;
			}
		};
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$extension$notValid() throws Exception {
		final File file=setUpExtension(NotRuntimeDelegate.class.getName());
		new MockUp<RuntimeDelegate>() {
			@Mock
			File getConfigurationFile() {
				return file;
			}
		};
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$extension$notInstantiable() throws Exception {
		final File file=setUpExtension(AbstractRuntimeDelegate.class.getName());
		new MockUp<RuntimeDelegate>() {
			@Mock
			File getConfigurationFile() {
				return file;
			}
		};
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$extension$privateConstructor() throws Exception {
		final File file=setUpExtension(HiddenRuntimeDelegate.class.getName());
		new MockUp<RuntimeDelegate>() {
			@Mock
			File getConfigurationFile() {
				return file;
			}
		};
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$systemProperty$notFound() throws Exception {
		System.setProperty(RuntimeDelegate.APPLICATION_ENGINE_SPI_PROPERTY,"not found");
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$systemProperty$notValid() throws Exception {
		System.setProperty(RuntimeDelegate.APPLICATION_ENGINE_SPI_PROPERTY,NotRuntimeDelegate.class.getName());
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$systemProperty$notInstantiable() throws Exception {
		System.setProperty(RuntimeDelegate.APPLICATION_ENGINE_SPI_PROPERTY,AbstractRuntimeDelegate.class.getName());
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$systemProperty$privateConstructor() throws Exception {
		System.setProperty(RuntimeDelegate.APPLICATION_ENGINE_SPI_PROPERTY,HiddenRuntimeDelegate.class.getName());
		verifyIsDefault(sut());
	}

	@Test
	public void testGetInstance$systemProperty$valid() throws Exception {
		System.setProperty(RuntimeDelegate.APPLICATION_ENGINE_SPI_PROPERTY,CustomRuntimeDelegate.class.getName());
		assertThat(sut(),instanceOf(CustomRuntimeDelegate.class));
	}

	@Test
	public void testSetInstance$null() throws Exception {
		RuntimeDelegate.setInstance(null);
		verifyIsDefault(sut());
	}

	@Test
	public void testSetInstance$securityManager$happyPath() throws Exception {
		class CustomSecurityManager extends SecurityManager {
			private final List<Permission> permissions=Lists.newArrayList();
			@Override
			public void checkPermission(Permission permission) {
				System.out.println("checkPermission"+permission+"");
				this.permissions.add(permission);
			}
		};
		final CustomSecurityManager manager=new CustomSecurityManager();
		new MockUp<RuntimeDelegate>() {
			@Mock
			public SecurityManager getSecurityManager() {
				return manager;
			}
		};
		RuntimeDelegate.setInstance(null);
		assertThat(hasReflectionPermission(manager.permissions),equalTo(true));
	}



	@Test
	public void testSetInstance$securityManager$exceptionPath() throws Exception {
		class CustomSecurityManager extends SecurityManager {
			@Override
			public void checkPermission(Permission permission) {
				if(permission instanceof ReflectPermission) {
					ReflectPermission rp=(ReflectPermission)permission;
					if(rp.getName().equals("suppressAccessChecks")) {
						System.out.println("checkPermission"+permission+"");
						throw new SecurityException("FAILURE");
					}
				}
			}
		};

		final SecurityManager manager=new CustomSecurityManager();
		new MockUp<RuntimeDelegate>() {
			@Mock
			public SecurityManager getSecurityManager() {
				return manager;
			}
		};
		try {
			RuntimeDelegate.setInstance(new CustomRuntimeDelegate());
			fail("Should not be able to set instance if reflection is not allowed");
		} catch (SecurityException e) {
			assertThat(e.getMessage(),equalTo("FAILURE"));
		}
	}

	@Test
	public void testIsOffline$defaultImplementation() throws Exception {
		assertThat(sut().isOffline(),equalTo(true));
	}

	@Test
	public void testCreateSession$defaultImplementation() throws Exception {
		try {
			sut().createSession();
		} catch(ApplicationContextException e) {
			assertThat(e.getMessage(),notNullValue());
		}
	}

	@Test
	public void testCreateResourceResolver$defaultImplementation() throws Exception {
		assertThat(sut().createResourceResolver(canonicalBase,session),notNullValue());
	}

	@Test
	public void testDefaultResourceSnapshotResolver$resolveSnapshot() throws Exception {
		ResourceSnapshotResolver resolver = sut().createResourceResolver(canonicalBase,session);
		assertThat(resolver.resolve(snapshot),nullValue());
	}

	@Test
	public void testDefaultResourceSnapshotResolver$resolveURI() throws Exception {
		ResourceSnapshotResolver resolver = sut().createResourceResolver(canonicalBase,session);
		assertThat(resolver.resolve(endpoint),nullValue());
	}

}
