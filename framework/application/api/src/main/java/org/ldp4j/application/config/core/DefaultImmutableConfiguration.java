package org.ldp4j.application.config.core;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.ImmutableConfiguration;
import org.ldp4j.application.config.Setting;

public class DefaultImmutableConfiguration extends BaseConfiguration implements ImmutableConfiguration {

	/**
	 *
	 */
	private static final long serialVersionUID = -1969151343231953574L;

	protected DefaultImmutableConfiguration(DefaultImmutableConfiguration configuration) {
		super(configuration);
	}

	public DefaultImmutableConfiguration(Configuration config) {
		super(config);
	}

	public DefaultImmutableConfiguration() {
		super();
	}

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
	@Override
	public <T> ImmutableConfiguration set(Setting<T> setting, T value) {
		DefaultImmutableConfiguration result=new DefaultImmutableConfiguration(this);
		result.update(setting,value);
		return result;
	}

}