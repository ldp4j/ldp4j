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

import java.io.Serializable;
import java.net.URI;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class IndividualReferenceBuilderTest {

	@Test
	public void testRelative() {
		IndividualReference<?, ?> reference =
			IndividualReferenceBuilder.
				newReference().
					toRelativeIndividual().
						atLocation("..").
						ofIndividualManagedBy("template").
						named(23);
		assertThat(reference.ref(),instanceOf(RelativeIndividualId.class));
		System.out.println(reference.ref());
	}

	@Test
	public void testExternal() {
		IndividualReference<?, ?> reference =
			IndividualReferenceBuilder.
				newReference().
					toExternalIndividual().
						atLocation("http://localhost:7080/test");
		assertThat((Serializable) reference.ref(),instanceOf(URI.class));
		System.out.println(reference.ref());
	}

	@Test
	public void testLocal() {
		IndividualReference<?, ?> reference =
			IndividualReferenceBuilder.
				newReference().
					toLocalIndividual().
						named(URI.class,"myresource");
		assertThat((Serializable) reference.ref(),instanceOf(Name.class));
		System.out.println(reference.ref());
	}

	@Test
	public void testManaged() {
		IndividualReference<?, ?> reference =
			IndividualReferenceBuilder.
				newReference().
					toManagedIndividual("template").
						named(23);
		assertThat((Serializable) reference.ref(),instanceOf(ManagedIndividualId.class));
		System.out.println(reference.ref());
	}

}
