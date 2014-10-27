/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.entity.spi.ValueFactory;
import org.ldp4j.application.entity.spi.ValueTransformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.escape.Escaper;
import com.google.common.net.PercentEscaper;

final class IdentifierUtil {

	private static final class NullValueFactory<T> implements ValueFactory<T> {

		private final Class<? extends T> valueClass;

		private NullValueFactory(Class<? extends T> valueClass) {
			this.valueClass = valueClass;
		}

		@Override
		public Class<? extends T> targetClass() {
			return this.valueClass;
		}

		@Override
		public T fromString(String rawValue) {
			throw new ValueTransformationException("Unsupported value type '"+targetClass().getName()+"'",null,this.valueClass);
		}

		@Override
		public String toString(T value) {
			throw new ValueTransformationException("Unsupported value type '"+targetClass().getName()+"'",null,this.valueClass);
		}

	}

	static enum Classifier {
		LOCAL("local"),
		EXTERNAL("external"),
		MANAGED("managed"),
		;
		private String tag;

		private Classifier(String tag) {
			this.tag = tag;
		}

		String tag() {
			return this.tag;
		}
		static Classifier fromString(String tag) {
			for(Classifier classifier:values()) {
				if(classifier.tag.equals(tag)) {
					return classifier;
				}
			}
			return null;
		}
	}

	static final class IdentifierIntrospector {

		private final URI identifier;
		private boolean valid;
		private Classifier classifier;
		private Class<?> owner;
		private Class<?> valueClass;
		private String rawValue;
		private Object value;

		private IdentifierIntrospector(URI identifier) {
			this.identifier = identifier;
		}

		private void setValue(Object value) {
			this.value=value;
			setValid(value!=null && this.valueClass.isInstance(value));
		}

		private void setRawValue(String rawValue) {
			this.rawValue=rawValue;
			setValid(rawValue!=null);
		}

		private void setOwner(Class<?> owner) {
			this.owner=owner;
			setValid(owner!=null);
		}

		private void setValueClass(Class<?> valueClass) {
			this.valueClass=valueClass;
			setValid(valueClass!=null);
		}

		private void setClassifier(Classifier classifier) {
			this.classifier=classifier;
			setValid(classifier!=null);
		}

		private void setValid(boolean valid) {
			this.valid=valid;
		}

		URI subject() {
			return this.identifier;
		}

		boolean isValid() {
			return this.valid;
		}

		Classifier classifier() {
			return this.classifier;
		}

		Class<?> owner() {
			return this.owner;
		}

		Class<?> valueClass() {
			return this.valueClass;
		}

		String rawValue() {
			return this.rawValue;
		}

		<T> T value(Class<? extends T> clazz) {
			if(!clazz.isInstance(this.value)) {
				throw new IllegalArgumentException("Incompatible types");
			}
			return clazz.cast(this.value);
		}

		private static IdentifierIntrospector create(URI identifier) {
			IdentifierIntrospector introspector = new IdentifierIntrospector(identifier);
			introspector.setClassifier(Classifier.fromString(identifier.getAuthority()));
			introspector.setOwner(getOwnerClass(identifier));
			Matcher matcher = QUERY_PATTERN.matcher(identifier.getQuery());
			if(matcher.matches()) {
				Class<?> valueClass = getValueClass(matcher.group(1));
				introspector.setValueClass(valueClass);
				String rawValue=matcher.group(2);
				introspector.setRawValue(rawValue);
				ValueFactory<?> factory=findFactory(valueClass);
				introspector.setValue(factory.fromString(rawValue));
			}
			introspector.setValid(IDENTIFIER_SCHEME.equals(identifier.getScheme()));
			return introspector;
		}

		private static Class<?> getValueClass(String valueClassName) {
			Class<?> valueClass=null;
			try {
				valueClass=Class.forName(valueClassName);
			} catch (ClassNotFoundException e) {
				// Nothing to do
			}
			return valueClass;
		}

