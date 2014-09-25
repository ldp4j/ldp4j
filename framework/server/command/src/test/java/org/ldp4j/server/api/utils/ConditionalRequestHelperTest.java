/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.api.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ldp4j.server.api.utils.ConditionalRequestHelper.PreconditionEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ObjectArrays;

public class ConditionalRequestHelperTest {

	private static final String[] NON_GET_HEAD_METHODS = new String[]{"POST","PUT","PATCH","DELETE","OPTIONS"};
	private static final String[] GET_HEAD_METHODS = new String[]{"GET","HEAD"};
	private static final String[] ALL_METHODS = ObjectArrays.concat(GET_HEAD_METHODS, NON_GET_HEAD_METHODS, String.class);

	private final class ValidationIsNegativeWhenIfNoneMatchPreconditionFails implements Validator {
		@Override
		public void validate(PreconditionEvaluation result) {
			assertThatValidationIsNegative(
					result,
					Arrays.asList(GET_HEAD_METHODS).contains(method)?
							Status.NOT_MODIFIED:
							Status.PRECONDITION_FAILED,
					"DelegatedResourceSnapshot entity tag", "matches one of");
		}
	}

	private final class ValidationIsNegativeWhenIfModifiedSincePreconditionFails implements Validator {
		@Override
		public void validate(PreconditionEvaluation result) {
			assertThatValidationIsNegative(
				executePreconditionEvaluation(),
				Status.NOT_MODIFIED, 
				"DelegatedResourceSnapshot has not been modified since","before than required");
		}
	}

	private final class ValidationIsNegativeWhenIfUnmodifiedSincePreconditionFails implements Validator {
		@Override
		public void validate(PreconditionEvaluation result) {
			assertThatValidationIsNegative(
				executePreconditionEvaluation(),
				Status.PRECONDITION_FAILED, 
				"DelegatedResourceSnapshot has been modified in","after than required");
		}
	}

	private final class ValidationIsNegativeWhenIfMatchPreconditionFails implements Validator {
		@Override
		public void validate(PreconditionEvaluation result) {
			assertThatValidationIsNegative(
				result,
				Status.PRECONDITION_FAILED, 
				"DelegatedResourceSnapshot entity tag", "does not match any of");
		}
	}

	private final class ValidationIsPositive implements Validator {
		@Override
		public void validate(PreconditionEvaluation result) {
			assertThat(reason("A result should be returned"),result,is(notNullValue()));
			assertThat(reason("The evaluation should be positive"),result.isPositive(),is(equalTo(true)));
		}
	}

	private static final class FakeHttpHeaders implements HttpHeaders {

		private final Map<String, List<String>> values;

		private FakeHttpHeaders(Map<String, List<String>> values) {
			this.values = values;
		}
		
		@Override
		public String toString() {
			return values.toString();
		}

		@Override
		public List<Locale> getAcceptableLanguages() {
			throw new UnsupportedOperationException("Method not implemented yet");
		}

		@Override
		public List<MediaType> getAcceptableMediaTypes() {
			throw new UnsupportedOperationException("Method not implemented yet");
		}

		@Override
		public Map<String, Cookie> getCookies() {
			throw new UnsupportedOperationException("Method not implemented yet");
		}

		@Override
		public Locale getLanguage() {
			throw new UnsupportedOperationException("Method not implemented yet");
		}

		@Override
		public MediaType getMediaType() {
			throw new UnsupportedOperationException("Method not implemented yet");
		}

		@Override
		public List<String> getRequestHeader(String name) {
			return values.get(name);
		}

		@Override
		public MultivaluedMap<String, String> getRequestHeaders() {
			throw new UnsupportedOperationException("Method not implemented yet");
		}
		
		public static HttpHeadersBuilder newInstance() {
			return new HttpHeadersBuilderImpl();
		}
		
		public interface HttpHeadersBuilder {

			HttpHeaderValueBuilder forHeader(String header);

			HttpHeaders build();
			
		}
		public interface HttpHeaderValueBuilder {

