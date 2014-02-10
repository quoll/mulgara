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
          <!-- Query for list of VCards in server -->
          <mulgaraDescriptor:query model="{$model}">
            <![CDATA[
            select $vcard from <@@model@@> where $vcard <http://www.w3.org/2001/vcard-rdf/3.0#FN> $o;
              ;
            ]]>
          </mulgaraDescriptor:query>
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
  <xsl:template match="mulgaraAnswer:answer">
    <desc:vcards>
    <xsl:apply-templates/>
  </desc:vcards>
        
  </xsl:template>


  <!-- #################################################################### -->
  <!-- converts solution into a HTML List Element  -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:solution">
    <desc:vcard resource="{mulgaraAnswer:vcard/@resource}">
    </desc:vcard>
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

        <dc:title>Given a Model return a list of VCards URIs in that model as XML</dc:title>

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
<desc:description>Model where VCard RDF is  stored</desc:description>
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
    
  

