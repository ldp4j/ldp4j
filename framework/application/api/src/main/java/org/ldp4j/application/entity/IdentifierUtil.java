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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ldp4j.application.data.Name;

import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.net.PercentEscaper;
import com.google.common.net.UrlEscapers;

final class IdentifierUtil {

	private static final class IdentifierParser {

		private static final Pattern PARAMETER_PATTERN=Pattern.compile("^(\\w+)='([^']+)'$");

		private final Map<String,String> parameters;

		private Classifier classifier;
		private String path;
		private boolean valid;

		private IdentifierParser() {
			this.parameters=Maps.newLinkedHashMap();
			this.valid=true;
		}

		private void setValid(boolean valid) {
			this.valid &= valid;
		}

		private void setPath(String path) {
			this.path = path;
		}

		private void setClassifier(Classifier classifier) {
			this.classifier = classifier;
			this.setValid(classifier!=null);
		}

		private void addParameter(String parameterName, String parameterValue) {
			this.parameters.put(parameterName,parameterValue);
		}

		boolean isValid() {
			return this.valid;
		}

		Classifier classifier() {
			return this.classifier;
		}

		String path() {
			return this.path;
		}

		String parameter(String parameterName) {
			return this.parameters.get(parameterName);
		}

		static IdentifierParser create(URI identifier) {
			IdentifierParser result = new IdentifierParser();
			result.setClassifier(Classifier.fromString(identifier.getAuthority()));
			result.setPath(identifier.getPath().substring(1));
			String query = identifier.getQuery();
			for(String parameter:query.split("&")) {
				Matcher matcher = PARAMETER_PATTERN.matcher(parameter);
				if(matcher.matches()) {
					String parameterName=matcher.group(1);
					String parameterValue=matcher.group(2);
					result.addParameter(parameterName,parameterValue);
				} else {
					result.setValid(false);
				}
			}
			result.setValid(IDENTIFIER_SCHEME.equals(identifier.getScheme()));
			return result;
		}

	}

	private static final class IdentifierBuilder {

		private final Map<String,Object> parameters;

		private Object owner;
		private Classifier classifier;

		private Object value;

		private IdentifierBuilder() {
			this.parameters=Maps.newLinkedHashMap();
		}

		IdentifierBuilder withClassifier(Classifier classifier) {
			this.classifier = classifier;
			return this;
		}

		IdentifierBuilder withOwner(Object owner) {
			this.owner = owner;
			return this;
		}

		IdentifierBuilder withParameter(String parameterName, Object parameterValue) {
			this.parameters.put(parameterName,parameterValue);
			return this;
		}

		URI build() {
			StringBuilder builder=new StringBuilder();
			builder.
				append(IDENTIFIER_SCHEME).
				append("://").
				append(classifier.tag()).
				append("/").
				append(IdentifierUtil.toPath(this.owner));
			if(!this.parameters.isEmpty()) {
				builder.append("?");
				for(Iterator<Entry<String,Object>> it=this.parameters.entrySet().iterator();it.hasNext();) {
					Entry<String, Object> entry = it.next();
					builder.
						append(IdentifierUtil.toQueryParameter(entry.getKey())).
						append("=\'").
						append(IdentifierUtil.toQueryParameter(entry.getValue())).
						append("\'");
					if(it.hasNext()) {
						builder.append("&");
					}
				}
			}
			String rawURI = builder.toString();
			try {
				return new URI(rawURI);
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Creation of "+classifier+" identifying uri '"+rawURI+"' for value '"+value+"' should not fail",e);
			}
		}

		static IdentifierBuilder create() {
			return new IdentifierBuilder();
		}

	}

	static enum Classifier {
		LOCAL("local"),
		EXTERNAL("external"),
		MANAGED("managed"),
		RELATIVE("relative"),
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
		private Object owner;
		private Class<?> valueClass;
		private String rawValue;
		private Object value;

