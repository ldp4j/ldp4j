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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-kernel-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensibilityTest {

	private static interface IFace {

	}

	private abstract static class Base {

		protected abstract Class<? extends IFace> implementationClass();

		protected abstract <T extends IFace> void dispose(T instance);

		public final void execute(IFace instance) {
			try {
				IFace safeCast = implementationClass().cast(instance);
				dispose(safeCast);
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Invalid implementation class",e);
			}
		}

	}

	private static class LoggedIFace implements IFace {

		private static final Logger LOGGER=LoggerFactory.getLogger(LoggedIFace.class);

		private String name;

		private LoggedIFace(String name) {
			this.name = name;
		}

		private void log() {
			LOGGER.debug("LoggedIFace::{}",name);
		}

	}

	private static class LoggedBase extends Base {

		@Override
		protected Class<? extends IFace> implementationClass() {
			return LoggedIFace.class;
		}

		@Override
		protected <T extends IFace> void dispose(T instance) {
			((LoggedIFace)instance).log();
		}

	}

	@Test
	public void testValid() {
		Base base=new LoggedBase();
		LoggedIFace iface = new LoggedIFace("example");
		base.execute(iface);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testInvalid() {
		Base base=new LoggedBase();
		IFace iface = new IFace() {

		};
		base.execute(iface);
	}
}
