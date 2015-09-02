<#--

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
      Artifact    : org.ldp4j:ldpj4-dist:0.1.0
      Bundle      : ldpj4-dist-0.1.0.jar
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#

-->
<#-- To render the third-party file.
Available context :
- dependencyMap a collection of Map.Entry with
  key are dependencies (as a MavenProject) (from the maven project)
  values are licenses of each dependency (array of string)

- licenseMap a collection of Map.Entry with
  key are licenses of each dependency (array of string)
  values are all dependencies using this license
-->
<#function licenseFormat licenses>
	<#assign result = ""/>
	<#list licenses as license>
		<#if result?length==0>
			<#assign result = license/>
		<#else>
			<#assign result = result + ", " +license/>
		</#if>
	</#list>
	<#assign result = result + "."/>
	<#return result>
</#function>

<#function artifactFormat p>
	<#if p.name?index_of('Unnamed') &gt; -1>
		<#return "(" + p.groupId + ":" + p.artifactId + ":" + p.version + ")">
	<#else>
		<#return p.name + " (" + p.groupId + ":" + p.artifactId + ":" + p.version + ")">
	</#if>
</#function>

<#assign version="${project.version}"/>
<#assign header="-- LDP4j "+version+" --"/>
<#assign underscore = ""/>
<#list 1..header?length as i>
	<#assign underscore = underscore + "-"/>
</#list>
${underscore}
${header}
${underscore}

<#if dependencyMap?size == 0>
The software does not include any third-party libraries.
<#else>
The software includes the following third-party libraries:

	<#list dependencyMap as e>
		<#assign project = e.getKey()/>
		<#assign licenses = e.getValue()/>
	- ${artifactFormat(project)}, licensed under:
		<#list licenses as license>
		+ ${license}
		</#list>

	</#list>
</#if>

Copies of the abovementioned licenses can be found in the *license* directory of
this distribution.