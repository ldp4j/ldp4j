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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.kernel.template.AttachedTemplate;
import org.ldp4j.application.kernel.template.ResourceTemplate;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;

public abstract class AbstractMutableTemplate<T extends ResourceHandler> implements ResourceTemplate {

	private final String id;
	private final Class<? extends T> handlerClass;
	private final Map<String,AttachedTemplate> attachments=new LinkedHashMap<String,AttachedTemplate>();

	private String name;
	private String description;

	protected AbstractMutableTemplate(String id, Class<? extends T> handlerClass) {
		checkNotNull(id,"Template id cannot be null");
		checkArgument(!id.trim().isEmpty(),"Template id cannot be empty");
		this.id=id.trim();
		this.handlerClass = handlerClass;
	}

	void setName(String name) {
		this.name=name;
	}

	void setDescription(String description) {
		this.description=description;
	}

	MutableAttachedTemplate attachTemplate(String attachmentId, ResourceTemplate template, String path) {
		checkNotNull(attachmentId, "AttachmentSnapshot identifier cannot be null");
		checkNotNull(template, "Attached template cannot be null");
		checkNotNull(path, "Attached template path cannot be null");
		checkArgument(!attachments.containsKey(path),"Another template is attached at path '%s'",path);
		MutableAttachedTemplate newAttachment = new MutableAttachedTemplate(attachmentId,template,path);
		this.attachments.put(attachmentId, newAttachment);
		return newAttachment;
	}

	void detachTemplate(String attachmentId) {
		checkNotNull(attachmentId, "Attached template id cannot be null");
		checkArgument(attachments.containsKey(attachmentId),"No template '%s' is attached template",attachmentId);
		this.attachments.remove(attachmentId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String id() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends T> handlerClass() {
		return handlerClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<String> name() {
		return Optional.fromNullable(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<String> description() {
		return Optional.fromNullable(description);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AttachedTemplate> attachedTemplates() {
		return Collections.unmodifiableSet(new LinkedHashSet<AttachedTemplate>(attachments.values()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachedTemplate attachedTemplate(String attachmentId) {
		checkNotNull(attachmentId, "AttachmentSnapshot identifier cannot be null");
		return attachments.get(attachmentId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<AttachedTemplate> iterator() {
		return Collections.unmodifiableCollection(attachments.values()).iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> attachmentIds() {
		return Collections.unmodifiableSet(attachments.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return
			Objects.
				hash(this.id,this.handlerClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj!=null && this.getClass()==obj.getClass()) {
			AbstractMutableTemplate<?> that = (AbstractMutableTemplate<?>) obj;
			result=
				Objects.equals(this.id, that.id) &&
				Objects.equals(this.handlerClass, that.handlerClass);
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
				toString();
	}

	protected ToStringHelper stringHelper() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("id",this.id).
					add("handlerClass",this.handlerClass.getCanonicalName()).
					add("name",this.name).
					add("description",this.description).
					add("attachments", this.attachments);
	}

}