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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-http-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;

@RunWith(JMockit.class)
public class MediaTypesTest {

	@Test
	public void isUtilityClass() throws Exception {
		assertThat(Utils.isUtilityClass(MediaTypes.class),equalTo(true));
	}

	@Test
	public void dependsOnImmutableMediaType() {
		final String mediaType = "text/turtle";
		new MockUp<ImmutableMediaType>() {
			@Mock
			public ImmutableMediaType fromString(Invocation context, String aValue, MediaRangeSyntax syntax) {
				assertThat(aValue,equalTo(mediaType));
				return context.proceed(aValue,syntax);
			}
		};
		MediaType result=MediaTypes.fromString(mediaType);
		assertThat(result.type(),equalTo("text"));
		assertThat(result.subType(),equalTo("turtle"));
	}

	@Test
	public void defaultPreferredSyntaxIsRFC7230() {
		assertThat(MediaTypes.preferredSyntax(),equalTo(MediaRangeSyntax.RFC7230));
	}

	@Test
	public void canCustomizePreferredSyntax() {
		assertThat(MediaTypes.preferredSyntax(),equalTo(MediaRangeSyntax.RFC7230));
		MediaTypes.preferredSyntax(MediaRangeSyntax.RFC6838);
		assertThat(MediaTypes.preferredSyntax(),equalTo(MediaRangeSyntax.RFC6838));
		MediaTypes.preferredSyntax(null);
		assertThat(MediaTypes.preferredSyntax(),equalTo(MediaRangeSyntax.RFC7230));
	}

	@Test
	public void regularWildcardTypeDoesNotIncludeNull() {
		MediaType one=anyMediaType();
		MediaType other=null;
		assertThat(MediaTypes.includes(one,other),equalTo(false));
	}

	@Test
	public void regularWildcardTypeIncludesRegularMediaTypes() {
		MediaType one=anyMediaType();
		MediaType other=textTurtle();
		checkIncludes(one, other, true);
	}

	@Test
	public void regularWildcardSubtypeIncludesMediaTypesFromSameTypeFamily() {
		MediaType one=anyTextMediaType();
		MediaType other=textTurtle();
		checkIncludes(one, other, true);
	}

	@Test
	public void regularWildcardSubtypeDoesNotIncludeMediaTypesFromOtherTypeFamily() {
		MediaType one=anyTextMediaType();
		MediaType other=applicationJson();
		checkIncludes(one, other, false);
	}

	@Test
	public void structuredWildcardSubtypeIncludesMediaTypesFromSameTypeFamilyAndSuffix() {
		MediaType one=anyApplicationMediaTypeAndXml();
		MediaType other=applicationRdfXml();
		checkIncludes(one, other, true);
	}

	@Test
	public void structuredWildcardSubtypeDoesNotIncludeMediaTypeWithSameTypeAndSubtypeMatchingTheSuffix() {
		MediaType one=anyApplicationMediaTypeAndXml();
		MediaType other=applicationXml();
		checkIncludes(one, other, false);
	}

	@Test
	public void structuredWildcardSubtypeIncludesSelf() {
		MediaType one=anyApplicationMediaTypeAndXml();
		MediaType other=anyApplicationMediaTypeAndXml();
		checkIncludes(one, other, true);
	}

	@Test
	public void regularMediaTypesDoNotIncludeNull() {
		MediaType one = textTurtle();
		MediaType other=null;
		assertThat(MediaTypes.includes(one,other),equalTo(false));
	}

	@Test
	public void regularMediaTypeIncludesSelf() {
		MediaType one=textTurtle();
		MediaType other=textTurtle();
		checkIncludes(one, other, true);
	}

	@Test
	public void regularMediaTypeDoesNotIncludeAlternativeMediaTypesFromTheTypeFamily() {
		MediaType one=textTurtle();
		MediaType other=textPlain();
		checkIncludes(one, other, false);
	}

	@Test
	public void regularMediaTypeDoNotIncludeAlternativeStructuredMedia() {
		MediaType one=applicationRdf();
		MediaType other=applicationRdfXml();
		checkIncludes(one, other, false);
	}

	@Test
	public void structuredMediaTypeIncludesSelf() {
		MediaType one=applicationRdfXml();
		MediaType other=applicationRdfXml();
		checkIncludes(one, other, true);
	}

	@Test
	public void regularWildcardTypeIsNotCompatibleWithNull() {
		MediaType one=anyMediaType();
		MediaType other=null;
		assertThat(MediaTypes.areCompatible(one,other),equalTo(false));
	}

	@Test
	public void regularWildcardTypeIsCompatibleWithRegularMediaTypes() {
		MediaType one=anyMediaType();
		MediaType other=textTurtle();
		checkCompatibility(one, other, true);
	}

	@Test
	public void regularWildcardSubtypeIsCompatibleWithMediaTypesFromSameTypeFamily() {
		MediaType one=anyTextMediaType();
		MediaType other=textTurtle();
		checkCompatibility(one, other, true);
	}

	@Test
	public void regularWildcardSubtypeIsNotCompatibleWithMediaTypesFromOtherTypeFamily() {
		MediaType one=anyTextMediaType();
		MediaType other=applicationJson();
		checkCompatibility(one, other, false);
	}

	@Test
	public void structuredWildcardSubtypeIsCompatibleWithMediaTypesFromSameTypeFamilyAndSuffix() {
		MediaType one=anyApplicationMediaTypeAndXml();
		MediaType other=applicationRdfXml();
		checkCompatibility(one, other, true);
	}

