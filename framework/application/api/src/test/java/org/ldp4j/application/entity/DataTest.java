package org.ldp4j.application.entity;

import org.junit.Test;
import org.ldp4j.application.entity.spi.DataSourceFactory;

public class DataTest {

	@Test
	public void testCreateDataSource() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	@Test
	public void testRegisterService() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	@Test
	public void testGetService() throws Exception {
		System.out.println(Data.getService(DataSourceFactory.class));
	}

}
