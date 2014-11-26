package org.ldp4j.application.config.core;

import org.ldp4j.application.config.Setting;
import org.ldp4j.application.util.Generics;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

public final class DefaultSetting<T> implements Setting<T> {

	private static final long serialVersionUID = -195744181766843999L;

	/**
	 * The type for this setting
	 */
	private final Class<T> type;

	/**
	 * A unique key for this setting.
	 */
	private final String key;

	/**
	 * A human-readable description for this setting
	 */
	private final String description;

	/**
	 * The default value for this setting. <br>
	 * NOTE: This value must be immutable.
	 */
	private final T defaultValue;

	private DefaultSetting(String key, String description, T defaultValue) {
		this.type=Generics.getTypeParameter(getClass(),Object.class);
		this.key = key;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> type() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getKey() {
		return this.key;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return this.description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.type,this.key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Setting<?>)) {
			return false;
		}
		Setting<?> that=(Setting<?>)obj;
		return
			this.type==that.type() &&
			Objects.equal(this.key, that.getKey());
	}

	/**
	 * Create a new setting object that will be used to reference the given
	 * setting.
	 *
	 * @param key
	 *        A unique key to use for this setting.
	 * @param description
	 *        A short human-readable description for this setting.
	 * @param defaultValue
	 *        An immutable value specifying the default for this setting.
	 */
	public static <T> Setting<T> create(String key, String description, T defaultValue) {
		checkNotNull(key,"Setting key cannot be null");
		checkNotNull(description,"Setting description cannot be null");
		checkNotNull(defaultValue,"Setting default value cannot be null");
		return new DefaultSetting<T>(key, description, defaultValue);
	}
}
