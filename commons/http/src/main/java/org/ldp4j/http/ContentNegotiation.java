package org.ldp4j.http;

public final class ContentNegotiation {

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * {@link <a href="https://tools.ietf.org/html/rfc7231#section-5.3.2">Section 5.3.2</a>}.
	 */
	public static final String ACCEPT = "Accept";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * {@link <a href="https://tools.ietf.org/html/rfc7231#section-5.3.3">Section 5.3.3</a>}.
	 */
	public static final String ACCEPT_CHARSET = "Accept-Charset";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * {@link <a href="https://tools.ietf.org/html/rfc7231#section-5.3.4">Section 5.3.4</a>}.
	 */
	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * {@link <a href="https://tools.ietf.org/html/rfc7231#section-5.3.5">Section 5.3.5</a>}.
	 */
	public static final String ACCEPT_LANGUAGE = "Accept-Language";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * {@link <a href="https://tools.ietf.org/html/rfc7231#section-7.1.4">Section 7.1.4</a>}.
	 */
	public static final String VARY = "Vary";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * {@link <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.5">Section 3.1.1.5</a>}
	 * .
	 */
	public static final String CONTENT_TYPE = "Content-Type";

	/**
	 * See <i>Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content</i>,
	 * {@link <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.2">Section 3.1.3.2</a>}.
	 */
	public static final String CONTENT_LANGUAGE = "Content-Language";

	private ContentNegotiation() {
	}

}
