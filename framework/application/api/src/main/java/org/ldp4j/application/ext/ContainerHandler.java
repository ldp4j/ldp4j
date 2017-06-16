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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.ext;

import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.data.DataSet;

/**
 * Interface to be implemented for handling Container LDP resources. <br>
 * <br>
 *
 * Handler implementations can be further extended by implementing the
 * {@link Deletable} and/or {@link Modifiable} interfaces.
 */
public interface ContainerHandler extends ResourceHandler {

	/**
	 * Creates a member of a container managed by the container handler.
	 *
	 * @param container
	 *            the container to which a member will be added.
	 * @param representation
	 *            the representation of the member that will be created.
	 * @param session
	 *            the session to use for registering the side effects of the
	 *            operation.
	 * @return the resource created.
	 * @throws UnknownResourceException
	 *             if the container handler does not manage the specified
	 *             container.
	 * @throws UnsupportedContentException
	 *             if the specified contents are not valid for creating the
	 *             member of the container.
	 * @throws ApplicationRuntimeException
	 *             if an internal exception prevents the creation of the member
	 *             resource.
	 */
	ResourceSnapshot create(ContainerSnapshot container, DataSet representation, WriteSession session)
			throws UnknownResourceException, UnsupportedContentException;

}