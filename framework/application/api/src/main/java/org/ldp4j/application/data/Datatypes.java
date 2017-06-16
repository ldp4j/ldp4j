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
package org.ldp4j.application.data;

import java.net.URI;
import java.util.List;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;

final class Datatypes {

	static final URI STRING = URI.create("http://www.w3.org/2001/XMLSchema#string");
	static final URI DATE_TIME=toURI(DatatypeConstants.DATETIME);
	static final URI DATE=toURI(DatatypeConstants.DATE);
	static final URI TIME=toURI(DatatypeConstants.TIME);
	static final URI GYEAR=toURI(DatatypeConstants.GYEAR);
	static final URI GMONTH=toURI(DatatypeConstants.GMONTH);
	static final URI GDAY=toURI(DatatypeConstants.GDAY);
	static final URI GYEARMONTH=toURI(DatatypeConstants.GYEARMONTH);
	static final URI GMONTHDAY=toURI(DatatypeConstants.GMONTHDAY);
	static final URI DURATION=toURI(DatatypeConstants.DURATION);

	private static final ImmutableList<URI> TEMPORAL_DATATYPES=
		ImmutableList.
			<URI>builder().
				add(Datatypes.DATE_TIME).
				add(Datatypes.DATE).
				add(Datatypes.TIME).
				add(Datatypes.GYEAR).
				add(Datatypes.GMONTH).
				add(Datatypes.GDAY).
				add(Datatypes.GYEARMONTH).
				add(Datatypes.GMONTHDAY).
				build();

	private Datatypes() {
	}

	private static URI toURI(QName qName) {
		return URI.create(qName.getNamespaceURI()+"#"+qName.getLocalPart());
	}

	static boolean isDuration(URI datatype) {
		return Datatypes.DURATION.equals(datatype);
	}

	static boolean isTemporal(URI datatype) {
		return
			Datatypes.DATE_TIME.equals(datatype) || // NOSONAR
			Datatypes.DATE.equals(datatype) ||
			Datatypes.TIME.equals(datatype) ||
			Datatypes.GYEAR.equals(datatype) ||
			Datatypes.GMONTH.equals(datatype) ||
			Datatypes.GDAY.equals(datatype) ||
			Datatypes.GYEARMONTH.equals(datatype) ||
			Datatypes.GMONTHDAY.equals(datatype);
	}

	static List<URI> temporalDatatypes() {
		return Datatypes.TEMPORAL_DATATYPES;
	}

}
