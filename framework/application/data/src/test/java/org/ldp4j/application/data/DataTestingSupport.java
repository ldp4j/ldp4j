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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import static org.ldp4j.application.data.IndividualReference.anonymous;

import java.net.URI;

public abstract class DataTestingSupport {

	protected static final Name<String> RESOURCE_NAME = name("resourceName");
	protected static final String       MANAGER_ID    = "managerId";
	protected static final URI          RELATIVE_PATH = URI.create("path/");

	protected static final ManagedIndividualId MANAGED_INDIVIDUAL_ID = ManagedIndividualId.createId(RESOURCE_NAME, MANAGER_ID);
	protected static final URI EXTERNAL = URI.create("http://www.ldp4j.org/external/");
	protected static final URI SELF     = URI.create("");
	protected static final URI NEW      = URI.create("relative/");
	protected static final Name<String> BLANK_NODE = name("bNode");

	protected static Name<String> name(String id) {
		return NamingScheme.getDefault().name(id);
	}

	protected static IndividualReference<?,?> id(String name, String... names) {
		return anonymous(NamingScheme.getDefault().name(name, names));
	}

	protected static String predicate(String name) {
		return "http://www.example.org/vocab#"+name;
	}

	protected static RelativeIndividualId relativeIndividualId(String resourceName, String managerId, URI relativePath) {
		return RelativeIndividualId.createId(managedIndividualId(resourceName, managerId), relativePath);
	}

	protected static ManagedIndividualId managedIndividualId(String resourceName, String managerId) {
		return ManagedIndividualId.createId(name(resourceName), managerId);
	}


}
