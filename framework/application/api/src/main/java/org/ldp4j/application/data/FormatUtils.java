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
package org.ldp4j.application.data;

import java.net.URI;

public final class FormatUtils {

	private static final class IdFormatter implements IndividualVisitor {

		private String format;

		private IdFormatter() {
		}

		private String getFormat() {
			return this.format;
		}

		@Override
		public void visitManagedIndividual(ManagedIndividual individual) {
			this.format=formatManagedIndividualId(individual.id());
		}

		@Override
		public void visitRelativeIndividual(RelativeIndividual individual) {
			this.format=formatRelativeIndividualId(individual.id());
		}

		@Override
		public void visitLocalIndividual(LocalIndividual individual) {
			this.format=formatLocalIndividualId(individual.id());
		}

		@Override
		public void visitExternalIndividual(ExternalIndividual individual) {
			this.format=formatExternalIndividualId(individual.id());
		}

		@Override
		public void visitNewIndividual(NewIndividual individual) {
			this.format=formatNewIndividualId(individual.id());
		}

	}

	private static final class LiteralFormatter implements LiteralVisitor {

		private String format;

		private LiteralFormatter() {
		}

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

		private String getFormat() {
			return this.format;
		}

	}

	private static final class ValueFormatter implements ValueVisitor {

		private String format=null;

		private ValueFormatter() {
		}

		private String getFormat() {
			return this.format;
		}

		@Override
		public void visitLiteral(Literal<?> value) {
			this.format=FormatUtils.formatLiteral(value);
		}
		@Override
		public void visitIndividual(Individual<?, ?> value) {
			this.format=FormatUtils.formatId(value);
		}

	}

	private static final class PropertyValueFormatter implements ValueVisitor {

		private final StringBuilder builder;

		private PropertyValueFormatter(StringBuilder builder) {
			this.builder = builder;
		}

		private void formatValue(String str) {
			this.builder.append(TAB).append(TAB).append(TAB).append("* ").append(str).append(NL);
		}

		@Override
		public void visitLiteral(Literal<?> value) {
			formatValue(FormatUtils.formatLiteral(value));
		}

		@Override
		public void visitIndividual(Individual<?, ?> value) {
			formatValue(FormatUtils.formatId(value));
		}

	}

	private static final String BLOCK_START = ") {";
	private static final String BLOCK_END   = "}";
	private static final String TAB         = "\t";
	private static final String NL          =System.lineSeparator();


	private static final String NEW_ID_FORMAT              = "<%s> {New}";
	private static final String RELATIVE_ID_FORMAT         = "<%s> {Parent: %s}";
	private static final String MANAGED_ID_INDIRECT_FORMAT = "%s {Managed by: %s, indirect id: <%s>}";
	private static final String MANAGED_ID_FORMAT          = "%s {Managed by: %s}";
	private static final String LOCAL_ID_FORMAT            = "%s [%s] {Local}";
	private static final String EXTERNAL_ID_FORMAT         = "<%s> {External}";
	private static final String COMPOSITE_FORMAT           = "%s (%s) [%s]";
	private static final String SIMPLE_FORMAT              = "%s [%s]";
	private static final String NULL                       = "<null>";

	private FormatUtils() {
	}

	private static String formatNewIndividualId(Object id) {
		return String.format(NEW_ID_FORMAT, id);
	}

	private static String formatExternalIndividualId(Object id) {
		return String.format(EXTERNAL_ID_FORMAT,id);
	}

	private static String formatLocalIndividualId(Name<?> name) {
		return String.format(LOCAL_ID_FORMAT,name,name.getClass().getCanonicalName());
	}

	private static String formatRelativeIndividualId(RelativeIndividualId rid) {
		return String.format(RELATIVE_ID_FORMAT,rid.path(),rid.parentId());
	}

	private static String formatManagedIndividualId(ManagedIndividualId mId) {
		String result=null;
		if(mId.indirectId()==null) {
			result=String.format(MANAGED_ID_FORMAT,mId.name(),mId.managerId());
		} else {
			result=String.format(MANAGED_ID_INDIRECT_FORMAT,mId.name(),mId.managerId(),mId.indirectId());
		}
		return result;
	}

	public static String formatLiteral(final Literal<?> literal) {
		if(literal==null) {
			return NULL;
		}
		LiteralFormatter literalFormatter = new LiteralFormatter();
		literal.accept(literalFormatter);
		return literalFormatter.getFormat();
	}

	public static String formatName(Name<?> name) {
		if(name==null) {
			return NULL;
		}
		return String.format(SIMPLE_FORMAT,name.id(),name.id().getClass().getCanonicalName());
	}

	public static String formatId(Individual<?, ?> individual) {
		if(individual==null) {
			return NULL;
		}
		IdFormatter formatter = new IdFormatter();
		individual.accept(formatter);
		return formatter.getFormat();
	}

	public static String formatId(Object id) {
		String result = NULL;
		if(id==null) {
			return result;
		}

		if(id instanceof URI) {
			result=formatExternalIndividualId(id);
		} else if(id instanceof Name<?>) {
			result=formatLocalIndividualId((Name<?>)id);
		} else if(id instanceof ManagedIndividualId) {
			result=formatManagedIndividualId((ManagedIndividualId)id);
		} else if(id instanceof RelativeIndividualId){
			result=formatRelativeIndividualId((RelativeIndividualId)id);
		} else {
			result=formatNewIndividualId(id);
		}
		return result;
	}

	public static String formatValue(Value value) {
		if(value==null) {
			return NULL;
		}
		ValueFormatter formatter=new ValueFormatter();
		value.accept(formatter);
		return formatter.getFormat();
	}
	public static String formatDataSet(DataSet dataSet) {
		if(dataSet==null) {
			return NULL;
		}

		StringBuilder builder=new StringBuilder();
		builder.append("DataSet(").append(FormatUtils.formatName(dataSet.name())).append(BLOCK_START).append(NL);
		for(Individual<?,?> individual:dataSet.individuals()) {
			if(individual.hasProperties()) {
				formatIndividual(builder, individual);
			}
		}
		builder.append(BLOCK_END);
		return builder.toString();
	}

	private static void formatIndividual(StringBuilder builder, Individual<?, ?> individual) {
		PropertyValueFormatter formatter=new PropertyValueFormatter(builder);
		builder.append(TAB).append("- Individual(").append(FormatUtils.formatId(individual)).append(BLOCK_START).append(NL);
		for(Property property:individual) {
			builder.append(TAB).append(TAB).append("+ Property(").append(property.predicate()).append(BLOCK_START).append(NL);
			for(Value value:property) {
				value.accept(formatter);
			}
			builder.append(TAB).append(TAB).append(BLOCK_END).append(NL);
		}
		builder.append(TAB).append(BLOCK_END).append(NL);
	}


}