/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ForwardingMap;

/**
 * {@link Map} variant that stores String keys in a case-insensitive manner, for
 * example for key-based access in a results table.
 *
 * <p>
 * Preserves the original order as well as the original casing of keys, while
 * allowing for contains, get, and remove calls with any case of key.
 *
 * <p>
 * Does <i>not</i> support {@code null} keys.
 *
 */
/**
 * TODO: Add support for load factor on the constructor
 */
final class CaseInsensitiveMap<V> extends ForwardingMap<String, V> {

	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	private final Map<String, V> delegate;
	private final Map<String, String> caseInsensitiveKeys;
	private final Locale locale;

	private CaseInsensitiveMap(final LinkedHashMap<String,V> delegate, final Map<String,String> caseInsensitiveKeys, final Locale locale) {
		this.delegate = delegate;
		this.caseInsensitiveKeys = caseInsensitiveKeys;
		this.locale = locale != null ? locale : Locale.getDefault();
	}

	/**
	 * Create a new CaseInsensitiveMap for the default Locale.
	 *
	 * @see java.lang.String#toLowerCase()
	 */
	CaseInsensitiveMap() {
		this(DEFAULT_INITIAL_CAPACITY);
	}

	/**
	 * Create a new CaseInsensitiveMap that wraps a {@link LinkedHashMap} with
	 * the given initial capacity and stores lower-case keys according to the
	 * default Locale.
	 *
	 * @param initialCapacity
	 *            the initial capacity
	 * @see java.lang.String#toLowerCase()
	 */
	CaseInsensitiveMap(final int initialCapacity) {
		this(initialCapacity, null);
	}

	/**
	 * Create a new CaseInsensitiveMap that stores lower-case keys according to
	 * the given Locale.
	 *
	 * @param locale
	 *            the Locale to use for lower-case conversion
	 * @see java.lang.String#toLowerCase(java.util.Locale)
	 */
	CaseInsensitiveMap(final Locale locale) {
		this(DEFAULT_INITIAL_CAPACITY,locale);
	}

	/**
	 * Create a new CaseInsensitiveMap that wraps a {@link LinkedHashMap} with
	 * the given initial capacity and stores lower-case keys according to the
	 * given Locale.
	 *
	 * @param initialCapacity
	 *            the initial capacity
	 * @param locale
	 *            the Locale to use for lower-case conversion
	 * @see java.lang.String#toLowerCase(java.util.Locale)
	 */
	CaseInsensitiveMap(final int initialCapacity, final Locale locale) {
		this(new LinkedHashMap<String,V>(initialCapacity),new HashMap<String, String>(initialCapacity),locale);
	}

	protected Locale locale() {
		return this.locale;
	}

	@Override
	protected Map<String, V> delegate() {
		return this.delegate;
	}

	@Override
	public V put(final String key, final V value) {
		super.put(key,value);
		final String oldKey = addKey(key);
		V result=null;
		if(oldKey!=null) {
			result=super.remove(oldKey);
		}
		return result;
	}

	@Override
	public void putAll(final Map<? extends String, ? extends V> map) {
		standardPutAll(map);
	}

	@Override
	public boolean containsKey(final Object key) {
		boolean result=false;
		if(key instanceof String) {
			result=hasKey(key);
		}
		return result;
	}

	@Override
	public V get(final Object key) {
		V result=null;
		if(key instanceof String) {
			result=super.get(getKey(key));
		}
		return result;
	}

	@Override
	public V remove(final Object key) {
		V result=null;
		if(key instanceof String) {
			result=super.remove(removeKey(key));
		}
		return result;
	}

	@Override
	public void clear() {
		this.caseInsensitiveKeys.clear();
		super.clear();
	}

	/**
	 * Hash code is calculated normalizing the keys of the map to lower-case.
	 */
	@Override
	public int hashCode() {
		int hash=19*this.locale.hashCode();
		for(Entry<String,V> entry:super.entrySet()) {
			hash*=convertKey(entry.getKey()).hashCode()^entry.getValue().hashCode();
		}
		return hash;
	}

	/**
	 * The instance is equal to another object if the other object is the
	 * instance itself, or it is an instance of CaseInsensitiveMap with the same
	 * locale and contains the same values as the instance for equivalent keys.
	 */
	@Override
	public boolean equals(final Object other) {
		if(this==other) {
			return true;
		}
		if(!(other instanceof CaseInsensitiveMap<?>)) {
			return false;
		}
		final CaseInsensitiveMap<?> that = (CaseInsensitiveMap<?>) other;
		return
			hasSameLocale(that) &&
			hasSameEntries(that);
	}

	private boolean hasSameEntries(CaseInsensitiveMap<?> that) {
		if(this.delegate.size()!=that.delegate.size()) {
			return false;
		}
		for(final Entry<String,V> parameter : this.delegate.entrySet()) {
			final String key=parameter.getKey();
			if(!Objects.equals(parameter.getValue(), that.get(key))) {
				return false;
			}
		}
		return true;
	}

	private boolean hasSameLocale(CaseInsensitiveMap<?> that) {
		return Objects.equals(this.locale, that.locale);
	}

	private String addKey(final String key) {
		return this.caseInsensitiveKeys.put(convertKey(key),key);
	}

	private boolean hasKey(final Object key) {
		return this.caseInsensitiveKeys.containsKey(convertKey((String)key));
	}

	private String getKey(final Object key) {
		return this.caseInsensitiveKeys.get(convertKey((String)key));
	}

	private String removeKey(final Object key) {
		return this.caseInsensitiveKeys.remove(convertKey((String)key));
	}

	/**
	 * Convert the given key to a case-insensitive key.
	 * <p>
	 * The default implementation converts the key to lower-case according to
	 * this Map's Locale.
	 *
	 * @param key
	 *            the user-specified key
	 * @return the key to use for storing
	 * @see java.lang.String#toLowerCase(java.util.Locale)
	 */
	private String convertKey(final String key) {
		return key.toLowerCase(this.locale);
	}

}