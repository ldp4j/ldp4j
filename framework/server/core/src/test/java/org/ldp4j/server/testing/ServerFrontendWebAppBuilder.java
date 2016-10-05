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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.testing;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerFrontendWebAppBuilder {

	public static enum Parameter {

		ResourceParameter(null, null) {
			@Override
			protected String formatValue(String value) {
				return PACKAGE_PATH.concat(value);
			}

		},
		SETTINGS(ResourceParameter,"settings.xml"),
		POM(ResourceParameter,"pom.xml"),
		WEB_XML(ResourceParameter,"web.xml"),
		BEANS(ResourceParameter,"beans.xml"),
		LiteralParameter(null, null) {
		},
		DEPLOYABLE_NAME(LiteralParameter,"ldp-application.war"),
		CONTROL_PHRASE(LiteralParameter,"LDP Application"),
		BooleanParameter(null, null) {
		},
		;

		private final String value;
		private final Parameter parameter;

		private Parameter(Parameter parameter, String resourceName) {
			this.parameter = parameter;
			this.value = resourceName;
		}

		public boolean isLiteral() {
			return LiteralParameter.equals(parameter);
		}

		public boolean isResource() {
			return ResourceParameter.equals(parameter);
		}

		public String getValue() {
			String result=value;
			if(parameter!=null) {
				result=parameter.formatValue(value);
			}
			return result;
		}

		protected String formatValue(String value) {
			return value;
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ServerFrontendWebAppBuilder.class);

	private final Map<Parameter,String> parameters=new HashMap<Parameter, String>();

	private static final String PACKAGE_PATH;

	static {
		PACKAGE_PATH = ServerFrontendWebAppBuilder.class.getPackage().getName().replace(".", "/").concat("/");
	}

	private void updateParameter(Parameter parameter, String value) {
		if(value!=null) {
			String tValue = value.trim();
			if(!tValue.isEmpty()) {
				parameters.put(parameter, tValue);
			}
		}
	}

	private String retrieveParameter(Parameter parameter) {
		String result=parameters.get(parameter);
		if(result==null) {
			result=parameter.getValue();
		}
		return result;
	}

	/**
	 * @return the settings
	 */
	public String getSettings() {
		return retrieveParameter(Parameter.SETTINGS);
	}

	/**
	 * @return the pom
	 */
	public String getPom() {
		return retrieveParameter(Parameter.POM);
	}

	/**
	 * @return the webXml
	 */
	public String getWebXml() {
		return retrieveParameter(Parameter.WEB_XML);
	}

	/**
	 * @return the beans
	 */
	public String getBeans() {
		return retrieveParameter(Parameter.BEANS);
	}

	/**
	 * @return the deployableName
	 */
	public String getDeployableName() {
		return retrieveParameter(Parameter.DEPLOYABLE_NAME);
	}

	/**
	 * @return the controlPhrase
	 */
	public String getControlPhrase() {
		return retrieveParameter(Parameter.CONTROL_PHRASE);
	}

	/**
	 * @param settings the settings to set
	 */
	public ServerFrontendWebAppBuilder withSettings(String settings) {
		updateParameter(Parameter.SETTINGS,settings);
		return this;
	}

	/**
	 * @param pom the pom to set
	 */
	public ServerFrontendWebAppBuilder withPom(String pom) {
		updateParameter(Parameter.POM,pom);
		return this;
	}

	/**
	 * @param beans the Spring configuration file to set
	 */
	public ServerFrontendWebAppBuilder withBeans(String beans) {
		updateParameter(Parameter.BEANS,beans);
		return this;
	}


	/**
	 * @param webXml the webXml to set
	 */
	public ServerFrontendWebAppBuilder withWebXml(String webXml) {
		updateParameter(Parameter.WEB_XML,webXml);
		return this;
	}


	/**
	 * @param deployableName the deployableName to set
	 */
	public ServerFrontendWebAppBuilder withDeployableName(String deployableName) {
		updateParameter(Parameter.DEPLOYABLE_NAME,deployableName);
		return this;
	}


	/**
	 * @param controlPhrase the controlPhrase to set
	 */
	public ServerFrontendWebAppBuilder withControlPhrase(String controlPhrase) {
		updateParameter(Parameter.CONTROL_PHRASE,controlPhrase);
		return this;
	}

	public WebArchive build(JavaArchive... archives) {
		if(LOGGER.isDebugEnabled() &&
			Boolean.parseBoolean(System.getProperty("org.ldp4j.testing.logging.setup"))) {
			LOGGER.debug("Creating testing web application archive:");
			LOGGER.debug("- Maven configuration:");
			LOGGER.debug("  + Setting: "+getSettings());
			LOGGER.debug("  + POM....: "+getPom());
			LOGGER.debug("- Web application configuration:");
			LOGGER.debug("  + Descriptor.....: "+getWebXml());
			LOGGER.debug("  + Deployable name: "+getDeployableName());
			LOGGER.debug("  + Control phrase.: "+getControlPhrase());
			if(archives.length>0) {
				LOGGER.debug("- Custom libraries:");
				for(JavaArchive archive:archives) {
					LOGGER.debug("  + "+archive);
					if(LOGGER.isTraceEnabled() &&
						Boolean.parseBoolean(System.getProperty("org.ldp4j.testing.logging.setup")) &&
						Boolean.parseBoolean(System.getProperty("org.ldp4j.testing.logging.archive"))) {
						LOGGER.trace(archive.toString(true));
					}
				}
			}
		}

		MavenResolverSystem resolver = Maven.
			configureResolver().fromClassloaderResource(getSettings());
		PomEquippedResolveStage mavenResolver=
			resolver.loadPomFromClassLoaderResource(getPom());

		WebArchive war=
			ShrinkWrap.
				create(WebArchive.class, getDeployableName()).
				addAsLibraries(
					mavenResolver.
						importRuntimeDependencies().asFile()).
				addAsLibraries(archives).
				addAsWebResource(
					new StringAsset(getControlPhrase()),"index.html");

		updateWebInf(war);

		if(LOGGER.isTraceEnabled() &&
			Boolean.parseBoolean(System.getProperty("org.ldp4j.testing.logging.setup")) &&
			Boolean.parseBoolean(System.getProperty("org.ldp4j.testing.logging.archive"))) {
			LOGGER.trace(String.format("Testing web application archive: \n%s",war.toString(true)));
		}

		return war;
	}

	private void updateWebInf(WebArchive war) {
		addWebInf(war, getWebXml());
		addWebInf(war, getBeans());
	}

	private void addWebInf(WebArchive war, String source) {
		File file = new File(source);
		if(file.canRead() && file.isFile()) {
			war.addAsWebInfResource(file);
		} else {
			war.addAsWebInfResource(source);
		}
	}

}