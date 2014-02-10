<?xml version="1.0" encoding="UTF-8"?>

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

<xsl:output omit-xml-declaration="yes"/>

  <!-- ============================================== -->
  <!-- Assemble the parameters -->
  <!-- ============================================== -->

  <!-- DESCRIPTOR PARAMS -->
  <xsl:param name="_usage"/> <!-- if set return the USAGE RDF -->
  <xsl:param name="_self"/> <!-- the URL of this descriptor -->

  <!-- USER PARAMS -->
  
<xsl:param name="firstWord"/>
<xsl:param name="secondWord"/>
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

        <!-- call the 'world' descriptor pass the model and the second word as a 
             parameter
             -->
        <xsl:variable name="answer">
          <mulgaraDescriptor:descriptor
            _target="world.xsl"
            _source="{$_self}"
            model="{$model}"
            secondWord="{$secondWord}"/>
        </xsl:variable>
        <!-- apply templates to answer -->
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- output the result of descriptor 2                                    -->
  <!-- #################################################################### -->
  <xsl:template match="desc:Word">
    <desc:message><xsl:copy-of select="$firstWord"/><xsl:value-of select="text()"/></desc:message>
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

        <dc:title>Unit Test: Hello World Part 1, Calls Hello World part 2</dc:title>

        <desc:hasParam>
          <desc:Param>
            <desc:name>_self</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URL of this Descriptor</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

         
        
        <!-- Parameter 1 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#" xmlns="http://www.w3.org/1999/xhtml">
          <desc:Param>
          <desc:name>firstWord</desc:name>
          <desc:type>String</desc:type>
          <desc:required>Yes</desc:required>
          <desc:description>First word of message</desc:description>
          </desc:Param>
          </desc:hasParam> 
        
        <!-- Parameter 2 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#" xmlns="http://www.w3.org/1999/xhtml">
          <desc:Param>
          <desc:name>secondWord</desc:name>
          <desc:type>String</desc:type>
          <desc:required>Yes</desc:required>
          <desc:description>Second word of message</desc:description>
          </desc:Param>
          </desc:hasParam> 

        <!-- Parameter 3 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#" xmlns="http://www.w3.org/1999/xhtml">
          <desc:Param>
          <desc:name>model</desc:name>
          <desc:type>String</desc:type>
          <desc:required>Yes</desc:required>
          <desc:description>descriptor model</desc:description>
          </desc:Param>
          </desc:hasParam> 

        <!-- MIME TYPE -->
      <desc:hasMimetype xmlns:desc="http://mulgara.org/descriptor#" xmlns="http://www.w3.org/1999/xhtml">
      <desc:Mimetype>
      <desc:mime-major>text</desc:mime-major>
      <desc:mime-minor>xml</desc:mime-minor>
      </desc:Mimetype>
      </desc:hasMimetype>
        
      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
    
  


