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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-api:0.2.2
 *   Bundle      : rmf-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import org.junit.Test;
import org.ldp4j.rdf.Datatype;

public class DatatypeTest {

	public void checkForValue(Object value) {
		System.out.println(String.format("%s (%s) --> %s",value.getClass().isArray()?Arrays.asList((Object[])value):value,value.getClass(),Datatype.forValue(value)));
	}
	
	@Test
	public void testForValue() throws Exception {
		checkForValue(-1);
		checkForValue(1);
		checkForValue(2l);
		checkForValue(-2l);
		checkForValue(123123123123111232l);
		checkForValue(3.0d);
		checkForValue(4.0f);
		checkForValue((short)5);
		checkForValue((short)2000);
		checkForValue((short)-5);
		checkForValue((byte)6);
		checkForValue(true);
		checkForValue(URI.create("http://www.example.org"));
		checkForValue("test");
		checkForValue(new String[]{"test","test2"});
		checkForValue(BigDecimal.ONE);
		checkForValue(BigInteger.ONE);
		checkForValue(BigInteger.ZERO);
		checkForValue(BigInteger.valueOf(-1));
		checkForValue(new QName("http://www.example.org#","name"));
		checkForValue(DatatypeFactory.newInstance().newDuration(23));
		GregorianCalendar gc=new GregorianCalendar();
		gc.setTime(new Date());
		checkForValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
	}

	@Test
	public void testAncestors() {
		for(Datatype type:Datatype.values()) {
			System.out.println("ancestors("+type+") : "+type.ancestors());
		}
	}
	
	@Test
	public void testRawClass() {
		for(Datatype type:Datatype.values()) {
			System.out.println("rawClass("+type+") : "+type.rawClass());
		}
	}

	@Test
	public void testToUri() {
		for(Datatype type:Datatype.values()) {
			System.out.println("toURI("+type+") : "+type.toURI());
		}
	}

}
