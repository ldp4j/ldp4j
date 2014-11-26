package org.ldp4j.application.config;

import java.util.Set;

public interface Configuration {

	/**
	 * Return the settings defined in the configuration.
	 *
	 * @return The settings defined in the configuration.
	 */
	Set<Setting<?>> settings();

	/**
	 * Return the value for a given {@link Setting} or the default value if it
	 * has not been set.
	 *
	 * @param setting
	 *        The {@link Setting} to fetch a value for.
	 * @return The value for the setting, or the default value if it is not set.
	 */
	<T> T get(Setting<T> setting);

	/**
	 * Checks for whether a {@link Setting} has been explicitly set by a user.
	 *
	 * @param setting
	 *        The setting to check for.
	 * @return True if the setting has been explicitly set, or false otherwise.
	 */
	<T> boolean isSet(Setting<T> setting);

	/**
	 * Resets all settings back to their default values.
	 */
	void useDefaults();

}