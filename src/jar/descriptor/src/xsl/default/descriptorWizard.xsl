<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  version="1.0"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:lxslt="http://xml.apache.org/xslt"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mulgaraDescriptor="mulgaraDescriptor"
  extension-element-prefixes="mulgaraDescriptor"
  exclude-result-prefixes="xsl rdf rdfs lxslt mulgaraAnswer xalan ns1"
  xmlns:mulgaraAnswer="http://mulgara.org/tql#"
  xmlns:desc="http://mulgara.org/descriptor#"
  xmlns:ns1="urn:Query">

  <xsl:output indent="yes"/>

  <!-- ============================================== -->
  <!-- Assemble the parameters as used in the query  -->
  <!-- ============================================== -->
  <!-- DESCRIPTOR PARAMS -->
  <xsl:param name="_usage"/> <!-- is set return the USAGE RDF -->
  <xsl:param name="_self"/>

  <!-- USER PARAMS -->

  <xsl:param name="template-mime-major" select="'text'"/>
  <xsl:param name="template-mime-minor" select="'xml'"/>
  <xsl:param name="template-title"/>  <!-- required -->

  <!-- 1 -->
  <xsl:param name="template-name1"/><!-- required -->
  <xsl:param name="template-type1" select="'String'"/><!-- required -->
  <xsl:param name="template-required1" select="'Yes'"/>
  <xsl:param name="template-description1"/><!-- required -->

  <!-- 2 -->
  <xsl:param name="template-name2"/><!-- required -->
  <xsl:param name="template-type2" select="'String'"/><!-- required -->
  <xsl:param name="template-required2" select="'Yes'"/>
  <xsl:param name="template-description2"/><!-- required -->
  <!-- 3 -->
  <xsl:param name="template-name3"/><!-- required -->
  <xsl:param name="template-type3" select="'String'"/><!-- required -->
  <xsl:param name="template-required3" select="'Yes'"/>
  <xsl:param name="template-description3"/><!-- required -->
  <!-- 4 -->
  <xsl:param name="template-name4"/><!-- required -->
  <xsl:param name="template-type4" select="'String'"/><!-- required -->
  <xsl:param name="template-required4" select="'Yes'"/>
  <xsl:param name="template-description4"/><!-- required -->
  <!-- 5 -->
  <xsl:param name="template-name5"/><!-- required -->
  <xsl:param name="template-type5" select="'String'"/><!-- required -->
  <xsl:param name="template-required5" select="'Yes'"/>
  <xsl:param name="template-description5"/><!-- required -->

  <!-- ============================================== -->
  <!-- Match the Solution -->
  <!-- ============================================== -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$_usage">
        <xsl:call-template name="usage"/>
      </xsl:when>
      <!-- validation -->

      <xsl:when test=
        "($template-title and $template-mime-major and $template-mime-minor)">
        <xsl:call-template name="generate-template"/>
      </xsl:when>
      <!--
      <xsl:when test=
        "(
        ($template-name1 and $template-type1 and $template-required1 and $template-description1) 
        or ($template-name2 and $template-type2 and $template-required2 and $template-description2) 
        or ($template-name3 and $template-type3 and $template-required3 and $template-description3) 
        or ($template-name4 and $template-type4 and $template-required4 and $template-description4) 
        or ($template-name5 and $template-type5 and $template-required5 and $template-description5) 
        )
        and 
        ($template-title and $template-mime-major and $template-mime-minor)">
        <xsl:call-template name="generate-template"/>
      </xsl:when>
      -->
      <xsl:otherwise>

    <html>
    <head>
    <title>Descriptor Wizard</title>
    <link media="all" href="/all.css" type="text/css" title="Burnt" rel="stylesheet"/>
    <link media="screen" href="/burnt.css" type="text/css" title="Burnt" rel="stylesheet"/>
    <link href="/grey.css" title="Grey" media="screen" type="text/css" rel="alternate stylesheet"/>
    </head>

    <body>
    <div id="container">

    <!-- Banner -->
    <div id="banner">
      <h1>mulgara.sourceforge.net</h1>
    </div>

    <div id="content">
    <div id="breadcrumb">
      [ Location:
      <ul>
        <li> <a href="/webservices/descriptor/" title="descriptors">descriptors</a> </li>
        <ul>
          <li>descriptor wizard</li>
        </ul>
      </ul>  ]
    </div>  


    <!-- first get the title -->
    <h1>Descriptor Wizard</h1>

    <form action="execute">

    <!-- hidden call back -->
    <input name="_self" type="hidden" value="{$_self}" />

    <!-- Description -->
    <h2>Title</h2>
    <div style="padding-left:15px">
    <b>Required</b><p/>

    Enter a title for this Descriptor.  It should describe exactly what this descriptor
    does. <p/>
    <input name="template-title" type="text" value="{$template-title}" size="60"/>

    </div>
    
    <h2>Parameters</h2>
    <div style="padding-left:15px">
    <b>Optional</b><p/>

    <!-- Parameters -->
    Enter Parameters for this Descriptor, if any, explain what the parameters mean.
    <p/>

    <!-- now set up params -->
    <table border="0" cellpadding="3">

    <tr>
      <td align="center"><b>Name</b></td>
      <td align="center"><b>Type</b></td>
      <td align="center"><b>Required</b></td>
      <td align="center"><b>Description</b></td>
    </tr>

    <!-- *********** -->
    <!-- parameter 1 -->
    <!-- *********** -->
    <tr>
      <td>
        <!-- name -->
        <input name="template-name1" type="text" value="{$template-name1}"/>
      </td>
      <td>
        <!-- type -->
        <select name="template-type1">
          <option><xsl:value-of select="$template-type1"/></option>
          <option>String</option>
        </select>
      </td>
      <td>
        <!-- required -->
        <select name="template-required">
          <option><xsl:value-of select="$template-required1"/></option>
          <option>No</option>
          <option>Yes</option>
        </select>
      </td>
      <td>
        <!-- description -->
        <input name="template-description1" type="text" value="{$template-description1}" size="30"/>
      </td>
    </tr>

    <!-- *********** -->
    <!-- parameter 2 -->
    <!-- *********** -->
    <tr>
      <td>
        <!-- name -->
        <input name="template-name2" type="text" value="{$template-name2}"/>
      </td>
      <td>
        <!-- type -->
        <select name="template-type2">
          <option><xsl:value-of select="$template-type2"/></option>
          <option>String</option>
        </select>
      </td>
      <td>
        <!-- required -->
        <select name="template-required">
          <option><xsl:value-of select="$template-required2"/></option>
          <option>No</option>
          <option>Yes</option>
        </select>
      </td>
      <td>
        <!-- description -->
        <input name="template-description2" type="text" value="{$template-description2}" size="30"/>
      </td>
    </tr>


    <!-- *********** -->
    <!-- parameter 3 -->
    <!-- *********** -->
    <tr>
      <td>
        <!-- name -->
        <input name="template-name3" type="text" value="{$template-name3}"/>
      </td>
      <td>
        <!-- type -->
        <select name="template-type3">
          <option><xsl:value-of select="$template-type3"/></option>
          <option>String</option>
        </select>
      </td>
      <td>
        <!-- required -->
        <select name="template-required">
          <option><xsl:value-of select="$template-required3"/></option>
          <option>No</option>
          <option>Yes</option>
        </select>
      </td>
      <td>
        <!-- description -->
        <input name="template-description3" type="text" value="{$template-description3}" size="30"/>
      </td>
    </tr>


    <!-- *********** -->
    <!-- parameter 4 -->
    <!-- *********** -->
    <tr>
      <td>
        <!-- name -->
        <input name="template-name4" type="text" value="{$template-name4}"/>
      </td>
      <td>
        <!-- type -->
        <select name="template-type4">
          <option><xsl:value-of select="$template-type4"/></option>
          <option>String</option>
        </select>
      </td>
      <td>
        <!-- required -->
        <select name="template-required">
          <option><xsl:value-of select="$template-required4"/></option>
          <option>No</option>
          <option>Yes</option>
        </select>
      </td>
      <td>
        <!-- description -->
        <input name="template-description4" type="text" value="{$template-description4}" size="30"/>
      </td>
    </tr>

    <!-- *********** -->
    <!-- parameter 5 -->
    <!-- *********** -->
    <tr>
      <td>
        <!-- name -->
        <input name="template-name5" type="text" value="{$template-name5}"/>
      </td>
      <td>
        <!-- type -->
        <select name="template-type5">
          <option><xsl:value-of select="$template-type5"/></option>
          <option>String</option>
        </select>
      </td>
      <td>
        <!-- required -->
        <select name="template-required">
          <option><xsl:value-of select="$template-required5"/></option>
          <option>No</option>
          <option>Yes</option>
        </select>
      </td>
      <td>
        <!-- description -->
        <input name="template-description5" type="text" value="{$template-description5}" size="30"/>
      </td>
    </tr>


    </table>
    
    </div>

    <!-- now enter mime type -->
    <h2>Mime type</h2>
    <div style="padding-left:55px">
    <b>Optional</b><p/>

    If known, enter the Mime type of data returned 

    <p/>
    <table border="0" cellpadding="3">

    <tr>
      <td>Major Type</td>
      <td>Minor Type</td>
    </tr>

    <tr>
      <td>
        <!-- mime major -->
        <select name="template-mime-major">
          <option><xsl:value-of select="$template-mime-major"/></option>
          <option>text</option>
          <option>application</option>
          <option>unknown</option>
        </select>
      </td>

      <td>
        <!-- mime minor - TODO constrain below on above -->
        <select name="template-mime-minor">
          <option><xsl:value-of select="$template-mime-minor"/></option>
          <option>xml</option>
          <option>html</option>
          <option>plain</option>
          <option>x-vcard</option>
          <option>csv</option>
          <option>unknown</option>
        </select>
      </td>

    </tr>

    </table>
    </div>



    <div style="padding-left:15px">

    <!-- submit -->
    <table border="0" cellpadding="3">

    <tr>
      <td colspan="3" align="center">
      <input type="submit" value="Generate Descriptor Template"/>
      </td>
    </tr>

    </table>
    </div>

    </form>



    </div></div>
    </body>
    </html>



      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <!-- #################################################################### -->
  <!-- generate template descriptor                                         -->
  <!-- #################################################################### -->
  <xsl:template name="generate-template">
