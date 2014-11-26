package org.ldp4j.application.config.core;

import java.util.Map;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.Setting;

public final class DefaultConfiguration extends BaseConfiguration {

	/**
	 *
	 */
	private static final long serialVersionUID = 4664675292338246252L;

	/**
	 * Create a configuration from the configuration.
	 */
	public DefaultConfiguration(Configuration config) {
		super(config);
	}

	/**
	 * Create a configuration from the given collection of settings.
	 */
	public DefaultConfiguration(Map<? extends Setting<?>, ? extends Object> settings) {
		super(settings);
	}

}