		private IdentifierIntrospector(URI identifier) {
			this.valid=true;
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

		private void setOwner(Object owner) {
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
			this.valid&=valid;
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

		Object owner() {
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
			IdentifierParser breakdown=IdentifierParser.create(identifier);
			introspector.setValid(breakdown.isValid());
			if(breakdown.isValid()) {
				introspector.setClassifier(breakdown.classifier());
				switch(breakdown.classifier()) {
				case EXTERNAL:
					initializeExternal(introspector, breakdown);
					break;
				case LOCAL:
					initializeLocal(introspector, breakdown);
					break;
				case MANAGED:
					initializeManaged(introspector, breakdown);
					break;
				case RELATIVE:
					initializeRelative(introspector, breakdown);
					break;
				default:
					break;
				}
			}
			return introspector;
		}

		private static void initializeLocal(
				IdentifierIntrospector introspector,
				IdentifierParser breakdown) {
			String rawValueClass=breakdown.parameter("class");
			Class<?> valueClass=getValueClass(rawValueClass);
			String rawValue=breakdown.parameter("value");

			populateIntrospector(
				introspector,
				getDataSourceId(breakdown),
				rawValue,
				valueClass,
				ObjectUtil.fromString(valueClass,rawValue));
		}

		private static void populateIntrospector(
				IdentifierIntrospector introspector, Object owner,
				String rawValue, Class<?> valueClass, Object value) {
			introspector.setOwner(owner);
			introspector.setRawValue(rawValue);
			introspector.setValueClass(valueClass);
			introspector.setValue(value);
		}

		private static void initializeManaged(
				IdentifierIntrospector introspector,
				IdentifierParser breakdown) {
			Class<?> valueClass=getValueClass(breakdown.parameter("class"));
			String rawValue=breakdown.parameter("value");

			populateIntrospector(
				introspector,
				getOwner(breakdown),
				rawValue,
				valueClass,
				ObjectUtil.fromString(valueClass,rawValue));
		}

		private static void initializeRelative(
				IdentifierIntrospector introspector,
				IdentifierParser breakdown) {
			String path=breakdown.parameter("path");
			Class<URI> clazz = URI.class;
			URI fromString = ObjectUtil.fromString(clazz, path);
			populateIntrospector(
				introspector,
				getKey(breakdown),
				path,
				clazz,
				fromString);
		}
		private static void initializeExternal(
				IdentifierIntrospector introspector,
				IdentifierParser breakdown) {
			String location= breakdown.parameter("location");
			URI value = getLocation(location);
			populateIntrospector(
				introspector,
				value,
				location,
				URI.class,
				value);
		}

		private static URI getLocation(String rawValue) {
			URI value=null;
			try {
				if(rawValue!=null) {
					value=new URI(rawValue);
				}
			} catch (URISyntaxException e) {
				// Nothing to do
			}
			return value;
		}

		private static UUID getDataSourceId(IdentifierParser breakdown) {
			UUID owner=null;
			String path=breakdown.path();
			if(path!=null) {
				owner=UUID.fromString(path);
			}
			return owner;
		}

		private static Class<?> getOwner(IdentifierParser breakdown) {
			Class<?> owner=null;
			String path=breakdown.path();
			if(path!=null) {
				owner=getValueClass(path.replace('/','.'));
			}
			return owner;
		}

		private static Key<?> getKey(IdentifierParser breakdown) {
			Key<?> key=null;
			Object nativeId=
				ObjectUtil.
					fromString(
						getValueClass(breakdown.parameter("class")),
						breakdown.parameter("value"));
			Class<?> owner = getOwner(breakdown);
			if(owner!=null && nativeId!=null) {
				key=Key.create(owner, nativeId);
			}
			return key;
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

	}

	private static final String IDENTIFIER_SCHEME = "ldp4j";

	static <T> URI createLocalIdentifier(UUID dataSourceId,Name<T> name) {
		checkNotNull(dataSourceId,"Data source identifier cannot be null");
		checkNotNull(name,"Local name cannot be null");
		T value=name.id();
		return
			new IdentifierBuilder().
				withClassifier(Classifier.LOCAL).
				withOwner(dataSourceId).
				withParameter("class", value.getClass().getName()).
				withParameter("value", value).
				build();
	}

	static <T> URI createManagedIdentifier(Key<T> key) {
		checkNotNull(key,"Managed key cannot be null");
		Object nativeId = key.nativeId();
		return
			IdentifierBuilder.
				create().
					withClassifier(Classifier.MANAGED).
					withOwner(key.owner()).
					withParameter("class", nativeId.getClass().getName()).
					withParameter("value", nativeId).
					build();
	}

	static <T> URI createRelativeIdentifier(Key<T> key, URI path) {
		checkNotNull(key,"Relative key cannot be null");
		Object nativeId = key.nativeId();
		return
			IdentifierBuilder.
				create().
					withClassifier(Classifier.RELATIVE).
					withOwner(key.owner()).
					withParameter("class", nativeId.getClass().getName()).
					withParameter("value", nativeId).
					withParameter("path", path).
					build();
	}

	static <T> URI createExternalIdentifier(URI location) {
		checkNotNull(location,"External location cannot be null");
		return
			IdentifierBuilder.
				create().
					withClassifier(Classifier.EXTERNAL).
					withParameter("location", location).
					build();
	}

	static IdentifierIntrospector introspect(URI uri) {
		checkNotNull(uri,"Identifier uri cannot be null");
		return IdentifierIntrospector.create(uri);
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

	private static String toQueryParameter(Object value) {
		return uriScaper().escape(ObjectUtil.toString(value));
	}

	private static String toPath(Object owner) {
		String result="";
		if(Class.class.isInstance(owner)) {
			result=classToPath(Class.class.cast(owner));
		} else if(UUID.class.isInstance(owner)){
			result=uuidToPath(UUID.class.cast(owner));
		} else if(owner!=null) {
			result=stringToPath(ObjectUtil.toString(owner));
		}
		return result;
	}

	private static String uuidToPath(UUID owner) {
		return stringToPath(owner.toString());
	}

	private static String classToPath(Class<?> owner) {
		StringBuilder builder=new StringBuilder();
		for(Iterator<String> it=Arrays.asList(owner.getName().split("\\.")).iterator();it.hasNext();) {
			builder.append(stringToPath(it.next()));
			if(it.hasNext()) {
				builder.append("/");
			}
		}
		return builder.toString();
	}

	private static String stringToPath(String str) {
		return UrlEscapers.urlPathSegmentEscaper().escape(str);
	}

}