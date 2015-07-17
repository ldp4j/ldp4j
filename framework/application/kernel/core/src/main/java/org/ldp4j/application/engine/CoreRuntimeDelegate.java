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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-kernel-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine;

import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.engine.session.WriteSessionConfiguration;
import org.ldp4j.application.engine.session.WriteSessionService;
import org.ldp4j.application.engine.transaction.Transaction;
import org.ldp4j.application.engine.transaction.TransactionManager;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.spi.RuntimeDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CoreRuntimeDelegate extends RuntimeDelegate {

	private static final Logger LOGGER=LoggerFactory.getLogger(CoreRuntimeDelegate.class);

	private DefaultApplicationEngine applicationEngine() throws ApplicationEngineException {
		return
			ApplicationEngine.
				engine().
					unwrap(DefaultApplicationEngine.class);
	}

	private WriteSessionService sessionService() throws ApplicationEngineException {
		return applicationEngine().writeSessionService();
	}

	private TransactionManager transactionManager() throws ApplicationEngineException {
		return applicationEngine().transactionManager();
	}

	@Override
	public boolean isOffline() {
		boolean result=true;
		try {
			result=!applicationEngine().state().isStarted();
		} catch (ApplicationEngineException e) {
			LOGGER.warn("Could not check engine state",e);
		}
		return result;
	}

	@Override
	public WriteSession createSession() throws ApplicationContextException {
		try {
			WriteSession delegate =
				sessionService().
					createSession(
						WriteSessionConfiguration.
							builder().
								build());
			Transaction transaction=transactionManager().currentTransaction();
			transaction.begin();
			return new TransactionalWriteSession(transaction, delegate);
		} catch (ApplicationEngineException e) {
			throw new ApplicationContextException("Unsupported application engine implementation",e);
		}
	}

	@Override
	public void terminateSession(WriteSession session) throws ApplicationContextException {
		try {
			sessionService().terminateSession(session);
		} catch (ApplicationEngineException e) {
			throw new ApplicationContextException("Unsupported application engine implementation",e);
		}
	}

}
