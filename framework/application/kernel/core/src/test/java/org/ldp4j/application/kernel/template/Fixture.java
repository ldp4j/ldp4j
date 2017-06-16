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

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.ext.UnsupportedContentException;
import org.ldp4j.application.ext.annotations.BasicContainer;
import org.ldp4j.application.ext.annotations.DirectContainer;
import org.ldp4j.application.ext.annotations.IndirectContainer;
import org.ldp4j.application.ext.annotations.Resource;
import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.example.PersonHandler;

public class Fixture {

	public static class NullResource implements ResourceHandler {

		@Override
		public final DataSet get(ResourceSnapshot resource)
				throws UnknownResourceException,
				ApplicationRuntimeException {
			return null;
		}

	}

	public static class NullContainer extends NullResource implements ContainerHandler {

		@Override
		public final ResourceSnapshot create(ContainerSnapshot container,
				DataSet representation, WriteSession session)
				throws UnknownResourceException,
				UnsupportedContentException, ApplicationRuntimeException {
			return null;
		}

	}

	public static class Invalid {

		public static class ContainerHC {

			public static class Basic {

				@BasicContainer(
					id = "id",
					memberHandler=NullResource.class
				)
				public static class UnnanotatedMemberResourceHandler extends NullContainer {
				}

				@BasicContainer(
					id="",
					memberHandler=PersonHandler.class
				)
				public static class EmptyID extends NullContainer {
				}

			}

			public static class Direct {

				@DirectContainer(
					id = "id",
					memberHandler=NullResource.class
				)
				public static class UnnanotatedMemberResourceHandler extends NullContainer {
				}

				@DirectContainer(
					id="",
					memberHandler=PersonHandler.class
				)
				public static class EmptyID extends NullContainer {
				}

				@DirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					membershipPredicate=""
				)
				public static class EmptyMembershipPredicate extends NullContainer {
				}

				@DirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					membershipPredicate="./predicate/../"
				)
				public static class NullMembershipPredicate extends NullContainer {
				}

				@DirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					membershipPredicate="relative/predicate/"
				)
				public static class RelativeMembershipPredicate extends NullContainer {
				}

				@DirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					membershipPredicate="scheme:relative/predicate/"
				)
				public static class OpaqueMembershipPredicate extends NullContainer {
				}

				@DirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					membershipPredicate="::invalidMembershipPredicate::"
				)
				public static class BadMembershipPredicate extends NullContainer {
				}

			}

			public static class Indirect {

				@IndirectContainer(
					id = "id",
					memberHandler=NullResource.class
				)
				public static class UnnanotatedMemberResourceHandler extends NullContainer {
				}

				@IndirectContainer(
					id="",
					memberHandler=PersonHandler.class
				)
				public static class EmptyID extends NullContainer {
				}

				@IndirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					membershipPredicate=""
				)
				public static class EmptyMembershipPredicate extends NullContainer {
				}

				@IndirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					membershipPredicate="::invalidMembershipPredicate::"
				)
				public static class BadMembershipPredicate extends NullContainer {
				}

				@IndirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					insertedContentRelation=""
				)
				public static class EmptyInsertedContentRelation extends NullContainer {
				}

				@IndirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					insertedContentRelation="./predicate/../"
				)
				public static class NullInsertedContentRelation extends NullContainer {
				}

				@IndirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					insertedContentRelation="relative/predicate/"
				)
				public static class RelativeInsertedContentRelation extends NullContainer {
				}

				@IndirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					insertedContentRelation="scheme:relative/predicate/"
				)
				public static class OpaqueInsertedContentRelation extends NullContainer {
				}

				@IndirectContainer(
					id="id",
					memberHandler=PersonHandler.class,
					insertedContentRelation="::invalidInsertedContentRelation::"
				)
				public static class BadInsertedContentRelation extends NullContainer {
				}
			}

			public static class Unannotated extends NullContainer {
			}

			@UnsupportedAnnotation
			public static class AnnotatedWithUnsupportedAnnotation extends NullContainer {
			}

		}

		public static class ResourceHC {

			public static class Unannotated extends NullResource {
			}

			@BasicContainer(
				id = "id",
				memberHandler=PersonHandler.class
			)
			public class AnnotatedAsBasicContainer extends Fixture.NullResource {
			}

			@DirectContainer(
				id = "id",
				memberHandler=PersonHandler.class
			)
			public static class AnnotatedAsDirectContainer extends NullResource {
			}

			@IndirectContainer(
				id = "id",
				memberHandler=PersonHandler.class
			)
			public static class AnnotatedAsIndirectContainer extends NullResource {
			}

			@UnsupportedAnnotation
			public static class AnnotatedWithUnsupportedAnnotation extends NullResource {
			}

			@Resource(
				id=""
			)
			public static class EmptyID extends NullResource {
			}
		}

		@BasicContainer(
			id = "id",
			memberHandler=PersonHandler.class
		)
		@DirectContainer(
			id = "id",
			memberHandler=PersonHandler.class
		)
		public static class MultipleContainerAnnotations extends NullContainer {
		}

		@Resource(
			id = "id"
		)
		@DirectContainer(
			id = "id",
			memberHandler=PersonHandler.class
		)
		public static class MixedTypeAnnotations extends NullContainer {
		}

	}

	public static class Valid {

		public static class ContainerHC {

			public static class Basic {
				@BasicContainer(
					id = "id",
					memberHandler=PersonHandler.class
				)
				public static class Handler extends NullContainer {
				}
			}

			public static class Direct {
				@DirectContainer(
					id = "id",
					memberHandler=PersonHandler.class
				)
				public static class Handler extends NullContainer {
				}
			}

			public static class Indirect {
				@IndirectContainer(
					id = "id",
					memberHandler=PersonHandler.class
				)
				public static class Handler extends NullContainer {
				}
			}
		}

		public static class ResourceHC {
			@Resource(
				id = "id"
			)
			public static class Handler extends NullResource {
			}
		}

	}

}
