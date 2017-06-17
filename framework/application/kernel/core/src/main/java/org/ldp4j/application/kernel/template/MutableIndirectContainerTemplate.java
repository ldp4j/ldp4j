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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-core:0.2.2
 *   Bundle      : ldp4j-application-kernel-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.template;

import java.net.URI;
import java.util.Objects;

import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.kernel.template.IndirectContainerTemplate;
import org.ldp4j.application.kernel.template.TemplateVisitor;

import com.google.common.base.MoreObjects.ToStringHelper;

final class MutableIndirectContainerTemplate extends MutableMembershipAwareContainerTemplate implements IndirectContainerTemplate {

	private final URI insertedContentRelation;

	MutableIndirectContainerTemplate(String id, Class<? extends ContainerHandler> handlerClass, URI insertedContentRelation) {
		super(id, handlerClass);
		this.insertedContentRelation = insertedContentRelation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(TemplateVisitor visitor) {
		visitor.visitIndirectContainerTemplate(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI insertedContentRelation() {
		return this.insertedContentRelation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return
			Objects.
				hash(super.hashCode(),this.insertedContentRelation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=super.equals(obj);
		if(result && this.getClass()==obj.getClass()) {
			MutableIndirectContainerTemplate that = (MutableIndirectContainerTemplate) obj;
			result=Objects.equals(this.insertedContentRelation, that.insertedContentRelation);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ToStringHelper stringHelper() {
		return
			super.
				stringHelper().
					add("insertedContentRelation",this.insertedContentRelation);
	}

}
