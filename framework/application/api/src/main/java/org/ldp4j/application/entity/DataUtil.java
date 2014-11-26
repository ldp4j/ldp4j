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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.entity;

public final class DataUtil {

	private static final String ENTITY_FIELD_PREFIX = "  ";
	private static final String ENTITY_OPENING = ") {";
	private static final String ENTITY_CLOSING = "}";
	private static final String TAB = "\t";
	private static final String NL=System.getProperty("line.separator");

	private DataUtil() {
	}

	private static String toString(Entity entity) {
		return String.format("%s : %s",entity.id(),prettyPrint(entity.identity()));
	}

	public static String prettyPrint(Literal<?> literal) {
		Object value = literal.value();
		return String.format("'%s' [%s]",literal.value(),value.getClass().getName());
	}

	public static String prettyPrint(Entity entity) {
		Identity identity=null;
		if(entity!=null) {
			identity=entity.identity();
		}
		return prettyPrint(identity);
	}

	public static String prettyPrint(Identity identity) {
		final StringBuilder builder=new StringBuilder();
		if(identity==null) {
			builder.append("null");
		} else {
			identity.accept(
				new IdentityVisitor() {
					private void log(String format, Object... args) {
						builder.append(String.format(format,args));
					}
					@Override
					public void visitLocal(LocalIdentity<?> identity) {
						Object localId = identity.localId();
						log("<'%s' [%s]> {Local}",localId,localId.getClass().getName());
					}
					@Override
					public void visitManaged(ManagedIdentity<?> identity) {
						Key<?> key = identity.key();
						Object nativeId = key.nativeId();
						log("<'%s' [%s]> {Managed by %s}",nativeId,nativeId.getClass().getName(),key.owner().getName());
					}
					@Override
					public void visitRelative(RelativeIdentity<?> identity) {
						log("<%s> {Child of <",identity.path());
						visitManaged(identity.parent());
						log(">}");
					}
					@Override
					public void visitExternal(ExternalIdentity identity) {
						log("<%s> {External}",identity.location());
					}
				}
			);
		}
		return builder.toString();
	}

	public static String prettyPrint(DataSource dataSource) {
		if(dataSource==null) {
			return null;
		}
		final StringBuilder builder=new StringBuilder();
		builder.append("DataSource(").append(dataSource.id()).append(ENTITY_OPENING).append(NL);
		for(Entity entity:dataSource) {
			builder.append(TAB).append("- Entity(").append(DataUtil.toString(entity)).append(ENTITY_OPENING).append(NL);
			for(Property property:entity) {
				builder.append(TAB).append(ENTITY_FIELD_PREFIX).append(TAB).append("+ Property(").append(property.predicate()).append(ENTITY_OPENING).append(NL);
				for(Value value:property) {
					builder.append(TAB).append(ENTITY_FIELD_PREFIX).append(TAB).append(ENTITY_FIELD_PREFIX).append(TAB).append("- ");
					value.accept(
						new ValueVisitor() {
							@Override
							void visitLiteral(Literal<?> value) {
								builder.append("Literal: ").append(prettyPrint(value));
							}
							@Override
							void visitEntity(Entity entity) {
								builder.append("Entity: ").append(DataUtil.toString(entity));
							}
						}
					);
					builder.append(NL);
				}
				builder.append(TAB).append(ENTITY_FIELD_PREFIX).append(TAB).append(ENTITY_FIELD_PREFIX).append(ENTITY_CLOSING).append(NL);
			}
			builder.append(TAB).append(ENTITY_FIELD_PREFIX).append(ENTITY_CLOSING).append(NL);
		}
		builder.append(ENTITY_CLOSING);
		return builder.toString();
	}

}
