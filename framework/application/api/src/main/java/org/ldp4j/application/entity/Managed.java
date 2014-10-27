package org.ldp4j.application.entity;

import java.net.URI;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public final class Managed<T> extends  Identity {

	private Key<T> key;

	private Managed(URI identifier, Key<T> key) {
		super(identifier);
		this.key = key;
	}

	public Key<T> key() {
		return this.key;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(IdentityVisitor visitor) {
		visitor.visitManaged(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode()+Objects.hashCode(this.key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=super.equals(obj);
		if(result && obj instanceof Managed) {
			Managed<?> that=(Managed<?>) obj;
			result=Objects.equal(this.key, that.key);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void toString(ToStringHelper helper) {
		helper.add("key",this.key);
	}

	static <T,V> Managed<T> create(Class<T> owner, V nativeId) {
		return create(Key.create(owner, nativeId));
	}

	static <T> Managed<T> create(Key<T> key) {
		URI identifier=
			IdentifierUtil.
				createManagedIdentifier(key);
		return new Managed<T>(identifier,key);
	}

}
