package org.ldp4j.application.config.core;

import org.ldp4j.application.config.ImmutableConfiguration;
import org.ldp4j.application.config.Setting;

final class CustomizableImmutableConfiguration extends CustomizableConfiguration implements ImmutableConfiguration {

	private CustomizableImmutableConfiguration(CustomizableImmutableConfiguration configuration) {
		super(configuration);
	}

	protected CustomizableImmutableConfiguration() {
		super();
	}

	@Override
	public <T> ImmutableConfiguration set(Setting<T> setting, T value) {
		CustomizableImmutableConfiguration result=new CustomizableImmutableConfiguration(this);
		result.update(setting, value);
		return result;
	}

}