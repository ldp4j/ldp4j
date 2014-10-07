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
package org.ldp4j.server.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that provides the means for evaluation conditional requests
 * according to RFC 7232. The current implementation does not support If-Range/
 * Range precondition evaluation.
 * 
 * @author Miguel Esteban Guti&eacute;rrez
 * @see <a href="http://tools.ietf.org/rfc/rfc7232.txt">RFC 7232 &mdash; Hypertext Transfer Protocol (HTTP/1.1): Conditional Requests</a>
 */
public class ConditionalRequestHelper {

	public static final class PreconditionEvaluation {
		
		private final String errorMessage;
		private final Status status;
		
		public PreconditionEvaluation(Status status, String errorMessage) {
			this.status = status;
			this.errorMessage = errorMessage;
		}
		
		public PreconditionEvaluation() {
			this(null,null);
		}
		
		public boolean isPositive() {
			return status==null;
		}
		
		public Status getFailureStatus() {
			return status;
		}
		
		public String getFailureMessage() {
			return errorMessage;
		}
		
		public String toString() {
			if(isPositive()) {
				return "Evaluation succeded: preconditions met";
			} else {
				return String.format("Evaluation failed (%s - %s): %s",status.getStatusCode(),status.getReasonPhrase(),errorMessage);
			}
		}
	}

	private enum HttpMethod {
		HEAD("HEAD"),
		GET("GET"),
		OPTIONS("OPTIONS"),
		PUT("PUT"),
		PATCH("PATCH"),
		POST("POST"),
		DELETE("DELETE")
		;
		
		private final String method;

		private HttpMethod(String method) {
			this.method = method;
		}
		
		public static HttpMethod fromString(String method) {
			for(HttpMethod candidate:values()) {
				if(candidate.method.equalsIgnoreCase(method)) {
					return candidate;
				}
			}
			return null;
		}
		
		@Override
		public String toString() {
			return method;
		}
	}
	
	private interface EntityTagComparator {
		
		boolean match(EntityTag e1, EntityTag e2);
		
	}
	
	private static class StrongEntityTagComparator implements EntityTagComparator {

		@Override
		public boolean match(EntityTag e1, EntityTag e2) {
			return !e1.isWeak() && !e2.isWeak() && e1.getValue().equals(e2.getValue());
		}
		
	}
	
	private static class WeakEntityTagComparator implements EntityTagComparator {

		@Override
		public boolean match(EntityTag e1, EntityTag e2) {
			return e1.getValue().equals(e2.getValue());
		}
		
	}

	private final class PreconditionEvaluator {

		private final HttpMethod method;
		private final HttpHeaders headers;

		public PreconditionEvaluator(HttpMethod method, HttpHeaders headers) {
			this.method = method;
			this.headers = headers;
		}

		public PreconditionEvaluation evaluate() {
			PreconditionEvaluation result = evaluateIfMatchIfUnmodifiedSince();
			if(result==null) {
				result=evaluateIfNoneMatchIfModifiedSince();
			}
			if(result==null) {
				result=new PreconditionEvaluation();
			}
			return result;
		}

		private PreconditionEvaluation evaluateIfNoneMatchIfModifiedSince() {
			PreconditionEvaluation result=null;
			List<String> ifNoneMatch = getHeader(HttpHeaders.IF_NONE_MATCH);
			if(!ifNoneMatch.isEmpty()) {
				if(findMatchingEntityTag(ifNoneMatch,new WeakEntityTagComparator())!=null) {
					Status status= 
							method.equals(HttpMethod.GET) || method.equals(HttpMethod.HEAD) ?
								Status.NOT_MODIFIED:
								Status.PRECONDITION_FAILED;
					result=new PreconditionEvaluation(status,"DelegatedResourceSnapshot entity tag '"+entityTag+"' matches one of "+ifNoneMatch);
				}
			} else if(method.equals(HttpMethod.GET) || method.equals(HttpMethod.HEAD)) {
				Date ifModifiedSince = getDateRelatedHeaderOrNull(HttpHeaders.IF_MODIFIED_SINCE);
				if(ifModifiedSince!=null && !ifModifiedSince.before(lastModified)) {
					result=new PreconditionEvaluation(Status.NOT_MODIFIED,"DelegatedResourceSnapshot has not been modified since '"+lastModified+"' (before than required '"+ifModifiedSince+"')");
				}
			}
			return result;
		}

