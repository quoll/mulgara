<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  version="1.0"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:lxslt="http://xml.apache.org/xslt"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mulgaraDescriptor="mulgaraDescriptor"
  extension-element-prefixes="mulgaraDescriptor"
  exclude-result-prefixes="xsl rdf rdfs lxslt mulgaraAnswer xalan ns1"
  xmlns:mulgaraAnswer="http://mulgara.org/tql#"
  xmlns:desc="http://mulgara.org/descriptor#"
  xmlns:ns1="urn:Query">

  <!-- ============================================== -->
  <!-- Assemble the parameters as used in the query  -->
  <!-- ============================================== -->
  <!-- DESCRIPTOR PARAMS -->
  <xsl:param name="_usage"/> <!-- is set return the USAGE RDF -->
  <xsl:param name="_self"/>

  <!-- USER PARAMS -->
  <xsl:param name="model"/>
  <xsl:param name="descriptorBase"/>

  <!-- ============================================== -->
  <!-- Match the Solution -->
  <!-- ============================================== -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$_usage">
        <xsl:call-template name="usage"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- get the list of descriptors as XML  -->
        <xsl:variable name="answer">
          <mulgaraDescriptor:descriptor
            _target="allDescriptorsDescriptor.xsl"
            _source="{$_self}"
            model="{$model}"/>
          <xsl:apply-templates/>
        </xsl:variable>
        <!-- Now apply the templates to the answer -->
        <!--
        <xsl:apply-templates select="$answer/*"/> 
        <xsl:copy-of select="xalan:nodeset($answer)/*"/>-->   
     
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- Matches the descriptor group                                         -->
  <!-- #################################################################### -->
  <xsl:template match="desc:descriptors">
    <html>
    <head>
      <title>Descriptor List</title>
    <link media="all" href="/all.css" type="text/css" title="Burnt" rel="stylesheet"/>
    <link media="screen" href="/burnt.css" type="text/css" title="Burnt" rel="stylesheet"/>
    <link href="/grey.css" title="Grey" media="screen" type="text/css" rel="alternate stylesheet"/>
    </head>

    <body>
    <div id="container">

    <!-- Banner -->
    <div id="banner">
      <h1>mulgara.sourceforge.net</h1>
    </div>

    <div id="content">
    <div id="breadcrumb">
      [ Location:
      <ul>
        <li> <a href="/webservices/descriptor/" title="descriptors">descriptors</a> </li>
          <ul>
            <li>list descriptors</li>
          </ul>
      </ul>  ]
    </div>  

    <!-- first get the title -->
    <h1>Descriptor List</h1>
    <table border="1">
      <tr>
        <td>
          <xsl:apply-templates/>
        </td>
      </tr>
    </table>

    </div></div>
    </body>
    </html>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- Matches a single descriptor                                          -->
  <!-- #################################################################### -->
  <xsl:template match="desc:descriptor">
    <div>
    <xsl:apply-templates/>
    </div>
    <hr/>
  </xsl:template>

  
  <!-- #################################################################### -->
  <!--  Matches the descriptor URL                                          -->
  <!-- #################################################################### -->
  <xsl:template match="desc:descriptorURL">

      <!-- dc:title -->
      <b><xsl:value-of select="../desc:descriptorTitle/text()"/></b>
      <br/>
      URL <a href="{./text()}" target="descriptor-source"><xsl:value-of select="./text()"/></a>

     <form action="execute">
        
        <!-- _self -->
        <!-- strip 
        
        _self 
        http://localhost:8080/webservices/descriptors/default/descriptorListHTML.xsl

        to 
        http://localhost:8080/webservices/descriptors/

        -->

        <input type="hidden" name="_self" value='{ concat($descriptorBase ,"default/descriptorHTML.xsl")}'/>

        <!-- url -->
        <input type="hidden" name="url" value="{./text()}"/>

        <!-- model -->
        <input type="hidden" name="model" value="{$model}"/> 

        <input type="submit" value="Usage"/>
     </form>


    <xsl:apply-templates/>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- Default catch all template                                           -->
  <!-- #################################################################### -->
  <xsl:template match="*/text()">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- Calls a java class for queries -->
  <!-- #################################################################### -->
  <lxslt:component prefix="mulgaraDescriptor" elements="descriptor query debug" functions="test">
    <lxslt:script lang="javaclass" src="xalan://org.mulgara.descriptor.DescriptorElement"/>
  </lxslt:component>


  <!-- #################################################################### -->
  <!-- USAGE of this descriptor                                             -->
  <!-- #################################################################### -->
  <xsl:template name="usage">
    <rdf:RDF
      xml:lang="en"
      xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
      xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
      xmlns:dc="http://purl.org/dc/elements/1.1/"
      xmlns:desc="http://mulgara.org/descriptor#">

      <!-- Descriptor metadata -->
      <desc:Descriptor rdf:about="">

        <dc:title>Lists the descriptors available in a descriptor Model as HTML</dc:title>

        <desc:hasParam>
          <desc:Param>
            <desc:name>_self</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URL of this Descriptor</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <desc:hasParam>
          <desc:Param>
            <desc:name>model</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URI of the model to query</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <desc:hasParam>
          <desc:Param>
            <desc:name>descriptorBase</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The Base URL where default descriptors are found 
              e.g. http://localhost:8080/webservices/descriptor/descriptors
              This must be the prefix used by the descriptors themselves in their RDF about attribute.

            </desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>
        <!-- MIME TYPE -->
        <desc:hasMimetype>
          <desc:Mimetype>
            <desc:mime-major>text</desc:mime-major>
            <desc:mime-minor>html</desc:mime-minor>
          </desc:Mimetype>
        </desc:hasMimetype>
      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
