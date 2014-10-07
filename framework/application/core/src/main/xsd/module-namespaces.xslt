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
      Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
      Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#

-->
<xsl:stylesheet 
	version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema" 
	xmlns:fn="http://www.w3.org/2005/xpath-functions" 
	xmlns:ns="urn:xmltidy:lib:namespaces"
	xmlns:internal="urn:xmltidy:lib:namespaces:internal">

	<xsl:function name="ns:createNamespaceDefinitionTable" as="element()">
		<xsl:param name="root" as="node()"/>
		<xsl:variable name="table"  select="internal:_findImplicitNamespaces($root)"/>
		<xsl:variable name="global" select="internal:_getGlobalNamespaces($table)"/>
		<xsl:variable name="local"  select="internal:_getLocalNamespaces($table,$global)"/>
		<ns:ndt>
			<xsl:copy-of select="$global" copy-namespaces="no"/>
			<xsl:copy-of select="$local"  copy-namespaces="no"/>
		</ns:ndt>
	</xsl:function>

	<xsl:function name="ns:hasNamespaces" as="xs:boolean">
		<xsl:param name="ndt" as="element()"/>
		<xsl:param name="element" as="node()"/>
		<xsl:value-of select="fn:exists(ns:getNamespaces($ndt,$element))"/>
	</xsl:function>

	<xsl:function name="ns:getNamespaces" as="element()*">
		<xsl:param name="ndt" as="element()"/>
		<xsl:param name="element" as="element()"/>
		<xsl:choose>
			<xsl:when test="fn:count($element/ancestor-or-self::*)=1">
				<xsl:copy-of select="$ndt//ns:global/ns:namespace"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="$ndt//ns:context[@id=fn:generate-id($element)]/ns:namespace"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="ns:getNamespaceURI" as="xs:string">
		<xsl:param name="ns" as="element()"/>
		<xsl:value-of select="$ns/@name"/>
	</xsl:function>

	<xsl:function name="ns:getNamespaceName" as="xs:string">
		<xsl:param name="ns" as="element()"/>
		<xsl:value-of select="$ns/@prefix"/>
	</xsl:function>

	<xsl:function name="ns:getQualifiedNodeName">
		<xsl:param name="ndt" as="node()" />
		<xsl:param name="element" as="node()" />
		<xsl:variable name="prefix" select="internal:_getNamespacePrefix($ndt,$element)"></xsl:variable>
		<xsl:choose>
			<xsl:when test="string-length($prefix)>0">
				<xsl:value-of select="fn:concat($prefix,':',fn:local-name($element))" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="fn:local-name($element)" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="internal:_getNamespacePrefix" as="xs:string">
		<xsl:param name="ndt" as="node()"/>
		<xsl:param name="element" as="node()"/>
		<xsl:variable name="globalNamespaces" select="$ndt//ns:global/ns:namespace[@name=namespace-uri($element)]"/>
		<xsl:choose>
			<xsl:when test="fn:count($globalNamespaces)=1">
				<xsl:value-of select="$globalNamespaces[1]/@prefix"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="localNamespaces" select="$ndt//ns:context[@id=generate-id($element)]/ns:namespace[@name=namespace-uri($element)]"/>
				<xsl:choose>
					<xsl:when test="fn:count($localNamespaces)=1">
						<xsl:value-of select="$globalNamespaces[1]/@prefix"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="fn:substring-before(name($element),':')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:function name="internal:_getGlobalNamespaces" as="element()">
		<xsl:param name="raw" as="element()"/>
		<ns:global>
			<xsl:for-each-group select="$raw//ns:usage" group-by="@namespace">
				<xsl:if test="fn:count(fn:distinct-values(fn:current-group()/@prefix))=1">
					<xsl:variable name="prefix" select="fn:current-group()[1]/@prefix"/>
					<xsl:if test="fn:count(fn:distinct-values($raw//ns:usage[@prefix=$prefix]/@namespace))=1">
						<xsl:comment>Top</xsl:comment>
						<ns:namespace name="{fn:current-grouping-key()}" prefix="{$prefix}">
							<xsl:copy-of select="fn:current-group()" copy-namespaces="no"/>
						</ns:namespace>
					</xsl:if>
				</xsl:if>
				<xsl:if test="fn:count(fn:distinct-values(fn:current-group()/@prefix))>1">
					<xsl:choose>
						<xsl:when test="fn:count(fn:distinct-values(fn:current-group()[@type='data']/@prefix))=0">
							<xsl:variable name="usage" select="fn:current-group()[1]"/>
							<xsl:comment>Translatable: <xsl:value-of select="$usage/@prefix"/> (<xsl:value-of select="fn:distinct-values(fn:current-group()/@prefix)"/>) </xsl:comment>
							<ns:namespace name="{fn:current-grouping-key()}" prefix="{$usage/@prefix}">
								<xsl:copy-of select="fn:current-group()" copy-namespaces="no"/>
							</ns:namespace>
						</xsl:when>
						<xsl:when test="fn:count(fn:distinct-values(fn:current-group()[@type='data']/@prefix))=1">
							<xsl:variable name="usage" select="fn:current-group()[@type='data'][1]" as="node()"/>
							<xsl:variable name="rest" select="fn:current-group()[@type='data']" as="node()*"/>
							<xsl:comment>Translatable: <xsl:value-of select="$usage/@prefix"/> (<xsl:value-of select="fn:distinct-values(fn:current-group()/@prefix)"/>) </xsl:comment>
							<ns:namespace name="{fn:current-grouping-key()}" prefix="{$usage/@prefix}">
								<xsl:copy-of select="$usage" copy-namespaces="no"/>
								<xsl:message>current-group-size:<xsl:value-of select="fn:count(fn:current-group())"/></xsl:message>
								<xsl:copy-of select="$rest except ($usage)" copy-namespaces="no"/>
							</ns:namespace>
						</xsl:when>
					</xsl:choose>
				</xsl:if>
			</xsl:for-each-group>
		</ns:global>
	</xsl:function>

	<xsl:function name="internal:_getLocalNamespaces" as="element()">
		<xsl:param name="raw" as="element()"/>
		<xsl:param name="global" as="element()"/>
		<ns:local>
			<xsl:for-each-group select="$raw//ns:usage" group-by="@context">
				<ns:context id="{fn:current-grouping-key()}">
					<xsl:for-each select="fn:distinct-values(fn:current-group()/@namespace)">
						<xsl:variable name="targetNS" select="."/>
						<xsl:if test="fn:not(fn:exists($global//ns:namespace[@name=$targetNS]))">
							<xsl:for-each select="fn:distinct-values(fn:current-group()[@namespace=$targetNS]/@prefix)">
								<xsl:variable name="prefix" select="."/>
								<ns:namespace name="{$targetNS}" prefix="{$prefix}">
									<xsl:copy-of select="fn:current-group()[@namespace=$targetNS and @prefix=$prefix]" copy-namespaces="no"/>
								</ns:namespace>
							</xsl:for-each>
						</xsl:if>
					</xsl:for-each>
				</ns:context>
			</xsl:for-each-group>
		</ns:local>
	</xsl:function>

	<xsl:function name="internal:_findImplicitNamespaces" as="element()">
		<xsl:param name="root" as="node()"/>
		<ns:usages>
			<xsl:for-each select="$root//*">
				<xsl:variable name="item" select="."/>
				<!-- Namespaces used for element definitions -->
				<xsl:copy-of select="internal:_findElementNamespace($item)" copy-namespaces="no"/>
				<xsl:for-each select="./@*">
					<!-- Namespaces used for attribute definitions -->
					<xsl:copy-of select="internal:_findAttributeNamespace(.,$item)" copy-namespaces="no"/>
					<!-- Namespaces used in attribute values -->
					<xsl:copy-of select="internal:_findDataNamespace(fn:data(.),$item)" copy-namespaces="no"/>
				</xsl:for-each>
				<!-- Namespaces used in element values -->
				<xsl:if test="fn:count(text())=1 and fn:count(child::*)=0">
					<xsl:copy-of select="internal:_findDataNamespace(text(),$item)" copy-namespaces="no"/>
				</xsl:if>
			</xsl:for-each>
		</ns:usages>
	</xsl:function>

	<xsl:function name="internal:_findElementNamespace" as="element()?">
		<xsl:param name="item" as="node()" />
		<xsl:if test="fn:string-length(xs:string(fn:namespace-uri($item)))>0">
			<ns:usage type="element" prefix="{fn:substring-before(fn:name($item),':')}" namespace="{fn:namespace-uri($item)}" id="{fn:generate-id($item)}" context="{fn:generate-id($item)}">
				<xsl:value-of select="fn:name($item)" />
			</ns:usage>
		</xsl:if>
	</xsl:function>

	<xsl:function name="internal:_findAttributeNamespace" as="element()?">
		<xsl:param name="item" as="node()"/>
		<xsl:param name="context" as="element()"/>
		<xsl:if test="fn:string-length(xs:string(fn:namespace-uri($item)))>0">
			<ns:usage type="attribute" prefix="{fn:substring-before(fn:name($item),':')}" namespace="{fn:namespace-uri($item)}" id="{fn:generate-id($item)}" context="{fn:generate-id($context)}">
				<xsl:value-of select="fn:name($item)"/>
			</ns:usage>
		</xsl:if>
	</xsl:function>

	<xsl:function name="internal:_findDataNamespace" as="element()?">
		<xsl:param name="value" as="xs:string"/>
		<xsl:param name="node" as="node()"/>
		<xsl:if test="fn:count($value)=1">
			<xsl:choose>
				<xsl:when test="fn:not(fn:contains($value,':'))">
					<xsl:if test="fn:matches($value,'[\i-[:]][\c-[:]]')">
						<ns:usage type="data" prefix="" namespace="{fn:namespace-uri-for-prefix('',$node)}" id="" context="{fn:generate-id($node)}">
							<xsl:value-of select="$value"/>
						</ns:usage>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="tokens" select="fn:tokenize($value,':')"/>
					<xsl:if test="fn:count($tokens)=2">
						<xsl:variable name="prefix" select="$tokens[1]"/>
						<xsl:variable name="localPart" select="$tokens[2]"/>
						<xsl:if test="fn:matches($prefix,'[\i-[:]][\c-[:]]')">
							<xsl:if test="fn:matches($localPart,'[\i-[:]][\c-[:]]')">
								<xsl:variable name="namespaces" select="fn:namespace-uri-for-prefix($prefix,$node)"/>
								<xsl:if test="fn:count($namespaces)>0">
									<ns:usage type="data" prefix="{$prefix}" namespace="{$namespaces[1]}" id="" context="{fn:generate-id($node)}">
										<xsl:value-of select="$value"/>
									</ns:usage>
								</xsl:if>
							</xsl:if>
						</xsl:if>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:function>

</xsl:stylesheet>