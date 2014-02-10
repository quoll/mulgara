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

        <html>
        <xsl:variable name="answer">
         <mulgaraDescriptor:descriptor
           _target="vcardlist.xsl"
           _source="{$_self}"
           model="{$model}"/>
        </xsl:variable>
       
        <!-- Now apply the templates to the answer -->
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>

        <!--
        <xmp>
        <xsl:copy-of select="xalan:nodeset($answer)/*"/>
        </xmp>
        -->

        </html>

      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- converts models into a HTML List Element  -->
  <!-- #################################################################### -->
  <xsl:template match="desc:vcards">
    <html>
      <title>List of VCards</title>
      <body>

        <div align="center">
        <h2>VCards</h2>
        <i>model <xsl:value-of select="$model"/></i>

        <p/>
        <!-- show button to insert vcard -->
        <form action="execute">
           <!-- descriptor -->
           <input type="hidden" name="_self" value="vcardinserthtml.xsl"/>
           <input type="hidden" name="_source" value="{$_self}"/>
           <input type="hidden" name="model" value="{$model}"/>
           <input type="submit" value="add New VCard"/>
        </form>

        </div>


      <xsl:apply-templates/>


      </body>
    </html>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- converts vcard into a HTML Table  -->
  <!-- #################################################################### -->
  <xsl:template match="desc:vcard">
    <table border="0" width="100%">
      <tr>
        <td colspan="2">

         <!-- get HTML for this VCard -->
         <mulgaraDescriptor:descriptor
           _target="vcardhtml.xsl"
           _source="{$_self}"
           uri="{@resource}"
           model="{$model}"/>
        
        </td>
      </tr>
      <xsl:apply-templates/>
      <th colspan="2" align="center">
      <!-- call descriptor to produce real VCard --> 
      <form action="execute">
        <input type="hidden" name="_self" value="vcard.xsl"/>
        <input type="hidden" name="_source" value="{$_self}"/>
        <input type="hidden" name="uri" value="{@resource}"/>
        <input type="hidden" name="model" value="{$model}"/>
        <input type="submit" value="export VCard"/>
      </form>
      
      </th>
      </table>
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

        <dc:title>Given a Model show all VCards in that model as HTML</dc:title>

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
    
  

