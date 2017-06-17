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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;
import org.ldp4j.example.PersonContainerHandler;

public class ImmutableTemplateLibraryTest extends TemplateLibraryTest {

	@Override
	protected TemplateLibrary getLibrary() {
		MutableTemplateLibrary library= new MutableTemplateLibrary();
		library.registerHandler(PersonContainerHandler.class);
		return new ImmutableTemplateLibrary(library);
	}

	@Test
	public void testFindsTemplatesByHandler() {
		final TemplateLibrary library=getLibrary();
		library.accept(
			new TemplateConsumer() {
				@Override
				protected void processTemplate(ResourceTemplate template) {
					ResourceTemplate other = library.findByHandler(template.handlerClass());
					assertThat(other,not(nullValue()));
				}
			}
		);
	}

	@Test
	public void testFindsTemplatesById() {
		final TemplateLibrary library=getLibrary();
		library.accept(
			new TemplateConsumer() {
				@Override
				protected void processTemplate(ResourceTemplate template) {
					ResourceTemplate other = library.findById(template.id());
					assertThat(other,not(nullValue()));
				}
			}
		);
	}

	@Test
	public void testContainsTemplates() {
		final TemplateLibrary library=getLibrary();
		library.accept(
			new TemplateConsumer() {
				@Override
				protected void processTemplate(ResourceTemplate template) {
					assertThat(library.contains(template),equalTo(true));
				}
			}
		);
	}

}
