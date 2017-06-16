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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.ldp4j.example.BookContainerHandler;
import org.ldp4j.example.PersonContainerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutableTemplateLibraryTest extends TemplateLibraryTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(MutableTemplateLibraryTest.class);

	@Rule
	public TestName name=new TestName();

	private MutableTemplateLibrary sut;

	@Before
	public void setUp() {
		this.sut=new MutableTemplateLibrary();
	}

	private void logFailure(Throwable e) {
		LOGGER.debug("{}: {}",name.getMethodName(),e.getMessage());
	}

	@Test
	public void testRegisterUnannotatedResourceHandler() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ResourceHC.Unannotated.class);
			fail("Should not allow registering unnanotated resource handlers");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterUnannotatedContainerHandler() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Unannotated.class);
			fail("Should not allow registering unnanotated container handlers");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterUnannotatedResourceHandler$unknownAnnotations() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ResourceHC.AnnotatedWithUnsupportedAnnotation.class);
			fail("Should not allow registering resource handlers without a know annotation");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterUnannotatedContainerHandler$unknownAnnotations() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.AnnotatedWithUnsupportedAnnotation.class);
			fail("Should not allow registering container handlers without a known annotation");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterMisannotatedResourceHandler$basicContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ResourceHC.AnnotatedAsBasicContainer.class);
			fail("Should not allow registering resource handlers annotated as basic containers");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterMisannotatedResourceHandler$directContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ResourceHC.AnnotatedAsDirectContainer.class);
			fail("Should not allow registering resource handlers annotated as direct containers");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterMisannotatedResourceHandler$indirectContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ResourceHC.AnnotatedAsIndirectContainer.class);
			fail("Should not allow registering resource handlers annotated as indirect containers");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterContainerWithUnannotatedMemberHandler$basicContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Basic.UnnanotatedMemberResourceHandler.class);
			fail("Should not allow registering container handlers annotated as direct containers with unnanotated member handlers");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterContainerWithUnannotatedMemberHandler$directContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Direct.UnnanotatedMemberResourceHandler.class);
			fail("Should not allow registering container handlers annotated as basic containers with unnanotated member handlers");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterContainerWithUnannotatedMemberHandler$indirectContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Indirect.UnnanotatedMemberResourceHandler.class);
			fail("Should not allow registering container handlers annotated as indirect containers with unnanotated member handlers");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterContainerHandlerWithMultipleContainerAnnotations() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.MultipleContainerAnnotations.class);
			fail("Should not allow registering container handlers with multiple container annotations");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegisterContainerHandlerWithMixedTypeAnnotations() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.MixedTypeAnnotations.class);
			fail("Should not allow registering container handlers with multiple mixed annotations");
		} catch (Exception e) {
			logFailure(e);
		}
	}


	@Test
	public void testEmptyId$resource() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ResourceHC.EmptyID.class);
			fail("Should not allow registering resource handlers annotated as resources with no id");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testEmptyId$basicContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Basic.EmptyID.class);
			fail("Should not allow registering container handlers annotated as basic containers with no id");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testEmptyId$directContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Direct.EmptyID.class);
			fail("Should not allow registering container handlers annotated as direct containers with no id");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testEmptyId$indirectContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Indirect.EmptyID.class);
			fail("Should not allow registering container handlers annotated as indirect containers with no id");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testEmptyMembershipPredicate$directContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Direct.EmptyMembershipPredicate.class);
			fail("Should not allow registering container handlers annotated as direct containers with no membership predicate");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testEmptyMembershipPredicate$indirectContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Indirect.EmptyMembershipPredicate.class);
			fail("Should not allow registering container handlers annotated as indirect containers with no membership predicate");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testNullMembershipPredicate$directContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Direct.NullMembershipPredicate.class);
			fail("Should not allow registering container handlers annotated as direct containers with null membership predicate");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRelativeMembershipPredicate$directContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Direct.RelativeMembershipPredicate.class);
			fail("Should not allow registering container handlers annotated as direct containers with relative membership predicate");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	/**
	 * TODO: Allow using opaque URIs whenever the RDF handling
	 * backend supports it (for the time being we are using
	 * Sesame and it requires using HTTP URIs)
	 */
	@Test
	public void testOpaqueMembershipPredicate$directContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Direct.OpaqueMembershipPredicate.class);
			fail("Should not allow registering container handlers annotated as direct containers with opaque membership predicate");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testBadMembershipPredicate$directContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Direct.BadMembershipPredicate.class);
			fail("Should not allow registering container handlers annotated as direct containers with bad membership predicate");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testBadMembershipPredicate$indirectContainer() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Indirect.BadMembershipPredicate.class);
			fail("Should not allow registering container handlers annotated as indirect containers with bad membership predicate");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testEmptyInsertedContentRelation() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Indirect.EmptyInsertedContentRelation.class);
			fail("Should not allow registering container handlers annotated as indirect containers with empty inserted content relation");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testNullInsertedContentRelation() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Indirect.NullInsertedContentRelation.class);
			fail("Should not allow registering container handlers annotated as indirect containers with null inserted content relation");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRelativeInsertedContentRelation() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Indirect.RelativeInsertedContentRelation.class);
			fail("Should not allow registering container handlers annotated as indirect containers with relative inserted content relation");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	/**
	 * TODO: Allow using opaque URIs whenever the RDF handling
	 * backend supports it (for the time being we are using
	 * Sesame and it requires using HTTP URIs)
	 */
	@Test
	public void testOpaqueInsertedContentRelation() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Indirect.OpaqueInsertedContentRelation.class);
			fail("Should not allow registering container handlers annotated as indirect containers with opaque inserted content relation");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testBadInsertedContentRelation() throws Exception {
		try {
			this.sut.registerHandler(Fixture.Invalid.ContainerHC.Indirect.BadInsertedContentRelation.class);
			fail("Should not allow registering container handlers annotated as indirect containers with bad inserted content relation");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testRegistareHandlersWithClashingIds() throws Exception {
		this.sut.registerHandler(Fixture.Valid.ResourceHC.Handler.class);
		try {
			this.sut.registerHandler(Fixture.Valid.ContainerHC.Basic.Handler.class);
			fail("Should not allow registering handlers with clashing ids");
		} catch (Exception e) {
			logFailure(e);
		}
		try {
			this.sut.registerHandler(Fixture.Valid.ContainerHC.Direct.Handler.class);
			fail("Should not allow registering handlers with clashing ids");
		} catch (Exception e) {
			logFailure(e);
		}
		try {
			this.sut.registerHandler(Fixture.Valid.ContainerHC.Indirect.Handler.class);
			fail("Should not allow registering handlers with clashing ids");
		} catch (Exception e) {
			logFailure(e);
		}
	}

	@Test
	public void testFindsTemplatesByHandler() {
		final TemplateLibrary library=getLibrary();
		library.accept(
			new TemplateConsumer() {
				@Override
				protected void processTemplate(ResourceTemplate template) {
					ResourceTemplate other = library.findByHandler(template.handlerClass());
					assertThat(other,equalTo(template));
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
					assertThat(other,equalTo(template));
				}
			}
		);
	}

	@Test
	public void testEqualsDifferent() {
		final TemplateLibrary library=getLibrary();
		library.accept(
			new TemplateConsumer() {
				@Override
				protected void processTemplate(final ResourceTemplate template) {
					library.accept(
						new TemplateConsumer() {
							@Override
							protected void processTemplate(final ResourceTemplate other) {
								if(template!=other) {
									assertThat(other,not(equalTo(template)));
								}
							}
						}
					);
				}
			}
		);
	}

	@Test
	public void testIndirectContainerTemplate() {
		final TemplateLibrary library=getLibrary();
		ResourceTemplate template = library.findByHandler(BookContainerHandler.class);
		assertThat(template,instanceOf(IndirectContainerTemplate.class));
		assertThat(((IndirectContainerTemplate)template).insertedContentRelation().toString(),equalTo(BookContainerHandler.INSERTED_CONTENT_RELATION));
	}

	@Override
	protected TemplateLibrary getLibrary() {
		MutableTemplateLibrary library=new MutableTemplateLibrary();
		library.registerHandler(PersonContainerHandler.class);
		return library;
	}


}
