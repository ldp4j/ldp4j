package org.ldp4j.application.config.core;

import org.ldp4j.application.config.MutableConfiguration;
import org.ldp4j.application.config.Setting;

final class CustomizableMutableConfiguration extends CustomizableConfiguration implements MutableConfiguration {

	protected CustomizableMutableConfiguration() {
		super();
	}

	@Override
	public <T> void set(Setting<T> setting, T value) {
		update(setting, value);
	}

}