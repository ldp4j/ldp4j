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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-examples:0.2.2
 *   Bundle      : ldp4j-application-examples-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.example;

import java.net.URI;
import java.util.Date;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SnapshotResolver;
import org.ldp4j.application.session.WriteSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that demonstrates how to resolve at runtime an snapshot address
 * and viceversa.
 */
public final class DynamicResourceResolver implements Runnable {

	private static final Logger LOGGER=LoggerFactory.getLogger(MyApplication.class);

	/** Predicate used for exposing whether or not the resolution roundtrip works */
	public static final URI SNAPSHOT_RESOLUTION = URI.create("http://www.ldp4j.org/ns#snapshotResolution");

	/** Predicate used for exposing the URI used for publishing the resource */
	public static final URI SNAPSHOT_ENDPOINT = URI.create("http://www.ldp4j.org/ns#snapshotEndpoint");

	/** Canonical base URI used for resolving the resources */
	public static final URI CANONICAL_BASE = URI.create("http://www.ldp4j.org/fixture/ldp4j/api/");

	private DynamicResourceHandler handler;
	private Name<String> name;

	/**
	 * Create a new instance with a handler and a resource name.
	 *
	 * @param handler
	 *            the handler that holds the resource to be resolved.
	 * @param name
	 *            the name of the resource to be resolved.
	 */
	public DynamicResourceResolver(DynamicResourceHandler handler, Name<String> name) {
		this.handler = handler;
		this.name = name;
	}

	/**
	 * Update the resource representation adding the path where the resource is
	 * published and whether or not if given that URI it can be resolved to the
	 * same resource.
	 */
	@Override
	public void run() {
		ApplicationContext ctx = ApplicationContext.getInstance();
		LOGGER.debug("Starting resolution process on {}...",new Date());
		try(WriteSession session=ctx.createSession()) {
			ResourceSnapshot snapshot=
				session.find(
					ResourceSnapshot.class,
					this.name,
					DynamicResourceHandler.class);

			DataSet dataSet = this.handler.get(snapshot);
			Individual<?,?> individual =
				dataSet.
					individualOfId(ManagedIndividualId.createId(snapshot.name(),snapshot.templateId()));

			SnapshotResolver snapshotResolver =
				SnapshotResolver.
					builder().
						withReadSession(session).
						withCanonicalBase(CANONICAL_BASE).
						build();
			URI snapshotEndpoint = snapshotResolver.toURI(snapshot);

			individual.
				addValue(
					SNAPSHOT_ENDPOINT,
					Literals.newLiteral(snapshotEndpoint));
			individual.
				addValue(
					SNAPSHOT_RESOLUTION,
					Literals.newLiteral(roundtrip(snapshotResolver,snapshotEndpoint,snapshot)));

			this.handler.update(this.name, dataSet);

			session.modify(snapshot);
			session.saveChanges();
		} catch (Exception e) {
			LOGGER.error("Could not resolve resource",e);
		} finally {
			LOGGER.debug("Finalized resolution process");
		}
	}

	private String roundtrip(SnapshotResolver snapshotResolver, URI snapshotEndpoint, ResourceSnapshot original) {
		ResourceSnapshot resolved = snapshotResolver.fromURI(snapshotEndpoint);
		return
			resolved!=null &&
			resolved.name().equals(original.name()) &&
			resolved.handlerClass()==original.handlerClass() ?
				"OK" :
				"KO";
	}

}