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
  <xsl:param name="_usage"/> <!-- if set return the USAGE RDF -->
  <xsl:param name="_self"/> <!-- the URL of this descriptor -->

  <!-- USER PARAMS -->
  <xsl:param name="document"/>
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
        <!-- EXECUTE THE QUERY to get leximancer concepts -->
        <xsl:variable name="answer">
          <mulgaraDescriptor:query model="{$model}" document="{$document}">
            <![CDATA[
            select $entity
              count (
                select $documentURI from <@@model@@>
                  where
                    $sNew <http://mulgara.org/mulgara/Document#Document> $documentURI and
                    $sNew <http://mulgara.org/mulgara/tool/Leximancer#relatedToValues> $conceptNode and
                    ($conceptNode $predicate $entity) )
              from
                <@@model@@>
              where
                $s <http://mulgara.org/mulgara/Document#Document> <@@document@@> and
                $s <http://mulgara.org/mulgara/tool/Leximancer#relatedToValues> $c and
                ($c <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> $entity or
                $c <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> $entity) ;
            ]]>
            <!--[CDATA[
            select $concept $entity 
            count ( 
            select $conceptNode from <@@model@@> where 
            ($conceptNode $predicate $entity) 
            )
            from <@@model@@> where $s <http://mulgara.org/mulgara/Document#Document> <@@document@@> and $s <http://mulgara.org/mulgara/tool/Leximancer#relatedToValues> $c and ($c <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> $entity or $c <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> $entity) and $entity <http://mulgara.org/mulgara/tool/Leximancer#name> $concept ;
            ]]-->
          </mulgaraDescriptor:query>
        </xsl:variable>
        <!-- Now apply the templates to the answer -->
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- CONCEPTS converts concept to nice XML format -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:answer">
    <desc:property desc:predicate="http://mulgara.org/mulgara/Document#hasConcept">
    <xsl:apply-templates/>
    </desc:property>
  </xsl:template>

  <!-- #################################################################### -->
  <!-- CONCEPTS converts concept to nice XML format -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:solution">
    <xsl:if test="mulgaraAnswer:entity">
      <desc:concept desc:count="{mulgaraAnswer:k0/text()}"><xsl:value-of select="substring-after(mulgaraAnswer:entity/@resource, '#')"/></desc:concept>
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

        <dc:title>
          Extracts concept-based concepts from a single document in a store, such as the ones produced 
          by the Leximancer tool.
        </dc:title>

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
            <desc:name>document</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URI of the document</desc:description>
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
            <desc:mime-minor>xml</desc:mime-minor>
          </desc:Mimetype>
        </desc:hasMimetype>

      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
