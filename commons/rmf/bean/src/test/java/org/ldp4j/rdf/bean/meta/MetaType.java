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
package org.ldp4j.rdf.bean.meta;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.ldp4j.rdf.bean.meta.MetaType.PropertyScanner.FilteringMemberCollector.Filter;
import org.ldp4j.rdf.bean.util.BeanUtils;

public final class MetaType<T> extends MetaAnnotatedObject<AnnotatedClass<T>> {

	public static class PropertyScanner {

		private static interface MemberCollector<S extends Member, T extends AnnotatedMember<S>> {
			Collection<T> collect(AnnotatedClass<?> root);
		}

		protected abstract static class FilteringMemberCollector<S extends Member, T extends AnnotatedMember<S>> implements PropertyScanner.MemberCollector<S,T> {

			public interface Filter<S extends Member, T extends AnnotatedMember<S>> {
				boolean isValid(T member);
			}

			private Filter<S,T> filter;

			public final void setFilter(Filter<S,T> filter) {
				this.filter = filter;
			}

			public final Filter<S,T> getFilter() {
				if(this.filter==null) {
					this.filter=new Filter<S,T>() {
						@Override
						public boolean isValid(T member) {
							return false;
						}
					};
				}
				return filter;
			}

			@Override
			public final Collection<T> collect(AnnotatedClass<?> root) {
				List<T> result=new ArrayList<T>();
				Filter<S,T> filterInUse=getFilter();
				AnnotatedClass<?> clazz=root;
				while(clazz!=null) {
					for(T member:getRawMembers(clazz)) {
						if(filterInUse.isValid(member)) {
							result.add(member);
						}
					}
					clazz=clazz.getSuperclass();
				}
				return result;
			}

			protected abstract Collection<T> getRawMembers(AnnotatedClass<?> clazz);
		}

		public static class FilteringFieldCollector extends PropertyScanner.FilteringMemberCollector<Field,AnnotatedField> {
			@Override
			protected Collection<AnnotatedField> getRawMembers(AnnotatedClass<?> root) {
				return root.getDeclaredField();
			}
		}

		public static class FilteringMethodCollector extends PropertyScanner.FilteringMemberCollector<Method,AnnotatedMethod> {
			@Override
			protected Collection<AnnotatedMethod> getRawMembers(AnnotatedClass<?> root) {
				return root.getDeclaredMethods();
			}
		}

		private final static class GetterFilter implements Filter<Method,AnnotatedMethod> {
			@Override
			public boolean isValid(AnnotatedMethod member) {
				return BeanUtils.isGetter(member.get());
			}
		}

		public List<MetaProperty<?>> scan(AnnotatedClass<?> context) {
			List<MetaProperty<?>> result=new ArrayList<MetaProperty<?>>();
			for(Entry<String,List<AnnotatedMethod>> entry:getClassifiedGetters(context).entrySet()) {
				result.add(MetaProperty.forMethods(context,entry.getValue()));
			}
			for(AnnotatedField field:getCandidateFields(context)) {
				result.add(MetaProperty.forField(context, field));
			}
			return result;
		}

		private Map<String, List<AnnotatedMethod>> getClassifiedGetters(AnnotatedClass<?> metaClass) {
			Map<String,List<AnnotatedMethod>> classification=new HashMap<String,List<AnnotatedMethod>>();
			for(AnnotatedMethod method:getCandidateGetters(metaClass)){
				List<AnnotatedMethod> methods = classification.get(method.getName());
				if(methods==null) {
					methods=new ArrayList<AnnotatedMethod>();
					classification.put(method.getName(),methods);
				}
				methods.add(method);
			}
			return classification;
		}

		private Collection<AnnotatedMethod> getCandidateGetters(AnnotatedClass<?> metaClass) {
			PropertyScanner.FilteringMethodCollector collector = new FilteringMethodCollector();
			collector.setFilter(new GetterFilter());
			return collector.collect(metaClass);
		}

		private Collection<AnnotatedField> getCandidateFields(AnnotatedClass<?> metaClass) {
			PropertyScanner.FilteringFieldCollector collector = new FilteringFieldCollector();
			collector.setFilter(
				new Filter<Field,AnnotatedField>(){
					@Override
					public boolean isValid(AnnotatedField member) {
						return true;
					}
				}
			);
			return collector.collect(metaClass);
		}

	}

	private final Map<String, MetaProperty<?>> metaproperties;
	private final AnnotatedClass<T> clazz;

	private MetaType(final AnnotatedClass<T> clazz, List<MetaProperty<?>> properties) {
		super(clazz,getClassHierarchy(clazz));
		this.clazz = clazz;
		this.metaproperties = new HashMap<String,MetaProperty<?>>();
		for(MetaProperty<?> property:properties) {
			if(!metaproperties.containsKey(property.getName())) {
				this.metaproperties.put(property.getName(), property);
			} else {
				String newPropertyName=property.getName()+"#"+property.getOwnerType();
				this.metaproperties.put(newPropertyName, MetaProperty.rename(newPropertyName, property));
			}
		}
	}

	private static List<? extends Annotated<?>> getClassHierarchy(AnnotatedClass<?> clazz) {
		List<Annotated<?>> result=new ArrayList<Annotated<?>>();
		AnnotatedClass<?> root=clazz;
		while(root!=null){
			result.add(root);
			root=root.getSuperclass();
		}
		return result;
	}

	@Override
	public AnnotatedClass<T> get() {
		return clazz;
	}

	public boolean hasProperty(String propertyName) {
		return metaproperties.containsKey(propertyName);
	}

	public Set<String> properties() {
		return new TreeSet<String>(metaproperties.keySet());
	}

	public MetaProperty<?> getProperty(String propertyName) {
		return metaproperties.get(propertyName);
	}

	public static <T> MetaType<T> forClass(Class<T> clazz) {
		MetaType.PropertyScanner scanner=new PropertyScanner();
		AnnotatedClass<T> metaClass=AnnotatedClass.forClass(clazz);
		return new MetaType<T>(metaClass,scanner.scan(metaClass));
	}

	@SuppressWarnings("unchecked")
	public static <T> MetaType<T> forObject(T object) {
		return forClass((Class<T>)object.getClass());
	}
}