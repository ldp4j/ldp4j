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
 *   Artifact    : org.ldp4j.commons.rmf:integration-sesame:0.2.2
 *   Bundle      : integration-sesame-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.sesame;


import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.rdf.sesame.TurtleValueUtils;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.memory.model.MemValueFactory;

public class TurtleValueUtilsTest {

	private static final String BASE = "http://www.megatwork.org/smart-aggregator/entity/";
	private Map<String,String> namespaces;
	private ValueFactory vf;
	private TurtleValueUtils sut;

	@Before
	public void setUp() throws Exception {
		vf=new MemValueFactory();
		namespaces=new HashMap<String, String>();
		namespaces.put("sav", "http://www.megatwork.org/vocabulary#");
		namespaces.put("saw", BASE);
		sut = new TurtleValueUtils(vf.createURI(BASE),namespaces);
	}

	@Test
	public void testToString$child() throws Exception {
		System.out.println("Child: "+sut.toString(vf.createURI(BASE+"child")));
	}

	@Test
	public void testToString$ancestor() throws Exception {
		System.out.println("Ancestor: "+sut.toString(vf.createURI(BASE+"..")));
	}

	@Test
	public void testToString$self() throws Exception {
		System.out.println("Self: "+sut.toString(vf.createURI(BASE.concat("."))));
	}

	@Test
	public void testToString$sameAsBase() throws Exception {
		System.out.println("Same as base: "+sut.toString(vf.createURI(BASE)));
	}

	@Test
	public void testToString$baseAsResource() throws Exception {
		System.out.println("Base as resource: "+sut.toString(vf.createURI(BASE.substring(0,BASE.length()-1))));
	}

}
