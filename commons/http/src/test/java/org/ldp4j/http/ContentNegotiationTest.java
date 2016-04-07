package org.ldp4j.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;


public class ContentNegotiationTest {

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(ContentNegotiation.class),equalTo(true));
	}

}
