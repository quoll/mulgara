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

        <!-- store query answer in a variable called answer -->
        <xsl:variable name="answer">
          <!-- Query for list of VCards in server -->
					<mulgaraDescriptor:query uri="{$uri}" model="{$model}">
            <![CDATA[
            select $FN $TITLE $ROLE $Street $Locality $Pcode $Country
            $EMAIL $NOTE $TEL
            from <@@model@@> where 
            <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#FN> $FN and
            <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#TITLE> $TITLE and
            <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#ROLE> $ROLE and
            <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#ADR> $ADR and
            $ADR <http://www.w3.org/2001/vcard-rdf/3.0#Street> $Street and
            $ADR <http://www.w3.org/2001/vcard-rdf/3.0#Locality> $Locality and
            $ADR <http://www.w3.org/2001/vcard-rdf/3.0#Pcode> $Pcode and
            $ADR <http://www.w3.org/2001/vcard-rdf/3.0#Country> $Country and 
            <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#EMAIL> $emailNode and
            $emailNode <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> $EMAIL and
            <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#NOTE> $NOTE and
            <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#TEL> $telNode and
            $telNode <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> $TEL ;
              
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
    <desc:vcard uri="{$uri}">
      <FN><xsl:value-of select="mulgaraAnswer:FN/text()"/></FN>
      <TITLE><xsl:value-of select="mulgaraAnswer:TITLE/text()"/></TITLE>
      <ROLE><xsl:value-of select="mulgaraAnswer:ROLE/text()"/></ROLE>
      <ADR>
        <Street><xsl:value-of select="mulgaraAnswer:Street/text()"/></Street>
        <Locality><xsl:value-of select="mulgaraAnswer:Locality/text()"/></Locality>
        <Pcode><xsl:value-of select="mulgaraAnswer:Pcode/text()"/></Pcode>
        <Country><xsl:value-of select="mulgaraAnswer:Country/text()"/></Country>
      </ADR>
      <EMAIL><xsl:value-of select="mulgaraAnswer:EMAIL/text()"/></EMAIL>
      <NOTE><xsl:value-of select="mulgaraAnswer:NOTE/text()"/></NOTE>
      <TEL><xsl:value-of select="mulgaraAnswer:TEL/text()"/></TEL>
    </desc:vcard>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- Calls a java class for Mulgara queries -->
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
<desc:description>Model where VCard RDF is  stored</desc:description>
</desc:Param>
</desc:hasParam> 
        
        <!-- Parameter 2 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#" xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>uri</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>URI (rdf subject) of a VCard in a model</desc:description>
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
    
  

