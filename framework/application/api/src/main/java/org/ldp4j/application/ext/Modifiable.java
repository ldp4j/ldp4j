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

import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.data.DataSet;

/**
 * Interface to be implemented by {@link ResourceHandler} implementations that
 * also support resource modification.
 */
public interface Modifiable {

	/**
	 * Update the state of a resource managed by a handler.
	 *
	 * @param resource
	 *            the resource whose state is to be updated.
	 * @param content
	 *            the new content for the resource.
	 * @param session
	 *            the session to use for registering the side effects of the
	 *            operation.
	 * @throws UnknownResourceException
	 *             if the handler does not manage the specified resource.
	 * @throws UnsupportedContentException
	 *             if the resource cannot be updated with the specified
	 *             contents.
	 * @throws InconsistentContentException
	 *             if the specified contents include values that modify current
	 *             values of server managed properties.
	 * @throws ApplicationRuntimeException
	 *             if internal exception prevents the update of the resource.
	 */
	void update(ResourceSnapshot resource, DataSet content, WriteSession session)
			throws
				UnknownResourceException,
				UnsupportedContentException,
				InconsistentContentException;

}