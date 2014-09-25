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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;


public final class CommandDescriptionUtil {
	
	private static final String COULD_NOT_CREATE_UTILITY_CLASS_FOR_COMMAND_API = "Could not create utility class for Command API";
	private static final String COULD_NOT_UNMARSHALL_RAW_COMMAND = "Could not unmarshall raw command";
	private static final String COULD_NOT_MARSHALL_COMMAND = "Could not marshall command";
	private final Unmarshaller unmarshaller;
	private final Marshaller marshaller;

	private CommandDescriptionUtil(
			Unmarshaller unmarshaller,
			Marshaller marshaller) {
		this.unmarshaller = unmarshaller;
		this.marshaller = marshaller;
	}

	public String toString(CommandDescription command) {
		try {
			StringWriter writer = new StringWriter();
			marshaller.marshal(command.as(Object.class),writer);
			return writer.toString();
		} catch (MarshalException e) {
			throw new IllegalStateException(COULD_NOT_MARSHALL_COMMAND,e);
		} catch (JAXBException e) {
			throw new IllegalStateException(COULD_NOT_MARSHALL_COMMAND,e);
		}
	}
	
	public CommandDescription fromString(String rawCommand) throws InvalidCommandException {
		try {
			Object commandDocument = 
				unmarshaller.
					unmarshal(new StreamSource(new StringReader(rawCommand)));
			return CommandDescription.newInstance(commandDocument);
		} catch (UnmarshalException e) {
			throw new IllegalStateException(COULD_NOT_UNMARSHALL_RAW_COMMAND,e);
		} catch (JAXBException e) {
			throw new IllegalStateException(COULD_NOT_UNMARSHALL_RAW_COMMAND,e);
		}
	}
	
	public static CommandDescriptionUtil newInstance() {
		try {
			JAXBContext context= 
				JAXBContext.newInstance("org.ldp4j.server.commands.xml");
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
			return 
				new CommandDescriptionUtil(
					context.createUnmarshaller(),
					marshaller);
		} catch (JAXBException e) {
			throw new IllegalStateException(COULD_NOT_CREATE_UTILITY_CLASS_FOR_COMMAND_API,e);
		}
	}
	
}