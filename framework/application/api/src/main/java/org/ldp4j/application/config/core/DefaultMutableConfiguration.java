package org.ldp4j.application.config.core;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.MutableConfiguration;
import org.ldp4j.application.config.Setting;

public class DefaultMutableConfiguration extends BaseConfiguration implements MutableConfiguration {

	/**
	 *
	 */
	private static final long serialVersionUID = -8811212400844264025L;

	public DefaultMutableConfiguration(Configuration config) {
		super(config);
	}

	public DefaultMutableConfiguration() {
		super();
	}

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
	@Override
	public <T> void set(Setting<T> setting, T value) {
		super.update(setting, value);
	}

}