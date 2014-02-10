<?xml version="1.0" encoding="UTF-8"?>


<!DOCTYPE rdf:RDF [
  <!ENTITY dc           "http://purl.org/dc/elements/1.1/">
  <!ENTITY desc         "http://mulgara.org/descriptor#">
  <!ENTITY rdf          "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
  <!ENTITY rdfs         "http://www.w3.org/2000/01/rdf-schema#">
]>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="&rdf;"
  xmlns:rdfs="&rdfs;"
  xmlns:dc="&dc;"
  xmlns:lxslt="http://xml.apache.org/xslt"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mulgaraDescriptor="mulgaraDescriptor"
  extension-element-prefixes="mulgaraDescriptor"
  exclude-result-prefixes="xsl rdf rdfs lxslt mulgaraAnswer xalan ns1"
  xmlns:mulgaraAnswer="http://mulgara.org/tql#"
  xmlns:desc="&desc;"
  xmlns:ns1="urn:Query">

  <!-- ============================================== -->
  <!-- Assemble the parameters -->
  <!-- ============================================== -->

  <!-- DESCRIPTOR PARAMS -->
  <xsl:param name="_usage"/> <!-- if set return the USAGE RDF -->
  <xsl:param name="_self"/> <!-- the URL of this descriptor -->

  <!-- USER PARAMS -->
  
<xsl:param name="server"/>

  
  <!-- ============================================== -->
  <!-- Match the Solution -->
  <!-- ============================================== -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$_usage">
        <xsl:call-template name="usage"/>
      </xsl:when>
      <xsl:otherwise>
  
        <!-- store query answer in a variable called answer -->
        <xsl:variable name="answer">
          <!-- Query for list of models on server -->
          <mulgaraDescriptor:query server="{$server}">
            <![CDATA[
            select $model from <@@server@@#> where 
            $model <http://www.w3.org/1999/02/22-rdf-syntax-ns#type><http://mulgara.org/mulgara#Model>;
            ]]>
          </mulgaraDescriptor:query>
        </xsl:variable>

        <!-- Now apply the templates to the answer -->
        <!--
        <xsl:copy-of select="xalan:nodeset($answer)/*"/>
        -->
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>

      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- converts models into a HTML List Element  -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:answer">
    <html>
      <head>
        <title>Mulgara models</title>
      </head>
      <body>

        List of models on server <xsl:value-of select="$server"/>
        <ol>
          <xsl:apply-templates/>
        </ol>
        
      </body>
    </html>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- converts solution into a HTML List Element  -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:solution">
    <li><xsl:value-of select="mulgaraAnswer:model/@resource"/></li>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- Calls a java class for Mulgara queries -->
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
      xmlns:rdf="&rdf;"
      xmlns:rdfs="&rdfs;"
      xmlns:dc="&dc;"
      xmlns:desc="&desc;">

      <desc:Descriptor rdf:about="">

        <dc:title>Lists models in a Mulgara as HTML</dc:title>

        <desc:hasParam>
          <desc:Param>
            <desc:name>_self</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URL of this Descriptor</desc:description>
          </desc:Param>
        </desc:hasParam>

         
        
        <!-- Parameter 1 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#" xmlns="http://www.w3.org/1999/xhtml">
          <desc:Param>
          <desc:name>server</desc:name>
          <desc:type>String</desc:type>
          <desc:required>Yes</desc:required>
          <desc:description>The server to get a model list from</desc:description>
          </desc:Param>
        </desc:hasParam> 
        
        <!-- MIME TYPE -->
        
        <desc:hasMimetype xmlns:desc="http://mulgara.org/descriptor#" xmlns="http://www.w3.org/1999/xhtml">
          <desc:Mimetype>
          <desc:mime-major>text</desc:mime-major>
          <desc:mime-minor>html</desc:mime-minor>
          </desc:Mimetype>
        </desc:hasMimetype>
        
      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
    
  

