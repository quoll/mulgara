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
  exclude-result-prefixes="xsl rdf rdfs lxslt mulgara xalan ns1"
  xmlns:mulgara="http://mulgara.org/tql#"
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
<xsl:param name="fullName"/>
<xsl:param name="title" select="' '"/>
<xsl:param name="role" select="' '"/>
<xsl:param name="street" select="' '"/>
<xsl:param name="locality" select="' '"/>
<xsl:param name="pcode" select="' '"/>
<xsl:param name="country" select="' '"/>
<xsl:param name="email"/>
<xsl:param name="note" select="' '"/>
<xsl:param name="telephone" select="' '"/>

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
         <mulgaraDescriptor:query model="{$model}"
                              uri="{$uri}"
                              fullName="{$fullName}"
                              title="{$title}"
                              role="{$role}"
                              street="{$street}"
                              locality="{$locality}"
                              pcode="{$pcode}"
                              country="{$country}"
                              email="{$email}"
                              note="{$note}"
                              telephone="{$telephone}" >
            <![CDATA[
 insert <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#FN>  '@@fullName@@'
         <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#TITLE> '@@title@@'
         <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#ROLE> '@@role@@'
         <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#ADR> $ADR
         $ADR <http://www.w3.org/2001/vcard-rdf/3.0#Street> '@@street@@'
         $ADR <http://www.w3.org/2001/vcard-rdf/3.0#Locality> '@@locality@@'
         $ADR <http://www.w3.org/2001/vcard-rdf/3.0#Pcode> '@@pcode@@'
         $ADR <http://www.w3.org/2001/vcard-rdf/3.0#Country> '@@country@@'
         <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#EMAIL> $email
         $email <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> '@@email@@'
         <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#NOTE> '@@note@@'
         <@@uri@@> <http://www.w3.org/2001/vcard-rdf/3.0#TEL> $tel
         $tel <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> '@@telephone@@'  into <@@model@@> ;
            ]]>
          </mulgaraDescriptor:query>
        </xsl:variable>
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>

      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- Pass title to other descriptor                                       -->
  <!-- #################################################################### -->
  <xsl:template match="mulgara:answer">
    <html>
    <head>
    <title>Mulgara answer</title>
    </head>
    <body>
      <h2>Mulgara response</h2>
      <xsl:value-of select="mulgara:query/mulgara:message/text()"/>
      <p/>

      <!-- show button to insert vcard -->
      <div align="center">
      <form action="execute">
         <!-- descriptor -->
         <input type="hidden" name="_self" value="vcardlisthtml.xsl"/>
         <input type="hidden" name="_source" value="{$_self}"/>
         <input type="hidden" name="model" value="{$model}"/>
         <input type="submit" value="Show all VCards"/>
      </form>
    </div>

    </body>
    </html>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- Calls a java class for Mulgara queries -->
  <!-- #################################################################### -->
  <lxslt:component prefix="mulgaraDescriptor" elements="descriptor query debug"
 functions="test">
    <lxslt:script lang="javaclass"
 src="xalan://org.mulgara.descriptor.DescriptorElement"/>
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

        <dc:title>Given VCard parameters insert a VCard into a Mulgara Store</dc:title>
        <desc:hasParam>
          <desc:Param>
            <desc:name>_self</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URL of this Descriptor</desc:description>
          </desc:Param>
        </desc:hasParam>



        <!-- Parameter 1 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#">
<desc:Param>
<desc:name>model</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>The model to store the vcard</desc:description>
</desc:Param>
</desc:hasParam>

   <!-- Parameter 2 -->


<desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>uri</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>URL for the vCard</desc:description>
</desc:Param>
</desc:hasParam>


<!-- Parameter 3 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>fullName</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>Full Name</desc:description>
</desc:Param>
</desc:hasParam>


<!-- Parameter 4 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>title</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Title</desc:description>
</desc:Param>
</desc:hasParam>

<!-- Parameter 5 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>role</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Role</desc:description>
</desc:Param>
</desc:hasParam>

<!-- Parameter 6 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>street</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Street</desc:description>
</desc:Param>
</desc:hasParam>


<!-- Parameter 7 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>locality</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Locality</desc:description>
</desc:Param>
</desc:hasParam>

<!-- Parameter 8 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>pcode</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Post code</desc:description>
</desc:Param>
</desc:hasParam>


<!-- Parameter 9 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>country</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>country</desc:description>
</desc:Param>
</desc:hasParam>

<!-- Parameter 10 -->


<desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>email</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>Email address</desc:description>
</desc:Param>
</desc:hasParam>

<!-- Parameter 11 -->

<desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>note</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Note</desc:description>
</desc:Param>
</desc:hasParam>


<!-- Parameter 12 -->

<desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Param>
<desc:name>telephone</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>telephone</desc:description>
</desc:Param>
</desc:hasParam>

<!-- MIME TYPE -->


<desc:hasMimetype xmlns:desc="http://mulgara.org/descriptor#"
 >
<desc:Mimetype>
<desc:mime-major>text</desc:mime-major>
<desc:mime-minor>html</desc:mime-minor>
</desc:Mimetype>
</desc:hasMimetype>

      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
