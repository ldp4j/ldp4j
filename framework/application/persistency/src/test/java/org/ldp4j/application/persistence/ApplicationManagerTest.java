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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-persistency:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-persistency-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ldp4j.application.persistence.domain.Application;
import org.ldp4j.application.persistence.domain.BasicContainerTemplate;
import org.ldp4j.application.persistence.domain.Container;
import org.ldp4j.application.persistence.domain.MembershipAwareContainer;
import org.ldp4j.application.persistence.domain.RDFSource;
import org.ldp4j.application.persistence.domain.RDFSourceTemplate;
import org.ldp4j.application.persistence.domain.Resource;
import org.ldp4j.application.persistence.domain.Template;
import org.ldp4j.application.persistence.example.Address;
import org.ldp4j.application.persistence.example.Book;
import org.ldp4j.application.persistence.example.Contract;
import org.ldp4j.application.persistence.example.Library;
import org.ldp4j.application.persistence.example.Person;
import org.ldp4j.application.persistence.example.Position;
import org.ldp4j.persistence.testing.DbUnitUtilsFactory;
import org.ldp4j.persistence.testing.IDbUnitUtils;
import org.ldp4j.persistence.testing.Population;
import org.ldp4j.persistence.testing.impl.PopulationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class ApplicationManagerTest {

	private static final String RID_1_BK = "My Resource Key";
	private static final long   RID_2_BK = 33L;

	private static final String RID_31_BK = "bcRSKey";
	private static final String RID_32_BK = "bcBCKey";
	private static final String RID_33_BK = "bcDCKey";
	private static final String RID_34_BK = "bcICKey";


	private static final Logger LOGGER=LoggerFactory.getLogger(ApplicationManagerTest.class);

	private static final String JDBC_DRIVER   = "javax.persistence.jdbc.driver";
	private static final String JDBC_URL      = "javax.persistence.jdbc.url";
	private static final String JDBC_USER     = "javax.persistence.jdbc.user";
	private static final String JDBC_PASSWORD = "javax.persistence.jdbc.password";

	private static final String SCHEMA_GENERATION_DROP_TARGET    = "javax.persistence.schema-generation.scripts.drop-target";
	private static final String SCHEMA_GENERATION_CREATE_TARGET  = "javax.persistence.schema-generation.scripts.create-target";
	private static final String SCHEMA_GENERATION_SCRIPTS_ACTION = "javax.persistence.schema-generation.scripts.action";

	public static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
	public static final String HSQLDB_URL = "jdbc:hsqldb:mem:dbunit";
	public static final String HSQLDB_USER = "sa";
	public static final String HSQLDB_PASSWORD = "";

	private static File create;
	private static File drop;

	private static EntityManagerFactory factory;
	private static PersistencyFacade persistencyFacade;

	private static final IDbUnitUtils dbUtils;

//
//	@Rule
//	public static TemporaryFolder TEMP_FOLDER = new TemporaryFolder();

	static {
		dbUtils=
			DbUnitUtilsFactory.createDbUtils(HSQLDB_DRIVER,
											 HSQLDB_URL,
											 HSQLDB_USER,
											 HSQLDB_PASSWORD);
	}

	private static URL loadResource(String resourceName) {
		return ClassLoader.getSystemResource(resourceName);
	}

	@BeforeClass
	public static void startUp() throws Exception {
		create = File.createTempFile("create",".ddl");
		drop = File.createTempFile("drop",".ddl");

		ImmutableMap<String, String> properties =
			ImmutableMap.
				<String,String>builder().
					put(JDBC_DRIVER, HSQLDB_DRIVER).
					put(JDBC_URL, HSQLDB_URL).
					put(JDBC_USER, HSQLDB_USER).
					put(JDBC_PASSWORD, HSQLDB_PASSWORD).
					put(SCHEMA_GENERATION_SCRIPTS_ACTION, "drop-and-create").
					put(SCHEMA_GENERATION_CREATE_TARGET, create.getAbsolutePath()).
					put(SCHEMA_GENERATION_DROP_TARGET, drop.getAbsolutePath()).
					build();

//		Persistence.generateSchema("jpaPersistency", properties);

//		dbUtils.executeScript(drop.toURI().toURL());
//		dbUtils.executeScript(create.toURI().toURL());

		factory = Persistence.createEntityManagerFactory("jpaPersistency",properties);

		Population population =
			PopulationBuilder.
				create().
					withDTD(loadResource("database-schema.dtd")).
					withDataset(loadResource("custom-application.xml")).
					withDataset(loadResource("custom-root-resources.xml")).
					withDataset(loadResource("custom-member-resources.xml")).
					withDataset(loadResource("deletable-application.xml")).
					withDataset(loadResource("deletable-root-resources.xml")).
					withDataset(loadResource("deletable-member-resources.xml")).
					replacingValue("containerKey").
						withSerializationOfValue(RID_2_BK).
					replacingValue("resourceKey").
						withSerializationOfValue(RID_1_BK).
					replacingValue(RID_31_BK).
						withSerializationOfValue(RID_31_BK).
					replacingValue(RID_32_BK).
						withSerializationOfValue(RID_32_BK).
					replacingValue(RID_33_BK).
						withSerializationOfValue(RID_33_BK).
					replacingValue(RID_34_BK).
						withSerializationOfValue(RID_34_BK).
					build();

		dbUtils.populateData(population);

		persistencyFacade = new PersistencyFacade(factory);
	}

	@AfterClass
	public static void shutDown() throws Exception {
//		LOGGER.debug("-- Shutting down:");
//		LOGGER.debug(dbUtils.exportData());
		if(factory!=null) {
			factory.close();
		}
//		dbUtils.executeScript(drop.toURI().toURL());
		create.delete();
		drop.delete();
	}

	private ApplicationManager appManager;
	private ResourceManager resManager;

	@Before
	public void setUp() {
		this.appManager=persistencyFacade.getApplicationManager();
		this.resManager=persistencyFacade.getResourceManager();
	}

	@After
	public void tearDown() throws Exception {
		persistencyFacade.disposeManagers();
	}

	private Application defaultApplication() {
		return appManager.findApplication("path");
	}

	private Template simpleRDFSourceTemplate() {
		return appManager.findTemplateByHandler(defaultApplication(),"org.ldp4j.application.MyRDFSourceTemplate",Template.class);
	}

	private Template simpleBasicContainerTemplate() {
		return appManager.findTemplateByHandler(defaultApplication(),"org.ldp4j.application.MyBasicContainerTemplate",Template.class);
	}

	private Template simpleDirectContainerTemplate() {
		return appManager.findTemplateByHandler(defaultApplication(),"org.ldp4j.application.MyDirectContainerTemplate",Template.class);
	}

	private Template simpleIndirectContainerTemplate() {
		return appManager.findTemplateByHandler(defaultApplication(),"org.ldp4j.application.MyIndirectContainerTemplate",Template.class);
	}

	private Template template51() {
		return appManager.findTemplateByHandler(defaultApplication(),"org.ldp4j.application.MyAttachingRDFSourceTemplate",Template.class);
	}

	private Template template31() {
		return appManager.findTemplateByHandler(defaultApplication(),"org.ldp4j.application.bc.RDFSources",BasicContainerTemplate.class);
	}

	private Template template32() {
		return appManager.findTemplateByHandler(defaultApplication(),"org.ldp4j.application.bc.BasicContainers",BasicContainerTemplate.class);
	}

	private Template template33() {
		return appManager.findTemplateByHandler(defaultApplication(),"org.ldp4j.application.bc.DirectContainers",BasicContainerTemplate.class);
	}

	private Template template34() {
		return appManager.findTemplateByHandler(defaultApplication(),"org.ldp4j.application.bc.IndirectContainers",BasicContainerTemplate.class);
	}

	private Resource simpleMemberRDFSource() {
		return appManager.findResourceByBusinessKey(simpleRDFSourceTemplate(),RID_1_BK,Resource.class);
	}

	protected Container simpleRootBasicContainer() {
		return appManager.findResourceByBusinessKey(simpleBasicContainerTemplate(),RID_2_BK,Container.class);
	}

	private Container resource31() {
		return appManager.findResourceByBusinessKey(template31(),RID_31_BK,Container.class);
	}

	private Container resource32() {
		return appManager.findResourceByBusinessKey(template32(),RID_32_BK,Container.class);
	}

	private Container resource33() {
		return appManager.findResourceByBusinessKey(template33(),RID_33_BK,Container.class);
	}

	private Container resource34() {
		return appManager.findResourceByBusinessKey(template34(),RID_34_BK,Container.class);
	}

	private String entityTag(Object rawEntity) {
		return Integer.toHexString(rawEntity.hashCode());
	}

	private void createDynamicApplication(String appPath, String appName) throws Exception {
		LOGGER.debug("Creating example schema...");
		appManager.beginTransaction();
		try {
			Application application=appManager.createApplication(appPath,appName,null);
			TemplateManager temManager = appManager.getTemplateManager(application);

			RDFSourceTemplate personTemplate = temManager.createRDFSourceTemplate(Person.class, Person.NAME, null);
			RDFSourceTemplate addressTemplate = temManager.createRDFSourceTemplate(Address.class, Address.NAME, null);
			RDFSourceTemplate positionTemplate = temManager.createRDFSourceTemplate(Position.class, Position.NAME, null);
			RDFSourceTemplate bookTemplate = temManager.createRDFSourceTemplate(Book.class, Book.NAME, null);
			RDFSourceTemplate contractTemplate = temManager.createRDFSourceTemplate(Contract.class, Contract.NAME, null);
			BasicContainerTemplate publicationsTemplate = temManager.createBasicContainerTemplate(Library.class, Library.NAME, null,bookTemplate);
			temManager.attachTemplate(personTemplate, addressTemplate, Person.ADDRESS_ATTACHMENT, "address", null);
			temManager.attachTemplate(personTemplate, positionTemplate, Person.POSITION_ATTACHMENT, "position", null);
			temManager.attachTemplate(personTemplate, publicationsTemplate, Person.LIBRARY_ATTACHMENT, "library", null);
			temManager.attachTemplate(positionTemplate, addressTemplate,Position.BUSINESS_ADDRESS_ATTACHMENT, "business_address", null);
			temManager.attachTemplate(positionTemplate, contractTemplate, Position.CONTRACT_ATTACHMENT, "contract", null);
			temManager.attachTemplate(publicationsTemplate, addressTemplate, Library.LOCATION_ATTACHMENT, "location", null);
			temManager.commitTransaction();
		} catch(Exception e) {
			appManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	private void populaDynamicApplication(String appPath, String rootPath, String personBusinessKey, String addressBusinessKey, String positionBusinessKey, String libraryBusinessKey) throws Exception {
		LOGGER.debug("Populating example...");
		appManager.beginTransaction();
		try {
			Application application=appManager.findApplication(appPath);
			Template personTemplate = appManager.findTemplateByHandler(application,Person.class.getName(),Template.class);
			Resource rootPerson = resManager.createRootResource(personTemplate, rootPath,personBusinessKey,entityTag("person"), null);
			resManager.createAttachedResource(rootPerson,Person.ADDRESS_ATTACHMENT,addressBusinessKey,entityTag("address"));
			Resource rootPersonPosition= resManager.createAttachedResource(rootPerson,Person.POSITION_ATTACHMENT,positionBusinessKey,entityTag("position"));
			resManager.createAttachedResource(rootPersonPosition,Position.BUSINESS_ADDRESS_ATTACHMENT,"businessAddress",entityTag("businessAddress"));
			resManager.createAttachedResource(rootPersonPosition,Position.CONTRACT_ATTACHMENT,"contract",entityTag("contract"));
			Resource rootPersonLibrary= resManager.createAttachedResource(rootPerson,Person.LIBRARY_ATTACHMENT,libraryBusinessKey,entityTag(libraryBusinessKey));
			resManager.createAttachedResource(rootPersonLibrary,Library.LOCATION_ATTACHMENT,"location",entityTag("location"));
			resManager.createMemberResource((Container)rootPersonLibrary, "first_book",entityTag("first_book"),null,null);
			resManager.createMemberResource((Container)rootPersonLibrary, "second_book",entityTag("second_book"),null,null);
			appManager.commitTransaction();
		} catch(Exception e) {
			appManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateRootResource$RDFSource$regular() throws Exception {
		resManager.beginTransaction();
		try {
			String rootPath = "/root/rdfsource/path";
			LOGGER.debug("Before creating root resource:");
			LOGGER.debug("- Template: "+simpleRDFSourceTemplate());
			Resource resource = resManager.createRootResource(simpleRDFSourceTemplate(),rootPath,rootPath,entityTag(rootPath),null);
			LOGGER.debug("After creating root resource:");
			LOGGER.debug("- Created resource: "+resource);
			LOGGER.debug("  + Endpoint......: "+resource.getEndpoint());
			resManager.commitTransaction();
			assertThat(resource,instanceOf(RDFSource.class));
			assertThat(resource.getEndpoint().getApplication(),equalTo(defaultApplication()));
			assertThat(resource.getEndpoint().getPath(),equalTo(rootPath));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateRootResource$BasicContainer$regular() throws Exception {
		resManager.beginTransaction();
		try {
			String rootPath = "/root/basic_container/path";
			LOGGER.debug("Before creating root resource:");
			LOGGER.debug("- Template: "+simpleBasicContainerTemplate());
			Resource resource = resManager.createRootResource(simpleBasicContainerTemplate(),rootPath,rootPath,entityTag(rootPath),null);
			LOGGER.debug("After creating root resource:");
			LOGGER.debug("- Created resource: "+resource);
			LOGGER.debug("  + Endpoint......: "+resource.getEndpoint());
			resManager.commitTransaction();
			assertThat(resource,instanceOf(Container.class));
			assertThat(resource.getEndpoint().getApplication(),equalTo(defaultApplication()));
			assertThat(resource.getEndpoint().getPath(),equalTo(rootPath));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateRootResource$DirectContainer$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Application app = defaultApplication();
			Template template=simpleDirectContainerTemplate();
			Resource targetResource = simpleMemberRDFSource();
			String rootPath = "/root/direct_container/path";
			LOGGER.debug("Before creating root resource:");
			LOGGER.debug("- Template: "+template);
			Resource resource = resManager.createRootResource(template,rootPath,rootPath,entityTag(rootPath),targetResource);
			LOGGER.debug("After creating root resource:");
			LOGGER.debug("- Created resource: "+resource);
			LOGGER.debug("  + Endpoint......: "+resource.getEndpoint());
			resManager.commitTransaction();
			assertThat(resource.getEndpoint().getApplication(),equalTo(app));
			assertThat(resource.getEndpoint().getPath(),equalTo(rootPath));
			assertThat(resource,instanceOf(MembershipAwareContainer.class));
			MembershipAwareContainer container=(MembershipAwareContainer)resource;
			assertThat(container.getTargetResource(),equalTo(targetResource));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateRootResource$IndirectContainer$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Application app = defaultApplication();
			String rootPath = "/root/indirect_container/path";
			LOGGER.debug("Before creating root resource:");
			LOGGER.debug("- Template: "+simpleDirectContainerTemplate());
			Resource resource = resManager.createRootResource(simpleDirectContainerTemplate(),rootPath,rootPath,entityTag(rootPath),simpleMemberRDFSource());
			LOGGER.debug("After creating root resource:");
			LOGGER.debug("- Created resource: "+resource);
			LOGGER.debug("  + Endpoint......: "+resource.getEndpoint());
			resManager.commitTransaction();
			assertThat(resource,instanceOf(MembershipAwareContainer.class));
			assertThat(resource.getEndpoint().getApplication(),equalTo(app));
			assertThat(resource.getEndpoint().getPath(),equalTo(rootPath));
			MembershipAwareContainer container=(MembershipAwareContainer)resource;
			assertThat(container.getTargetResource(),equalTo(simpleMemberRDFSource()));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateAttachedResource$RDFSource$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Application app = defaultApplication();
			Template attachingTemplate=template51();
			Template attachedTemplate=simpleRDFSourceTemplate();
			String rootPath = "/attaching/rdfsource/rdfsource/path";
			Resource attachingResource=resManager.createRootResource(attachingTemplate,rootPath,rootPath,entityTag(rootPath),null);;
			LOGGER.debug("Before creating attached resource:");
			LOGGER.debug("- Parent resource: "+attachingResource);
			LOGGER.debug("  + Endpoint.....: "+attachingResource.getEndpoint());
			LOGGER.debug("  + Attachments..: "+attachingResource.getResourceAttachments());
			Resource attachedResource=resManager.createAttachedResource(attachingResource,"RDFSource","rdf_source",entityTag("rdf_source"));
			LOGGER.debug("After creating attached resource:");
			LOGGER.debug("- Parent resource attachments: "+attachingResource.getResourceAttachments());
			LOGGER.debug("- Created resource: "+attachedResource);
			LOGGER.debug("  + Endpoint......: "+attachedResource.getEndpoint());
			resManager.commitTransaction();
			assertThat(attachingResource,instanceOf(RDFSource.class));
			assertThat(attachingResource.getEndpoint().getApplication(),equalTo(app));
			assertThat(attachingResource.getEndpoint().getPath(),equalTo(rootPath));
			assertThat(attachedResource,instanceOf(RDFSource.class));
			assertThat(attachedResource.getDefinedBy(),equalTo(attachedTemplate));
			assertThat(attachedResource.getEndpoint().getApplication(),equalTo(app));
			assertThat(attachedResource.getEndpoint().getPath(),startsWith(rootPath));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateAttachedResource$BasicContainer$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Application app = defaultApplication();
			Template attachingTemplate=template51();
			Template attachedTemplate=simpleBasicContainerTemplate();
			String rootPath = "/attaching/rdfsource/basic_container/path";
			Resource attachingResource=resManager.createRootResource(attachingTemplate,rootPath,rootPath,entityTag(rootPath),null);;
			LOGGER.debug("Before creating attached resource:");
			LOGGER.debug("- Parent resource: "+attachingResource);
			LOGGER.debug("  + Endpoint.....: "+attachingResource.getEndpoint());
			LOGGER.debug("  + Attachments..: "+attachingResource.getResourceAttachments());
			Resource attachedResource=resManager.createAttachedResource(attachingResource,"BasicContainer","basic_container",entityTag("basic_container"));
			LOGGER.debug("After creating attached resource:");
			LOGGER.debug("- Parent resource attachments: "+attachingResource.getResourceAttachments());
			LOGGER.debug("- Created resource: "+attachedResource);
			LOGGER.debug("  + Endpoint......: "+attachedResource.getEndpoint());
			resManager.commitTransaction();
			assertThat(attachingResource,instanceOf(RDFSource.class));
			assertThat(attachingResource.getEndpoint().getApplication(),equalTo(app));
			assertThat(attachingResource.getEndpoint().getPath(),equalTo(rootPath));
			assertThat(attachedResource,instanceOf(Container.class));
			assertThat(attachedResource.getDefinedBy(),equalTo(attachedTemplate));
			assertThat(attachedResource.getEndpoint().getApplication(),equalTo(app));
			assertThat(attachedResource.getEndpoint().getPath(),startsWith(rootPath));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateAttachedResource$DirectContainer$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Application app = defaultApplication();
			Template attachingTemplate=template51();
			Template attachedTemplate=simpleDirectContainerTemplate();
			String rootPath = "/attaching/rdfsource/direct_container/path";
			Resource attachingResource=resManager.createRootResource(attachingTemplate,rootPath,rootPath,entityTag(rootPath),null);;
			LOGGER.debug("Before creating attached resource:");
			LOGGER.debug("- Parent resource: "+attachingResource);
			LOGGER.debug("  + Endpoint.....: "+attachingResource.getEndpoint());
			LOGGER.debug("  + Attachments..: "+attachingResource.getResourceAttachments());
			Resource attachedResource=resManager.createAttachedResource(attachingResource,"DirectContainer","direct_container",entityTag("direct_container"));
			LOGGER.debug("After creating attached resource:");
			LOGGER.debug("- Parent resource attachments: "+attachingResource.getResourceAttachments());
			LOGGER.debug("- Created resource: "+attachedResource);
			LOGGER.debug("  + Endpoint......: "+attachedResource.getEndpoint());
			resManager.commitTransaction();
			assertThat(attachingResource,instanceOf(RDFSource.class));
			assertThat(attachingResource.getEndpoint().getApplication(),equalTo(app));
			assertThat(attachingResource.getEndpoint().getPath(),equalTo(rootPath));
			assertThat(attachedResource,instanceOf(MembershipAwareContainer.class));
			assertThat(attachedResource.getDefinedBy(),equalTo(attachedTemplate));
			assertThat(attachedResource.getEndpoint().getApplication(),equalTo(app));
			assertThat(attachedResource.getEndpoint().getPath(),startsWith(rootPath));
			MembershipAwareContainer container=(MembershipAwareContainer)attachedResource;
			assertThat(container.getTargetResource(),equalTo(attachingResource));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateAttachedResource$IndirectContainer$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Application app = defaultApplication();
			Template attachingTemplate=template51();
			Template attachedTemplate=simpleIndirectContainerTemplate();
			String rootPath = "/attaching/rdfsource/indirect_container/path";
			Resource attachingResource=resManager.createRootResource(attachingTemplate,rootPath,rootPath,entityTag(rootPath),null);
			LOGGER.debug("Before creating attached resource:");
			LOGGER.debug("- Parent resource: "+attachingResource);
			LOGGER.debug("  + Endpoint.....: "+attachingResource.getEndpoint());
			LOGGER.debug("  + Attachments..: "+attachingResource.getResourceAttachments());
			Resource attachedResource=resManager.createAttachedResource(attachingResource,"IndirectContainer","indirect_container",entityTag("indirect_container"));
			LOGGER.debug("After creating attached resource:");
			LOGGER.debug("- Parent resource attachments: "+attachingResource.getResourceAttachments());
			LOGGER.debug("- Created resource: "+attachedResource);
			LOGGER.debug("  + Endpoint......: "+attachedResource.getEndpoint());
			resManager.commitTransaction();
			assertThat(attachingResource,instanceOf(RDFSource.class));
			assertThat(attachingResource.getEndpoint().getApplication(),equalTo(app));
			assertThat(attachingResource.getEndpoint().getPath(),equalTo(rootPath));
			assertThat(attachedResource,instanceOf(MembershipAwareContainer.class));
			assertThat(attachedResource.getDefinedBy(),equalTo(attachedTemplate));
			assertThat(attachedResource.getEndpoint().getApplication(),equalTo(app));
			assertThat(attachedResource.getEndpoint().getPath(),startsWith(rootPath));
			MembershipAwareContainer container=(MembershipAwareContainer)attachedResource;
			assertThat(container.getTargetResource(),equalTo(attachingResource));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateMemberResource$RDFSource$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Application app = defaultApplication();
			Container container=resource31();
			LOGGER.debug("Before member creation:");
			LOGGER.debug("- Original Container: "+container);
			LOGGER.debug("  + Endpoint........: "+container.getEndpoint());
			Resource member=resManager.createMemberResource(container,"bcRSMemberKey",entityTag("bcRSMemberKey"), null, null);
			LOGGER.debug("After member creation:");
			LOGGER.debug("- Updated container: "+container);
			LOGGER.debug("- Created member...: "+member);
			LOGGER.debug("  + Endpoint.......: "+member.getEndpoint());
			resManager.commitTransaction();
			assertThat(member,instanceOf(RDFSource.class));
			assertThat(member.getDefinedBy(),equalTo(container.getDefinedBy().getMemberTemplate()));
			assertThat(member.getEndpoint().getApplication(),equalTo(app));
			assertThat(member.getEndpoint().getPath(),startsWith(container.getEndpoint().getPath()));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateMemberResource$BasicContainer$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Application app = defaultApplication();
			Container container=resource32();
			LOGGER.debug("Before member creation:");
			LOGGER.debug("- Original Container: "+container);
			LOGGER.debug("  + Endpoint........: "+container.getEndpoint());
			Resource member=resManager.createMemberResource(container,"bcBCMemberKey",entityTag("bcBCMemberKey"), null, null);
			LOGGER.debug("After member creation:");
			LOGGER.debug("- Updated container: "+container);
			LOGGER.debug("- Created member...: "+member);
			LOGGER.debug("  + Endpoint.......: "+member.getEndpoint());
			resManager.commitTransaction();
			assertThat(member,instanceOf(Container.class));
			assertThat(member.getDefinedBy(),equalTo(container.getDefinedBy().getMemberTemplate()));
			assertThat(member.getEndpoint().getApplication(),equalTo(app));
			assertThat(member.getEndpoint().getPath(),startsWith(container.getEndpoint().getPath()));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateMemberResource$DirectContainer$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Application app = defaultApplication();
			Container container=resource33();
			LOGGER.debug("Before member creation:");
			LOGGER.debug("- Original Container: "+container);
			LOGGER.debug("  + Endpoint........: "+container.getEndpoint());
			Resource member=resManager.createMemberResource(container,"bcDCMemberKey",entityTag("bcDCMemberKey"), null, null);
			LOGGER.debug("After member creation:");
			LOGGER.debug("- Updated container: "+container);
			LOGGER.debug("- Created member...: "+member);
			LOGGER.debug("  + Endpoint.......: "+member.getEndpoint());
			resManager.commitTransaction();
			assertThat(member,instanceOf(MembershipAwareContainer.class));
			assertThat(member.getDefinedBy(),equalTo(container.getDefinedBy().getMemberTemplate()));
			assertThat(member.getEndpoint().getApplication(),equalTo(app));
			assertThat(member.getEndpoint().getPath(),startsWith(container.getEndpoint().getPath()));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCreateMemberResource$IndirectContainer$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Application app = defaultApplication();
			Container container=resource34();
			LOGGER.debug("Before member creation:");
			LOGGER.debug("- Original Container: "+container);
			LOGGER.debug("  + Endpoint........: "+container.getEndpoint());
			Resource member=resManager.createMemberResource(container,"bcICMemberKey",entityTag("bcICMemberKey"), null, null);
			LOGGER.debug("After member creation:");
			LOGGER.debug("- Updated container: "+container);
			LOGGER.debug("- Created member...: "+member);
			LOGGER.debug("  + Endpoint.......: "+member.getEndpoint());
			resManager.commitTransaction();
			assertThat(member,instanceOf(MembershipAwareContainer.class));
			assertThat(member.getDefinedBy(),equalTo(container.getDefinedBy().getMemberTemplate()));
			assertThat(member.getEndpoint().getApplication(),equalTo(app));
			assertThat(member.getEndpoint().getPath(),startsWith(container.getEndpoint().getPath()));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testDeleteResource$leafResource() throws Exception{
		createDynamicApplication("deleteLeaf", "Delete leaf app");
		populaDynamicApplication("deleteLeaf", "/dl/person", "person", "address", "position", "library");
		resManager.beginTransaction();
		try {
			assertThat(findResource("deleteLeaf",Address.class,"address"),notNullValue());
			resManager.deleteResource(findResource("deleteLeaf",Address.class,"address"));
			resManager.commitTransaction();
			assertThat(findResource("deleteLeaf",Address.class,"address"),nullValue());
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	public Resource findResource(String appPath, Class<?> templateClass, Serializable businessKey) {
		Application application = appManager.findApplication(appPath);
		Template template = appManager.findTemplateByHandler(application, templateClass.getName(), Template.class);
		return appManager.findResourceByBusinessKey(template,businessKey, Resource.class);
	}


	@Test
	public void testDeleteResource$resourceWithAttachments() throws Exception{
		createDynamicApplication("deleteResourceWithAttachments", "Delete resource with attachments");
		populaDynamicApplication("deleteResourceWithAttachments", "/dl/person", "person", "address", "position", "library");
		resManager.beginTransaction();
		try {
			assertThat(findResource("deleteResourceWithAttachments",Position.class,"position"),notNullValue());
			assertThat(findResource("deleteResourceWithAttachments",Contract.class,"contract"),notNullValue());
			assertThat(findResource("deleteResourceWithAttachments",Address.class,"businessAddress"),notNullValue());
			resManager.deleteResource(findResource("deleteResourceWithAttachments",Position.class,"position"));
			resManager.commitTransaction();
			assertThat(findResource("deleteResourceWithAttachments",Position.class,"position"),nullValue());
			assertThat(findResource("deleteResourceWithAttachments",Contract.class,"contract"),nullValue());
			assertThat(findResource("deleteResourceWithAttachments",Address.class,"businessAddress"),nullValue());
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testDeleteResource$containerWithMembers() throws Exception{
		createDynamicApplication("deleteContainerWithMembers", "Delete container with members");
		populaDynamicApplication("deleteContainerWithMembers", "/dl/person", "person", "address", "position", "library");
		resManager.beginTransaction();
		try {
			assertThat(findResource("deleteContainerWithMembers",Library.class,"library"),notNullValue());
			assertThat(findResource("deleteContainerWithMembers",Address.class,"location"),notNullValue());
			assertThat(findResource("deleteContainerWithMembers",Book.class,"first_book"),notNullValue());
			assertThat(findResource("deleteContainerWithMembers",Book.class,"second_book"),notNullValue());
			resManager.deleteResource(findResource("deleteContainerWithMembers",Library.class,"library"));
			resManager.commitTransaction();
			assertThat(findResource("deleteContainerWithMembers",Library.class,"library"),nullValue());
			assertThat(findResource("deleteContainerWithMembers",Address.class,"location"),nullValue());
			assertThat(findResource("deleteContainerWithMembers",Book.class,"first_book"),nullValue());
			assertThat(findResource("deleteContainerWithMembers",Book.class,"second_book"),nullValue());
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testDeleteResource$resourceGraph() throws Exception{
		createDynamicApplication("deleteResourceGraph", "Delete resource graph");
		populaDynamicApplication("deleteResourceGraph", "/dl/person", "person", "address", "position", "library");
		resManager.beginTransaction();
		try {
			assertThat(findResource("deleteResourceGraph",Person.class,"person"),notNullValue());
			assertThat(findResource("deleteResourceGraph",Address.class,"address"),notNullValue());
			assertThat(findResource("deleteResourceGraph",Position.class,"position"),notNullValue());
			assertThat(findResource("deleteResourceGraph",Contract.class,"contract"),notNullValue());
			assertThat(findResource("deleteResourceGraph",Address.class,"businessAddress"),notNullValue());
			assertThat(findResource("deleteResourceGraph",Library.class,"library"),notNullValue());
			assertThat(findResource("deleteResourceGraph",Address.class,"location"),notNullValue());
			assertThat(findResource("deleteResourceGraph",Book.class,"first_book"),notNullValue());
			assertThat(findResource("deleteResourceGraph",Book.class,"second_book"),notNullValue());
			resManager.deleteResource(findResource("deleteResourceGraph",Person.class,"person"));
			resManager.commitTransaction();
			assertThat(findResource("deleteResourceGraph",Person.class,"person"),nullValue());
			assertThat(findResource("deleteResourceGraph",Address.class,"address"),nullValue());
			assertThat(findResource("deleteResourceGraph",Position.class,"position"),nullValue());
			assertThat(findResource("deleteResourceGraph",Contract.class,"contract"),nullValue());
			assertThat(findResource("deleteResourceGraph",Address.class,"businessAddress"),nullValue());
			assertThat(findResource("deleteResourceGraph",Library.class,"library"),nullValue());
			assertThat(findResource("deleteResourceGraph",Address.class,"location"),nullValue());
			assertThat(findResource("deleteResourceGraph",Book.class,"first_book"),nullValue());
			assertThat(findResource("deleteResourceGraph",Book.class,"second_book"),nullValue());
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testDeleteApplicationResources() throws Exception{
		createDynamicApplication("clearApplication", "Clear application");
		populaDynamicApplication("clearApplication", "/dl/person", "person", "address", "position", "library");
		resManager.beginTransaction();
		try {
			assertThat(findResource("clearApplication",Person.class,"person"),notNullValue());
			assertThat(findResource("clearApplication",Address.class,"address"),notNullValue());
			assertThat(findResource("clearApplication",Position.class,"position"),notNullValue());
			assertThat(findResource("clearApplication",Contract.class,"contract"),notNullValue());
			assertThat(findResource("clearApplication",Address.class,"businessAddress"),notNullValue());
			assertThat(findResource("clearApplication",Library.class,"library"),notNullValue());
			assertThat(findResource("clearApplication",Address.class,"location"),notNullValue());
			assertThat(findResource("clearApplication",Book.class,"first_book"),notNullValue());
			assertThat(findResource("clearApplication",Book.class,"second_book"),notNullValue());
			appManager.deleteApplicationResources(appManager.findApplication("clearApplication"));
			resManager.commitTransaction();
			assertThat(findResource("clearApplication",Person.class,"person"),nullValue());
			assertThat(findResource("clearApplication",Address.class,"address"),nullValue());
			assertThat(findResource("clearApplication",Position.class,"position"),nullValue());
			assertThat(findResource("clearApplication",Contract.class,"contract"),nullValue());
			assertThat(findResource("clearApplication",Address.class,"businessAddress"),nullValue());
			assertThat(findResource("clearApplication",Library.class,"library"),nullValue());
			assertThat(findResource("clearApplication",Address.class,"location"),nullValue());
			assertThat(findResource("clearApplication",Book.class,"first_book"),nullValue());
			assertThat(findResource("clearApplication",Book.class,"second_book"),nullValue());
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testDeleteApplication() throws Exception{
		resManager.beginTransaction();
		try {
			String path = "deletable_path";
			Application delApp = appManager.findApplication(path);
			appManager.deleteApplication(delApp);
			resManager.commitTransaction();
			assertThat(appManager.findApplication(path),nullValue());
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testTouchResource$regular() throws Exception {
		resManager.beginTransaction();
		try {
			String newEtag = "another etag";
			Resource resource = simpleMemberRDFSource();
			Date prevModified = resource.getEndpoint().getModified();
			LOGGER.debug("Before resource touch:");
			LOGGER.debug("- Resource..: "+resource);
			LOGGER.debug("  + Endpoint: "+resource.getEndpoint());
			resManager.touchResource(resource,newEtag);
			LOGGER.debug("After resource touch:");
			LOGGER.debug("- Resource..: "+resource);
			LOGGER.debug("  + Endpoint: "+resource.getEndpoint());
			resManager.commitTransaction();
			assertThat(resource.getEndpoint().getEntityTag(),equalTo(newEtag));
			assertThat(resource.getEndpoint().getModified(),not(equalTo(prevModified)));
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testFindResourceByBusinessKey$regular() throws Exception {
		resManager.beginTransaction();
		try {
			Resource resource = appManager.findResourceByBusinessKey(simpleRDFSourceTemplate(),RID_1_BK, Resource.class);
			LOGGER.debug("Looking for resource with business key {}",RID_1_BK);
			LOGGER.debug("- Resource: {}",resource);
			assertThat(resource,notNullValue());
			assertThat(resource,instanceOf(RDFSource.class));
			assertThat(resource.getId(),equalTo(1L));
			resManager.commitTransaction();
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testFindResourceByBusinessKey$not_found() throws Exception {
		resManager.beginTransaction();
		try {
			Resource resource = appManager.findResourceByBusinessKey(simpleRDFSourceTemplate(),"123123123", Resource.class);
			assertThat(resource,nullValue());
			resManager.commitTransaction();
		} catch(Exception e) {
			resManager.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}
	}

}
