package org.ldp4j.application.entity;

import static com.google.common.base.Preconditions.*;

import java.net.URI;


public final class IdentityFactory {

	private IdentityFactory() {
	}

	public static Local<?> createLocalIdentity(DataSource dataSource) {
		checkNotNull(dataSource,"Data source cannot be null");
		return Local.create(dataSource.identifier(),dataSource.nextName());
	}

	public static <T> Managed<T> createManagedIdentity(Key<T> key) {
		checkNotNull(key,"Key cannot be null");
		return Managed.create(key);
	}

	public static <T,V> Managed<T> createManagedIdentity(Class<T> owner, V nativeId) {
		checkNotNull(owner,"Key owner cannot be null");
		checkNotNull(nativeId,"Key native identifier cannot be null");
		return createManagedIdentity(Key.create(owner, nativeId));
	}

	public static External createExternalIdentity(URI location) {
		checkNotNull(location,"Location cannot be null");
		return External.create(location);
	}

}