	@Test
	public void structuredWildcardSubtypeIsNotCompatibleWithMediaTypeWithSameTypeAndSubtypeMatchingTheSuffix() {
		MediaType one=anyApplicationMediaTypeAndXml();
		MediaType other=applicationXml();
		checkCompatibility(one, other, false);
	}

	@Test
	public void structuredWildcardSubtypeIsCompatibleWithSelf() {
		MediaType one=anyApplicationMediaTypeAndXml();
		MediaType other=anyApplicationMediaTypeAndXml();
		checkCompatibility(one, other, true);
	}

	@Test
	public void regularMediaTypesIsNotCompatibleWithNull() {
		MediaType one = textTurtle();
		MediaType other=null;
		assertThat(MediaTypes.areCompatible(one,other),equalTo(false));
	}

	@Test
	public void regularMediaTypeIsCompatibleWithSelf() {
		MediaType one=textTurtle();
		MediaType other=textTurtle();
		checkCompatibility(one, other, true);
	}

	@Test
	public void regularMediaTypeIsNotCompatibleWithAlternativeMediaTypesFromTheTypeFamily() {
		MediaType one=textTurtle();
		MediaType other=textPlain();
		checkCompatibility(one, other, false);
	}

	@Test
	public void regularMediaTypeIsNotCompatibleWithAlternativeStructuredMedia() {
		MediaType one=applicationRdf();
		MediaType other=applicationRdfXml();
		checkCompatibility(one, other, false);
	}

	@Test
	public void structuredMediaTypeIsCompatibleWithSelf() {
		MediaType one=applicationRdfXml();
		MediaType other=applicationRdfXml();
		checkCompatibility(one, other, true);
	}

	@Test
	public void isStructuredRequiresNonNull() {
		try {
			MediaTypes.isStructured(null);
			fail("Should not accept null");
		} catch (NullPointerException e) {
			assertThat(e.getMessage(),equalTo("Media type cannot be null"));
		}
	}

	@Test
	public void regularMediaTypeIsNotStructured() {
		assertThat(MediaTypes.isStructured(textTurtle()),equalTo(false));
	}

	@Test
	public void structuredMediaTypeIsNotStructured() {
		assertThat(MediaTypes.isStructured(applicationRdfXml()),equalTo(true));
	}

	@Test
	public void headerStringNormalizesMediaRange() {
		MediaType mt=MediaTypes.fromString("APPLICATION/RDF+XML");
		assertThat(MediaTypes.toHeader(mt),equalTo("application/rdf+xml"));
	}

	@Test
	public void headerStringNormalizesCharsetParameter() {
		MediaType mt=MediaTypes.fromString("application/rdf+xml;CHARSET=\"UTF-8\"");
		assertThat(MediaTypes.toHeader(mt),equalTo("application/rdf+xml;charset=utf-8"));
	}

	@Test
	public void headerStringNormalizesQualityParameter() {
		MediaType mt=MediaTypes.fromString("application/rdf+xml;Q=0.123");
		assertThat(MediaTypes.toHeader(mt),equalTo("application/rdf+xml;q=0.123"));
	}

	@Test
	public void headerStringNormalizesCustomParameterNames() {
		MediaType mt=MediaTypes.fromString("application/rdf+xml;PARAM=value;QPARAM=\"value\"");
		assertThat(MediaTypes.toHeader(mt),equalTo("application/rdf+xml;param=value;qparam=\"value\""));
	}

	@Test
	public void headerStringSortsParameters() {
		MediaType mt=MediaTypes.fromString("application/rdf+xml;PARAM=value;QPARAM=\"value\";CHARSET=\"UTF-8\";Q=0.001");
		assertThat(MediaTypes.toHeader(mt),equalTo("application/rdf+xml;charset=utf-8;param=value;qparam=\"value\";q=0.001"));
	}

	private MediaType textTurtle() {
		return MediaTypes.fromString("text/turtle");
	}

	private MediaType textPlain() {
		return MediaTypes.fromString("text/plain");
	}

	private MediaType applicationJson() {
		return MediaTypes.fromString("application/json");
	}

	private MediaType applicationRdfXml() {
		return MediaTypes.fromString("application/rdf+xml");
	}

	private MediaType applicationRdf() {
		return MediaTypes.fromString("application/rdf");
	}

	private MediaType applicationXml() {
		return MediaTypes.fromString("application/xml");
	}

	private MediaType anyMediaType() {
		return MediaTypes.fromString("*/*");
	}

	@SuppressWarnings("unused")
	private MediaType anyMediaTypeAndXml() {
		return MediaTypes.fromString("*/*+xml");
	}

	private MediaType anyTextMediaType() {
		return MediaTypes.fromString("text/*");
	}

	private MediaType anyApplicationMediaTypeAndXml() {
		return MediaTypes.fromString("application/*+xml");
	}

	private void checkIncludes(MediaType one, MediaType other, boolean expected) {
		System.out.printf("includes(%s, %s)=%s%n",MediaTypes.toHeader(one),MediaTypes.toHeader(other),expected);
		assertThat(MediaTypes.includes(one,other),equalTo(expected));
	}

	private void checkCompatibility(MediaType one, MediaType other, boolean expected) {
		System.out.printf("compatible(%s, %s)=%s%n",MediaTypes.toHeader(one),MediaTypes.toHeader(other),expected);
		assertThat(MediaTypes.areCompatible(one,other),equalTo(expected));
		System.out.printf("compatible(%s, %s)=%s%n",MediaTypes.toHeader(other),MediaTypes.toHeader(one),expected);
		assertThat(MediaTypes.areCompatible(other,one),equalTo(expected));
	}

}
