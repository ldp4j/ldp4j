package org.ldp4j.application.config;


public interface ImmutableConfiguration extends Configuration {

	/**
	 * Create a new immutable configuration from the current configuration with
	 * a new value for the specified {@link Setting}. If the value is null, the
	 * setting is removed and the default will be used instead.
	 *
	 * @param setting
	 *            The setting to set a new value for.
	 * @param value
	 *            The value for the setting, or null to reset the setting to use
	 *            the default value.
	 * @return A copy of the configuration with the specified setting updated.
	 */
	<T> ImmutableConfiguration set(Setting<T> setting, T value);

}