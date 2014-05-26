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
 *   Artifact    : org.ldp4j.framework:ldp4j-client-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client;

import java.io.IOException;
import java.util.Date;

/**
 * A {@code IRepresentation} is a particular serialization of a <i>Linked Data
 * Platform Resource</i>. The representation provides the means for
 * {@link #getContent() retrieving the contents} using a specific {@link Format
 * format}, as well as inspecting the {@link #getEntityTag() entity tag} and
 * {@link #getLastModified() last modified date} of the resource.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public interface IRepresentation {

	/**
	 * Get a the contents of the resource.
	 * 
	 * @return The contents of the representation.
	 * @throws IOException
	 *             if the contents cannot be retrieved.
	 * @see IContent
	 */
	IContent getContent() throws IOException;
	
	/**
	 * Get the current entity tag of the resource.
	 * 
	 * @return The entity tag of the resource.
	 */
	String getEntityTag();

	/**
	 * Get the date when the resource was modified for the last time.
	 * 
	 * @return The last modified date of the resource.
	 */
	Date getLastModified();

}
