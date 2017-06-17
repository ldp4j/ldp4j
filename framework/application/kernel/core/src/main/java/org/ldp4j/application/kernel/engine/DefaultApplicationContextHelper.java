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
package org.ldp4j.application.kernel.engine;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.IndividualVisitor;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.LocalIndividual;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.NewIndividual;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.RelativeIndividual;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.ValueVisitor;
import org.ldp4j.application.engine.context.InvalidIndirectIdentifierException;
import org.ldp4j.application.kernel.resource.Container;
import org.ldp4j.application.kernel.resource.FeatureException;
import org.ldp4j.application.kernel.resource.Resource;
import org.ldp4j.application.kernel.session.WriteSessionConfiguration;
import org.ldp4j.application.kernel.template.TemplateIntrospector;
import org.ldp4j.application.kernel.template.TemplateManagementService;
import org.ldp4j.application.vocabulary.LDP;

import com.google.common.collect.Lists;

/**
 * TODO: Make indirect identifier validation model externally configurable
 */
final class DefaultApplicationContextHelper {

	private static final class IndirectIdCollector implements ValueVisitor {

		private final class InnerVisitor implements IndividualVisitor {

			private final List<URI> ids;

			private InnerVisitor() {
				this.ids=Lists.newArrayList();
			}

			@Override
			public void visitManagedIndividual(ManagedIndividual individual) {
				IndirectIdCollector.this.managedIndividualIds=true;
			}

			@Override
			public void visitLocalIndividual(LocalIndividual individual) {
				IndirectIdCollector.this.localIndividualIds=true;
			}

			@Override
			public void visitExternalIndividual(ExternalIndividual individual) {
				this.ids.add(individual.id());
			}

			@Override
			public void visitRelativeIndividual(RelativeIndividual individual) {
				IndirectIdCollector.this.relativeIndividualIds=true;
			}

			@Override
			public void visitNewIndividual(NewIndividual individual) {
				IndirectIdCollector.this.newIndividualIds=true;
			}

		}

		private final boolean strict;
		private final InnerVisitor innerVisitor;
		private final URI insertedContentRelation;

		private boolean memberSubject=false;
		private boolean literalValues=false;
		private boolean managedIndividualIds=false;
		private boolean newIndividualIds=false;
		private boolean relativeIndividualIds=false;
		private boolean localIndividualIds=false;
		private int totalValues=0;

		private IndirectIdCollector(boolean strict, URI insertedContentRelation, Property property) {
			this.strict = strict;
			this.insertedContentRelation = insertedContentRelation;
			this.innerVisitor=new InnerVisitor();
			this.memberSubject=LDP.MEMBER_SUBJECT.as(URI.class).equals(insertedContentRelation);
			if(property==null) {
				if(!this.memberSubject) {
					throw new InvalidIndirectIdentifierException(insertedContentRelation,"Inserted content relation '%s' not defined for the empty resource");
				}
			} else {
				for(Value v:property) {
					this.totalValues++;
					v.accept(this);
				}
			}
		}

		@Override
		public void visitLiteral(Literal<?> value) {
			this.literalValues=true;
		}

		@Override
		public void visitIndividual(Individual<?, ?> value) {
			value.accept(this.innerVisitor);
		}

		private URI getIndirectId() {
			validate();
			URI result=null;
			List<URI> ids = this.innerVisitor.ids;
			if(!ids.isEmpty()) {
				result=ids.get(0);
			}
			return result;
		}

		private void validate() {
			if(this.totalValues==0 && !this.memberSubject) {
				throw new InvalidIndirectIdentifierException(this.insertedContentRelation,"No values defined for inserted content relation '%s' for the empty resource");
			} else if(this.totalValues>1) {
				throw new InvalidIndirectIdentifierException(this.insertedContentRelation,"Multiple values defined for inserted content relation '%s' for the empty resource");
			} else if(this.literalValues) {
				throw new InvalidIndirectIdentifierException(this.insertedContentRelation,"Invalid value defined for inserted content relation '%s' for the empty resource (literal)");
			} else if(this.strict) {
				verifyExternalIndividualSpecified();
			}
		}

		private void verifyExternalIndividualSpecified() {
			if(this.managedIndividualIds) {
				throw new InvalidIndirectIdentifierException(this.insertedContentRelation,"Invalid value defined for inserted content relation '%s' for the empty resource (managed individual identifier)");
			} else if(this.localIndividualIds) {
				throw new InvalidIndirectIdentifierException(this.insertedContentRelation,"Invalid value defined for inserted content relation '%s' for the empty resource (local individual identifier)");
			} else if(this.newIndividualIds) {
				throw new InvalidIndirectIdentifierException(this.insertedContentRelation,"Invalid value defined for inserted content relation '%s' for the empty resource (empty resource)");
			} else if(this.relativeIndividualIds) {
				throw new InvalidIndirectIdentifierException(this.insertedContentRelation,"Invalid value defined for inserted content relation '%s' for the empty resource (relative individual identifier)");
			}
		}

	}

	private static final URI NEW_RESOURCE_SURROGATE_ID = URI.create("");

	private final TemplateManagementService templateManagementService;

	private DefaultApplicationContextHelper(TemplateManagementService templateManagementService) {
		this.templateManagementService = templateManagementService;
	}

	WriteSessionConfiguration createConfiguration(Resource resource, Date lastModified) {
		return
			WriteSessionConfiguration.
				builder().
					withTarget(resource).
					withLastModified(lastModified).
					build();
	}

	WriteSessionConfiguration createConfiguration(Container container, DataSet dataSet, String desiredPath, Date lastModified) throws FeatureException {
		return
			WriteSessionConfiguration.
				builder().
					withTarget(container).
					withPath(desiredPath).
					withIndirectId(getIndirectId(container, dataSet)).
					withLastModified(lastModified).
					build();
	}

	private URI getIndirectId(Container container, DataSet dataSet) {
		TemplateIntrospector introspector=
			TemplateIntrospector.
				newInstance(
					this.templateManagementService.
						templateOfId(container.id().templateId()));
		if(!introspector.isIndirectContainer()) {
			return null;
		}
		URI insertedContentRelation=introspector.getInsertedContentRelation();
		IndirectIdCollector collector=
			new IndirectIdCollector(
				false,
				insertedContentRelation,
				getInsertedContentRelationProperty(dataSet,insertedContentRelation));
		return collector.getIndirectId();
	}

	private Property getInsertedContentRelationProperty(DataSet dataSet, URI insertedContentRelation) {
		NewIndividual individual = dataSet.individual(NEW_RESOURCE_SURROGATE_ID, NewIndividual.class);
		return individual.property(insertedContentRelation);
	}

	static DefaultApplicationContextHelper create(TemplateManagementService templateManagementService) {
		return new DefaultApplicationContextHelper(templateManagementService);
	}

}