<xsl:text disable-output-escaping="yes">
<![CDATA[
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
  ]]>
</xsl:text>

<!-- inserting template parameters -->
<!-- 1 -->
<xsl:choose>
  <xsl:when test="$template-name1">
    <xsl:text disable-output-escaping="yes"><![CDATA[<xsl:param name="]]></xsl:text><xsl:value-of select="$template-name1"/><xsl:text disable-output-escaping="yes"><![CDATA["/>
]]></xsl:text>
  </xsl:when>
</xsl:choose>

<!-- 2 -->
<xsl:choose>
  <xsl:when test="$template-name2">
    <xsl:text disable-output-escaping="yes"><![CDATA[<xsl:param name="]]></xsl:text><xsl:value-of select="$template-name2"/><xsl:text disable-output-escaping="yes"><![CDATA["/>
]]></xsl:text>
  </xsl:when>
</xsl:choose>

<!-- 3 -->
<xsl:choose>
  <xsl:when test="$template-name3">
    <xsl:text disable-output-escaping="yes"><![CDATA[<xsl:param name="]]></xsl:text><xsl:value-of select="$template-name3"/><xsl:text disable-output-escaping="yes"><![CDATA["/>
]]></xsl:text>
  </xsl:when>
</xsl:choose>

<!-- 4 -->
<xsl:choose>
  <xsl:when test="$template-name4">
    <xsl:text disable-output-escaping="yes"><![CDATA[<xsl:param name="]]></xsl:text><xsl:value-of select="$template-name4"/><xsl:text disable-output-escaping="yes"><![CDATA["/>
]]></xsl:text>
  </xsl:when>
