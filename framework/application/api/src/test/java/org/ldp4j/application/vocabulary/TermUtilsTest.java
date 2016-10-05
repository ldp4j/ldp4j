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
package org.ldp4j.application.vocabulary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;

import com.google.common.collect.ImmutableMap;

public class TermUtilsTest {

	private final Map<String, String> termMappings=
			ImmutableMap.
				<String,String>builder().
					put("v","V").
					put("va","VA").
					put("vA","V_A").
					put("Va","VA").
					put("VA","VA").
					put("valid","VALID").
					put("valiD","VALI_D").
					put("valID","VAL_ID").
					put("Valid","VALID").
					put("VaLID","VA_LID").
					put("vALid","V_A_LID").
					put("vALId","V_AL_ID").
					put("VALid","VA_LID").
					put("VALId","VAL_ID").
					put("vALID","V_ALID").
					put("VALID","VALID").
					put("validComposite","VALID_COMPOSITE").
					build();

	@Test
	public void verifyIsUtilityClass() {
		assertThat(Utils.isUtilityClass(TermUtils.class),equalTo(true));
	}

	@Test
	public void testToTermName$valid() throws Exception {
		for(Entry<String, String> entry:termMappings.entrySet()) {
			assertThat(entry.getKey()+"-->"+entry.getValue(),TermUtils.toTermName(entry.getKey()),equalTo(entry.getValue()));
		}
	}

	@Test
	public void testToTermName$invalid() throws Exception {
		try {
			TermUtils.toTermName("invalid name");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Object 'invalid name' is not a valid entity name"));
		}
	}

	@Test
	public void testIsValidTermName$invalid() throws Exception {
		assertThat(TermUtils.isValidTermName("invalid name"),equalTo(false));
	}

	@Test
	public void testIsValidTermName$null() throws Exception {
		assertThat(TermUtils.isValidTermName(null),equalTo(false));
	}

	@Test
	public void testIsValidEntityName$invalid() throws Exception {
		assertThat(TermUtils.isValidEntityName("invalid name"),equalTo(false));
	}

	@Test
	public void testIsValidEntityName$null() throws Exception {
		assertThat(TermUtils.isValidEntityName(null),equalTo(false));
	}

}
