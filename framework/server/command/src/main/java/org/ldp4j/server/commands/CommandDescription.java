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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.commands;

public final class CommandDescription {

	public static enum CommandType {
		CREATE_ENDPOINT(CommandProcessor.CREATE_ENDPOINT_CLAZZ),
		DELETE_ENDPOINT(CommandProcessor.DELETE_ENDPOINT_CLAZZ),
		MODIFY_ENDPOINT_CONFIGURATION(CommandProcessor.MODIFY_ENDPOINT_CONFIGURATION_CLAZZ),
		UPDATE_RESOURCE_STATE(CommandProcessor.UPDATE_RESOURCE_STATE_CLAZZ),
		;
		
		private final Class<?> clazz;

		private CommandType(Class<?> clazz) {
			this.clazz = clazz;
		}
		
		static CommandType fromDescription(Object commandDocument) {
			for(CommandType type:values()) {
				if(type.clazz.isInstance(commandDocument)) {
					return type;
				}
			}
			return null;
		}
		
		@Override
		public String toString() {
			return name()+" ("+clazz.getCanonicalName()+")";
		}

	}
	
	private final Object commandDocument;
	private final CommandType type;

	private CommandDescription(CommandType type, Object commandDocument) {
		this.type = type;
		this.commandDocument = commandDocument;
	}

	public static CommandDescription newInstance(Object commandDocument) throws InvalidCommandException {
		CommandType type = CommandType.fromDescription(commandDocument);
		if(type==null) {
			throw new InvalidCommandException("Unsupported command '"+commandDocument.getClass()+"'");
		}
		return new CommandDescription(type,commandDocument);
	}
	
	public CommandType getType() {
		return type;
	}
	
	public <T> T as(Class<? extends T> clazz) {
		T result=null;
		if(clazz.isInstance(commandDocument)) {
			result=clazz.cast(commandDocument);
		}
		return result;
	}
	
}