</xsl:choose>

<!-- 5 -->
<xsl:choose>
  <xsl:when test="$template-name5">
    <xsl:text disable-output-escaping="yes"><![CDATA[<xsl:param name="]]></xsl:text><xsl:value-of select="$template-name5"/><xsl:text disable-output-escaping="yes"><![CDATA["/>
]]></xsl:text>
  </xsl:when>
</xsl:choose>


<xsl:text disable-output-escaping="yes">
  <![CDATA[
  <!-- ============================================== -->
  <!-- Match the Solution -->
  <!-- ============================================== -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$_usage">
        <xsl:call-template name="usage"/>
      </xsl:when>
      <xsl:otherwise>

        <!-- INSERT DESCRIPTOR LOGIC HERE SUCH AS QUERIES, TRANSFORMATIONS AND
             CALLING OTHER DESCRIPTORS 
        -->

      </xsl:otherwise>
    </xsl:choose>
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

        <dc:title>]]></xsl:text><xsl:value-of select="$template-title"/><xsl:text disable-output-escaping="yes"><![CDATA[</dc:title>

        <desc:hasParam>
          <desc:Param>
            <desc:name>_self</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URL of this Descriptor</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        ]]></xsl:text>
        <!-- write out user supplied parameters -->

        <!-- 1 -->
        <xsl:choose>
          <xsl:when test="string-length($template-name1) > 0">
        <!-- write out mime type info if any -->
        <xsl:text disable-output-escaping="yes"><![CDATA[ 
        
        <!-- Parameter 1 -->

        ]]></xsl:text>
        <desc:hasParam>
          <desc:Param>
            <desc:name><xsl:value-of select="$template-name1"/></desc:name>
            <desc:type><xsl:value-of select="$template-type1"/></desc:type>
            <desc:required><xsl:value-of select="$template-required1"/></desc:required>
            <desc:description><xsl:value-of select="$template-description1"/></desc:description>
          </desc:Param>
        </desc:hasParam>
        </xsl:when>
        </xsl:choose>

        <!-- 2 -->
        <xsl:choose>
          <xsl:when test="string-length($template-name2) > 0">
        <xsl:text disable-output-escaping="yes"><![CDATA[ 
        
        <!-- Parameter 2 -->

        ]]></xsl:text>
        <desc:hasParam>
          <desc:Param>
            <desc:name><xsl:value-of select="$template-name2"/></desc:name>
            <desc:type><xsl:value-of select="$template-type2"/></desc:type>
            <desc:required><xsl:value-of select="$template-required2"/></desc:required>
            <desc:description><xsl:value-of select="$template-description2"/></desc:description>
          </desc:Param>
        </desc:hasParam>
        </xsl:when>
        </xsl:choose>

        <!-- 3 -->
        <xsl:choose>
          <xsl:when test="string-length($template-name3) > 0">
        <xsl:text disable-output-escaping="yes"><![CDATA[ 
        
        <!-- Parameter 3 -->

        ]]></xsl:text>
        <desc:hasParam>
          <desc:Param>
            <desc:name><xsl:value-of select="$template-name3"/></desc:name>
            <desc:type><xsl:value-of select="$template-type3"/></desc:type>
            <desc:required><xsl:value-of select="$template-required3"/></desc:required>
            <desc:description><xsl:value-of select="$template-description3"/></desc:description>
          </desc:Param>
        </desc:hasParam>
        </xsl:when>
        </xsl:choose>

        <!-- 4 -->
        <xsl:choose>
          <xsl:when test="string-length($template-name4) > 0">
        <xsl:text disable-output-escaping="yes"><![CDATA[ 
        
        <!-- Parameter 4 -->

        ]]></xsl:text>
        <desc:hasParam>
          <desc:Param>
            <desc:name><xsl:value-of select="$template-name4"/></desc:name>
            <desc:type><xsl:value-of select="$template-type4"/></desc:type>
            <desc:required><xsl:value-of select="$template-required4"/></desc:required>
            <desc:description><xsl:value-of select="$template-description4"/></desc:description>
          </desc:Param>
        </desc:hasParam>
        </xsl:when>
        </xsl:choose>

        <!-- 5 -->
        <xsl:choose>
          <xsl:when test="string-length($template-name5) > 0">
        <xsl:text disable-output-escaping="yes"><![CDATA[ 
        
        <!-- Parameter 5 -->

        ]]></xsl:text>
        <desc:hasParam>
          <desc:Param>
            <desc:name><xsl:value-of select="$template-name5"/></desc:name>
            <desc:type><xsl:value-of select="$template-type5"/></desc:type>
            <desc:required><xsl:value-of select="$template-required5"/></desc:required>
            <desc:description><xsl:value-of select="$template-description5"/></desc:description>
          </desc:Param>
        </desc:hasParam>
        </xsl:when>
        </xsl:choose>


        <!-- write out mime type info if any -->
        <xsl:text disable-output-escaping="yes"><![CDATA[ 
        
        <!-- MIME TYPE -->

        ]]></xsl:text>

        <desc:hasMimetype>
          <desc:Mimetype>
            <desc:mime-major><xsl:value-of select="$template-mime-major"/></desc:mime-major>
            <desc:mime-minor><xsl:value-of select="$template-mime-minor"/></desc:mime-minor>
          </desc:Mimetype>
        </desc:hasMimetype>