		private static Class<?> getOwnerClass(URI identifier) {
			Class<?> owner=null;
			try {
				String ownerClassName=identifier.getPath().substring(1).replace('/', '.');
				owner=Class.forName(ownerClassName);
			} catch(ClassNotFoundException e) {
				// Nothing to do
			}
			return owner;
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(IdentifierUtil.class);

	private static final String IDENTIFIER_SCHEME = "ldp4j";

	private static final Pattern QUERY_PATTERN=Pattern.compile("^class='([^']+)'&value='([^']+)'$");

	private static Map<Class<?>,ValueFactory<?>> factoryCache=new HashMap<Class<?>, ValueFactory<?>>();
	private static Set<Class<?>> preloadedFactories=new HashSet<Class<?>>();

	static <T> URI createLocalIdentifier(Name<T> name) {
		T value=name.id();
		return createIdentifier(Classifier.LOCAL,Name.class,value);
	}

	static <T> URI createManagedIdentifier(Key<T> key) {
		return createIdentifier(Classifier.MANAGED,key.owner(),key.nativeId());
	}

	static <T> URI createExternalIdentifier(URI uri) {
		return createIdentifier(Classifier.EXTERNAL,URI.class,uri);
	}

	static IdentifierIntrospector introspect(URI uri) {
		checkNotNull(uri,"Identifier uri cannot be null");
		return IdentifierIntrospector.create(uri);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static synchronized <T> ValueFactory<T> findFactory(final Class<? extends T> valueClass) {
		ValueFactory<?> rawResult=factoryCache.get(valueClass);
		if(rawResult==null) {
			for(ValueFactory candidate:ServiceLoader.load(ValueFactory.class)) {
				if(preloadedFactories.contains(candidate.getClass())) {
					if(LOGGER.isTraceEnabled()) {
						LOGGER.trace("Discarding cached factory '{}' for value class '{}'",candidate.getClass().getName(),candidate.targetClass().getName());
					}
					continue;
				}
				if(factoryCache.containsKey(candidate.targetClass())) {
					if(LOGGER.isWarnEnabled()) {
						LOGGER.warn("Discarding clashing factory '{}' for value class '{}'",candidate.getClass().getName(),candidate.targetClass().getName());
					}
					continue;
				}
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Caching factory '{}' for value class '{}'",candidate.getClass().getName(),candidate.targetClass().getName());
				}
				factoryCache.put(candidate.targetClass(),candidate);
				preloadedFactories.add(candidate.getClass());
				if(candidate.targetClass()==valueClass) {
					rawResult=candidate;
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("Found factory '{}' for value class '{}'",candidate.getClass().getName(),valueClass.getName());
					}
					break;
				}
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Discarding factory '{}': target class '{}' does not match value class '{}'",candidate.getClass().getName(),candidate.targetClass().getName(),valueClass.getName());
				}
			}
		}
		if(rawResult==null) {
			rawResult=new NullValueFactory<T>(valueClass);
			if(LOGGER.isWarnEnabled()) {
				LOGGER.warn("No factory found for value class '{}'",valueClass.getName());
			}
		}
		return (ValueFactory<T>)rawResult;
	}

	private static <T,V> URI createIdentifier(Classifier classifier, Class<?> owner, V value) {
		StringBuilder builder=new StringBuilder();
		builder.
			append("ldp4j://").
			append(classifier.tag()).
			append("/").
			append(owner.getCanonicalName().replace('.','/')).
			append("?").
			append("class=\'").
			append(toString(value.getClass().getName())).
			append("\'").
			append("&").
			append("value=\'").
			append(toString(value)).
			append("\'");
		String rawURI = builder.toString();
		try {
			return new URI(rawURI);
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Creation of "+classifier+" identifying uri '"+rawURI+"' for value '"+value+"' should not fail",e);
		}
	}

	/**
	 * Returns an {@link Escaper} instance that escapes strings so they can be
	 * safely included in <a href="http://goo.gl/OQEc8">URL form parameter names
	 * and values</a>. Escaping is performed with the UTF-8 character encoding.
	 * The caller is responsible for <a href="http://goo.gl/i20ms">replacing any
	 * unpaired carriage return or line feed characters with a CR+LF pair</a> on
	 * any non-file inputs before escaping them with this escaper.
	 *
	 * <p>
	 * When escaping a String, the following rules apply:
	 * <ul>
	 * <li>The alphanumeric characters "a" through "z", "A" through "Z" and "0"
	 * through "9" remain the same.
	 * <li>The special characters ".", "-", "*", and "_" remain the same.
	 * <li>The space character " " is converted into %20.
	 * <li>All other characters are converted into one or more bytes using UTF-8
	 * encoding and each byte is then represented by the 3-character string
	 * "%XY", where "XY" is the two-digit, uppercase, hexadecimal representation
	 * of the byte value.
	 * </ul>
	 *
	 * <p>
	 * This escaper is suitable for escaping parameter names and values even
	 * when <a href="http://goo.gl/utn6M">using the non-standard semicolon</a>,
	 * rather than the ampersand, as a parameter delimiter. Nevertheless, we
	 * recommend using the ampersand unless you must interoperate with systems
	 * that require semicolons.
	 *
	 * <p>
	 * <b>Note</b>: Unlike other escapers, URL escapers produce uppercase
	 * hexadecimal sequences. From <a
	 * href="http://www.ietf.org/rfc/rfc3986.txt"> RFC 3986</a>:<br>
	 * <i>"URI producers and normalizers should use uppercase hexadecimal digits
	 * for all percent-encodings."</i>
	 *
	 */
	private static Escaper uriScaper() {
		return new PercentEscaper("-_.*",false);
	}

	private static String toString(Object value) {
		ValueFactory<Object> factory = findFactory(value.getClass());
		return uriScaper().escape(factory.toString(value));
	}

}