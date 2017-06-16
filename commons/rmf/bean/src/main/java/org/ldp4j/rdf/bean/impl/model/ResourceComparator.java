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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-bean:0.2.2
 *   Bundle      : rmf-bean-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.bean.impl.model;

import java.io.Serializable;
import java.util.Comparator;

import org.ldp4j.rdf.BlankNode;
import org.ldp4j.rdf.Resource;
import org.ldp4j.rdf.URIRef;

final class ResourceComparator implements Comparator<Resource<?>>, Serializable {

	private static final long serialVersionUID = 3688707084336037225L;

	@Override
	public int compare(Resource<?> o1, Resource<?> o2) {
		int result=0;
		if(o1 instanceof URIRef) {
			result=compareURI(o2, (URIRef)o1);
		} else {
			result=compareOther(o1, o2);
		}
		return result;
	}

	private int compareOther(Resource<?> o1, Resource<?> o2) {
		if(o2 instanceof BlankNode) {
			return o1.toString().compareTo(o2.toString());
		} else {
			return 1;
		}
	}

	private int compareURI(Resource<?> o2, URIRef uri) {
		if(o2 instanceof URIRef) {
			return ModelUtils.compare(uri,(URIRef)o2);
		} else {
			return -1;
		}
	}

}