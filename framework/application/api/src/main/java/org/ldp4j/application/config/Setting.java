package org.ldp4j.application.config;

import java.io.Serializable;

/**
 * A configurable setting. Settings are identified by their key and type.
 *
 * @param <T>
 *            The expected configuration value type for the setting.
 */
public interface Setting<T> extends Serializable {

	/**
	 * The type of the value of the setting.
	 *
	 * @return The class of the value of the setting.
	 */
	Class<T> type();

	/**
	 * A unique key for this setting.
	 *
	 * @return A unique key identifying this setting.
	 */
	String getKey();

	/**
	 * The human readable name for this setting
	 *
	 * @return The name for this setting.
	 */
	String getDescription();

	/**
	 * Returns the default value for this setting if it is not set by a
	 * user.
	 *
	 * @return The default value for this setting.
	 */
	T getDefaultValue();

}