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

  
  <!-- ============================================== -->
  <!-- Match the Solution -->
  <!-- ============================================== -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$_usage">
        <xsl:call-template name="usage"/>
      </xsl:when>
      <xsl:otherwise>

        <xsl:variable name="answer">
         <mulgaraDescriptor:descriptor
           _target="vcardxml.xsl"
           _source="{$_self}"
           model="{$model}"
           uri="{$uri}"/>
        </xsl:variable>
       
        <!-- Now apply the templates to the answer -->
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>

      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- converts vcard into a HTML Table  -->
  <!-- #################################################################### -->
  <xsl:template match="desc:vcard">
      <table border="0" width="100%">
      <tr>
      <th colspan="2" bgcolor="lightgrey"><xsl:value-of select="FN/text()"/></th>
      </tr>
      <xsl:apply-templates/>
      </table>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- TITLE  -->
  <!-- #################################################################### -->
  <xsl:template match="TITLE">
      <tr>
        <td width="100"><b>Title</b></td>
        <td><xsl:value-of select="text()"/></td>
      </tr>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- ROLE  -->
  <!-- #################################################################### -->
  <xsl:template match="ROLE">
      <tr>
        <td width="100"><b>Role</b></td>
        <td><xsl:value-of select="text()"/></td>
      </tr>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- TEL  -->
  <!-- #################################################################### -->
  <xsl:template match="TEL">
      <tr>
        <td width="100"><b>Telephone</b></td>
        <td><xsl:value-of select="text()"/></td>
      </tr>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- EMAIL  -->
  <!-- #################################################################### -->
  <xsl:template match="EMAIL">
      <tr>
        <td width="100"><b>Email</b></td>
        <td><a href="mailto:{text()}"><xsl:value-of select="text()"/></a></td>
      </tr>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- ADR  -->
  <!-- #################################################################### -->
  <xsl:template match="ADR">
      <tr>
        <td width="100"><b>Address</b></td>
        <td>
          <xsl:value-of select="Street/text()"/><br/>
          <xsl:value-of select="Locality/text()"/><br/>
          <xsl:value-of select="Pcode/text()"/><br/>
          <xsl:value-of select="Country/text()"/>
        </td>
      </tr>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- NOTE  -->
  <!-- #################################################################### -->
  <xsl:template match="NOTE">
      <tr>
        <td width="100"><b>Note</b></td>
        <td><xsl:value-of select="text()"/></td>
      </tr>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- FN  -->
  <!-- #################################################################### -->
  <xsl:template match="FN"/>

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

        <dc:title>Given a VCard URI return a HTML version of the VCard</dc:title>

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
<desc:description>URI of the Vcard to display as HTML</desc:description>
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
    
  

