package org.ldp4j.application.config.util;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.ConfigurationException;

public interface Configurable<T extends Configuration> {

	Class<? extends T> configType();

	boolean canConfigure();

	void configure(T configuration) throws ConfigurationException;

}
