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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ldp4j.rdf.bean.Category;
import org.ldp4j.rdf.bean.InvalidDefinitionException;
import org.ldp4j.rdf.bean.Type;
import org.ldp4j.rdf.bean.Vocabulary;
import org.ldp4j.rdf.bean.annotations.VocabularyProvider;
import org.ldp4j.rdf.bean.spi.IVocabularyProvider;

final class DefinitionLoader {

	private static final class VocabularyLoader {

		private final TypeManager manager;

		private VocabularyLoader(TypeManager manager) {
			this.manager = manager;
		}

		Vocabulary load(Package target) {
			Vocabulary result = loadFromProvider(target);
			if(result==null) {
				result=loadFromDefinition(target);
			}
			return result;
		}

		private Vocabulary loadFromDefinition(Package target) {
			org.ldp4j.rdf.bean.annotations.Vocabulary vocabulary = target.getAnnotation(org.ldp4j.rdf.bean.annotations.Vocabulary.class);
			Vocabulary result=null;
			if(vocabulary!=null) {
				String namespace=getVocabularyNamespace(target);
				String prefix=StringUtils.nonEmptyOrNull(vocabulary.prefix());
				List<Type> types = getVocabularyTypes(vocabulary.types());
				result=new VocabularyDefinition(namespace,prefix,types);
			}
			return result;
		}

		private List<Type> getVocabularyTypes(Class<?>[] types){
			List<Type> result=new ArrayList<Type>();
			for(Class<?> clazz:types) {
				List<Type> definitions=manager.getTypes(clazz);
				if(definitions.isEmpty()) {
					throw new InvalidDefinitionException("Invalid vocabulary definition: class '"+clazz.getCanonicalName()+"' does not denote a type");
				}
				result.add(definitions.get(0));
			}
			return result;
		}

		private Vocabulary loadFromProvider(Package target) {
			VocabularyProvider provider=target.getAnnotation(VocabularyProvider.class);
			Vocabulary result=null;
			if(provider!=null) {
				Class<? extends IVocabularyProvider> clazz = provider.provider();
				try {
					result=clazz.newInstance().getInstance();
				} catch (InstantiationException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
			return result;
		}

		private static String getVocabularyNamespace(Package vocabulary) {
			if(vocabulary==null) {
				return null;
			}
			org.ldp4j.rdf.bean.annotations.Vocabulary definition=vocabulary.getAnnotation(org.ldp4j.rdf.bean.annotations.Vocabulary.class);
			if(definition==null) {
				return getPackageNamespace(vocabulary);
			}
			String result=StringUtils.nonEmptyOrNull(definition.namespace());
			if(result==null) {
				throw new InvalidDefinitionException("Invalid vocabulary declaration: namespace definition is required");
			}
			return result;
		}

		private static String getPackageNamespace(Package pack) {
			String[] segments=pack.getName().split("\\.");
			StringBuilder authority=new StringBuilder();
			if(segments.length==1){
				authority.append(segments[0]);
			} else {
				authority.append(segments[1]).append(".").append(segments[0]);
				for(int i=2;i<segments.length;i++) {
					authority.append("/").append(segments[i]);
				}
			}
			return String.format("http://%s/",authority);
		}

	}

	private static final class TypeLoader {

		private final TypeManager manager;

		private TypeLoader(TypeManager manager) {
			this.manager = manager;
		}

		Type load(Class<?> clazz) {
			Type result=manager.getRegistry().lookup(clazz);
			if(result==null) {
				loadTypeDefinition(clazz,manager);
				result=manager.getRegistry().lookup(clazz);
			}
			return result;
		}

		private String getTypeName(String name, Class<?> clazz) {
			return StringUtils.nonEmptyOrDefault(name,clazz.getSimpleName());
		}

		private String getTypeNamespace(String namespace, Class<?> clazz) {
			String result=StringUtils.nonEmptyOrNull(namespace);
			if(result==null) {
				Package targetPackage=clazz.getPackage();
				if(targetPackage==null) {
					throw new InvalidDefinitionException("Invalid type declaration: orphan types are not allowed");
				}
				result=VocabularyLoader.getVocabularyNamespace(targetPackage);
			}
			return result;
		}

		private void loadTypeDefinition(Class<?> clazz, TypeManager manager) {
			org.ldp4j.rdf.bean.annotations.Type type = clazz.getAnnotation(org.ldp4j.rdf.bean.annotations.Type.class);
			if(type!=null) {
				String name=getTypeName(type.name(), clazz);
				String namespace=getTypeNamespace(type.namespace(),clazz);
				Category category=type.category();
				TypeDefinition definition=new TypeDefinition(name,namespace,category,clazz);
				manager.getRegistry().register(clazz, definition);
				PropertyScanner scanner=new PropertyScanner(clazz, namespace);
				TypeDefinition.
					initiliazeProperties(
						definition,
						scanner.getProperties(manager)
					);
				if(Category.ENUMERATION.equals(category)) {
					TypeDefinition.
						setHelper(
							definition,
							EnumerationHelper.newInstance(clazz)
						);
				}
			}
		}
	}

	private DefinitionLoader(){
	}

	static Vocabulary loadVocabulary(Package target, TypeManager manager) {
		Objects.requireNonNull(target,"Target package cannot be null");
		Objects.requireNonNull(manager,"Type manager cannot be null");
		return new VocabularyLoader(manager).load(target);
	}

	static Type loadType(Class<?> clazz, TypeManager manager) {
		Objects.requireNonNull(clazz,"Class cannot be null");
		Objects.requireNonNull(manager,"Type manager cannot be null");
		return new TypeLoader(manager).load(clazz);
	}

}
