<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:lxslt="http://xml.apache.org/xslt" xmlns:xalan="http://xml.apache.org/xalan" xmlns:mulgaraDescriptor="mulgaraDescriptor" xmlns:mulgaraAnswer="http://mulgara.org/tql#" xmlns:desc="http://mulgara.org/descriptor#" xmlns:ns1="urn:Query" version="1.0" extension-element-prefixes="mulgaraDescriptor" exclude-result-prefixes="xsl rdf rdfs lxslt mulgaraAnswer xalan ns1">
<!-- ============================================== -->
<!-- Assemble the parameters -->
<!-- ============================================== -->
<!-- DESCRIPTOR PARAMS -->
  <xsl:param name="_usage"/>
<!-- if set return the USAGE RDF -->
  <xsl:param name="_self"/>
<!-- the URL of this descriptor -->
<!-- USER PARAMS -->
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
        <!-- create a temp model -->
        <xsl:variable name="createModelAnswer">
          <mulgaraDescriptor:query model="{$model}" secondWord="{$secondWord}"><![CDATA[ create <@@model@@TEMP> ; ]]></mulgaraDescriptor:query>
        </xsl:variable>
        <!-- insert the second word into the database -->
        <xsl:variable name="insertAnswer">
          <mulgaraDescriptor:query model="{$model}" secondWord="{$secondWord}"><![CDATA[ insert <urn:temp#Subject> <urn:word#second> '@@secondWord@@' into <@@model@@TEMP> ; ]]></mulgaraDescriptor:query>
        </xsl:variable>
        <!-- query it out again -->
        <xsl:variable name="queryAnswer">
          <mulgaraDescriptor:query model="{$model}" secondWord="{$secondWord}"><![CDATA[ 
            select $secondWord from <@@model@@TEMP> where 
              <urn:temp#Subject> <urn:word#second> $secondWord; 
            ]]></mulgaraDescriptor:query>
        </xsl:variable>
        <!-- delete the temp model -->
        <xsl:variable name="deleteModelAnswer">
          <mulgaraDescriptor:query model="{$model}" secondWord="{$secondWord}"><![CDATA[ drop <@@model@@TEMP> ; ]]></mulgaraDescriptor:query>
        </xsl:variable>
        <!-- process the query answer -->
        <xsl:apply-templates select="xalan:nodeset($queryAnswer)/*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
<!-- #################################################################### -->
<!-- converts solution into a 'Word' tag -->
<!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:solution">
    <desc:Word><xsl:copy-of select="mulgaraAnswer:secondWord/text()"/></desc:Word>
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
    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:desc="http://mulgara.org/descriptor#" xml:lang="en">
      <desc:Descriptor rdf:about="">
        <dc:title>Unit Test: Hello World Part 2</dc:title>
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
            <desc:name>secondWord</desc:name>
            <desc:type>String</desc:type>
            <desc:required>Yes</desc:required>
            <desc:description>second word of message</desc:description>
          </desc:Param>
        </desc:hasParam>
<!-- Parameter 2-->
        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#" xmlns="http://www.w3.org/1999/xhtml">
          <desc:Param>
            <desc:name>model</desc:name>
            <desc:type>String</desc:type>
            <desc:required>Yes</desc:required>
            <desc:description>model for descriptors AND temp statement</desc:description>
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
