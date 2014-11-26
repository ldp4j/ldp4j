package org.ldp4j.application.entity;

import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

final class EntityAggregation extends BaseEntity {

	static final class EntityAggregationBuilder {

		private final List<BaseEntity> parts;
		private CompositeManagedEntity owner;

		private EntityAggregationBuilder() {
			parts=Lists.newArrayList();
		}

		public EntityAggregation.EntityAggregationBuilder withOwner(CompositeManagedEntity owner) {
			if(owner!=null) {
				this.owner = owner;
			}
			return this;
		}

		public EntityAggregation.EntityAggregationBuilder withPart(ManagedEntity part) {
			if(part!=null) {
				this.parts.add(part);
			}
			return this;
		}

		EntityAggregation build() {
			checkState(this.owner!=null,"No entity aggregation owner specified");
			checkState(!this.parts.isEmpty(),"No entity aggregation parts specified");
			return new EntityAggregation(this.owner,ImmutableList.copyOf(this.parts));
		}

	}

	private final List<BaseEntity> parts;
	private final CompositeManagedEntity owner;

	private EntityAggregation(CompositeManagedEntity owner,List<BaseEntity> parts) {
		this.owner=owner;
		this.parts=parts;
	}

	private ImmutableProperty createProperty(URI predicate) {
		return new ImmutableProperty(predicate,this.owner);
	}

	private void populateProperties(Builder<? super ImmutableProperty> builder) {
		Map<URI,ImmutableProperty> properties=Maps.newLinkedHashMap();
		for(BaseEntity part:this.parts) {
			for(ImmutableProperty tmp:part.properties()) {
				URI predicate = tmp.predicate();
				ImmutableProperty current = properties.get(predicate);
				if(current==null) {
					current=createProperty(predicate);
				}
				properties.put(predicate, current.merge(tmp));
			}
		}
		for(ImmutableProperty property:properties.values()) {
			builder.add(property);
		}
	}

	@Override
	void removeProperties(Entity removedEntity) {
		for(BaseEntity part:this.parts) {
			part.removeProperties(removedEntity);
		}
	}

	@Override
	Collection<ImmutableProperty> properties() {
		Builder<ImmutableProperty> builder = ImmutableList.<ImmutableProperty>builder();
		populateProperties(builder);
		return builder.build();
	}

	@Override
	public CompositeDataSource dataSource() {
		return this.owner.dataSource();
	}

	@Override
	public UUID id() {
		return this.owner.id();
	}

	@Override
	public Identity identity() {
		return this.owner.identity();
	}

	@Override
	public ImmutableProperty getProperty(URI predicate) {
		ImmutableProperty property=createProperty(predicate);
		for(BaseEntity part:this.parts) {
			ImmutableProperty tmp=part.getProperty(predicate);
			property=property.merge(tmp);
		}
		return property;
	}

	@Override
	public void addProperty(URI predicate, Literal<?> literal) {
		throw new UnsupportedOperationException("Aggregations cannot be modified");
	}

	@Override
	public void addProperty(URI predicate, Entity entity) {
		throw new UnsupportedOperationException("Aggregations cannot be modified");
	}

	@Override
	public void removeProperty(URI predicate) {
		for(BaseEntity part:this.parts) {
			part.removeProperty(predicate);
		}
	}

	@Override
	public void removeProperty(URI predicate, Entity entity) {
		for(BaseEntity part:this.parts) {
			part.removeProperty(predicate,entity);
		}
	}

	@Override
	public void removeProperty(URI predicate, Literal<?> literal) {
		for(BaseEntity part:this.parts) {
			part.removeProperty(predicate,literal);
		}
	}

	@Override
	public void accept(ValueVisitor visitor) {
		visitor.visitEntity(this);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.parts);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=obj!=this;
		if(!result && obj.getClass()==getClass()) {
			EntityAggregation that=(EntityAggregation)obj;
			result=Objects.equals(this.parts,that.parts);
		}
		return result;
	}

	static EntityAggregationBuilder builder() {
		return new EntityAggregationBuilder();
	}

}