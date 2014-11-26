package org.ldp4j.application.entity.spi;

import java.net.URI;

import org.ldp4j.application.entity.ExternalIdentity;
import org.ldp4j.application.entity.Identity;
import org.ldp4j.application.entity.Key;
import org.ldp4j.application.entity.ManagedIdentity;
import org.ldp4j.application.entity.RelativeIdentity;

public interface IdentityFactory extends DataService {

	Identity createIdentity();

	<T> ManagedIdentity<T> createManagedIdentity(Key<T> key);

	ExternalIdentity createExternalIdentity(URI location);

	<T> RelativeIdentity<T> createRelativeIdentity(Key<T> parent, URI path);

}