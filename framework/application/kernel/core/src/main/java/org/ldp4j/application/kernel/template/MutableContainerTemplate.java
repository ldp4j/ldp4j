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

import java.util.Objects;

import org.ldp4j.application.ext.ContainerHandler;
import org.ldp4j.application.kernel.template.ContainerTemplate;
import org.ldp4j.application.kernel.template.ResourceTemplate;
import org.ldp4j.application.kernel.template.TemplateVisitor;

import com.google.common.base.Optional;
import com.google.common.base.MoreObjects.ToStringHelper;

public class MutableContainerTemplate extends AbstractMutableTemplate<ContainerHandler> implements ContainerTemplate {

	private ResourceTemplate memberTemplate;
	private String memberPath;

	public MutableContainerTemplate(String id, Class<? extends ContainerHandler> handlerClass) {
		super(id, handlerClass);
	}

	void setMemberTemplate(ResourceTemplate memberTemplate) {
		this.memberTemplate = memberTemplate;
	}

	void setMemberPath(String memberPath) {
		this.memberPath = memberPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(TemplateVisitor visitor) {
		visitor.visitContainerTemplate(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceTemplate memberTemplate() {
		return this.memberTemplate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<String> memberPath() {
		return Optional.fromNullable(this.memberPath);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return
			Objects.
				hash(super.hashCode(),this.memberTemplate,this.memberPath);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=super.equals(obj);
		if(result && this.getClass()==obj.getClass()) {
			MutableContainerTemplate that = (MutableContainerTemplate) obj;
			result=
				Objects.equals(this.memberTemplate, that.memberTemplate) &&
				Objects.equals(this.memberPath, that.memberPath);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			stringHelper().
				add("memberTemplate.id()",this.memberTemplate.id()).
				add("memberPath",this.memberPath).
				toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ToStringHelper stringHelper() {
		return
			super.
				stringHelper().
					add("memberTemplate",this.memberTemplate.id()).
					add("memberPath",this.memberPath);
	}

}