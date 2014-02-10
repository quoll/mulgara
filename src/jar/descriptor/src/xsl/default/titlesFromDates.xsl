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
  <xsl:param name="_self"/>

  <!-- USER PARAMS -->
  <xsl:param name="month"/>
  <xsl:param name="year"/>
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
        <desc:component>
        <!-- EXECUTE THE QUERY to get a person -->
        <xsl:variable name="answer">
          <mulgaraDescriptor:query model="{$model}" month="{$month}" year="{$year}">
            <![CDATA[
            select $title $url from <@@model@@> where $url <http://mulgara.org/mulgara/Document#title> $title and 
            $url <http://mulgara.org/mulgara/Document#containsDate> $dateNode and 
            $dateNode <http://mulgara.org/mulgara/tool/DateExtractor#month> '@@month@@' and 
            $dateNode <http://mulgara.org/mulgara/tool/DateExtractor#year> '@@year@@';
            ]]>
          </mulgaraDescriptor:query>
        </xsl:variable>
        <!-- Now apply the templates to the answer -->
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>
        </desc:component>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- assemble controller and content -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:answer">
    <desc:controller>
      <form action="@@DESCRIPTOR_SERVLET@@">
        <input type="hidden" name="_target" value="{$parentController}"/>
        <input type="hidden" name="parentController" value="{$parentController}"/>
        <input type="hidden" name="model" value="{$model}"/>
        <select name="month">
          <option value="{$month}" selected="true"><xsl:value-of select="$month"/></option>
          <option value="January">January</option>
          <option value="February">February</option>
          <option value="March">March</option>
          <option value="April">April</option>
          <option value="May">May</option>
          <option value="June">June</option>
          <option value="July">July</option>
          <option value="August">August</option>
          <option value="September">September</option>
          <option value="October">October</option>
          <option value="November">November</option>
          <option value="December">December</option>
        </select>
        <select name="year">
          <option value="{$year}" selected="true"><xsl:value-of select="$year"/></option>
          <option value="2000">2000</option>
          <option value="1999">1999</option>
          <option value="1998">1998</option>
          <option value="1997">1997</option>
          <option value="1996">1996</option>
          <option value="1995">1995</option>
          <option value="1994">1994</option>
          <option value="1993">1993</option>
        </select>
        <input type="submit"/>
      </form>
    </desc:controller>
    <desc:content>
    <ol>
    <xsl:apply-templates/>
    </ol>
    </desc:content>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- Make the title list -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:solution">
      <xsl:if test="mulgaraAnswer:title">
        <li>
        <a>
        <xsl:attribute name="href">
        <xsl:value-of select="mulgaraAnswer:url/@resource"/>
        </xsl:attribute>
        <xsl:value-of select="mulgaraAnswer:title/text()"/>
        </a>
        </li>
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

        <dc:title>Extracts Titles and URLs of documents containing a certain date store</dc:title>

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
