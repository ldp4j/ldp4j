package org.ldp4j.application.entity;

import java.net.URI;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public abstract class Identity implements Comparable<Identity> {

	private URI identifier;

	Identity(URI identifier) {
		this.identifier = identifier;
	}

	@Override
	public final int compareTo(Identity that) {
		return IdentityUtil.compare(this, that);
	}

	public final URI identifier() {
		return this.identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Identity) {
			Identity that = (Identity) obj;
			result=Objects.equal(this.identifier, that.identifier);
		}
		return result;
	}

	@Override
	public final String toString() {
		ToStringHelper helper=
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
					add("identifier",this.identifier);
		toString(helper);
		return helper.toString();
	}

	protected void toString(ToStringHelper helper) {
	}

	public abstract void accept(IdentityVisitor visitor);

}