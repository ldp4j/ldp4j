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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-api:0.2.2
 *   Bundle      : rmf-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * Remember that section 5.1 of the RDF Semantics recommendation identifies
 * which types can be safely used and reasoned about in RDF(S). It does not mean
 * whatsoever that other applications cannot used the other datatypes. The only
 * thing is that the model semantics defined in there cannot be applied.
 */
public enum Datatype {

	ANY_TYPE(Namespace.XML_SCHEMA,"anyType",String.class,null, BuiltInType.UR, null),
	ANY_SIMPLE_TYPE(Namespace.XML_SCHEMA,"anySimpleType",null, ANY_TYPE, BuiltInType.UR, DerivationType.RESTRICTION),
	DATE_TIME(Namespace.XML_SCHEMA,"dateTime",XMLGregorianCalendar.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			encodeXMLGregorianCalender(this,rawValue, consumer);
		}
	},
	DATE(Namespace.XML_SCHEMA,"date",XMLGregorianCalendar.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			encodeXMLGregorianCalender(this,rawValue, consumer);
		}
	},
	TIME(Namespace.XML_SCHEMA,"time",XMLGregorianCalendar.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			encodeXMLGregorianCalender(this,rawValue, consumer);
		}
	},
	G_MONTH(Namespace.XML_SCHEMA,"gMonth",XMLGregorianCalendar.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			encodeXMLGregorianCalender(this,rawValue, consumer);
		}
	},
	G_DAY(Namespace.XML_SCHEMA,"gDay",XMLGregorianCalendar.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			encodeXMLGregorianCalender(this,rawValue, consumer);
		}
	},
	G_MONTH_DAY(Namespace.XML_SCHEMA,"gMonthDay",XMLGregorianCalendar.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			encodeXMLGregorianCalender(this,rawValue, consumer);
		}
	},
	G_YEAR(Namespace.XML_SCHEMA,"gYear",XMLGregorianCalendar.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			encodeXMLGregorianCalender(this,rawValue, consumer);
		}
	},
	G_YEAR_MONTH(Namespace.XML_SCHEMA,"gYearMonth",XMLGregorianCalendar.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			encodeXMLGregorianCalender(this,rawValue, consumer);
		}
	},
	DURATION(Namespace.XML_SCHEMA,"duration",Duration.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			encodeDuration(this, rawValue, consumer);
		}
	},
	BOOLEAN(Namespace.XML_SCHEMA,"boolean",Boolean.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeBoolean(Boolean.parseBoolean(rawValue));
		}
	},
	BASE_64_BINARY(Namespace.XML_SCHEMA,"base64Binary",null, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION),
	HEX_BINARY(Namespace.XML_SCHEMA,"hexBinary",null, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION),
	FLOAT(Namespace.XML_SCHEMA,"float",Float.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeFloat(Float.parseFloat(rawValue));
		}
	},
	DOUBLE(Namespace.XML_SCHEMA,"double",Double.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeDouble(Double.parseDouble(rawValue));
		}
	},
	ANY_URI(Namespace.XML_SCHEMA,"anyURI",URI.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION){
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeURI(URI.create(rawValue));
		}
	},
	QNAME(Namespace.XML_SCHEMA,"QName",QName.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			String[] split = rawValue.split(":");
			consumer.consumeQName(split[0],split[1]);
		}
	},
	NOTATION(Namespace.XML_SCHEMA,"NOTATION",QName.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			String[] split = rawValue.split(":");
			consumer.consumeQName(split[0],split[1]);
		}
	},
	STRING(Namespace.XML_SCHEMA,"string",String.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeString(rawValue);
		}
	},
	NORMALIZED_STRING(Namespace.XML_SCHEMA,"normalizedString",null, STRING, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	TOKEN(Namespace.XML_SCHEMA,"token",null, NORMALIZED_STRING, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	LANGUAGE(Namespace.XML_SCHEMA,"language",null, TOKEN, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	NAME(Namespace.XML_SCHEMA,"Name",null, TOKEN, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	NMTOKEN(Namespace.XML_SCHEMA,"NMTOKEN",null, TOKEN, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	NCNAME(Namespace.XML_SCHEMA,"NCName",null, NAME, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	ID(Namespace.XML_SCHEMA,"ID",null, NCNAME, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	IDREF(Namespace.XML_SCHEMA,"IDREF",null, NCNAME, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	ENTITY(Namespace.XML_SCHEMA,"ENTITY",null, NCNAME, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	NMTOKENS(Namespace.XML_SCHEMA,"NMTOKENS",String[].class, NMTOKEN, BuiltInType.DERIVED, DerivationType.LIST) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeStringArray(rawValue.split("\\s+"));
		}
	},
	IDREFS(Namespace.XML_SCHEMA,"IDREFS",String[].class, IDREF, BuiltInType.DERIVED, DerivationType.LIST) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeStringArray(rawValue.split("\\s+"));
		}
	},
	ENTITIES(Namespace.XML_SCHEMA,"ENTITIES",String[].class, ENTITY, BuiltInType.DERIVED, DerivationType.LIST){
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeStringArray(rawValue.split("\\s+"));
		}
	},
	DECIMAL(Namespace.XML_SCHEMA,"decimal",BigDecimal.class, ANY_SIMPLE_TYPE, BuiltInType.PRIMITIVE, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeBigDecimal(new BigDecimal(rawValue));
		}
	},
	INTEGER(Namespace.XML_SCHEMA,"integer",BigInteger.class, DECIMAL, BuiltInType.DERIVED, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeBigInteger(new BigInteger(rawValue));
		}
	},
	LONG(Namespace.XML_SCHEMA,"long",Long.class, INTEGER, BuiltInType.DERIVED, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeLong(Long.parseLong(rawValue));
		}
	},
	INT(Namespace.XML_SCHEMA,"int",Integer.class, LONG, BuiltInType.DERIVED, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeInteger(Integer.parseInt(rawValue));
		}
	},
	SHORT(Namespace.XML_SCHEMA,"short",Short.class, INT, BuiltInType.DERIVED, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeShort(Short.parseShort(rawValue));
		}
	},
	BYTE(Namespace.XML_SCHEMA,"byte",Byte.class, SHORT, BuiltInType.DERIVED, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeByte(Byte.parseByte(rawValue));
		}
	},
	NON_POSITIVE_INTEGER(Namespace.XML_SCHEMA,"nonPositiveInteger",null, INTEGER, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	NEGATIVE_INTEGER(Namespace.XML_SCHEMA,"negativeInteger",null, NON_POSITIVE_INTEGER, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	NON_NEGATIVE_INTEGER(Namespace.XML_SCHEMA,"nonNegativeInteger",null, INTEGER, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	POSITIVE_INTEGER(Namespace.XML_SCHEMA,"positiveInteger",null, NON_NEGATIVE_INTEGER, BuiltInType.DERIVED, DerivationType.RESTRICTION),
	UNSIGNED_LONG(Namespace.XML_SCHEMA,"unsignedLong",Long.class, NON_NEGATIVE_INTEGER, BuiltInType.DERIVED, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeLong(Long.parseLong(rawValue));
		}
	},
	UNSIGNED_INT(Namespace.XML_SCHEMA,"unsignedInt",Long.class, UNSIGNED_LONG, BuiltInType.DERIVED, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeLong(Long.parseLong(rawValue));
		}
	},
	UNSIGNED_SHORT(Namespace.XML_SCHEMA,"unsignedShort",Integer.class, UNSIGNED_INT, BuiltInType.DERIVED, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeInteger(Integer.parseInt(rawValue));
		}
	},
	UNSIGNED_BYTE(Namespace.XML_SCHEMA,"unsignedByte",Short.class, UNSIGNED_SHORT, BuiltInType.DERIVED, DerivationType.RESTRICTION) {
		@Override
		public void decode(String rawValue, ValueSink consumer) {
			consumer.consumeShort(Short.parseShort(rawValue));
		}
	},
	XML_LITERAL(Namespace.RDF,"XMLLiteral",null,ANY_TYPE,null, null),
	;

	private static final String CLAZZ_PARAM = "Class cannot be null";

	private final Datatype parent;
	private final Datatype.BuiltInType type;
	private final Datatype.DerivationType derivation;
	private final String localName;
	private final Class<?> clazz;
	private final Namespace namespace;

	Datatype(Namespace namespace, String localPart, Class<?> clazz, Datatype parent, Datatype.BuiltInType type, Datatype.DerivationType restriction) {
		this.namespace=namespace;
		this.localName=localPart;
		this.type = type;
		this.derivation = restriction;
		this.parent = parent;
		this.clazz=clazz;
	}

	public void decode(String rawValue, ValueSink consumer) {
		if(parent==null) {
			consumer.consumeRawValue(rawValue);
		} else {
			parent.decode(rawValue, consumer);
		}
	}

	boolean isSimple() {
		return !isComplex();
	}

	boolean isComplex() {
		return DerivationType.EXTENSION_OR_RESTRICTION.equals(derivation);
	}

	boolean isPrimitive() {
		return BuiltInType.PRIMITIVE.equals(type);
	}

	boolean isDerived() {
		return BuiltInType.DERIVED.equals(type);
	}

	boolean isDerivedByList() {
		return DerivationType.LIST.equals(derivation);
	}

	boolean encodes(Class<?> clazz) {
		Objects.requireNonNull(clazz, CLAZZ_PARAM);
		if(this.clazz!=null) {
			return this.clazz.isAssignableFrom(clazz);
		} else if(parent!=null) {
			return parent.encodes(clazz);
		} else {
			return false;
		}
	}

	boolean decodes(Class<?> clazz) {
		Objects.requireNonNull(clazz, CLAZZ_PARAM);
		if(this.clazz!=null) {
			return clazz.isAssignableFrom(this.clazz);
		} else if(parent!=null) {
			return parent.encodes(clazz);
		} else {
			return false;
		}
	}

	int depth() {
		if(parent==null) {
			return 1;
		} else {
			return 1+parent.depth();
		}
	}

	Class<?> rawClass() {
		Class<?> result=null;
		if(clazz!=null) {
			result=clazz;
		} else if(parent!=null) {
			result=parent.rawClass();
		}
		return result;
	}

	List<Datatype> ancestors() {
		return DatatypeMetadata.getInstance().getAncestors(this);
	}

	public URI toURI() {
		return namespace.toURI(localName);
	}

	public static Datatype fromURI(URI uri) {
		Datatype result = null;
		if(uri!=null) {
			result=fromString(uri.toString());
		}
		return result;
	}

	public static Datatype fromString(String uri) {
		Datatype result=null;
		if(uri!=null) {
			if(Namespace.RDF.belongsToNamespace(uri)) {
				result=findRDFDataType(uri);
			} else if(Namespace.XML_SCHEMA.belongsToNamespace(uri)) {
				result=findXMLDataType(uri);
			}
		}
		return result;
	}

	/**
	 * @param uri
	 * @return
	 */
	private static Datatype findRDFDataType(String uri) {
		Datatype rdfType=null;
		if(Namespace.RDF.localName(uri).equals(XML_LITERAL.localName)) {
			rdfType=XML_LITERAL;
		}
		return rdfType;
	}

	/**
	 * @param uri
	 * @return
	 */
	private static Datatype findXMLDataType(String uri) {
		Datatype xmlType=null;
		String localName=Namespace.XML_SCHEMA.localName(uri);
		for(Datatype value:values()) {
			if(value.localName.equals(localName)) {
				xmlType=value;
				break;
			}
		}
		return xmlType;
	}

	public static Datatype matching(Class<?> clazz) {
		Datatype result = null;
		if(clazz!=null) {
			for(Datatype type:values()) {
				if(clazz.equals(type.clazz)) {
					return type;
				}
			}
		}
		return result;
	}

	public static List<Datatype> forValue(Object value) {
		return Collections.unmodifiableList(TypeSelector.select(value));
	}

	public static interface ValueSink {

		void consumeString(String value);

		void consumeQName(String prefix, String localName);

		void consumeDuration(Duration value);

		void consumeXMLGregorianCalendar(XMLGregorianCalendar value);

		void consumeByte(byte value);

		void consumeShort(short value);

		void consumeInteger(int value);

		void consumeLong(long value);

		void consumeBigInteger(BigInteger value);

		void consumeBigDecimal(BigDecimal value);

		void consumeURI(URI value);

		void consumeDouble(double value);

		void consumeFloat(float value);

		void consumeBoolean(boolean value);

		void consumeStringArray(String[] value);

		void consumeRawValue(String value);

	}

	private static void encodeXMLGregorianCalender(Datatype type, String rawValue, ValueSink consumer) {
		try {
			consumer.consumeXMLGregorianCalendar(DatatypeFactory.newInstance().newXMLGregorianCalendar(rawValue));
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException(String.format("Could not decode an xml gregorian calendar from raw value '%s' of type '%s'",rawValue,type.toURI()),e);
		}
	}

	private static void encodeDuration(Datatype type, String rawValue, ValueSink consumer) {
		try {
			consumer.consumeDuration(DatatypeFactory.newInstance().newDuration(rawValue));
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException(String.format("Could not decode a duration from raw value '%s' of type '%s'",rawValue,type.toURI()),e);
		}
	}

	private enum Namespace {

		XML_SCHEMA("http://www.w3.org/2001/XMLSchema#"),
		RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
		;

		private final String value;

		Namespace(String value) {
			this.value = value;
		}

		public URI toURI(String localPart) {
			return URI.create(value.concat(localPart));
		}

		public boolean belongsToNamespace(String uri) {
			return uri.startsWith(value);
		}

		public String localName(String uri) {
			String result = null;
			if(belongsToNamespace(uri)) {
				result=uri.substring(value.length());
			}
			return result;
		}

	}

	private enum BuiltInType {
		UR,
		PRIMITIVE,
		DERIVED
	}

	private enum DerivationType {
		RESTRICTION,
		LIST,
		EXTENSION_OR_RESTRICTION
	}

	private static final class DatatypeMetadata {

		private static final class Singleton {
			private static final DatatypeMetadata INSTANCE=new DatatypeMetadata();
			private Singleton() {
			}
		}

		private final ConcurrentMap<Datatype,List<Datatype>> ancestors;
		private final Lock ancestorsLock;

		private DatatypeMetadata() {
			ancestors=new ConcurrentHashMap<Datatype, List<Datatype>>();
			ancestorsLock=new ReentrantLock();
		}

		private void ancestors(Datatype type,List<Datatype> parents) {
			if(type.parent!=null) {
				parents.add(type.parent);
				ancestors(type.parent,parents);
			}
		}

		private List<Datatype> ancestors(Datatype type) {
			List<Datatype> result=new ArrayList<Datatype>();
			ancestors(type,result);
			return Collections.unmodifiableList(result);
		}

		List<Datatype> getAncestors(Datatype type) {
			List<Datatype> result = ancestors.get(type);
			if(result==null) {
				ancestorsLock.lock();
				try {
					result=ancestors.get(type);
					if(result==null) {
						result=ancestors(type);
						ancestors.put(type, result);
					}
				} finally {
					ancestorsLock.unlock();
				}
			}
			return result;
		}

		public static DatatypeMetadata getInstance() {
			return Singleton.INSTANCE;
		}

	}

	private static class TypeSelector {

		private static class XSTComparator implements Comparator<Datatype>, Serializable {

			/**
			 *
			 */
			private static final long serialVersionUID = -1261762409645119729L;

			@Override
			public int compare(Datatype o1, Datatype o2) {
				if(o1.equals(o2)) {
					return 0;
				}
				int result = compareByAncestors(o1, o2);
				if(result==0) {
					assertEqualRawTypes(o1, o2);
					result=compareByMeta(o1, o2);
				}
				return result;
			}

			/**
			 * @param o1
			 * @param o2
			 * @return
			 */
			private int compareByMeta(Datatype o1, Datatype o2) {
				List<Integer> comparisons=new ArrayList<Integer>();
				comparisons.add(o1.depth()-o2.depth());
				comparisons.add(safeCompare(o2.type,o1.type));
				comparisons.add(safeCompare(o2.derivation,o1.derivation));
				comparisons.add(o1.ordinal()-o2.ordinal());
				for(Integer c:comparisons) {
					if(c!=0) {
						return c;
					}
				}
				return 0;
			}

			private <T> int safeCompare(Comparable<T> o1, T o2) {
				if(o1==null) {
					if(o2==null) {
						return 0;
					} else {
						return -1;
					}
				} else if(o2==null) {
					return 1;
				} else {
					return o1.compareTo(o2);
				}
			}

			/**
			 * @param o1
			 * @param o2
			 */
			private void assertEqualRawTypes(Datatype o1, Datatype o2) {
				if(o1.rawClass()!=o2.rawClass()) {
					throw new IllegalArgumentException("Types cannot be compared because they are not related in any manner");
				}
			}

			/**
			 * @param o1
			 * @param o2
			 * @return
			 */
			private int compareByAncestors(Datatype o1, Datatype o2) {
				int result=0;
				if(o1.ancestors().contains(o2)) {
					result=+1;
				} else if(o2.ancestors().contains(o1)) {
					result=-1;
				}
				return result;
			}

		}

		private static interface Selector<T> {
			public List<Datatype> select(T value);
		}

		private static class IntegerSelector implements Selector<Integer> {
			@Override
			public List<Datatype> select(Integer value) {
				List<Datatype> result=new ArrayList<Datatype>();
				if(value>=0 && value<=65535) {
					result.add(Datatype.UNSIGNED_SHORT);
				}
				result.add(Datatype.INT);
				return result;
			}
		}

		private static class LongSelector implements Selector<Long> {
			@Override
			public List<Datatype> select(Long value) {
				List<Datatype> result=new ArrayList<Datatype>();
				if(value>=0) {
					if(value<=4294967295L) {
						result.add(Datatype.UNSIGNED_INT);
					}
					result.add(Datatype.UNSIGNED_LONG);
				}
				result.add(Datatype.LONG);
				return result;
			}
		}

		private static class ShortSelector implements Selector<Short> {
			@Override
			public List<Datatype> select(Short value) {
				List<Datatype> result=new ArrayList<Datatype>();
				if(value>=0 && value<=255) {
					result.add(Datatype.UNSIGNED_BYTE);
				}
				result.add(Datatype.SHORT);
				return result;
			}
		}

		private static class BigIntegerSelector implements Selector<BigInteger> {
			@Override
			public List<Datatype> select(BigInteger value) {
				List<Datatype> result=new ArrayList<Datatype>();
				int sign=value.signum();
				if(sign<0) {
					result.add(Datatype.NON_POSITIVE_INTEGER);
					result.add(Datatype.NEGATIVE_INTEGER);
				} else if(sign>0) {
					result.add(Datatype.NON_NEGATIVE_INTEGER);
					result.add(Datatype.POSITIVE_INTEGER);
				} else {
					result.add(Datatype.NON_NEGATIVE_INTEGER);
					result.add(Datatype.NON_POSITIVE_INTEGER);
				}
				result.add(Datatype.INTEGER);
				return result;
			}
		}

		private static IntegerSelector INTEGER_SELECTOR=new IntegerSelector();
		private static LongSelector LONG_SELECTOR=new LongSelector();
		private static ShortSelector SHORT_SELECTOR=new ShortSelector();
		private static BigIntegerSelector BIG_INTEGER_SELECTOR=new BigIntegerSelector();

		private TypeSelector() {
		}

		private static List<Datatype> classEncodings(Class<?> value) {
			Collection<Datatype> result = new PriorityQueue<Datatype>(10,new XSTComparator());
			if(value!=null) {
				for(Datatype type:values()) {
					Class<?> rawClass = type.rawClass();
					if(rawClass!=null && rawClass.isAssignableFrom(value)) {
						result.add(type);
					}
				}
			}
			return new ArrayList<Datatype>(result);
		}

		public static <T> List<Datatype> select(T value) {
			List<Datatype> result=null;
			if(value instanceof Integer) {
				result=INTEGER_SELECTOR.select((Integer)value);
			} else if(value instanceof Long) {
				result=LONG_SELECTOR.select((Long)value);
			} else if(value instanceof Short) {
				result=SHORT_SELECTOR.select((Short)value);
			} else if(value instanceof BigInteger) {
				result=BIG_INTEGER_SELECTOR.select((BigInteger)value);
			} else {
				result=classEncodings(value.getClass());
			}
			return result;
		}

	}

}