			RecursiveHttpHeaderValueBuilder withValue(Object header);
			RecursiveHttpHeaderValueBuilder withDate(Date header);

		}
		
		public interface RecursiveHttpHeaderValueBuilder extends HttpHeadersBuilder, HttpHeaderValueBuilder {
			
		}
		
		private static final class HttpHeadersBuilderImpl implements HttpHeadersBuilder {
			
			private final Map<String,List<String>> values=new LinkedHashMap<String, List<String>>();
			
			@Override
			public HttpHeaderValueBuilder forHeader(String header) {
				List<String> list = values.get(header);
				if(list==null) {
					list=new ArrayList<String>();
					values.put(header, list);
				}
				return new HttpHeaderValueBuilderImpl(this,list);
			}
			
			@Override
			public HttpHeaders build() {
				return new FakeHttpHeaders(values);
			}
			
		}
		
		private static class HttpHeaderValueBuilderImpl implements HttpHeaderValueBuilder {

			private final HttpHeadersBuilderImpl builder;
			private final List<String> headerValues;

			private HttpHeaderValueBuilderImpl(HttpHeadersBuilderImpl builder, List<String> headerValues) {
				this.builder = builder;
				this.headerValues = headerValues;
			}
			
			@Override
			public RecursiveHttpHeaderValueBuilder withValue(Object value) {
				if(value==null) {
					throw new IllegalArgumentException("Object 'value' cannot be null");
				}
				this.headerValues.add(value.toString());
				return new ExtendedHttpHeaderValueBuilder(builder,headerValues);
			}

			@Override
			public RecursiveHttpHeaderValueBuilder withDate(Date value) {
				if(value==null) {
					throw new IllegalArgumentException("Object 'value' cannot be null");
				}
				this.headerValues.add(HttpDateUtils.format(value));
				return new ExtendedHttpHeaderValueBuilder(builder,headerValues);
			}
			
		}

		private static final class ExtendedHttpHeaderValueBuilder implements RecursiveHttpHeaderValueBuilder {

			private final HttpHeadersBuilder builder;
			private final List<String> headerValues;

			private ExtendedHttpHeaderValueBuilder(HttpHeadersBuilder builder, List<String> headerValues) {
				this.builder = builder;
				this.headerValues = headerValues;
			}
			
			@Override
			public HttpHeaderValueBuilder forHeader(String header) {
				return builder.forHeader(header);
			}
			
			@Override
			public HttpHeaders build() {
				return builder.build();
			}

			@Override
			public RecursiveHttpHeaderValueBuilder withValue(Object value) {
				if(value==null) {
					throw new IllegalArgumentException("Object 'value' cannot be null");
				}
				this.headerValues.add(value.toString());
				return this;
			}

			@Override
			public RecursiveHttpHeaderValueBuilder withDate(Date value) {
				if(value==null) {
					throw new IllegalArgumentException("Object 'value' cannot be null");
				}
				this.headerValues.add(HttpDateUtils.format(value));
				return this;
			}
			
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ConditionalRequestHelperTest.class);

	private static Date NOW;
	private static Date DAY_BEFORE;
	private static Date DAY_AFTER;
	private Date lastModified;
	private String method;
	private EntityTag entityTag;

	private FakeHttpHeaders.HttpHeadersBuilder builder;
	@BeforeClass
	public static void setUpBefore() {
		Calendar calendar=new GregorianCalendar();
		calendar.set(Calendar.MILLISECOND, 0);
		NOW=calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		DAY_BEFORE=calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, +2);
		DAY_AFTER=calendar.getTime();
	}
	
	@Before
	public void setUp() throws Exception {
		builder = FakeHttpHeaders.newInstance();
		configureMethod("POST");
		useEntityTag("value", true);
		useLastModified(NOW);
	}

