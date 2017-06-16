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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-api:0.2.2
 *   Bundle      : ldp4j-application-api-0.2.2.jar
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
 * Used for defining LDP resources that are attached to other LDP resources.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Attachment {

	/**
	 * The identifier of the attachment. The identifier must be
	 * template-unique.
	 *
	 * @return the identifier of the attachment.
	 */
	String id();

	/**
	 * The human-based name of the attachment.
	 *
	 * @return the name of the attachment.
	 */
	String name() default "";

	/**
	 * The description of the purpose of the attachment.
	 *
	 * @return the description of the attachment.
	 */
	String description() default "";

	/**
	 * The relative path to be used when publishing the attached resource.
	 *
	 * @return the relative path of the attachment
	 */
	String path();

	/**
	 * If defined, the predicate to be used for relating the attaching resource
	 * to the attached resource
	 *
	 * @return the predicate used to related the attachment to its parent
	 *         resource.
	 */
	String predicate() default "";

	/**
	 * The {@code ResourceHandler} class that will handle the attached resource.
	 *
	 * @return the class that handles the attache resource.
	 */
	Class<? extends ResourceHandler> handler();

}
