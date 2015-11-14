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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-kernel-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.session;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.ldp4j.application.session.WriteSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.io.Files;

public final class JournalingService {

	private static final Logger LOGGER=LoggerFactory.getLogger(JournalingService.class);

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd.HHmmss");

	private static final JournalingService SINGLETON=new JournalingService();

	private static final ThreadLocal<JournalManager> CURRENT_JOURNAL_MANAGER=new ThreadLocal<JournalManager>();

	private final Map<SessionId,JournalManager> journals;

	private final File defaultWorkingDirectory;

	private JournalManagerFactory factory;

	private File workingDirectory;

	private JournalingService() {
		this.journals=Maps.newLinkedHashMap();
		this.factory=new NullJournalManagerFactory();
		this.defaultWorkingDirectory = Files.createTempDir();
		this.workingDirectory=this.defaultWorkingDirectory;
	}

	private Journal getNotNull(JournalManager manager, SessionId sessionId) {
		return
			manager!=null?
				manager.journal():
				new NullJournal(sessionId);
	}

	private void sanitize(Journal journal) {
		if(journal.size()==0) {
			return;
		}
		logJournal(journal);
		persistJournal(journal);
	}

	private void persistJournal(Journal journal) {
		File journalFile=new File(this.workingDirectory,"journal."+DATE_FORMAT.format(new Date())+"."+Integer.toHexString(journal.sessionId().hash())+".log");
		try (FileWriter writer=new FileWriter(journalFile)) {
			for(Record record:journal) {
				writer.write(record.toString());
				writer.write(System.lineSeparator());
			}
			LOGGER.trace("Persisted journal for session {} in {}",journal.sessionId(),journalFile.getAbsolutePath());
		} catch (IOException e) {
			LOGGER.warn("Could not persist journal for session {} in {}",journal.sessionId(),journalFile.getAbsolutePath(),e);
		}
	}

	private void logJournal(Journal journal) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Session activity ({}) { ",journal.sessionId());
			for(Record record:journal) {
				LOGGER.trace(" - {}",record);
			}
			LOGGER.trace("}");
		}
	}

	Journaler journaler() {
		JournalManager manager=CURRENT_JOURNAL_MANAGER.get();
		Journaler result;
		if(manager!=null) {
			result=manager.journaler();
		} else {
			result=new NullJournaler();
		}
		return result;
	}

	void createJournal(WriteSession session) {
		JournalManager manager = this.factory.create(SessionId.create(session));
		CURRENT_JOURNAL_MANAGER.set(manager);
		this.journals.put(manager.sessionId(),manager);
	}

	public JournalingService setJournalManagerFactory(JournalManagerFactory factory) {
		if(factory!=null) {
			this.factory=factory;
		} else {
			this.factory=new NullJournalManagerFactory();
		}
		return this;
	}

	public JournalingService setWorkingDirectory(File workingDirectory) {
		if(workingDirectory==null) {
			this.workingDirectory=defaultWorkingDirectory;
		} else {
			this.workingDirectory=workingDirectory;
		}
		return this;
	}

	public Journal journal() {
		return getNotNull(CURRENT_JOURNAL_MANAGER.get(),null);
	}

	public void disposeJournal() {
		JournalManager manager = CURRENT_JOURNAL_MANAGER.get();
		if(manager==null) {
			return;
		}
		CURRENT_JOURNAL_MANAGER.remove();
		this.journals.remove(manager.sessionId());
		sanitize(manager.journal());
	}

	public static JournalingService getInstance() {
		return SINGLETON;
	}

}
