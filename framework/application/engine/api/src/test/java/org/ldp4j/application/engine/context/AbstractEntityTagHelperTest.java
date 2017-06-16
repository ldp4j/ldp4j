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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-api:0.2.2
 *   Bundle      : ldp4j-application-engine-api-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

import org.junit.Test;

public abstract class  AbstractEntityTagHelperTest {

	protected static final String QUOTED_STRONG_ETAG = "\"value\"";
	protected static final String QUOTED_WEAK_ETAG = "W/\"value\"";

	private static final String QUOTED_STRONG_EMPTY = "\"\"";
	private static final String QUOTED_WEAK_EMPTY = "W/\"\"";

	private static final String UNQUOTED_REGULAR_VALUE = "value";
	private static final String UNQUOTED_WEAK_VALUE = "W/value";

	private static final String UNQUOTED_STRONG_EMPTY = "";
	private static final String UNQUOTED_WEAK_EMPTY = "W/";

	private static final String UNQUOTED_WEAK_WILDCARD = "W/*";
	private static final String UNQUOTED_STRONG_WILDCARD = "*";

	private static final String QUOTED_ESCAPED_QUOTATION_MARK = new String(new byte[] {0x22,'b','a','d','\\',0x22,0x22});
	private static final String UNQUOTED_ESCAPED_QUOTATION_MARK = new String(new byte[] {'b','a','d','\\',0x22});

	private static final String QUOTED_QUOTATION_MARK = "\"\"\"";
	private static final String QUOTED_DANGLING_MIDDLE_QUOTATION_MARK = "\"danglingMiddle\"QuotationMark\"";

	private static final String UNQUOTED_QUOTATION_MARK = "\"";
	private static final String UNQUOTED_DANGLING_MIDDLE_QUOTATION_MARK = "danglingMiddle\"QuotationMark";

	private static final String DANGLING_FINAL_QUOTATION_MARK = "danglingFinalQuotationMark\"";
	private static final String DANGLING_INITIAL_QUOTATION_MARK = "\"danglingInitialQuotationMark";

	protected static final EntityTag WEAK_ETAG   = EntityTag.createWeak(UNQUOTED_REGULAR_VALUE);
	protected static final EntityTag STRONG_ETAG = EntityTag.createStrong(UNQUOTED_REGULAR_VALUE);

	protected abstract EntityTag fromString(String testCase);

/**
	@Test
	public void testRewrite() {
		String[] cases={
			QUOTED_STRONG_EMPTY,
			QUOTED_STRONG_ETAG,
			QUOTED_WEAK_EMPTY,
			QUOTED_WEAK_ETAG,
			DANGLING_FINAL_QUOTATION_MARK,
			DANGLING_INITIAL_QUOTATION_MARK,
			QUOTED_DANGLING_MIDDLE_QUOTATION_MARK,
			UNQUOTED_DANGLING_MIDDLE_QUOTATION_MARK,
			QUOTED_QUOTATION_MARK,
			UNQUOTED_QUOTATION_MARK,
			UNQUOTED_STRONG_EMPTY,
			UNQUOTED_REGULAR_VALUE,
			UNQUOTED_STRONG_WILDCARD,
			UNQUOTED_WEAK_EMPTY,
			UNQUOTED_WEAK_VALUE,
			UNQUOTED_WEAK_WILDCARD,
			QUOTED_ESCAPED_QUOTATION_MARK
		};
		for(String text:cases) {
			try {
				System.out.println("Current -------> "+EntityTagHelper.fromString(text));
			} catch (Exception e) {
				System.err.println("Current -------> "+e.getMessage());
			}
			try {
				System.out.println("State machine -> "+EntityTagHelper.fromString0(text));
			} catch (Exception e) {
				System.err.println("State machine -> "+e.getMessage());
			}
			System.out.println("RegEx ---------> "+ EntityTagHelper.fromString1(text));
		}
	}
*/

