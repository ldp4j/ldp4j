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
<!DOCTYPE xsl:stylesheet [
	<!ENTITY startTag '&#xE000;'>
	<!ENTITY endTag   '&#xE001;'>
	<!ENTITY eol      '&#xE002;'>
]>
<xsl:stylesheet 
	version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema" 
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:internal="urn:xmltidy:main"
	xmlns:ns="urn:xmltidy:lib:namespaces" 
	xmlns:strc="urn:xmltidy:lib:structure"
	xmlns:math="urn:xmltidy:lib:math" 
	xmlns:format="urn:xmltidy:lib:format">

	<xsl:import href="module-namespaces.xslt"/>
	<xsl:import href="module-structure.xslt"/>
	<xsl:import href="module-format.xslt"/>
	
	<xsl:output method="text" use-character-maps="xmlChMap"/>

	<xsl:preserve-space elements="*"/>

	<xsl:character-map name="xmlChMap">
		<xsl:output-character character="&startTag;" string="&#60;"/>
		<xsl:output-character character="&endTag;"   string="&#62;"/>
		<xsl:output-character character="&eol;"   	 string="&#10;"/>
	</xsl:character-map>

	<xsl:template match="/">
		<xsl:variable name="ndt" select="ns:createNamespaceDefinitionTable(.)"/>
		<xsl:apply-templates select="child::*" mode="generate">
		<xsl:with-param name="ndt" select="$ndt"/>
			<xsl:with-param name="level" select="0"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="element()" mode="generate">
		<xsl:param name="ndt" as="element()"/>
		<xsl:param name="level" as="xs:integer"/>
		<xsl:variable name="element" select="."/>
		<xsl:if test="(fn:count(../element())+fn:count(../comment()))=fn:count(../node())">
			<xsl:if test="$level > 0">
				<xsl:text>&eol;</xsl:text>
			</xsl:if>
			<xsl:value-of select="format:indent($level)"/>
		</xsl:if>
		<xsl:value-of select="internal:_startElement($element,$ndt)"/>
		<xsl:apply-templates select="$element/node()" mode="generate">
			<xsl:with-param name="ndt" select="$ndt"/>
			<xsl:with-param name="level" select="$level + 1"/>
		</xsl:apply-templates>
		<xsl:if test="fn:count($element/text())=0">
			<xsl:if test="fn:count($element/node()) > 0">
				<xsl:text>&eol;</xsl:text>
				<xsl:value-of select="format:indent($level)"/>
			</xsl:if>
		</xsl:if>
		<xsl:value-of select="internal:_endElement($element,$ndt)"/>
	</xsl:template>

	<xsl:template match="comment()" mode="generate">
		<xsl:param name="ndt" as="element()"/>
		<xsl:param name="level" as="xs:integer"/>
		<xsl:variable name="element" select="."/>
		<xsl:if test="(fn:count(../element())+fn:count(../comment()))=fn:count(../node())">
			<xsl:if test="$level > 0">
				<xsl:text>&eol;</xsl:text>
			</xsl:if>
			<xsl:value-of select="format:indent($level)"/>
		</xsl:if>
		<xsl:text>&startTag;!--</xsl:text><xsl:value-of select="."/><xsl:text>--&endTag;</xsl:text>
<!-- 
		<xsl:if test="fn:count($element/text())=0">
			<xsl:if test="fn:count($element/node()) > 0">
				<xsl:text>&eol;</xsl:text>
				<xsl:value-of select="format:indent($level)"/>
			</xsl:if>
		</xsl:if>
 -->
	</xsl:template>

	<xsl:template match="text()" mode="generate">
		<xsl:param name="ndt" as="element()"/>
		<xsl:param name="level" as="xs:integer"/>
		<xsl:copy/>
	</xsl:template>

	<!-- We don't need to take care about processing instructions yet -->
	<xsl:template match="processing-instruction()" mode="generate">
		<xsl:param name="ndt" as="element()"/>
		<xsl:param name="level" as="xs:integer"/>
	</xsl:template>
	
	<xsl:function name="internal:_startElement">
		<xsl:param name="element" as="element()"/>
		<xsl:param name="ndt" as="element()"/>
		<xsl:variable name="case" select="strc:getCase($element,$ndt)"/>
		<xsl:text>&startTag;</xsl:text><xsl:value-of select="ns:getQualifiedNodeName($ndt,$element)"/>
		<xsl:if test="strc:hasHeader($case)">
			<xsl:if test="strc:hasNamespaces($case)">
				<xsl:for-each select="ns:getNamespaces($ndt,$element)">
					<xsl:value-of select="internal:_formatNamespaceDeclaration($element,.)"/>
				</xsl:for-each>
			</xsl:if>
			<xsl:if test="strc:hasAttributes($case)">
				<xsl:for-each select="$element/@*">
					<xsl:value-of select="internal:_formatAttribute(.,$ndt)"/>
				</xsl:for-each>
			</xsl:if>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="strc:hasBody($case)">
				<xsl:text>&endTag;</xsl:text> 
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>/&endTag;</xsl:text> 
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="internal:_formatAttribute" as="xs:string">
		<xsl:param name="attr" as="attribute()"/>
		<xsl:param name="ndt" as="element()"/>
		<xsl:value-of select="concat(' ',format:formatAttribute(ns:getQualifiedNodeName($ndt,$attr),data($attr)))"/>
	</xsl:function>
	
	<xsl:function name="internal:_formatNamespaceDeclaration" as="xs:string">
		<xsl:param name="element" as="element()"/>
		<xsl:param name="ns" as="element()"/>
		<xsl:variable name="format">
			<xsl:variable name="prefix" select="ns:getNamespaceName($ns)"/>
			<xsl:variable name="uri" select="ns:getNamespaceURI($ns)"/>
			<xsl:value-of select="format:formatNamespaceDeclaration($element,$prefix,$uri)"/>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="fn:string-length($format)>0">
				<xsl:value-of select="fn:concat(' ',$format)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$format"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="internal:_endElement">
		<xsl:param name="element"/>
		<xsl:param name="ndt"/>
		<xsl:if test="strc:hasBody(strc:getCase($element,$ndt))">
			<xsl:text>&startTag;/</xsl:text><xsl:value-of select="ns:getQualifiedNodeName($ndt,$element)"/><xsl:text>&endTag;</xsl:text> 
		</xsl:if>
	</xsl:function>

</xsl:stylesheet>