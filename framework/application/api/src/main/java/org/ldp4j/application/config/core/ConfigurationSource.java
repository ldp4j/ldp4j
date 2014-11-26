package org.ldp4j.application.config.core;

public enum ConfigurationSource {
	ENVIRONMENT_PROPERTIES,
	SYSTEM_PROPERTIES,
	CUSTOM_PROPERTIES,
	USER_SETTINGS() {
		public boolean isInterpolable() {
			return false;
		}
	},
	DEFAULTS() {
		public boolean isInterpolable() {
			return false;
		}
	}
	;

	public boolean isInterpolable() {
		return true;
	}

}
