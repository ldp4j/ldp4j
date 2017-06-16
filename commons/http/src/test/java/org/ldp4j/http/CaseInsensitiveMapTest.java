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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Locale;
import java.util.Map;

import mockit.Deencapsulation;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;


public class CaseInsensitiveMapTest {

	@Test
	public void usesDefaultLocaleByDefault() {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		assertThat(sut.locale(),equalTo(Locale.getDefault()));
	}

	@Test
	public void localeCanBeCustomized() {
		Locale custom = customLocale();
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>(custom);
		assertThat(sut.locale(),equalTo(custom));
	}

	@Test
	public void putIgnoresKeyCase() throws Exception {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		assertThat(sut.put("KEY","value"),nullValue());
		assertThat(sut.put("key","otherValue"),equalTo("value"));
	}

	@Test
	public void putAllIgnoresKeyCase() throws Exception {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		assertThat(sut.put("KEY","value"),nullValue());
		sut.putAll(
			ImmutableMap.
				<String,String>builder().
					put("key","otherValue").
					put("otherKey","anotherValue").
					build());
		assertThat(sut.get("key"),equalTo("otherValue"));
		assertThat(sut.get("OTHERKEY"),equalTo("anotherValue"));
	}

	@Test
	public void getIgnoresCaseOfStringKeys() throws Exception {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		assertThat(sut.put("KEY","value"),nullValue());
		assertThat(sut.get("key"),equalTo("value"));
	}

	@Test
	public void getRequiresStringKeys() throws Exception {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		assertThat(sut.get(new Object()),nullValue());
	}

	@Test
	public void removeIgnoresCaseOfStringKeys() throws Exception {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		assertThat(sut.put("KEY","value"),nullValue());
		assertThat(sut.remove("key"),equalTo("value"));
	}

	@Test
	public void removeRequiresStringKeys() throws Exception {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		assertThat(sut.remove(new Object()),nullValue());
	}

	@Test
	public void containsKeyIgnoresCaseOfStringKeys() throws Exception {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		assertThat(sut.put("KEY","value"),nullValue());
		assertThat(sut.containsKey("key"),equalTo(true));
	}

	@Test
	public void mapOnlyContainsStringKeys() throws Exception {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		assertThat(sut.containsKey(new Object()),equalTo(false));
	}

	@Test
	public void clearCleansCache() throws Exception {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		final Map<String,String> cache=Deencapsulation.getField(sut,"caseInsensitiveKeys");
		assertThat(sut.put("KEY","value"),nullValue());
		assertThat(cache.keySet(),hasSize(1));
		sut.clear();
		assertThat(cache.keySet(),hasSize(0));
	}

	@Test
	public void sameInstanceIsEqual() {
		final CaseInsensitiveMap<String> one=defaultMap();
		assertThat(one,equalTo(one));
	}

	@Test
	public void caseInsenstiveMapsAreOnlyEqualToCaseInsensitiveMaps() {
		final CaseInsensitiveMap<String> one=defaultMap();
		assertThat((Object)one,not(equalTo((Object)"data")));
	}

	@Test
	public void caseInsenstiveMapWithDifferentLocaleAreDifferent() {
		final CaseInsensitiveMap<String> one=new CaseInsensitiveMap<String>();
		final CaseInsensitiveMap<String> other=new CaseInsensitiveMap<String>(customLocale());
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void caseInsenstiveMapWithDifferentLocaleHaveDifferentHashCode() {
		final CaseInsensitiveMap<String> one=new CaseInsensitiveMap<String>();
		final CaseInsensitiveMap<String> other=new CaseInsensitiveMap<String>(customLocale());
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void caseInsenstiveMapWithSameEntriesAreEqual() {
		final CaseInsensitiveMap<String> one=defaultMap();
		final CaseInsensitiveMap<String> other=defaultMap();
		assertThat(one,equalTo(other));
	}

	@Test
	public void caseInsenstiveMapWithSameEntriesHaveSameHashCode() {
		final CaseInsensitiveMap<String> one=defaultMap();
		final CaseInsensitiveMap<String> other=defaultMap();
		assertThat(one.hashCode(),equalTo(other.hashCode()));
	}

	@Test
	public void caseInsenstiveMapWithEquivalentEntriesAreEqual() {
		final CaseInsensitiveMap<String> one=defaultMap();
		final CaseInsensitiveMap<String> other=equivalentMap();
		assertThat(one,equalTo(other));
	}

	@Test
	public void caseInsenstiveMapWithEquivalentEntriesHaveSameHashCode() {
		final CaseInsensitiveMap<String> one=defaultMap();
		final CaseInsensitiveMap<String> other=equivalentMap();
		assertThat(one.hashCode(),equalTo(other.hashCode()));
	}

	@Test
	public void caseInsenstiveMapWithNonEquivalentEntriesAreDifferent() {
		final CaseInsensitiveMap<String> one=defaultMap();
		final CaseInsensitiveMap<String> other=differentMap();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void caseInsenstiveMapWithNonEquivalentEntriesHaveDifferentHashCode() {
		final CaseInsensitiveMap<String> one=defaultMap();
		final CaseInsensitiveMap<String> other=differentMap();
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void caseInsenstiveMapWithDifferentSizeAreDifferent() {
		final CaseInsensitiveMap<String> one=defaultMap();
		final CaseInsensitiveMap<String> other=subMap();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void caseInsenstiveMapWithDifferentSizeHaveDifferentHashCode() {
		final CaseInsensitiveMap<String> one=defaultMap();
		final CaseInsensitiveMap<String> other=subMap();
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	private Locale customLocale() {
		Locale custom=null;
		for(final Locale available:Locale.getAvailableLocales()) {
			if(!available.equals(Locale.getDefault())) {
				custom=available;
			}
		}
		return custom;
	}

	private CaseInsensitiveMap<String> defaultMap() {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		sut.put("camelCaseKey","value1");
		sut.put("UPPERCASEKEY","value2");
		sut.put("lowercasekey","value3");
		return sut;
	}

	private CaseInsensitiveMap<String> equivalentMap() {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		sut.put("CAMELCASEKEY","value1");
		sut.put("uppercasekey","value2");
		sut.put("lowerCaseKey","value3");
		return sut;
	}

	private CaseInsensitiveMap<String> differentMap() {
		final CaseInsensitiveMap<String> sut=new CaseInsensitiveMap<String>();
		sut.put("camelCaseKey","value1");
		sut.put("UPPER_CASE_KEY","value2");
		sut.put("lowercasekey","value3");
		return sut;
	}

	private CaseInsensitiveMap<String> subMap() {
		CaseInsensitiveMap<String> sut = defaultMap();
		sut.remove("camelCaseKey");
		return sut;
	}

}
