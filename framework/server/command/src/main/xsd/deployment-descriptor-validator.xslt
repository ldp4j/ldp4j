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
	xmlns:ldp4j="http://www.ldp4j.org/schemas/deployment-descriptor/1.0" 
	xmlns:aux="http://www.ldp4j.org/schemas/deployment-descriptor-validator/1.0">

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" exclude-result-prefixes="fn xs aux"/>

	<xsl:template match="ldp4j:ldp-app">
		<ldp4j:validation>

			<xsl:call-template name="validate-template-id-uniqueness">
				<xsl:with-param name="failure">reused by multiple resource template definitions</xsl:with-param>
				<xsl:with-param name="templates" select="ldp4j:templates/ldp4j:resource"/>
			</xsl:call-template>

			<xsl:call-template name="validate-template-id-uniqueness">
				<xsl:with-param name="failure">reused by multiple container template definitions</xsl:with-param>
				<xsl:with-param name="templates" select="ldp4j:templates/ldp4j:container"/>
			</xsl:call-template>

			<xsl:call-template name="validate-template-id-uniqueness">
				<xsl:with-param name="failure">reused by multiple resource and container template definitions</xsl:with-param>
				<xsl:with-param name="templates" select="ldp4j:templates/*"/>
			</xsl:call-template>

			<xsl:for-each select="ldp4j:templates//ldp4j:attached-resource[empty(@predicate)]">
				<xsl:variable name="ref" select="@ref"/>
				<xsl:if test="not(aux:isMembershipAwareContainerTemplate(../../ldp4j:container[@ldp4j:id=$ref]))">
					<ldp4j:failure code="2">Resource attached at path '<xsl:value-of select="@path"/>' of template '<xsl:value-of select="../@ldp4j:id"/>' does not refer to a membership-aware container template (<xsl:value-of select="$ref"/>).</ldp4j:failure>
				</xsl:if>
			</xsl:for-each>

			<xsl:for-each-group select="ldp4j:endpoints/ldp4j:endpoint" group-by="ldp4j:target-template/@ref">
				<xsl:variable name="template" select="current-grouping-key()"/>
				<xsl:for-each-group select="current-group()" group-by="aux:qualifyName(ldp4j:target-name)">
					<xsl:variable name="endpoints" select="aux:join(current-group()/@ldp4j:id)"/>
					<xsl:if test="count(current-group()) &gt; 1">
						<ldp4j:failure code="3">Target resource name '<xsl:value-of select="current-grouping-key()"/>' for template '<xsl:value-of select="$template"/>' is published by multiple endpoints (<xsl:value-of select="$endpoints"/>).</ldp4j:failure>
					</xsl:if>
				</xsl:for-each-group>
			</xsl:for-each-group>

		</ldp4j:validation>
	</xsl:template>

	<xsl:template name="validate-template-id-uniqueness" as="element()*">
		<xsl:param name="failure" as="xs:string" required="yes"/>
		<xsl:param name="templates" as="element()*" required="yes"/>
		<xsl:for-each-group select="$templates" group-by="@ldp4j:id">
			<xsl:if test="count(current-group()) &gt; 1">
				<ldp4j:failure code="1">The identifier '<xsl:value-of select="current-grouping-key()"/>' is <xsl:value-of select="$failure"/>.</ldp4j:failure>
			</xsl:if>
		</xsl:for-each-group>
	</xsl:template>

	<xsl:function name="aux:isMembershipAwareContainerTemplate" as="xs:boolean">
		<xsl:param name="template" as="element()"/>
		<xsl:sequence select="not(empty($template/ldp4j:membership-predicate) and empty($template/ldp4j:membership-relation) and empty($template/ldp4j:inserted-content-relation))"/>
	</xsl:function>
	
	<xsl:function name="aux:join" as="xs:string">
		<xsl:param name="ids" as="item()*"/>
		<xsl:choose>
			<xsl:when test="count($ids)=0">
				<xsl:variable name="empty" as="xs:string"></xsl:variable>
				<xsl:sequence select="$empty"/>
			</xsl:when>
			<xsl:when test="count($ids)=1">
				<xsl:sequence select="data($ids)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="head" select="subsequence($ids,1,1)"/>
				<xsl:variable name="rest" select="subsequence($ids,2)"/>
				<xsl:sequence select="aux:joinAux($head,$rest,'','')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="aux:joinAux" as="xs:string">
		<xsl:param name="head" as="item()"/>
		<xsl:param name="rest" as="item()*"/>
		<xsl:param name="separator" as="xs:string"/>
		<xsl:param name="result" as="xs:string"/>
		<xsl:variable name="newResult" select="fn:concat($result,$separator,data($head))"/>
		<xsl:choose>
			<xsl:when test="count($rest)=0">
				<xsl:sequence select="$newResult"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="newHead" select="subsequence($rest,1,1)"/>
				<xsl:variable name="newRest" select="subsequence($rest,2)"/>
				<xsl:sequence select="aux:joinAux($newHead,$newRest,', ',$newResult)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	<xsl:function name="aux:qualifyName" as="xs:string">
		<xsl:param name="name" as="item()*"/>
		<xsl:sequence select="fn:concat('{',data($name/@ldp4j:class),'}',$name/text())"/>
	</xsl:function>

</xsl:stylesheet>
