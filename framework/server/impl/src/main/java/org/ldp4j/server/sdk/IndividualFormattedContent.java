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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.sdk;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.namespace.QName;

import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;
import org.ldp4j.server.sdk.IndividualFormattedContent.Individual.Value;

public final class IndividualFormattedContent implements IContent {
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static final String EMPTY_TURTLE = 
		"@prefix dc: <http://purl.org/dc/elements/1.1/>."+LINE_SEPARATOR+
		"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.";

	private static final String EMPTY_RDF_XML_HEADER = 
	"<rdf:RDFS xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+LINE_SEPARATOR+
	"\txmlns:dc=\"http://purl.org/dc/elements/1.1/\">"+LINE_SEPARATOR;

	private static final String EMPTY_RDF_XML_FOOTER = 
	"</rdf:RDFS>";

	private final Format format;

	private final Individual[] individuals;

	public static final class Individual {
		
		static final class Value {

			private final QName resource;
			private final String literal;

			public Value(QName resource) {
				this.resource = resource;
				this.literal=null;
			}

			public Value(String literal) {
				this.resource = null;
				this.literal=literal;
			}

			public String format(Format format) {
				String result=null;
				switch(format) {
				case RDFXML:
					if(isResource()) {
						result=resource.getNamespaceURI()+resource.getLocalPart();
					} else {
						// Hopefully the value is encoded properly
						result=literal;
					}
					break;
				default: 
					// Is the case of TURTLE:
					if(isResource()) {
						result="<"+resource.getNamespaceURI()+resource.getLocalPart()+">";
					} else {
						// Hopefully the value is encoded properly
						result="\""+literal+"\"";
					}
					break;
				}
				return result;
			}

			public boolean isBlankNode() {
				return false;
			}

			public boolean isResource() {
				return resource!=null;
			}
			
		}
		
		private QName resource;
		private final Map<QName,Set<Value>> properties=new HashMap<QName,Set<Value>>();
		private boolean has_properties;

		public QName getResource() {
			return resource;
		}
		
		private void setResource(QName resource) {
			this.resource = resource;
		}

		private void addProperty(QName property, Value value) {
			Set<Value> set = properties.get(property);
			if(set==null) {
				set=new HashSet<Value>();
				properties.put(property, set);
			}
			set.add(value);
			has_properties=true;
		}
		
		public Collection<QName> getProperties() {
			return properties.keySet();
		}
		public Collection<Value> getPropertyValues(QName property) {
			Set<Value> values = properties.get(property);
			if(values==null) {
				values=Collections.emptySet();
			}
			return Collections.unmodifiableSet(values);
		}

		public boolean hasProperties() {
			return has_properties;
		}
		
	}
	
	public interface Placeholder<T> {

		T build();

	}

	
	public static final class Placeholders {
		
		private Placeholders() {
		}
		
		public static QNameBuilder qualifiedName(String name) {
			return new QNameBuilder(name);
		}
		
		public static Placeholder<Value> resource(final Placeholder<QName> qname) {
			return new Placeholder<Value>() {

				@Override
				public Value build() {
					return new Value(qname.build());
				}
				
			};
		}

		public static Placeholder<Value> literal(final String literal) {
			return new Placeholder<Value>() {

				@Override
				public Value build() {
					return new Value(literal);
				}
				
			};
		}

	}
		
	public static final class QNameBuilder implements Placeholder<QName> {
		
		private static final AtomicLong NS_PREFIX_COUNTER=new AtomicLong(); 
		private static final AtomicLong NS_COUNTER=new AtomicLong(); 
		
		private String namespaceURI;
		private String localPart;
		private String prefix;

		private QNameBuilder(String localPart) {
			this.localPart = localPart;
		}

		public QNameBuilder withNamespace(String namespace) {
			this.namespaceURI = namespace;
			return this;
		}

		public QNameBuilder withNamespacePrefix(String prefix) {
			this.prefix = prefix;
			return this;
		}
		
		public QName build() {
			String localNamespace=namespaceURI;
			if(localNamespace==null) {
				localNamespace="http://www.example.org/v"+NS_COUNTER.incrementAndGet()+"#";
			}
			String localPrefix=prefix;
			if(localPrefix==null) {
				localPrefix="ns"+NS_PREFIX_COUNTER.incrementAndGet();
			}
			return new QName(localNamespace, localPart, localPrefix);
		}
		
		
	}
	
	public static final class IndividualBuilder {
		
		private Individual individual=new Individual();

		private IndividualBuilder(QName resource) {
			individual=new Individual();
			individual.setResource(resource);
		}
		
		public IndividualBuilder withPropertyValue(Placeholder<QName> property, Placeholder<Value> value) {
			individual.addProperty(property.build(), value.build());
			return this;
		}
		
		public Individual build() {
			Individual result=individual;
			individual=new Individual();
			individual.setResource(result.getResource());
			return result;
		}

	}

	public static IndividualBuilder individual(Placeholder<QName> resource) {
		return newIndividualBuilder(resource.build());
	}

	public static IndividualBuilder newIndividualBuilder(QName resource) {
		return new IndividualBuilder(resource);
	}
	
	
	public IndividualFormattedContent(Format format,Individual...individuals) {
		this.format = format;
		this.individuals = individuals;
	}

	@Override
	public <S> S serialize(Class<S> clazz) throws IOException {
		StringBuilder builder=new StringBuilder();
		if(Format.TURTLE.equals(format)) {
			builder.append(EMPTY_TURTLE);
			for(Individual individual:individuals) {
				if(individual.hasProperties()) {
					builder.append(LINE_SEPARATOR);
					builder.append("<").append(individual.getResource().getNamespaceURI()+individual.getResource().getLocalPart()).append("> ");
					builder.append(LINE_SEPARATOR);
					boolean firstProperty=true;
					for(QName property:individual.getProperties()) {
						if(!firstProperty) {
							builder.append(";").append(LINE_SEPARATOR);
						} else {
							firstProperty=false;
						}
						builder.append("\t<").append(property.getNamespaceURI()+property.getLocalPart()).append("> ");
						boolean firstValue=true;
						for(Value value:individual.getPropertyValues(property)) {
							if(!firstValue) {
								builder.append(", ");
							} else {
								firstValue=false;
							}
							builder.append(value.format(format));
						}
					}
				}
				builder.append(".").append(LINE_SEPARATOR);
			}
		} else {
			builder.append(EMPTY_RDF_XML_HEADER);
			builder.append(EMPTY_RDF_XML_FOOTER);
		}
		return new StringContent(builder.toString()).serialize(clazz);
	}
}