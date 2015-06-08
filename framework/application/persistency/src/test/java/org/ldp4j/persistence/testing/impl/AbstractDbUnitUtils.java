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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatDtdWriter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.ldp4j.persistence.testing.IDbUnitUtils;
import org.ldp4j.persistence.testing.Population;
import org.ldp4j.persistence.testing.Population.Replacement;
import org.ldp4j.persistence.testing.PopulationException;
import org.ldp4j.persistence.testing.ScriptExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractDbUnitUtils implements IDbUnitUtils {

	private interface DatabaseJob<T,E extends Exception> {

		T execute(IDatabaseConnection connection) throws DatabaseUnitException,SQLException, E;

	}

	private static final class ResetOnCloseInputStream extends InputStream {

		private final InputStream decorated;

		public ResetOnCloseInputStream(InputStream anInputStream) {
			if (!anInputStream.markSupported()) {
				throw new IllegalArgumentException("marking not supported");
			}
			anInputStream.mark(Integer.MAX_VALUE);
			decorated = anInputStream;
		}

		@Override
		public void close() throws IOException {
			decorated.reset();
		}

		@Override
		public int read() throws IOException {
			return decorated.read();
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(AbstractDbUnitUtils.class);

	protected abstract DataSource getDataSource();

	private <T,E extends Exception> T executeJob(DatabaseJob<T,E> job) throws E {
		Connection conn=null;
		try {
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Opening JDBC database connection...");
			}
			conn=getDataSource().getConnection();
			IDatabaseConnection connection=null;
			try {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Opening DbUnit database connection...");
				}
				connection = new DatabaseConnection(conn);

				// Setup DBUnit the HSQLDB data type factory
				DatabaseConfig config = connection.getConfig();
				config.setProperty(
					DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
					new HsqldbDataTypeFactory());

				try {
					return job.execute(connection);
				} catch (DatabaseUnitException e) {
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("Database operation failed: "+e.getMessage());
						LOGGER.trace("Full stacktrace follows: ",e);
					}
					throw new DatabaseOperationException("Database operation failed",e);
				} catch (SQLException e) {
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("Database operation failed: "+e.getMessage());
						LOGGER.trace("Full stacktrace follows: ",e);
					}
					throw new DatabaseOperationException("Database operation failed",e);

				}
			} catch (DatabaseUnitException e) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Could not create a DbUnit database connection: "+e.getMessage());
					LOGGER.trace("Full stacktrace follows: ",e);
				}
				throw new DatabaseOperationException("Could not create a DbUnit database connection.",e);
			} finally {
				if(connection!=null) {
					try {
						if(LOGGER.isTraceEnabled()) {
							LOGGER.trace("Closing DbUnit database connection...");
						}
						connection.close();
						if(LOGGER.isTraceEnabled()) {
							LOGGER.trace("DbUnit database connection closed.");
						}
					} catch (SQLException e) {
						LOGGER.error("Could not close DbUnit connection. Full stacktrace follows.",e);
					}
				}
			}
		} catch (SQLException e) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Could not create a JDBC database connection: "+e.getMessage());
				LOGGER.trace("Full stacktrace follows: ",e);
			}
			throw new DatabaseOperationException("Could not create a JDBC database connection.",e);
		} finally {
			if(conn!=null) {
				try {
					if(LOGGER.isTraceEnabled()) {
						LOGGER.trace("Closing JDBC database connection...");
					}
					conn.close();
					if(LOGGER.isTraceEnabled()) {
						LOGGER.trace("JDBC database connection closed.");
					}
				} catch (SQLException e) {
					LOGGER.error("Could not close JDBC connection. Full stacktrace follows.",e);
				}
			}
		}
	}

	private void populateData(IDatabaseConnection connection, InputStream is, InputStream dis, Population population) throws SQLException, PopulationException, DatabaseUnitException {
		FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
		if(dis!=null) {
			try {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Loading DTD '{}'...",population.getDTD());
				}
				builder.setMetaDataSetFromDtd(dis);
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("DTD loaded.");
				}
			} catch (IOException e) {
				LOGGER.error("Could not load DTD '"+population.getDTD()+"'. Full stacktrace follows.",e);
				throw new PopulationException("Could not load DTD '"+population.getDTD()+"'",e);
			}
		}

		IDataSet dataSet = builder.build(is);
		List<Replacement> replacements=population.getReplacements();
		if(replacements!=null) {
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Replacing dynamic values...");
			}
			ReplacementDataSet rds=new ReplacementDataSet(dataSet);
			rds.setStrictReplacement(true);
			for(Replacement replacement:replacements) {
				rds.addReplacementObject(replacement.getOldValue(),replacement.getNewValue());
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace(" - {}",replacement);
				}
			}
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("Dynamic values replaced.");
			}
			dataSet=rds;
		}
		// Insert the data
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Triggering data insertion...");
		}
		DatabaseOperation.INSERT.execute(connection, dataSet);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Data insertion completed.");
		}
	}

	private void executeScript(InputStream is) throws ScriptExecutionException {
		DataSource dataSource=getDataSource();

		try {
			String query = IOUtils.toString(is);
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace("  * SQL script:");
				LOGGER.trace(query);
			}

			JdbcTemplate template = new JdbcTemplate(dataSource);
			try {
				int result=template.update(query);
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("- Rows affected: "+result);
				}
			} catch (DataAccessException e) {
				throw new ScriptExecutionException("The SQL script could not be executed.",e);
			}
		} catch (IOException e) {
			throw new ScriptExecutionException("Could not load SQL script.",e);
		}
	}

	@Override
	public void executeScript(File dataResource) throws ScriptExecutionException {
		try {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Executing script '"+dataResource.getAbsolutePath()+"'...");
			}
			executeScript(new FileInputStream(dataResource));
		} catch (FileNotFoundException e) {
			throw new ScriptExecutionException("Invalid input file.",e);
		}
	}

	@Override
	public void executeScript(URL dataResource) throws ScriptExecutionException {
		try {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Executing script '"+dataResource+"'...");
			}
			executeScript(dataResource.openStream());
		} catch (IOException e) {
			throw new ScriptExecutionException("Invalid input URL.",e);
		}
	}

	@Override
	public void populateData(final Population population) throws PopulationException {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Populating '"+population+"'...");
		}
		final AtomicReference<InputStream> dis = new AtomicReference<InputStream>();
		try {
			if(population.getDTD()!=null) {
				dis.set(new ResetOnCloseInputStream(population.getDTD().openStream()));
			}
			executeJob(
				new DatabaseJob<Void,PopulationException>() {
					@Override
					public Void execute(IDatabaseConnection connection) throws SQLException, PopulationException, DatabaseUnitException {
						for(URL source:population.getDatasets()) {
							InputStream sis = null;
							try {
								if(LOGGER.isTraceEnabled()) {
									LOGGER.trace("Loading dataset '{}'...",source);
								}
								sis = source.openStream();
								populateData(connection,sis,dis.get(),population);
								if(LOGGER.isTraceEnabled()) {
									LOGGER.trace("Dataset '{}' loaded.",source);
								}
							} catch (IOException e) {
								LOGGER.error("Could not open dataset '"+source+"'. Full stacktrace follows.",e);
								throw new PopulationException("Could not open dataset '"+source+"'",e);
							} finally {
								IOUtils.closeQuietly(sis);
							}
						}
						return null;
					}
				}
			);
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Population completed.");
			}
		} catch (DatabaseOperationException e) {
			LOGGER.error("Population failed. Full stacktrace follows.",e);
			throw new PopulationException("Could not populate data due to a database failure",e);
		} catch (IOException e) {
			LOGGER.error("Population failed. Full stacktrace follows.",e);
			throw new PopulationException("Could not open DTD '"+population.getDTD()+"'",e);
		} finally {
			IOUtils.closeQuietly(dis.get());
		}
	}

	@Override
	public String exportDTD() throws IOException {
		try {
			return executeJob(
				new DatabaseJob<String,RuntimeException>() {
					@Override
					public String execute(IDatabaseConnection connection) throws SQLException, DataSetException {
						IDataSet dataSet = connection.createDataSet();
						StringWriter str = new StringWriter();
						FlatDtdWriter datasetWriter = new FlatDtdWriter(str);
						datasetWriter.setContentModel(FlatDtdWriter.CHOICE);
						datasetWriter.write(dataSet);
						return str.toString();
					}
				}
			);
		} catch (DatabaseOperationException e) {
			throw new IOException("Could not export DTD due to a database failure",e);
		}
	}

	@Override
	public String exportData() throws IOException {
		try {
			return executeJob(
				new DatabaseJob<String,IOException>() {
					@Override
					public String execute(IDatabaseConnection connection) throws SQLException, DataSetException, IOException {
						IDataSet fullDataSet = connection.createDataSet();
						StringWriter str = new StringWriter();
						FlatXmlDataSet.write(fullDataSet, str);
						return str.toString();
					}
				}
			);
		} catch (DatabaseOperationException e) {
			throw new IOException("Could not export data due to a database failure",e);
		}
	}

}