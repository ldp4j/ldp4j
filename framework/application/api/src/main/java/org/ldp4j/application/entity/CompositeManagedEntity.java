package org.ldp4j.application.entity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.entity.EntityAggregation.EntityAggregationBuilder;
import org.ldp4j.application.entity.util.ListenerManager;
import org.ldp4j.application.entity.util.Notification;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

final class CompositeManagedEntity extends ManagedEntity {

	private final class UpdateNotification implements Notification<Recycler> {
		@Override
		public void propagate(Recycler listener) {
			listener.onUpdate(CompositeManagedEntity.this);
		}
	}

	private final class Recycler {
		public void onUpdate(CompositeManagedEntity source) {
			invalidateAggregation(source);
		}
	}

	private enum DeletionStrategy {
		SHALLOW,
		DEEP
	}

	private final ManagedEntity self;
	private final List<CompositeManagedEntity> externals;

	private final AtomicReference<DeletionStrategy> deletionStrategy;

	private final ListenerManager<Recycler> listeners;
	private final Recycler recycler;
	private final UpdateNotification modified;

	private EntityAggregation aggregation;

	private CompositeManagedEntity(ManagedEntity self) {
		this.self=self;
		this.externals=Lists.newArrayList();
		this.deletionStrategy=new AtomicReference<DeletionStrategy>(DeletionStrategy.SHALLOW);
		this.recycler=new Recycler();
		this.modified=new UpdateNotification();
		this.listeners=ListenerManager.newInstance();
	}

	private EntityAggregation buildAggregation() {
		EntityAggregationBuilder builder=
			EntityAggregation.
				builder().
					withOwner(this);
		Queue<CompositeManagedEntity> toCollect=new LinkedList<CompositeManagedEntity>();
		Set<CompositeManagedEntity> collected=Sets.newLinkedHashSet();
		CompositeManagedEntity first=this;
		while(first!=null) {
			if(!collected.contains(first)) {
				collected.add(first);
				builder.withPart(first.self);
				toCollect.addAll(first.externals);
			}
			first=toCollect.poll();
		}
		return builder.build();
	}

	private synchronized EntityAggregation getAggregation() {
		if(this.aggregation==null) {
			this.aggregation=buildAggregation();
		}
		return this.aggregation;
	}

	private void invalidateAggregation(CompositeManagedEntity changed) {
		synchronized(this) {
			this.aggregation=null;
		}
		if(changed!=this) {
			this.listeners.notify(changed.modified);
		}
	}

	private BaseEntity selectTarget() {
		DeletionStrategy strategy = this.deletionStrategy.get();
		switch(strategy) {
		case DEEP:
			return this.self;
		case SHALLOW:
			return this.getAggregation();
		default:
			throw new IllegalStateException("Unsupported deletion strategy "+strategy);
		}
	}

	// Lifecycle protocol

	@Override
	void attach(UUID id, CompositeDataSource dataSource) {
		this.self.attach(id, dataSource);
	}

	@Override
	void dettach(CompositeDataSource dataSource) {
		this.self.dettach(dataSource);
	}

	// Aggregation protocol

	void join(CompositeManagedEntity entity) {
		checkNotNull(entity,"Entity cannot be null");
		checkArgument(entity!=this,"Self attachment is disallowed");
		checkArgument(entity.identity().equals(this.identity()),"Cannot attach entities of different entities");
		boolean dirty=false;
		synchronized(this) {
			if(!this.externals.contains(entity)) {
				this.externals.add(entity);
				this.aggregation=null;
				dirty=true;
			}
		}
		if(dirty) {
			entity.listeners.registerListener(this.recycler);
			this.listeners.notify(this.modified);
		}
	}

	void leave(CompositeManagedEntity entity) {
		checkNotNull(entity,"Entity cannot be null");
		checkArgument(entity!=this,"Self dettachment is disallowed");
		checkArgument(entity.identity().equals(this.identity()),"Cannot dettach entities of different entities");
		boolean dirty=false;
		synchronized(this) {
			if(this.externals.remove(entity.self)) {
				this.aggregation=null;
				dirty=true;
			}
		}
		if(dirty) {
			this.listeners.notify(this.modified);
			entity.listeners.deregisterListener(this.recycler);
		}
	}

	// Configuration API

	CompositeManagedEntity deletionStrategy(DeletionStrategy deletionStrategy) {
		checkNotNull(deletionStrategy,"Deletion strategy cannot be null");
		this.deletionStrategy.set(deletionStrategy);
		return this;
	}

	@Override
	void removeProperties(Entity removedEntity) {
		selectTarget().removeProperties(removedEntity);
	}

	// Public API

	@Override
	public CompositeDataSource dataSource() {
		return this.self.dataSource();
	}

	@Override
	public UUID id() {
		return this.self.id();
	}

	@Override
	public Identity identity() {
		return this.self.identity();
	}

	@Override
	public ImmutableProperty getProperty(URI predicate) {
		return this.getAggregation().getProperty(predicate);
	}

	@Override
	public void addProperty(URI predicate, Literal<?> literal) {
		this.self.addProperty(predicate, literal);
	}

	@Override
	public void addProperty(URI predicate, Entity entity) {
		CompositeManagedEntity surrogate=this.self.dataSource().merge(entity);
		this.self.addProperty(predicate, surrogate);
	}

	@Override
	public void removeProperty(URI predicate, Literal<?> literal) {
		selectTarget().removeProperty(predicate,literal);
	}

	@Override
	public void removeProperty(URI predicate, Entity entity) {
		selectTarget().removeProperty(predicate,entity);
	}

	@Override
	public void removeProperty(URI predicate) {
		selectTarget().removeProperty(predicate);
	}

	@Override
	public void accept(ValueVisitor visitor) {
		visitor.visitEntity(this);
	}

	static CompositeManagedEntity wrap(Entity entity) {
		CompositeManagedEntity part=null;
		if(entity instanceof CompositeManagedEntity) {
			part=(CompositeManagedEntity)entity;
		} else if(entity instanceof ManagedEntity){
			part=create((ManagedEntity)entity);
		} else {
			// TODO: We could make this behaviour configurable. We could select
			// between a) failing, b) wrapping, c) deep cloning.
			part=create(ManagedEntityAdapter.create(entity));
		}
		return part;
	}

	static CompositeManagedEntity create(ManagedEntity self) {
		return new CompositeManagedEntity(self);
	}

	@Override
	Collection<ImmutableProperty> properties() {
		return this.getAggregation().properties();
	}

}