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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-data-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public final class FormatUtils implements IndividualVisitor {

	private static final String NEW_ID_FORMAT              = "<%s> {New}";
	private static final String RELATIVE_ID_FORMAT         = "<%s> {Parent: %s}";
	private static final String MANAGED_ID_INDIRECT_FORMAT = "%s {Managed by: %s, indirect id: <%s>}";
	private static final String MANAGED_ID_FORMAT          = "%s {Managed by: %s}";
	private static final String LOCAL_ID_FORMAT            = "%s [%s] {Local}";
	private static final String EXTERNAL_ID_FORMAT         = "<%s> {External}";
	private static final String COMPOSITE_FORMAT           = "%s (%s) [%s]";
	private static final String SIMPLE_FORMAT              = "%s [%s]";
	private static final String NULL                       = "<null>";

	private String id;

	private FormatUtils() {
	}

	public String getId() {
		return id;
	}

	private void log(String message, Object... args) {
		this.id=String.format(message,args);
	}

	@Override
	public void visitManagedIndividual(ManagedIndividual individual) {
		ManagedIndividualId mId = individual.id();
		if(mId.indirectId()==null) {
			log(MANAGED_ID_FORMAT,mId.name(),mId.managerId());
		} else {
			log(MANAGED_ID_INDIRECT_FORMAT,mId.name(),mId.managerId(),mId.indirectId());
		}
	}

	@Override
	public void visitRelativeIndividual(RelativeIndividual individual) {
		RelativeIndividualId rId = individual.id();
		log(RELATIVE_ID_FORMAT,rId.path(),formatId(rId.parentId()));
	}

	@Override
	public void visitLocalIndividual(LocalIndividual individual) {
		Name<?> name = individual.id();
		Object id=name.id();
		log(LOCAL_ID_FORMAT,id,id.getClass().getCanonicalName());
	}

	@Override
	public void visitExternalIndividual(ExternalIndividual individual) {
		log(EXTERNAL_ID_FORMAT,individual.id());
	}

	@Override
	public void visitNewIndividual(NewIndividual individual) {
		log(NEW_ID_FORMAT,individual.id());
	}

	public static String formatLiteral(final Literal<?> literal) {
		class LiteralFormatter implements LiteralVisitor {
			private String format;
			@Override
			public void visitLiteral(Literal<?> literal) {
				this.format=String.format(SIMPLE_FORMAT,literal.get(),literal.get().getClass().getCanonicalName());
			}
			@Override
			public void visitTypedLiteral(TypedLiteral<?> literal) {
				this.format=String.format(COMPOSITE_FORMAT,literal.get(),literal.type(),literal.get().getClass().getCanonicalName());
			}
			@Override
			public void visitLanguageLiteral(LanguageLiteral literal) {
				this.format=String.format(COMPOSITE_FORMAT,literal.get(),literal.language(),literal.get().getClass().getCanonicalName());
			}
			String format() {
				return this.format;
			}
		}
		LiteralFormatter literalFormatter = new LiteralFormatter();
		literal.accept(literalFormatter);
		return literalFormatter.format();
	}

	public static String formatName(Name<?> tmp) {
		if(tmp==null) {
			return NULL;
		}
		return String.format(SIMPLE_FORMAT,tmp.id(),tmp.id().getClass().getCanonicalName());
	}

	public static String formatIndividualId(Individual<?, ?> individual) {
		if(individual==null) {
			return NULL;
		}
		FormatUtils visitor = new FormatUtils();
		individual.accept(visitor);
		return visitor.getId();
	}

	public static String formatId(Object id) {
		String result = NULL;
		if(id==null) {
			return result;
		}

		if(id instanceof URI) {
			result=String.format(EXTERNAL_ID_FORMAT,id);
		} else if(id instanceof Name<?>) {
			Name<?> name=(Name<?>)id;
			result=String.format(LOCAL_ID_FORMAT,name,name.getClass().getCanonicalName());
		} else if(id instanceof ManagedIndividualId) {
			ManagedIndividualId mid = (ManagedIndividualId)id;
			if(mid.indirectId()==null) {
				result=String.format(MANAGED_ID_FORMAT,mid.name(),mid.managerId());
			} else {
				result=String.format(MANAGED_ID_INDIRECT_FORMAT,mid.name(),mid.managerId(),mid.indirectId());
			}
		} else if(id instanceof RelativeIndividualId){
			RelativeIndividualId rid = (RelativeIndividualId)id;
			result=String.format(RELATIVE_ID_FORMAT,rid.path(),rid.parentId());
		} else {
			result=id.toString();
		}
		return result;
	}

	public static String formatValue(Value value) {
		final AtomicReference<String> strValue=new AtomicReference<String>();
		value.accept(
			new ValueVisitor() {
				@Override
				public void visitLiteral(Literal<?> value) {
					strValue.set(FormatUtils.formatLiteral(value));
				}
				@Override
				public void visitIndividual(Individual<?, ?> value) {
					strValue.set(FormatUtils.formatIndividualId(value));
				}
			}
		);
		return strValue.get();
	}

}