		private PreconditionEvaluation evaluateIfMatchIfUnmodifiedSince() {
			PreconditionEvaluation result=null;
			List<String> ifMatch = getHeader(HttpHeaders.IF_MATCH);
			if(!ifMatch.isEmpty()) {
				if(findMatchingEntityTag(ifMatch,new StrongEntityTagComparator())==null) {
					result=new PreconditionEvaluation(Status.PRECONDITION_FAILED,"DelegatedResourceSnapshot entity tag '"+entityTag+"' does not match any of "+ifMatch);
				} 
			} else {
				Date ifUnmodifiedSince = getDateRelatedHeaderOrNull(HttpHeaders.IF_UNMODIFIED_SINCE);
				if(ifUnmodifiedSince!=null && ifUnmodifiedSince.before(lastModified)) {
					result=new PreconditionEvaluation(Status.PRECONDITION_FAILED,"DelegatedResourceSnapshot has been modified in '"+lastModified+"' (after than required '"+ifUnmodifiedSince+"')");
				}
			}
			return result;
		}

		private String findMatchingEntityTag(List<String> headers, EntityTagComparator comparator) {
			for(String value:headers) {
				if("*".equals(value)) {
					return value;
				}
				try {
					EntityTag requestTag = EntityTag.valueOf(value) ;
					if(comparator.match(requestTag,entityTag)) {
						return value;
					}
				} catch (IllegalArgumentException ex) {
					// Best effort, try next
					if(LOGGER.isTraceEnabled()) {
						LOGGER.trace("Discarding invalid entity tag",ex);
					}
				}
			}
			return null;
		}

		private List<String> getHeader(String header) {
			List<String> values = headers.getRequestHeader(header);
			if(values==null) {
				values=new ArrayList<String>();
			}
			return values;
		}

		private Date getDateRelatedHeaderOrNull(String header) {
			Date result=null;
			List<String> values = getHeader(header);
			if(!values.isEmpty()) {
				try {
					result=HttpDateUtils.parse(values.get(0));
				} catch (UnknownHttpDateFormatException ex) {
					// Ignore, as per http://tools.ietf.org/search/rfc7232#section-3.4
					if(LOGGER.isTraceEnabled()) {
						LOGGER.trace("Discarding invalid HTTP date",ex);
					}
				}
			}
			return result;
		}
		
	}
	
	private static final Logger LOGGER=LoggerFactory.getLogger(ConditionalRequestHelper.class);
	
	private final EntityTag entityTag;
	private final Date lastModified;

	private ConditionalRequestHelper(Date lastModified, EntityTag entityTag) {
		this.lastModified = lastModified;
		this.entityTag = entityTag;
	}
	
	public PreconditionEvaluation evaluate(String method, HttpHeaders headers) {
		checkNotNull(method,"Object 'method' cannot be null");
		checkNotNull(headers,"Object 'headers' cannot be null");
		HttpMethod parsedMethod = HttpMethod.fromString(method);
		checkArgument(parsedMethod!=null,"Unknown HTTP method '"+method+"'");
		return new PreconditionEvaluator(parsedMethod,headers).evaluate();
	}
	
	public static ConditionalRequestHelper newInstance(EntityTag entityTag, Date lastModified) {
		checkNotNull(entityTag,"Object 'entityTag' cannot be null");
		checkNotNull(lastModified,"Object 'lastModified' cannot be null");
		return new ConditionalRequestHelper(lastModified, entityTag);
	}
	
}
