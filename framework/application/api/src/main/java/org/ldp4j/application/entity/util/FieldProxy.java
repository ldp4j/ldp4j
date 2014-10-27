package org.ldp4j.application.entity.util;

import java.lang.reflect.Field;

public final class FieldProxy<T,V> {

	private final Class<T> type;
	private final Class<V> value;
	private final String name;
	private final Field field;

	private FieldProxy(Class<T> type, Class<V> valueClass, String fieldName) {
		this.type = type;
		this.value = valueClass;
		this.name = fieldName;
		try {
			this.field = this.type.getDeclaredField(this.name);
			this.field.setAccessible(true);
			if(this.field.getType()!=valueClass) {
				throw new IllegalArgumentException(String.format("Field '%s' of class %s is not of type %s",fieldName,type.getName(),valueClass.getName()));
			}
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(String.format("Class %s does not have a field named '%s'",type.getName(),fieldName),e);
		} catch (SecurityException e) {
			throw new IllegalStateException(String.format("Could not make access field '%s' of class %s",fieldName,type.getName()),e);
		}
	}

	public V get(T instance) {
		try {
			return this.value.cast(this.field.get(instance));
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	public void set(T instance, V value) {
		try {
			this.field.set(instance, value);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	public static <T,V> FieldProxy<T,V> create(Class<T> type, Class<V> valueClass, String fieldName) {
		return new FieldProxy<T, V>(type, valueClass, fieldName);
	}

}