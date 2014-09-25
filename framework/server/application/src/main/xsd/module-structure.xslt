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
	xmlns:strc="urn:xmltidy:lib:structure"
	xmlns:internal="urn:xmltidy:lib:structure:internal"
	xmlns:math="urn:xmltidy:lib:math" 
	xmlns:ns="urn:xmltidy:lib:namespaces"
	xmlns:debug="urn:xmltidy:lib:debug">
	
	<xsl:import href="module-math.xslt"/>
	<xsl:import href="module-namespaces.xslt"/>
	<xsl:import href="module-debug.xslt"/>
	
	<xsl:function name="strc:hasBody" as="xs:boolean">
		<xsl:param name="value" as="xs:integer"/>
		<xsl:value-of select="math:bitset($value,0)"/>
	</xsl:function>

	<xsl:function name="strc:hasHeader" as="xs:boolean">
		<xsl:param name="value" as="xs:integer"/>
		<xsl:value-of select="strc:hasAttributes($value) or strc:hasNamespaces($value)"/>
	</xsl:function>

	<xsl:function name="strc:hasAttributes" as="xs:boolean">
		<xsl:param name="value" as="xs:integer"/>
		<xsl:value-of select="math:bitset($value,1)"/>
	</xsl:function>

	<xsl:function name="strc:hasNamespaces" as="xs:boolean">
		<xsl:param name="value" as="xs:integer"/>
		<xsl:value-of select="math:bitset($value,2)"/>
	</xsl:function>

	<xsl:function name="strc:getCase" as="xs:integer">
		<xsl:param name="element" as="element()"/>
		<xsl:param name="ndt" as="element()"/>
		<xsl:variable name="hasNamespaces" select="ns:hasNamespaces($ndt,$element)"/>
		<xsl:variable name="attrCount" select="fn:count($element/@*)"/>
		<xsl:variable name="childCount" select="fn:count($element/node())"/>
		<xsl:call-template name="debug:trace">
			<xsl:with-param name="message">
				<xsl:value-of select="fn:concat('Case(',$hasNamespaces,',',$attrCount,',',$childCount,')')"/>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:choose>
			<xsl:when test="fn:not($hasNamespaces) and $attrCount=0 and $childCount=0">
				<xsl:value-of select="0"/>
			</xsl:when>
			<xsl:when test="fn:not($hasNamespaces) and $attrCount=0 and $childCount>0">
				<xsl:value-of select="1"/>
			</xsl:when>
			<xsl:when test="fn:not($hasNamespaces) and $attrCount>0 and $childCount=0">
				<xsl:value-of select="2"/>
			</xsl:when>
			<xsl:when test="fn:not($hasNamespaces) and $attrCount>0 and $childCount>0">
				<xsl:value-of select="3"/>
			</xsl:when>
			<xsl:when test="$hasNamespaces and $attrCount=0 and $childCount=0">
				<xsl:value-of select="4"/>
			</xsl:when>
			<xsl:when test="$hasNamespaces and $attrCount=0 and $childCount>0">
				<xsl:value-of select="5"/>
			</xsl:when>
			<xsl:when test="$hasNamespaces and $attrCount>0 and $childCount=0">
				<xsl:value-of select="6"/>
			</xsl:when>
			<xsl:when test="$hasNamespaces and $attrCount>0 and $childCount>0">
				<xsl:value-of select="7"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="8"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

</xsl:stylesheet>