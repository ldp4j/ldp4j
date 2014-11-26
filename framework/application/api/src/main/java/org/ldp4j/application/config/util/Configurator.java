package org.ldp4j.application.config.util;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.ConfigurationException;

import com.google.common.base.Optional;

public final class Configurator {

	private Configurator() {
	}

	public static void configure(Object object, Configuration config) throws ConfigurationException {
		if(!(object instanceof Configurable<?>)) {
			return;
		}
		Configurable<? extends Configuration> cService=(Configurable<?>)object;
		if(!cService.canConfigure()) {
			throw new ConfigurationException("Object cannot be configured at this time");
		}
		if(!cService.configType().isInstance(config)) {
			throw new ConfigurationException("Invalid configuration type");
		}
		doConfigure(cService,config);
	}

	public static <E extends Exception> void configure(Object object, Configuration config, E failure) throws E {
		if(!(object instanceof Configurable<?>)) {
			return;
		}
		boolean failed=true;
		Configurable<? extends Configuration> cService=(Configurable<?>)object;
		if(cService.canConfigure() && cService.configType().isInstance(config)) {
			try {
				doConfigure(cService,config);
				failed=false;
			} catch (ConfigurationException e) {
				// TODO: log the failure
				failure.initCause(e);
			}
		}
		if(failed) {
			throw failure;
		}
	}

	public static <T, C extends Configuration> Optional<Boolean> tryConfigure(T object, C config) {
		if(!(object instanceof Configurable<?>)) {
			return Optional.absent();
		}
		Optional<Boolean> configured = Optional.of(false);
		Configurable<? extends Configuration> cService=(Configurable<?>)object;
		if(cService.canConfigure() && cService.configType().isInstance(config)) {
			try {
				doConfigure(cService,config);
				configured=Optional.of(true);
			} catch (ConfigurationException e) {
				// Nothing to do but log the failure
			}
		}
		return configured;
	}

	private static <C extends Configuration, T extends Configurable<C>> void doConfigure(T configurable, Configuration config) throws ConfigurationException {
		configurable.configure(configurable.configType().cast(config));
	}

}