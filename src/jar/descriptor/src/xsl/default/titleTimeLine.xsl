<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  version="1.0"
  xmlns="http://www.w3.org/1999/xhtml"
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
  <xsl:param name="_self"/>  <!-- this descriptor -->

  <!-- USER PARAMS -->
  <xsl:param name="month" select="'November'"/>
  <xsl:param name="year" select="'2000'"/>
  <xsl:param name="parentController"/>
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
        <html>
          <xsl:variable name="titles">
         <mulgaraDescriptor:descriptor
           _target="titlesFromDates.xsl"
           _source="{$_self}"
           month="{$month}"
           year="{$year}"
           parentController="{$parentController}"
           model="{$model}"/>
        </xsl:variable>
        <!-- Now apply the templates to the answer -->
        <xsl:apply-templates select="xalan:nodeset($titles)/*"/>
      </html>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- match component -->
  <!-- #################################################################### -->
  <xsl:template match="desc:component">
    <html>
      <head>
      <title>Calendar View</title>
      </head>
      <body>
      <xsl:apply-templates/>
     </body>
    </html>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- show controller -->
  <!-- #################################################################### -->
  <xsl:template match="desc:controller">
    <!-- show controller -->
    CONTROLLER
    <xsl:copy-of select="./*"/>
      <xsl:apply-templates/>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- show the results -->
  <!-- #################################################################### -->
  <xsl:template match="desc:content">
    CONTENT
    <xsl:copy-of select="./*"/>
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

      <desc:Descriptor rdf:about="">

        <dc:title>Displays titles of documents from a certain date</dc:title>

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
            <desc:name>month</desc:name>
            <desc:type>String</desc:type>
            <desc:description>Month to search for</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <desc:hasParam>
          <desc:Param>
            <desc:name>year</desc:name>
            <desc:type>String</desc:type>
            <desc:description>Year to search for</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <desc:hasParam>
          <desc:Param>
            <desc:name>parentController</desc:name>
            <desc:type>String</desc:type>
            <desc:description>the URL of the descriptor to call upon submitting a form element from a view generated by this descriptor</desc:description>
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
