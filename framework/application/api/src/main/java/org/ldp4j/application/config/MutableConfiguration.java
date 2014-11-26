package org.ldp4j.application.config;


public interface MutableConfiguration extends Configuration {

	/**
	 * Sets a {@link Setting} to have a new value. If the value is null, the
	 * setting is removed and the default will be used instead.
	 *
	 * @param setting
	 *        The setting to set a new value for.
	 * @param value
	 *        The value for the setting, or null to reset the setting to use
	 *        the default value.
	 */
	<T> void set(Setting<T> setting, T value) throws ConfigurationException;

}