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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

@RunWith(JMockit.class)
public class ImmutableMediaTypeTest {

	private static final String OUTPUT = "output";
	private static final String INPUT  = "input";

	@Test
	public void usesSyntaxForValidatingType(@Mocked final MediaRangeSyntax syntax) {
		new Expectations() {{
			syntax.checkType(INPUT);result=OUTPUT;
			syntax.checkSubType(null);
			syntax.checkSuffix(null);
		}};
		ImmutableMediaType sut=new ImmutableMediaType(syntax,INPUT,null,null,null);
		assertThat(sut.type(),equalTo(OUTPUT));
		assertThat(sut.subType(),nullValue());
		assertThat(sut.suffix(),nullValue());
	}

	@Test
	public void usesSyntaxForValidatingSubtype(@Mocked final MediaRangeSyntax syntax) {
		new Expectations() {{
			syntax.checkType(null);
			syntax.checkSubType(INPUT);result=OUTPUT;
			syntax.checkSuffix(null);
		}};
		ImmutableMediaType sut=new ImmutableMediaType(syntax,null,INPUT,null,null);
		assertThat(sut.type(),nullValue());
		assertThat(sut.subType(),equalTo(OUTPUT));
		assertThat(sut.suffix(),nullValue());
	}

	@Test
	public void usesSyntaxForValidatingSuffix(@Mocked final MediaRangeSyntax syntax) {
		new Expectations() {{
			syntax.checkType(null);
			syntax.checkSubType(null);
			syntax.checkSuffix(INPUT);result=OUTPUT;
		}};
		ImmutableMediaType sut=new ImmutableMediaType(syntax,null,null,INPUT,null);
		assertThat(sut.type(),nullValue());
		assertThat(sut.subType(),nullValue());
		assertThat(sut.suffix(),equalTo(OUTPUT));
	}

	@Test
	public void constructorAcceptsNullParameters() {
		ImmutableMediaType sut=new ImmutableMediaType(MediaRangeSyntax.RFC7230,"type","subtype",null,null);
		assertThat(sut.parameters(),notNullValue());
		assertThat(sut.parameters().isEmpty(),equalTo(true));
	}

	@Test
	public void mediaRangeIsNormalized() {
		ImmutableMediaType sut=new ImmutableMediaType(MediaRangeSyntax.RFC7230,"TYPE","SUBTYPE","SUFFIX",null);
		assertThat(sut.type(),equalTo("type"));
		assertThat(sut.subType(),equalTo("subtype"));
		assertThat(sut.suffix(),equalTo("suffix"));
	}

