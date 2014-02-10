<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:lxslt="http://xml.apache.org/xslt"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mulgaraDescriptor="mulgaraDescriptor"
  extension-element-prefixes="mulgaraDescriptor"
  exclude-result-prefixes="xsl rdf rdfs lxslt mulgaraAnswer xalan ns1"
  xmlns:mulgaraAnswer="http://mulgara.org/tql#"
  xmlns:desc="http://mulgara.org/descriptor#"
  xmlns:ns1="urn:Query">

  <!-- ============================================== -->
  <!-- Assemble the parameters -->
  <!-- ============================================== -->

  <!-- DESCRIPTOR PARAMS -->
  <xsl:param name="_usage"/> <!-- is set return the USAGE RDF -->
  <xsl:param name="_self"/> <!-- the URL of this descriptor -->

  <!-- USER PARAMS -->
  <xsl:param name="model"/>

  <!-- ============================================== -->
  <!-- Match the Solution -->
  <!-- ============================================== -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$_usage">
        <xsl:call-template name="usage"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- EXECUTE THE QUERY to get all descriptors  -->
        <xsl:variable name="answer">
          <mulgaraDescriptor:query model="{$model}">
            <![CDATA[
            select $descriptorURL $title from <@@model@@> where 
            $descriptorURL <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://mulgara.org/descriptor#Descriptor> and
            $descriptorURL <http://purl.org/dc/elements/1.1/title> $title;
            ]]>
          </mulgaraDescriptor:query>
        </xsl:variable>
        <!-- Now apply the templates to the answer -->
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- Descriptor  property element -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:answer">
    <desc:descriptors>
    <xsl:apply-templates/>
    </desc:descriptors>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- converts companies to nice XML format -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:solution">
      <xsl:if test="mulgaraAnswer:descriptorURL">
        <desc:descriptor>
          <desc:descriptorTitle><xsl:value-of select="mulgaraAnswer:title/text()"/></desc:descriptorTitle>
          <desc:descriptorURL><xsl:value-of select="mulgaraAnswer:descriptorURL/@resource"/></desc:descriptorURL>
        </desc:descriptor>
      </xsl:if>
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

      <desc:Descriptor rdf:about="">

        <dc:title>Gets list of Descriptors from a store</dc:title>

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
            <desc:description>The URI of the model to query, normally in the #descriptors model</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- MIME TYPE -->
        <desc:hasMimetype>
          <desc:Mimetype>
            <desc:mime-major>text</desc:mime-major>
            <desc:mime-minor>xml</desc:mime-minor>
          </desc:Mimetype>
        </desc:hasMimetype>

      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