<xsl:text disable-output-escaping="yes">
        <![CDATA[
      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
    ]]>
  </xsl:text>
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

      <!-- Descriptor metadata -->
      <desc:Descriptor rdf:about="">

        <dc:title>Web application to create Descriptor Templates</dc:title>

        <desc:hasParam>
          <desc:Param>
            <desc:name>_self</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URL of this Descriptor</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-title</desc:name>
            <desc:type>String</desc:type>
            <desc:description>title of descriptor to create</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-mime-major</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptors return this mime major type </desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-mime-minor</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptors return this mime minor type </desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- 1 -->
        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-name1</desc:name>
            <desc:type>String</desc:type>
            <desc:description>name of descriptor parameter 1 to create</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-type1</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptor parameter 1 data type</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-required1</desc:name>
            <desc:type>String</desc:type>
            <desc:description>is descriptor parameter 1 required</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-description1</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptor parameter 1 description</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>


        <!-- 2 -->
        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-name2</desc:name>
            <desc:type>String</desc:type>
            <desc:description>name of descriptor parameter 2 to create</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-type2</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptor parameter 2 data type</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-required2</desc:name>
            <desc:type>String</desc:type>
            <desc:description>is descriptor parameter 2 required</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-description2</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptor parameter 2 description</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>


        <!-- 3 -->
        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-name3</desc:name>
            <desc:type>String</desc:type>
            <desc:description>name of descriptor parameter 3 to create</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-type3</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptor parameter 3 data type</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-required3</desc:name>
            <desc:type>String</desc:type>
            <desc:description>is descriptor parameter 3 required</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-description3</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptor parameter 3 description</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>


        <!-- 4 -->
        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-name4</desc:name>
            <desc:type>String</desc:type>
            <desc:description>name of descriptor parameter 4 to create</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-type4</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptor parameter 4 data type</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-required4</desc:name>
            <desc:type>String</desc:type>
            <desc:description>is descriptor parameter 4 required</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-description4</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptor parameter 4 description</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>


        <!-- 5 -->
        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-name5</desc:name>
            <desc:type>String</desc:type>
            <desc:description>name of descriptor parameter 5 to create</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-type5</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptor parameter 5 data type</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-required5</desc:name>
            <desc:type>String</desc:type>
            <desc:description>is descriptor parameter 5 required</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- template parameter -->
        <desc:hasParam>
          <desc:Param>
            <desc:name>template-description5</desc:name>
            <desc:type>String</desc:type>
            <desc:description>descriptor parameter 5 description</desc:description>
            <desc:required>No</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- MIME TYPE -->
        <desc:hasMimetype>
          <desc:Mimetype>
            <desc:mime-major>unknown</desc:mime-major>
            <desc:mime-minor>unknown</desc:mime-minor>
          </desc:Mimetype>
        </desc:hasMimetype>

      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
