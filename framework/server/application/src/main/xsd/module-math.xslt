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
      Artifact    : org.ldp4j.framework:ldp4j-server-application:1.0.0-SNAPSHOT
      Bundle      : ldp4j-server-application-1.0.0-SNAPSHOT.jar
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#

-->
<xsl:stylesheet 
	version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema" 
	xmlns:fn="http://www.w3.org/2005/xpath-functions" 
	xmlns:error="urn:xmltidy:lib:error" 
	xmlns:math="urn:xmltidy:lib:math" 
	xmlns:internal="urn:xmltidy:lib:math:internal">

	<xsl:import href="module-error.xslt"/>

	<!-- power(base, power) := base ^ power -->
	<xsl:function name="math:power" as="xs:integer">
		<xsl:param name="base" as="xs:integer"/>
		<xsl:param name="power" as="xs:integer"/>
		<xsl:choose>
			<xsl:when test="$power &lt; 0 or fn:contains(string($power), '.')">
				<xsl:value-of select="error:throw('urn:xmltidy:lib:math','math:power','The function does not support negative or fractional arguments.')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="internal:_power($base,$power,1)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="internal:_power" as="xs:integer">
		<xsl:param name="base" as="xs:integer"/>
		<xsl:param name="power" as="xs:integer"/>
		<xsl:param name="result" as="xs:integer"/>
		<xsl:choose>
			<xsl:when test="$power = 0">
				<xsl:value-of select="$result"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="internal:_power($base, $power - 1, $result * $base)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	<!-- bitset(x, n) := floor(x / 2^n) mod 2 == 1 -->
	<xsl:function name="math:bitset" as="xs:boolean">
		<xsl:param name="x" as="xs:integer"/>
		<xsl:param name="n" as="xs:integer"/>
		<xsl:choose>
			<xsl:when test="$n &lt; 0 or fn:contains(string($n), '.')">
				<xsl:value-of select="error:throw('urn:xmltidy:lib:math','math:bitset','The function does not support negative or fractional arguments.')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="fn:floor($x div  math:power(2,$n)) mod 2 = 1"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

</xsl:stylesheet>