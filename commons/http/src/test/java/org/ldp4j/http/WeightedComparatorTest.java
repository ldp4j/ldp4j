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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import java.util.Comparator;

import org.junit.Test;

public class WeightedComparatorTest extends AbstractComparatorTest<Weighted<String>>{

	private static final Weighted<String> VALUE = Weighted.newInstance().withEntity("str1").withWeight(0.123D);
	private static final Weighted<String> SMALLER_ENTITY = Weighted.newInstance().withEntity("str0").withWeight(0.123D);
	private static final Weighted<String> SMALLER_WEIGHT = Weighted.newInstance().withEntity("str1").withWeight(0.001D);

	protected WeightedComparator<String> sut() {
		return
			WeightedComparator.
				create(
					new Comparator<String>(){
						@Override
						public int compare(String o1, String o2) {
							return o1.compareTo(o2);
						}
					}
				);
	}

	@Test
	public void weightedAreEqualIfQualityAndEntitiesAreEqual() {
		assertIsEqualTo(VALUE, VALUE);
	}

	@Test
	public void comparatorChecksEntityFirst() throws Exception {
		assertIsGreaterThan(VALUE,SMALLER_ENTITY);
		assertIsLowerThan(SMALLER_ENTITY,VALUE);
	}

	@Test
	public void comparatorChecksEntityLater() throws Exception {
		assertIsGreaterThan(VALUE,SMALLER_WEIGHT);
		assertIsLowerThan(SMALLER_WEIGHT,VALUE);
	}

}
