<?xml version="1.0" encoding="UTF-8"?>
<!--

    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
      This file is part of the LDP4j Project:
        http://www.ldp4j.org/

      Center for Open Middleware
        http://www.centeropenmiddleware.com/
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
      Copyright (C) 2014 Center for Open Middleware.
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at

                http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
      Artifact    : org.ldp4j.framework:ldp4j-server-command:1.0.0-SNAPSHOT
      Bundle      : ldp4j-server-command-1.0.0-SNAPSHOT.jar
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#

-->
<xsl:stylesheet 
	version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema" 
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:error="urn:xmltidy:lib:error" 
	xmlns:format="urn:xmltidy:lib:format"
	xmlns:internal="urn:xmltidy:lib:format:internal">

	<xsl:import href="module-error.xslt"/>

	<xsl:function name="format:formatAttribute" as="xs:string">
		<xsl:param name="attr" as="xs:string"/>
		<xsl:param name="value" as="xs:string"/>
		<xsl:value-of select="concat($attr,'=&quot;',$value,'&quot;')"/>
	</xsl:function>

	<xsl:function name="format:formatNamespaceDeclaration" as="xs:string">
		<xsl:param name="element" as="element()"/>
		<xsl:param name="prefix" as="xs:string"/>
		<xsl:param name="uri" as="xs:string"/>
		<xsl:choose>
			<xsl:when test="fn:string-length($prefix)>0">
				<xsl:variable name="nsName" select="fn:concat('xmlns:',$prefix)"/>
				<xsl:value-of select="format:formatAttribute($nsName,$uri)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="ancestorDefaultNS" select="fn:distinct-values(for $n in $element/ancestor-or-self::* return fn:namespace-uri-for-prefix('',$n))"/>
				<xsl:choose>
					<xsl:when test="fn:count($ancestorDefaultNS)>1">
						<xsl:value-of select="format:formatAttribute('xmlns',$uri)"/>
					</xsl:when>
					<xsl:when test="fn:count($ancestorDefaultNS)=1 and fn:string-length($ancestorDefaultNS[1])>0">
						<xsl:value-of select="format:formatAttribute('xmlns',$uri)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="''"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="format:indent" as="xs:string">
		<xsl:param name="level" as="xs:integer"/>
		<xsl:choose>
			<xsl:when test="$level &lt; 0 or fn:contains(xs:string($level), '.')">
				<xsl:value-of select="error:throw('urn:xmltidy:lib:format','format:indent','The function does not support negative or fractional arguments.')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="internal:_indent($level,'')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="internal:_indent" as="xs:string">
		<xsl:param name="level" as="xs:integer"/>
		<xsl:param name="result" as="xs:string"/>
		<xsl:choose>
			<xsl:when test="$level = 0">
				<xsl:value-of select="$result"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="internal:_indent($level - 1,fn:concat('  ',$result))"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

</xsl:stylesheet>