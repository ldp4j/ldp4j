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
 * Used for defining templates for Basic Container LDP Resources.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface BasicContainer {

	/**
	 * The identifier of the template. The identifier must be
	 * application-unique.
	 *
	 * @return the identifier of the template.
	 */
	String id();

	/**
	 * The human-based name of the template.
	 *
	 * @return the name of the template.
	 */
	String name() default "";

	/**
	 * A description of the purpose of the template.
	 *
	 * @return the description of the template
	 */
	String description() default "";

	/**
	 * The attachments of the template.
	 *
	 * @return the attachments of the template
	 */
	Attachment[] attachments() default {};

	/**
	 * The {@code ResourceHandler} class that will handle the member resources
	 * of the container.
	 *
	 * @return the class that will handle the members of the container.
	 */
	Class<? extends ResourceHandler> memberHandler();

	/**
	 * If defined, the path prefix to be used when publishing member resources.
	 *
	 * @return the path prefix used for publishing member resources
	 */
	String memberPath() default "";

}