	@Test
	public void canParseMediaTypesWithRegularMediaRange() throws Exception {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("text/turtle", MediaTypes.preferredSyntax());
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("text"));
		assertThat(actual.subType(),equalTo("turtle"));
		assertThat(actual.suffix(),nullValue());
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().isEmpty(),equalTo(true));
	}

	@Test
	public void canParseMediaTypesWithSimpleWildcardSubtype() throws Exception {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("text/*", MediaTypes.preferredSyntax());
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("text"));
		assertThat(actual.subType(),equalTo("*"));
		assertThat(actual.suffix(),nullValue());
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().isEmpty(),equalTo(true));
	}

	@Test
	public void canParseMediaTypesWithCompositeWildcardSubtype() throws Exception {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("text/*+xml", MediaTypes.preferredSyntax());
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("text"));
		assertThat(actual.subType(),equalTo("*"));
		assertThat(actual.suffix(),equalTo("xml"));
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().isEmpty(),equalTo(true));
	}

	@Test
	public void canParseMediaTypesWithWildcardMediaRange() throws Exception {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("*/*", MediaTypes.preferredSyntax());
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("*"));
		assertThat(actual.subType(),equalTo("*"));
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().isEmpty(),equalTo(true));
	}

	@Test
	public void canParseMediaTypesWithLegacyWildcardMediaRange() throws Exception {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("*", MediaTypes.preferredSyntax());
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("*"));
		assertThat(actual.subType(),equalTo("*"));
		assertThat(actual.suffix(),nullValue());
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().isEmpty(),equalTo(true));
	}

	@Test
	public void canParseMediaTypesWithUnquotedParameterValues() throws Exception {
		final MediaType actual=verifyParam("myparam", "unquoted-value");
		assertThat(actual.parameters().get("myparam"),equalTo("unquoted-value"));
	}

	@Test
	public void canParseMediaTypesWithQuotedParameterValues() throws Exception {
		final MediaType actual = verifyParam("myparam", "\"quoted-value\"");
		assertThat(actual.parameters().get("myparam"),equalTo("\"quoted-value\""));
	}

	@Test
	public void canParseMediaTypesWithQuotedParameterValuesHavingQuotedPairs() throws Exception {
		final MediaType actual = verifyParam("myparam", "\"quoted\\-value\"");
		assertThat(actual.parameters().get("myparam"),equalTo("\"quoted\\-value\""));
	}

	@Test
	public void canParseMediaTypesWithWeirdParameterNames() throws Exception {
		final MediaType actual = verifyParam("!#$%&'*+-.^_`|~", "\"quoted-value\"");
		assertThat(actual.parameters().get("!#$%&'*+-.^_`|~"),equalTo("\"quoted-value\""));
	}

	@Test
	public void canParseMediaTypesWithWeirdUnquotedParameterValues() throws Exception {
		final MediaType actual=verifyParam("myparam", "!#$%&'*+-.^_`|~");
		assertThat(actual.parameters().get("myparam"),equalTo("!#$%&'*+-.^_`|~"));
	}

	@Test
	public void canParseMediaTypesWithWeirdQuotedParameterValues() throws Exception {
		final MediaType actual=verifyParam("myparam", "\"!#$%&'*+-.^_`|~\"");
		assertThat(actual.parameters().get("myparam"),equalTo("\"!#$%&'*+-.^_`|~\""));
	}

	@Test
	public void canParseMediaTypesWithPreParameterWhitespace() throws Exception {
		final MediaType actual = createParam(" \tparam=value");
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().get("param"),equalTo("value"));
	}

	@Test
	public void canParseMediaTypesWithValidUnquotedCharsetName() throws Exception {
		final MediaType actual = createParam("charset=UTF-8");
		assertThat(actual.charset(),equalTo(StandardCharsets.UTF_8));
		assertThat(actual.parameters().get("charset"),equalTo("UTF-8"));
	}

	@Test
	public void canParseMediaTypesWithValidQuotedCharsetName() throws Exception {
		final MediaType actual = createParam("charset=\"UTF-8\"");
		assertThat(actual.charset(),equalTo(StandardCharsets.UTF_8));
		assertThat(actual.parameters().get("charset"),equalTo("\"UTF-8\""));
	}

	@Test
	public void canParseMediaTypesWithMultipleEqualParameterDefinitions() throws Exception {
		final MediaType actual= createParam("myparam=value;myparam=value");
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().get("myparam"),equalTo("value"));
	}

	@Test
	public void canParseMediaTypesWithCompatibleCaseCharsetDefinitions() throws Exception {
		final MediaType actual= createParam("charset=utf-8;charset=UTF-8");
		assertThat(actual.charset(),equalTo(StandardCharsets.UTF_8));
		assertThat(actual.parameters().get("charset"),equalTo("UTF-8"));
	}

	@Test
	public void canParseMediaTypesWithCompatibleQuotedCharsetDefinitions() throws Exception {
		final MediaType actual= createParam("charset=utf-8;charset=\"UTF-8\"");
		assertThat(actual.charset(),equalTo(StandardCharsets.UTF_8));
		assertThat(actual.parameters().get("charset"),equalTo("\"UTF-8\""));
	}

	@Test
	public void canParseStructuredMediaTypesWithSubtypeIncludingThePlusSymbol() throws Exception {
		ImmutableMediaType sut = ImmutableMediaType.fromString("text/turtle+one+other", MediaTypes.preferredSyntax());
		assertThat(sut.type(),equalTo("text"));
		assertThat(sut.subType(),equalTo("turtle+one"));
		assertThat(sut.suffix(),equalTo("other"));
	}

	@Test
	public void cannotParseNullMediaTypes() throws Exception {
		try {
			ImmutableMediaType.fromString(null, MediaTypes.preferredSyntax());
			fail("Should fail for null media type");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Media type cannot be null"));
			assertThat(e.getMediaType(),equalTo(null));
		}
	}

	@Test
	public void cannotParseEmptyMediaTypes() throws Exception {
		try {
			ImmutableMediaType.fromString("", MediaTypes.preferredSyntax());
			fail("Should fail for null media type");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Media type cannot be empty"));
			assertThat(e.getMediaType(),equalTo(""));
		}
	}

	@Test
	public void cannotParseMediaTypesPartialMediaRange() throws Exception {
		final String offending="partial;parameter=value";
		try {
			ImmutableMediaType.fromString(offending, MediaTypes.preferredSyntax());
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMediaType(),equalTo(offending));
			assertThat(Throwables.getRootCause(e).getMessage(),equalTo("No media range subtype specified"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithoutMediaRange() throws Exception {
		final String offending = " ;parameter=value";
		try {
			ImmutableMediaType.fromString(offending, MediaTypes.preferredSyntax());
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMediaType(),equalTo(offending));
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("No media range specified"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithVariableType() throws Exception {
		try {
			ImmutableMediaType.fromString("*/turtle", MediaTypes.preferredSyntax());
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Wildcard type is legal only in wildcard media range ('*/*')"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithEmptySubtype() throws Exception {
		try {
			ImmutableMediaType.fromString("text/ ", MediaTypes.preferredSyntax());
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("No media range subtype specified"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithoutType() throws Exception {
		try {
			ImmutableMediaType.fromString("/turtle", MediaTypes.preferredSyntax());
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("No media range type specified"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithNeitherTypeNorSubtype() throws Exception {
		try {
			ImmutableMediaType.fromString("/", MediaTypes.preferredSyntax());
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("No media range type specified"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithMoreTypesThanExpected() throws Exception {
		try {
			ImmutableMediaType.fromString("text/turtle/something", MediaTypes.preferredSyntax());
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Expected 2 types in media range but got 3"));
		}
	}

	@Test
	public void cannotParseMediaTypeWithDanglingStructureSeparator() throws Exception {
		try {
			ImmutableMediaType.fromString("text/turtle+", MediaTypes.preferredSyntax());
			fail("Should fail for invalid structured media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("missing suffix for structured media type (turtle)"));
		}
	}

	@Test
	public void cannotParseStructuredMediaTypeWithMissingSubtype() throws Exception {
		try {
			ImmutableMediaType.fromString("text/+structure", MediaTypes.preferredSyntax());
			fail("Should fail for invalid structured media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("missing subtype for structured media type (structure)"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithUnsupportedCharsets() throws Exception {
		try {
			createParam("charset=catepora");
			fail("Should fail for unsupported charsets");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getCause(),instanceOf(IllegalArgumentException.class));
			assertThat(e.getCause().getCause(),instanceOf(UnsupportedCharsetException.class));
			assertThat(((UnsupportedCharsetException)e.getCause().getCause()).getCharsetName(),equalTo("catepora"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithBadCharsetName() throws Exception {
		try {
			createParam("charset=<catepora>");
			fail("Should fail for bad-name charsets");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getCause(),instanceOf(IllegalArgumentException.class));
			assertThat(((IllegalCharsetNameException)e.getCause().getCause()).getCharsetName(),equalTo("<catepora>"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithEmptyParameters() throws Exception {
		try {
			createParam(" \t \t");
			fail("Should fail for empty parameters");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Invalid parameter ' \t \t'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithEmptyParameterValue() throws Exception {
		try {
			createParam("myparam= ");
			fail("Should fail for empty parameter value");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Value for parameter 'myparam' cannot be empty"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithDanglingQuotationMark() throws Exception {
		try {
			createParam("myparam=\"");
			fail("Should fail for dangling quotation mark");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Invalid character '\"' in token '\"' at 0"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithMissingEndingQuotationMark() throws Exception {
		try {
			createParam("myparam=\"value");
			fail("Should fail for dangling initial quotation mark");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Invalid character '\"' in token '\"value' at 0"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithMissingInitialQuotationMark() throws Exception {
		try {
			createParam("myparam=value\"");
			fail("Should fail for dangling final quotation mark");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Invalid character '\"' in token 'value\"' at 5"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithInvalidQuotedParameterValue() throws Exception {
		try {
			createParam("myparam=\"a"+offending()+"a\"");
			fail("Should fail for invalid quoted character");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Invalid character '"+offending()+"' in quoted string"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithInvalidUnquotedParameterValue() throws Exception {
		try {
			createParam("myparam=a"+offending()+"a");
			fail("Should fail for invalid unquoted parameter value");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Invalid character '"+offending()+"' in token"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithDanglingQuotedPair() throws Exception {
		try {
			createParam("myparam=\"a"+Character.toString('\\')+offending()+"a\"");
			fail("Should fail for dangling quoted pair");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Invalid quoted-pair character '"+offending()+"'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithDanglingInitialQuotedPair() throws Exception {
		try {
			createParam("myparam=\""+Character.toString('\\')+offending()+"a\"");
			fail("Should fail for dangling initial quoted pair");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Invalid quoted-pair character '"+offending()+"'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithDanglingFinalQuotedPair() throws Exception {
		try {
			createParam("myparam=\"a\\\"");
			fail("Should fail for dangling final quoted pair");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Missing quoted-pair character in quoted string"));
		}
	}

	@Test
	public void cannotParseMediaTypesMultipleDifferentDefinitionsForTheSameParameter() throws Exception {
		try {
			createParam("myparam=1;myparam=2");
			fail("Should fail for multiple different definitions for the same parameter");
		} catch (final InvalidMediaTypeException e) {
			assertThat(Throwables.getRootCause(e).getMessage(),containsString("Duplicated parameter 'myparam': found '2' after '1'"));
		}
	}

	@Test
	public void sameInstanceIsEqual() {
		final ImmutableMediaType one=defaultMediaType().build();
		assertThat(one,equalTo(one));
	}

	@Test
	public void parameterlessMediaTypesAreEqualIfHaveSameMediaRange() {
		final ImmutableMediaType one=new ImmutableMediaType(MediaRangeSyntax.RFC7230, "application", "rdf", "xml", null);
		final ImmutableMediaType other=new ImmutableMediaType(MediaRangeSyntax.RFC7230, "application", "rdf", "xml", null);
		assertThat(one,equalTo(other));
	}

	@Test
	public void equalParameterlessMediaTypesHaveSameHashCode() {
		final ImmutableMediaType one=new ImmutableMediaType(MediaRangeSyntax.RFC7230, "application", "rdf", "xml", null);
		final ImmutableMediaType other=new ImmutableMediaType(MediaRangeSyntax.RFC7230, "application", "rdf", "xml", null);
		assertThat(one.hashCode(),equalTo(other.hashCode()));
	}

	@Test
	public void mediaTypesWithSameConfigurationAreEqual() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().build();
		assertThat(one,equalTo(other));
	}

	@Test
	public void mediaTypesWithSameConfigurationHaveSameHashCode() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().build();
		assertThat(one.hashCode(),equalTo(other.hashCode()));
	}

	@Test
	public void mediaTypesAreOnlyEqualToMediaTypes() {
		final MediaType one=defaultMediaType().build();
		assertThat((Object)one,not(equalTo((Object)"data")));
	}

	@Test
	public void mediaTypesWithDifferentMediaRangeAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeMediaRange().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithDifferentMediaRangeHaveDifferentHashCode() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeMediaRange().build();
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void mediaTypesWithDifferentTypeAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeType().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithDifferentTypeAreHaveDifferentHashCode() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeType().build();
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void mediaTypesWithDifferentSubtypeAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeSubtype().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithDifferentSubtypeHaveDifferentHashCode() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeSubtype().build();
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void mediaTypesWithDifferentSuffixAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().withSuffix().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeSuffix().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithDifferentSuffixHaveDifferentHashCode() {
		final ImmutableMediaType one=defaultMediaType().withSuffix().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeSuffix().build();
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void mediaTypesWithDifferentCharsetsAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeCharset().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithDifferentCharsetsHaveDifferentHashCode() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeCharset().build();
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void mediaTypesWithCompatibleCharsetsAreEqual() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withCompatibleCharset().build();
		assertThat(one.toString(),not(equalTo(other.toString())));
		assertThat(one,equalTo(other));
	}

	@Test
	public void mediaTypesWithCompatibleCharsetsHaveSameHashCode() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withCompatibleCharset().build();
		assertThat(one.toString(),not(equalTo(other.toString())));
		assertThat(one.hashCode(),equalTo(other.hashCode()));
	}

	@Test
	public void mediaTypesWithDifferentParameterNumberAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAdditionalParameter().build();
		assertThat(one,not(equalTo(other)));
	}

	/**
	 * WARNING: Collisions may happen because of rounding and overflow when
	 * calculating the hash code
	 */
	@Test
	public void mediaTypesWithDifferentParameterNumberHaveDifferentHashCode() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAdditionalParameter().build();
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void mediaTypesWithDifferentValuesForTheSameParameterAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeParameter().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithDifferentValuesForTheSameParameterHaveDifferentHashCode() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeParameter().build();
		assertThat(one.hashCode(),not(equalTo(other.hashCode())));
	}

	@Test
	public void typeWilcardIsWildcard() {
		ImmutableMediaType sut = new ImmutableMediaType(MediaRangeSyntax.RFC6838,"*","*",null,null);
		assertThat(sut.isWildcard(),equalTo(true));
	}

	@Test
	public void unstructuredSubtypeWilcardIsWildcard() {
		ImmutableMediaType sut = new ImmutableMediaType(MediaRangeSyntax.RFC6838,"application","*",null,null);
		assertThat(sut.isWildcard(),equalTo(true));
	}

	@Test
	public void structuredSubtypeWilcardIsWildcard() {
		ImmutableMediaType sut = new ImmutableMediaType(MediaRangeSyntax.RFC6838,"application","*","xml",null);
		assertThat(sut.isWildcard(),equalTo(true));
	}

	@Test
	public void regularMediaTypesAreNotWildcard() {
		ImmutableMediaType sut = new ImmutableMediaType(MediaRangeSyntax.RFC6838,"application","rdf","xml",null);
		assertThat(sut.isWildcard(),equalTo(false));
	}

	@Test
	public void parameterlessRegularMediaTypeHeaderRepresentationIsValid() throws Exception {
		ImmutableMediaType sut = new ImmutableMediaType(MediaRangeSyntax.RFC6838, "type", "subtype", null, null);
		assertThat(sut.toHeader(),equalTo("type/subtype"));
	}

	@Test
	public void parameterlessStructuredMediaTypeHeaderRepresentationIsValid() throws Exception {
		ImmutableMediaType sut = new ImmutableMediaType(MediaRangeSyntax.RFC6838, "type", "subtype", "suffix", null);
		assertThat(sut.toHeader(),equalTo("type/subtype+suffix"));
	}

	@Test
	public void mediaTypeWithCharsetHeaderRepresentationIsValid() throws Exception {
		ImmutableMediaType sut = new ImmutableMediaType(MediaRangeSyntax.RFC6838, "type", "subtype", null, ImmutableMap.<String,String>builder().put("charset","\"UTF-8\"").build());
		assertThat(sut.toHeader(),equalTo("type/subtype;charset=utf-8"));
	}

	@Test
	public void mediaTypeWithCharsetHeaderAndOtherParametersHeaderRepresentationIsValid() throws Exception {
		ImmutableMediaType sut = new ImmutableMediaType(MediaRangeSyntax.RFC6838, "type", "subtype", null, ImmutableMap.<String,String>builder().put("param","value").put("charset","\"UTF-8\"").build());
		assertThat(sut.toHeader(),equalTo("type/subtype;charset=utf-8;param=value"));
	}

	@Test
	public void doesNotCloneNull() throws Exception {
		ImmutableMediaType original = null;
		ImmutableMediaType copy = ImmutableMediaType.copyOf(original);
		assertThat(copy,sameInstance(original));
	}

	@Test
	public void copyDoesNotCloneImmutableInstances() throws Exception {
		ImmutableMediaType original = defaultMediaType().build();
		ImmutableMediaType copy = ImmutableMediaType.copyOf(original);
		assertThat(copy,sameInstance(original));
	}

	@Test
	public void copyClonesAllMediaTypeComponents(@Mocked final MediaType original) throws Exception {
		final ImmutableMediaType expected = defaultMediaType().build();
		new Expectations() {{
			original.toHeader();result=expected.toHeader();
		}};
		ImmutableMediaType copy = ImmutableMediaType.copyOf(original);
		assertThat(copy,not(sameInstance(original)));
		assertThat(copy,equalTo(expected));
	}

	@Test
	public void copySupportsBothSyntaxes(@Mocked final MediaType original) throws Exception {
		final ImmutableMediaType expected = ImmutableMediaType.fromString(".type/subtype",MediaRangeSyntax.RFC7230);
		new Expectations() {{
			original.toHeader();result=expected.toHeader();
		}};
		ImmutableMediaType copy = ImmutableMediaType.copyOf(original);
		assertThat(copy,not(sameInstance(original)));
		assertThat(copy,equalTo(expected));
	}

	@Test
	public void copyMayFailIfOriginalImplementationIsBroken(@Mocked final MediaType original) throws Exception {
		new Expectations() {{
			original.toHeader();result="<invalid media type>";
		}};
		try {
			ImmutableMediaType.copyOf(original);
			fail("Should not copy non-immutable media types whose toHeader method is broken");
		} catch (InvalidMediaTypeException e) {
			assertThat(e.getMessage(),equalTo("No media range subtype specified"));
		}
	}

	private String offending() {
		return Character.toString((char)0x7f);
	}

	private MediaType createParam(final String paramDef) {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("text/turtle;"+paramDef, MediaTypes.preferredSyntax());
		assertThat(actual,not(nullValue()));
		return actual;
	}

	private MediaType verifyParam(final String param, final String value) {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("text/turtle;"+param+"="+value+";q=0.123", MediaTypes.preferredSyntax());
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("text"));
		assertThat(actual.subType(),equalTo("turtle"));
		assertThat(actual.suffix(),nullValue());
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().size(),equalTo(2));
		return actual;
	}

	private Builder defaultMediaType() {
		return new Builder();
	}

	private static class Builder {

		private String mediaRange;
		private String charsetName;
		private String parameter;
		private String random;

		Builder() {
			this.mediaRange="text/turtle";
			this.charsetName=StandardCharsets.UTF_8.name();
			this.parameter="param=value";
			this.random="";
		}

		Builder withAlternativeType() {
			this.mediaRange="application/turtle";
			return this;
		}

		Builder withAlternativeSubtype() {
			this.mediaRange="text/html";
			return this;
		}

		Builder withSuffix() {
			this.mediaRange="text/turle+zip";
			return this;
		}

		Builder withAlternativeSuffix() {
			this.mediaRange="text/turle+ber";
			return this;
		}

		Builder withAlternativeMediaRange() {
			this.mediaRange="application/rdf+xml";
			return this;
		}

		Builder withAlternativeCharset() {
			this.charsetName=StandardCharsets.ISO_8859_1.name();
			return this;
		}

		Builder withAlternativeParameter() {
			this.parameter="param=anotherValue";
			return this;
		}

		Builder withAdditionalParameter() {
			this.random=";anotherParam=anotherValue";
			return this;
		}

		Builder withCompatibleCharset() {
			this.charsetName="\""+StandardCharsets.UTF_8.name()+"\"";
			return this;
		}

		ImmutableMediaType build() {
			final ImmutableMediaType actual=ImmutableMediaType.fromString(this.mediaRange+";charset="+this.charsetName+";"+this.parameter+this.random, MediaTypes.preferredSyntax());
			assertThat(actual,not(nullValue()));
			return actual;
		}

	}

}
