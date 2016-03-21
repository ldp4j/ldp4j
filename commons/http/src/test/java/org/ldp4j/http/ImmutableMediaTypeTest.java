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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class ImmutableMediaTypeTest {

	@Test
	public void canParseMediaTypesWithRegularMediaRange() throws Exception {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("text/turtle");
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("text"));
		assertThat(actual.subType(),equalTo("turtle"));
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().isEmpty(),equalTo(true));
	}

	@Test
	public void canParseMediaTypesWithWildcardSubtype() throws Exception {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("text/*");
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("text"));
		assertThat(actual.subType(),equalTo("*"));
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().isEmpty(),equalTo(true));
	}

	@Test
	public void canParseMediaTypesWithWildcardMediaRange() throws Exception {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("*/*");
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("*"));
		assertThat(actual.subType(),equalTo("*"));
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().isEmpty(),equalTo(true));
	}

	@Test
	public void canParseMediaTypesWithLegacyWildcardMediaRange() throws Exception {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("*");
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("*"));
		assertThat(actual.subType(),equalTo("*"));
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
	public void canParseMediaTypesWithQualityEqualToOneAndDecimalPointButNoDecimals() throws Exception {
		final ImmutableMediaType actual=createWithQualityValue("1.");
		assertThat(actual.weight(),equalTo(1.0D));
	}

	@Test
	public void canParseMediaTypesWithQualityEqualToZeroAndDecimalPointButNoDecimals() throws Exception {
		final ImmutableMediaType actual=createWithQualityValue("0.");
		assertThat(actual.weight(),equalTo(0.0D));
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
	public void canParseMediaTypesWithCompatibleQuotationCharsetDefinitions() throws Exception {
		final MediaType actual= createParam("charset=utf-8;charset=\"UTF-8\"");
		assertThat(actual.charset(),equalTo(StandardCharsets.UTF_8));
		assertThat(actual.parameters().get("charset"),equalTo("\"UTF-8\""));
	}


	@Test
	public void sameInstanceIsEqual() {
		final ImmutableMediaType one=defaultMediaType().build();
		assertThat(one,equalTo(one));
	}

	@Test
	public void mediaTypesWithSameConfigurationAreEqual() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().build();
		assertThat(one,equalTo(other));
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
	public void mediaTypesWithDifferentTypeAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeType().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithDifferentSubtypeAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeSubtype().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithDifferentCharsetsAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeCharset().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithCompatibleCharsetsAreEqual() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withCompatibleCharset().build();
		assertThat(one.toString(),not(equalTo(other.toString())));
		assertThat(one,equalTo(other));
	}

	@Test
	public void mediaTypesWithCompatibleQualityEqual() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withCompatibleQuality().build();
		assertThat(one.toString(),not(equalTo(other.toString())));
		assertThat(one,equalTo(other));
	}

	@Test
	public void mediaTypesWithDifferentQualityAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeQuality().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithDifferentParameterNumberAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAdditionalParameter().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void mediaTypesWithDifferentValuesForTheSameParameterAreDifferent() {
		final ImmutableMediaType one=defaultMediaType().build();
		final ImmutableMediaType other=defaultMediaType().withAlternativeParameter().build();
		assertThat(one,not(equalTo(other)));
	}

	@Test
	public void cannotParseNullMediaTypes() throws Exception {
		try {
			ImmutableMediaType.fromString(null);
			fail("Should fail for null media type");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("Media type cannot be null"));
			assertThat(e.getMediaType(),equalTo(null));
		}
	}
	@Test
	public void cannotParseEmptyMediaTypes() throws Exception {
		try {
			ImmutableMediaType.fromString("");
			fail("Should fail for null media type");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("media type cannot be empty"));
			assertThat(e.getMediaType(),equalTo(""));
		}
	}

	@Test
	public void cannotParseMediaTypesWithoutMediaRange() throws Exception {
		final String offending = " ;parameter=value";
		try {
			ImmutableMediaType.fromString(offending);
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMediaType(),equalTo(offending));
			assertThat(e.getMessage(),containsString("no media range specified"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithVariableType() throws Exception {
		try {
			ImmutableMediaType.fromString("*/turtle");
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("wildcard type is legal only in wildcard media range ('*/*')"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithEmptySubtype() throws Exception {
		try {
			ImmutableMediaType.fromString("text/ ");
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("media range subtype cannot be empty"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithoutType() throws Exception {
		try {
			ImmutableMediaType.fromString("/turtle");
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("media range type cannot be empty"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithNeitherTypeNorSubtype() throws Exception {
		try {
			ImmutableMediaType.fromString("/");
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("expected 2 types in media range (0)"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithMoreTypesThanExpected() throws Exception {
		try {
			ImmutableMediaType.fromString("text/turtle/something");
			fail("Should fail for invalid media range");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("expected 2 types in media range (3)"));
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
	public void cannotParseMediaTypesQualityGreaterThanOne() throws Exception {
		try {
			createWithQualityValue("23.000");
			fail("Should fail for invalid quality value");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("Invalid quality value '23.000'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithQualityWithoutDecimals() throws Exception {
		try {
			createWithQualityValue("23");
			fail("Should fail for invalid quality value");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("Invalid quality value '23'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithQualityEqualToOneButWithTooManyDecimals() throws Exception {
		try {
			createWithQualityValue("1.0000");
			fail("Should fail for invalid quality value");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("Invalid quality value '1.0000'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithQualityLowerThanOneButWithTooManyDecimals() throws Exception {
		try {
			createWithQualityValue("0.0123");
			fail("Should fail for invalid quality value");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("Invalid quality value '0.0123'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithEmptyParameters() throws Exception {
		try {
			createParam(" \t \t");
			fail("Should fail for empty parameters");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("invalid parameter ' \t \t'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithDanglingQuotationMark() throws Exception {
		try {
			createParam("myparam=\"");
			fail("Should fail for dangling quotation mark");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("invalid token character '\"'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithMissingEndingQuotationMark() throws Exception {
		try {
			createParam("myparam=\"value");
			fail("Should fail for dangling initial quotation mark");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("invalid token character '\"'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithMissingInitialQuotationMark() throws Exception {
		try {
			createParam("myparam=value\"");
			fail("Should fail for dangling final quotation mark");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("invalid token character '\"'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithInvalidQuotedCharacter() throws Exception {
		try {
			createParam("myparam=\"a"+offending()+"a\"");
			fail("Should fail for dangling quoted pair");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("Invalid character '"+offending()+"' in quoted string"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithDanglingQuotedPair() throws Exception {
		try {
			createParam("myparam=\"a"+Character.toString('\\')+offending()+"a\"");
			fail("Should fail for dangling quoted pair");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("Invalid quoted-pair character '"+offending()+"'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithDanglingInitialQuotedPair() throws Exception {
		try {
			createParam("myparam=\""+Character.toString('\\')+offending()+"a\"");
			fail("Should fail for dangling initial quoted pair");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("Invalid quoted-pair character '"+offending()+"'"));
		}
	}

	@Test
	public void cannotParseMediaTypesWithDanglingFinalQuotedPair() throws Exception {
		try {
			createParam("myparam=\"a\\\"");
			fail("Should fail for dangling final quoted pair");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("Missing quoted-pair character in quoted string"));
		}
	}

	@Test
	public void cannotParseMediaTypesMultipleDifferentDefinitionsForTheSameParameter() throws Exception {
		try {
			createParam("myparam=1;myparam=2");
			fail("Should fail for multiple different definitions for the same parameter");
		} catch (final InvalidMediaTypeException e) {
			assertThat(e.getMessage(),containsString("duplicated parameter 'myparam': found '2' after '1'"));
		}
	}

	private String offending() {
		return Character.toString((char)0x7f);
	}

	private MediaType createParam(final String paramDef) {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("text/turtle;"+paramDef);
		assertThat(actual,not(nullValue()));
		return actual;
	}

	private MediaType verifyParam(final String param, final String value) {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("text/turtle;"+param+"="+value+";q=0.123");
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("text"));
		assertThat(actual.subType(),equalTo("turtle"));
		assertThat(actual.charset(),nullValue());
		assertThat(actual.weight(),equalTo(0.123D));
		assertThat(actual.parameters().size(),equalTo(2));
		return actual;
	}

	private ImmutableMediaType createWithQualityValue(final String weight) {
		final ImmutableMediaType actual = ImmutableMediaType.fromString("text/turtle;q="+weight);
		assertThat(actual,not(nullValue()));
		assertThat(actual.type(),equalTo("text"));
		assertThat(actual.subType(),equalTo("turtle"));
		assertThat(actual.charset(),nullValue());
		assertThat(actual.parameters().size(),equalTo(1));
		return actual;
	}


	private Builder defaultMediaType() {
		return new Builder();
	}

	private static class Builder {

		private String mediaRange;
		private String charsetName;
		private String weight;
		private String parameter;
		private String random;

		Builder() {
			this.mediaRange="text/turtle";
			this.charsetName=StandardCharsets.UTF_8.name();
			this.weight="0.120";
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

		Builder withAlternativeQuality() {
			this.weight="1.000";
			return this;
		}

		Builder withCompatibleQuality() {
			this.weight="0.12";
			return this;
		}

		Builder withCompatibleCharset() {
			this.charsetName="\""+StandardCharsets.UTF_8.name()+"\"";
			return this;
		}

		ImmutableMediaType build() {
			final ImmutableMediaType actual=ImmutableMediaType.fromString(this.mediaRange+";charset="+this.charsetName+";q="+this.weight+";"+this.parameter+this.random);
			assertThat(actual,not(nullValue()));
			return actual;
		}

	}

}