	@Test
	public void testFromString$nullETag() {
		try {
			fromString(null);
			fail("Should not accept null");
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void testFromString$wildcard() {
		EntityTag etag=fromString(UNQUOTED_STRONG_WILDCARD);
		assertThat(etag.getValue(),equalTo(UNQUOTED_STRONG_WILDCARD));
		assertThat(etag.isWeak(),equalTo(false));
	}

	@Test
	public void testFromString$wildcard$weak() {
		EntityTag etag=fromString(UNQUOTED_WEAK_WILDCARD);
		assertThat(etag.getValue(),equalTo(UNQUOTED_STRONG_WILDCARD));
		assertThat(etag.isWeak(),equalTo(true));
	}

	@Test
	public void testFromString$regularEmpty$unquoted() {
		EntityTag etag=fromString(UNQUOTED_STRONG_EMPTY);
		assertThat(etag.getValue(),equalTo(UNQUOTED_STRONG_EMPTY));
		assertThat(etag.isWeak(),equalTo(false));
	}

	@Test
	public void testFromString$regularEmpty$quoted() {
		EntityTag etag=fromString(QUOTED_STRONG_EMPTY);
		assertThat(etag.getValue(),equalTo(UNQUOTED_STRONG_EMPTY));
		assertThat(etag.isWeak(),equalTo(false));
	}

	@Test
	public void testFromString$weakEmpty$unquoted() {
		EntityTag etag=fromString(UNQUOTED_WEAK_EMPTY);
		assertThat(etag.getValue(),equalTo(UNQUOTED_STRONG_EMPTY));
		assertThat(etag.isWeak(),equalTo(true));
	}

	@Test
	public void testFromString$weakEmpty$quoted() {
		EntityTag etag=fromString(QUOTED_WEAK_EMPTY);
		assertThat(etag.getValue(),equalTo(UNQUOTED_STRONG_EMPTY));
		assertThat(etag.isWeak(),equalTo(true));
	}

	@Test
	public void testFromString$regularETag$unquoted() {
		EntityTag etag=fromString(UNQUOTED_REGULAR_VALUE);
		assertThat(etag,equalTo(STRONG_ETAG));
	}

	@Test
	public void testFromString$weakETag$unquoted() {
		EntityTag etag=fromString(UNQUOTED_WEAK_VALUE);
		assertThat(etag,equalTo(WEAK_ETAG));
	}

	@Test
	public void testFromString$regularETag$quoted() {
		EntityTag etag=fromString(QUOTED_STRONG_ETAG);
		assertThat(etag,equalTo(STRONG_ETAG));
	}

	@Test
	public void testFromString$weakETag$quoted() {
		EntityTag etag=fromString(QUOTED_WEAK_ETAG);
		assertThat(etag,equalTo(WEAK_ETAG));
	}

	@Test
	public void testFromString$strongETag$quoted$scaped() {
		EntityTag etag=fromString(QUOTED_ESCAPED_QUOTATION_MARK);
		assertThat(etag.getValue(),equalTo(UNQUOTED_ESCAPED_QUOTATION_MARK));
		assertThat(etag.isWeak(),equalTo(false));
	}

	@Test
	public void testFromString$strongETag$unquoted$escaped() {
		EntityTag etag=fromString(UNQUOTED_ESCAPED_QUOTATION_MARK);
		assertThat(etag.getValue(),equalTo(UNQUOTED_ESCAPED_QUOTATION_MARK));
		assertThat(etag.isWeak(),equalTo(false));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$danglingInitialQuotationMark$min() {
		fromString(QUOTED_QUOTATION_MARK);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$danglingInitialQuotationMark$regular() {
		fromString(DANGLING_INITIAL_QUOTATION_MARK);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$danglingFinalQuotationMark$min() {
		fromString(UNQUOTED_QUOTATION_MARK);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$danglingFinalQuotationMark$regular() {
		fromString(DANGLING_FINAL_QUOTATION_MARK);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$danglingMiddleQuotationMark$quoted() {
		fromString(QUOTED_DANGLING_MIDDLE_QUOTATION_MARK);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$danglingMiddleQuotationMark$unquoted() {
		fromString(UNQUOTED_DANGLING_MIDDLE_QUOTATION_MARK);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$danglingScapedChar$quoted() {
		fromString("\"bad\\\"");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$danglingScapeChar$unquoted() {
		fromString("bad\\");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$badScapedChar$quoted() {
		fromString(new String(new char[] {0x22,'b','a','d','\\',0x80,0x22}));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$badScapeChar$unquoted() {
		fromString(new String(new char[] {'b','a','d','\\',0x80}));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$controlCode$quoted() {
		fromString(new String(new char[] {0x22,'b','a','d',0x13,0x22}));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$controlCode$unquoted() {
		fromString(new String(new char[] {'b','a','d',0x13}));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$delCode$quoted() {
		fromString(new String(new char[] {0x22,'b','a','d',0x7f,0x22}));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromString$delCode$unquoted() {
		fromString(new String(new char[] {'b','a','d',0x7f}));
	}

}
