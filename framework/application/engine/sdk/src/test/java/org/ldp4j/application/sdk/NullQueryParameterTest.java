package org.ldp4j.application.sdk;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.ldp4j.application.ext.Parameter;

public class NullQueryParameterTest {

	private static final String PARAMETER_NAME = "parameter";

	private Parameter sut=NullQueryParameter.create(PARAMETER_NAME);

	@Test
	public void testName() throws Exception {
		assertThat(this.sut.name(),equalTo(PARAMETER_NAME));
	}

	@Test
	public void testIsMultivalued() throws Exception {
		assertThat(this.sut.isMultivalued(),equalTo(false));
	}

	@Test
	public void testRawValuesAs() throws Exception {
		assertThat(this.sut.rawValuesAs(Integer.class),hasSize(0));
	}

	@Test
	public void testRawValues() throws Exception {
		assertThat(this.sut.rawValues(),hasSize(0));
	}

	@Test
	public void testRawValueAs() throws Exception {
		assertThat(this.sut.rawValueAs(Integer.class),nullValue());
	}

	@Test
	public void testRawValue() throws Exception {
		assertThat(this.sut.rawValue(),nullValue());
	}

	@Test
	public void testCardinality() throws Exception {
		assertThat(this.sut.cardinality(),equalTo(0));
	}

}