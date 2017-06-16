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

import java.nio.charset.Charset;
import java.util.Map;

/**
 * An abstraction for a media type. <br>
 *
 * <b>NOTE</b>: <br>
 * <br>
 *
 * RFC 2046 specified that the default "charset" parameter (i.e., the value used
 * when the parameter is not specified) is "US-ASCII" (Section 4.1.2 of
 * [RFC2046]). RFC 2616 changed the default for use by HTTP (Hypertext Transfer
 * Protocol) to be "ISO-8859-1" (Section 3.7.1 of [RFC2616]). This encoding is
 * not very common for new "text/*" media types and a special rule in the HTTP
 * specification adds confusion about which specification ([RFC2046] or
 * [RFC2616]) is authoritative in regards to the default charset for "text/*"
 * media types. <br>
 * <br>
 *
 * Many complex text subtypes such as "text/html" [RFC2854] and "text/ xml"
 * [RFC3023] have internal (to their format) means of describing the charset.
 * Many existing User Agents ignore the default of "US-
 * ASCII" rule for at least "text/html" and "text/xml". <br>
 * <br>
 *
 * RFC 6657 changes RFC 2046 rules regarding default "charset" parameter values
 * for "text/*" media types to better align with common usage by existing
 * clients and servers. It does not change the defaults for any currently
 * registered media type. <br>
 * <br>
 *
 * Each new subtype of the "text" media type that uses the "charset" parameter
 * can define its own default value for the "charset" parameter, including the
 * absence of any default. <br>
 * <br>
 *
 * In order to improve interoperability with deployed agents, "text/*" media
 * type registrations SHOULD either <br>
 * a. specify that the "charset" parameter is not used for the defined subtype,
 * because the charset information is transported inside the payload (such as in
 * "text/xml"), or <br>
 * b. require explicit unconditional inclusion of the "charset" parameter,
 * eliminating the need for a default value. <br>
 * <br>
 *
 * In accordance with option (a) above, registrations for "text/*" media types
 * that can transport charset information inside the corresponding payloads
 * (such as "text/html" and "text/xml") SHOULD NOT specify the use of a
 * "charset" parameter, nor any default value, in order to avoid conflicting
 * interpretations should the "charset" parameter value and the value specified
 * in the payload disagree. <br>
 * <br>
 *
 * Thus, new subtypes of the "text" media type SHOULD NOT define a default
 * "charset" value. If there is a strong reason to do so despite this advice,
 * they SHOULD use the "UTF-8" [RFC3629] charset as the default. <br>
 * <br>
 *
 * Regardless of what approach is chosen, all new "text/*" registrations MUST
 * clearly specify how the charset is determined; relying on the default defined
 * in Section 4.1.2 of [RFC2046] is no longer permitted. However, existing
 * "text/*" registrations that fail to specify how the charset is determined
 * still default to US-ASCII. <br>
 * <br>
 *
 * Specifications covering the "charset" parameter, and what default value, if
 * any, is used, are subtype-specific, NOT protocol-specific. Protocols that use
 * MIME, therefore, MUST NOT override default charset values for "text/*" media
 * types to be different for their specific protocol. The protocol definitions
 * MUST leave that to the subtype definitions. <br>
 * <br>
 *
 * However, the default "charset" parameter value for "text/plain" is unchanged
 * from [RFC2046] and remains as "US-ASCII".
 *
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.1">[RFC 7231]
 *      Hypertext Transfer Protocol (HTTP/1.1): Semantics and Content, section
 *      3.1.1.1</a>
 * @see <a href="https://tools.ietf.org/html/rfc6838#section-4.2.8">[RFC 6838]
 *      Media Type Specifications and Registration Procedures, section 4.2.8</a>
 * @see <a href="http://tools.ietf.org/html/rfc6657">[RFC 6657] Update to MIME
 *      regarding "charset" Parameter Handling in Textual Media Types</a>
 */
public interface MediaType extends Negotiable {

	/**
	 * Get the primary type.
	 *
	 * @return value of the primary type.
	 */
	String type();

	/**
	 * Get the subtype.
	 *
	 * @return value of the subtype.
	 */
	String subType();

	/**
	 * Get the structure suffix
	 *
	 * @return value of the suffix, or {@code null} if media type is not
	 *         structure.
	 */
	String suffix();

	/**
	 * Get the preferred charset for the media type.
	 *
	 * @return the preferred charset or {@code null} if non is defined
	 */
	Charset charset();

	/**
	 * Get a <b>read-only</b> parameter map with case-insensitive keys.
	 *
	 * @return an immutable map of parameters.
	 */
	Map<String,String> parameters();

}