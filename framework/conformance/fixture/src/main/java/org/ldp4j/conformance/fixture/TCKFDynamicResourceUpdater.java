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
 *   Artifact    : org.ldp4j.framework:ldp4j-conformance-fixture:0.1.0
 *   Bundle      : ldp4j-conformance-fixture-0.1.0.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.conformance.fixture;

import java.net.URI;
import java.util.Date;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TCKFDynamicResourceUpdater implements Runnable {

	private static final Logger LOGGER=LoggerFactory.getLogger(TCKFApplication.class);

	private TCKFDynamicResourceHandler handler;
	private Name<String> name;

	public TCKFDynamicResourceUpdater(TCKFDynamicResourceHandler handler, Name<String> name) {
		this.handler = handler;
		this.name = name;
	}

	@Override
	public void run() {
		Date date = new Date();
		LOGGER.debug("Starting update process on {}...",date);
		try {
			WriteSession session = ApplicationContext.getInstance().createSession();
			ResourceSnapshot snapshot = session.find(ResourceSnapshot.class, this.name,TCKFDynamicResourceHandler.class);
			DataSet dataSet = this.handler.get(snapshot);
			Individual<?,?> individual =
				dataSet.
					individualOfId(
						ManagedIndividualId.
							createId(this.name, TCKFDynamicResourceHandler.ID));
			individual.
				addValue(
					URI.create("http://www.ldp4j.org/ns#refreshedOn"),
					Literals.of(date).dateTime());
			this.handler.update(this.name, dataSet);
			session.modify(snapshot);
			session.saveChanges();
		} catch (WriteSessionException e) {
			LOGGER.error("Could not update resource",e);
		} catch (ApplicationContextException e) {
			LOGGER.error("Could not update resource",e);
		} finally {
			LOGGER.debug("Finalized update process");
		}
	}

}