	private void log(String template, Object... args) {
		if(LOGGER.isTraceEnabled()) {
			Throwable e=new RuntimeException();
			e.fillInStackTrace();
			String methodName = e.getStackTrace()[2].getMethodName();
			Object[] revisedArgs=new Object[args.length];
			for(int i=0;i<args.length;i++) {
				Object arg = args[i];
				revisedArgs[i]=
					arg instanceof Date?
						HttpDateUtils.format((Date)arg):
						arg.toString();
			}
			LOGGER.trace(">> "+methodName+"("+String.format(template,revisedArgs)+")");
		}
	}

	private EntityTag createEntityTag(String value, boolean weak) {
		return new EntityTag(value,weak);
	}

	private EntityTag entityTag() {
		return this.entityTag;
	}

	private Date lastModified() {
		return this.lastModified;
	}

		
	private ConditionalRequestHelperTest useEntityTag(String value, boolean weak) {
		this.entityTag = createEntityTag(value, weak);
		return this;
	}

	private ConditionalRequestHelperTest useLastModified(Date date) {
		this.lastModified = date;
		return this;
	}

	private ConditionalRequestHelperTest configureMethod(String method) {
		this.method = method;
		return this;
	}
	
	private ConditionalRequestHelperTest configureHttpHeader(String header, Object value) {
		if(value instanceof Date) {
			builder.forHeader(header).withDate((Date)value);
		} else {
			builder.forHeader(header).withValue(value);
		}
		return this;
	}
	
	private String evaluationContext() {
		return String.format("DelegatedResourceSnapshot {etag: %s, lastModified: %s}, Request {method: %s, headers: %s}",entityTag(),lastModified(),method,builder.build());
	}
	
	private PreconditionEvaluation executePreconditionEvaluation() {
		HttpHeaders headers = builder.build();
		log(evaluationContext());
		return ConditionalRequestHelper.newInstance(entityTag(), lastModified()).evaluate(method, headers);
	}

	private void assertThatValidationIsNegative(PreconditionEvaluation result, Status status, String prefix, String... substrings) {
		assertThat(reason("A result should be returned"),result,is(notNullValue()));
		assertThat(reason("The evaluation should not be positive"),result.isPositive(),is(not(equalTo(true))));
		assertThat(reason("Invalid status"),result.getFailureStatus(),equalTo(status));
		assertThat(reason("A failure message should be returned"),result.getFailureMessage(), is(notNullValue()));
		assertThat(reason("Invalid failure prefix"),result.getFailureMessage(),startsWith(prefix));
		assertThatFailureMessageHasSubstrings(result, substrings);
	}

	private String reason(String reason) {
		return reason+" ("+evaluationContext()+")";
	}

	public void assertThatFailureMessageHasSubstrings(PreconditionEvaluation result, String... substrings) {
		for(String n:substrings) {
			assertThat(reason("Missing substring in failure message"),result.getFailureMessage(),containsString(n));
		}
	}

	private ValidatorAcceptor assertFor(String... methods) {
		return new ValidatorAcceptor(methods);
	}
	
	private interface Validator {
		
		void validate(PreconditionEvaluation result);
		
	}
	
	private final class ValidatorAcceptor {
		
		private final String[] methods;

		private ValidatorAcceptor(String[] methods) {
			this.methods = methods;
		}
		
		public void that(Validator validator) {
			for(String method:methods) {
				configureMethod(method);
				validator.validate(executePreconditionEvaluation());
			}
		}
		
	}

	//--------------------------------------------------------------------------
	// Tests
	//--------------------------------------------------------------------------
	
	@Test(expected=NullPointerException.class)
	public void testNewInstanceNullEntityTag() {
		ConditionalRequestHelper.newInstance(null, NOW);
		fail("Null entity tag should not be accepted");
	}

	@Test(expected=NullPointerException.class)
	public void testNewInstanceNullLastModified() {
		ConditionalRequestHelper.newInstance(createEntityTag("value", false), null);
		fail("Null last modified should not be accepted");
	}

	@Test(expected=NullPointerException.class)
	public void testEvaluatePreconditionNullMethod() {
		ConditionalRequestHelper sut = ConditionalRequestHelper.newInstance(createEntityTag("value", true), NOW);
		sut.evaluate(null, builder.build());
	}

