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

  <!-- ============================================== -->
  <!-- Assemble the parameters -->
  <!-- ============================================== -->

  <!-- DESCRIPTOR PARAMS -->
  <xsl:param name="_usage"/> <!-- if set return the USAGE RDF -->
  <xsl:param name="_self"/> <!-- the URL of this descriptor -->

  <!-- USER PARAMS -->
  
<xsl:param name="model"/>
<xsl:param name="uri"/>

<xsl:output
  omit-xml-declaration="yes"
  method="text"
  encoding="us-ascii"/>

  <!-- ============================================== -->
  <!-- Match the Solution -->
  <!-- ============================================== -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$_usage">
        <xsl:call-template name="usage"/>
      </xsl:when>
      <xsl:otherwise>

        <!-- get the VCard as XML -->
        <xsl:variable name="answer">
         <mulgaraDescriptor:descriptor
           _target="vcardxml.xsl"
           _source="{$_self}"
           uri="{$uri}"
           model="{$model}"/>
        </xsl:variable>
       
        <!-- Now apply the templates to the answer -->
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>

        <!--
        <xmp>
        <xsl:copy-of select="xalan:nodeset($answer)/*"/>
        </xmp>
        -->

      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- converts models into a HTML List Element  -->
  <!-- #################################################################### -->
  <xsl:template match="desc:vcards">
      <xsl:apply-templates/>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- converts vcard into a HTML Table  -->
  <!-- #################################################################### -->
  <xsl:template match="desc:vcard">
BEGIN:VCARD    
VERSION:2.1
      <xsl:apply-templates/>
END:VCARD    
  </xsl:template>


  <!-- #################################################################### -->
  <!-- TITLE  -->
  <!-- #################################################################### -->
  <xsl:template match="TITLE">
TITLE:<xsl:value-of select="text()"/>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- ROLE  -->
  <!-- #################################################################### -->
  <xsl:template match="ROLE">
ROLE:<xsl:value-of select="text()"/>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- TEL  -->
  <!-- #################################################################### -->
  <xsl:template match="TEL">
TEL;WORK;VOICE:<xsl:value-of select="text()"/>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- EMAIL  -->
  <!-- #################################################################### -->
  <xsl:template match="EMAIL">
EMAIL;PREF;INTERNET:<xsl:value-of select="text()"/>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- ADR  -->
  <!-- #################################################################### -->
  <xsl:template match="ADR">
ADR;WORK:;<xsl:value-of select="Street/text()"/>;<xsl:value-of select="Locality/text()"/>;<xsl:value-of select="Pcode/text()"/>;<xsl:value-of select="Country/text()"/>
LABEL;WORK;ENCODING=QUOTED-PRINTABLE:<xsl:value-of select="Street/text()"/>=0D=0A<xsl:value-of select="Locality/text()"/>=0D=0A<xsl:value-of select="Pcode/text()"/><xsl:value-of select="Country/text()"/>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- NOTE  -->
  <!-- #################################################################### -->
  <xsl:template match="NOTE">
NOTE:<xsl:value-of select="text()"/>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- FN  -->
  <!-- #################################################################### -->
  <xsl:template match="FN">
FN:<xsl:value-of select="text()"/>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- N  -->
  <!-- #################################################################### -->
  <xsl:template match="N">
N:<xsl:value-of select="text()"/>
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

        <dc:title>Given a VCard URI return a XML version of the VCard</dc:title>

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
<desc:name>model</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>model where VCards are located</desc:description>
</desc:Param>
</desc:hasParam> 
        
        <!-- Parameter 2 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#" xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>uri</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>URI of VCard</desc:description>
</desc:Param>
</desc:hasParam> 

        <!-- MIME TYPE -->
        
<desc:hasMimetype xmlns:desc="http://mulgara.org/descriptor#" xmlns="http://www.w3.org/1999/xhtml">
<desc:Mimetype>
<desc:mime-major>text</desc:mime-major>
<desc:mime-minor>x-vcard</desc:mime-minor>
</desc:Mimetype>
</desc:hasMimetype>
        
      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
    
  

