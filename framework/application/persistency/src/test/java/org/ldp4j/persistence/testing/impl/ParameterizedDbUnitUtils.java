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
package org.ldp4j.persistence.testing.impl;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


public class ParameterizedDbUnitUtils extends AbstractDbUnitUtils {

	private static final Logger LOGGER=LoggerFactory.getLogger(ParameterizedDbUnitUtils.class);

	private final String driverName;
	private final String databaseURL;

	private String userName;
	private String password;
	private DriverManagerDataSource dataSource;

	private String validateParameter(String value,
									 String parameterName,
									 String parameterDescription) {
		if(value==null) {
			throw new IllegalArgumentException("Object '"+parameterName+"' cannot be null.");
		}
		String tDatabaseURL=value.trim();
		if(tDatabaseURL.isEmpty()) {
			throw new IllegalArgumentException(parameterDescription+" cannot be empty.");
		}
		return tDatabaseURL;
	}

	public ParameterizedDbUnitUtils(String driverName, String databaseURL) {
		this.driverName =validateParameter(driverName,"driverName","Driver name");
		this.databaseURL=validateParameter(databaseURL,"databaseURL","Database URL");
	}

	public void setConnectionDetails(String userName, String password) {
		String tUserName = validateParameter(userName,"userName","User name");
		if(password==null) {
			throw new IllegalArgumentException("Object '"+"password"+"' cannot be null.");
		}
		this.userName=tUserName;
		this.password=password.trim();
		dataSource=null;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	@Override
	protected DataSource getDataSource() {
		if(dataSource==null) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Creating data source for driver class '"+driverName+"' to database '"+databaseURL+"'...");
			}
			dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName(driverName);
			dataSource.setUrl(databaseURL);
			if(userName!=null) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Connecting as '"+userName+"'...");
				}
				dataSource.setUsername(userName);
			}
			if(password!=null) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Using password '"+password+"'...");
				}
				dataSource.setPassword(password);
			}
		}
		return dataSource;
	}

}
