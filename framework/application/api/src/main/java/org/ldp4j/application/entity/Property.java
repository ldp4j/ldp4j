package org.ldp4j.application.entity;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public final class Property implements Iterable<Value> {

	private final URI predicate;
	private final Entity entity;
	private final List<Literal<?>> literals;
	private final List<Entity> entities;

	Property(URI predicate, Entity entity) {
		this.predicate=predicate;
		this.entity=entity;
		this.literals=Lists.newArrayList();
		this.entities=Lists.newArrayList();
	}

	void addValue(Literal<?> value) {
		if(!this.literals.contains(value)) {
			this.literals.add(value);
		}
	}

	void addValue(Entity value) {
		if(!this.entities.contains(value)) {
			this.entities.add(value);
		}
	}

	void removeValue(Literal<?> value) {
		this.literals.remove(value);
	}

	void removeValue(Entity value) {
		this.entities.remove(value);
	}

	public Entity entity() {
		return this.entity;
	}

	public URI predicate() {
		return this.predicate;
	}

	public boolean hasValues() {
		return !this.literals.isEmpty() || !this.entities.isEmpty();
	}

	public Iterable<Literal<?>> literalValues() {
		return ImmutableList.copyOf(this.literals);
	}

	public Iterable<Entity> entityValues() {
		return ImmutableList.copyOf(this.entities);
	}

	@Override
	public Iterator<Value> iterator() {
		return
			ImmutableList.
				<Value>builder().
					addAll(this.literals).
					addAll(this.entities).
					build().
						iterator();
	}

}