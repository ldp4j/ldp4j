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
		ManagedIndividualId id = individual.id();
		log("%s {Managed by: %s}",id.name(),id.managerId());
	}

	@Override
	public void visitLocalIndividual(LocalIndividual individual) {
		Name<?> name = individual.id();
		Object id=name.id();
		log("%s [%s] {Local}",id,id.getClass().getCanonicalName());
	}

	@Override
	public void visitExternalIndividual(ExternalIndividual individual) {
		log("<%s> {External}",individual.id());
	}

	public static String formatLiteral(Literal<?> literal) {
		return String.format("%s [%s]",literal.get(),literal.get().getClass().getCanonicalName());
	}
	
	public static String formatName(Name<?> tmp) {
		if(tmp==null) {
			return "<null>";
		}
		return String.format("%s [%s]",tmp.id(),tmp.id().getClass().getCanonicalName());
	}

	public static String formatIndividualId(Individual<?, ?> individual) {
		if(individual==null) {
			return "<null>";
		}
		FormatUtils visitor = new FormatUtils();
		individual.accept(visitor);
		return visitor.getId();
	}

	public static String formatId(Object id) {
		String result = "<null>";
		if(id==null) {
			return result;
		}
		
		if(id instanceof URI) {
			result=String.format("<%s> {External}",id);
		} else if(id instanceof Name<?>) {
			Name<?> name=(Name<?>)id;
			result=String.format("%s [%s] {Local}",name,name.getClass().getCanonicalName());
		} else if(id instanceof ManagedIndividualId) {
			ManagedIndividualId mid = (ManagedIndividualId)id;
			result=String.format("%s {Managed by: %s}",mid.name(),mid.managerId());
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