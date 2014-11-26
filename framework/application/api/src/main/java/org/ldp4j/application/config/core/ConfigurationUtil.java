package org.ldp4j.application.config.core;

import java.util.Map;
import java.util.Map.Entry;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.Setting;

import com.google.common.collect.Maps;

public final class ConfigurationUtil {

	private ConfigurationUtil() {
		// Prevent instantiation
	}

	public static Map<? extends Setting<?>, ? extends Object> toMap(Configuration config) {
		Map<Setting<?>,Object> rawSettings=Maps.newLinkedHashMap();
		for(Setting<?> setting:config.settings()) {
			rawSettings.put(setting, config.get(setting));
		}
		return rawSettings;
	}

	public static Map<Setting<?>,Object> verify(Map<? extends Setting<?>, ? extends Object> settings) {
		Map<Setting<?>,Object> invalidSettings=Maps.newLinkedHashMap();
		for(Entry<? extends Setting<?>, ? extends Object> entry:settings.entrySet()) {
			Setting<?> setting = entry.getKey();
			Object value = entry.getValue();
			if(!isValid(setting,value)) {
				invalidSettings.put(entry.getKey(), entry.getValue());
			}
		}
		return invalidSettings;
	}

	private static boolean isValid(Setting<?> key, Object value) {
		return key.type().isInstance(value);
	}

}