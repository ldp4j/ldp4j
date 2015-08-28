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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.ext.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.ldp4j.application.ext.ResourceHandler;

/**
 * Used for defining templates for Indirect Container LDP Resources.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface IndirectContainer {

	/**
	 * The identifier of the template. The identifier must be
	 * application-unique.
	 */
	String id();

	/** The human-based name of the template. */
	String name() default "";

	/** A description of the purpose of the template. */
	String description() default "";

	/** The attachments of the template. */
	Attachment[] attachments() default {};

	/**
	 * The {@code ResourceHandler} class that will handle the member resources
	 * of the container.
	 */
	Class<? extends ResourceHandler> memberHandler();

	/** If defined, the path prefix to be used when publishing member resources. */
	String memberPath() default "";

	/**
	 * The membership predicate to use for relating the member resources with
	 * the container.
	 */
	String membershipPredicate() default "http://www.w3.org/ns/ldp#member";

	/**
	 * The membership relation to use for defining direction of the relationship
	 * between the member resources with the container .
	 */
	MembershipRelation membershipRelation() default MembershipRelation.HAS_MEMBER;

	/**
	 * The predicate that should be used for specifying the indirect identifier
	 * (URIRef) of a member of the container
	 */
	String insertedContentRelation() default "http://www.w3.org/ns/ldp#MemberSubject";

}
