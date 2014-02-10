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
  <xsl:param name="document"/>
  <xsl:param name="model"/>
  <xsl:param name="predicate"/>

  <!-- ============================================== -->
  <!-- Match the Solution -->
  <!-- ============================================== -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$_usage">
        <xsl:call-template name="usage"/>
      </xsl:when>
      <xsl:otherwise>
        <!--
          <debug>Searching node...<xsl:value-of select="$node"/> in model <xsl:value-of select="$model"/></debug>
          -->
        <!-- EXECUTE THE QUERY to get a date -->
        <xsl:variable name="answer">
          <mulgaraDescriptor:query model="{$model}" document="{$document}" predicate="{$predicate}">
            <![CDATA[
              select $address $addressNumber $userId $node
                subquery(
                  select $personal
                    from <@@model@@>
                    where ( $node <http://mulgara.org/mulgara/EmailAddress#personal> $personal ) )
                subquery(
                  select $domain
                    from <@@model@@>
                    where ( $node <http://mulgara.org/mulgara/EmailAddress#domain> $domain ) )
                count( select $docURI
                  from <@@model@@>
                  where (
                    ( $docURI <@@predicate@@> $addressNode ) and
                    ( $addressNode <http://mulgara.org/mulgara/EmailAddress#address> $address ) and
                    ( $addressNode <http://mulgara.org/mulgara/EmailAddress#addressNumber> $addressNumber ) and
                    ( $addressNode <http://mulgara.org/mulgara/EmailAddress#userId> $userId ) ) )
              from <@@model@@>
              where
                ( <@@document@@> <@@predicate@@> $node ) and
                ( $node <http://mulgara.org/mulgara/EmailAddress#address> $address ) and
                ( $node <http://mulgara.org/mulgara/EmailAddress#addressNumber> $addressNumber ) and
                ( $node <http://mulgara.org/mulgara/EmailAddress#userId> $userId ) ;
            ]]>
          </mulgaraDescriptor:query>
        </xsl:variable>
        <!-- Now apply the templates to the answer -->
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>
        <!--<xsl:copy-of select="xalan:nodeset($answer)/*"/>-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- Addresses grouping element -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:answer">
    <desc:property desc:predicate="{$predicate}">
    <xsl:apply-templates/>
    </desc:property>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- Converts dates to nice XML format -->
  <!-- #################################################################### -->
  <xsl:template match="mulgaraAnswer:solution">
    <xsl:if test="mulgaraAnswer:address|mulgaraAnswer:personal|mulgaraAnswer:addressNumber|mulgaraAnswer:domain|mulgaraAnswer:userId">
      <desc:emailAddress desc:count="{mulgaraAnswer:k2/text()}">
      <xsl:if test="mulgaraAnswer:address">
        <desc:address><xsl:value-of select="mulgaraAnswer:address/text()"/></desc:address>
      </xsl:if>
      <xsl:if test="mulgaraAnswer:k0/mulgaraAnswer:solution/mulgaraAnswer:personal">
        <desc:personal><xsl:value-of select="mulgaraAnswer:k0/mulgaraAnswer:solution/mulgaraAnswer:personal/text()"/></desc:personal>
      </xsl:if>
      <xsl:if test="mulgaraAnswer:addressNumber">
        <desc:addressNumber><xsl:value-of select="mulgaraAnswer:addressNumber/text()"/></desc:addressNumber>
      </xsl:if>
      <xsl:if test="mulgaraAnswer:k1/mulgaraAnswer:solution/mulgaraAnswer:domain">
        <desc:domain><xsl:value-of select="mulgaraAnswer:domain/text()"/></desc:domain>
      </xsl:if>
      <xsl:if test="mulgaraAnswer:userId">
        <desc:userId><xsl:value-of select="mulgaraAnswer:userId/text()"/></desc:userId>
      </xsl:if>
      </desc:emailAddress>
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

        <dc:title>Extracts email Addresses from store</dc:title>

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

        <desc:hasParam>
          <desc:Param>
            <desc:name>predicate</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The predicate to query on the document to find Address Types</desc:description>
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