	@Test(expected=NullPointerException.class)
	public void testEvaluatePreconditionNullHeaders() {
		ConditionalRequestHelper sut = ConditionalRequestHelper.newInstance(createEntityTag("value", true), NOW);
		sut.evaluate(ALL_METHODS[0], null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEvaluatePreconditionInvalidMethod() {
		ConditionalRequestHelper sut = ConditionalRequestHelper.newInstance(createEntityTag("value", true), NOW);
		sut.evaluate("invalid", builder.build());
	}
	
	@Test
	public void testEvaluatePreconditions_ifMatch_allMethods_happyPath_strongEntityTag() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,entityTag());
		assertFor(ALL_METHODS).
			that(new ValidationIsPositive());
	}

	@Test
	public void testEvaluatePreconditions_ifMatch_allMethods_happyPath_lastFromMany() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,createEntityTag("other",true));
		configureHttpHeader(HttpHeaders.IF_MATCH,entityTag());
		assertFor(ALL_METHODS).
			that(new ValidationIsPositive());
	}

	@Test
	public void testEvaluatePreconditions_ifMatch_allMethods_happyPath_oneFromMany() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,createEntityTag("other",true));
		configureHttpHeader(HttpHeaders.IF_MATCH,entityTag());
		configureHttpHeader(HttpHeaders.IF_MATCH,createEntityTag("another",true));
		assertFor(ALL_METHODS).
			that(new ValidationIsPositive());
	}

	@Test
	public void testEvaluatePreconditions_ifMatch_allMethods_happyPath_matchingWildcard() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,"*");
		assertFor(ALL_METHODS).
			that(new ValidationIsPositive());
	}

	@Test
	public void testEvaluatePreconditions_ifMatch_allMethods_exceptionPath_weakEntityTag() throws Exception {
		useEntityTag("value",true);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,entityTag());
		assertFor(ALL_METHODS).
			that(new ValidationIsNegativeWhenIfMatchPreconditionFails());
	}

	@Test
	public void testEvaluatePreconditions_ifMatch_allMethods_exceptionPath_notMatchingTag() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,createEntityTag("otherValue",true));
		assertFor(ALL_METHODS).
			that(new ValidationIsNegativeWhenIfMatchPreconditionFails());
	}

	@Test
	public void testEvaluatePreconditions_ifMatch_allMethods_exceptionPath_invalidEntityTag() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,"\"unclosed");
		assertFor(ALL_METHODS).
			that(new ValidationIsNegativeWhenIfMatchPreconditionFails());
	}

	@Test
	public void testEvaluatePreconditions_ifUnmodifiedSince_allMethods_happyPath() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);

		configureHttpHeader(HttpHeaders.IF_UNMODIFIED_SINCE,lastModified());
		assertFor(ALL_METHODS).
			that(new ValidationIsPositive());

		configureHttpHeader(HttpHeaders.IF_UNMODIFIED_SINCE,DAY_BEFORE);
		assertFor(ALL_METHODS).
			that(new ValidationIsPositive());
	}

	@Test
	public void testEvaluatePreconditions_ifUnmodifiedSince_allMethods_happyPath_invalidDate() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_UNMODIFIED_SINCE,lastModified().toString());
		assertFor(ALL_METHODS).
			that(new ValidationIsPositive());
	}

	@Test
	public void testEvaluatePreconditions_ifUnmodifiedSince_allMethods_exceptionPath() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_UNMODIFIED_SINCE,DAY_BEFORE);
		assertFor(ALL_METHODS).
			that(new ValidationIsNegativeWhenIfUnmodifiedSincePreconditionFails());
	}

	@Test
	public void testEvaluatePreconditions_ifNoneMatch_allMethods_exceptionPath_strongEntityTag() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_NONE_MATCH,entityTag());
		assertFor(ALL_METHODS).
			that(new ValidationIsNegativeWhenIfNoneMatchPreconditionFails());
	}

	@Test
	public void testEvaluatePreconditions_ifNoneMatch_allMethods_exceptionPath_weakEntityTag() throws Exception {
		useEntityTag("value",true);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_NONE_MATCH,entityTag());
		assertFor(ALL_METHODS).
			that(new ValidationIsNegativeWhenIfNoneMatchPreconditionFails());
	}

	@Test
	public void testEvaluatePreconditions_ifNoneMatch_allMethods_exceptionPath_weakStrongEntities() throws Exception {
		useEntityTag("value",true);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_NONE_MATCH,createEntityTag("value",false));
		assertFor(ALL_METHODS).
			that(new ValidationIsNegativeWhenIfNoneMatchPreconditionFails());
	}

	@Test
	public void testEvaluatePreconditions_ifNoneMatch_allMethods_exceptionPath_strongWeakEntities() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_NONE_MATCH,createEntityTag("value",true));
		assertFor(ALL_METHODS).
			that(new ValidationIsNegativeWhenIfNoneMatchPreconditionFails());
	}

	@Test
	public void testEvaluatePreconditions_ifMatchAndIfModifiedSince_allMethods_exceptionPath_etag() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,createEntityTag("other",false));
		configureHttpHeader(HttpHeaders.IF_MODIFIED_SINCE,DAY_BEFORE);
		assertFor(ALL_METHODS).
			that(new ValidationIsNegativeWhenIfMatchPreconditionFails());
	}

	@Test
	public void testEvaluatePreconditions_ifMatchAndIfModifiedSince_nonGetHeadMethods_happyPath() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,entityTag());

		// In case of GET/HEAD this would succeed
		configureHttpHeader(HttpHeaders.IF_MODIFIED_SINCE,DAY_BEFORE);
		assertFor(NON_GET_HEAD_METHODS).
			that(new ValidationIsPositive());

		// In case of GET/HEAD this would fail
		configureHttpHeader(HttpHeaders.IF_MODIFIED_SINCE,NOW);
		assertFor(NON_GET_HEAD_METHODS).
			that(new ValidationIsPositive());

		// In case of GET/HEAD this would fail
		configureHttpHeader(HttpHeaders.IF_MODIFIED_SINCE,DAY_AFTER);
		assertFor(NON_GET_HEAD_METHODS).
			that(new ValidationIsPositive());
	}

	@Test
	public void testEvaluatePreconditions_ifMatchAndIfModifiedSince_getHeadMethods_happyPath() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,entityTag());
		configureHttpHeader(HttpHeaders.IF_MODIFIED_SINCE,DAY_BEFORE);
		assertFor(GET_HEAD_METHODS).
			that(new ValidationIsPositive());
	}

	@Test
	public void testEvaluatePreconditions_ifMatchAndIfModifiedSince_getHeadMethods_happyPath_invalidDate() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,entityTag());
		configureHttpHeader(HttpHeaders.IF_MODIFIED_SINCE,DAY_AFTER.toString());
		assertFor(GET_HEAD_METHODS).
			that(new ValidationIsPositive());
	}

	@Test
	public void testEvaluatePreconditions_ifMatchAndIfModifiedSince_getHeadMethods_exceptionPath_sameAsLastModified() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,entityTag());
		configureHttpHeader(HttpHeaders.IF_MODIFIED_SINCE,NOW);
		assertFor(GET_HEAD_METHODS).
			that(new ValidationIsNegativeWhenIfModifiedSincePreconditionFails());
	}

	@Test
	public void testEvaluatePreconditions_ifMatchAndIfModifiedSince_getHeadMethods_exceptionPath_afterLastModified() throws Exception {
		useEntityTag("value",false);
		useLastModified(NOW);
		configureHttpHeader(HttpHeaders.IF_MATCH,entityTag());
		configureHttpHeader(HttpHeaders.IF_MODIFIED_SINCE,DAY_AFTER);
		assertFor(GET_HEAD_METHODS).
			that(new ValidationIsNegativeWhenIfModifiedSincePreconditionFails());
	}

}
