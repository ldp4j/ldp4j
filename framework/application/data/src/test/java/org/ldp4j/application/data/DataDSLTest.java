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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-data:0.2.2
 *   Bundle      : ldp4j-application-data-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.ldp4j.application.data.IndividualReference.external;
import static org.ldp4j.application.data.IndividualReference.managed;
import static org.ldp4j.application.data.IndividualReferenceBuilder.newReference;

import java.net.URI;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;
import org.ldp4j.application.vocabulary.LDP;
import org.ldp4j.application.vocabulary.RDF;
import org.ldp4j.commons.testing.Utils;

public class DataDSLTest extends DataTestingSupport {

	private static final String TEMPLATE_ID = "template";
	private static final String READ_ONLY_PROPERTY = "my:property";

	@Test
	public void verifyIsUtilityClass() {
		assertThat(Utils.isUtilityClass(DataDSL.class),equalTo(true));
	}

	@Test
	public void testDSL() {
		DataSet data=
		DataDSL.
		dataSet().
			individual(id("root")).
				hasProperty(predicate("dataTypePropertyWithMultipleValues")).
					withValue("1").
					withValue("2").
				hasLink(predicate("objectPropertyWithMultipleReferralsAfterDatatypeProperty")).
					referringTo(external(EXTERNAL)).
					referringTo(managed(RESOURCE_NAME,MANAGER_ID)).
				hasProperty(predicate("dataTypePropertyWithMultipleValuesAfterObjectPropertyWithMultipleReferralsAfterDatatypeProperty")).
					withValue("3").
					withValue("4").
				hasProperty(predicate("dataTypePropertyWithMultipleValuesAfterDataTypePropertyWithMultipleValuesAfterObjectPropertyWithMultipleReferralsAfterDatatypeProperty")).
					withValue("5").
					withValue("6").
				hasLink(predicate("toIndividualAfterReferring")).
					referringTo(external(EXTERNAL)).
					toIndividual(external(EXTERNAL)).
						hasLink(predicate("hasLink")).
							referringTo(managed(RESOURCE_NAME,MANAGER_ID)).
				hasLink(predicate("toIndividualWithoutReferring")).
					toIndividual(external(EXTERNAL)).
						hasLink(predicate("hasLink")).
							referringTo(managed(RESOURCE_NAME,MANAGER_ID)).
			build();
		System.out.println(data);

	}

	@Test
	public void testDataDSL() {
		Date date=new Date();
		Name<String> name = NamingScheme.getDefault().name("name");
		DataSet initialData = getInitialData(TEMPLATE_ID,name.id(),false,date);
		DataSetHelper helper=DataSetUtils.newHelper(initialData);
		DateTime firstValue=
			helper.
				managedIndividual(name,TEMPLATE_ID).
					property(READ_ONLY_PROPERTY).
						firstValue(DateTime.class);
		assertThat(firstValue,notNullValue());
		assertThat(TimeUtils.newInstance().from(firstValue).toDate(),equalTo(date));
	}

	private DataSet getInitialData(String templateId, String name, boolean markContainer, Date date) {
		DataSet initial=null;
		if(!markContainer) {
			initial=
				DataDSL.
					dataSet().
						individual(newReference().toManagedIndividual(templateId).named(name)).
							hasProperty(READ_ONLY_PROPERTY).
								withValue(date).
							build();
		} else {
			initial=
				DataDSL.
					dataSet().
						individual(newReference().toManagedIndividual(templateId).named(name)).
							hasProperty(READ_ONLY_PROPERTY).
								withValue(date).
							hasLink(RDF.TYPE.qualifiedEntityName()).
								referringTo(newReference().toExternalIndividual().atLocation(LDP.BASIC_CONTAINER.as(URI.class))).
							build();
		}
		return initial;
	}


}
